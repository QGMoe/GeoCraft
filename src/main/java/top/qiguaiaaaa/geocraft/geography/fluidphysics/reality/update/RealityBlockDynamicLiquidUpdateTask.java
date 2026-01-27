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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.update;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;
import top.qiguaiaaaa.geocraft.block.reality.ILayeredFluidHostFiniteLiquid;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.RealityBlockLiquidUpdater;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.pressure.RealityPressureTaskBuilder;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.update.FluidUpdateBaseTask;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.BlockLiquidUpdater;
import top.qiguaiaaaa.geocraft.handler.ServerStatusMonitor;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
public class RealityBlockDynamicLiquidUpdateTask extends FluidUpdateBaseTask {
    protected static final ThreadLocal<List<FlowChoice>> AVERAGE_MODE_FLOW_CHOICES = ThreadLocal.withInitial(ArrayList::new);
    protected final @Nonnull RealityBlockLiquidUpdater updater;
    protected final @Nonnull BlockDynamicLiquid block;
    protected IBlockState state;
    protected Material material;
    public RealityBlockDynamicLiquidUpdateTask(@Nonnull final Fluid fluid, @Nonnull final BlockPos pos, @Nonnull final RealityBlockLiquidUpdater updater) {
        super(fluid, pos);
        this.updater = updater;
        this.block = updater.getBlock();
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull final IBlockState curState, @Nonnull final Random rand) {
        if (!world.isAreaLoaded(pos,1)){
            return;
        }

        state = curState;
        material = state.getMaterial();
        int liquidMeta = state.getValue(LEVEL);
        final int updateFlag = ServerStatusMonitor.getRecommendedBlockFlags();
        if(liquidMeta >= 8){
            world.setBlockToAir(pos);
            return;
        }
        int liquidQuanta = 8-liquidMeta;
        int updateRate = MiscUtil.modifyTickRateByGravity(world,block.tickRate(world));

        if(updateRate <= 0){//无重力
            updater.placeStaticBlock(world,pos,curState);
            return;
        }

        final @Nonnull BlockPos downPos = pos.down();
        final @Nonnull IBlockState stateBelow = world.getBlockState(downPos);
        final boolean canMoveDown = updater.canMoveDownTo(world,downPos,stateBelow,liquidQuanta,state);

        if(canMoveDown){ //向下流动
            if(isSameLiquid(stateBelow)){
                flowDown(world,pos,stateBelow,liquidQuanta,updateRate);
            }else if(stateBelow.getBlock() == Blocks.WATER || stateBelow.getBlock() == Blocks.FLOWING_WATER){ // 岩浆碰到水,消耗岩浆
                liquidQuanta--;
                liquidMeta = 8-liquidQuanta;
                if (liquidQuanta<=0) world.setBlockState(pos,Blocks.AIR.getDefaultState(),updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,liquidMeta);
                    world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockUpdater.scheduleUpdate(world,pos,block, updateRate);
                    world.notifyNeighborsOfStateChange(pos,block, false);
                }
                world.setBlockState(downPos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, downPos, pos, Blocks.STONE.getDefaultState()));
                FluidOperationUtil.triggerFluidMixEffects(world,downPos);
            }else if(stateBelow.getBlock() instanceof ILayeredFluidHost){
                final ILayeredFluidHost host = (ILayeredFluidHost) stateBelow.getBlock();
                final long qbToFill = QBUtil.toQBFromQuanta(liquidQuanta);
                final long qbFilled = host.addAmountInQB(world,downPos,stateBelow,fluid,qbToFill,true);
                liquidQuanta = QBUtil.toQuanta(APIMathUtil.clamp(qbToFill-qbFilled,0,qbToFill));
                liquidMeta = 8 -liquidQuanta;
                if(liquidQuanta <=0) world.setBlockState(pos,Blocks.AIR.getDefaultState(),updateFlag); //先更新自身状态
                else {
                    state = state.withProperty(LEVEL,liquidMeta);
                    world.setBlockState(pos,state, Constants.BlockFlags.SEND_TO_CLIENTS);
                    BlockUpdater.scheduleUpdate(world,pos,block,updateRate);
                    world.notifyNeighborsOfStateChange(pos,block,false);
                }
            }else{
                FluidOperationUtil.moveFluid(world,pos,downPos);
            }
            return;
        }

        if(checkPressureTask(world)){ //压强流动模式
            BlockUpdater.scheduleUpdate(world,pos,block, updateRate);
            return;
        }

        if ((state.getMaterial() == Material.LAVA) && rand.nextInt(4) != 0){ //岩浆速度处理
            updateRate *= 4;
        }

        //Q=1 坡度流动模式
        if(liquidMeta == 7){
            if(!FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaIs1.getValue()){
                if(FluidUtil.getFluid(stateBelow) == fluid){
                    this.placeStaticBlock(world,pos,state,FlowingMode.SLOPE_MODE_ON_WATER);
                    return;
                }
            }
            if (!world.isAreaLoaded(pos, updater.getSlopeFindDistance(world))){
                return;
            }
            Set<EnumFacing> directions = updater.getPossibleFlowDirections(world, pos);
            if(directions.isEmpty()){
                this.placeStaticBlock(world,pos,state,FlowingMode.SLOPE_MODE);
                return;
            }

            EnumFacing randomFacing = (EnumFacing) directions.toArray()[rand.nextInt(directions.size())];
            world.setBlockState(pos,Blocks.AIR.getDefaultState(),updateFlag);
            updater.tryFlowInto(world, pos.offset(randomFacing), world.getBlockState(pos.offset(randomFacing)), 7);
            return;
        }

        //可流动方向检查
        final List<FlowChoice> averageModeFlowDirections = AVERAGE_MODE_FLOW_CHOICES.get();//平均流动模式可用方向
        averageModeFlowDirections.clear();
        Set<EnumFacing> slopeModeFlowDirections = FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()?
                EnumSet.noneOf(EnumFacing.class):null;//非Q=1坡度模式可用方向
        updater.checkNeighborsToFindFlowChoices(world,pos,state,liquidQuanta,averageModeFlowDirections,slopeModeFlowDirections);

        if(!averageModeFlowDirections.isEmpty()){ //平均流动模式
            int newLiquidQuanta = LayeredFluidHostUtil.averageFlow(liquidQuanta, ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA, QBUtil.QUANTA_VOLUME,0,averageModeFlowDirections);

            if(newLiquidQuanta == liquidQuanta){
                this.placeStaticBlock(world,pos,state,FlowingMode.AVERAGE_MODE);
                return;
            }

            long left = 0;
            for(@Nonnull FlowChoice choice:averageModeFlowDirections){ //向四周流动
                if(choice.getAddedLayers() == 0){
                    left += choice.getAddedAmountInQB();
                    continue;
                }
                BlockPos facingPos = pos.offset(choice.direction);
                if(choice.isAir()){
                    directlyFlowInto(world,facingPos,world.getBlockState(facingPos),8-choice.getNewLayers());
                }else{
                    left += choice.apply(world,facingPos,world.getBlockState(facingPos),fluid);
                }
            }
            newLiquidQuanta += QBUtil.toQuanta(left);

            liquidMeta = 8 - newLiquidQuanta;
            if (newLiquidQuanta<=0) world.setBlockState(pos,Blocks.AIR.getDefaultState(),updateFlag); //先更新自身状态
            else {
                state = state.withProperty(LEVEL,liquidMeta);
                world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                BlockUpdater.scheduleUpdate(world,pos,block, updateRate);
                if(FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue() && !FluidPressureSearchManager.isTaskRunning(world,pos)){
                    createFluidPressureSearchTask(world,pos,state,FlowingMode.AVERAGE_MODE);
                }
                world.notifyNeighborsOfStateChange(pos,block, false);
            }

            averageModeFlowDirections.clear();
        }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()) { //非Q=1坡度模式
            if(!world.isAreaLoaded(pos, updater.getSlopeFindDistance2(world))){
                this.placeStaticBlock(world,pos,state,FlowingMode.NO_MODE);
                return;
            }
            slopeModeFlowDirections = updater.getPossibleFlowDirections(world, pos, slopeModeFlowDirections, liquidQuanta);
            if (slopeModeFlowDirections.isEmpty()) {
                this.placeStaticBlock(world, pos, state,FlowingMode.SLOPE_MODE_ON_WATER_2);
                return;
            }
            final EnumFacing randomFacing = (EnumFacing) slopeModeFlowDirections.toArray()[rand.nextInt(slopeModeFlowDirections.size())];
            final int newLiquidQuanta = liquidQuanta - 1;
            final int newLiquidMeta = 8 - newLiquidQuanta;
            //更新自己
            state = state.withProperty(LEVEL, newLiquidMeta);
            world.setBlockState(pos, state, updateFlag);
            BlockUpdater.scheduleUpdate(world,pos, block, updateRate);
            world.notifyNeighborsOfStateChange(pos, block, false);
            //移动至新位置
            setLiquidToFlowingLevel(world, pos.offset(randomFacing), liquidMeta);
        }else {
            this.placeStaticBlock(world,pos,state,FlowingMode.NO_MODE);
        }
    }

    @Override
    public void onFailure(@Nonnull World world, @Nonnull IBlockState state, @Nonnull Random rand) {
        updater.placeStaticBlock(world,pos,state);
    }

    /**
     * 在下方有相同流体的情况下，流下去
     * @param world 所在世界
     * @param currentPos 当前位置
     * @param downState 下方方块状态
     * @param liquidQuanta 当前流体量
     * @param tickRate 更新间隔
     */
    protected void flowDown(World world,BlockPos currentPos,IBlockState downState,int liquidQuanta,int tickRate){
        BlockPos downPos = currentPos.down();
        int belowQuanta = FluidUtil.getFluidQuanta(world,downPos,downState);
        int totalQuanta = liquidQuanta+belowQuanta;
        if(totalQuanta<=8){
            world.setBlockToAir(currentPos);
            setLiquidToFlowingLevel(world,downPos,8-totalQuanta);
        }else{
            int remain = totalQuanta-8;
            setLiquidToFlowingLevel(world,currentPos,8-remain);
            BlockUpdater.scheduleUpdate(world,currentPos,block,tickRate);
            setLiquidToFlowingLevel(world,downPos,0);
        }
    }

    /**
     * 将指定位置的方块设置为指定等级的流动型流体
     * @param worldIn 所在世界
     * @param pos 位置
     * @param newLevel 新等级
     */
    protected void setLiquidToFlowingLevel(World worldIn,BlockPos pos,int newLevel){
        worldIn.setBlockState(pos, block.getDefaultState().withProperty(LEVEL,newLevel),Constants.BlockFlags.DEFAULT);
    }

    /**
     * 不检查是否能够流入，直接流入对应方块
     * @param worldIn 所在世界
     * @param pos 流入位置
     * @param state 当前方块状态
     * @param level 流入流体等级
     */
    protected void directlyFlowInto(World worldIn, BlockPos pos, IBlockState state, int level) {
        FluidOperationUtil.triggerDestroyBlockEffectByFluid(worldIn,pos,state,fluid);
        worldIn.setBlockState(pos, block.getDefaultState().withProperty(LEVEL, level), Constants.BlockFlags.DEFAULT);
    }

    /**
     * 是否是相同液体
     */
    protected boolean isSameLiquid(@Nonnull final IBlockState state){
        return state.getMaterial() == this.material && state.getBlock() instanceof BlockLiquid;
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return block;
    }

    protected void createFluidPressureSearchTask(World world,BlockPos pos,IBlockState state,FlowingMode mode){
        switch (mode){
            case AVERAGE_MODE:
                FluidPressureSearchManager.addTask(world, RealityPressureTaskBuilder.createVanillaTask(fluid,state,pos,0));
                break;
            case SLOPE_MODE:return;
            default:
                FluidPressureSearchManager.addTask(world,
                        RealityPressureTaskBuilder.createVanillaTask(fluid,state,pos,
                                FluidPhysicsConfig.PRESSURE_TASK_RANGE_DYNAMIC_FLUID_NO_AVERAGE.getValue()));
        }
    }

    protected boolean checkPressureTask(World worldIn){
        if(!FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue()) return false;
        if(!worldIn.isRemote){
            final IFluidPressureSearchTaskResult res = FluidPressureSearchManager.getTaskResult(worldIn,pos);
            if(res == null || res.isEmpty()){
                return false;
            }
            IBlockState nowState =state;
            while (res.hasNext()){
                BlockPos toPos = res.next();
                if(!nowState.getMaterial().isLiquid()) break;
                if(tryMoveInto(worldIn,toPos,pos,nowState)) break;
                nowState = worldIn.getBlockState(pos);
            }
            nowState = worldIn.getBlockState(pos);
            return nowState != state;
        }
        return false;
    }

    protected boolean tryMoveInto(World world,BlockPos toPos,BlockPos srcPos,IBlockState myState){
        if(!world.isBlockLoaded(toPos)) return false;
        IBlockState toState = world.getBlockState(toPos);
        if(FluidUtil.getFluid(toState) == fluid){
            int toQuanta = 8-toState.getValue(BlockLiquid.LEVEL);
            int myQuanta = 8 -myState.getValue(BlockLiquid.LEVEL);
            if(toPos.getY() == srcPos.getY() && toQuanta>=myQuanta-1) return false;
            int movQuanta = srcPos.getY()==toPos.getY()?(myQuanta-toQuanta)/2:Math.min(8-toQuanta,myQuanta);
            myQuanta -=movQuanta;
            if(myQuanta <= 0){
                world.setBlockToAir(srcPos);
            }else world.setBlockState(srcPos,block.getDefaultState().withProperty(BlockLiquid.LEVEL,8-myQuanta));
            toQuanta += movQuanta;
            world.setBlockState(toPos,block.getDefaultState().withProperty(BlockLiquid.LEVEL,8-toQuanta));
            return myQuanta==0;
        }
        if(!BlockLiquidUpdater.isBlocked(toState)){
            int quanta = 8 -myState.getValue(BlockLiquid.LEVEL);
            int movQuanta = srcPos.getY()==toPos.getY()?quanta/2:quanta;
            if(movQuanta <= 0)return false;
            quanta -=movQuanta;
            if(quanta <= 0){
                world.setBlockToAir(srcPos);
            }else world.setBlockState(srcPos,block.getDefaultState().withProperty(BlockLiquid.LEVEL,8-quanta));
            FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,toPos,toState,fluid);
            world.setBlockState(toPos,block.getDefaultState().withProperty(BlockLiquid.LEVEL,8-movQuanta));
            return quanta == 0;
        }
        return false;
    }

    protected void placeStaticBlock(World worldIn, BlockPos pos, IBlockState currentState,FlowingMode mode){
        updater.placeStaticBlock(worldIn,pos,currentState);
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

    protected enum FlowingMode{
        NO_MODE,
        SLOPE_MODE,
        SLOPE_MODE_ON_WATER,
        SLOPE_MODE_ON_WATER_2,
        AVERAGE_MODE
    }
}
