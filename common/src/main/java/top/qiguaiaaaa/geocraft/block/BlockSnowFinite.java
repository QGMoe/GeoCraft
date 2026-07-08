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

package top.qiguaiaaaa.geocraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.RealitySnowUpdater;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.MIXTURE;

/**
 * @since 0.2.0-beta.2
 * @author QiguaiAAAA
 */
public class BlockSnowFinite extends BlockSnowExtended implements IBlockStateLayeredFluidHost {
    @Override
    public int tickRate(final @Nonnull World worldIn) {
        return 5;
    }

    @Override
    public boolean canPlaceBlockAt(final @Nonnull World worldIn,final @Nonnull BlockPos pos) {
        final BlockPos down = pos.down();
        IBlockState state = worldIn.getBlockState(down);
        return canBePlacedOn(worldIn,down,state);
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state,
                                @Nonnull final World worldIn,
                                @Nonnull final BlockPos pos,
                                @Nonnull final Block blockIn,
                                @Nonnull final BlockPos fromPos) {
        this.checkAndFallBlock(worldIn, pos, state);
    }

    protected boolean checkAndFallBlock(final @Nonnull World worldIn, final @Nonnull BlockPos pos, final @Nonnull IBlockState state) {
        if (!this.canPlaceBlockAt(worldIn, pos)) {
            MiscUtil.scheduleFluidBlockUpdate(worldIn,pos,this,tickRate(worldIn));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void updateTick(@Nonnull final World worldIn,final @Nonnull BlockPos pos,@Nonnull IBlockState state,final @Nonnull Random rand) {
        if(worldIn.isRemote) return;
        if(trySmelt(worldIn, pos, state, rand)) return;
        state = worldIn.getBlockState(pos);
        tryFallDown(worldIn, pos, state);
    }

    @Override
    public boolean isReplaceable(@Nonnull final IBlockAccess worldIn,final @Nonnull BlockPos pos) {
        return false;
    }

    protected boolean canBePlacedOn(@Nonnull final World world,@Nonnull final BlockPos downPos,@Nonnull final IBlockState downState){
        final Block block = downState.getBlock();

        final BlockFaceShape shape = downState.getBlockFaceShape(world, downPos, EnumFacing.UP);
        return shape == BlockFaceShape.SOLID || block.isLeaves(downState, world, downPos) || block == this && downState.getValue(BlockSnow.LAYERS) == 8;
    }

    protected boolean tryFallDown(final @Nonnull World world,final @Nonnull BlockPos pos,final @Nonnull IBlockState state){
        if(world.isRemote) return false;
        final BlockPos downPos = pos.down();
        IBlockState downState = world.getBlockState(downPos);
        if(RealitySnowUpdater.isBlocked(world,downPos,downState,state)){
//            if(!canBePlacedOn(world,downPos,downState)){
//                world.setBlockToAir(pos);
//            }
            return false;
        }
        final boolean isMixture = state.getValue(MIXTURE);
        Block downBlock = downState.getBlock();
        if(downBlock == Blocks.SNOW_LAYER){ //雪和雪
            boolean isDownMixture = downState.getValue(MIXTURE);
            final int newLayers = state.getValue(BlockSnow.LAYERS) + downState.getValue(BlockSnow.LAYERS);
            if(isMixture == isDownMixture){ //类型相同，直接合并
                if(newLayers<=8){
                    world.setBlockToAir(pos);
                    world.setBlockState(downPos,downState.withProperty(BlockSnow.LAYERS,newLayers));
                }else{
                    world.setBlockState(pos,state.withProperty(BlockSnow.LAYERS,newLayers-8));
                    world.setBlockState(downPos,downState.withProperty(BlockSnow.LAYERS,8));
                }
            }else{ //否则，将多余的水冻结成冰
                final int totalWater = isMixture? getLayers(world,pos,state, FluidRegistry.WATER): getLayers(world,downPos,downState,FluidRegistry.WATER);
                if(newLayers<=8){
                    world.setBlockToAir(pos);
                    world.setBlockState(downPos,downState
                            .withProperty(BlockSnow.LAYERS,newLayers)
                            .withProperty(MIXTURE,false));
                }else{
                    world.setBlockState(pos,state.withProperty(BlockSnow.LAYERS,newLayers-8)
                            .withProperty(MIXTURE,false));
                    world.setBlockState(downPos,downState.withProperty(BlockSnow.LAYERS,8)
                            .withProperty(MIXTURE,false));
                }
                try(@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){
                    if(accessor == null) return true;

                    accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*totalWater/2d);
                }
            }
        }
        else if(downBlock instanceof ILayeredFluidHost){ //雪和其他方块
            ILayeredFluidHost host = (ILayeredFluidHost) downState.getBlock();
            final int curLayers_F = getLayers(world,pos,state,null); //这里的 layer为雪的载流方块单位
            long curAmountSnow = getAmountInQB(world,pos,state, GeoFluids.SNOW);
            if (isMixture) {
                final boolean hasHalfQuanta = (curLayers_F >> 1 & 1) != 0;

                try (final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world, pos, true)) {
                    long curAmountWater = getAmountInQB(world, pos, state, FluidRegistry.WATER);

                    final boolean melting, doMelted;
                    if (hasHalfQuanta) {
                        curAmountWater += QBUtil.HALF_QUANTA_VOLUME; //将另外半层雪融化，补充成整数层layer
                        melting = true;
                    } else melting = false;
                    boolean changed;

                    assert curAmountWater > 0;
                    final long filledAmountWater = host.addAmountInQB(world, downPos, downState, FluidRegistry.WATER, curAmountWater, true);
                    curAmountWater -= filledAmountWater; //减去已经填充的量
                    changed = filledAmountWater > 0;
                    //现在，若剩余的量大于等于刚才融化以补充成整数层的量，说明没有用到融化的部分，因此不需要融化一部分雪以补充水。但若小于，则直接当成融化，并扣除相应热量。
                    doMelted = melting && curAmountWater < QBUtil.HALF_QUANTA_VOLUME;

                    if (melting && !doMelted) {
                        curAmountWater -= QBUtil.HALF_QUANTA_VOLUME; //没有用到，所以扣掉
                    } else if (doMelted){
                        curAmountSnow -= QBUtil.HALF_QUANTA_VOLUME; //如果上面真的融化，雪需要扣除相应的量
                        if (accessor != null)
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA * 0.5);//真的融化掉
                    }

                    if (changed){
                        downState = world.getBlockState(downPos); //更新状态，因为下面的状态可能改变
                        if ((downBlock = downState.getBlock()) instanceof ILayeredFluidHost)
                            host = (ILayeredFluidHost) downBlock;
                        else host = null;
                    }

                    if (host != null) {
                        final boolean freezing, doFreeze;
                        if (curAmountWater >= QBUtil.HALF_QUANTA_VOLUME && hasHalfQuanta) { //若有足够的水，则将半层水冻结成雪。这里水一定不会小于1/16 B
                            curAmountSnow += QBUtil.HALF_QUANTA_VOLUME;
                            freezing = true;
                        } else freezing = false;

                        assert curAmountSnow > 0;
                        final long filledAmountSnow = host.addAmountInQB(world, downPos, downState, GeoFluids.SNOW, curAmountSnow, true);
                        curAmountSnow -= filledAmountSnow;
                        changed |= filledAmountSnow > 0;
                        doFreeze = freezing && curAmountSnow < QBUtil.HALF_QUANTA_VOLUME;

                        if (freezing && !doFreeze) {
                            curAmountSnow -= QBUtil.HALF_QUANTA_VOLUME;//没用到，还回去
                        } else if (doFreeze) {
                            curAmountWater -= QBUtil.HALF_QUANTA_VOLUME;
                            if (accessor != null)
                                accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA * 0.5);
                        }
                    }

                    if (!changed) return false;

                    if (curAmountSnow + curAmountWater < QBUtil.QUANTA_VOLUME) { //小于最小的可存储单位
                        world.setBlockToAir(pos);
                    } else {
                        final long totalAmount = curAmountSnow + curAmountWater;
                        final int totalLayers = QBUtil.toQuanta(totalAmount);
                        if (curAmountWater > curAmountSnow) {
                            turnIntoWater(world, pos, accessor, totalLayers);
                            if (accessor != null)
                                accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA * QBUtil.toPreciseQuanta(curAmountSnow));
                        } else if (curAmountSnow == curAmountWater) {
                            world.setBlockState(pos, state.withProperty(LAYERS, totalLayers)
                                    .withProperty(MIXTURE, true));
                        } else {
                            world.setBlockState(pos, state.withProperty(LAYERS, totalLayers)
                                    .withProperty(MIXTURE, false));
                            if (accessor != null)
                                accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA * QBUtil.toPreciseQuanta(curAmountWater));
                        }
                    }
                }

                return true;
            }else { //非混合物，很简单，直接像普通流体一样流入即可
                final long filledAmountSnow = host.addAmountInQB(world,downPos,downState,GeoFluids.SNOW,curAmountSnow,true);
                curAmountSnow = curAmountSnow-filledAmountSnow;
                if(curAmountSnow<=0) world.setBlockToAir(pos);
                else {
                    final int quanta = Math.min(QBUtil.toQuanta(curAmountSnow),8);
                    world.setBlockState(pos,state.withProperty(LAYERS,quanta));
                }
            }
            return true;
        }
        else{
            FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,downPos,downState, GeoFluids.SNOW);
            world.setBlockToAir(pos);
            world.setBlockState(downPos,state);
            MiscUtil.scheduleFluidBlockUpdate(world,downPos,this,tickRate(world));
        }
        return true;
    }

    //**********
    // ILayeredFluidHost Block
    // 注意，雪的载流方块的层数为 16，单层为 62.5 mB ，这和雪本身的Layers属性有不同
    //**********

    @Override
    public boolean isAcceptedFluid(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return fluid == FluidRegistry.WATER || fluid == GeoFluids.SNOW;
    }

    @Override
    public int getLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid == FluidRegistry.WATER){
            return state.getValue(MIXTURE)?state.getValue(LAYERS):0;
        }else if(fluid == GeoFluids.SNOW){
            return state.getValue(MIXTURE)?state.getValue(LAYERS):state.getValue(LAYERS)<<1;
        }else if(fluid == null){
            return state.getValue(LAYERS)<<1;
        }
        return 0;
    }

    @Override
    public int getMaxLayers(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid == null) return 16;
        if(!isAcceptedFluid(world, pos, state, fluid)) return 0;
        final boolean mixture = state.getValue(MIXTURE);
        final int layer = state.getValue(LAYERS);
        if(mixture) return 16-layer;
        return fluid == GeoFluids.SNOW?16:16-2*layer; //不是混合物
    }

    @Override
    public int getEmptyHeight(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid) {
        if(fluid == FluidRegistry.WATER){
            return 0;
        }else if(fluid == GeoFluids.SNOW){
            return state.getValue(MIXTURE)?state.getValue(LAYERS)* getHeightPerLayer(world,pos,state):0;
        }
        return 0;
    }

    @Override
    public int getHeightPerLayer(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state) {
        return LayeredFluidHostUtil.SIXTEENTH_HEIGHT;
    }

    @Override
    public long getAmountInQBPerLayer(@Nullable World world, @Nullable BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid) {
        return QBUtil.HALF_QUANTA_VOLUME;
    }

    @Override
    public void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int 添加的层数〇载流方块层, @Nullable NBTTagCompound nbt, int disabledBlockFlags, int enabledBlockFlags) {
        if(!isAcceptedFluid(world, pos, state, fluid)) return;
        if(添加的层数〇载流方块层== 0) return;
        final boolean mixture = state.getValue(MIXTURE);

        try(final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){

            final int flag = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);

            final int 雪本身层〇雪层 = state.getValue(LAYERS);
            int 总层数〇载流方块层 = 雪本身层〇雪层<<1;
            if(mixture){
                int 当前雪层数〇载流方块层 = 雪本身层〇雪层;
                int 当前水层数〇载流方块层 = 雪本身层〇雪层;
                添加的层数〇载流方块层 = MathHelper.clamp(添加的层数〇载流方块层,-雪本身层〇雪层,16-总层数〇载流方块层); //修正

                总层数〇载流方块层 += 添加的层数〇载流方块层; //此时总层数可能范围 1 ～ 16

                if(总层数〇载流方块层==1){ //仅剩一层载流方块层，转换为雪层为零层，此时需要特殊处理，变成空气
                    world.setBlockState(pos,Blocks.AIR.getDefaultState(),flag);
                    return;
                }
                if(fluid == FluidRegistry.WATER){
                    当前水层数〇载流方块层 += 添加的层数〇载流方块层;
                    if(添加的层数〇载流方块层 <0){ //扣水，此时水小于雪
                        world.setBlockState(pos,state.withProperty(MIXTURE,false)
                                .withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*当前水层数〇载流方块层/2d);
                    }else{ //加水，此时水多于雪
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                        if(accessor != null)
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*当前雪层数〇载流方块层/2d);
                    }
                }else{ // 雪
                    当前雪层数〇载流方块层 += 添加的层数〇载流方块层;
                    if(添加的层数〇载流方块层 <0){ //扣雪，此时水多于雪
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                        if(accessor != null)
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*当前雪层数〇载流方块层/2d);
                    }else{ //加雪，此时雪多于水
                        world.setBlockState(pos,state.withProperty(MIXTURE,false)
                                .withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*当前水层数〇载流方块层/2d);
                    }
                }
            }else{ //此时都是纯雪
                if(fluid == GeoFluids.SNOW){ //雪
                    添加的层数〇载流方块层 = MathHelper.clamp(添加的层数〇载流方块层,-总层数〇载流方块层,(8-雪本身层〇雪层)<<1); //(8-雪本身层〇雪层) 计算空余层数〇雪层，然后转换为载流方块层
                    总层数〇载流方块层 += 添加的层数〇载流方块层;
                    if(总层数〇载流方块层 <= 1){
                        world.setBlockState(pos,Blocks.AIR.getDefaultState(),flag);
                    }else world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                }else { //水
                    final int 当前雪层数〇载流方块层 = 总层数〇载流方块层;
                    添加的层数〇载流方块层 = MathHelper.clamp(添加的层数〇载流方块层,0,(8-雪本身层〇雪层)<<1); //(8-雪本身层〇雪层) 计算空余层数〇雪层，然后转换为载流方块层
                    final int 当前水层数〇载流方块层 = 添加的层数〇载流方块层;
                    总层数〇载流方块层 += 添加的层数〇载流方块层; //一定是增加的
                    if(当前水层数〇载流方块层<当前雪层数〇载流方块层){
                        world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA* 当前水层数〇载流方块层/2d);
                    }else if(当前雪层数〇载流方块层 == 当前水层数〇载流方块层){
                        world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1)
                                .withProperty(MIXTURE,true),flag);
                    }else{
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                        if(accessor != null){
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA* 添加的层数〇载流方块层/2d);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer, @Nullable NBTTagCompound nbt, int disabledBlockFlags, int enabledBlockFlags, boolean doAdd) {
        layer = (layer>>1)<<1;
        return IBlockStateLayeredFluidHost.super.addLayer(world, pos, state, fluid, layer, nbt, disabledBlockFlags, enabledBlockFlags, doAdd);
    }

    @Override
    public boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int 新的层数〇载流方块层,@Nullable NBTTagCompound nbt,final int disabledBlockFlags,final int enabledBlockFlags) {
        if(!isAcceptedFluid(world, pos, state, fluid)) return false;
        if(新的层数〇载流方块层<0 || 新的层数〇载流方块层 > 16) return false;
        final boolean mixture = state.getValue(MIXTURE);

        final int flag = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);

        final int 雪本身层〇雪层 = state.getValue(LAYERS);
        int 总层数〇载流方块层 = 雪本身层〇雪层<<1;
        if(mixture){
            int 当前雪层数〇载流方块层 = 雪本身层〇雪层;
            int 当前水层数〇载流方块层 = 雪本身层〇雪层;

            if(新的层数〇载流方块层>16-雪本身层〇雪层) return false; //没有空闲空间了
            if(新的层数〇载流方块层==雪本身层〇雪层) return true; //和原来一样，没变化

            总层数〇载流方块层 = 雪本身层〇雪层 + 新的层数〇载流方块层; //此时总层数可能范围 1 ～ 16

            if(总层数〇载流方块层==1){ //仅剩一层载流方块层，转换为雪层为零层，此时需要特殊处理，变成空气
                return world.setBlockState(pos,Blocks.AIR.getDefaultState(),flag);
            }
            if(fluid == FluidRegistry.WATER){
                当前水层数〇载流方块层 = 新的层数〇载流方块层;
                if(当前水层数〇载流方块层 <当前雪层数〇载流方块层){
                    return world.setBlockState(pos,state.withProperty(MIXTURE,false)
                            .withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                }else{ //水多于雪
                    return world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                }
            }else{ // 雪
                当前雪层数〇载流方块层 = 新的层数〇载流方块层;
                if(当前雪层数〇载流方块层<当前水层数〇载流方块层){ //水多于雪
                    return world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                }else{ //雪多于水
                    return world.setBlockState(pos,state.withProperty(MIXTURE,false)
                            .withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                }
            }
        }else{ //此时都是纯雪
            if(fluid == GeoFluids.SNOW){ //雪
                if(总层数〇载流方块层 == 新的层数〇载流方块层) return true;//没变化
                总层数〇载流方块层 = 新的层数〇载流方块层;
                if(总层数〇载流方块层 <= 1){
                    return world.setBlockState(pos,Blocks.AIR.getDefaultState(),flag);
                }else return world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1),flag);
            }else { //水
                if(新的层数〇载流方块层 == 0) return true;//没变化
                final int 当前雪层数〇载流方块层 = 总层数〇载流方块层;
                if(新的层数〇载流方块层>16-当前雪层数〇载流方块层) return false; //没有空闲空间了
                final int 当前水层数〇载流方块层 = 新的层数〇载流方块层;
                总层数〇载流方块层 = 当前雪层数〇载流方块层+当前水层数〇载流方块层; //相等
                if(当前水层数〇载流方块层<当前雪层数〇载流方块层){ //水少于雪
                    return world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1),flag);
                }else if(当前雪层数〇载流方块层 == 当前水层数〇载流方块层){ //水等于雪
                    return world.setBlockState(pos,state.withProperty(LAYERS,总层数〇载流方块层>>1)
                            .withProperty(MIXTURE,true),flag);
                }else{ //水多余雪
                    return world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1)),flag);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBlockState getLayerState(@Nonnull IBlockState state, @Nonnull Fluid fluid, int 新的层数〇载流方块层) {
        if(!isAcceptedFluid(null,null,state,fluid)) return null;
        if(新的层数〇载流方块层<0 || 新的层数〇载流方块层 > 16) return null;
        final boolean mixture = state.getValue(MIXTURE);
        final int 雪本身层〇雪层 = state.getValue(LAYERS);
        int 总层数〇载流方块层 = 雪本身层〇雪层<<1;

        if(mixture){
            int 当前雪层数〇载流方块层 = 雪本身层〇雪层;
            int 当前水层数〇载流方块层 = 雪本身层〇雪层;

            if(新的层数〇载流方块层>16-雪本身层〇雪层) return null; //没有空闲空间了
            if(新的层数〇载流方块层==雪本身层〇雪层) return state; //和原来一样，没变化

            总层数〇载流方块层 = 雪本身层〇雪层 + 新的层数〇载流方块层; //此时总层数可能范围 1 ～ 16

            if(总层数〇载流方块层==1){ //仅剩一层载流方块层，转换为雪层为零层，此时需要特殊处理，变成空气
                return Blocks.AIR.getDefaultState();
            }
            if(fluid == FluidRegistry.WATER){
                当前水层数〇载流方块层 = 新的层数〇载流方块层;
                if(当前水层数〇载流方块层 <当前雪层数〇载流方块层){
                    return state.withProperty(MIXTURE,false).withProperty(LAYERS,总层数〇载流方块层>>1);
                }else{ //水多于雪
                    return Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1));
                }
            }else{ // 雪
                当前雪层数〇载流方块层 = 新的层数〇载流方块层;
                if(当前雪层数〇载流方块层<当前水层数〇载流方块层){ //水多于雪
                    return Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1));
                }else{ //雪多于水
                    return state.withProperty(MIXTURE,false).withProperty(LAYERS,总层数〇载流方块层>>1);
                }
            }
        }else{ //此时都是纯雪
            if(fluid == GeoFluids.SNOW){ //雪
                if(总层数〇载流方块层 == 新的层数〇载流方块层) return state;//没变化
                总层数〇载流方块层 = 新的层数〇载流方块层;
                if(总层数〇载流方块层 <= 1){
                    return Blocks.AIR.getDefaultState();
                }else return state.withProperty(LAYERS,总层数〇载流方块层>>1);
            }else { //水
                if(新的层数〇载流方块层 == 0) return state;//没变化
                final int 当前雪层数〇载流方块层 = 总层数〇载流方块层;
                if(新的层数〇载流方块层>16-当前雪层数〇载流方块层) return null; //没有空闲空间了
                final int 当前水层数〇载流方块层 = 新的层数〇载流方块层;
                总层数〇载流方块层 = 当前雪层数〇载流方块层+当前水层数〇载流方块层; //相等
                if(当前水层数〇载流方块层<当前雪层数〇载流方块层){ //水少于雪
                    return state.withProperty(LAYERS,总层数〇载流方块层>>1);
                }else if(当前雪层数〇载流方块层 == 当前水层数〇载流方块层){ //水等于雪
                    return state.withProperty(LAYERS,总层数〇载流方块层>>1).withProperty(MIXTURE,true);
                }else{ //水多余雪
                    return Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,8-(总层数〇载流方块层>>1));
                }
            }
        }
    }
}
