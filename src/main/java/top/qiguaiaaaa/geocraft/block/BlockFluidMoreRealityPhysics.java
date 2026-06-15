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

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.math.OldFlowChoice;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 这是一个基于MORE REALITY模式流体的流体实现，你可以通过继承该类来实现自己的物理流体
 * 注意该类实现的是早期开发的MORE_REALITY流体实现,可能不适用于现在的流体
 */
@Deprecated
public class BlockFluidMoreRealityPhysics extends BlockFluidFinite {
    protected int slopeFindDistanceWhenQuantaAbove1 = (int) (quantaPerBlock*1.5/2);

    public BlockFluidMoreRealityPhysics(Fluid fluid, Material material, MapColor mapColor) {
        super(fluid, material, mapColor);
    }

    public BlockFluidMoreRealityPhysics(Fluid fluid, Material material) {
        super(fluid, material);
    }

    @Override
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        int meta = state.getValue(LEVEL);
        int quanta = meta+1;
        BlockPos downPos = pos.up(densityDir);

        IBlockState stateBelow = world.getBlockState(downPos);
        boolean canMoveDown = this.canMoveDownTo(world, downPos);
        if(canMoveDown){
            if(isSameLiquid(stateBelow)){
                flowDown(world,pos,state,stateBelow);
            }else if(FluidUtil.isFluid(stateBelow)){
                FluidOperationUtil.swapFluid(world,pos,pos.down());
            }else{
                FluidOperationUtil.moveFluid(world,pos,downPos);
            }
            return;
        }
        //坡度流动模式
        if(quanta == 1){
            Set<EnumFacing> directions = this.getPossibleFlowDirections(world, pos);
            if(directions.isEmpty()){
                return;
            }
            EnumFacing randomFacing = (EnumFacing) directions.toArray()[rand.nextInt(directions.size())];
            world.setBlockToAir(pos);
            this.flowIntoBlockDirectly(world, pos.offset(randomFacing), meta);
            return;
        }
        //可流动方向检查
        final ArrayList<OldFlowChoice> averageModeFlowDirections = new ArrayList<>();//平均流动模式可用方向
        Set<EnumFacing> slopeModeFlowDirections = EnumSet.noneOf(EnumFacing.class);//非Q=1坡度模式可用方向
        for(EnumFacing facing:EnumFacing.Plane.HORIZONTAL){
            IBlockState facingState = world.getBlockState(pos.offset(facing));
            if(!this.canDisplaceEvenIsFluid(world,pos.offset(facing))) continue;
            int facingQuanta = FluidUtil.getFluidQuanta(world,pos.offset(facing),facingState);
            if(facingQuanta<quanta-1){
                averageModeFlowDirections.add(new OldFlowChoice(facingQuanta,facing));
            }
            if(facingQuanta<quanta) slopeModeFlowDirections.add(facing);
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
            meta = quanta-1;
            if (quanta<=0) world.setBlockToAir(pos); //先更新自身状态
            else {
                state = state.withProperty(LEVEL,meta);
                world.setBlockState(pos, state, Constants.BlockFlags.SEND_TO_CLIENTS);
                world.scheduleUpdate(pos, this, tickRate(world));
                world.notifyNeighborsOfStateChange(pos,this, false);
            }
            for(OldFlowChoice choice:averageModeFlowDirections){ //向四周流动
                if(choice.getQuantaOfThisFluid() == 0) continue;
                if(choice.direction == null) continue;
                BlockPos facingPos = pos.offset(choice.direction);
                flowIntoBlockDirectly(world,facingPos,choice.getQuantaOfThisFluid()-1);
            }
        }else if(!slopeModeFlowDirections.isEmpty()){ //非Q=1坡度模式
            slopeModeFlowDirections = getPossibleFlowDirections(world,pos,slopeModeFlowDirections,quanta);
            if(slopeModeFlowDirections.isEmpty()){
                return;
            }
            EnumFacing randomFacing = (EnumFacing) slopeModeFlowDirections.toArray()[rand.nextInt(slopeModeFlowDirections.size())];
            int newLiquidQuanta = quanta-1;
            int newLiquidMeta = newLiquidQuanta-1;
            //更新自己
            state = state.withProperty(LEVEL,newLiquidMeta);
            world.setBlockState(pos,state,Constants.BlockFlags.SEND_TO_CLIENTS);
            world.scheduleUpdate(pos,this,tickRate(world));
            world.notifyNeighborsOfStateChange(pos,this,false);
            //移动至新位置
            world.setBlockState(pos.offset(randomFacing), state.withProperty(LEVEL, meta), Constants.BlockFlags.DEFAULT);
        }
    }

    protected void flowDown(World worldIn,BlockPos currentPos,IBlockState thisState,IBlockState downState){
        BlockPos downPos = currentPos.up(densityDir);
        int liquidQuanta = thisState.getValue(BlockLiquid.LEVEL)+1;
        int belowQuanta = downState.getValue(BlockLiquid.LEVEL)+1;
        int totalQuanta = liquidQuanta+belowQuanta;
        if(totalQuanta<=quantaPerBlock){
            worldIn.setBlockToAir(currentPos);
            worldIn.setBlockState(downPos, downState.withProperty(LEVEL, totalQuanta-1), Constants.BlockFlags.DEFAULT);
        }else{
            int remain = totalQuanta-quantaPerBlock;
            worldIn.setBlockState(currentPos,thisState.withProperty(LEVEL, remain-1),Constants.BlockFlags.DEFAULT);
            worldIn.scheduleUpdate(currentPos,this,tickRate(worldIn));
            worldIn.setBlockState(downPos,this.getDefaultState().withProperty(LEVEL,quantaPerBlock-1), Constants.BlockFlags.DEFAULT);
        }
    }

    protected void flowIntoBlockDirectly(World world, BlockPos pos, int meta) {
        if (meta < 0) return;
        FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,pos,world.getBlockState(pos),getFluid());
        world.setBlockState(pos, getDefaultState().withProperty(LEVEL, meta));
    }
    @Override
    public boolean displaceIfPossible(World world, BlockPos pos) {
        boolean canDisplace = canDisplace(world, pos);
        if (canDisplace) FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,pos,world.getBlockState(pos),getFluid());
        return canDisplace;
    }
    /**
     * Q=1 坡度流动模式的可流动方向寻找算法
     */
    protected Set<EnumFacing> getPossibleFlowDirections(World worldIn, BlockPos pos) {
        int difficulty = 1000;
        Set<EnumFacing> possibleDirections = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos facingPos = pos.offset(enumfacing);
            IBlockState state = worldIn.getBlockState(facingPos);
            if (!canDisplace(worldIn,facingPos) || FluidUtil.isFluid(state)) {
                continue;
            }
            int slope;
            if (!canMoveDownTo(worldIn,facingPos.up(densityDir))) {
                slope = this.getSlopeDistance(worldIn, facingPos, 1, enumfacing.getOpposite());
            } else{
                slope = 0;
            }

            if (slope < difficulty)
                possibleDirections.clear();
            if (slope <= difficulty) {
                possibleDirections.add(enumfacing);
                difficulty = slope;
            }
        }
        if(difficulty == 1000) possibleDirections.clear();
        return possibleDirections;
    }
    /**
     * Q=1 坡度流动模式的可流动方向寻找内层递归算法
     */
    protected int getSlopeDistance(World worldIn, BlockPos pos, int distance, EnumFacing from) {
        int difficulty = 1000;

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (enumfacing == from) continue;
            BlockPos facingPos = pos.offset(enumfacing);
            IBlockState state = worldIn.getBlockState(facingPos);

            if (!canDisplace(worldIn,facingPos) || FluidUtil.isFluid(state)) {
                continue;
            }
            if (canMoveDownTo(worldIn,facingPos.up(densityDir))) {
                return distance;
            }

            if (distance < quantaPerBlock / 2) {
                int newDistance = this.getSlopeDistance(worldIn, facingPos, distance + 1, enumfacing.getOpposite());
                if (newDistance < difficulty) difficulty = newDistance;
            }
        }

        return difficulty;
    }
    /**
     * Q>1 坡度流动模式的可流动方向寻找算法
     * @param worldIn 所在世界
     * @param pos 位置
     * @param accessibleDirections 可流动的方向
     * @param thisQuanta 搜寻者的液体量
     * @return 一个流动方向的集合，意味着最佳的流动方向
     */
    protected Set<EnumFacing> getPossibleFlowDirections(World worldIn, BlockPos pos,Set<EnumFacing> accessibleDirections,int thisQuanta) {
        double difficulty = 10000d;
        Set<EnumFacing> possibleDirections = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing enumfacing : accessibleDirections) {
            BlockPos facingPos = pos.offset(enumfacing);

            double slope = this.getSlopeDistance(worldIn, facingPos, 1,thisQuanta, enumfacing.getOpposite());

            if (slope < difficulty)
                possibleDirections.clear();
            if (slope <= difficulty) {
                possibleDirections.add(enumfacing);
                difficulty = slope;
            }
        }
        if(difficulty == 10000d) possibleDirections.clear();
        return possibleDirections;
    }

    /***
     * Q>1 坡度流动模式的可流动方向寻找内层递归算法
     * @param worldIn 所在世界
     * @param pos 位置
     * @param distance 当前距离原点的距离
     * @param thisQuanta 搜寻者的液体量
     * @param from 来源方向
     * @return 难易度，即坡度的余切值
     */
    protected double getSlopeDistance(World worldIn, BlockPos pos, int distance,int thisQuanta ,EnumFacing from) {
        double difficulty = 10000d;

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (enumfacing == from) continue;
            BlockPos facingPos = pos.offset(enumfacing);
            IBlockState state = worldIn.getBlockState(facingPos);
            int quantaDiffer = getQuantaDiffer(state,thisQuanta);
            boolean isFluid = FluidUtil.isFluid(state);
            boolean isAir = state.getMaterial() == Material.AIR;
            if (!this.canDisplaceEvenIsFluid(worldIn,facingPos) || (isFluid && quantaDiffer <1)) {
                continue;
            }
            if(isAir){
                if (canMoveDownTo(worldIn,facingPos.up(densityDir))) {
                    return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,quantaPerBlock+thisQuanta);
                }else{
                    return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta);
                }
            }else if(quantaDiffer >1){ //同样的流体
                return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta-quantaDiffer);
            }else if(!isFluid){ //例如火把
                return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta);
            }

            if (distance < slopeFindDistanceWhenQuantaAbove1) {
                double slope = this.getSlopeDistance(worldIn, facingPos, distance + 1,thisQuanta, enumfacing.getOpposite());
                if (slope < difficulty) difficulty = slope;
            }
        }
        return difficulty;
    }

    protected int getQuantaDiffer(IBlockState state,int thisQuanta){
        if(!isSameLiquid(state)) return Integer.MIN_VALUE;
        int quanta = state.getValue(BlockLiquid.LEVEL)+1;
        if(quanta<0) quanta = 0;
        return thisQuanta - quanta;
    }
    protected boolean canMoveDownTo(World worldIn, BlockPos pos){
        IBlockState state = worldIn.getBlockState(pos);
        if(FluidUtil.isFluid(state)){
            if(FluidUtil.getFluid(state) != getFluid()){
                return canDisplace(worldIn,pos);
            }
            return !FluidUtil.isFullFluid(worldIn,pos,state);
        }
        return canDisplace(worldIn,pos);
    }

    public boolean isSameLiquid(IBlockState state){
        return state.getBlock() == this;
    }

    protected boolean canDisplaceEvenIsFluid(World world,BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (isSameLiquid(state)) return true;
        return canDisplace(world, pos);
    }

}
