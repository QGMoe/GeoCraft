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

import com.google.common.annotations.Beta;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 经典设计载流方块接口，目前仅适用于流体方块，且只能有一种层
 * 经典设计是否可以用于非流体方块的情况，还得进一步研究
 */
@Beta
public interface IClassicLaminarifer extends ILaminarifer{

    @Override
    default void describeModel(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState state,
                               @Nonnull final Fluid fluid,
                               @Nullable final NBTTagCompound nbt,
                               @Nonnull final LaminariferModelBuffer buffer) {
        buffer.maxLayers = 1L;
        buffer.currentLayers = this.getLayers(world, pos, state, fluid, nbt);
        buffer.heightPerLayer = this.getHeightPerLayer(world, pos, state, fluid, nbt);
        buffer.emptyHeight = 0L;
        buffer.amountInQBPerLayer = QBUnit.BUCKET_VOLUME;
    }

    @Override
    default long getMaxLayers(@Nonnull final World world,
                              @Nonnull final BlockPos pos,
                              @Nonnull final IBlockState state,
                              @Nonnull final Fluid fluid,
                              @Nullable final NBTTagCompound nbt){
        return 1L;
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
        return QBUnit.BUCKET_VOLUME;
    }
}
