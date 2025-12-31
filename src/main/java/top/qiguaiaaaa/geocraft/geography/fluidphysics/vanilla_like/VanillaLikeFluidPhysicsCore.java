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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla_like;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nullable;

public class VanillaLikeFluidPhysicsCore {
    /**
     * 是否能够在指定位置降水
     * @param world 世界
     * @param pos 位置
     * @return 如果能，则返回true
     */
    public static boolean canRainAt(World world, BlockPos pos){
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return false;
        }
        if(!world.isAreaLoaded(pos,1)) return false;
        try(@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,false);) {
            if(accessor == null) return false;
            Atmosphere atmosphere = accessor.getAtmosphereHere();
            if(atmosphere == null) return false;
            if(atmosphere.drainWater(Fluid.BUCKET_VOLUME,pos,false)< Fluid.BUCKET_VOLUME) return false;
            double temp = accessor.getTemperature();
            if (!(temp < TemperatureProperty.ICE_POINT) && !(temp > TemperatureProperty.BOILED_POINT)) {
                IBlockState state = world.getBlockState(pos);
                if(FluidUtil.getFluid(state) != FluidRegistry.WATER) return false;
                if(state.getValue(BlockLiquid.LEVEL) != 1) return false;
                int adjacentSourceBlocks = 0;
                for(EnumFacing facing: ChunkUtil.HORIZONTALS){
                    BlockPos facingPos = pos.offset(facing);
                    IBlockState facingState = world.getBlockState(facingPos);
                    if(FluidUtil.getFluid(facingState) != FluidRegistry.WATER) continue;
                    adjacentSourceBlocks += FluidUtil.isFullFluid(world,facingPos,facingState)?1:0;
                }
                return adjacentSourceBlocks>=2;
            }
            return false;
        }

    }
}
