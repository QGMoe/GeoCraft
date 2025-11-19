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

package top.qiguaiaaaa.geocraft.api.block;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import git.jbredwards.fluidlogged_api.mod.asm.plugins.forge.PluginBlockFluidBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 写好了，但还没有测试。<br/>
 * 一个让含水方块和载流方块兼容的接口，为含水方块实现了载流方块的默认行为。
 * @author QiguaiAAAA
 */
public interface IFluidloggableLayeredFluidHost extends IFluidloggable, ILayeredFluidHost {
    int DEFAULT_QUANTA_PER_BLOCK = 8;

    @Override
    default boolean isAcceptedFluid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return isFluidValid(state,world,pos,fluid);
    }

    @Override
    default int getLayers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid){
        FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return 0;
        return fluidState.getQuantaValue();
    }

    @Override
    default int getMaxLayers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        final FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()){
            if (fluid == null) return DEFAULT_QUANTA_PER_BLOCK;
            final Block block = fluid.getBlock();
            if(block instanceof PluginBlockFluidBase.Accessor)
                return ((PluginBlockFluidBase.Accessor)block).getQuantaPerBlock_Public();
            return DEFAULT_QUANTA_PER_BLOCK;
        }else if(fluidState.getFluid() == fluid || fluid == null){
            return fluidState.getQuantaPerBlock();
        }
        return 0;
    }

    @Override
    default int getEmptyHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid){
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default int getHeightPerLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state){
        final FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return LayeredFluidHostUtil.EIGHTH_HEIGHT;
        return LayeredFluidHostUtil.HEIGHTS.get(fluidState.getQuantaPerBlock());
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        final FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return QBUtil.QUANTA_VOLUME;
        if(fluid != fluidState.getFluid()) return 0;
        return QBUtil.VOLUMES_1_TO_16.get(fluidState.getQuantaPerBlock());
    }

    @Override
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer, @Nullable NBTTagCompound nbt, final int disabledBlockFlags, final int enabledBlockFlags){
        if(newLayer == 0){
            return FluidloggedUtils.setFluidState(world,pos,state,FluidState.EMPTY,false,APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT, disabledBlockFlags,enabledBlockFlags));
        }
        if(newLayer < 0) return false;
        world.setBlockState(pos,state);
        final int quantaPerBlock;
        final Block block = fluid.getBlock();
        if(block instanceof PluginBlockFluidBase.Accessor){
            quantaPerBlock = ((PluginBlockFluidBase.Accessor)block).getQuantaPerBlock_Public();
        }else quantaPerBlock = DEFAULT_QUANTA_PER_BLOCK;
        if(newLayer > quantaPerBlock) return false;
        final boolean isFinite = block instanceof BlockFluidFinite;
        FluidState newState = isFinite?FluidState.of(fluid).withLevel(newLayer-1):FluidState.of(fluid).withLevel(quantaPerBlock-newLayer);
        return FluidloggedUtils.setFluidState(world,pos,state,newState,false,APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT, disabledBlockFlags,enabledBlockFlags));
    }

    @Override
    default boolean canFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, @Nonnull EnumFacing side, @Nullable IBlockState source) {
        if(!isFluidValid(state,world, pos, fluid)) return false;
        if(isFull(world, pos, state, fluid)) return false;
        return canFluidFlow(world,pos,state,side);
    }

    @Override
    default boolean canDrain(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, @Nonnull EnumFacing side, @Nullable IBlockState source) {
        final FluidState fluidState = FluidState.get(world,pos);
        return !fluidState.isEmpty() && fluidState.getFluid() == fluid;
    }

    @Override
    default boolean isFull(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        final FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return fluid != null && !isFluidValid(state,world,pos,fluid);
        if(fluid != null && fluid != fluidState.getFluid()) return false;
        return fluidState.getQuantaPerBlock() == fluidState.getQuantaValue();
    }

    @Override
    default boolean isEmpty(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        final FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return fluid == null || isFluidValid(state,world,pos,fluid);
        return false;
    }
}
