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

package moe.qingu.orbtellus.block.soil;

import moe.qingu.orbtellus.api.laminarifer.AHUnit;
import moe.qingu.orbtellus.api.laminarifer.IBlockStateLaminarifer;
import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.Laminarifers;
import moe.qingu.orbtellus.api.laminarifer.flow.IFlowInitiator;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.laminarifer.flow.source.FlowSources;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import moe.qingu.orbtellus.geography.soil.BlockSoilType;
import moe.qingu.orbtellus.util.BaseUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.orbtellus.api.block.BlockProperties.HUMIDITY;

public interface IBlockSoil extends IBlockStateLaminarifer, IFlowInitiator<IBlockSoil> {

    @Nonnull
    BlockSoilType getType(@Nonnull final IBlockState state);

    default int getMaxStableHumidity(@Nonnull final IBlockState state){
        return getType(state).getMaxStableHumidity();
    }

    default double getFlowInPossibility(@Nonnull final IBlockState state){
        return getType(state).getFlowInPossibility();
    }

    default double getRainInPossibility(@Nonnull final IBlockState state){
        return getType(state).getRainInPossibility();
    }

    //******************
    // ILaminarifer
    //******************


    @Override
    default boolean isAcceptedFluid(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
        return fluid == FluidRegistry.WATER;
    }

    @Override
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final NBTTagCompound nbt,
                            @Nullable final IFlowSource<?> source) {
        if(fluid != FluidRegistry.WATER) return false;
        if(Laminarifers.isFull(this,state,fluid,nbt)) return false;
        if(FlowSources.isAtmosphere(source)){
            return BaseUtil.getRandomResult(world.rand,getRainInPossibility(state));
        }else if (FlowSources.isRunoff(source)){
            return BaseUtil.getRandomResult(world.rand, getFlowInPossibility(state));
        }
        return true;
    }

    @Override
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             @Nullable final IFlowDrainer<?> drainer) {
        if(fluid != FluidRegistry.WATER) return false;
        return getLayers(state,fluid,nbt)>getMaxStableHumidity(state);
    }

    @Override
    default void describeModel(@Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt,
                               @Nonnull final LaminariferModelBuffer buffer) {
        if(fluid == FluidRegistry.WATER){
            buffer.maxLayers = 4L;
            buffer.currentLayers = getLayers(state, fluid, nbt);
        }
        buffer.heightPerLayer = AHUnit.FIFTH_FLUID;
        buffer.emptyHeight = 0L;
        buffer.amountInQBPerLayer = QBUnit.QUANTA_VOLUME;
    }

    @Override
    default long getMaxLayers(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
        if(fluid != FluidRegistry.WATER) return 0L;
        return 4L;
    }

    @Override
    default long getLayers(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
        if(fluid != FluidRegistry.WATER) return 0L;
        return state.getValue(HUMIDITY);
    }

    @Override
    default long getEmptyHeight(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
        return 0L;
    }

    @Override
    default long getHeightPerLayer(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
        return AHUnit.FIFTH_FLUID;
    }

    @Override
    default long getMaxHeight(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt) {
        return fluid == FluidRegistry.WATER? AHUnit.FIFTH_FLUID<<2:0L;
    }

    @Override
    default long getHeight(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt) {
        if(fluid != FluidRegistry.WATER) return 0L;
        return state.getValue(HUMIDITY) * getHeightPerLayer(state,fluid,nbt);
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt){
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
        if(fluid != FluidRegistry.WATER) return false;
        if(newLayer < 0L || newLayer > 4L) return false;
        world.setBlockState(pos,state.withProperty(HUMIDITY, (int) newLayer), BlockFlagModifier.modify(Constants.BlockFlags.SEND_TO_CLIENTS,blockFlagsModifier));
        return true;
    }

    @Override
    default IBlockState getLayerState(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt, final long layer){
        if(fluid != FluidRegistry.WATER) return layer == 0L? state: null;
        if(layer < 0L || layer> 4L) return null;
        return state.withProperty(HUMIDITY, (int) layer);
    }

    @Nullable
    @Override
    default QBFluidStack drainStackInQB(@Nonnull final World world,
                                        @Nonnull final BlockPos pos,
                                        @Nonnull final IBlockState state,
                                        @Nullable final Fluid fluid,
                                        final long amount,
                                        final boolean doOperate,
                                        final long pulse,
                                        @Nullable final IFlowDrainer<?> drainer,
                                        final long blockFlagsModifier){
        if(fluid == null || fluid == FluidRegistry.WATER){
            final long actually = Laminarifers.extractAmountInQB(this,world,pos,state,FluidRegistry.WATER,null,amount,doOperate,pulse,drainer,blockFlagsModifier);
            return new QBFluidStack(FluidRegistry.WATER,actually);
        }else return null;
    }
}
