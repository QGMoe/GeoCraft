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

package moe.qingu.geocraft.geography.fluidphysics.finite.update;

import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.world.tick.scheduler.BlockTickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import moe.qingu.geocraft.api.block.ILayeredFluidHost;
import moe.qingu.geocraft.api.util.APIMathUtil;
import moe.qingu.geocraft.api.util.FluidUtil;
import moe.qingu.geocraft.api.util.LayeredFluidHostUtil;
import moe.qingu.geocraft.api.util.QBUtil;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.FlowChoice;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.block.finite.ILayeredFluidHostFiniteLiquid;
import moe.qingu.geocraft.configs.FluidPhysicsConfig;
import moe.qingu.geocraft.geography.fluidphysics.pressure.FluidPressureSearchManager;
import moe.qingu.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import moe.qingu.geocraft.geography.fluidphysics.finite.pressure.FinitePressureTasks;
import moe.qingu.geocraft.handler.ServerStatusMonitor;
import moe.qingu.geocraft.util.MiscUtil;
import moe.qingu.geocraft.util.fluid.FluidOperationUtil;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
@ThreadOnly(ThreadType.MINECRAFT_SERVER)
@NotThreadSafe
public final class FiniteFluidVanillaFluidTask implements IFluidTask {
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) List<FlowChoice> averageFlowChoices = new ArrayList<>();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> slopeFlowableDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] slopeFlowDirectionsArr = new EnumFacing[4];
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> bestFlowDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] bestFlowDirectionsArr = new EnumFacing[4];
    private static final @Nonnull IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) MBlockPos facingPos$mut = new MBlockPos();
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
            BlockTickScheduler.schedule(world,pos,state.getBlock(),updateRate);
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
        int liquidQuanta = 8-liquidMeta;
        final int updateFlag = ServerStatusMonitor.getRecommendedBlockFlags();

        final @Nonnull BlockPos downPos = pos.down();
        final @Nonnull IBlockState stateBelow = world.getBlockState(downPos);
        final @Nonnull Block blockBelow = stateBelow.getBlock();
        final boolean canMoveDown = flowing.canFlowDownTo(world,downPos,stateBelow,liquidQuanta,state);

        if(canMoveDown){ //向下流动
            if(flowing.isEqualFluid(stateBelow)){
                flowing.flowDown(world,pos,stateBelow,liquidQuanta,updateRate);
            }else if(blockBelow == Blocks.WATER || blockBelow == Blocks.FLOWING_WATER){ // 岩浆碰到水,消耗岩浆
                liquidQuanta--;
                liquidMeta = 8-liquidQuanta;
                if (liquidQuanta<=0) world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,liquidMeta);
                    world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockTickScheduler.schedule(world,pos,flowing.dynamic, updateRate);
                    world.notifyNeighborsOfStateChange(pos,flowing.dynamic, false);
                }
                world.setBlockState(downPos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, downPos, pos, Blocks.STONE.getDefaultState()));
                FluidOperationUtil.triggerFluidMixEffects(world,downPos);
            }else if(blockBelow instanceof ILayeredFluidHost){
                final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost) blockBelow;
                final long qbToFill = QBUtil.toQBFromQuanta(liquidQuanta);
                final long qbFilled = host.addAmountInQB(world,downPos,stateBelow,fluid,qbToFill,true);
                liquidQuanta = QBUtil.toQuanta(APIMathUtil.clamp(qbToFill-qbFilled,0,qbToFill));
                liquidMeta = 8 -liquidQuanta;
                if(liquidQuanta <=0) world.setBlockState(pos,AIR_DEFAULT_STATE,updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,liquidMeta);
                    world.setBlockState(pos,state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockTickScheduler.schedule(world,pos,flowing.dynamic,updateRate);
                    world.notifyNeighborsOfStateChange(pos,flowing.dynamic,false);
                }
            }else{
                FluidOperationUtil.moveFluid(world,pos,downPos);
            }
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
            updateRate *= 4;
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
        averageFlowChoices.clear();
        final @Nullable Set<EnumFacing> slopeModeFlowDirections = FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()?
                slopeFlowableDirections:null;//多层坡度模式可用方向
        flowing.gatherFlowChoices(world,pos,state,liquidQuanta,averageFlowChoices,slopeModeFlowDirections);

        if(!averageFlowChoices.isEmpty()){
            // *******************
            //  Average Flow
            // *******************
            int newLiquidQuanta = LayeredFluidHostUtil.averageFlow(liquidQuanta,
                    ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA,
                    QBUtil.QUANTA_VOLUME,
                    0,
                    averageFlowChoices);

            if(newLiquidQuanta == liquidQuanta){
                // *******************
                //  Pressure Flow
                // *******************
                this.placeStaticBlock(world,pos,state,FlowingMode.AVERAGE_MODE);
                return;
            }

            long left = 0;
            for(@Nonnull final FlowChoice choice:averageFlowChoices){ //向四周流动
                if(choice.getAddedLayers() == 0){
                    left += choice.getAddedAmountInQB();
                    continue;
                }
                facingPos$mut.setPos(pos).offsetM(choice.direction,1);
                if(choice.isAir()){
                    final @Nonnull IBlockState facingState = world.getBlockState(facingPos$mut);
                    FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,facingPos$mut,facingState,fluid);
                    world.setBlockState(facingPos$mut,
                            flowing.dynamic.getDefaultState().withProperty(LEVEL, 8-choice.getNewLayers()),
                            Constants.BlockFlags.DEFAULT);
                }else{
                    left += choice.apply(world,facingPos$mut,world.getBlockState(facingPos$mut),fluid);
                }
            }
            newLiquidQuanta += QBUtil.toQuanta(left);

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

            averageFlowChoices.clear();
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
