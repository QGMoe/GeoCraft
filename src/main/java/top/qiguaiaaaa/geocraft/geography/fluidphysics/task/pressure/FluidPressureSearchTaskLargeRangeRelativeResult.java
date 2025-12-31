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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.Int21;
import top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

import static top.qiguaiaaaa.geocraft.geography.fluidphysics.ThreadLocalHelper.MUTABLE_BLOCK_POS_FOR_RES;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i.X_LONG_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.IVec3i.Y_LONG_OFFSET;

/**
 * @author QiguaiAAAA
 */
public class FluidPressureSearchTaskLargeRangeRelativeResult implements IFluidPressureSearchTaskResult{
    protected final int cx, cy, cz;
    protected final LongList res = new LongArrayList();
    protected int curIndex = 0;

    public FluidPressureSearchTaskLargeRangeRelativeResult(int cx, int cy, int cz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }

    public FluidPressureSearchTaskLargeRangeRelativeResult(Vec3i centerPos){
        this(centerPos.getX(),centerPos.getY(),centerPos.getZ());
    }

    public FluidPressureSearchTaskLargeRangeRelativeResult(IVec3i centerPos){
        this(centerPos.getX(),centerPos.getY(),centerPos.getZ());
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_TASKS)
    public Collection<BlockPos> toResultCollection(){
        ArrayList<BlockPos> list = new ArrayList<>(res.size());
        for(long p:res){
            list.add(getPosFromLong(p).toImmutable());
        }
        return list;
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_TASKS)
    public void put(IVec3i pos){
        res.add(pos.toLong());
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean hasNext() {
        return curIndex < res.size();
    }

    @Override
    public int size() {
        return res.size();
    }

    @Nullable
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    @Override
    public BlockPos next() {
        if(!hasNext()) return null;
        return getPosFromLong(res.get(curIndex++));
    }

    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_TASKS})
    protected BlockPos getPosFromLong(long posLong){
        final int x = Int21.toInt((posLong& IVec3i.X_LONG_MASK)>> X_LONG_OFFSET),
                y = Int21.toInt((posLong&IVec3i.Y_LONG_MASK)>> Y_LONG_OFFSET),
                z = Int21.toInt(posLong&IVec3i.Z_LONG_MASK);
        return MUTABLE_BLOCK_POS_FOR_RES.get().setPos(cx+x,cy+y,cz+z);
    }
}
