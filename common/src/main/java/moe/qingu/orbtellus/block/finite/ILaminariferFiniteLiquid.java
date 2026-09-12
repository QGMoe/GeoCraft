/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.block.finite;

import moe.qingu.orbtellus.api.laminarifer.*;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import moe.qingu.orbtellus.geography.snow.SnowFlowing;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import moe.qingu.orbtellus.api.OTCFluids;
import moe.qingu.orbtellus.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.orbtellus.api.util.APIMathUtil;
import moe.qingu.orbtellus.api.util.AtmosphereUtil;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
public interface ILaminariferFiniteLiquid extends IBlockStateLaminarifer, IOperateLayerLaminarifer {

    @Nonnull
    Fluid 天圆地方$getFluid();

    @Override
    default boolean isAcceptedFluid(@Nonnull final IBlockState state,
                                    @Nonnull final Fluid fluid,
                                    @Nullable final NBTTagCompound nbt){
        return fluid == 天圆地方$getFluid() || fluid == OTCFluids.SNOW;
    }

    @Override
    default void describeModel(@Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt,
                               @Nonnull final LaminariferModelBuffer buffer) {
        final Fluid current = 天圆地方$getFluid();
        if(fluid == OTCFluids.SNOW && current == FluidRegistry.WATER){
            buffer.maxLayers = 8 - Math.max(8-state.getValue(LEVEL),1);
            buffer.currentLayers = 0L;
        }else if(current == fluid){
            buffer.maxLayers = 8L;
            buffer.currentLayers = Math.max(8-state.getValue(LEVEL),1);
        }else {
            buffer.maxLayers = 0L;
            buffer.currentLayers = 0L;
        }
        buffer.heightPerLayer = AHUnit.EIGHTH_FLUID;
        buffer.amountInQBPerLayer = QBUnit.QUANTA_VOLUME;
        buffer.emptyHeight = 0L;
    }

    @Override
    default long getMaxLayers(@Nonnull final IBlockState state,
                              @Nonnull final Fluid fluid,
                              @Nullable final NBTTagCompound nbt){
        final Fluid current = 天圆地方$getFluid();
        if(fluid == OTCFluids.SNOW && current == FluidRegistry.WATER) return 8L - getLayers(state,FluidRegistry.WATER,null);
        if(current == fluid) return 8L;
        return 0L;
    }

    @Override
    default long getLayers(@Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt){
        if(fluid == 天圆地方$getFluid()) return Math.max(8-state.getValue(LEVEL),1);
        return 0;
    }

    @Override
    default long getEmptyHeight(@Nonnull final IBlockState state,
                                @Nonnull final Fluid fluid,
                                @Nullable final NBTTagCompound nbt){
        if(天圆地方$getFluid() == FluidRegistry.WATER && fluid == OTCFluids.SNOW){
            return getHeight(state,FluidRegistry.WATER,null);
        }
        return 0L;
    }

    @Override
    default long getHeightPerLayer(@Nonnull final IBlockState state,
                                   @Nonnull final Fluid fluid,
                                   @Nullable final NBTTagCompound nbt){
        return AHUnit.EIGHTH_FLUID;
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final IBlockState state,
                                       @Nonnull final Fluid fluid,
                                       @Nullable final NBTTagCompound nbt){
        return QBUnit.QUANTA_VOLUME;
    }

    @Override
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             final long newLayer,
                             final long blockFlagsModifier) {
        return IBlockStateLaminarifer.super.setLayer(world, pos, state, fluid, nbt, newLayer, blockFlagsModifier);
    }

    @Override
    default long operateLayer(@Nonnull final World world,
                              @Nonnull final BlockPos pos,
                              @Nonnull final IBlockState state,
                              @Nonnull final Fluid fluid,
                              @Nullable final NBTTagCompound nbt,
                              final long layer,
                              final boolean doOperate,
                              final long pulse,
                              final long blockFlagsModifier){
        final Fluid current = 天圆地方$getFluid();
        if(fluid == OTCFluids.SNOW && current == FluidRegistry.WATER){
            final int quantaWater = (int) getLayers(state,FluidRegistry.WATER,null);
            final int actualLayer = (int) APIMathUtil.clamp(layer,0L,8L - quantaWater);
            if(actualLayer == 0L) return 0L;
            if(doOperate)
                try(@Nullable final IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)) {
                    final int flags = BlockFlagModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
                    SnowFlowing.mixSnowWithWater(world,pos,accessor,quantaWater,actualLayer,flags);
                }
            return actualLayer;
        }
        if(fluid != current) return 0L;
        final int currentQuanta = (int) getLayers(state,current,null);
        final int actualLayer = (int) APIMathUtil.clamp(layer,-currentQuanta,8L-currentQuanta);
        if(actualLayer == 0L) return 0L;
        if(doOperate){
            final int flags = BlockFlagModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
            final int newQuanta = currentQuanta + actualLayer;
            assert Blocks.AIR != null;
            if(newQuanta == 0L) world.setBlockState(pos, Blocks.AIR.getDefaultState(), flags);
            else world.setBlockState(pos, FiniteFlowingVanilla.getFlowingByMaterial(state.getMaterial()).dynamic.getDefaultState().withProperty(LEVEL,8 - newQuanta), flags);
        }
        return actualLayer;
    }

    @Override
    default IBlockState getLayerState(@Nonnull final IBlockState state,
                                      @Nonnull final Fluid fluid,
                                      @Nullable final NBTTagCompound nbt,
                                      final long layer){
        final Fluid current = 天圆地方$getFluid();
        if(fluid == OTCFluids.SNOW && current == FluidRegistry.WATER){ //雪水混合
            final long quantaWater = getLayers(state, FluidRegistry.WATER, null);
            if(layer < 0L || layer + quantaWater > 8L) return null;
            return SnowFlowing.getSnowWaterMixState((int) layer,(int) quantaWater,state.getBlock() instanceof BlockStaticLiquid);
        }
        if(fluid != current) return null;
        if(layer< 0L || layer > 8L) return null;
        assert Blocks.AIR != null;
        if(layer == 0L) return Blocks.AIR.getDefaultState();
        return state.withProperty(LEVEL, Math.max( 8- (int) layer, 0));
    }

    @Nullable
    @Override
    default QBFluidStack drainStackInQB(@Nonnull final World world,
                                        @Nonnull final BlockPos pos,
                                        @Nonnull final IBlockState state,
                                        @Nullable final Fluid fluid,
                                        long amount,
                                        final boolean doOperate,
                                        final long pulse,
                                        @Nullable final IFlowDrainer<?> drainer,
                                        final long blockFlagsModifier){
        final Fluid current = 天圆地方$getFluid();
        if(fluid != null && fluid != current) return null;
        final long qb = Laminarifers.extractAmountInQB(this,world,pos,state,current,amount,doOperate,pulse,drainer,blockFlagsModifier);
        return new QBFluidStack(current,qb);
    }
}
