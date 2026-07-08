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

package top.qiguaiaaaa.geocraft.block.soil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.qiguaiaaaa.geocraft.geography.soil.BlockSoilType;

import javax.annotation.Nonnull;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.HUMIDITY;

/**
 * @author QiguaiAAAA
 */
public class BlockSoilSand extends BlockSoilExtends.Sand implements IBlockSoil {

    public BlockSoilSand(){
        this.setTickRandomly(true);
    }

    @Override
    public int getMetaFromState(@Nonnull final IBlockState state) {
        return state.getValue(VARIANT).getMetadata()+state.getValue(HUMIDITY)*2;
    }

    @Override
    public void randomTick(final @Nonnull World worldIn,
                           final @Nonnull BlockPos pos,
                           final @Nonnull IBlockState state,
                           final @Nonnull Random random) {
        this.onRandomTick(worldIn, pos, state, random);
    }

    @Override
    public void updateTick(final @Nonnull World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state,final @Nonnull Random rand) {
        if(state.getValue(HUMIDITY) == getMaxStableHumidity(state)) return;
        super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void onPlayerDestroy(final @Nonnull World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state) {
        dropWaterWhenBroken(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(final @Nonnull World worldIn,
                                    final @Nonnull BlockPos pos,
                                    final @Nonnull IBlockState state,
                                    final @Nonnull EntityPlayer playerIn,
                                    final @Nonnull EnumHand hand,
                                    final @Nonnull EnumFacing facing,
                                    final float hitX,
                                    final float hitY,
                                    final float hitZ) {
        return onPlayerUseBottle(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    //********************
    // IBlockSoil
    //********************

    @Nonnull
    @Override
    public BlockSoilType getType(@Nonnull IBlockState state) {
        return BlockSoilType.SAND;
    }
}
