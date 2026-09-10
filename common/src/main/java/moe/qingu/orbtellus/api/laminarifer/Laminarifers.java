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

import moe.qingu.orbtellus.api.fluid.unit.FluidUnit;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifiers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QGMoe
 */
public final class Laminarifers {
    private Laminarifers(){}

    /**
     * 指定方块状态是否是一个载流方块的方块状态
     * @param state 需要检查的方块状态
     * @return 若该方块状态是一个载流方块的方块状态,则返回 true.否则返回 false
     */
    public static boolean isLaminarifer(final @Nonnull IBlockState state){
        return state.getBlock() instanceof ILaminarifer;
    }

    /**
     * 指定方块是否是一个载流方块
     * @param block 需要检查的方块
     * @return 若该方块是一个载流方块,则返回 true.否则返回 false
     */
    public static boolean isLaminarifer(final @Nullable Block block){
        return block instanceof ILaminarifer;
    }


    /* =========================================
                    基本状态查询
       ========================================= */


    /**
     * 在指定位置，以给定的方块状态，该载流方块的指定流体是否已经装满
     * @since API-0.3.5
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 若不支持含有该流体或当前流体已满，则返回true，否则为false
     */
    public static boolean isFull(@Nonnull final ILaminarifer laminarifer,
                                 @Nonnull final World world,
                                 @Nonnull final BlockPos pos,
                                 @Nonnull final IBlockState state,
                                 @Nonnull final Fluid fluid,
                                 @Nullable final NBTTagCompound nbt) {
        return laminarifer.getLayers(world, pos, state, fluid, nbt) >= laminarifer.getMaxLayers(world, pos, state, fluid, nbt);
    }

    /**
     * @see #isFull(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound)
     */
    public static boolean isFull(@Nonnull final IBlockStateLaminarifer laminarifer,
                                 @Nonnull final IBlockState state,
                                 @Nonnull final Fluid fluid,
                                 @Nullable final NBTTagCompound nbt) {
        return laminarifer.getLayers(state, fluid, nbt) >= laminarifer.getMaxLayers(state, fluid, nbt);
    }

    /**
     * 在指定位置，以给定的方块状态，该载流方块指定流体是否是空的，且还有空间存入该流体
     * @since API-0.3.5
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 若指定流体是空的，且还有空间存入该流体，则返回 true，否则返回 false
     */
    public static boolean isEmpty(@Nonnull final ILaminarifer laminarifer,
                                  @Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nonnull final Fluid fluid,
                                  @Nullable final NBTTagCompound nbt) {
        if (isFull(laminarifer,world, pos, state, fluid, nbt)) return false;
        return laminarifer.getLayers(world, pos, state, fluid, nbt) <= 0L;
    }

