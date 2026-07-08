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

package top.qiguaiaaaa.geocraft.api.atmosphere;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereSystemEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气系统管理器，管理所有世界的大气系统
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class AtmosphereSystemManager {
    /**
     * 请使用{@link AtmosphereSystemEvent.Create}事件来向某个维度创建大气系统
     */
    static final BiMap<WorldServer, IAtmosphereSystem> atmosphereSystems = HashBiMap.create();

    /**
     * 获取某个世界的大气系统
     * @param world 世界，若不是{@link WorldServer}及其子类则始终返回null
     * @return 对应的大气系统，若不存在则返回null
     */
    @Nullable
    public static IAtmosphereSystem getAtmosphereSystem(@Nonnull World world){
        WorldServer server = getValidWorld(world);
        if(server == null) return null;
        return atmosphereSystems.get(server);
    }

    /**
     * 获取指定位置的大气访问器
     * @return 如果大气不存在或未初始化，则返回null。正常情况下返回大气访问器。
     */
    @Nullable
    public static IAtmosphereAccessor getAtmosphereAccessor(@Nonnull final World world,@Nonnull final BlockPos pos,final boolean notAir){
        IAtmosphereSystem system = getAtmosphereSystem(world);
        if(system == null) return null;
        return system.getAccessor(pos,notAir);
    }

    /**
     * 获取指定位置的大气
     * @return 如果大气不存在或未初始化，则返回null。正常情况下返回大气。
     */
    @Nullable
    public static Atmosphere getAtmosphere(@Nonnull World world,@Nonnull BlockPos pos){
        IAtmosphereSystem system = getAtmosphereSystem(world);
        if(system == null) return null;
        return system.getAtmosphere(pos);
    }

    @Nullable
    static WorldServer getValidWorld(@Nonnull World world){
        if(world.isRemote || (!(world instanceof WorldServer))) return null;
        return (WorldServer) world;
    }
}
