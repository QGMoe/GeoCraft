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

package top.qiguaiaaaa.geocraft.util.wrappers;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.math.PlaceChoice;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.VanillaFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.fluid.FluidSearchUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FiniteBlockLiquidWrapper extends BlockLiquidWrapper {
    protected final @Nonnull VanillaFlowingVanilla flowing;
    protected boolean ignoreCurrentPos = false;

    public FiniteBlockLiquidWrapper(final @Nonnull VanillaFlowingVanilla flowing,
                                    final @Nonnull World world,
                                    final @Nonnull BlockPos blockPos) {
        super(flowing.dynamic, world, blockPos);
        this.flowing = flowing;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        FluidStack stack= null;
        final IBlockState state = world.getBlockState(blockPos);
        if (state.getBlock() == blockLiquid) {
            stack = getStackByMaxQuanta(state,16);
        }
        return new FluidTankProperties[]{new FluidTankProperties(stack, Fluid.BUCKET_VOLUME, false, true)};
    }

    @Override
    public int fill(final @Nonnull FluidStack resource,final boolean doFill) {
        final int expectedQuanta = Math.min(8,resource.amount/FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME);
        if(expectedQuanta <= 0) return 0;
        final int expectedAmount = expectedQuanta *FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;

        final @Nonnull Set<PlaceChoice> choices = FluidSearchUtil.findPlaceableLocations(world,blockPos,flowing.fluid,8,ignoreCurrentPos,null);
        if(choices.isEmpty()) return 0;
        int quantaLeft = expectedQuanta;
        for(final @Nonnull PlaceChoice choice:choices){
            quantaLeft = placeLiquid(choice.pos,quantaLeft,doFill);
            if(quantaLeft<=0) break;
        }
        if(quantaLeft <=0) return expectedAmount;
        return (expectedQuanta-quantaLeft)*FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
    }

    public void setIgnoreCurrentPos(final boolean ignoreCurrentPos) {
        this.ignoreCurrentPos = ignoreCurrentPos;
    }

    /**
     * 在某处放置指定量的液体
     * @param placePos 位置
     * @param placeQuanta 放置量
     * @return 剩余未放置的量
     */
    protected int placeLiquid(final @Nonnull BlockPos placePos,final int placeQuanta,final boolean doFill){
        if(placeQuanta <=0) return 0;
        final @Nonnull IBlockState state = world.getBlockState(placePos);
        final int quanta = FluidUtil.getFluidQuanta(world,placePos,state);
        final int newQuanta = quanta+placeQuanta;
        if(newQuanta<=8){
            if(doFill) directlyPlaceLiquid(placePos,8-newQuanta);
            return 0;
        }
        if(doFill) directlyPlaceLiquid(placePos,0);
        return newQuanta-8;
    }

    protected void directlyPlaceLiquid(BlockPos placePos,int level){
        world.setBlockState(placePos, flowing.dynamic.getDefaultState().withProperty(BlockLiquid.LEVEL, level), 11);
    }

    @Nullable
    @Override
    public FluidStack drain(final FluidStack resource,final boolean doDrain) {
        if (resource == null) {
            return null;
        }
        return drain(resource.getFluid(),resource.amount,doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(final int maxDrain,final boolean doDrain) {
        return drain(null,maxDrain,doDrain);
    }

    @Nullable
    private FluidStack drain(final @Nullable Fluid fluid,final int maxDrain,final boolean doDrain) {
        if (maxDrain < FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME) {
            return null;
        }

        final int maxDrainQuanta = maxDrain / FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
        final @Nonnull IBlockState state = world.getBlockState(blockPos);
        if (state.getBlock() == flowing.dynamic || state.getBlock() == flowing._static) {
            final @Nullable FluidStack stack = getStackByMaxQuanta(state,maxDrainQuanta);
            if (stack != null && (fluid == null || stack.getFluid().equals(fluid))) {
                if (doDrain) {
                    final int leftQuanta = getQuantaOf(state) - stack.amount/FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
                    final @Nonnull IBlockState newState = leftQuanta>0?flowing.dynamic.getDefaultState().withProperty(BlockLiquid.LEVEL,8-leftQuanta):Blocks.AIR.getDefaultState();
                    world.setBlockState(blockPos, newState, Constants.BlockFlags.DEFAULT_AND_RERENDER);
                }
                return stack;
            }
        }
        return null;
    }

    private int getQuantaOf(@Nonnull final IBlockState state){
        final int level = state.getValue(BlockLiquid.LEVEL);
        return level>7?1:8-level;
    }

    @Nullable
    private FluidStack getStackByMaxQuanta(final @Nonnull IBlockState state,final int maxQuanta) {
        final @Nonnull Material material = state.getMaterial();
        final int quanta = Math.min(getQuantaOf(state),maxQuanta);
        if (material == Material.WATER) {
            return new FluidStack(FluidRegistry.WATER, FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME*quanta);
        } else if (material == Material.LAVA) {
            return new FluidStack(FluidRegistry.LAVA, FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME*quanta);
        } else return null;
    }
}
