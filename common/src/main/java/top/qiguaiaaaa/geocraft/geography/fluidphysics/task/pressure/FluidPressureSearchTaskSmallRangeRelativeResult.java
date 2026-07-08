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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.Int10;
import top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static top.qiguaiaaaa.geocraft.geography.fluidphysics.ThreadLocalHelper.MUTABLE_BLOCK_POS_FOR_RES;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s.X_INT_OFFSET;
import static top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s.Y_INT_OFFSET;

/**
 * @author QiguaiAAAA
 */
public class FluidPressureSearchTaskSmallRangeRelativeResult implements IFluidPressureSearchTaskResult{
    protected final int cx, cy, cz;
    protected final IntList res = new IntArrayList();
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    protected int curIndex = 0;

    public FluidPressureSearchTaskSmallRangeRelativeResult(final int cx,final int cy,final int cz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }

    public FluidPressureSearchTaskSmallRangeRelativeResult(final @Nonnull Vec3i centerPos){
        this(centerPos.getX(),centerPos.getY(),centerPos.getZ());
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_TASKS)
    public void put(@Nonnull final Vec3s pos){
        res.add(pos.toInt());
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_TASKS)
    @Nonnull
    public Collection<BlockPos> toResultCollection(){
        ArrayList<BlockPos> list = new ArrayList<>(res.size());
        for(int p:res){
            list.add(getPosFromInt(p).toImmutable());
        }
        return list;
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    @Override
    public boolean hasNext() {
        return curIndex < res.size();
    }

    @Override
    public int size() {
        return res.size();
    }

    /**
     * @return 返回一个BlockPos.MutableBlockPos的结果位置
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    @Override
    @Nullable
    public BlockPos next() {
        if(!hasNext()) return null;
        return getPosFromInt(res.get(curIndex++));
    }

    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_TASKS})
    protected BlockPos getPosFromInt(int posInt){
        final int x = Int10.toInt((posInt& Vec3s.X_INT_MASK)>> X_INT_OFFSET),
                y = Int10.toInt((posInt& Vec3s.Y_INT_MASK)>> Y_INT_OFFSET),
                z = Int10.toInt(posInt& Vec3s.Z_INT_MASK);
        return  MUTABLE_BLOCK_POS_FOR_RES.get().setPos(cx+x,cy+y,cz+z);
    }
}
