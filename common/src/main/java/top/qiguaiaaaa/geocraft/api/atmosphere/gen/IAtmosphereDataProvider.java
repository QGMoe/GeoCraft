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

package top.qiguaiaaaa.geocraft.api.atmosphere.gen;

import net.minecraft.world.chunk.IChunkProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.IAtmosphereDataLoader;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * 大气数据提供者，类似{@link IChunkProvider}
 * 用于向{@link IAtmosphereSystem}提供对应区块的{@link AtmosphereData}
 */
public interface IAtmosphereDataProvider {

    void setMaxLoadDistance(int distance);

    /**
     * 获取指定区块已加载的大气数据，该方法不应创建新的大气数据或从磁盘读取未加载的数据
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 一个大气数据，若为null则没有大气已加载
     */
    @Nullable
    AtmosphereData getLoadedAtmosphereData(int x, int z);

    /**
     * 获取当前已经加载的大气集合
     * @return 一个大气数据集合，全部都是已加载的大气数据
     */
    @Nonnull
    Collection<AtmosphereData> getLoadedAtmosphereDataCollection();

    /**
     * 提供指定区块的大气数据，若没有则应当从磁盘读取，还是没有的话应新建数据。
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 一个大气数据，不应为null
     */
    @Nonnull AtmosphereData provideAtmosphereData(int x, int z);

    /**
     * 指定区块的大气是否已经生成，注意可能对应的区块或大气还尚未加载
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 若已经生成，则返回true，否则返回false
     */
    boolean isAtmosphereGeneratedAt(int x, int z);

    /**
     * 将指定区块的大气数据标记为待卸载
     * @param x 区块X坐标
     * @param z 区块Z坐标
     */
    void queueUnloadAtmosphereData(int x,int z);

    /**
     * 直接保存指定区块的大气数据
     * 注意考虑到{@link IAtmosphereDataLoader}的实现，该方法不一定是同步的
     * @param x 区块X坐标
     * @param z 区块Z坐标
     */
    void saveAtmosphereData(int x,int z);

    /**
     * 直接保存所有大气数据
     * 注意考虑到{@link IAtmosphereDataLoader}的实现，该方法不一定是同步的
     */
    void saveAllAtmosphereData();

    /**
     * 每游戏刻调用一次
     * @return 暂时不知道有什么用
     */
    boolean tick();

    /**
     * 将当前{@link IAtmosphereDataProvider}的大气加载信息输出为字符串
     * @return 序列化为字符串的大气加载信息
     */
    @Nonnull
    String makeString();
}
