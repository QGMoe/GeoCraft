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

package moe.qingu.geocraft.mixin.classic;

import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskCollector;
import moe.qingu.geocraft.api.util.DeferredActions;
import moe.qingu.geocraft.geography.fluidphysics.FluidTasks;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTaskResponder;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moe.qingu.geocraft.api.setting.GeoFluidSetting;
import moe.qingu.geocraft.api.util.FluidUtil;
import moe.qingu.geocraft.geography.fluidphysics.classic.mixin.IClassicBlock;
import moe.qingu.geocraft.mixin.common.block.BlockFluidBaseAccessor;
import moe.qingu.geocraft.util.MiscUtil;
import moe.qingu.geocraft.util.fluid.FluidOperationUtil;
import moe.qingu.geocraft.util.fluid.FluidSearchUtil;
import moe.qingu.geocraft.world.BlockUpdater;

import javax.annotation.Nonnull;
import java.util.*;

import static moe.qingu.geocraft.configs.FluidPhysicsConfig.*;

@Mixin(value = BlockFluidClassic.class)
public abstract class BlockFluidClassicMixin extends BlockFluidBase implements IClassicBlock, IFluidTaskResponder {
    @Final
    @Shadow(remap = false)
    protected static final List<EnumFacing> SIDES = Collections.unmodifiableList(Arrays.asList(
            EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH));
    @Shadow(remap = false) protected boolean canCreateSources;

    @Unique private Fluid 天圆地方$CLASSIC$fluid;
    @Unique private boolean 天圆地方$CLASSIC$physical = true;

    public BlockFluidClassicMixin(final Fluid fluid, final Material material) {
        super(fluid, material);
    }

    @Inject(method = "Lnet/minecraftforge/fluids/BlockFluidClassic;<init>(Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/block/material/Material;Lnet/minecraft/block/material/MapColor;)V",
            at = @At("TAIL"))
    private void 天圆地方$FINITE$init(final @Nonnull Fluid fluid, final @Nonnull Material material, final @Nonnull MapColor color, final @Nonnull CallbackInfo ci) {
        DeferredActions.onInited(() -> this.天圆地方$CLASSIC$fluid = this.getFluid());
        DeferredActions.onServerAboutToStart(() -> this.天圆地方$CLASSIC$physical = GeoFluidSetting.isFluidToBePhysical(this.天圆地方$CLASSIC$fluid));
    }

    /**
     * @author QiguaiAAAA
     * @reason Configure for Forge Fluids
     */
    @Inject(method = "updateTick",at= @At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(@Nonnull final World world,
                                    @Nonnull final BlockPos pos,
                                    @Nonnull final IBlockState state,
                                    @Nonnull final Random rand,
                                    @Nonnull final CallbackInfo ci) {
        if(!天圆地方$CLASSIC$physical) return;
        ci.cancel();
        if(world.isRemote) return;
        FluidTaskScheduler.schedule(world,pos, FluidTasks.CLASSIC_TASK,天圆地方$CLASSIC$fluid);
    }

    @Override
    @Unique
    public final void 天圆地方$onFlowingTask(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nonnull final Random rand) {
        final int modifiedTickRate = MiscUtil.modifyTickRateByGravity(world,tickRate);
        if(modifiedTickRate<=0) return; //无重力

        int quantaRemaining = quantaPerBlock - state.getValue(LEVEL);
        int newQuanta;


        //是否能够往下流
        Optional<BlockPos> sourcePosOption = Optional.empty();
        if(quantaRemaining == quantaPerBlock) sourcePosOption = Optional.of(pos);
        final BlockPos downPos = pos.up(densityDir);
        boolean canMoveSourceDown = this.天圆地方$canMoveInto(world, downPos);
        if(canMoveSourceDown){
            if (!sourcePosOption.isPresent())
                sourcePosOption = FluidSearchUtil.findSource(world,pos,天圆地方$CLASSIC$fluid,false,false,
                        findSourceMaxIterationsWhenVerticalFlowing.getValue(),
                        findSourceMaxSameLevelIterationsWhenVerticalFlowing.getValue());
            if(sourcePosOption.isPresent()){
                FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),downPos);
                if(sourcePosOption.get() == pos) return;
            }
        }else if(quantaRemaining == quantaPerBlock-1){
            sourcePosOption = FluidSearchUtil.findSource(world,pos,天圆地方$CLASSIC$fluid,true,false,
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
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                    if (isSourceBlock(world, pos.offset(side))) adjacentSourceBlocks++;

            // 无限液体
            if (!disableInfiniteFluidForAllModFluid.getValue() && adjacentSourceBlocks >= 2 && (world.getBlockState(downPos).getMaterial().isSolid() || isSourceBlock(world, downPos))) {
                newQuanta = quantaPerBlock;
            } else if (((BlockFluidBaseAccessor)this).天圆地方$hasVerticalFlow(world, pos) && !天圆地方$isSameFluidUnder(world,downPos)) {//垂直流入
                newQuanta = quantaPerBlock - 1;
            } else { //水平流动
                int maxQuanta = -100;
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                    maxQuanta = getLargerQuanta(world, pos.offset(side), maxQuanta);
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
                    BlockUpdater.scheduleUpdate(world,pos,this,modifiedTickRate);
                    world.notifyNeighborsOfStateChange(pos, this, false);
                }
            }
        }
        // 垂直流入
        if (this.canDisplace(world, downPos)) {
            flowIntoBlock(world, downPos, 1);
            return;
        }

