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

package moe.qingu.geocraft.geography.fluidphysics.classic.update;

import moe.qingu.geocraft.api.util.FluidUtil;
import moe.qingu.geocraft.fluid.FluidSnow;
import moe.qingu.geocraft.geography.fluidphysics.updater.IFluidTask;
import moe.qingu.geocraft.mixin.common.block.BlockFluidBaseAccessor;
import moe.qingu.geocraft.mixin.common.block.BlockFluidClassicAccessor;
import moe.qingu.geocraft.util.MiscUtil;
import moe.qingu.geocraft.util.fluid.FluidOperationUtil;
import moe.qingu.geocraft.util.fluid.FluidSearchUtil;
import moe.qingu.geocraft.world.BlockUpdater;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.Random;

import static moe.qingu.geocraft.configs.FluidPhysicsConfig.*;
import static moe.qingu.geocraft.configs.FluidPhysicsConfig.disableInfiniteFluidForAllModFluid;
import static moe.qingu.geocraft.configs.FluidPhysicsConfig.findSourceMaxSameLevelIterationsWhenHorizontalFlowing;

/**
 * @author QiguaiAAAA
 */
@NotThreadSafe
public class ClassicFluidClassicUpdateTask extends BlockFluidClassic implements IFluidTask {
    private static final Fluid USELESS_FLUID = new FluidSnow();
    private boolean running = false;
    private Fluid curFluid;
    private BlockFluidClassic block;

