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
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.block.IBlockFalling;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.geography.soil.BlockSoilType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author QiguaiAAAA
 */
public class BlockSoilFarmland extends BlockFarmland implements IBlockSoil, IBlockFalling {

    protected final ThreadLocal<Boolean> isRandomTick = ThreadLocal.withInitial(()->Boolean.FALSE);

    public BlockSoilFarmland(){
        this.setSoundType(SoundType.GROUND);
    }

    protected void updateTickOnRandom(final @Nonnull World worldIn, final @Nonnull BlockPos pos, final @Nonnull IBlockState state, final @Nonnull Random random){
        super.updateTick(worldIn, pos, state, random);
    }

    @Override
    public void randomTick(final @Nonnull World worldIn, final @Nonnull BlockPos pos, final @Nonnull IBlockState state, final @Nonnull Random random) {
        isRandomTick.set(Boolean.TRUE);
        this.onRandomTick(worldIn, pos, state, random);
        super.randomTick(worldIn, pos, state, random);
        isRandomTick.set(Boolean.FALSE);
    }

    @Override
    public void onPlayerDestroy(final @Nonnull World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state) {
        dropWaterWhenBroken(worldIn, pos, state);
    }

    @Override
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        if(isRandomTick.get()){
            updateTickOnRandom(worldIn, pos, state, rand);
            return;
        }
        if(getLayers(worldIn,pos,state,FluidRegistry.WATER) <= getMaxStableHumidity(state)) return;
        if (!worldIn.isRemote) {
            this.checkAndFall(worldIn, pos);
        }
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        state = worldIn.getBlockState(pos);
        if(state.getBlock() != this) return;
        if(getLayers(worldIn,pos,state, FluidRegistry.WATER) <= getMaxStableHumidity(state)) return;
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public int tickRate(@Nonnull World worldIn) {
        return 5;
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

    /**
     * @see BlockFarmland#hasWater(World, BlockPos)
     */
    protected boolean 天圆地方$hasWater(final @Nonnull World worldIn,final @Nonnull BlockPos pos) {
        for (BlockPos.MutableBlockPos poses : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (worldIn.getBlockState(poses).getMaterial() == Material.WATER) {
                return true;
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(worldIn, pos);
    }

    /**
     * @see BlockFarmland#hasCrops(World, BlockPos)
     */
    protected boolean 天圆地方$hasCrops(final @Nonnull World worldIn,final @Nonnull BlockPos pos) {
        final Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof IPlantable && canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, EnumFacing.UP, (IPlantable)block);
    }

    //********************
    // IBlockSoil
    //********************

    @Nonnull
    @Override
    public BlockSoilType getType(@Nonnull final IBlockState state) {
        return BlockSoilType.GRAVEL;
    }

    //********************
    // IBlockFalling
    //********************

    @Override
    public int getDustColor(IBlockState state) {
        return 0xFF866043;
    }

    //***********
    // ILayeredHostBlock
    //***********

    @Override
    public int getLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid != null && fluid != FluidRegistry.WATER) return 0;
        int moisture = state.getValue(MOISTURE);
        return (moisture+1)>>1;
    }

    @Override
    public int getHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid == FluidRegistry.WATER || fluid == null) return getHeightPerLayer(world,pos,state)* getLayers(world,pos,state,fluid);
        return 0;
    }

    @Override
    public boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer, @Nullable NBTTagCompound nbt, final int disabledBlockFlags, final int enabledBlockFlags) {
        if(fluid != FluidRegistry.WATER) return false;
        newLayer = MathHelper.clamp(newLayer,0,4);
        int moisture = newLayer == 0?0: newLayer *2-1;
        return world.setBlockState(pos,state.withProperty(MOISTURE,moisture), APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags));
    }

    @Nullable
    @Override
    public IBlockState getLayerState(@Nonnull IBlockState state, @Nonnull Fluid fluid, int layer) {
        if(fluid != FluidRegistry.WATER) return null;
        if(layer <0 || layer >4) return null;
        int moisture = layer == 0?0: layer *2-1;
        return state.withProperty(MOISTURE,moisture);
    }

    @Override
    public boolean isFull(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid != FluidRegistry.WATER) return true;
        return state.getValue(MOISTURE) >= 7;
    }

    public static class MoreReality extends BlockSoilFarmland{

        @Override
        protected void updateTickOnRandom(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
            int moisture = state.getValue(MOISTURE);

            if(moisture>0) return;

            if (!this.天圆地方$hasWater(worldIn,pos) && !worldIn.isRainingAt(pos.up()) && !this.天圆地方$hasCrops(worldIn, pos)) {
                turnToDirt(worldIn, pos);
            }
        }
    }
}
