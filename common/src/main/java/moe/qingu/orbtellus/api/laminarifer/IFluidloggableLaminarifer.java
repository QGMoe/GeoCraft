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

package moe.qingu.orbtellus.api.laminarifer;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import git.jbredwards.fluidlogged_api.mod.asm.plugins.forge.PluginBlockFluidBase;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 写好了，但还没有测试。<br/>
 * 一个让含水方块和载流方块兼容的接口，为含水方块实现了载流方块的默认行为。
 * @author QiguaiAAAA
 */
public interface IFluidloggableLaminarifer extends IFluidloggable, ILaminarifer {
    int DEFAULT_QUANTA_PER_BLOCK = 8;

    /* =========================================
                     交互许可
       ========================================= */

    @Override
    default boolean isAcceptedFluid(@Nonnull final World world,
                                    @Nonnull final BlockPos pos,
                                    @Nonnull final IBlockState state,
                                    @Nonnull final Fluid fluid,
                                    @Nullable final NBTTagCompound nbt){
        return isFluidValid(state,world,pos,fluid);
    }

    @Override
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final NBTTagCompound nbt,
                            @Nullable final IFlowSource<?> source) {
        if(!isFluidValid(state,world, pos, fluid)) return false;
        if(Laminarifers.isFull(this, world, pos, state, fluid, null)) return false;
        if(side == null){
            for(final EnumFacing facing:EnumFacing.VALUES) if(canFluidFlow(world,pos,state,facing)) return true;
            return false;
        }else return canFluidFlow(world,pos,state,side);
    }

    @Override
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             @Nullable final IFlowDrainer<?> drainer) {
        final @Nonnull FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.getFluid() != fluid) return false;
        if(side == null){
            for(final EnumFacing facing:EnumFacing.VALUES) if(canFluidFlow(world,pos,state,facing)) return true;
            return false;
        }else return canFluidFlow(world,pos,state,side);
    }

    /* =========================================
                   分层流体承载方块模型
       ========================================= */

    @Override
    default long getMaxLayers(@Nonnull final World world,
                              @Nonnull final BlockPos pos,
                              @Nonnull final IBlockState state,
                              @Nonnull final Fluid fluid,
                              @Nullable final NBTTagCompound nbt) {
        final @Nonnull FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()){
            final Block block = fluid.getBlock();
            if(block instanceof PluginBlockFluidBase.Accessor)
                return ((PluginBlockFluidBase.Accessor)block).getQuantaPerBlock_Public();
            return DEFAULT_QUANTA_PER_BLOCK;
        }else if(fluidState.getFluid() == fluid){
            return fluidState.getQuantaPerBlock();
        }
        return 0;
    }

    @Override
    default long getLayers(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt){
        final @Nonnull FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty()) return 0L;
        return fluidState.getFluid() == fluid?fluidState.getQuantaValue():0L;
    }


    @Override
    default long getHeightPerLayer(@Nonnull final World world,
                                   @Nonnull final BlockPos pos,
                                   @Nonnull final IBlockState state,
                                   @Nonnull final Fluid fluid,
                                   @Nullable final NBTTagCompound nbt){
        final Block block = fluid.getBlock();
        if(block instanceof PluginBlockFluidBase.Accessor){
            return AHUnit.BLOCK_HEIGHTS.get(((PluginBlockFluidBase.Accessor) block).getQuantaPerBlock_Public()+1);
        }else return AHUnit.EIGHTH_FLUID;
    }

    @Override
    default long getEmptyHeight(@Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nonnull final Fluid fluid,
                                @Nullable final NBTTagCompound nbt){
        return 0L;
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nonnull final Fluid fluid,
                                       @Nullable final NBTTagCompound nbt){
        final Block block = fluid.getBlock();
        if(block instanceof PluginBlockFluidBase.Accessor){
            return QBUnit.VOLUMES_1_TO_16.get(((PluginBlockFluidBase.Accessor) block).getQuantaPerBlock_Public());
        }else return QBUnit.QUANTA_VOLUME;
    }

    @Override
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             final long newLayer,
                             final long blockFlagsModifier){
        if(newLayer == 0L){
            FluidloggedUtils.setFluidState(world, pos, state, FluidState.EMPTY,false, BlockFlagModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier));
            return true;
        } else if(newLayer < 0L) return false;
        else {
            final int quantaPerBlock;
            final Block block = fluid.getBlock();
            if(block instanceof PluginBlockFluidBase.Accessor){
                quantaPerBlock = ((PluginBlockFluidBase.Accessor)block).getQuantaPerBlock_Public();
            }else quantaPerBlock = DEFAULT_QUANTA_PER_BLOCK;
            if(newLayer > quantaPerBlock) return false;
            final boolean isFinite = block instanceof BlockFluidFinite;
            final @Nonnull FluidState newState = isFinite?FluidState.of(fluid).withLevel((int) (newLayer-1)):FluidState.of(fluid).withLevel((int) (quantaPerBlock-newLayer));
            FluidloggedUtils.setFluidState(world,pos,state,newState,false, BlockFlagModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier));
            return true;
        }
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
        final @Nonnull FluidState fluidState = FluidState.get(world,pos);
        if(fluidState.isEmpty() || fluidState.getFluid() != fluid) return null;
        return new QBFluidStack(
                fluidState.getFluid(),
                Laminarifers.extractAmountInQB(this,world,pos,state,fluidState.getFluid(),null,amount,doOperate,pulse,drainer,blockFlagsModifier)
        );
    }
}
