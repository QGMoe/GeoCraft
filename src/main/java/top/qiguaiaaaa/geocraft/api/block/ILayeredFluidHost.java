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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidFrom;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidTo;
import top.qiguaiaaaa.geocraft.api.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 载流方块，全称分层流体承载方块，英文名 Layered Fluid Host Block <br/>
 * 注意这和含水方块有本质区别，含水方块是载流方块的子集。例如，泥土不是含水方块，但应该为载流方块。<br/>
 * 载流方块的结构有三层，分别为最外层的方块，中层的流体容器，底层的流体层。一个方块在同一时刻，每个方向都对应一个流体容器，一个流体容器可以对应多个方向。
 * 每个流体容器可以容纳多种流体，相同流体以层为单位在垂直方向上堆叠形成流体柱，同一个流体容器每层的高度是一致的，但同一个流体容器的不同液体其每层可以含有不同的流体量。
 * 对于一个流体容器内某特定流体形成的流体柱，其在空间位置上仅由 Y 坐标体现，并可以在同一流体容器内和其他不同流体柱存在高度范围上的重叠。不同流体容器也可以在高度范围上重叠。
 * @author QiguaiAAAA
 * @implNote 对于依赖外部数据进行状态存储的方块，例如状态实体、流体状态（FluidloggedAPI）之类的方块，建议在假设情况下以默认的状态为准。
 * 当然如果觉得实现非常复杂，可以干脆在假设情况下完全返回 false，就目前为止，这样的实现下不会有太大的问题。
 */