    /* =========================================
                      流体储存语义
       ========================================= */

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块承载指定流体的量，以 QB 为单位
     * @since API-0.3.5
     * @param laminarifer 载流方块
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，方块承载指定流体的量，以 QB 为单位
     */
    public static long getAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt) {
        return laminarifer.getLayers(world, pos, state, fluid, nbt) * laminarifer.getAmountInQBPerLayer(world, pos, state, fluid, nbt);
    }

    /**
     * @see #getAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound)
     */
    public static long getAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid) {
        return laminarifer.getLayers(world, pos, state, fluid, null) * laminarifer.getAmountInQBPerLayer(world, pos, state, fluid, null);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试输入指定量的指定流体，单位为 QB。
     * 在添加前一般需要检测{@link ILaminarifer#canFill}
     * @since API-0.3.5
     * @param laminarifer        载流方块
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致。
     * @param fluid              要添加的流体
     * @param nbt                添加的流体的 NBT 复合标签
     * @param amount             流体量，单位为 QB
     * @param doOperate          进行操作
     * @param pulse              来势脉冲
     * @param source             流体来源
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source,
                                     final long blockFlagsModifier){
        if(amount <= 0L) return 0L;
        final long amountPerLayer = laminarifer.getAmountInQBPerLayer(world, pos, state, fluid, nbt);
        final long curAmount = getAmountInQB(laminarifer, world, pos, state, fluid, nbt);
        final long amountInFact = Math.min(amount, laminarifer.getMaxAmountInQB(world, pos, state, fluid, nbt)-curAmount);
        if(amountInFact <= 0L) return 0L;
        return laminarifer.addLayer(world,pos,state,fluid,nbt, FluidUnit.sample(world.rand, amountInFact, amountPerLayer),doOperate,pulse,source,blockFlagsModifier)*amountPerLayer;
    }


    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,nbt, amount, doOperate, pulse, source, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     final long blockFlagsModifier){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,nbt, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,nbt, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long) 
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source,
                                     final long blockFlagsModifier){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,null, amount, doOperate, pulse, source, blockFlagsModifier);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,null, amount, doOperate, pulse, source, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse,
                                     final long blockFlagsModifier){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,null, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     long amount,
                                     final boolean doOperate,
                                     final long pulse){
        return addAmountInQB(laminarifer, world, pos, state, fluid ,null, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final QBFluidStack stack,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source,
                                     final long blockFlagsModifier){
        return addAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, source, blockFlagsModifier);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final QBFluidStack stack,
                                     final boolean doOperate,
                                     final long pulse,
                                     @Nullable final IFlowSource<?> source){
        return addAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, source, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final QBFluidStack stack,
                                     final boolean doOperate,
                                     final long pulse,
                                     final long blockFlagsModifier){
        return addAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #addAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowSource, long)
     */
    public static long addAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                     @Nonnull final World world,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final QBFluidStack stack,
                                     final boolean doOperate,
                                     final long pulse){
        return addAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试抽取指定量的指定流体，单位为 QB。
     * 在抽取前一般需要检测{@link ILaminarifer#canDrain}
     * @since API-0.3.5
     * @param laminarifer        载流方块
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致。
     * @param fluid              要抽取的流体
     * @param nbt                添抽取流体的 NBT 复合标签
     * @param amount             流体量，单位为 QB
     * @param doOperate          进行操作
     * @param pulse              去势脉冲
     * @param drainer            抽取者
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         @Nullable final NBTTagCompound nbt,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer,
                                         final long blockFlagsModifier){
        if(amount <= 0L) return 0L;
        final long amountPerLayer = laminarifer.getAmountInQBPerLayer(world, pos, state, fluid, nbt);
        final long curAmount = getAmountInQB(laminarifer, world, pos, state, fluid, nbt);
        final long drainedInFact = Math.min(amount,curAmount);
        if(drainedInFact <= 0) return 0;
        return laminarifer.drainLayer(world,pos,state,fluid,nbt,FluidUnit.sample(world.rand, drainedInFact, amountPerLayer),doOperate,pulse,drainer,blockFlagsModifier)*amountPerLayer;
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         @Nullable final NBTTagCompound nbt,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, nbt, amount, doOperate, pulse, drainer, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         @Nullable final NBTTagCompound nbt,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         final long blockFlagsModifier){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, nbt, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         @Nullable final NBTTagCompound nbt,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, nbt, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer,
                                         final long blockFlagsModifier){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, null, amount, doOperate, pulse, drainer, blockFlagsModifier);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, null, amount, doOperate, pulse, drainer, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse,
                                         final long blockFlagsModifier){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, null, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final Fluid fluid,
                                         long amount,
                                         final boolean doOperate,
                                         final long pulse){
        return extractAmountInQB(laminarifer, world, pos, state, fluid, null, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final QBFluidStack stack,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer,
                                         final long blockFlagsModifier){
        return extractAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, drainer, blockFlagsModifier);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final QBFluidStack stack,
                                         final boolean doOperate,
                                         final long pulse,
                                         @Nullable final IFlowDrainer<?> drainer){
        return extractAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, drainer, BlockFlagModifiers.KEEP);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final QBFluidStack stack,
                                         final boolean doOperate,
                                         final long pulse,
                                         final long blockFlagsModifier){
        return extractAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see #extractAmountInQB(ILaminarifer, World, BlockPos, IBlockState, Fluid, NBTTagCompound, long, boolean, long, IFlowDrainer, long)
     */
    public static long extractAmountInQB(@Nonnull final ILaminarifer laminarifer,
                                         @Nonnull final World world,
                                         @Nonnull final BlockPos pos,
                                         @Nonnull final IBlockState state,
                                         @Nonnull final QBFluidStack stack,
                                         final boolean doOperate,
                                         final long pulse){
        return extractAmountInQB(laminarifer, world, pos, state, stack.getFluid(), stack.tag, stack.amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              @Nullable final Fluid fluid,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse,
                                              @Nullable final IFlowDrainer<?> drainer){
        return laminarifer.drainStackInQB(world, pos, state, fluid, amount, doOperate, pulse, drainer, BlockFlagModifiers.KEEP);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              @Nullable final Fluid fluid,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse,
                                              final long blockFlagsModifier){
        return laminarifer.drainStackInQB(world, pos, state, fluid, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              @Nullable final Fluid fluid,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse){
        return laminarifer.drainStackInQB(world, pos, state, fluid, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse,
                                              @Nullable final IFlowDrainer<?> drainer,
                                              final long blockFlagsModifier){
        return laminarifer.drainStackInQB(world, pos, state, null, amount, doOperate, pulse, drainer, blockFlagsModifier);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse,
                                              final long blockFlagsModifier){
        return laminarifer.drainStackInQB(world, pos, state, null, amount, doOperate, pulse, null, blockFlagsModifier);
    }

    /**
     * @see ILaminarifer#drainStackInQB
     */
    @Nullable
    public static QBFluidStack drainStackInQB(@Nonnull final ILaminarifer laminarifer,
                                              @Nonnull final World world,
                                              @Nonnull final BlockPos pos,
                                              @Nonnull final IBlockState state,
                                              long amount,
                                              final boolean doOperate,
                                              final long pulse){
        return laminarifer.drainStackInQB(world, pos, state, null, amount, doOperate, pulse, null, BlockFlagModifiers.KEEP);
    }
}