        // 水平流动
        int flowMeta = quantaPerBlock - quantaRemaining + 1;
        if (flowMeta >= quantaPerBlock) return;

        if (FluidUtil.isFullFluid(world,downPos,world.getBlockState(downPos)) || !isFlowingVertically(world, pos)) {
            if (((BlockFluidBaseAccessor)this).天圆地方$hasVerticalFlow(world, pos)) flowMeta = 1;
            boolean[] flowTo = getOptimalFlowDirections(world, pos);
            for (int i = 0; i < 4; i++)
                if (flowTo[i]) flowIntoBlock(world, pos.offset(SIDES.get(i)), flowMeta);
        }
    }

    @Override
    public void onBlockAdded(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        MiscUtil.scheduleFluidBlockUpdate(world,pos,this,tickRate);
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state,
                                @Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final Block neighborBlock,
                                @Nonnull final BlockPos neighbourPos) {
        MiscUtil.scheduleFluidBlockUpdate(world,pos,this,tickRate);
    }

    @Unique
    private boolean 天圆地方$canMoveInto(World worldIn, BlockPos pos){
        IBlockState state = worldIn.getBlockState(pos);
        if(FluidUtil.isFluid(state)){
            if(FluidUtil.getFluid(state) != 天圆地方$CLASSIC$fluid) return false;
            return state.getValue(LEVEL) != 0;
        }
        return this.canDisplace(worldIn,pos);
    }

    @Unique
    private boolean 天圆地方$isSameFluidUnder(World worldIn, BlockPos pos){
        Fluid thisFluid = 天圆地方$CLASSIC$fluid;
        Fluid underFluid = FluidUtil.getFluid(worldIn.getBlockState(pos));
        return thisFluid == underFluid;
    }

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean accepts(@Nonnull final World world,@Nonnull final IBlockState state,@Nonnull final IFluidTask task) {
        return 天圆地方$CLASSIC$physical;
    }

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void onStaleTask(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nonnull final IFluidTask task,
                            @Nonnull final FluidTaskCollector collector) {
        if(天圆地方$CLASSIC$physical) collector.schedule(FluidTasks.CLASSIC_TASK,天圆地方$CLASSIC$fluid);
    }

    @Shadow(remap = false)
    protected void flowIntoBlock(World world, BlockPos pos, int meta) {}
    @Shadow(remap = false)
    public boolean isSourceBlock(IBlockAccess world, BlockPos pos){return false;}
    @Shadow(remap = false)
    protected int getLargerQuanta(IBlockAccess world, BlockPos pos, int compare){return 0;}
    @Shadow(remap = false)
    public boolean isFlowingVertically(IBlockAccess world, BlockPos pos){return false;}
    @Shadow(remap = false)
    protected boolean[] getOptimalFlowDirections(World world, BlockPos pos){return null;}
}
