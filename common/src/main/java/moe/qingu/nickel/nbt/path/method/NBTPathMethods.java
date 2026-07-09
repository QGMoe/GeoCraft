/*
 * Copyright 2025 QiguaiAAAA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 版权所有 2025 QiguaiAAAA
 * 根据Apache许可证第2.0版（“本许可证”）许可；
 * 除非符合本许可证的规定，否则你不得使用此文件。
 * 你可以在此获取本许可证的副本：
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非所适用法律要求或经书面同意，在本许可证下分发的软件是“按原样”分发的，
 * 没有任何形式的担保或条件，不论明示或默示。
 * 请查阅本许可证了解有关本许可证下许可和限制的具体要求。
 * 中文译文来自开放原子开源基金会，非官方译文，如有疑议请以英文原文为准
 */

package moe.qingu.nickel.nbt.path.method;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import moe.qingu.nickel.NickelAPI;
import moe.qingu.nickel.nbt.NBTFunctionType;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author QGMoe
 */
public final class NBTPathMethods {
    private static final Table<String, NBTFunctionType,NBTPathMethod> methods = HashBasedTable.create();
    private static final Map<NBTPathMethod,String> signatures = new HashMap<>();
    private static final MethodHandles.Lookup PERMISSION = MethodHandles.lookup();

    private NBTPathMethods(){}

    @Nonnull
    @NBTPathFunction
    public static Collection<NBTBase> values(final @Nonnull NBTBase base){
        if(base instanceof NBTTagCompound){
            final NBTTagCompound compound = (NBTTagCompound) base;
            return compound.getKeySet().stream().map(compound::getTag).collect(Collectors.toSet());
        }else return Collections.emptyList();
    }

    public static void register(final @Nonnull String name, final @Nonnull NBTFunctionType type, final @Nonnull NBTPathMethod method){
        final String signature = name + type;
        if(signatures.containsValue(signature)){
            NickelAPI.LOGGER.error("Duplicated register for NBTPath method {}",signature);
            return;
        }
        methods.put(name,type,method);
        signatures.put(method,signature);
    }

    @Nullable
    public static NBTPathMethod resolve(final @Nonnull String name, final @Nonnull NBTBase[] args){
        final @Nullable Map<NBTFunctionType,NBTPathMethod> candidates = methods.row(name);
        if(candidates == null) return null;
        return NBTFunctionType.resolve(candidates,args);
    }

    @Nonnull
    public static String signatureOf(final @Nonnull NBTPathMethod method){
        final String sign = signatures.get(method);
        if(sign == null) throw new IllegalArgumentException();
        return sign;
    }

    @SuppressWarnings("unchecked")
    public static void loadFuncs(final @Nonnull Class<?> cls){
        final Method[] methods = cls.getDeclaredMethods();
        for(final Method method:methods){
            if(!Modifier.isStatic(method.getModifiers())) continue;
            if(!method.isAnnotationPresent(NBTPathFunction.class)) continue;
            if(!Modifier.isPublic(method.getModifiers())){
                NickelAPI.LOGGER.warn("Skipped loading NBTPath method {} in class {}, because it is not public.",method,cls.getName());
                continue;
            }
            final NBTPathFunction annotation = method.getAnnotation(NBTPathFunction.class);
            final Class<?>[] paras = method.getParameterTypes();
            if(paras.length == 0){
                NickelAPI.LOGGER.warn("NBTPathMethod must have at least a NBTBase parameter! Ignored method {} in class {}",method,cls.getName());
                continue;
            }else if(paras[0] != NBTBase.class){
                NickelAPI.LOGGER.warn("The first parameter of NBTPathMethod must be NBTbase! Ignored method {} in class {}",method,cls.getName());
                continue;
            }
            final Class<?>[] actualParas = new Class[paras.length-1];
            System.arraycopy(paras, 1, actualParas, 0, actualParas.length);
            try{
                final MethodHandle handle = PERMISSION.unreflect(method)
                        .asSpreader(NBTBase[].class,paras.length)
                        .asType(MethodType.methodType(Collection.class,Object.class));
                final NBTFunctionType type = new NBTFunctionType((Class<? extends NBTBase>[]) actualParas);
                final String name = annotation.name().isEmpty()? method.getName(): annotation.name();

                @Nonnull
                final NBTPathMethod func = args -> {
                    try {
                        return (Collection<NBTBase>) handle.invokeExact((Object) args);
                    }catch (final Throwable t){
                        NickelAPI.LOGGER.warn("Invoke {} failed:",name + type);
                        NickelAPI.LOGGER.warn(t);
                        return Collections.emptyList();
                    }
                };
                register(name, type, func);
            } catch (final @Nonnull Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void scanProviders(final @Nonnull ASMDataTable table){
        NickelAPI.LOGGER.info("NickelAPI is scanning NBTPath method providers");
        table.getAll(NBTPathFunction.class.getName())
                .stream()
                .map(ASMDataTable.ASMData::getClassName)
                .distinct()
                .forEach(e ->{
                    try {
                        loadFuncs(Class.forName(e));
                    } catch (final @Nonnull ClassNotFoundException exception) {
                        NickelAPI.LOGGER.warn("Couldn't process NBTPath method provider {}",e);
                        NickelAPI.LOGGER.warn("Because:",exception);
                    }
                });
    }
}
