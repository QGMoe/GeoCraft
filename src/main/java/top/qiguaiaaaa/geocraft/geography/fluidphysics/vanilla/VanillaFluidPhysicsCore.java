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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.util.WaterUtil;

import javax.annotation.Nullable;
import java.util.Random;

public class VanillaFluidPhysicsCore {
    public static IBlockState evaporateWater(World world, BlockPos pos, IBlockState state, Random rand){
        try (@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)) {
            if(accessor == null) return state;
            if(!accessor.canAccessAtmosphere()) return state;
            if(!accessor.getAtmosphereInfo().canWaterEvaporate(pos)) return state;

            int meta = state.getValue(BlockLiquid.LEVEL);
            if(accessor.getTemperature()> TemperatureProperty.BOILED_POINT){
                FluidRegistry.WATER.vaporize(null,world,pos,null);
                if(meta == 0) accessor.fillFluidToAtmosphere(FluidRegistry.WATER,Fluid.BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true);
                return Blocks.AIR.getDefaultState();
            }

            int amount = (int) MathHelper.clamp(WaterUtil.getWaterEvaporateAmount(accessor),0,Fluid.BUCKET_VOLUME);
            if(amount == 0) return state;

            if((amount = accessor.fillFluidToAtmosphere(FluidRegistry.WATER,amount, StateOfMatter.GAS,accessor.getTemperature(true),true)) <= 0) return state;
            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_EVAPORATE_LATENT_HEAT_PER_QUANTA*(double)amount/FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME);
            if(meta == 0 && amount >= Fluid.BUCKET_VOLUME){
                return Blocks.AIR.getDefaultState();
            }
        }

        return state;
    }

}
