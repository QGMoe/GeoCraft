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

package top.qiguaiaaaa.geocraft.geography.fluid_physics.reality.pressure;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.pressure.FluidPressureSearchBaseTask;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.pressure.FluidPressureSearchTaskSmallRangeRelativeResult;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.api.util.math.Int10;
import top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static top.qiguaiaaaa.geocraft.geography.fluid_physics.ThreadLocalHelper.*;
import static top.qiguaiaaaa.geocraft.geography.fluid_physics.task.pressure.FluidPressureSmallBFSBaseTask.MAX_RELATIVE_POS_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i.X_INT_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i.Y_INT_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.RelativeBlockPosS.Mutable.MUTABLE;

/**
 * 该类仅单线程使用，不得多线程并发使用
 * @author QiguaiAAAA
 */
public abstract class 单次小范围物理压强广搜任务 extends FluidPressureSearchBaseTask implements IRealityPressureBFSTask {

    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    private static final ThreadLocal<IntSet> visited =
            ThreadLocal.withInitial(IntOpenHashSet::new);
    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    private static final ThreadLocal<IntArrayFIFOQueue> queue =
            ThreadLocal.withInitial(IntArrayFIFOQueue::new);

    //********
    // Object Field
    //********
    protected final short maxSearchTimes;
    protected final FluidPressureSearchTaskSmallRangeRelativeResult res;

    protected boolean searched = false;

    public 单次小范围物理压强广搜任务(@Nonnull Fluid fluid, @Nonnull IBlockState beginState, @Nonnull BlockPos beginPos, int searchRange) {
        super(fluid, beginState, beginPos);
        if(searchRange >4) throw new IllegalArgumentException("FluidPressureSmallBFSBaseTask can not handle search range larger than 511 blocks!");
        else if(searchRange == 4) maxSearchTimes = MAX_RELATIVE_POS_OFFSET;
        else maxSearchTimes = (short) IRealityPressureSearchTask.getMaxSearchTimesFromRange(searchRange);
        res = new FluidPressureSearchTaskSmallRangeRelativeResult(beginPos);
    }

    @Override
    public boolean isVisited(@Nonnull BlockPos pos) {
        return visited.get().contains(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    @Override
    public void markVisited(@Nonnull BlockPos pos) {
        visited.get().add(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    @Override
    public int getVisitedSize() {
        return visited.get().size();
    }

    @Override
    public boolean isQueueEmpty() {
        return queue.get().isEmpty();
    }

    @Override
    public void queued(@Nonnull BlockPos pos) {
        queue.get().enqueue(MUTABLE.get().setPos(beginPos,pos).toInt());
    }

    @Nonnull
    @Override
    public BlockPos pull() {
        int relativePos = queue.get().dequeueInt();
        return getPosFromInt(relativePos);
    }

    @Nonnull
    @Override
    public BlockPos peek() {
        int relativePos = queue.get().firstInt();
        return getPosFromInt(relativePos);
    }

    protected BlockPos getPosFromInt(int posInt){
        final int x = Int10.toInt((posInt&IVec3i.X_INT_MASK)>> X_INT_OFFSET),
                y = Int10.toInt((posInt&IVec3i.Y_INT_MASK)>> Y_INT_OFFSET),
                z = Int10.toInt(posInt&IVec3i.Z_INT_MASK);
        return MUTABLE_BLOCK_POS_FOR_QUEUE.get().setPos(beginPos.getX()+x,beginPos.getY()+y,beginPos.getZ()+z);
    }

    @Override
    public int getQueueSize() {
        return queue.get().size();
    }

    @Nonnull
    @Override
    public Collection<BlockPos> getResultCollection() {
        return res.toResultCollection();
    }

    @Override
    public void putBlockPosToResults(@Nonnull BlockPos pos) {
        res.put(MUTABLE_POS_S_FOR_REALITY_BFS_RES.get().setPos(beginPos,pos));
    }

    @Override
    public int getMaxSearchTimes() {
        return maxSearchTimes;
    }

    @Override
    public boolean hasFoundEnoughResults() {
        return res.size()>getBeginQuanta()+2;
    }

    @Nullable
    @Override
    public IFluidPressureSearchTaskResult search(@Nonnull WorldServer world) {
        if(!queue.get().isEmpty() || !visited.get().isEmpty()){
            GeoCraft.getLogger().warn("Single Pressure Task {} found queue and visited set are not empty before search, it should not happen!",hashCode());
            queue.get().clear();
            visited.get().clear();
        }
        queued(beginPos);
        markVisited(beginPos);
        for(int i=0;i<=maxSearchTimes;i++){
            if(isQueueEmpty()) break;
            BlockPos pos = pull();
            if(search_Inner(world,pos)) break;
        }
        visited.get().clear();
        queue.get().clear();
        searched = true;
        return res;
    }

    @Override
    public void cancel() {}

    @Override
    public void finish() {}

    @Override
    public int getSearchTimes() {
        return searched?maxSearchTimes:0;
    }

    @Override
    public boolean hasSearchTimeReachedMax() {
        return searched;
    }

    @Override
    public boolean isFinished() {
        return searched;
    }
}
