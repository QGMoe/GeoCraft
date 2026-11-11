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

package moe.qingu.orbtellus.block.soil;

import moe.qingu.orbtellus.geography.soil.OTCSoil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import moe.qingu.orbtellus.api.block.IBlockFalling;
import moe.qingu.orbtellus.geography.soil.BlockSoilType;

import javax.annotation.Nonnull;
import java.util.Random;

import static moe.qingu.orbtellus.api.block.BlockProperties.HUMIDITY;

/**
 * 为了更好的兼容性，采用继承替换而非直接 Mixin
 * @author QiguaiAAAA
 */
public class BlockSoilDirt extends BlockSoilExtends.Dirt implements IBlockSoil, IBlockFalling {

    protected final OTCSoil dirt = new OTCSoil(BlockSoilType.DIRT);
    protected final OTCSoil coarseDirt = new OTCSoil(BlockSoilType.COARSE_DIRT);
    protected final OTCSoil podzol = new OTCSoil(BlockSoilType.PODZOL);

    public BlockSoilDirt(){
        this.setTickRandomly(true);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(VARIANT).getMetadata()+state.getValue(HUMIDITY)*3;
    }

    @Override
    public void randomTick(final @Nonnull World worldIn,
                           final @Nonnull BlockPos pos,
                           final @Nonnull IBlockState state,
                           final @Nonnull Random random) {
        BlockSoils.onSoilTick(this,worldIn, pos, state, random);
    }

    @Override
    public void updateTick(@Nonnull final World worldIn,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Random rand) {
        if(getSoil(state).isStable(state.getValue(HUMIDITY))) return;
        IBlockFalling.super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void onPlayerDestroy(final @Nonnull World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state) {
        BlockSoils.dropWaterWhenBroken(this,worldIn, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(final @Nonnull IBlockState state,
                                final @Nonnull World worldIn,
                                final @Nonnull BlockPos pos,
                                final @Nonnull Block blockIn,
                                final @Nonnull BlockPos fromPos) {
        if(getSoil(state).isStable(state.getValue(HUMIDITY))) return;
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public int tickRate(@Nonnull final World worldIn) {
        return 2;
    }

    @Override
    public void onBlockAdded(@Nonnull final World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state) {
        if(getSoil(state).isStable(state.getValue(HUMIDITY))) return;
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
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
        return BlockSoils.onPlayerUseBottle(this,worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getDustColor(final IBlockState state) {
        return 0xFF866043;
    }

    //********************
    // IBlockSoil
    //********************

    @Nonnull
    @Override
    public OTCSoil getSoil(@Nonnull final IBlockState state) {
        switch (state.getValue(VARIANT)) {
            case PODZOL: return podzol;
            case COARSE_DIRT: return coarseDirt;
            case DIRT:
            default: return dirt;
        }
    }
}
