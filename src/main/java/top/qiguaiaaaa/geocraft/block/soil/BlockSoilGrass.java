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

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.qiguaiaaaa.geocraft.api.block.IBlockFalling;
import top.qiguaiaaaa.geocraft.api.util.math.vec.BlockPosI;
import top.qiguaiaaaa.geocraft.block.IBlockSoil;
import top.qiguaiaaaa.geocraft.geography.soil.BlockSoilType;

import javax.annotation.Nonnull;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.HUMIDITY;

/**
 * 为了更好的兼容性，采用继承替换而非直接 Mixin
 * @author QiguaiAAAA
 */
public class BlockSoilGrass extends BlockSoilExtends.Grass implements IBlockSoil, IBlockFalling {
    private final ThreadLocal<Boolean> isRandomTick = ThreadLocal.withInitial(()-> Boolean.FALSE);

    @Override
    public void updateTick(@Nonnull final World worldIn, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final Random rand) {
        if(worldIn.isRemote) return;
        if(!isRandomTick.get()){
            if(state.getValue(HUMIDITY) <= getMaxStableHumidity(state)) return;
            checkAndFall(worldIn, pos);
            return;
        }
        if (!worldIn.isAreaLoaded(pos, 3)) return;
        if (worldIn.getLightFromNeighbors(pos.up()) < 4 && worldIn.getBlockState(pos.up()).getLightOpacity(worldIn, pos.up()) > 2) {
            worldIn.setBlockState(pos, state.withProperty(HUMIDITY,state.getValue(HUMIDITY)));
            return;
        }
        final BlockPosI.Mutable mutablePos = new BlockPosI.Mutable();
        if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            for (int i = 0; i < 4; ++i) {
                mutablePos.setPos(pos.getX()+rand.nextInt(3) - 1, pos.getY()+rand.nextInt(5) - 3, pos.getZ()+rand.nextInt(3) - 1);

                if (mutablePos.getY() >= 0 && mutablePos.getY() < 256 && !worldIn.isBlockLoaded(mutablePos)) {
                    return;
                }

                mutablePos.upM();
                IBlockState upState = worldIn.getBlockState(mutablePos);
                mutablePos.downM();
                IBlockState curPosState = worldIn.getBlockState(mutablePos);

                mutablePos.upM();
                if (curPosState.getBlock() == Blocks.DIRT && curPosState.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && worldIn.getLightFromNeighbors(mutablePos) >= 4 && upState.getLightOpacity(worldIn, pos.up()) <= 2) {
                    worldIn.setBlockState(mutablePos.downM(), Blocks.GRASS.getDefaultState().withProperty(HUMIDITY,curPosState.getValue(HUMIDITY)));
                }
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HUMIDITY);
    }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        isRandomTick.set(Boolean.TRUE);
        super.randomTick(worldIn, pos, state, random);
        this.onRandomTick(worldIn, pos, state, random);
        isRandomTick.set(Boolean.FALSE);
    }

    @Override
    public void onPlayerDestroy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        dropWaterWhenBroken(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if(state.getValue(HUMIDITY) <= getMaxStableHumidity(state)) return;
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public int tickRate(@Nonnull World worldIn) {
        return 2;
    }

    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, IBlockState state) {
        if(state.getValue(HUMIDITY) <= getMaxStableHumidity(state)) return;
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return onPlayerUseBottle(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    //********************
    // IBlockSoil
    //********************

    @Nonnull
    @Override
    public BlockSoilType getType(@Nonnull IBlockState state) {
        return BlockSoilType.GRASS;
    }
}
