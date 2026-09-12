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

package moe.qingu.orbtellus.geography.fluidphysics.finite.update;

import moe.qingu.orbtellus.api.fluid.unit.QuantaUnit;
import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.Laminarifers;
import moe.qingu.orbtellus.api.laminarifer.flow.AverageFlow;
import moe.qingu.orbtellus.api.laminarifer.flow.source.FlowSources;
import moe.qingu.orbtellus.api.laminarifer.request.FillLaminariferRequest;
import moe.qingu.orbtellus.api.world.tick.scheduler.BlockTickScheduler;
import moe.qingu.orbtellus.geography.fluidphysics.AbstractFluidTask;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.util.APIMathUtil;
import moe.qingu.orbtellus.api.util.FluidUtil;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.util.annotation.ThreadOnly;
import moe.qingu.orbtellus.api.util.annotation.ThreadType;
import moe.qingu.orbtellus.api.laminarifer.flow.FlowChoice;
import moe.qingu.orbtellus.api.util.math.vec.MBlockPos;
import moe.qingu.orbtellus.configs.FluidPhysicsConfig;
import moe.qingu.orbtellus.geography.fluidphysics.pressure.FluidPressureSearchManager;
import moe.qingu.orbtellus.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import moe.qingu.orbtellus.geography.fluidphysics.finite.pressure.FinitePressureTasks;
import moe.qingu.orbtellus.handler.ServerStatusMonitor;
import moe.qingu.orbtellus.util.MiscUtil;
import moe.qingu.orbtellus.util.fluid.FluidOperationUtil;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
@ThreadOnly(ThreadType.MINECRAFT_SERVER)
@NotThreadSafe
public final class FiniteFluidVanillaFluidTask extends AbstractFluidTask {
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> slopeFlowableDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] slopeFlowDirectionsArr = new EnumFacing[4];
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> bestFlowDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] bestFlowDirectionsArr = new EnumFacing[4];
    private static final @Nonnull IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) MBlockPos facingPos$mut = new MBlockPos();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) AverageFlow averageFlow = new AverageFlow(LaminariferModelBuffer.createFiniteVanillaLiquidModel());
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) FillLaminariferRequest fillRequest = new FillLaminariferRequest();
    public final @Nonnull Fluid fluid;
    public final @Nonnull FiniteFlowingVanilla flowing;

    public FiniteFluidVanillaFluidTask(@Nonnull final FiniteFlowingVanilla flowing) {
        this.flowing = flowing;
        this.fluid = flowing.fluid;
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull IBlockState state,@Nonnull final BlockPos pos, @Nonnull final Random rand) {
        int updateRate = MiscUtil.modifyTickRateByGravity(world,this.flowing.dynamic.tickRate(world));
        if (!world.isAreaLoaded(pos,1)){
            BlockTickScheduler.schedule(world,pos,state.getBlock(),updateRate<=0?20:updateRate);
            return;
        }
        int liquidMeta = state.getValue(LEVEL);
        if(liquidMeta >= 8){
            world.setBlockToAir(pos);
            return;
        }else if(updateRate <= 0){//无重力
            flowing.placeStaticBlock(world,pos,state);
            return;
        }
        final int liquidQuanta = 8-liquidMeta;
        final int updateFlag = ServerStatusMonitor.getRecommendedBlockFlags();

        final @Nonnull BlockPos downPos = pos.down();
        final @Nonnull IBlockState stateBelow = world.getBlockState(downPos);
        final @Nonnull Block blockBelow = stateBelow.getBlock();

        verticalFlow:
        if(Laminarifers.isLaminarifer(stateBelow) || flowing.canFlowDownTo(stateBelow)){ //向下流动
            final int newLiquidQuanta;
            final int newLiquidMeta;
            if(flowing.isEqualFluid(stateBelow)){
                flowing.flowDown(world,pos,stateBelow,liquidQuanta,updateRate);
            }else if(blockBelow == Blocks.WATER || blockBelow == Blocks.FLOWING_WATER){ // 岩浆碰到水,消耗岩浆
                newLiquidQuanta = liquidQuanta - 1;
                newLiquidMeta = 8 - newLiquidQuanta;
                if (newLiquidQuanta<=0) world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,newLiquidMeta);
                    world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockTickScheduler.schedule(world,pos,flowing.dynamic, updateRate);
                    world.notifyNeighborsOfStateChange(pos,flowing.dynamic, false);
                }
                assert Blocks.STONE != null;
                world.setBlockState(downPos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, downPos, pos, Blocks.STONE.getDefaultState()));
                FluidOperationUtil.triggerFluidMixEffects(world,downPos);
            }else if(Laminarifers.isLaminarifer(blockBelow)){
                try (final FillLaminariferRequest request = fillRequest.open()){
                    final long qbToFill = QuantaUnit.toQB(liquidQuanta);
                    final long qbFilled = request.to(world,downPos,stateBelow).side(EnumFacing.UP)
                            .target((ILaminarifer) blockBelow)
                            .specific(FluidRegistry.WATER).amount(qbToFill)
                            .source(FlowSources.RUNOFF)
                            .fill(true);
                    newLiquidQuanta = QBUnit.sampleQuantaAsInt(rand,APIMathUtil.clamp(qbToFill - qbFilled,0L,qbToFill));
                    if(newLiquidQuanta == liquidQuanta) break verticalFlow;
                    newLiquidMeta = 8 - newLiquidQuanta;
                }
                if(newLiquidQuanta <= 0) world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,newLiquidMeta);
                    world.setBlockState(pos,state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockTickScheduler.schedule(world,pos,flowing.dynamic,updateRate);
                    world.notifyNeighborsOfStateChange(pos,flowing.dynamic,false);
                }
            }else FluidOperationUtil.moveFluid(world,pos,downPos);
            return;
        }

        // *******************
        //  Pressure Flow
        // *******************
        if(checkPressureTask(world,pos,state)){
            BlockTickScheduler.schedule(world,pos,flowing.dynamic, updateRate);
            return;
        }

        if ((state.getMaterial() == Material.LAVA) && rand.nextInt(4) != 0){ //岩浆速度处理
            updateRate <<= 2;
        }

        if(liquidMeta == 7){
            if(!FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaIs1.getValue() && flowing.isEqualFluid(stateBelow)){
                this.placeStaticBlock(world,pos,state,FlowingMode.SLOPE_MODE_ON_WATER);
                return;
            }
            // *******************
            //  Single Quanta Slope Flow
            // *******************
            if (!world.isAreaLoaded(pos, flowing.getSingleSlopeFindDistance(world))){
                BlockTickScheduler.schedule(world,pos,state.getBlock(),updateRate);
                return;
            }
            slopeFlowableDirections.clear();
            flowing.singleSlopeAlgorithm(world, pos, slopeFlowableDirections);
            if(slopeFlowableDirections.isEmpty()){
                this.placeStaticBlock(world,pos,state,FlowingMode.SLOPE_MODE);
            }else {
                int i = 0;
                for(@Nonnull final EnumFacing dir:slopeFlowableDirections){
                    slopeFlowDirectionsArr[i++] = dir;
                }
                final @Nonnull EnumFacing randomFacing = slopeFlowDirectionsArr[rand.nextInt(i)];
                world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag);
                facingPos$mut.setPos(pos).offsetM(randomFacing,1);
                flowing.flowInto(world, facingPos$mut, world.getBlockState(facingPos$mut), 7);
            }
            return;
        }

        //可流动方向检查
        try (final AverageFlow flow = averageFlow){
            flow.at(world,pos)
                    .fluid(FluidRegistry.WATER)
                    .source(FlowSources.RUNOFF)
                    .centralModel.currentLayers = liquidQuanta;
            final @Nullable Set<EnumFacing> slopeModeFlowDirections = FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()?
                    slopeFlowableDirections:null;//多层坡度模式可用方向
            if(slopeModeFlowDirections != null) slopeModeFlowDirections.clear();
            flowing.gatherFlowChoices(flow,slopeModeFlowDirections);

            if(flow.hasNext()){
                // *******************
                //  Average Flow
                // *******************

                if(!flow.resolve()){
                    // *******************
                    //  Pressure Flow
                    // *******************
                    this.placeStaticBlock(world,pos,state,FlowingMode.AVERAGE_MODE);
                    return;
                }

                long left = flow.extraAmountInQB;
                while (flow.hasNext()){
                    final FlowChoice choice = flow.next();
                    if(choice.isAir()){
                        choice.sampleExtraAmount(rand);
                        if(choice.getNewLayers() <= 0L) continue;
                        facingPos$mut.setPos(pos).offsetM(choice.direction,1);
                        final @Nonnull IBlockState facingState = world.getBlockState(facingPos$mut);
                        FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,facingPos$mut,facingState,fluid);
                        world.setBlockState(facingPos$mut,
                                flowing.dynamic.getDefaultState().withProperty(LEVEL, 8-(int) choice.getNewLayers()),
                                Constants.BlockFlags.DEFAULT);
                    }else{
                        left += flow.applyCurrentChoice();
                    }
                }

                final int newLiquidQuanta = (int)(flow.finalLayers + QBUnit.sampleQuanta(rand, left));

                liquidMeta = 8 - newLiquidQuanta;
                if (newLiquidQuanta<=0) world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,liquidMeta);
                    world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockTickScheduler.schedule(world,pos,flowing.dynamic, updateRate);
                    if(FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue() && !FluidPressureSearchManager.isTaskRunning(world,pos)){
                        // *******************
                        //  Pressure Flow [Average]
                        // *******************
                        createFluidPressureSearchTask(world,pos,state,FlowingMode.AVERAGE_MODE);
                    }
                    world.notifyNeighborsOfStateChange(pos,flowing.dynamic, false);
                }
            }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()) {
                // ********************
                //  Multi-Quanta Slope Flow
                // ********************
                if(!world.isAreaLoaded(pos, flowing.getMultiSlopeFindDistance(world))){
                    BlockTickScheduler.schedule(world,pos,state.getBlock(),updateRate);
                    return;
                }
                bestFlowDirections.clear();
                flowing.multiSlopeAlgorithm(world, pos, slopeModeFlowDirections, liquidQuanta,bestFlowDirections);
                if (bestFlowDirections.isEmpty()) {
                    this.placeStaticBlock(world, pos, state,FlowingMode.SLOPE_MODE_ON_WATER_2);
                    return;
                }
                int i = 0;
                for(@Nonnull final EnumFacing dir:bestFlowDirections){
                    bestFlowDirectionsArr[i++] = dir;
                }
                @Nonnull final EnumFacing flowDir = bestFlowDirectionsArr[rand.nextInt(i)];
                final int newLiquidQuanta = liquidQuanta - 1;
                final int newLiquidMeta = 8 - newLiquidQuanta;
                //更新自己
                state = state.withProperty(LEVEL, newLiquidMeta);
                world.setBlockState(pos, state, updateFlag);
                BlockTickScheduler.schedule(world,pos, flowing.dynamic, updateRate);
                world.notifyNeighborsOfStateChange(pos, flowing.dynamic, false);
                //移动至新位置
                flowing.placeDynamicBlock(world, pos.offset(flowDir), liquidMeta);
            }else {
                // *******************
                //  Pressure Flow
                // *******************
                this.placeStaticBlock(world,pos,state,FlowingMode.NO_MODE);
            }
        }
    }

    @Override
    public void onFailure(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
        flowing.placeStaticBlock(world,pos,state);
    }

    @Override
    public boolean accepts(@Nonnull final World world, @Nonnull final IBlockState state) {
        return state.getBlock() == flowing.dynamic;
    }

    private void createFluidPressureSearchTask(final @Nonnull World world,
                                               final @Nonnull BlockPos pos,
                                               final @Nonnull IBlockState state,
                                               final @Nonnull FlowingMode mode){
        switch (mode){
            case AVERAGE_MODE:
                FluidPressureSearchManager.addTask(world, FinitePressureTasks.createVanillaTask(fluid,state,pos,0));
                break;
            case SLOPE_MODE:return;
            default:
                FluidPressureSearchManager.addTask(world,
                        FinitePressureTasks.createVanillaTask(fluid,state,pos,
                                FluidPhysicsConfig.PRESSURE_TASK_RANGE_DYNAMIC_FLUID_NO_AVERAGE.getValue()));
        }
    }

    private boolean checkPressureTask(final @Nonnull World worldIn,final @Nonnull BlockPos pos,final @Nonnull IBlockState state){
        final @Nullable IBlockState result = flowing.tryPressureFlow(worldIn,pos,state, Constants.BlockFlags.DEFAULT);
        return result != null && result != state;
    }

    private void placeStaticBlock(final @Nonnull World worldIn,
                                  final @Nonnull BlockPos pos,
                                  final @Nonnull IBlockState currentState,
                                  final @Nonnull FlowingMode mode){
        flowing.placeStaticBlock(worldIn,pos,currentState);
        if(mode == FlowingMode.SLOPE_MODE) return;
        if(!FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue()) return;

        IBlockState newState = worldIn.getBlockState(pos);
        if(newState.getMaterial().isLiquid()){
            if(FluidPressureSearchManager.isTaskRunning(worldIn,pos)){
                return;
            }
            IBlockState upState = worldIn.getBlockState(pos.up());
            if(FluidUtil.getFluid(upState)==fluid){
                if(upState.getValue(LEVEL)==0)return;
            }
            createFluidPressureSearchTask(worldIn,pos,newState,mode);
        }
    }

    enum FlowingMode{
        NO_MODE,
        SLOPE_MODE,
        SLOPE_MODE_ON_WATER,
        SLOPE_MODE_ON_WATER_2,
        AVERAGE_MODE
    }
}
