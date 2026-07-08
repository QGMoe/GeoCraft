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

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidSearchUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

/**
 * 一个基于VANILLA_LIKE模式的流体物理方块实现,尚未经过测试
 */
public class BlockFluidVanillaLikePhysics extends BlockFluidClassic {
    protected int findSourceMaxIterationsWhenVerticalFlowing =255;
    protected int findSourceMaxSameLevelIterationsWhenVerticalFlowing = 0;
    protected int findSourceMaxIterationsWhenHorizontalFlowing = 17;
    protected int findSourceMaxSameLevelIterationsWhenHorizontalFlowing = 16;
    public BlockFluidVanillaLikePhysics(Fluid fluid, Material material, MapColor mapColor) {
        super(fluid, material, mapColor);
    }

    public BlockFluidVanillaLikePhysics(Fluid fluid, Material material) {
        super(fluid, material);
    }
    @Override
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        int quantaRemaining = quantaPerBlock - state.getValue(LEVEL);
        int expQuanta;

        //是否能够往下流
        Optional<BlockPos> sourcePosOption = Optional.empty();
        if(quantaRemaining == quantaPerBlock) sourcePosOption = Optional.of(pos);
        boolean canMoveSourceDown = this.canMoveInto(world, pos.up(densityDir));
        if(canMoveSourceDown){
            if (!sourcePosOption.isPresent())
                sourcePosOption = FluidSearchUtil.findSource(world,pos,this.getFluid(),false,false,
                        findSourceMaxIterationsWhenVerticalFlowing,
                        findSourceMaxSameLevelIterationsWhenVerticalFlowing);
            if(sourcePosOption.isPresent()){
                FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),pos.up(densityDir));
                if(sourcePosOption.get() == pos) return;
            }
        }else if(quantaRemaining == quantaPerBlock-1){
            sourcePosOption = FluidSearchUtil.findSource(world,pos,this.getFluid(),true,false,
                    findSourceMaxIterationsWhenHorizontalFlowing,
                    findSourceMaxSameLevelIterationsWhenHorizontalFlowing);
            if(sourcePosOption.isPresent()){
                FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),pos);
                world.scheduleUpdate(pos, this,this.tickRate);
                return;
            }
        }

        if (quantaRemaining < quantaPerBlock) {
            int adjacentSourceBlocks = 0;

            if (ForgeEventFactory.canCreateFluidSource(world, pos, state, canCreateSources))
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                    if (isSourceBlock(world, pos.offset(side))) adjacentSourceBlocks++;

            // 无限液体
            if (adjacentSourceBlocks >= 2 && (world.getBlockState(pos.up(densityDir)).getMaterial().isSolid() || isSourceBlock(world, pos.up(densityDir)))) {
                expQuanta = quantaPerBlock;
            } else if (hasVerticalFlow(world, pos) && !isSameFluidUnder(world,pos.up(densityDir))) {//垂直流入
                expQuanta = quantaPerBlock - 1;
            } else { //水平流动
                int maxQuanta = -100;
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                    maxQuanta = getLargerQuanta(world, pos.offset(side), maxQuanta);
                }
                expQuanta = maxQuanta - 1;
            }

            // 更新液体状态
            if (expQuanta != quantaRemaining) {
                quantaRemaining = expQuanta;
                if (expQuanta <= 0) {
                    world.setBlockToAir(pos);
                } else {
                    world.setBlockState(pos, state.withProperty(LEVEL, quantaPerBlock - expQuanta), Constants.BlockFlags.SEND_TO_CLIENTS);
                    world.scheduleUpdate(pos, this, this.tickRate);
                    world.notifyNeighborsOfStateChange(pos, this, false);
                }
            }
        }
            // 垂直流入
        if (canDisplace(world, pos.up(densityDir))) {
            flowIntoBlock(world, pos.up(densityDir), 1);
            return;
        }

        // 水平流动
        int flowMeta = quantaPerBlock - quantaRemaining + 1;
        if (flowMeta >= quantaPerBlock) return;

        if (FluidUtil.isFullFluid(world,pos.up(densityDir),world.getBlockState(pos.up(densityDir))) || !isFlowingVertically(world, pos)) {
            if (hasVerticalFlow(world, pos)) flowMeta = 1;
            boolean[] flowTo = getOptimalFlowDirections(world, pos);
            for (int i = 0; i < 4; i++)
                if (flowTo[i]) flowIntoBlock(world, pos.offset(SIDES.get(i)), flowMeta);
        }
    }

    private boolean canMoveInto(World worldIn, BlockPos pos){
        IBlockState state = worldIn.getBlockState(pos);
        if(FluidUtil.isFluid(state)){
            if(FluidUtil.getFluid(state) != this.getFluid()) return false;
            return state.getValue(LEVEL) != 0;
        }
        return this.canDisplace(worldIn,pos);
    }

    private boolean isSameFluidUnder(World worldIn,BlockPos pos){
        Fluid thisFluid = this.getFluid();
        Fluid underFluid = FluidUtil.getFluid(worldIn.getBlockState(pos));
        return thisFluid == underFluid;
    }
    public boolean hasVerticalFlow(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos.down(densityDir)).getBlock() == this;
    }

    public void setFindSourceMaxIterationsWhenHorizontalFlowing(int findSourceMaxIterationsWhenHorizontalFlowing) {
        if(findSourceMaxIterationsWhenHorizontalFlowing<0) return;
        this.findSourceMaxIterationsWhenHorizontalFlowing = findSourceMaxIterationsWhenHorizontalFlowing;
    }

    public void setFindSourceMaxIterationsWhenVerticalFlowing(int findSourceMaxIterationsWhenVerticalFlowing) {
        if(findSourceMaxIterationsWhenVerticalFlowing<0) return;
        this.findSourceMaxIterationsWhenVerticalFlowing = findSourceMaxIterationsWhenVerticalFlowing;
    }

    public void setFindSourceMaxSameLevelIterationsWhenHorizontalFlowing(int findSourceMaxSameLevelIterationsWhenHorizontalFlowing) {
        if(findSourceMaxSameLevelIterationsWhenHorizontalFlowing<0) return;
        this.findSourceMaxSameLevelIterationsWhenHorizontalFlowing = findSourceMaxSameLevelIterationsWhenHorizontalFlowing;
    }

    public void setFindSourceMaxSameLevelIterationsWhenVerticalFlowing(int findSourceMaxSameLevelIterationsWhenVerticalFlowing) {
        if(findSourceMaxSameLevelIterationsWhenVerticalFlowing<0) return;
        this.findSourceMaxSameLevelIterationsWhenVerticalFlowing = findSourceMaxSameLevelIterationsWhenVerticalFlowing;
    }
}
