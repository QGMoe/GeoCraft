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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.BlockFlagsModifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public interface IBlockStateLayeredFluidHost extends ILayeredFluidHost{
    /**
     * 获取指定流体下指定层数时的方块状态
     * @param state 查询的方块状态
     * @param fluid 指定流体
     * @param layer 指定层数
     * @return 若指定状态不存在,返回null
     */
    @Nullable
    IBlockState getLayerState(@Nonnull final IBlockState state,
                              @Nullable final EnumFacing side,
                              @Nonnull final Fluid fluid,
                              final int layer);

    @Nullable
    EnumFacing getDefaultSide(@Nonnull final IBlockState state);

    @Nullable
    @Override
    default EnumFacing getDefaultSide(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state){
        return getDefaultSide(state);
    }

    @Nullable
    @Override
    default EnumFacing getDefaultSide(@Nonnull final World world,
                                      @Nonnull final BlockPos pos,
                                      @Nonnull final IBlockState state,
                                      final boolean isAssumed){
        return getDefaultSide(state);
    }

    boolean isAcceptedFluid(@Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid);

    @Override
    default boolean isAcceptedFluid(@Nonnull final World world,
                                    @Nonnull final BlockPos pos,
                                    @Nonnull final IBlockState state,
                                    @Nullable final EnumFacing side,
                                    @Nonnull final Fluid fluid) {
        return isAcceptedFluid(state,side,fluid);
    }

    @Override
    default boolean isAcceptedFluid(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            final boolean isAssumed){
        return isAcceptedFluid(state,side,fluid);
    }

    default int getLayers(@Nonnull final IBlockState state,
                  @Nullable final EnumFacing side,
                  @Nonnull final Fluid fluid){
        if(!isAcceptedFluid(state, side, fluid)) return 0;
        return (getMaxHeight(state, side, fluid) - getEmptyHeight(state, side, fluid))
                /getHeightPerLayer(state, side);
    }

    @Override
    default int getLayers(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid) {
        return getLayers(state,side,fluid);
    }

    @Override
    default int getLayers(@Nonnull final World world,
                  @Nonnull final BlockPos pos,
                  @Nonnull final IBlockState state,
                  @Nullable final EnumFacing side,
                  @Nonnull final Fluid fluid,
                  final boolean isAssumed){
        return getLayers(state,side,fluid);
    }

    long getAmountInQBPerLayer(@Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid);

    @Override
    default long getAmountInQBPerLayer(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid) {
        return getAmountInQBPerLayer(state,side,fluid);
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid,
                               final boolean isAssumed){
        return getAmountInQBPerLayer(state,side,fluid);
    }

    int getHeightPerLayer(@Nonnull final IBlockState state,
                          @Nullable final EnumFacing side);

    @Override
    default int getHeightPerLayer(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final IBlockState state,
                                  @Nullable final EnumFacing side) {
        return getHeightPerLayer(state,side);
    }

    @Override
    default int getHeightPerLayer(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          final boolean isAssumed){
        return getHeightPerLayer(state,side);
    }

    int getEmptyHeight(@Nonnull final IBlockState state,
                       @Nullable final EnumFacing side,
                       @Nonnull final Fluid fluid);

    @Override
    default int getEmptyHeight(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid) {
        return getEmptyHeight(state,side,fluid);
    }

    @Override
    default int getEmptyHeight(@Nonnull final World world,
                       @Nonnull final BlockPos pos,
                       @Nonnull final IBlockState state,
                       @Nullable final EnumFacing side,
                       @Nonnull final Fluid fluid,
                       final boolean isAssumed){
        return getEmptyHeight(state,side,fluid);
    }

    default int getHeight(@Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid) {
        return getEmptyHeight(state,side,fluid)+
                getLayers(state,side,fluid)* getHeightPerLayer(state,side);
    }

    @Override
    default int getHeight(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid) {
        return getHeight(state, side, fluid);
    }

    @Override
    default int getHeight(@Nonnull final World world,
                          @Nonnull final BlockPos pos,
                          @Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid,
                          final boolean isAssumed) {
        return getHeight(state, side, fluid);
    }

    int getMaxHeight(@Nonnull final IBlockState state,
                     @Nullable final EnumFacing side,
                     @Nonnull final Fluid fluid);

    @Override
    default int getMaxHeight(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        return getMaxHeight(state, side, fluid);
    }

    @Override
    default int getMaxHeight(@Nonnull final World world,
                     @Nonnull final BlockPos pos,
                     @Nonnull final IBlockState state,
                     @Nullable final EnumFacing side,
                     @Nonnull final Fluid fluid,
                     final boolean isAssumed){
        return getMaxHeight(state, side, fluid);
    }

    default int getMaxLayers(@Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        return (getMaxHeight(state,side,fluid) - getEmptyHeight(state,side,fluid))
                / getHeightPerLayer(state,side);
    }

    @Override
    default int getMaxLayers(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        return getMaxHeight(state, side, fluid);
    }

    @Override
    default int getMaxLayers(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             final boolean isAssumed) {
        return getMaxLayers(state, side, fluid);
    }

    @Nullable
    default NBTTagCompound getFluidNBTData(@Nonnull final IBlockState state,
                                   @Nullable final EnumFacing side,
                                   @Nonnull final Fluid fluid){
        return null;
    }

    @Nullable
    @Override
    default NBTTagCompound getFluidNBTData(@Nonnull final World world,
                                           @Nonnull final BlockPos pos,
                                           @Nonnull final IBlockState state,
                                           @Nullable final EnumFacing side,
                                           @Nonnull final Fluid fluid){
        return getFluidNBTData(state, side, fluid);
    }

    @Nullable
    @Override
    default NBTTagCompound getFluidNBTData(@Nonnull final World world,
                                   @Nonnull final BlockPos pos,
                                   @Nonnull final IBlockState state,
                                   @Nullable final EnumFacing side,
                                   @Nonnull final Fluid fluid,
                                   final boolean isAssumed){
        return getFluidNBTData(state,side,fluid);
    }

    @Override
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             final int newLayer,
                             @Nullable final NBTTagCompound nbt,
                             final long blockFlagsModifier){
        final @Nullable IBlockState newState = getLayerState(state,side, fluid, newLayer);
        if(newState == null) return false;
        return world.setBlockState(pos,newState, BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier));
    }

    default boolean isFull(@Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid) {
        return getLayers(state,side,fluid) >= getMaxLayers(state,side,fluid);
    }

    @Override
    default boolean isFull(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid) {
        return isFull(state, side, fluid);
    }

    @Override
    default boolean isFull(@Nonnull final World world,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid,
                           final boolean isAssumed) {
        return isFull(state, side, fluid);
    }
}
