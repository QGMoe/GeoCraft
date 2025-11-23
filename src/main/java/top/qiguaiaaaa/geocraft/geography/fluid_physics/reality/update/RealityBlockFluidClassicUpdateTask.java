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

package top.qiguaiaaaa.geocraft.geography.fluid_physics.reality.update;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.math.OldFlowChoice;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.update.FluidUpdateBaseTask;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.reality.pressure.RealityPressureTaskBuilder;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.util.mixinapi.IMoreRealityBlockFluidBase;

import javax.annotation.Nonnull;
import java.util.*;

import static net.minecraftforge.fluids.BlockFluidBase.LEVEL;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.slopeModeForModsWhenOnFluidsAndQuantaAbove1;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.slopeModeForModsWhenOnFluidsAndQuantaIs1;

/**
 * @author QiguaiAAAA
 */
public class RealityBlockFluidClassicUpdateTask extends FluidUpdateBaseTask implements IMoreRealityBlockFluidBase<BlockFluidClassic> {
    protected final BlockFluidClassic block;
    protected final byte quantaPerBlock,tickRate,densityDir;
    protected IBlockState state;
    public RealityBlockFluidClassicUpdateTask(@Nonnull Fluid fluid, @Nonnull BlockPos pos, @Nonnull BlockFluidClassic block, int quantaPerBlock, int tickRate, int densityDir) {
        super(fluid, pos);
        this.block = block;
        this.quantaPerBlock = (byte) quantaPerBlock;
        this.tickRate = (byte) tickRate;
        this.densityDir = (byte) densityDir;
    }

