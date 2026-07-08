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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.update;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.OldFlowChoice;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingClassic;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.update.FluidUpdateBaseTask;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.pressure.FinitePressureTasks;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import java.util.*;

import static net.minecraftforge.fluids.BlockFluidBase.LEVEL;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.slopeModeForModsWhenOnFluidsAndQuantaAbove1;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.slopeModeForModsWhenOnFluidsAndQuantaIs1;

/**
 * @author QiguaiAAAA
 */
@ThreadOnly(ThreadType.MINECRAFT_SERVER)
@NotThreadSafe
public class FiniteFluidClassicUpdateTask extends FluidUpdateBaseTask{
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) List<OldFlowChoice> averageFlowChoices = new ArrayList<>(5);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> slopeFlowableDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] slopeFlowDirectionsArr = new EnumFacing[4];
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> bestFlowDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) EnumFacing[] bestFlowDirectionsArr = new EnumFacing[4];
    protected final @Nonnull FiniteFlowingClassic flowing;
    protected IBlockState state;
    protected int tickRate;
    public FiniteFluidClassicUpdateTask(@Nonnull final BlockPos pos,
                                        @Nonnull final FiniteFlowingClassic flowingHandler) {
        super(flowingHandler.fluid, pos);
        this.flowing = flowingHandler;
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull final IBlockState curState, @Nonnull final Random rand) {
        this.tickRate = flowing.tickRate(world);
        if(tickRate <= 0) return; //无重力
        this.state = curState;
        int meta = state.getValue(LEVEL);
        int quanta = flowing.quantaPerBlock-meta;
        final @Nonnull BlockPos downPos = pos.up(flowing.densityDir);

        // *******************
        //  Vertical Flow
        // *******************

        final @Nonnull IBlockState stateBelow = world.getBlockState(downPos);
        switch (flowing.canFlowDownTo(world,downPos)){
            case EQUAL_FLUID:
                flowing.flowDown(world,pos,downPos,state,stateBelow,quanta);
                return;
            case SWAP:
                FluidOperationUtil.swapFluid(world,pos,downPos);
                return;
            case DISPLACE:
                FluidOperationUtil.moveFluid(world,pos,downPos);
                return;
            default:break;
        }

        // *******************
        //  Single Quanta Slope Flow
        // *******************

        if(quanta == 1){
            if(!slopeModeForModsWhenOnFluidsAndQuantaIs1.getValue() && flowing.isEqualFluid(stateBelow)){
                tryPressureFlow(world,rand);
                return;
            }
            slopeFlowableDirections.clear();
            flowing.singleSlopeAlgorithm(world,pos,slopeFlowableDirections);
            if(slopeFlowableDirections.isEmpty()){
                tryPressureFlow(world,rand);
            }else{
                int i = 0;
                for(@Nonnull final EnumFacing dir:slopeFlowableDirections){
                    slopeFlowDirectionsArr[i++] = dir;
                }
                final @Nonnull EnumFacing randomFacing = slopeFlowDirectionsArr[rand.nextInt(i)];
                world.setBlockToAir(pos);
                flowing.flowIntoBlockDirectly(world, pos.offset(randomFacing),state, meta);
            }
            return;
        }

        //可流动方向检查
        averageFlowChoices.clear();
        final @Nullable Set<EnumFacing> slopeModeFlowDirections = slopeModeForModsWhenOnFluidsAndQuantaAbove1.getValue()? slopeFlowableDirections:null;//非Q=1坡度模式可用方向
        flowing.gatherFlowChoices(world,pos,averageFlowChoices,slopeModeFlowDirections,quanta);

        /// To DO: Change To New Average Flow Algorithm
        /// @see top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil#averageFlow(int, int, long, int, List) 
        if(!averageFlowChoices.isEmpty()){
            // *******************
            //  Average Flow
            // *******************
            averageFlowChoices.sort(Comparator.comparingInt(OldFlowChoice::getQuantaOfThisFluid));
            averageFlowChoices.add(new OldFlowChoice(flowing.quantaPerBlock,null));
            int newQuanta = quanta;
            while(averageFlowChoices.get(0).getQuantaOfThisFluid()<newQuanta-1){ //向四周分配流量
                newQuanta--;
                averageFlowChoices.get(0).addQuanta(1);
                if(averageFlowChoices.get(0).getQuantaOfThisFluid() > averageFlowChoices.get(1).getQuantaOfThisFluid()){
                    Collections.swap(averageFlowChoices,0,1);
                }
            }
            quanta = newQuanta;
            meta = flowing.quantaPerBlock - quanta;
            if (quanta<=0) world.setBlockToAir(pos); //先更新自身状态
            else {
                state = state.withProperty(LEVEL,meta);
                world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                BlockUpdater.scheduleUpdate(world,pos, flowing.block, tickRate);
                world.notifyNeighborsOfStateChange(pos,flowing.block, false);
            }
            for(final @Nonnull OldFlowChoice choice:averageFlowChoices){ //向四周流动
                if(choice.getQuantaOfThisFluid() == 0) continue;
                if(choice.direction == null) continue;
                BlockPos facingPos = pos.offset(choice.direction);
                flowing.flowIntoBlockDirectly(world,facingPos,state,flowing.quantaPerBlock-choice.getQuantaOfThisFluid());
            }
        }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()) {
            // ********************
            //  Multi-Quanta Slope Flow
            // ********************
            bestFlowDirections.clear();
            flowing.multiSlopeAlgorithm(world,pos,slopeModeFlowDirections,quanta,bestFlowDirections);
            if (bestFlowDirections.isEmpty()) return;

            int i = 0;
            for(@Nonnull final EnumFacing dir:bestFlowDirections){
                bestFlowDirectionsArr[i++] = dir;
            }
            @Nonnull final EnumFacing flowDir = bestFlowDirectionsArr[rand.nextInt(i)];
            final int newLiquidQuanta = quanta - 1;
            final int newLiquidMeta = flowing.quantaPerBlock - newLiquidQuanta;
            //更新自己
            state = state.withProperty(LEVEL, newLiquidMeta);
            world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
            BlockUpdater.scheduleUpdate(world, pos, flowing.block, tickRate);
            world.notifyNeighborsOfStateChange(pos, flowing.block, false);
            //移动至新位置
            world.setBlockState(pos.offset(flowDir), state.withProperty(LEVEL, meta), Constants.BlockFlags.SEND_TO_CLIENTS);
        }else {
            // *******************
            //  Pressure Flow
            // *******************
            tryPressureFlow(world,rand);
        }
    }

    @Override
    public void onFailure(@Nonnull World world, @Nonnull IBlockState state, @Nonnull Random rand) {}

    @Nonnull
    @Override
    public final Block getBlock() {
        return flowing.block;
    }

    protected boolean tryPressureFlow(final @Nonnull World world, final @Nonnull Random rand){
        if(FluidPressureSearchManager.isTaskRunning(world,pos)){
            BlockUpdater.scheduleUpdate(world,pos,flowing.block,this.tickRate);
            return false;
        }
        final @Nullable IFluidPressureSearchTaskResult res = FluidPressureSearchManager.getTaskResult(world,pos);
        if(res == null || res.isEmpty()){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),false);
            return false;
        }
        IBlockState nowState = state;
        while (res.hasNext()){
            final BlockPos toPos = res.next();
            assert toPos != null;
            if(!flowing.isEqualFluid(nowState)) break;
            if(flowing.tryTeleport(world,toPos,pos,nowState)) break;
            nowState = world.getBlockState(pos);
        }
        if(nowState != state && flowing.isEqualFluid(nowState)){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),true);
            return true;
        }else if(nowState == state){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),false);
            return false;
        }
        return true;
    }

    protected void sendPressureQuery(final @Nonnull World world,
                                     final @Nonnull Random rand,
                                     final int range,
                                     final boolean directly){
        final @Nonnull IBlockState up = world.getBlockState(pos.down(flowing.densityDir));
        if(FluidUtil.getFluid(up)!=fluid && (directly || BaseUtil.getRandomResult(rand, FluidPhysicsConfig.POSSIBILITY_FOR_CLASSIC_FLUIDS_TO_CREATE_PRESSURE_TASK.getValue()))) {
            FluidPressureSearchManager.addTask(world, FinitePressureTasks.createModClassicTask(fluid,state,pos, range,flowing.quantaPerBlock));
        }
    }
}
