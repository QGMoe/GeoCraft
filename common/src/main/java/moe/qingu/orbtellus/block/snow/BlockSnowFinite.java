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

package moe.qingu.orbtellus.block.snow;

import moe.qingu.orbtellus.api.fluid.QBFluidStack;
import moe.qingu.orbtellus.api.fluid.unit.FluidUnit;
import moe.qingu.orbtellus.api.fluid.unit.QuantaUnit;
import moe.qingu.orbtellus.api.laminarifer.*;
import moe.qingu.orbtellus.api.laminarifer.flow.drainer.IFlowDrainer;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.laminarifer.request.FillLaminariferRequest;
import moe.qingu.orbtellus.api.util.APIMathUtil;
import moe.qingu.orbtellus.api.util.math.vec.MBlockPos;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import moe.qingu.orbtellus.api.OTCFluids;
import moe.qingu.orbtellus.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.orbtellus.api.util.AtmosphereUtil;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.geography.snow.SnowFlowing;
import moe.qingu.orbtellus.util.MiscUtil;
import moe.qingu.orbtellus.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static moe.qingu.orbtellus.api.block.BlockProperties.MIXTURE;

/**
 * @since 0.2.0-beta.2
 * @author QiguaiAAAA
 */
public class BlockSnowFinite extends BlockSnowExtended implements IBlockStateLaminarifer, IOperateLayerLaminarifer, IFlowSource<BlockSnowFinite> {
    protected static final MBlockPos mPos = new MBlockPos();
    protected static final FillLaminariferRequest fillRequest = new FillLaminariferRequest();

    @Override
    public int tickRate(final @Nonnull World worldIn) {
        return 5;
    }

    @Override
    public boolean canPlaceBlockAt(final @Nonnull World worldIn,final @Nonnull BlockPos pos) {
        final BlockPos down = pos.down();
        final IBlockState state = worldIn.getBlockState(down);
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

    //**********
    // 雪的下落
    //**********

    protected final boolean tryFallDown(final @Nonnull World world,final @Nonnull BlockPos pos,final @Nonnull IBlockState state){
        if(world.isRemote) return false;
        final BlockPos downPos = mPos.setPos(pos).downM();
        final IBlockState downState = world.getBlockState(downPos);
        if(SnowFlowing.isBlocked(downState)){
//            if(!canBePlacedOn(world,downPos,downState)){
//                world.setBlockToAir(pos);
//            }
            return false;
        }
        final Block downBlock = downState.getBlock();
        if(downBlock == Blocks.SNOW_LAYER) return tryFallUponSnow(world, pos, state, downState);
        else if(Laminarifers.isLaminarifer(downBlock)) return tryFallUponLaminarifer(world, pos, state, downState);
        else { //直接下落
            FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,downPos,downState, OTCFluids.SNOW);
            world.setBlockToAir(pos);
            world.setBlockState(downPos,state);
            MiscUtil.scheduleFluidBlockUpdate(world,downPos,this,tickRate(world));
            return true;
        }
    }

