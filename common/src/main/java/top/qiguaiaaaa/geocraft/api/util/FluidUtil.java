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

package top.qiguaiaaaa.geocraft.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.util.exception.UnsupportedFluidException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class FluidUtil {
    public static final int ONE_IN_EIGHT_OF_BUCKET_VOLUME = Fluid.BUCKET_VOLUME/8;

    /**
     * 对应方块是否是一个液体
     * @param state 方块状态
     */
    public static boolean isFluid(@Nonnull IBlockState state) {
        return state.getMaterial().isLiquid() || state.getBlock() instanceof IFluidBlock;
    }
    /**
     * 对应方块是否是一个液体
     * @param block 方块
     */
    public static boolean isFluid(@Nonnull Block block) {
        return block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    /**
     * 检测是否是一个完整的流体方块
     * @param state 状态
     * @return 检测结果
     * @throws UnsupportedFluidException 当无法获取对应流体的等级时抛出
     */
    public static boolean isFullFluid(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state){
        if(!isFluid(state)) return false;
        Block block = state.getBlock();
        if(block instanceof BlockLiquid || block instanceof BlockFluidClassic){
            return state.getValue(LEVEL) == 0;
        }else if(block instanceof BlockFluidFinite){
            BlockFluidFinite fluidBlock = (BlockFluidFinite) block;
            return fluidBlock.getQuantaPercentage(world,pos) == 1f;
        }else if(block instanceof IFluidBlock){
            IFluidBlock fluidBlock = (IFluidBlock) block;
            FluidStack stack = fluidBlock.drain(world,pos,false);
            if(stack == null) return false;
            return stack.amount == Fluid.BUCKET_VOLUME;
        }
        try{
            return state.getValue(LEVEL) == 0;
        }catch (final Exception ignore){}
        try {
            return state.getValue(BlockFluidBase.LEVEL) == 0;
        }catch (final Exception e){
            throw new UnsupportedFluidException(state.getBlock());
        }
    }

    public static boolean isFluidPlaceable(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull Fluid fluid){
        IBlockState state = world.getBlockState(pos);
        return FluidPhysicsMode.getCurrentMode().getChecker().canPlaceAt(world,pos,state,fluid);
    }

    public static boolean isFluidPlaceablePermeable(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull Fluid fluid,boolean allowAir){
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if(block instanceof ILayeredFluidHost){
            return  ((ILayeredFluidHost)block).isAcceptedFluid(world,pos,state,fluid);
        }
        return allowAir && isFluidPlaceable(world, pos, fluid);
    }

    /**
     * 获取指定方块状态的液体
     * @param state 方块状态
     * @return 指定方块状态的液体，若没有则返回null
     */
    @Nullable
    public static Fluid getFluid(@Nonnull final IBlockState state) {
        final Block block = state.getBlock();

        if (block instanceof IFluidBlock) {
            return ((IFluidBlock)block).getFluid();
        }else if (block instanceof BlockLiquid) {
            if (state.getMaterial() == Material.WATER) {
                return FluidRegistry.WATER;
            }else if (state.getMaterial() == Material.LAVA) {
                return FluidRegistry.LAVA;
            }
        }
        return null;
    }

    @Nullable
    @Deprecated
    public static Fluid getFluid(@Nonnull final Material material){
        if(material == Material.WATER) return FluidRegistry.WATER;
        else if(material == Material.LAVA) return FluidRegistry.LAVA;
        return null;
    }


    /**
     * 获取指定方块的液体
     * @param block 方块
     * @return 指定方块的液体，若没有则返回null
     */
    @Nullable
    public static Fluid getFluid(@Nonnull final Block block) {
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock)block).getFluid();
        }else if (block instanceof BlockLiquid) {
            final Material material = block.getDefaultState().getMaterial();
            if (material == Material.WATER) {
                return FluidRegistry.WATER;
            }else if (material == Material.LAVA) {
                return FluidRegistry.LAVA;
            }
        }
        return null;
    }

    /**
     * 获得溶液方块剩余量,单位量
     * @param worldIn 所在世界
     * @param pos 方块位置
     * @param state 方块状态
     * @return 一个数值，表示剩余的量
     */
    public static int getFluidQuanta(@Nonnull World worldIn,@Nonnull BlockPos pos,@Nonnull IBlockState state){
        if(!isFluid(state)) return 0;
        if(state.getBlock() instanceof BlockFluidBase){
            BlockFluidBase fluidBase = (BlockFluidBase) state.getBlock();
            return Math.max(fluidBase.getQuantaValue(worldIn,pos),0);
        }
        if(state.getBlock() instanceof IFluidBlock){
            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
            FluidStack fluidStack = fluidBlock.drain(worldIn,pos,false);
            if(fluidStack == null) return 0;
            return fluidStack.amount/ONE_IN_EIGHT_OF_BUCKET_VOLUME;
        }
        int stateValue = state.getValue(BlockLiquid.LEVEL);
        if(stateValue>=8) return 1;
        else return 8-stateValue;
    }

    /**
     * 获得溶液方块剩余量,单位mB
     * @param worldIn 所在世界
     * @param pos 方块位置
     * @param state 方块状态
     * @return 方块剩余量
     */
    public static int getFluidAmount(@Nonnull World worldIn,@Nonnull BlockPos pos,@Nonnull IBlockState state){
        if(!isFluid(state)) return 0;
        if(state.getBlock() instanceof IFluidBlock){
            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
            FluidStack fluidStack = fluidBlock.drain(worldIn,pos,false);
            if(fluidStack == null)return 0;
            return fluidStack.amount;
        }
        int stateValue = state.getValue(BlockLiquid.LEVEL);
        if(stateValue>=8) return 0;
        else return (8-stateValue)*ONE_IN_EIGHT_OF_BUCKET_VOLUME;
    }

    /**
     * 计算液体流动难度（坡度的余切）
     * @param distance 距离
     * @param heightDiff 难易度
     * @return 坡度的余切
     */
    public static double getFlowDifficulty(int distance,int heightDiff){
        return distance/((double)heightDiff);
    }

}