    ClassicFluidClassicUpdateTask() {
        super(USELESS_FLUID, Material.WATER);
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {
        if(running) throw new IllegalStateException();
        try {
            running = true;
            this.block = (BlockFluidClassic) state.getBlock();
            final int modifiedTickRate = MiscUtil.modifyTickRateByGravity(world,block.tickRate(world));
            if(modifiedTickRate<=0) return; //无重力

            this.quantaPerBlock = ((BlockFluidBaseAccessor)block).天圆地方$getQuantaPerBlock();
            this.curFluid = block.getFluid();
            this.densityDir = curFluid.getDensity() > 0 ? -1 : 1;
            this.canCreateSources = ((BlockFluidClassicAccessor)block).天圆地方$canCreateSources();
            this.setDefaultState(this.block.getDefaultState());

            int quantaRemaining = quantaPerBlock - state.getValue(LEVEL);
            final int newQuanta;

            //是否能够往下流
            Optional<BlockPos> sourcePosOption = Optional.empty();
            if(quantaRemaining == quantaPerBlock) sourcePosOption = Optional.of(pos);
            final BlockPos downPos = pos.up(densityDir);
            boolean canMoveSourceDown = this.canMoveInto(world, downPos);
            if(canMoveSourceDown){
                if (!sourcePosOption.isPresent())
                    sourcePosOption = FluidSearchUtil.findSource(world,pos,curFluid,false,false,
                            findSourceMaxIterationsWhenVerticalFlowing.getValue(),
                            findSourceMaxSameLevelIterationsWhenVerticalFlowing.getValue());
                if(sourcePosOption.isPresent()){
                    FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),downPos);
                    if(sourcePosOption.get() == pos) return;
                }
            }else if(quantaRemaining == quantaPerBlock-1){
                sourcePosOption = FluidSearchUtil.findSource(world,pos,curFluid,true,false,
                        findSourceMaxIterationsWhenHorizontalFlowing.getValue(),
                        findSourceMaxSameLevelIterationsWhenHorizontalFlowing.getValue());
                if(sourcePosOption.isPresent()){
                    FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),pos);
                    BlockUpdater.scheduleUpdate(world,pos,this,modifiedTickRate);
                    return;
                }
            }

            if (quantaRemaining < quantaPerBlock) {
                int adjacentSourceBlocks = 0;

                if (ForgeEventFactory.canCreateFluidSource(world, pos, state, canCreateSources))
                    for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                        if (block.isSourceBlock(world, pos.offset(side))) adjacentSourceBlocks++;

                // 无限液体
                if (!disableInfiniteFluidForAllModFluid.getValue() && adjacentSourceBlocks >= 2 && (world.getBlockState(downPos).getMaterial().isSolid() || block.isSourceBlock(world, downPos))) {
                    newQuanta = quantaPerBlock;
                } else if (((BlockFluidBaseAccessor)block).天圆地方$hasVerticalFlow(world, pos) && !this.isSameFluidUnder(world,downPos)) {//垂直流入
                    newQuanta = quantaPerBlock - 1;
                } else { //水平流动
                    int maxQuanta = -100;
                    for (final EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                        maxQuanta = ((BlockFluidClassicAccessor)block).天圆地方$getLargerQuanta(world, pos.offset(side), maxQuanta);
                    }
                    newQuanta = maxQuanta - 1;
                }

                // 更新液体状态
                if (newQuanta != quantaRemaining) {
                    quantaRemaining = newQuanta;
                    if (newQuanta <= 0) {
                        world.setBlockToAir(pos);
                    } else {
                        world.setBlockState(pos, state.withProperty(LEVEL, quantaPerBlock - newQuanta), Constants.BlockFlags.SEND_TO_CLIENTS);
                        BlockUpdater.scheduleUpdate(world,pos,block,modifiedTickRate);
                        world.notifyNeighborsOfStateChange(pos, block, false);
                    }
                }
            }
            // 垂直流入
            if (block.canDisplace(world, downPos)) {
                this.flowIntoBlock(world, downPos, 1);
                return;
            }

            // 水平流动
            int flowMeta = quantaPerBlock - quantaRemaining + 1;
            if (flowMeta >= quantaPerBlock) return;

            if (FluidUtil.isFullFluid(world,downPos,world.getBlockState(downPos)) || !block.isFlowingVertically(world, pos)) {
                if (((BlockFluidBaseAccessor)block).天圆地方$hasVerticalFlow(world, pos)) flowMeta = 1;
                boolean[] flowTo = ((BlockFluidClassicAccessor)block).天圆地方$getOptimalFlowDirections(world, pos);
                for (int i = 0; i < 4; i++)
                    if (flowTo[i]) this.flowIntoBlock(world, pos.offset(SIDES.get(i)), flowMeta);
            }
        }finally {
            running = false;
        }

    }

    @Override
    public void onFailure(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final Random rand) {}

    @Override
    public boolean accepts(@Nonnull final World world, @Nonnull final IBlockState state) {
        return state.getBlock() instanceof BlockFluidClassic;
    }

    protected boolean canMoveInto(final @Nonnull World worldIn,final @Nonnull BlockPos pos){
        final @Nonnull IBlockState state = worldIn.getBlockState(pos);
        if(FluidUtil.isFluid(state)){
            if(FluidUtil.getFluid(state) != curFluid) return false;
            return state.getValue(LEVEL) != 0;
        }
        return block.canDisplace(worldIn,pos);
    }

    protected boolean isSameFluidUnder(final @Nonnull World worldIn,final @Nonnull BlockPos pos){
        final @Nullable Fluid underFluid = FluidUtil.getFluid(worldIn.getBlockState(pos));
        return curFluid == underFluid;
    }

    @Override
    @Deprecated
    public boolean canDisplace(final @Nonnull IBlockAccess world,final @Nonnull BlockPos pos) {
        return block.canDisplace(world, pos);
    }

    @Override
    @Deprecated
    public boolean displaceIfPossible(final @Nonnull World world,final @Nonnull BlockPos pos) {
        return block.displaceIfPossible(world, pos);
    }

    @Override
    @Deprecated
    public final Fluid getFluid() {
        return curFluid;
    }

    @Override
    @Deprecated
    public boolean isFlowingVertically(final @Nonnull IBlockAccess world,final @Nonnull BlockPos pos) {
        return block.isFlowingVertically(world, pos);
    }

    @Override
    @Deprecated
    public boolean isSourceBlock(final @Nonnull IBlockAccess world,final @Nonnull BlockPos pos) {
        return block.isSourceBlock(world,pos);
    }
}
