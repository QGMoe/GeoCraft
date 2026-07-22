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

package moe.qingu.geocraft.geography.fluidphysics.vanilla.update;

import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author QGMoe
 */
final class VanillaFluidVanillaFluidTask implements IFluidTask {
    public final BlockDynamicLiquid dynamic;
    public final BlockStaticLiquid static_;

    public VanillaFluidVanillaFluidTask(final @Nonnull Material material) {
        this.dynamic = BlockLiquid.getFlowingBlock(material);
        this.static_ = BlockLiquid.getStaticBlock(material);
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
        dynamic.updateTick(world,pos,state,rand);
    }

    @Override
    public void onFailure(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
        world.setBlockState(pos,static_.getDefaultState().withProperty(BlockLiquid.LEVEL,state.getValue(BlockLiquid.LEVEL)), Constants.BlockFlags.SEND_TO_CLIENTS);
    }

    @Override
    public boolean accepts(@Nonnull final World world, @Nonnull final IBlockState state) {
        return state.getBlock() == dynamic;
    }
}
