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

package top.qiguaiaaaa.geocraft.compat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.handler.MixinHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @see top.qiguaiaaaa.geocraft.command.BrigoCompat
 * @author QiguaiAAAA
 */
public final class GeoCompatLoader {
    public static final String ENTRY_POINT = "init";
    private static final Map<String, String> compats = new HashMap<>();
    private static final Map<String, Method> caches = new HashMap<>();

    private GeoCompatLoader(){}

    static {
        final @Nonnull FluidPhysicsMode mode = FluidPhysicsMode.getCurrentMode();
        MixinHandler.FLUID_PHYSICS_TO_COMPATS[mode.ordinal()].stream()
                .filter(compat -> compat.compatClass != null && compat.getEnableCondition().getAsBoolean())
                .forEach(compat -> registerCompat(compat.modid,compat.compatClass));
    }

    public static void loadCompats(@Nonnull final LoaderState state){
        for(final @Nonnull Map.Entry<String,String> compat:compats.entrySet()){
            if(!Loader.isModLoaded(compat.getKey())) continue;
            final @Nullable Method entryPoint = caches.computeIfAbsent(compat.getKey(),k -> getEntryPoint(compat.getValue()));
            if(entryPoint == null) continue;
            try {
                entryPoint.invoke(null,state);
            } catch (@Nonnull final InvocationTargetException | IllegalAccessException e) {
                GeoCraft.getLogger().error("Error in invoking compat class {} for mod {}",compat.getValue(),compat.getKey());
                GeoCraft.getLogger().error("Details:",e);
            }
        }
    }

    public static void registerCompat(@Nonnull final String modid,@Nonnull final String compatCls){
        compats.put(Objects.requireNonNull(modid),Objects.requireNonNull(compatCls));
    }

    @Nullable
    private static Method getEntryPoint(@Nonnull final String compatCls){
        try {
            final @Nonnull Class<?> cls = Class.forName(compatCls);
            final @Nonnull Method method = cls.getMethod(ENTRY_POINT,LoaderState.class);
            if((method.getModifiers() & Modifier.STATIC) == 0){
                GeoCraft.getLogger().error("Entry point of compat class {} shouldn't be an instance method, but it was!",compatCls);
                return null;
            }
            return method;
        } catch (final @Nonnull ClassNotFoundException e) {
            GeoCraft.getLogger().error("Cannot load compat class {} for GeoCraft",compatCls);
            GeoCraft.getLogger().error("Exception:",e);
        } catch (final @Nonnull NoSuchMethodException e) {
            GeoCraft.getLogger().error("Cannot find entry point of compat class {} for GeoCraft",compatCls);
            GeoCraft.getLogger().error("Exception:",e);
        } catch (final @Nonnull SecurityException e){
            GeoCraft.getLogger().error("Security violation when accessing method in class {} for GeoCraft",compatCls);
            GeoCraft.getLogger().error("Exception:",e);
        }
        return null;
    }
}
