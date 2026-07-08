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

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.math.Int10;
import top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s;

import javax.annotation.Nonnull;
import java.util.Set;

import static top.qiguaiaaaa.geocraft.geography.fluidphysics.ThreadLocalHelper.MUTABLE_BLOCK_POS_FOR_QUEUE;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s.X_INT_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s.Y_INT_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s.RelativeMVec3s.MUTABLE;

/**
 * 以{@link Integer}存储坐标的BFS搜寻任务，适用于搜寻范围不超过{@link #MAX_RELATIVE_POS_OFFSET}的任务
 * @author QiguaiAAAA
 */
public abstract class FluidPressureSmallBFSBaseTask extends FluidPressureBFSBaseTask{
    public static final int MAX_RELATIVE_POS_OFFSET = (1<<(Integer.SIZE/3)-1)-1;
    protected final IntSet visited = new IntOpenHashSet();
    protected final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();

    public FluidPressureSmallBFSBaseTask(@Nonnull Fluid fluid, @Nonnull IBlockState beginState, @Nonnull BlockPos beginPos) {
        super(fluid, beginState, beginPos);
    }

    @Override
    protected Set<?> getVisitedSet() {
        return visited;
    }

    /**
     * {@inheritDoc}
     * @param pos {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isVisited(@Nonnull BlockPos pos){
        return visited.contains(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    /**
     * {@inheritDoc}
     * @param pos {@inheritDoc}
     */
    @Override
    public void markVisited(@Nonnull BlockPos pos){
        visited.add(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    @Override
    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     * @param pos {@inheritDoc}
     */
    @Override
    public void queued(@Nonnull BlockPos pos){
        queue.enqueue(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    @Nonnull
    @Override
    public BlockPos pull() {
        int relativePos = queue.dequeueInt();
        return getPosFromInt(relativePos);
    }

    @Nonnull
    @Override
    public BlockPos peek() {
        int relativePos = queue.firstInt();
        return getPosFromInt(relativePos);
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    protected BlockPos getPosFromInt(int posInt){
        final int x = Int10.toInt((posInt& Vec3s.X_INT_MASK)>> X_INT_OFFSET),
                y = Int10.toInt((posInt& Vec3s.Y_INT_MASK)>> Y_INT_OFFSET),
                z = Int10.toInt(posInt& Vec3s.Z_INT_MASK);
        return MUTABLE_BLOCK_POS_FOR_QUEUE.get().setPos(beginPos.getX()+x,beginPos.getY()+y,beginPos.getZ()+z);
    }

    @Override
    public void cancel() {
        visited.clear();
        queue.clear();
    }

    @Override
    public void finish() {
        visited.clear();
        queue.clear();
    }
}