public interface ILayeredFluidHost {

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块在没有提供面参数时，默认使用作为流体容器的面
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @return 在给定条件下，默认使用作为流体容器的面，可以为 null。
     * 特别的，当为 null 时表示该方块只有且只能有一个流体容器。
     */
    @Nullable
    default EnumFacing getDefaultSide(@Nonnull final World world,
                              @Nonnull final BlockPos pos,
                              @Nonnull final IBlockState state){
        return getDefaultSide(world,pos,state,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块在没有提供面参数时，默认使用作为流体容器的面
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，默认使用作为流体容器的面，可以为 null。
     * 特别的，当为 null 时表示该方块一定只有且只能有一个流体容器。
     */
    @Nullable
    EnumFacing getDefaultSide(@Nonnull final World world,
                              @Nonnull final BlockPos pos,
                              @Nonnull final IBlockState state,
                              final boolean isAssumed);

    /**
     * 在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器是否能够承载指定的流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 指定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 询问的流体
     * @return 给定的流体在当前条件下是否被允许流入指定的方块
     */
    default boolean isAcceptedFluid(@Nonnull final World world,
                                    @Nonnull final BlockPos pos,
                                    @Nonnull final IBlockState state,
                                    @Nullable final EnumFacing side,
                                    @Nonnull final Fluid fluid){
        return isAcceptedFluid(world,pos,state,side,fluid,false);
    }

    /**
     * 在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器是否能够承载指定的流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 指定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 询问的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的方块状态一致，则应为 true，否则为 false
     * @return 给定的流体在当前条件下是否被允许流入指定的方块
     * @apiNote 例如，isAcceptedFluid(主世界,{1,2,3},土壤[humidity=1],水,false) 就表示确定主世界位于(1,2,3)的方块是湿度为 1 的土壤，并询问在这样的条件下，水是否可以被土壤承载。
     * 如果最后的 false 改为 true，则表示调用者不确定这个位置是否真的是土壤，它可能是其他方块，例如空气、水甚至石头，此时返回的结果就表示假设这个位置是湿度为 1 的土壤，这时候是否能承载水分。
     * 后面这种情况可见于玩家放置方块时的检测。比如玩家要在水中放置一个土壤，在土壤放下去之前需要检查玩家手中的土壤是否能填入水，这时候就需要假设一下了。
     */
    boolean isAcceptedFluid(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            final boolean isAssumed);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体的层数
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，方块承载指定流体的层数
     */
    default int getLayers(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid){
        return getLayers(world,pos,state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体的层数
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，方块承载指定流体的层数
     */
    default int getLayers(@Nonnull final World world,
                  @Nonnull final BlockPos pos,
                  @Nonnull final IBlockState state,
                  @Nullable final EnumFacing side,
                  @Nonnull final Fluid fluid,
                  final boolean isAssumed){
        if(!isAcceptedFluid(world, pos, state, side, fluid,isAssumed)) return 0;
        return (getMaxHeight(world, pos, state, side, fluid, isAssumed) - getEmptyHeight(world, pos, state, side, fluid, isAssumed))
                /getHeightPerLayer(world, pos, state, side, isAssumed);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体所用的每层容量大小，以 QB 为单位
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，单层指定流体以 QB 为单位记的容量
     */
    default long getAmountInQBPerLayer(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid){
        return getAmountInQBPerLayer(world,pos,state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体所用的每层容量大小，以 QB 为单位
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，单层指定流体以 QB 为单位记的容量
     */
    long getAmountInQBPerLayer(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               final boolean isAssumed);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体的量，以 QB 为单位
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，方块承载指定流体的量，以 QB 为单位
     */
    default long getAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid){
        return getLayers(world, pos, state, side,fluid,false)* getAmountInQBPerLayer(world, pos, state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器承载指定流体的量，以 QB 为单位
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，方块承载指定流体的量，以 QB 为单位
     */
    default long getAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               final boolean isAssumed){
        return getLayers(world, pos, state, side,fluid,isAssumed) * getAmountInQBPerLayer(world, pos, state,side,fluid,isAssumed);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器每层流体的高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @return 在给定条件下，方块承载指定流体的量，以 QB 为单位
     * @implSpec 返回的值必须是 {@link #getMaxHeight} 的因数
     */
    default int getHeightPerLayer(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nullable final EnumFacing side){
        return getHeightPerLayer(world,pos,state,side,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器每层流体的高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，方块承载指定流体的量，以 QB 为单位
     * @implSpec 返回的值必须是 {@link #getMaxHeight} 的因数
     */
    int getHeightPerLayer(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          final boolean isAssumed);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器，指定流体被承载时的基准高度。
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，指定流体的基准高度。
     */
    default int getEmptyHeight(@Nonnull final World world,
                       @Nonnull final BlockPos pos,
                       @Nonnull final IBlockState state,
                       @Nullable final EnumFacing side,
                       @Nonnull final Fluid fluid){
        return getEmptyHeight(world,pos,state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器，指定流体被承载时的基准高度。
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，指定流体的基准高度。
     */
    int getEmptyHeight(@Nonnull final World world,
                       @Nonnull final BlockPos pos,
                       @Nonnull final IBlockState state,
                       @Nullable final EnumFacing side,
                       @Nonnull final Fluid fluid,
                       final boolean isAssumed);


    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的表面高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，流体表面的高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     */
    default int getHeight(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid){
        return getEmptyHeight(world,pos,state,side,fluid,false)+
                getLayers(world,pos,state,side,fluid,false)* getHeightPerLayer(world,pos,state,side,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的表面高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，流体表面的高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     */
    default int getHeight(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid,
                          final boolean isAssumed){
        return getEmptyHeight(world,pos,state,side,fluid,isAssumed)+
                getLayers(world,pos,state,side,fluid,isAssumed)* getHeightPerLayer(world,pos,state,side,isAssumed);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体能够有的最高表面高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，指定流体能够有的最高表面高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     */
    default int getMaxHeight(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid){
        return getMaxHeight(world,pos,state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体能够有的最高表面高度
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，指定流体能够有的最高表面高度。
     * @apiNote 反重力流体在考虑时不用反转，仍然当成普通流体看待。
     */
    int getMaxHeight(@Nonnull final World world,
                     @Nonnull final BlockPos pos,
                     @Nonnull final IBlockState state,
                     @Nullable final EnumFacing side,
                     @Nonnull final Fluid fluid,
                     final boolean isAssumed);

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的最大层数。
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，对应流体的层数最大值
     */
    default int getMaxLayers(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid){
        return (getMaxHeight(world,pos,state,side,fluid,false) - getEmptyHeight(world,pos,state,side,fluid,false))
                / getHeightPerLayer(world,pos,state,side,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的最大层数。
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，对应流体的层数最大值
     */
    default int getMaxLayers(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             final boolean isAssumed){
        return (getMaxHeight(world,pos,state,side,fluid,isAssumed) - getEmptyHeight(world,pos,state,side,fluid,isAssumed))
                / getHeightPerLayer(world,pos,state,side,isAssumed);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的最大含量，单位为 QB
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，对应流体的最大含量，单位为 QB
     */
    default long getMaxAmountInQB(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nullable final EnumFacing side,
                                  @Nonnull final Fluid fluid){
        return getMaxLayers(world, pos, state, side, fluid, false)* getAmountInQBPerLayer(world, pos, state, side, fluid, false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体的最大含量，单位为 QB
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，对应流体的最大含量，单位为 QB
     */
    default long getMaxAmountInQB(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nullable final EnumFacing side,
                                  @Nonnull final Fluid fluid,
                                  final boolean isAssumed){
        return getMaxLayers(world, pos, state, side, fluid, isAssumed)* getAmountInQBPerLayer(world, pos, state, side, fluid, isAssumed);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体存储的 NBT 数据
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 在给定条件下，对应流体存储的 NBT 数据，可能为 null
     */
    @Nullable
    default NBTTagCompound getFluidNBTData(@Nonnull final World world,
                                   @Nonnull final BlockPos pos,
                                   @Nonnull final IBlockState state,
                                   @Nullable final EnumFacing side,
                                   @Nonnull final Fluid fluid){
        return getFluidNBTData(world,pos,state,side,fluid,false);
    }

    /**
     * 查询在指定位置的方块状态是给定的属于该载流方块的方块状态时，该方块指定面对应的流体容器指定流体存储的 NBT 数据
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 在给定条件下，对应流体存储的 NBT 数据，可能为 null
     */
    @Nullable
    NBTTagCompound getFluidNBTData(@Nonnull final World world,
                                   @Nonnull final BlockPos pos,
                                   @Nonnull final IBlockState state,
                                   @Nullable final EnumFacing side,
                                   @Nonnull final Fluid fluid,
                                   final boolean isAssumed);

    /**
     * 在指定位置，以给定的方块状态，将指定面对应的流体容器的指定流体的层数设置为指定层数。
     * @since GeoCraftAPI 0.3.1
     * @apiNote 当原位置不是同种载流方块时，该方法会像 {@link World#setBlockState(BlockPos, IBlockState)} 一样覆盖原有的方块及其数据。
     * 如果原位置是同种的载流方块，则该方法应当只会修改指定的流体容器的指定流体的层数。在大多数情况下，同位置的其他流体容器以及其他流体的含量不会发生变化。
     * 但在特殊情况下，方块发生变化是允许的，甚至可能会与其他游戏机制相互作用导致方块变成其他方块，因此在操作完成之后务必调用 {@link World#getBlockState(BlockPos)} 获取实际状态。
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 方块状态，该方块状态是指示性的，不代表指定位置的确是指定的方块状态
     * @param side 方块的面，当为 null 时表示默认的面，决定了要操作的流体容器
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @return 操作是否成功
     */
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             final int newLayer){
        return setLayer(world, pos, state, side, fluid, newLayer,null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，将指定面对应的流体容器的指定流体的层数设置为指定层数。
     * @since GeoCraftAPI 0.3.1
     * @apiNote 当原位置不是同种载流方块时，该方法会像 {@link World#setBlockState(BlockPos, IBlockState)} 一样覆盖原有的方块及其数据。
     * 如果原位置是同种的载流方块，则该方法应当只会修改指定的流体容器的指定流体的层数。在大多数情况下，同位置的其他流体容器以及其他流体的含量不会发生变化。
     * 但在特殊情况下，方块发生变化是允许的，甚至可能会与其他游戏机制相互作用导致方块变成其他方块，因此在操作完成之后务必调用 {@link World#getBlockState(BlockPos)} 获取实际状态。
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 方块状态，该方块状态是指示性的，不代表指定位置的确是指定的方块状态
     * @param side 方块的面，当为 null 时表示默认的面，决定了要操作的流体容器
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @param nbt 流体的 NBT 标签，会覆盖原有的数据（如有）
     * @return 操作是否成功
     */
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             final int newLayer,
                             @Nullable final NBTTagCompound nbt){
        return setLayer(world, pos, state, side, fluid, newLayer,null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，将指定面对应的流体容器的指定流体的层数设置为指定层数。
     * @since GeoCraftAPI 0.3.1
     * @apiNote 当原位置不是同种载流方块时，该方法会像 {@link World#setBlockState(BlockPos, IBlockState)} 一样覆盖原有的方块及其数据。
     * 如果原位置是同种的载流方块，则该方法应当只会修改指定的流体容器的指定流体的层数。在大多数情况下，同位置的其他流体容器以及其他流体的含量不会发生变化。
     * 但在特殊情况下，方块发生变化是允许的，甚至可能会与其他游戏机制相互作用导致方块变成其他方块，因此在操作完成之后务必调用 {@link World#getBlockState(BlockPos)} 获取实际状态。
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 方块状态，该方块状态是指示性的，不代表指定位置的确是指定的方块状态
     * @param side 方块的面，当为 null 时表示默认的面，决定了要操作的流体容器
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @param nbt 流体的 NBT 标签，会覆盖原有的数据（如有）
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagsModifier} 构建
     * @return 操作是否成功
     */
    boolean setLayer(@Nonnull final World world,
                     @Nonnull final BlockPos pos,
                     @Nonnull final IBlockState state,
                     @Nullable final EnumFacing side,
                     @Nonnull final Fluid fluid,
                     final int newLayer,
                     @Nullable final NBTTagCompound nbt,
                     final long blockFlagsModifier);

    /**
     * 在指定位置，以给定的方块状态，指定流体是否能够以指定条件流入当前载流方块的指定面对应的流体容器
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 流入方块的面，当为 null 时表示默认的面
     * @param fluid 需要流入的流体
     * @param from 流体来源
     * @return 若可以，则返回 true
     */
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final IFluidFrom from){
        return this.canFill(world,pos,state,side,fluid,from,false);
    }

    /**
     * 在指定位置，以给定的方块状态，指定流体是否能够以指定条件流入当前载流方块的指定面对应的流体容器
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 流入方块的面，当为 null 时表示默认的面
     * @param fluid 需要流入的流体
     * @param from 流体来源
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 若可以，则返回 true
     */
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final IFluidFrom from,
                            final boolean isAssumed){
        return !isFull(world,pos,state,side, fluid);
    }

    /**
     * 在指定位置，以给定的方块状态，是否能够以指定条件从当前载流方块的指定面对应的流体容器抽取指定的流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 抽取方块的面，当为 null 时表示默认的面
     * @param fluid 需要抽取的流体
     * @param to 流体去向
     * @return 若可以，则返回 true
     */
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final IFluidTo to){
        return getLayers(world,pos,state,side,fluid,false) != 0;
    }

    /**
     * 在指定位置，以给定的方块状态，是否能够以指定条件从当前载流方块的指定面对应的流体容器抽取指定的流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 抽取方块的面，当为 null 时表示默认的面
     * @param fluid 需要抽取的流体
     * @param to 流体去向
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 若可以，则返回 true
     */
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final IFluidTo to,
                             final boolean isAssumed){
        return getLayers(world,pos,state,side,fluid,isAssumed) != 0;
    }

    /**
     * 在指定位置，以给定的方块状态，该载流方块在指定面对应的流体容器的指定流体是否已经装满
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 若指定的流体容器不支持含有该流体或当前流体已满，则返回true，否则为false
     */
    default boolean isFull(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid){
        return getLayers(world,pos,state,side,fluid,false) >= getMaxLayers(world,pos,state,side,fluid,false);
    }

    /**
     * 在指定位置，以给定的方块状态，该载流方块在指定面对应的流体容器的指定流体是否已经装满
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @param isAssumed 是否是假设的条件。如果无法确定当前位置的实际方块状态是否和给定的状态一致，则应为 true，否则为 false。
     * @return 若指定的流体容器不支持含有该流体或当前流体已满，则返回true，否则为false
     */
    default boolean isFull(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid,
                           final boolean isAssumed){
        return getLayers(world,pos,state,side,fluid,isAssumed) >= getMaxLayers(world,pos,state,side,fluid,isAssumed);
    }

    /**
     * 在指定位置，以给定的方块状态，该载流方块在指定面对应的流体容器的指定流体是否是空的，且还有空间存入该流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，必须要满足与该位置实际的方块状态一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 若在指定流体容器中，指定流体是空的，且还有空间存入该流体，则返回 true，否则返回 false
     */
    default boolean isEmpty(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid){
        if(isFull(world,pos,state,side,fluid,false)) return false;
        return getLayers(world,pos,state,side,fluid,false) <= 0;
    }

    /**
     * 在指定位置，以给定的方块状态，该载流方块在指定面对应的流体容器的指定流体是否是空的，且还有空间存入该流体
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态，可能和实际位置的方块状态不一致
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 查询的流体
     * @return 若在指定流体容器中，指定流体是空的，且还有空间存入该流体，则返回 true，否则返回 false
     */
    default boolean isEmpty(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            final boolean isAssumed){
        if(isFull(world,pos,state,side,fluid,isAssumed)) return false;
        return getLayers(world,pos,state,side,fluid,isAssumed) <= 0;
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定层数的指定液体。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param operation 操作
     * @return 在给定条件下，实际添加的层数
     */
    default int addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nullable final EnumFacing side,
                         @Nonnull final Fluid fluid,
                         final int layer,
                         @Nonnull final FluidHostOperation operation){
        return addLayer(world, pos, state, side, fluid, layer, operation, null, null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定层数的指定液体。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @return 在给定条件下，实际添加的层数
     */
    default int addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nullable final EnumFacing side,
                         @Nonnull final Fluid fluid,
                         final int layer,
                         @Nonnull final FluidHostOperation operation,
                         @Nullable final NBTTagCompound nbt){
        return addLayer(world, pos, state, side, fluid, layer, operation, nbt,null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定层数的指定液体。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @param from 流体来源
     * @return 在给定条件下，实际添加的层数
     */
    default int addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nullable final EnumFacing side,
                         @Nonnull final Fluid fluid,
                         final int layer,
                         @Nonnull final FluidHostOperation operation,
                         @Nullable final NBTTagCompound nbt,
                         @Nullable final IFluidFrom from){
        return addLayer(world,pos,state,side,fluid,layer,operation,nbt,from,BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定层数的指定液体。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @param from 流体来源
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagsModifier} 构建
     * @return 在给定条件下，实际添加的层数
     */
    default int addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nullable final EnumFacing side,
                         @Nonnull final Fluid fluid,
                         final int layer,
                         @Nonnull final FluidHostOperation operation,
                         @Nullable final NBTTagCompound nbt,
                         @Nullable final IFluidFrom from,
                         final long blockFlagsModifier){
        if(layer <= 0) return 0;
        final int curLayer = getLayers(world,pos,state,side,fluid,operation.isAssumed);
        final int addedInFact = Math.min(layer, getMaxLayers(world,pos,state,side,fluid,operation.isAssumed)-curLayer);
        if(addedInFact <= 0) return 0;
        if(operation.doOperate){
            final int newLayer = curLayer + addedInFact;
            final @Nullable NBTTagCompound newNBTDat = nbt == null? getFluidNBTData(world, pos, state, side, fluid,operation.isAssumed):nbt;
            this.setLayer(world,pos,state,side,fluid,newLayer,newNBTDat,blockFlagsModifier);
        }
        return addedInFact;
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定量的指定液体，单位为 QB。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param amount 流体量，单位为 QB
     * @param operation 操作
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    default long addAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               final long amount,
                               @Nonnull final FluidHostOperation operation){
        return addAmountInQB(world, pos, state, side, fluid, amount,operation,null,null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定量的指定液体，单位为 QB。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param amount 流体量，单位为 QB
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    default long addAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               final long amount,
                               @Nonnull final FluidHostOperation operation,
                               @Nullable final NBTTagCompound nbt){
        return addAmountInQB(world, pos, state, side, fluid, amount,operation,nbt,null,BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定量的指定液体，单位为 QB。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param amount 流体量，单位为 QB
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @param from 流体来源
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    default long addAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               long amount,
                               @Nonnull final FluidHostOperation operation,
                               @Nullable final NBTTagCompound nbt,
                               @Nullable final IFluidFrom from){
        return addAmountInQB(world, pos, state, side, fluid, amount,operation, nbt, from,BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面输入指定量的指定液体，单位为 QB。
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidFrom, boolean)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要添加的流体
     * @param amount 流体量，单位为 QB
     * @param operation 操作
     * @param nbt 添加的流体的 NBT 复合标签
     * @param from 流体来源
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagsModifier} 构建
     * @return 在给定条件下，实际添加的流体量，单位为 QB
     */
    default long addAmountInQB(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               long amount,
                               @Nonnull final FluidHostOperation operation,
                               @Nullable final NBTTagCompound nbt,
                               @Nullable final IFluidFrom from,
                               final long blockFlagsModifier){
        if(amount <= 0L) return 0L;
        final long amountPerLayer = getAmountInQBPerLayer(world, pos, state, side, fluid,operation.isAssumed);
        amount -= (amount%amountPerLayer);
        if(amount <= 0L) return 0L;
        final long curAmount = getAmountInQB(world, pos, state, side, fluid, operation.isAssumed);
        final long amountInFact = Math.min(amount, getMaxAmountInQB(world, pos, state, side, fluid, operation.isAssumed)-curAmount);
        if(amountInFact <= 0L) return 0L;
        return addLayer(world,pos,state,side,fluid,(int)(amount/amountPerLayer),operation,nbt,from,blockFlagsModifier)*amountPerLayer;
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面抽取指定层数的指定液体。
     * 在抽取前一般需要检测{@link #canDrain(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidTo)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要抽取的流体
     * @param layer 层数
     * @param operation 操作
     * @return 在给定条件下，实际抽取的流体层数
     */
    default int drainLayer(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid,
                           final int layer,
                           @Nonnull final FluidHostOperation operation){
        return drainLayer(world, pos, state, side, fluid, layer,operation,null, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面抽取指定层数的指定液体。
     * 在抽取前一般需要检测{@link #canDrain(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidTo)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要抽取的流体
     * @param layer 层数
     * @param operation 操作
     * @param to 流体去向
     * @return 在给定条件下，实际抽取的流体层数
     */
    default int drainLayer(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Fluid fluid,
                           @Nullable final EnumFacing side,
                           final int layer,
                           @Nonnull final FluidHostOperation operation,
                           @Nullable final IFluidTo to){
        return drainLayer(world, pos, state, side, fluid, layer, operation,to, BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 在指定位置，以给定的方块状态，尝试从指定的面抽取指定层数的指定液体。
     * 在抽取前一般需要检测{@link #canDrain(World, BlockPos, IBlockState, EnumFacing, Fluid, IFluidTo)}
     * @since GeoCraftAPI 0.3.1
     * @param world 所在世界
     * @param pos 方块位置
     * @param state 给定的方块状态。
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 true 的操作，则可以和实际位置的方块状态不一致；
     *              如果选择 {@link FluidHostOperation#isAssumed} 为 false 的操作，则必须要满足与该位置实际的方块状态一致。
     * @param side 方块的面，当为 null 时表示默认的面
     * @param fluid 要抽取的流体
     * @param layer 层数
     * @param operation 操作
     * @param to 流体去向
     * @param blockFlagsModifier 方块更新操作的修改器，用一个 long 表示，可通过 {@link BlockFlagsModifier} 构建
     * @return 在给定条件下，实际抽取的流体层数
     */
    default int drainLayer(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid,
                           final int layer,
                           @Nonnull final FluidHostOperation operation,
                           @Nullable final IFluidTo to,
                           final long blockFlagsModifier){
        if(layer <= 0) return 0;
        final int curLayer = getLayers(world,pos,state,side,fluid,operation.isAssumed);
        final int drainedInFact = Math.min(layer, curLayer);
        if(drainedInFact <= 0) return 0;
        if(operation.doOperate){
            final int newLayer = curLayer - drainedInFact;
            final @Nullable NBTTagCompound nbt = getFluidNBTData(world, pos, state, side, fluid,operation.isAssumed);
            this.setLayer(world,pos,state,side,fluid,newLayer,nbt,blockFlagsModifier);
        }
        return drainedInFact;
    }

    default long drainAmountInQB(@Nonnull final World world,
                                 @Nonnull final BlockPos pos,
                                 @Nonnull final IBlockState state,
                                 @Nullable final EnumFacing side,
                                 @Nonnull final Fluid fluid,
                                 long amount,
                                 @Nonnull final FluidHostOperation operation){
        return drainAmountInQB(world,pos,state,side,fluid,amount,operation,null,BlockFlagsModifier.MODIFY_NOTHING);
    }

    default long drainAmountInQB(@Nonnull final World world,
                                 @Nonnull final BlockPos pos,
                                 @Nonnull final IBlockState state,
                                 @Nullable final EnumFacing side,
                                 @Nonnull final Fluid fluid,
                                 long amount,
                                 @Nonnull final FluidHostOperation operation,
                                 @Nullable final IFluidTo to){
        return drainAmountInQB(world,pos,state,side,fluid,amount,operation,to,BlockFlagsModifier.MODIFY_NOTHING);
    }

    default long drainAmountInQB(@Nonnull final World world,
                                 @Nonnull final BlockPos pos,
                                 @Nonnull final IBlockState state,
                                 @Nullable final EnumFacing side,
                                 @Nonnull final Fluid fluid,
                                 long amount,
                                 @Nonnull final FluidHostOperation operation,
                                 @Nullable final IFluidTo to,
                                 final long blockFlagsModifier){
        if(amount <= 0L) return 0L;
        final long amountPerLayer = getAmountInQBPerLayer(world, pos, state, side, fluid,operation.isAssumed);
        amount += (amountPerLayer-(amount%amountPerLayer));
        final long curAmount = getAmountInQB(world, pos, state,side, fluid,operation.isAssumed);
        final long drainedInFact = Math.min(amount,curAmount);
        if(drainedInFact <= 0) return 0;
        return drainLayer(world,pos,state,side,fluid,(int) (amount/amountPerLayer),operation,to,blockFlagsModifier)*amountPerLayer;
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid,
                                       final int amount,
                                       @Nonnull final FluidHostOperation operation){
        return drainAmountInMB(world, pos, state, side, fluid, amount, operation,null,BlockFlagsModifier.MODIFY_NOTHING);
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid,
                                       final int amount,
                                       @Nonnull final FluidHostOperation operation,
                                       @Nullable final IFluidTo to){
        return drainAmountInMB(world, pos, state, side, fluid, amount, operation,to,BlockFlagsModifier.MODIFY_NOTHING);
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid,
                                       final int amount,
                                       @Nonnull final FluidHostOperation operation,
                                       @Nullable final IFluidTo to,
                                       final long blockFlagsModifier){
        final @Nullable NBTTagCompound nbt = getFluidNBTData(world,pos,state,side,fluid,operation.isAssumed);
        return new FluidStack(fluid,
                QBUtil.toMB(drainAmountInQB(world,pos,state,side, fluid,QBUtil.toQBFromMB(amount),operation,to,blockFlagsModifier)),
                nbt);
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final FluidStack stack,
                                       @Nonnull final FluidHostOperation operation){
        return new FluidStack(stack,
                QBUtil.toMB(drainAmountInQB(world,pos,state,side, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),operation,null,BlockFlagsModifier.MODIFY_NOTHING)));
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final FluidStack stack,
                                       @Nonnull final FluidHostOperation operation,
                                       @Nullable final IFluidTo to){
        return new FluidStack(stack,
                QBUtil.toMB(drainAmountInQB(world,pos,state,side, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),operation,to,BlockFlagsModifier.MODIFY_NOTHING)));
    }

    @Nonnull
    default FluidStack drainAmountInMB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final FluidStack stack,
                                       @Nonnull final FluidHostOperation operation,
                                       @Nullable final IFluidTo to,
                                       final long blockFlagsModifier){
        return new FluidStack(stack,
                QBUtil.toMB(drainAmountInQB(world,pos,state,side, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),operation,to,blockFlagsModifier)));
    }
}
