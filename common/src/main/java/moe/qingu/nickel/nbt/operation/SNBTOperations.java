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

package moe.qingu.nickel.nbt.operation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import moe.qingu.nickel.NickelAPI;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.node.parameter.generic.UUIDNode;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.LongStream;

/**
 * @author QGMoe
 */
@SuppressWarnings("unused")
public final class SNBTOperations {
    private static final Table<String, SNBTOperation.OperationType,SNBTOperation> functions = HashBasedTable.create();
    private static final Map<SNBTOperation,String> signatures = new HashMap<>();
    private static final MethodHandles.Lookup PERMISSION = MethodHandles.lookup();

    public static void register(final @Nonnull String name, final @Nonnull SNBTOperation.OperationType type, final @Nonnull SNBTOperation function){
        final String signature = name + type;
        if(signatures.containsValue(signature)){
            NickelAPI.LOGGER.error("Duplicated register for SNBTOperation {}",signature);
            return;
        }
        functions.put(name,type,function);
        signatures.put(function,signature);
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagByte bool(final @Nonnull NBTPrimitive num){
        return new NBTTagByte((byte) (num.getLong()==0?0:1));
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagIntArray uuid(final @Nonnull NBTTagString str) throws NickelRuntimeException {
        try {
            final UUID uuid = UUID.fromString(str.getString());
            final long most = uuid.getMostSignificantBits();
            final long least = uuid.getLeastSignificantBits();
            return new NBTTagIntArray(new int[]{
                    (int)(most >> Integer.SIZE),
                    (int)most,
                    (int)(least >> Integer.SIZE),
                    (int)least
            });
        }catch (final @Nonnull IllegalArgumentException e){
            throw new NickelRuntimeException(UUIDNode.buildUUIDFormatErrorInfo(e,str.getString()));
        }
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagCompound uuid(final @Nonnull NBTTagString key,final @Nonnull NBTTagString str) throws NickelRuntimeException {
        try {
            final UUID uuid = UUID.fromString(str.getString());
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setUniqueId(key.getString(),uuid);
            return compound;
        }catch (final @Nonnull IllegalArgumentException e){
            throw new NickelRuntimeException(UUIDNode.buildUUIDFormatErrorInfo(e,str.getString()));
        }
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagCompound concat(final @Nonnull NBTTagCompound a,final @Nonnull NBTTagCompound b){
        b.getKeySet().forEach(k -> a.setTag(k,b.getTag(k)));
        return a;
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagByteArray concat(final @Nonnull NBTTagByteArray a,final @Nonnull NBTTagByteArray b){
        final byte[] arrA = a.getByteArray();
        final byte[] arrB = b.getByteArray();
        final byte[] arr = new byte[arrA.length+arrB.length];
        System.arraycopy(arrA, 0, arr, 0, arrA.length);
        System.arraycopy(arrB, 0, arr, arrA.length, arrB.length);
        return new NBTTagByteArray(arr);
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagIntArray concat(final @Nonnull NBTTagIntArray a,final @Nonnull NBTTagIntArray b){
        final int[] arrA = a.getIntArray();
        final int[] arrB = b.getIntArray();
        final int[] arr = new int[arrA.length+arrB.length];
        System.arraycopy(arrA, 0, arr, 0, arrA.length);
        System.arraycopy(arrB, 0, arr, arrA.length, arrB.length);
        return new NBTTagIntArray(arr);
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagLongArray concat(final @Nonnull NBTTagLongArray a,final @Nonnull NBTTagLongArray b){
        return new NBTTagLongArray(LongStream.concat(NBTUtils.streamOf(a),NBTUtils.streamOf(b))
                .toArray());
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagList concat(final @Nonnull NBTTagList a,final @Nonnull NBTTagList b){
        final NBTTagList list = new NBTTagList();
        for(final NBTBase nbt:a) list.appendTag(nbt);
        for(final NBTBase nbt:b) list.appendTag(nbt);
        return list;
    }

    @Nonnull
    @SNBTFunction
    public static NBTTagString concat(final @Nonnull NBTTagString a,final @Nonnull NBTTagString b){
        return new NBTTagString(a.getString()+b.getString());
    }

    @Nullable
    public static SNBTOperation resolve(final @Nonnull String name, final @Nonnull NBTBase[] args){
        final @Nullable Map<SNBTOperation.OperationType,SNBTOperation> candidates = functions.row(name);
        if(candidates == null) return null;

        SNBTOperation bestOpt = null;
        int bestScore = Integer.MAX_VALUE;
        boolean ambiguous = false;
        outer:
        for(final Map.Entry<SNBTOperation.OperationType,SNBTOperation> entry:candidates.entrySet()){
            final SNBTOperation.OperationType type = entry.getKey();
            if(type.getParameterCount() != args.length) continue;

            int score = 0;
            for(int i=0;i<args.length;i++){
                if(args[i] == null) return null;
                final int dis = SNBTOperation.distance(args[i].getClass(),type.getInputTypeAt(i));
                if(dis <0 ) continue outer;
                score += dis;
            }

            if(score < bestScore){
                bestScore = score;
                ambiguous  =false;
                bestOpt = entry.getValue();
            } else if(score == bestScore) ambiguous = true;
        }
        if(ambiguous) return null;
        return bestOpt;
    }

    @Nonnull
    public static String signatureOf(final @Nonnull SNBTOperation operation){
        final String sign = signatures.get(operation);
        if(sign == null) throw new IllegalArgumentException();
        return sign;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static String signatureOf(final @Nonnull String name,final @Nonnull List<NBTBase> args){
        return name + new SNBTOperation.OperationType(args.stream()
                .map(Object::getClass)
                .toArray(Class[]::new));
    }

    @SuppressWarnings("unchecked")
    public static void loadFuncs(final @Nonnull Class<?> cls){
        final Method[] methods = cls.getDeclaredMethods();
        for(final Method method:methods){
            if(!Modifier.isStatic(method.getModifiers())) continue;
            if(!method.isAnnotationPresent(SNBTFunction.class)) continue;
            if(!Modifier.isPublic(method.getModifiers())){
                NickelAPI.LOGGER.warn("Skipped loading SNBT operation {} in class {}, because it is not public.",method,cls.getName());
                continue;
            }
            final SNBTFunction annotation = method.getAnnotation(SNBTFunction.class);
            final Class<?>[] paras = method.getParameterTypes();
            try{
                final MethodHandle handle = PERMISSION.unreflect(method)
                        .asSpreader(NBTBase[].class,paras.length)
                        .asType(MethodType.methodType(NBTBase.class,Object.class));
                final SNBTOperation.OperationType type = new SNBTOperation.OperationType((Class<? extends NBTBase>[]) paras);
                final String name = annotation.name().isEmpty()? method.getName(): annotation.name();

                @Nonnull
                final SNBTOperation func = args -> {
                    try {
                        return (NBTBase) handle.invokeExact((Object) args);
                    } catch (final NickelRuntimeException | RuntimeException e) {
                        throw e;
                    } catch (final Throwable t){
                        throw new RuntimeException(t);
                    }
                };
                register(name, type, func);
            } catch (final @Nonnull Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void scanProviders(final @Nonnull ASMDataTable table){
        NickelAPI.LOGGER.info("NickelAPI is scanning SNBT operation providers");
        table.getAll(SNBTFunction.class.getName())
                .stream()
                .map(ASMDataTable.ASMData::getClassName)
                .distinct()
                .forEach(e ->{
                    try {
                        loadFuncs(Class.forName(e));
                    } catch (final @Nonnull ClassNotFoundException exception) {
                        NickelAPI.LOGGER.warn("Couldn't process SNBT Operation provider {}",e);
                        NickelAPI.LOGGER.warn("Because:",exception);
                    }
                });
    }
}
