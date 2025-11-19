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

package top.qiguaiaaaa.geocraft.geography.fluid_physics.reality;

import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
public interface ILayeredFluidHostLiquid extends IBlockStateLayeredFluidHost {
    int HEIGHT_PER_QUANTA = LayeredFluidHostUtil.EIGHTH_HEIGHT;
    @Nonnull
    Fluid getFluid();

    @Override
    default boolean isAcceptedFluid(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return fluid == getFluid();
    }

    @Override
    default int getLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid){
        if(fluid == getFluid() || fluid == null) return Math.max(8-state.getValue(LEVEL),1);
        return 0;
    }

    @Override
    default int getMaxLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(getFluid() == FluidRegistry.WATER && fluid == GeoFluids.SNOW){
            return 8- getLayers(world,pos,state,FluidRegistry.WATER);
        }
        if(fluid == getFluid() || fluid == null) return 8;
        return 0;
    }

    @Override
    default int getEmptyHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid){
        if(getFluid() == FluidRegistry.WATER && fluid == GeoFluids.SNOW){
            return getHeight(world,pos,state,FluidRegistry.WATER);
        }
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default int getHeightPerLayer(@Nullable World world,@Nullable BlockPos pos,@Nonnull IBlockState state){
        return LayeredFluidHostUtil.EIGHTH_HEIGHT;
    }

    @Override
    default long getAmountInQBPerLayer(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return QBUtil.QUANTA_VOLUME;
    }

    @Override
    default void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid , int layer, @Nullable NBTTagCompound nbt, final int disabledBlockFlags, final int enabledBlockFlags){
        if(fluid == GeoFluids.SNOW && getFluid() == FluidRegistry.WATER){
            int quantaWater = getLayers(world,pos,state,FluidRegistry.WATER);
            layer = MathHelper.clamp(layer,0,8- getLayers(world,pos,state,FluidRegistry.WATER));
            if(layer == 0) return;
            try(@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)) {
                final int flags = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);
                MoreRealityFluidPhysicsCore.mixSnowWithWater(world,pos,accessor,quantaWater,layer,flags);
            }
            return;
        }
        if(fluid != getFluid()) return;
        if(layer == 0) return;
        int newQuanta = Math.max(8-state.getValue(LEVEL),0)+ layer;
        setLayer(world,pos,state,fluid,newQuanta,disabledBlockFlags,enabledBlockFlags);
    }

    @Override
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid , int newLayer,@Nullable NBTTagCompound nbt,final int disabledBlockFlags,final int enabledBlockFlags){
        if(fluid == GeoFluids.SNOW && getFluid() == FluidRegistry.WATER){
            final int waterQuanta = Math.max(8-state.getValue(LEVEL),0);
            final int snowQuanta = MathHelper.clamp(newLayer,0,8-waterQuanta);
            return MoreRealityFluidPhysicsCore.mixSnowWithWater(world,pos,null,waterQuanta,snowQuanta,APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags));
        }
        if(fluid != getFluid()) return false;
        newLayer = Math.min(newLayer,8);
        final int flags = APIMathUtil.getModifiedFlag(Constants.BlockFlags.SEND_TO_CLIENTS,disabledBlockFlags,enabledBlockFlags);
        if(newLayer <= 0) {
            return world.setBlockState(pos,Blocks.AIR.getDefaultState(),flags);
        }
        return world.setBlockState(pos,state.withProperty(LEVEL,8- newLayer), flags);
    }

    @Nullable
    @Override
    default IBlockState getLayerState(@Nonnull IBlockState state, @Nonnull Fluid fluid, int layer){
        if(fluid == GeoFluids.SNOW && getFluid() == FluidRegistry.WATER){
            int quantaWater = Math.max(8-state.getValue(LEVEL),0);
            if(layer <0 || layer + quantaWater>8) return null;
            return MoreRealityFluidPhysicsCore.getSnowWaterMixState(layer,quantaWater,state.getBlock() instanceof BlockStaticLiquid);
        }
        if(fluid != getFluid()) return null;
        if(layer <= 0) return Blocks.AIR.getDefaultState();
        return state.withProperty(LEVEL,Math.max(8- layer,0));
    }

    @Override
    default boolean isFull(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid == GeoFluids.SNOW && getFluid() == FluidRegistry.WATER) return state.getValue(LEVEL) == 0;
        if(fluid != null && fluid != getFluid()) return true;
        return state.getValue(LEVEL) == 0;
    }
}
