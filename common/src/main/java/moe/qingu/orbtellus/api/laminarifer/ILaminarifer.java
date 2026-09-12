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

import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 载流方块，全称分层流体承载方块，英文名 Laminarifer Block <br/>
 * 注意这和含水方块有本质区别，含水方块是载流方块的子集。例如，泥土不是含水方块，但应该为载流方块。<br/>
 * 一个载流方块可以承载多种流体，每个流体可以视为存在一个虚拟的容器，类似于 {@link net.minecraftforge.fluids.capability.IFluidHandler} 之于 {@link net.minecraftforge.fluids.IFluidTank} 的关系
 * @author QGMoe
 * @since API-0.3.5
 */
public interface ILaminarifer{

    /* =========================================
                     交互许可
       ========================================= */

    /**
     * 在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块是否能够承载指定的流体
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 指定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 询问的流体种类
     * @param nbt   流体的附加 NBT
     * @return 给定的流体在当前条件下是否被允许流入指定的方块
     * @since API-0.3.5
     */
    boolean isAcceptedFluid(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nonnull final Fluid fluid,
                            @Nullable final NBTTagCompound nbt);

    /**
     * 在指定位置，以给定的方块状态，指定流体是否能够以指定条件流入当前载流方块的指定面
     * @since API-0.3.5
     * @param world  所在世界
     * @param pos    方块位置
     * @param state  给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side   流入方块的面，为 null 表示不考虑面
     * @param fluid  需要流入的流体
     * @param nbt    流体的附加 NBT
     * @param source 流体来源
     * @return 若可以，则返回 true
     */
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final NBTTagCompound nbt,
                            @Nullable final IFlowSource<?> source) {
        return isAcceptedFluid(world, pos, state, fluid, nbt) && !Laminarifers.isFull(this, world, pos, state, fluid, nbt);
    }

    /**
     * 在指定位置，以给定的方块状态，是否能够以指定条件从当前载流方块的指定面抽取指定的流体
     * @since API-0.3.5
     * @param world   所在世界
     * @param pos     方块位置
     * @param state   给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side    抽取方块的面，为 null 表示不考虑面
     * @param fluid   需要抽取的流体
     * @param drainer 抽取者
     * @return 若可以，则返回 true
     */
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             @Nullable final IFlowDrainer<?> drainer) {
        return getLayers(world, pos, state, fluid, nbt) != 0;
    }

    /* =========================================
                   分层流体承载方块模型
       ========================================= */

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体的载流方块模型。
     * @param world  所在世界
     * @param pos    方块位置
     * @param state  给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid  查询的流体
     * @param nbt    流体的附加 NBT
     * @param buffer 载流方块模型输出
     */
    default void describeModel(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt,
                               final @Nonnull LaminariferModelBuffer buffer){
        buffer.maxLayers = this.getMaxLayers(world, pos, state, fluid, nbt);
        buffer.currentLayers = this.getLayers(world, pos, state, fluid, nbt);
        buffer.heightPerLayer = this.getHeightPerLayer(world, pos, state, fluid, nbt);
        buffer.emptyHeight = this.getEmptyHeight(world, pos, state, fluid, nbt);
        buffer.amountInQBPerLayer = this.getAmountInQBPerLayer(world, pos, state, fluid, nbt);
    }

    /// 层状结构的定义

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体的最大层数。
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，对应流体的层数最大值
     * @since GeoCraftAPI 0.3.1
     */
    long getMaxLayers(@Nonnull final World world,
                      @Nonnull final BlockPos pos,
                      @Nonnull final IBlockState state,
                      @Nonnull final Fluid fluid,
                      @Nullable final NBTTagCompound nbt);

    /// 层状结构的查询

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块承载指定流体的层数
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，方块承载指定流体的层数
     * @since API-0.3.5
     */
    long getLayers(@Nonnull final World world,
                   @Nonnull final BlockPos pos,
                   @Nonnull final IBlockState state,
                   @Nonnull final Fluid fluid,
                   @Nullable final NBTTagCompound nbt);

    /// 空间高度的定义

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体被承载时的基准高度。
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt 流体的附加 NBT
     * @return 在给定条件下，指定流体的基准高度。
     * @since API-0.3.5
     */
    long getEmptyHeight(@Nonnull final World world,
                       @Nonnull final BlockPos pos,
                       @Nonnull final IBlockState state,
                       @Nonnull final Fluid fluid,
                       @Nullable final NBTTagCompound nbt);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块每层流体的高度
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @return 在给定条件下，该方块每层流体的高度，单位为 AH。
     * @implSpec 返回的值必须是 {@link #getMaxHeight} - {@link #getEmptyHeight} 的因数
     * @since API-0.3.5
     */
    long getHeightPerLayer(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体能够有的最高表面高度
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt 流体的附加 NBT
     * @return 在给定条件下，指定流体能够有的最高表面高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     * @since API-0.3.5
     */
    default long getMaxHeight(@Nonnull final World world,
                     @Nonnull final BlockPos pos,
                     @Nonnull final IBlockState state,
                     @Nonnull final Fluid fluid,
                     @Nullable final NBTTagCompound nbt){
        return getEmptyHeight(world, pos, state, fluid, nbt) + getMaxLayers(world, pos, state, fluid, nbt) * getHeightPerLayer(world, pos, state, fluid, nbt);
    }

    /// 空间高度的查询

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体的表面高度
     *
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，流体表面的高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     * @since API-0.3.5
     */
    default long getHeight(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt) {
        return getEmptyHeight(world, pos, state, fluid, nbt) + getLayers(world, pos, state, fluid, nbt) * getHeightPerLayer(world, pos, state, fluid, nbt);
    }

    /* =========================================
                   方块间流体交互
       ========================================= */

    /**
     * 在指定位置，以给定的方块状态，将指定流体的层数设置为指定层数。
     *
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid              指定流体
     * @param nbt                流体的 NBT 标签
     * @param newLayer           新的层数
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 操作是否成功
     * @apiNote 原位置需要是同种的载流方块，该方法应当只会修改指定流体的层数。在大多数情况下，同位置的其他流体的含量不会发生变化。
     * 但在特殊情况下，方块发生变化是允许的，甚至可能会与其他游戏机制相互作用导致方块变成其他方块，因此在操作完成之后务必调用 {@link World#getBlockState(BlockPos)} 获取实际状态。
     * @since API-0.3.5
     */
    boolean setLayer(@Nonnull final World world,
                     @Nonnull final BlockPos pos,
                     @Nonnull final IBlockState state,
                     @Nonnull final Fluid fluid,
                     @Nullable final NBTTagCompound nbt,
                     final long newLayer,
                     final long blockFlagsModifier);

    /**
     * 在指定位置，以给定的方块状态，尝试以改载流方块的层数标准输入指定层数的指定流体。
     * 在添加前一般需要检测{@link #canFill}
     *
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致。
     * @param fluid              要添加的流体
     * @param nbt                添加的流体的 NBT 复合标签
     * @param layer              层数
     * @param doOperate          进行操作
     * @param pulse              流强脉冲
     * @param source             流体来源
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 在给定条件下，实际添加的层数
     * @since API-0.3.5
     */
    default long addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nonnull final Fluid fluid,
                         @Nullable final NBTTagCompound nbt,
                         final long layer,
                         final boolean doOperate,
                         final long pulse,
                         @Nullable final IFlowSource<?> source,
                         final long blockFlagsModifier) {
        if (layer <= 0) return 0;
        final long curLayer = getLayers(world, pos, state, fluid, nbt);
        final long addedInFact = Math.min(layer, getMaxLayers(world, pos, state, fluid, nbt) - curLayer);
        if (addedInFact <= 0) return 0;
        if (doOperate) {
            final long newLayer = curLayer + addedInFact;
            this.setLayer(world, pos, state, fluid, nbt, newLayer, blockFlagsModifier);
        }
        return addedInFact;
    }

    /**
     * 在指定位置，以给定的方块状态，尝试抽取指定层数的指定流体。
     * 在抽取前一般需要检测{@link #canDrain}
     *
     * @since API-0.3.5
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致。
     * @param fluid              要抽取的流体
     * @param nbt                要抽取流体的附加 NBT 复合标签
     * @param layer              层数
     * @param doOperate          进行操作
     * @param pulse              流强脉冲
     * @param drainer            抽取者
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 在给定条件下，实际抽取的流体层数
     */
    default long drainLayer(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final NBTTagCompound nbt,
                           final long layer,
                           final boolean doOperate,
                           final long pulse,
                           @Nullable final IFlowDrainer<?> drainer,
                           final long blockFlagsModifier){
        if(layer <= 0) return 0;
        final long curLayer = getLayers(world,pos,state,fluid,nbt);
        final long drainedInFact = Math.min(layer, curLayer);
        if(drainedInFact <= 0) return 0;
        if(doOperate){
            final long newLayer = curLayer - drainedInFact;
            this.setLayer(world,pos,state,fluid,nbt,newLayer,blockFlagsModifier);
        }
        return drainedInFact;
    }

    /* =========================================
                      流体储存语义
       ========================================= */

    /// 流体存储的定义

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块承载指定流体所用的每层容量大小，以 QB 为单位
     * @since API-0.3.5
     * @param world 所在世界
     * @param pos   方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，单层指定流体以 QB 为单位记的容量
     */
    long getAmountInQBPerLayer(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定流体的最大含量，单位为 QB
     * @since API-0.3.5
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param fluid 查询的流体
     * @param nbt   流体的附加 NBT
     * @return 在给定条件下，对应流体的最大含量，单位为 QB
     */
    default long getMaxAmountInQB(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nonnull final Fluid fluid,
                                  @Nullable final NBTTagCompound nbt) {
        return getMaxLayers(world, pos, state, fluid, nbt) * getAmountInQBPerLayer(world, pos, state, fluid, nbt);
    }

    /// 流体存储的操作

    /**
     * 在指定位置，以给定的方块状态，尝试抽取指定量的满足指定条件的流体，单位为 QB。
     * 在抽取前一般需要检测{@link #canDrain}
     * @since API-0.3.5
     * @param world              所在世界
     * @param pos                方块位置
     * @param state              给定的方块状态，必须要满足与该位置实际的方块状态一致。
     * @param fluid              要抽取的流体类型，可以为 null，表示不限定流体类型
     * @param amount             流体量，单位为 QB
     * @param doOperate          进行操作
     * @param pulse              流强脉冲
     * @param drainer            抽取者
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagModifier} 构建
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    @Nullable
    QBFluidStack drainStackInQB(@Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nullable final Fluid fluid,
                                final long amount,
                                final boolean doOperate,
                                final long pulse,
                                @Nullable final IFlowDrainer<?> drainer,
                                final long blockFlagsModifier);
}
