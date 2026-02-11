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

package top.qiguaiaaaa.geocraft.block.finite;

import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidFrom;
import top.qiguaiaaaa.geocraft.api.util.*;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.FluidPhysicsCoreFinite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
public interface ILayeredFluidHostFiniteLiquid extends IBlockStateLayeredFluidHost {
    int HEIGHT_PER_QUANTA = LayeredFluidHostUtil.EIGHTH_HEIGHT;
    @Nonnull
    Fluid 天圆地方$getFluid();

    @Nullable
    @Override
    default EnumFacing getDefaultSide(@Nonnull final IBlockState state){
        return null;
    }

    @Override
    default boolean isAcceptedFluid(@Nonnull final IBlockState state, @Nullable final EnumFacing side, @Nonnull final Fluid fluid){
        return fluid == 天圆地方$getFluid();
    }

    @Override
    default int getLayers(@Nonnull final IBlockState state,@Nullable final EnumFacing side,@Nonnull Fluid fluid){
        if(fluid == 天圆地方$getFluid()) return Math.max(8-state.getValue(LEVEL),1);
        return 0;
    }

    @Override
    default int getMaxLayers(@Nonnull final IBlockState state, @Nullable final EnumFacing side, @Nonnull Fluid fluid) {
        if(天圆地方$getFluid() == FluidRegistry.WATER && fluid == GeoFluids.SNOW){
            return 8- getLayers(state,null,FluidRegistry.WATER);
        }
        if(fluid == 天圆地方$getFluid()) return 8;
        return 0;
    }

    @Override
    default int getEmptyHeight(@Nonnull final IBlockState state,@Nullable final EnumFacing side, @Nonnull final Fluid fluid){
        if(天圆地方$getFluid() == FluidRegistry.WATER && fluid == GeoFluids.SNOW){
            return getHeight(state,null,FluidRegistry.WATER);
        }
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default int getHeightPerLayer(@Nonnull final IBlockState state,@Nullable final EnumFacing side){
        return LayeredFluidHostUtil.EIGHTH_HEIGHT;
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final IBlockState state, @Nullable final EnumFacing side,@Nonnull Fluid fluid){
        return QBUtil.QUANTA_VOLUME;
    }

    // TODO: Fix Add layer With Water
    @Override
    default int addLayer(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid ,
                          int layer,
                          @Nonnull final FluidHostOperation operation,
                          @Nullable final NBTTagCompound nbt,
                          @Nullable final IFluidFrom from,
                          final long blockFlagsModifier){
        if(fluid == GeoFluids.SNOW && 天圆地方$getFluid() == FluidRegistry.WATER){
            final int quantaWater = getLayers(state,null,FluidRegistry.WATER);
            layer = MathHelper.clamp(layer,0,8- getLayers(state,null,FluidRegistry.WATER));
            if(layer == 0) return 0;
            try(@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)) {
                final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
                FluidPhysicsCoreFinite.mixSnowWithWater(world,pos,accessor,quantaWater,layer,flags);
            }
            return;
        }
        if(fluid != 天圆地方$getFluid()) return 0;
        if(layer == 0) return 0;
        final int newQuanta = getLayers(state,null, fluid)+ layer;
        setLayer(world,pos,state,null,fluid,newQuanta,null,BlockFlagsModifier.disableFor(blockFlagsModifier,Constants.BlockFlags.NOTIFY_NEIGHBORS));
    }

    @Override
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid ,
                             int newLayer,
                             @Nullable final NBTTagCompound nbt,
                             final long blockFlagsModifier){
        if(fluid == GeoFluids.SNOW && 天圆地方$getFluid() == FluidRegistry.WATER){ //雪水混合
            final int waterQuanta = Math.max(8-state.getValue(LEVEL),0);
            final int snowQuanta = MathHelper.clamp(newLayer,0,8-waterQuanta);
            return FluidPhysicsCoreFinite.mixSnowWithWater(world,pos,null,waterQuanta,snowQuanta,BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier));
        }
        if(fluid != 天圆地方$getFluid()) return false;
        newLayer = Math.min(newLayer,8);
        final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
        if(newLayer <= 0) {
            return world.setBlockState(pos,Blocks.AIR.getDefaultState(),flags);
        }
        return world.setBlockState(pos,state.withProperty(LEVEL,8- newLayer), flags);
    }

    @Nullable
    @Override
    default IBlockState getLayerState(@Nonnull final IBlockState state,
                                      @Nullable final EnumFacing side,
                                      @Nonnull final Fluid fluid,
                                      final int layer){
        if(fluid == GeoFluids.SNOW && 天圆地方$getFluid() == FluidRegistry.WATER){ //雪水混合
            int quantaWater = Math.max(8-state.getValue(LEVEL),1);
            if(layer <0 || layer + quantaWater>8) return null;
            return FluidPhysicsCoreFinite.getSnowWaterMixState(layer,quantaWater,state.getBlock() instanceof BlockStaticLiquid);
        }
        if(fluid != 天圆地方$getFluid()) return null;
        if(layer<0) return null;
        if(layer == 0) return Blocks.AIR.getDefaultState();
        return state.withProperty(LEVEL,Math.max(8- layer,0));
    }

    @Override
    default boolean isFull(@Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid) {
        if(fluid == GeoFluids.SNOW && 天圆地方$getFluid() == FluidRegistry.WATER) return state.getValue(LEVEL) == 0;
        if(fluid != 天圆地方$getFluid()) return true;
        return state.getValue(LEVEL) == 0;
    }
}
