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

package top.qiguaiaaaa.geocraft.mixin.reality.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.block.IBlockSnow;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.RealitySnowUpdater;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.MIXTURE;

/**
 * @see top.qiguaiaaaa.geocraft.mixin.common.block.BlockSnowMixin
 * @author QiguaiAAAA
 */
@Deprecated
@Mixin(value = BlockSnow.class)
public class BlockSnowMixin extends Block implements IBlockStateLayeredFluidHost, IBlockSnow {
    @Shadow @Final public static PropertyInteger LAYERS;

    public BlockSnowMixin(Material materialIn) {
        super(materialIn);
    }

    /**
     * 用于下落动作
     */
    @Override
    @Unique
    public int tickRate(@Nonnull World worldIn) {
        return 5;
    }

    @Inject(method = "canPlaceBlockAt",at = @At("HEAD"),cancellable = true)
    public void canPlaceBlockAt(World worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        final BlockPos down = pos.down();
        IBlockState state = worldIn.getBlockState(down);
        cir.setReturnValue(canBePlacedOn(worldIn,down,state));
    }

    @Inject(method = "checkAndDropBlock",at = @At("HEAD"),cancellable = true)
    private void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!this.canPlaceBlockAt(worldIn, pos)) {
            worldIn.scheduleUpdate(pos,this,tickRate(worldIn));
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }

    /**
     * 注意该Mixin的优先级低于 {@link top.qiguaiaaaa.geocraft.mixin.common.block.BlockSnowMixin#updateTick(World, BlockPos, IBlockState, Random, CallbackInfo)}
     * 因此当前的 Mixin 会覆盖 Common 的 Mixin
     * @reason 复写自定义逻辑
     */
    @Inject(method = "updateTick",at =@At("HEAD"),cancellable = true)
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        ci.cancel();
        if(worldIn.isRemote) return;
        if(trySmelt(worldIn, pos, state, rand)) return;
        state = worldIn.getBlockState(pos);
        tryFallDown(worldIn, pos, state);
    }

    @Inject(method = "isReplaceable",at =@At("HEAD"),cancellable = true)
    public void isReplaceable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos,CallbackInfoReturnable<Boolean> cir){
        cir.cancel();
        cir.setReturnValue(false);
    }

    protected boolean canBePlacedOn(@Nonnull World world,@Nonnull BlockPos downPos,@Nonnull IBlockState downState){
        Block block = downState.getBlock();

        BlockFaceShape shape = downState.getBlockFaceShape(world, downPos, EnumFacing.UP);
        return shape == BlockFaceShape.SOLID || block.isLeaves(downState, world, downPos) || block == this && downState.getValue(BlockSnow.LAYERS) == 8;
    }

    @Unique
    protected boolean tryFallDown(World world,BlockPos pos,IBlockState state){
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
                final int totalWater = isMixture? getLayers(world,pos,state,FluidRegistry.WATER): getLayers(world,downPos,downState,FluidRegistry.WATER);
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
            long curAmountSnow = getAmountInQB(world,pos,state,GeoFluids.SNOW);
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
            world.scheduleUpdate(downPos,this,tickRate(world));
        }
        return true;
    }

    @Shadow
    public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {return false;}

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
        boolean mixture = state.getValue(MIXTURE);
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
    public void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer, @Nullable NBTTagCompound nbt, int disabledBlockFlags, int enabledBlockFlags) {
        if(!isAcceptedFluid(world, pos, state, fluid)) return;
        if(layer == 0) return;
        final boolean mixture = state.getValue(MIXTURE);

        try(final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){

            final int flag = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);

            int curSnowLayer = state.getValue(LAYERS);
            if(mixture){
                int curWaterLayer = curSnowLayer;
                layer = MathHelper.clamp(layer,-curWaterLayer,16-2*curWaterLayer);
                if(fluid == FluidRegistry.WATER){
                    curWaterLayer += layer;
                    if(layer <0){
                        world.setBlockState(pos,state.withProperty(MIXTURE,false)
                                .withProperty(LAYERS,(curSnowLayer+curWaterLayer)>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*curWaterLayer/2d);
                    }else{
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(curSnowLayer+curWaterLayer)/2),flag);
                        if(accessor != null)
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*curSnowLayer/2d);
                    }
                }else{
                    curSnowLayer += layer;
                    if(layer <0){
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(curSnowLayer+curWaterLayer)/2),flag);
                        if(accessor != null)
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*curSnowLayer/2d);
                    }else{
                        world.setBlockState(pos,state.withProperty(MIXTURE,false)
                                .withProperty(LAYERS,(curSnowLayer+curWaterLayer)>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*curWaterLayer/2d);
                    }
                }
            }else{
                if(fluid == GeoFluids.SNOW){
                    layer = MathHelper.clamp(layer,-(curSnowLayer<<1),(8-curSnowLayer)<<1);
                    world.setBlockState(pos,state.withProperty(LAYERS,curSnowLayer+ (layer>>1)),flag);
                }else {
                    layer = MathHelper.clamp(layer,0,(8-curSnowLayer)<<1);
                    if(layer <curSnowLayer<<1){
                        world.setBlockState(pos,state.withProperty(LAYERS,((curSnowLayer<<1)+ layer)>>1),flag);
                        if(accessor != null)
                            accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA* layer/2d);
                    }else if(layer == curSnowLayer<<1){
                        world.setBlockState(pos,state.withProperty(LAYERS,((curSnowLayer<<1)+ layer)>>1)
                                .withProperty(MIXTURE,true),flag);
                    }else{
                        world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                                .withProperty(BlockLiquid.LEVEL,8-(((curSnowLayer<<1)+ layer)>>1)),flag);
                        if(accessor != null){
                            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA* layer/2d);
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
    public boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer,@Nullable NBTTagCompound nbt,final int disabledBlockFlags,final int enabledBlockFlags) {
        if(!isAcceptedFluid(world, pos, state, fluid)) return false;
        if(newLayer<0 || newLayer > 16) return false;
        final boolean mixture = state.getValue(MIXTURE);

        final int flag = APIMathUtil.getModifiedFlag(Constants.BlockFlags.DEFAULT,disabledBlockFlags,enabledBlockFlags);

        int curSnowLayer = state.getValue(LAYERS);
        if(mixture){
            int curWaterLayer = curSnowLayer;
            if(fluid == FluidRegistry.WATER){
                curWaterLayer = newLayer;
                if(curWaterLayer <curSnowLayer){
                    world.setBlockState(pos,state.withProperty(MIXTURE,false)
                            .withProperty(LAYERS,(curSnowLayer+curWaterLayer)>>1),flag);
                }else if(curWaterLayer>curSnowLayer){
                    world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(curSnowLayer+curWaterLayer)/2),flag);
                }
            }else{
                curSnowLayer = newLayer;
                if(curSnowLayer <curWaterLayer){
                    world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(curSnowLayer+curWaterLayer)/2),flag);
                }else if(curSnowLayer>curWaterLayer){
                    world.setBlockState(pos,state.withProperty(MIXTURE,false)
                            .withProperty(LAYERS,(curSnowLayer+curWaterLayer)>>1),flag);
                }
            }
        }else{
            if(fluid == GeoFluids.SNOW){
                world.setBlockState(pos,state.withProperty(LAYERS,newLayer>>1),flag);
            }else {
                if(newLayer <curSnowLayer<<1){
                    world.setBlockState(pos,state.withProperty(LAYERS,curSnowLayer+ (newLayer>>1)),flag);
                }else if(newLayer == curSnowLayer<<1){
                    world.setBlockState(pos,state.withProperty(LAYERS,curSnowLayer+ (newLayer>>1))
                            .withProperty(MIXTURE,true),flag);
                }else{
                    world.setBlockState(pos,Blocks.FLOWING_WATER.getDefaultState()
                            .withProperty(BlockLiquid.LEVEL,8-(curSnowLayer+ (newLayer>>1))),flag);
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public IBlockState getLayerState(@Nonnull IBlockState state, @Nonnull Fluid fluid, int layer) {
        if(!isAcceptedFluid(null,null,state,fluid)) return null;
        final boolean mixture = state.getValue(MIXTURE);
        final int curLayer = state.getValue(LAYERS);
        if(layer<0) return null;
        if(mixture && layer > getMaxLayers(null, null, state, fluid)) return null;
        else if(!mixture){
            if(fluid == FluidRegistry.WATER) if(layer>16-curLayer*2) return null;
            if(fluid == GeoFluids.SNOW) if(layer>16) return null;
        }

        if(fluid == GeoFluids.SNOW){
            if(mixture){
                if(layer ==curLayer) return state;
                if(layer == 0 && curLayer>1) return Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,8-curLayer/2);
                if(layer <curLayer) return Blocks.FLOWING_WATER.getDefaultState()
                        .withProperty(BlockLiquid.LEVEL,8-(curLayer+ layer)/2);
                return state.withProperty(MIXTURE,false).withProperty(LAYERS,(curLayer+ layer)>>1);
            }
            if(layer == 0) return Blocks.AIR.getDefaultState();
            return state.withProperty(LAYERS, layer>>1);
        }else if(fluid == FluidRegistry.WATER){
            if(mixture){
                if(layer ==curLayer) return state;
                if(layer == 0) return state.withProperty(MIXTURE,false)
                        .withProperty(LAYERS,curLayer/2);
                if(layer >curLayer) return Blocks.FLOWING_WATER.getDefaultState()
                        .withProperty(BlockLiquid.LEVEL,8-(curLayer+ layer)/2);
                return state.withProperty(MIXTURE,false).withProperty(LAYERS,(curLayer+ layer)>>1);
            }
            if(layer == 0) return state;
            if(layer == curLayer<<1) return
                    state.withProperty(MIXTURE,true)
                            .withProperty(LAYERS, layer);
            else if(layer > curLayer<<1) return Blocks.FLOWING_WATER.getDefaultState()
                    .withProperty(BlockLiquid.LEVEL,8-(curLayer*2+ layer)/2);
            else return state.withProperty(LAYERS, (layer +curLayer*2)>>1);
        }
        return null;
    }
}
