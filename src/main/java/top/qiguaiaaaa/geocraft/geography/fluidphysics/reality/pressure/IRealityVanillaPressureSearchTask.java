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

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public interface IRealityVanillaPressureSearchTask extends IRealityPressureSearchTask{

    default boolean canSearchInto(@Nonnull WorldServer world, @Nonnull BlockPos pos, int[] dir){
        if(!world.isBlockLoaded(pos)) return false;
        final IBlockState state = world.getBlockState(pos);
        if(state.getMaterial() == Material.AIR && (dir[1] != 0 || getBeginQuanta()>1 || pos.getY() < getBeginPos().getY())) return true;
        if(FluidUtil.getFluid(state) == getFluid()) return true;
        return false;
    }

    default boolean checkIfValid(@Nonnull WorldServer world,@Nonnull IBlockState state, @Nonnull BlockPos pos){
        if(state.getMaterial() == Material.AIR){
            return pos.getY() != getBeginPos().getY() || getBeginQuanta() > 1;
        }else if(FluidUtil.getFluid(state) == getFluid()){
            int quanta = 8-state.getValue(BlockLiquid.LEVEL);
            return (pos.getY() < getBeginPos().getY() && quanta < 8) || (pos.getY() == getBeginPos().getY() && quanta < getBeginQuanta() - 1);
        }
        return false;
    }

    @Override
    default boolean isEqualState(@Nonnull IBlockState curState) {
        if(FluidUtil.getFluid(curState) == getFluid()){
            return curState.getValue(BlockLiquid.LEVEL) == 8-getBeginQuanta();
        }
        return false;
    }

    @Override
    default byte getQuantaPerBlock(){
        return 8;
    }
}
