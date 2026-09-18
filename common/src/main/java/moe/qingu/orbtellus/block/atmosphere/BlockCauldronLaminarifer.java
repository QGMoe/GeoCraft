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

package moe.qingu.orbtellus.block.atmosphere;

import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.laminarifer.AHUnit;
import moe.qingu.orbtellus.api.laminarifer.IBlockStateLaminarifer;
import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.Laminarifers;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.configs.AtmosphereConfig;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockCauldronLaminarifer extends BlockCauldron implements IBlockStateLaminarifer {

    @Override
    public void fillWithRain(final @Nonnull World worldIn,final @Nonnull BlockPos pos) {
        if(AtmosphereConfig.ALLOW_CAULDRON_GET_INFINITE_WATER.getValue()) super.fillWithRain(worldIn, pos);
    }

    /* -------------------
          Laminarifer
       ------------------- */

    @Override
    public void describeModel(@Nonnull final IBlockState state,
                              @Nonnull final Fluid fluid,
                              @Nullable final NBTTagCompound nbt,
                              @Nonnull final LaminariferModelBuffer buffer) {
        if(fluid == FluidRegistry.WATER){
            buffer.maxLayers = 3L;
            buffer.currentLayers = state.getValue(LEVEL);
        }
        buffer.heightPerLayer = AHUnit.THIRD_FLUID;
        buffer.emptyHeight = AHUnit.SIXTEENTH_BLOCK;
        buffer.amountInQBPerLayer = QBUnit.BOTTLE_VOLUME;
    }

    @Override
    public IBlockState getLayerState(@Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     final long layer) {
        if(fluid != FluidRegistry.WATER) return layer == 0L? state : null;
        if(layer > 3L || layer < 0L) return null;
        return state.withProperty(LEVEL, (int) layer);
    }

    @Override
    public boolean isAcceptedFluid(@Nonnull final IBlockState state,
                                   @Nonnull final Fluid fluid,
                                   @Nullable final NBTTagCompound nbt) {
        return FluidRegistry.WATER == fluid;
    }

    @Override
    public long getMaxLayers(@Nonnull final IBlockState state,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt) {
        return fluid == FluidRegistry.WATER? 3L : 0L;
    }

    @Override
    public long getLayers(@Nonnull final IBlockState state,
                          @Nonnull final Fluid fluid,
                          @Nullable final NBTTagCompound nbt) {
        return state.getValue(LEVEL);
    }

    @Override
    public long getEmptyHeight(@Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt) {
        return AHUnit.SIXTEENTH_BLOCK;
    }

    @Override
    public long getHeightPerLayer(@Nonnull final IBlockState state,
                                  @Nonnull final Fluid fluid,
                                  @Nullable final NBTTagCompound nbt) {
        return AHUnit.THIRD_FLUID;
    }

    @Override
    public long getAmountInQBPerLayer(@Nonnull final IBlockState state,
                                      @Nonnull final Fluid fluid,
                                      @Nullable final NBTTagCompound nbt) {
        return QBUnit.BOTTLE_VOLUME;
    }

    @Override
    public boolean canFill(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt,
                           @Nullable final IFlowSource<?> source) {
        return side == EnumFacing.UP;
    }

    @Override
    public boolean canDrain(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final NBTTagCompound nbt,
                            @Nullable final IFlowDrainer<?> drainer) {
        return side == EnumFacing.UP;
    }

    @Nullable
    @Override
    public QBFluidStack drainStackInQB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final Fluid fluid,
                                       final long amount,
                                       final boolean doOperate,
                                       final long pulse,
                                       @Nullable final IFlowDrainer<?> drainer,
                                       final long blockFlagsModifier) {
        if(fluid == FluidRegistry.WATER) return new QBFluidStack(FluidRegistry.WATER,
                Laminarifers.extractAmountInQB(this, world, pos, state, FluidRegistry.WATER,null, amount, doOperate, pulse, drainer, blockFlagsModifier));
        else return null;
    }
}
