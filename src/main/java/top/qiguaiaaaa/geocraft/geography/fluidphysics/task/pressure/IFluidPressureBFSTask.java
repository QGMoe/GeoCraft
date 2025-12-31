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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author QiguaiAAAA
 */
public interface IFluidPressureBFSTask extends IFluidPressureSearchTask {
    /**
     * 指定位置是否已经被访问
     * @param pos 位置
     * @return 若已被访问,则返回true
     */
    boolean isVisited(@Nonnull BlockPos pos);

    /**
     * 标记指定位置被访问
     * @param pos 被访问的位置
     */
    void markVisited(@Nonnull BlockPos pos);
    int getVisitedSize();

    boolean isQueueEmpty();

    /**
     * 将指定位置加入队列
     * @param pos 位置
     */
    void queued(@Nonnull BlockPos pos);

    /**
     * 取出队列中的第一个位置，会从队列中移除第一个位置
     * @return 第一个位置
     */
    @Nonnull
    BlockPos pull();

    /**
     * 获取队列中的第一个位置，不会在队列中将其移除
     * @return 第一个位置
     */
    @Nonnull
    BlockPos peek();

    int getQueueSize();

    /**
     * 获取结果集合
     * @return 一个BlockPos集合
     */
    @Nonnull
    Collection<BlockPos> getResultCollection();

    void putBlockPosToResults(@Nonnull BlockPos pos);
}
