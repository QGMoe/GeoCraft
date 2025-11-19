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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;

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
    IBlockState getLayerState(@Nonnull IBlockState state, @Nonnull Fluid fluid, int layer);

    @Override
    boolean isAcceptedFluid(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid);

    @Override
    int getLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid);

    @Override
    default long getAmountInQB(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return getLayers(world, pos, state, fluid)* getAmountInQBPerLayer(world, pos, state,fluid);
    }

    @Override
    default int getMaxLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        return (LayeredFluidHostUtil.DEFAULT_MAX_HEIGHT -getEmptyHeight(world,pos,state,fluid))/ getHeightPerLayer(world,pos,state);
    }

    @Override
    default long getMaxAmountInQB(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return getMaxLayers(world, pos, state, fluid)* getAmountInQBPerLayer(world, pos, state,fluid);
    }

    @Override
    default int getHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        return getEmptyHeight(world,pos,state,fluid)+ getLayers(world,pos,state,fluid)* getHeightPerLayer(world,pos,state);
    }

    @Override
    int getEmptyHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid);

    @Override
    default int getMaxHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        return getEmptyHeight(world,pos,state,fluid) + getMaxLayers(world,pos,state,fluid)* getHeightPerLayer(world,pos,state);
    }

    @Override
    int getHeightPerLayer(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state);

    @Override
    long getAmountInQBPerLayer(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid);

    @Override
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer, @Nullable NBTTagCompound nbt, final int disabledBlockFlags, final int enabledBlockFlags){
        IBlockState newState = getLayerState(state, fluid,newLayer);
        if(newState == null) return false;
        final int flags = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);
        return world.setBlockState(pos,newState,flags);
    }
}