    @Override
    public void onUpdate(@Nonnull World world, @Nonnull IBlockState curState, @Nonnull Random rand) {
        this.state = curState;
        int meta = state.getValue(LEVEL);
        int quanta = quantaPerBlock-meta;
        BlockPos downPos = pos.up(densityDir);

        IBlockState stateBelow = world.getBlockState(downPos);
        boolean canMoveDown = this.canMoveDownTo(world, downPos);
        if(canMoveDown){
            if(isSameLiquid(stateBelow)){
                flowDown(world,pos,downPos,state,stateBelow,quanta,quantaPerBlock);
            }else if(FluidUtil.isFluid(stateBelow)){//下面密度小，交换上下液体
                FluidOperationUtil.swapFluid(world,pos,downPos);
            }else{
                FluidOperationUtil.moveFluid(world,pos,downPos);
            }
            return;
        }
        //坡度流动模式
        if(quanta == 1){
            if(!slopeModeForModsWhenOnFluidsAndQuantaIs1.getValue() && isSameLiquid(stateBelow)){
                if(!managePressureTask(world,rand)) updateUp(world,rand);
                return;
            }
            Set<EnumFacing> directions = this.getPossibleFlowDirections(world, pos,densityDir,quantaPerBlock);
            if(directions.isEmpty()){
                if(!managePressureTask(world,rand)) updateUp(world,rand);
                return;
            }
            EnumFacing randomFacing = (EnumFacing) directions.toArray()[rand.nextInt(directions.size())];
            world.setBlockToAir(pos);
            this.flowIntoBlockDirectly(world, pos.offset(randomFacing),state, meta);
            return;
        }
        //可流动方向检查
        final ArrayList<OldFlowChoice> averageModeFlowDirections = new ArrayList<>();//平均流动模式可用方向
        Set<EnumFacing> slopeModeFlowDirections = slopeModeForModsWhenOnFluidsAndQuantaAbove1.getValue()?EnumSet.noneOf(EnumFacing.class):null;//非Q=1坡度模式可用方向
        for(EnumFacing facing:EnumFacing.Plane.HORIZONTAL){
            IBlockState facingState = world.getBlockState(pos.offset(facing));
            if(!this.canDisplaceEvenIsFluid(world,pos.offset(facing))) continue;
            int facingQuanta = FluidUtil.getFluidQuanta(world,pos.offset(facing),facingState);
            if(facingQuanta<quanta-1){
                averageModeFlowDirections.add(new OldFlowChoice(facingQuanta,facing));
            }
            if(slopeModeFlowDirections!=null && facingQuanta<quanta) slopeModeFlowDirections.add(facing);
        }
        if(!averageModeFlowDirections.isEmpty()){ //平均流动模式
            averageModeFlowDirections.sort(Comparator.comparingInt(OldFlowChoice::getQuantaOfThisFluid));
            averageModeFlowDirections.add(new OldFlowChoice(quantaPerBlock,null));
            int newQuanta = quanta;
            while(averageModeFlowDirections.get(0).getQuantaOfThisFluid()<newQuanta-1){ //向四周分配流量
                newQuanta--;
                averageModeFlowDirections.get(0).addQuanta(1);
                if(averageModeFlowDirections.get(0).getQuantaOfThisFluid() > averageModeFlowDirections.get(1).getQuantaOfThisFluid()){
                    Collections.swap(averageModeFlowDirections,0,1);
                }
            }
            quanta = newQuanta;
            meta = quantaPerBlock - quanta;
            if (quanta<=0) world.setBlockToAir(pos); //先更新自身状态
            else {
                state = state.withProperty(LEVEL,meta);
                world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                BlockUpdater.scheduleUpdate(world,pos, block, this.tickRate);
                world.notifyNeighborsOfStateChange(pos,block, false);
            }
            for(OldFlowChoice choice:averageModeFlowDirections){ //向四周流动
                if(choice.getQuantaOfThisFluid() == 0) continue;
                if(choice.direction == null) continue;
                BlockPos facingPos = pos.offset(choice.direction);
                flowIntoBlockDirectly(world,facingPos,state,quantaPerBlock-choice.getQuantaOfThisFluid());
            }
        }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()) { //非Q=1坡度模式
            slopeModeFlowDirections = getPossibleFlowDirections(world, pos, slopeModeFlowDirections, densityDir, quantaPerBlock, quanta);
            if (slopeModeFlowDirections.isEmpty()) {
                return;
            }
            EnumFacing randomFacing = (EnumFacing) slopeModeFlowDirections.toArray()[rand.nextInt(slopeModeFlowDirections.size())];
            int newLiquidQuanta = quanta - 1;
            int newLiquidMeta = quantaPerBlock - newLiquidQuanta;
            //更新自己
            state = state.withProperty(LEVEL, newLiquidMeta);
            world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
            BlockUpdater.scheduleUpdate(world, pos, block, this.tickRate);
            world.notifyNeighborsOfStateChange(pos, block, false);
            //移动至新位置
            world.setBlockState(pos.offset(randomFacing), state.withProperty(LEVEL, meta), Constants.BlockFlags.SEND_TO_CLIENTS);
        }else {
            if(!managePressureTask(world,rand)) updateUp(world,rand);
        }
    }

    @Override
    public void onFailure(@Nonnull World world, @Nonnull IBlockState state, @Nonnull Random rand) {
    }

    protected boolean managePressureTask(World world, Random rand){
        if(FluidPressureSearchManager.isTaskRunning(world,pos)){
            BlockUpdater.scheduleUpdate(world,pos,block,block.tickRate(world));
            return false;
        }
        IFluidPressureSearchTaskResult res =FluidPressureSearchManager.getTaskResult(world,pos);
        if(res == null || res.isEmpty()){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),false);
            return false;
        }
        IBlockState nowState =state;
        while (res.hasNext()){
            BlockPos toPos = res.next();
            if(FluidUtil.getFluid(nowState) != fluid) break;
            if(tryMoveInto(world,toPos,pos,nowState)) break;
            nowState = world.getBlockState(pos);
        }
        if(nowState != state && FluidUtil.getFluid(nowState) == fluid){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),true);
        }else if(nowState == state){
            sendPressureQuery(world,rand,BaseUtil.getRandomPressureSearchRange(),false);
            return false;
        }
        return true;
    }

    protected void updateUp(World world,Random random){
        if(random.nextInt(3)<2) return;
        BlockPos upPos = pos.down(densityDir);
        IBlockState up =world.getBlockState(upPos);
        if(up.getBlock() == block){
            BlockUpdater.scheduleUpdate(world,upPos,block,block.tickRate(world));
        }
    }

    protected void sendPressureQuery(World world,Random rand,int range,boolean directly){
        IBlockState up = world.getBlockState(pos.down(densityDir));
        if(FluidUtil.getFluid(up)!=fluid && (directly || BaseUtil.getRandomResult(rand, FluidPhysicsConfig.POSSIBILITY_FOR_CLASSIC_FLUIDS_TO_CREATE_PRESSURE_TASK.getValue()))) {
            FluidPressureSearchManager.addTask(world, RealityPressureTaskBuilder.createModClassicTask(fluid,state,pos, range,quantaPerBlock));
        }
    }

    protected boolean tryMoveInto(World world,BlockPos pos,BlockPos srcPos,IBlockState myState){
        if(!world.isBlockLoaded(pos)) return false;
        IBlockState state = world.getBlockState(pos);
        if(state.getMaterial() == Material.AIR){
            int quanta = quantaPerBlock -myState.getValue(BlockFluidClassic.LEVEL);
            int movQuanta = srcPos.getY()==pos.getY()?quanta/2:quanta;
            if(movQuanta <= 0)return false;
            quanta -=movQuanta;
            if(quanta <= 0){
                world.setBlockToAir(srcPos);
            }else world.setBlockState(srcPos,block.getDefaultState().withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-quanta));
            world.setBlockState(pos,block.getDefaultState().withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-movQuanta));
            return quanta == 0;
        }else if(FluidUtil.getFluid(state) == fluid){
            int quanta = quantaPerBlock-state.getValue(BlockFluidClassic.LEVEL);
            int myQuanta = quantaPerBlock -myState.getValue(BlockFluidClassic.LEVEL);
            if(pos.getY() == srcPos.getY() && quanta>=myQuanta-1) return false;
            int movQuanta = srcPos.getY()==pos.getY()?(myQuanta-quanta)/2:Math.min(quantaPerBlock-quanta,myQuanta);
            myQuanta -=movQuanta;
            if(myQuanta <= 0){
                world.setBlockToAir(srcPos);
            }else world.setBlockState(srcPos,block.getDefaultState().withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-myQuanta));
            quanta += movQuanta;
            world.setBlockState(pos,block.getDefaultState().withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-quanta));
            return myQuanta==0;
        }
        return false;
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockFluidClassic getThis() {
        return block;
    }

    @Override
    public int getTickRate() {
        return tickRate;
    }
}
