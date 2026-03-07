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

package top.qiguaiaaaa.geocraft_test.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.assets.MockFluids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public class MockBlockFluidHostCommon extends Block implements ILayeredFluidHost {
    public static final PropertyInteger LAYERS = PropertyInteger.create("layers",1,8);

    public MockBlockFluidHostCommon() {
        super(Material.WATER);
        this.setDefaultState(this.getDefaultState().withProperty(LAYERS,1));
        this.setRegistryName(GeoCraftTest.MODID,"common_fluid_host");
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,LAYERS);
    }

    @Override
    public boolean isAcceptedFluid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return fluid == MockFluids.SNOW;
    }

    @Override
    public int getLayers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        return fluid == null || isAcceptedFluid(world, pos, state, fluid)?state.getValue(LAYERS):0;
    }

    @Override
    public int getEmptyHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    public int getHeightPerLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        return LayeredFluidHostUtil.EIGHTH_HEIGHT;
    }

    @Override
    public long getAmountInQBPerLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return isAcceptedFluid(world, pos, state, fluid)? QBUtil.QUANTA_VOLUME:0L;
    }

    @Override
    public boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer, @Nullable NBTTagCompound nbt, int disabledBlockFlags, int enabledBlockFlags) {
        return true;
    }
}
