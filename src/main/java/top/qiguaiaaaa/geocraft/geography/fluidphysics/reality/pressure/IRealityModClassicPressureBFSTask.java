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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.pressure;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidSearchUtil;

import javax.annotation.Nonnull;

import static top.qiguaiaaaa.geocraft.geography.fluidphysics.ThreadLocalHelper.MUTABLE_BLOCK_POS_FOR_REALITY_BFS;

/**
 * @author QiguaiAAAA
 */
public interface IRealityModClassicPressureBFSTask extends IRealityPressureBFSTask{
    byte getDensityDir();

    @Override
    default boolean search_Inner(@Nonnull WorldServer world,@Nonnull BlockPos pos){
        if(!world.isBlockLoaded(pos)) return false;
        final BlockPos.MutableBlockPos mutablePos = MUTABLE_BLOCK_POS_FOR_REALITY_BFS.get();
        IBlockState state = world.getBlockState(pos);
        if(state.getMaterial() == Material.AIR){
            if(pos.getY() != getBeginPos().getY() || getBeginQuanta() >1)
                putBlockPosToResults(pos);
        }else if(FluidUtil.getFluid(state) == getFluid()){
            int quanta = getQuantaPerBlock()-state.getValue(BlockFluidBase.LEVEL);
            if((isLowerPos(pos) && quanta <getQuantaPerBlock()) || (pos.getY() == getBeginPos().getY() && quanta < getBeginQuanta()-1))
                putBlockPosToResults(pos);
        }
        if(hasFoundEnoughResults()) return true; //够了
        if(hasSearchTimeReachedMax()) return true;

        if(state.getMaterial() == Material.AIR) return false;
        for(int[] dir: FluidSearchUtil.DIRS6){
            if(pos.getY() == getBeginPos().getY() && dir[1]* getDensityDir() >0) continue;
            mutablePos.setPos(pos.getX()+dir[0],pos.getY()+dir[1],pos.getZ()+dir[2]);
            if(mutablePos.getY()<0 || mutablePos.getY()>=world.getHeight()) continue;
            if(isVisited(mutablePos)) continue;
            markVisited(mutablePos);
            if(canSearchInto(world,mutablePos,dir)){
                queued(mutablePos);
            }
        }
        return false;
    }

    default boolean isLowerPos(BlockPos pos){
        return getDensityDir()>0?pos.getY()<getBeginPos().getY():pos.getY()>getBeginPos().getY();
    }

    default boolean canSearchInto(@Nonnull WorldServer world,@Nonnull BlockPos pos, int[] dir){
        if(!world.isBlockLoaded(pos)) return false;
        IBlockState state = world.getBlockState(pos);
        if(state.getMaterial() == Material.AIR &&
                (dir[1] != 0 || getBeginQuanta()>1 || isLowerPos(pos))
        ) return true;
        if(FluidUtil.getFluid(state) == getFluid()) return true;
        return ((BlockFluidBase)getBeginState().getBlock()).canDisplace(world,pos);
    }

    @Override
    default boolean isEqualState(@Nonnull IBlockState curState){
        return getBeginState() == curState;
    }
}
