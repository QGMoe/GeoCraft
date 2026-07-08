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

package top.qiguaiaaaa.geocraft.api.atmosphere.storage;

import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * 从系统存储或其他地方加载大气数据的东西
 */
public interface IAtmosphereDataLoader {
    /**
     * 加载指定区块的大气数据
     * @param worldIn 区块所在世界
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 加载的大气数据，可能为null
     * @throws IOException 若出现IO异常，则抛出
     */
    @Nullable
    AtmosphereData loadAtmosphereData(@Nonnull World worldIn, int x, int z) throws IOException;

    /**
     * 保存指定大气数据
     * @param worldIn 大气数据所在世界
     * @param data 需要保存的大气数据
     * @throws MinecraftException 若已有另一个Minecraft实例锁定了存档，则抛出
     * @throws IOException 若出现IO异常，则抛出
     */
    void saveAtmosphereData(@Nonnull World worldIn,@Nonnull AtmosphereData data) throws MinecraftException, IOException;

    /**
     * 检查指定区块的大气是否存在，注意该方法不是检查指定区块的大气是否已加载
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 若存在，则返回true
     */
    boolean doesAtmosphereExistsAt(int x, int z);

    /**
     * 刷新该Loader使得大气数据被立刻（同步）保存或（同步）加载
     */
    void flush();
}
