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

package top.qiguaiaaaa.geocraft.api.atmosphere.system;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气系统，每个维度都可以有一个大气系统，用来管理这个维度的大气<br/>
 * 注意，有大气系统不代表这个维度有大气。因为实际上大气系统还管理着下垫面。可能会出现一个维度没有大气但有大气系统的情况。
 */
public interface IAtmosphereSystem {
    /**
     * 每游戏刻更新，更新大气
     */
    void updateTick();

    /**
     * 当一个新区块被生成的时候调用，以生成区块对应的大气
     * @param chunk 区块
     */
    void onChunkGenerated(@Nonnull Chunk chunk);

    /**
     * 当一个已生成区块被加载时，加载对应的大气
     * @param chunk 区块
     */
    void onChunkLoaded(@Nonnull Chunk chunk);

    /**
     * 当一个加载区块被卸载时，处理对应的大气
     * @param chunk 区块
     */
    void onChunkUnloaded(@Nonnull Chunk chunk);

    /**
     * 当对应世界要保存的时候，处理大气
     */
    void onWorldSave();

    /**
     * 当服务器正在停止的时候，处理大气
     * @param event 服务器正在停止事件
     */
    void onServerStopping(@Nonnull FMLServerStoppingEvent event);

    /**
     * 当服务器停止的时候
     * @param event 服务器停止事件
     */
    default void onServerStopped(@Nonnull FMLServerStoppedEvent event){}

    boolean isAtmosphereLoaded(@Nonnull ChunkPos pos);

    /**
     * 设置是否停止大气更新
     * @param status 状态
     */
    void setStop(boolean status);

    /**
     * 大气更新是否已经停止
     * @return 若停止，则返回true
     */
    boolean isStopped();

    /**
     * 获取向该大气系统提供底层大气数据的提供者
     * @return 大气数据提供者
     */
    @Nonnull
    IAtmosphereDataProvider getDataProvider();

    /**
     * 获取指定方块或其他游戏部分用于与大气进行交互的接口对象
     * @param pos 方块位置
     * @param notAir 方块是否不为空气
     * @return 若指定位置没有可用大气,则返回null
     */
    @Nullable
    IAtmosphereAccessor getAccessor(@Nonnull BlockPos pos,boolean notAir);

    /**
     * 获取指定位置的大气
     * @param pos 位置
     * @return 若大气可用，则返回大气。否则返回null
     */
    @Nullable
    Atmosphere getAtmosphere(@Nonnull BlockPos pos);

    /**
     * 获取指定区块的大气
     * @param chunk 区块
     * @return 若大气可用，则返回大气，否则返回null
     */
    @Nullable
    Atmosphere getAtmosphere(@Nonnull Chunk chunk);

    /**
     * 获取指定区块坐标的大气
     * @param x 区块坐标X
     * @param z 区块坐标Z
     * @return 若大气可用，则返回大气，否则返回null
     */
    @Nullable
    Atmosphere getAtmosphere(int x,int z);

    @Nonnull
    AtmosphereInfo getAtmosphereInfo();
}