    protected final boolean tryFallUponSnow(final @Nonnull World world,
                                            final @Nonnull BlockPos pos,
                                            final @Nonnull IBlockState state,
                                            final @Nonnull IBlockState downState){
        final BlockPos downPos = mPos.setPos(pos).downM();
        final int $总层数_存储层 = state.getValue(BlockSnow.LAYERS) + downState.getValue(BlockSnow.LAYERS);
        if(state.getValue(MIXTURE) == downState.getValue(MIXTURE)){ //类型相同，直接合并
            if($总层数_存储层<=8){
                world.setBlockToAir(pos);
                world.setBlockState(downPos,downState.withProperty(BlockSnow.LAYERS,$总层数_存储层));
            }else{
                world.setBlockState(pos,state.withProperty(BlockSnow.LAYERS,$总层数_存储层-8));
                world.setBlockState(downPos,downState.withProperty(BlockSnow.LAYERS,8));
            }
        }else{ //否则，用统一的水雪混合逻辑
            final long $总水量_载流层 = getLayers(state,FluidRegistry.WATER,null) + getLayers(downState,FluidRegistry.WATER,null);
            final long $总雪量_载流层 = getLayers(state,OTCFluids.SNOW,null) + getLayers(downState,OTCFluids.SNOW,null);
            final Random rand = world.rand;
            try(final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){
                if($总层数_存储层<=8){
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.NO_OBSERVERS);
                    final long $总层数_载流层 = $总水量_载流层 + $总雪量_载流层;
                    final int $采样总量_存储层 = (int) FluidUnit.sample(rand,$总层数_载流层,2L);
                    final int $采样水量_存储层 = $采样总量_存储层 <= 0? 0 : (int) FluidUnit.sample(rand,$采样总量_存储层 * $总水量_载流层 , $总层数_载流层);
                    SnowFlowing.mixSnowWithWater(world,downPos,accessor,
                            $采样水量_存储层,$采样总量_存储层 - $采样水量_存储层, Constants.BlockFlags.DEFAULT);
                    world.notifyNeighborsRespectDebug(pos, Blocks.AIR, true);
                }else{
                    final int $总水量_存储层 = (int) FluidUnit.sample(rand, $总水量_载流层, 2L);
                    final int $下方水量_存储层 = (int) FluidUnit.sample(rand, 8L * $总水量_存储层, $总层数_存储层); // 总水量 * ( 8 / 总层数 )
                    final int $上方水量_存储层 = $总水量_存储层 - $下方水量_存储层;
                    final IBlockState newUpState = SnowFlowing.mixSnowWithWater(world, pos, accessor,
                            $上方水量_存储层, $总层数_存储层 - 8 - $上方水量_存储层, Constants.BlockFlags.NO_OBSERVERS);
                    SnowFlowing.mixSnowWithWater(world, downPos, accessor, $下方水量_存储层, 8 - $下方水量_存储层, Constants.BlockFlags.DEFAULT);
                    world.notifyNeighborsRespectDebug(pos, newUpState.getBlock(), true);
                }
            }
        }
        return true;
    }

    protected final boolean tryFallUponLaminarifer(final @Nonnull World world,
                                                   final @Nonnull BlockPos pos,
                                                   final @Nonnull IBlockState state,
                                                   final @Nonnull IBlockState downState){
        final BlockPos downPos = mPos.setPos(pos).downM();
        final long $当前水量_载流层 = getLayers(state,FluidRegistry.WATER,null);
        final long $当前雪量_载流层 = getLayers(state,OTCFluids.SNOW,null);
        final Random rand = world.rand;

        final long $总量_存储层 = ($当前水量_载流层 + $当前雪量_载流层) >> 1;
        final long $填充水量_存储层 = FluidUnit.sample(rand,$当前水量_载流层,2L);

        final long initState = (QuantaUnit.toQB($填充水量_存储层) & 0xFFFF_FFFFL) | (QuantaUnit.toQB($总量_存储层 - $填充水量_存储层) << 32); // 32 位宽足够
        final long finalState = fillDownByOrder(world,downPos,downState, initState);
        if(initState == finalState) return false;

        try(final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){
            final long $剩余水量_QB = finalState & 0xFFFF_FFFFL;
            final long $剩余总量_QB = (finalState >>> 32) + $剩余水量_QB;
            final int $采样总量_存储层 = QBUnit.sampleQuantaAsInt(rand, $剩余总量_QB);
            final int $采样水量_存储层 = $采样总量_存储层 <= 0? 0 : (int) FluidUnit.sample(rand,$采样总量_存储层 * $剩余水量_QB , $剩余总量_QB);
            SnowFlowing.mixSnowWithWater(world,pos,accessor, $采样水量_存储层,$采样总量_存储层 - $采样水量_存储层, Constants.BlockFlags.DEFAULT);
        }
        return true;
    }

    protected final long fillDownByOrder(final @Nonnull World world,
                                            final @Nonnull BlockPos downPos,
                                            @Nonnull IBlockState downState,
                                            long left){
        final long initLeft = left;
        for(int i = 0; i< SnowFlowing.FILL_ORDER.length; i++){
            final int offset = i<<5;
            final long qb = (left >>> offset) & 0xFFFF_FFFFL;
            if(qb == 0L) continue;
            final Block downBlock = downState.getBlock();
            if(!Laminarifers.isLaminarifer(downBlock)) break;
            try (final FillLaminariferRequest request = fillRequest){
                final long filled = request.to(world, downPos, downState)
                        .side(EnumFacing.UP)
                        .specific(SnowFlowing.FILL_ORDER[i])
                        .amount(qb)
                        .source(this)
                        .disableFlags(Constants.BlockFlags.NOTIFY_NEIGHBORS)
                        .enableFlags(Constants.BlockFlags.NO_OBSERVERS)
                        .fill(true);
                if(filled <= 0L) continue;
                left &= ~(0xFFFF_FFFFL << offset);
                left |= (Math.max(0L, qb - filled) << offset);
                downState = world.getBlockState(downPos);
            }
        }
        if(initLeft != left) world.notifyNeighborsRespectDebug(downPos, downState.getBlock(), true);
        return left;
    }

    //**********
    // 载流方块
    // 注意，雪的载流方块的层数为 16，单层为 62.5 mB ，这和雪本身的Layers属性有不同
    //**********


    @Override
    public final boolean isAcceptedFluid(@Nonnull final IBlockState state,
                                   @Nonnull final Fluid fluid,
                                   @Nullable final NBTTagCompound nbt) {
        return fluid == FluidRegistry.WATER || fluid == OTCFluids.SNOW;
    }

    @Override
    public final long getMaxLayers(@Nonnull final IBlockState state,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt) {
        return ((SnowBlockState)state).getMaxAmount(fluid);
    }

    @Override
    public final long getLayers(@Nonnull final IBlockState state,
                          @Nonnull final Fluid fluid,
                          @Nullable final NBTTagCompound nbt) {
        return ((SnowBlockState)state).getAmount(fluid);
    }

    @Override
    public final long getEmptyHeight(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt) {
        return ((SnowBlockState)state).getEmptyHeight(fluid);
    }

    @Override
    public final long getHeightPerLayer(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt) {
        return AHUnit.SIXTEENTH_BLOCK;
    }

    @Override
    public final long getAmountInQBPerLayer(@Nonnull final IBlockState state, @Nonnull final Fluid fluid, @Nullable final NBTTagCompound nbt) {
        return QBUnit.HALF_QUANTA_VOLUME;
    }

    @Override
    public long operateLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nonnull final Fluid fluid,
                             @Nullable final NBTTagCompound nbt,
                             final long $变化层数_载流层,
                             final boolean doOperate,
                             final long pulse,
                             final long blockFlagsModifier) {
        if(!isAcceptedFluid(state, fluid, nbt)) return 0L;

        final int flags = BlockFlagModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
        final long $当前层数_载流层 = getLayers(state, fluid, nbt);
        final long $实际变化层数_载流层 = APIMathUtil.clamp($变化层数_载流层, -$当前层数_载流层, getMaxLayers(state, fluid, nbt) - $当前层数_载流层);

        if($实际变化层数_载流层 == 0L) return 0L;

        final long $新雪层_载流层 = getLayers(state,OTCFluids.SNOW,null) + (fluid == OTCFluids.SNOW? $实际变化层数_载流层 : 0L);
        final long $新水层_载流层 = getLayers(state,FluidRegistry.WATER,null) + (fluid == FluidRegistry.WATER? $实际变化层数_载流层 : 0L);

        final long $总层数_载流层 = $新雪层_载流层 + $新水层_载流层;
        final int $新雪层_存储层;
        final int $新水层_存储层;

        if($总层数_载流层 > 0L){
            final long $总层数_存储层 = FluidUnit.sample(world.rand, $总层数_载流层, 2L);
            $新雪层_存储层 = (int) FluidUnit.sample(world.rand, $总层数_存储层 * $新雪层_载流层, $总层数_载流层);
            $新水层_存储层 = (int) $总层数_存储层 - $新雪层_存储层;
        }else $新雪层_存储层 = $新水层_存储层 = 0;

        try (final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){
            SnowFlowing.mixSnowWithWater(world, pos, accessor, $新水层_存储层, $新雪层_存储层, flags);
        }

        return $实际变化层数_载流层;
    }

    @Override
    public final IBlockState getLayerState(@Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     @Nullable final NBTTagCompound nbt,
                                     final long $新层数_载流层) {
        if(!isAcceptedFluid(state,fluid,nbt)) return null;
        if($新层数_载流层 < 0L || $新层数_载流层 > 16L) return null;
        if(getLayers(state,fluid,nbt) == $新层数_载流层) return state;
        final long $当前水层_载流层 = fluid == FluidRegistry.WATER? $新层数_载流层 : getLayers(state,FluidRegistry.WATER,null);
        final long $当前雪层_载流层 = fluid == OTCFluids.SNOW? $新层数_载流层: getLayers(state,OTCFluids.SNOW,null);
        final long $总层数_载流层 = $当前水层_载流层 + $当前雪层_载流层;
        if($总层数_载流层 > 16L) return null;
        final int $当前水层_存储层;
        final int $当前雪层_存储层;
        if($总层数_载流层 > 0L){
            final Random rnd = ThreadLocalRandom.current();
            final long $总层数_存储层 = FluidUnit.sample(rnd, $总层数_载流层, 2L);
            $当前水层_存储层 = (int) FluidUnit.sample(rnd, $总层数_存储层 * $当前水层_载流层, $总层数_载流层);
            $当前雪层_存储层 = (int) $总层数_存储层 - $当前水层_存储层;
        }else $当前水层_存储层 = $当前雪层_存储层 = 0;

        return SnowFlowing.getSnowWaterMixStateDynamic($当前雪层_存储层,$当前水层_存储层);
    }

    @Nullable
    @Override
    public QBFluidStack drainStackInQB(@Nonnull final World world,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final IBlockState state,
                                       @Nullable final Fluid fluid,
                                       final long amount,
                                       final boolean doOperate,
                                       final long pulse,
                                       @Nullable final IFlowDrainer<?> drainer,
                                       final long blockFlagsModifier) {
        final long drained;
        final Fluid fluidToExtract;
        if(fluid == null){
            final long water = getLayers(state,FluidRegistry.WATER,null);
            fluidToExtract = water <=0L ? OTCFluids.SNOW : FluidRegistry.WATER;
            drained = Laminarifers.extractAmountInQB(this, world, pos, state, fluidToExtract, null, amount, doOperate, pulse, drainer, blockFlagsModifier);
        }else{
            if(!isAcceptedFluid(state, fluid, null)) return null;
            fluidToExtract = fluid;
            drained = Laminarifers.extractAmountInQB(this, world, pos, state, fluid, null, amount, doOperate, pulse, drainer, blockFlagsModifier);
        }
        return new QBFluidStack(fluidToExtract, drained);
    }
}
