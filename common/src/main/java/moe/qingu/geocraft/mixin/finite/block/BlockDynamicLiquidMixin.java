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

package moe.qingu.geocraft.mixin.finite.block;

import moe.qingu.geocraft.api.fluidphysics.updater.task.FluidTaskCollector;
import moe.qingu.geocraft.api.fluidphysics.updater.task.IFluidTaskResponder;
import moe.qingu.geocraft.api.util.DeferredActions;
import moe.qingu.geocraft.geography.fluidphysics.updater.FluidTasks;
import moe.qingu.geocraft.api.fluidphysics.updater.task.IFluidTask;
import moe.qingu.geocraft.api.fluidphysics.updater.scheduler.FluidTaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moe.qingu.geocraft.api.setting.GeoFluidSetting;
import moe.qingu.geocraft.block.finite.ILayeredFluidHostFiniteLiquid;
import moe.qingu.geocraft.configs.FluidPhysicsConfig;
import moe.qingu.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import moe.qingu.geocraft.util.MiscUtil;

import javax.annotation.Nonnull;
import java.util.*;

@Mixin(value = BlockDynamicLiquid.class)
public class BlockDynamicLiquidMixin extends BlockLiquid implements ILayeredFluidHostFiniteLiquid, IFluidTaskResponder {
    @Unique private FiniteFlowingVanilla 天圆地方$FINITE$flowingHandler;
    @Unique private IFluidTask 天圆地方$FINITE$task;
    @Unique private boolean 天圆地方$FINITE$physical = true;

    protected BlockDynamicLiquidMixin(final @Nonnull Material materialIn) {
        super(materialIn);
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state,
                                @Nonnull final World worldIn,
                                @Nonnull final BlockPos pos,
                                @Nonnull final Block blockIn,
                                @Nonnull final BlockPos fromPos) {
        if(!FluidPhysicsConfig.ALLOW_DYNAMIC_LIQUID_NEIGHBOR_UPDATE.getValue()){
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
            return;
        }
        MiscUtil.scheduleFluidBlockUpdate(worldIn,pos, this, this.tickRate(worldIn));
    }

    @Inject(method = "<init>",at = @At("TAIL"))
    private void 天圆地方$FINITE$init(final @Nonnull Material material,final @Nonnull CallbackInfo ci) {
        DeferredActions.onInited(() -> {
            this.天圆地方$FINITE$flowingHandler = FiniteFlowingVanilla.getFlowingByMaterial(this.material);
            if(天圆地方$FINITE$flowingHandler.fluid == FluidRegistry.WATER) 天圆地方$FINITE$task = FluidTasks.WATER_TASK;
            else 天圆地方$FINITE$task = FluidTasks.LAVA_TASK;
        });
        DeferredActions.onServerAboutToStart(()-> this.天圆地方$FINITE$physical = GeoFluidSetting.isFluidToBePhysical(天圆地方$FINITE$flowingHandler.fluid));
    }

    @Inject(method = "updateTick",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(@Nonnull final World worldIn,
                                    @Nonnull final BlockPos pos,
                                    @Nonnull final IBlockState state,
                                    @Nonnull final Random rand,
                                    @Nonnull final CallbackInfo ci) {
        if(!天圆地方$FINITE$physical) return;
        ci.cancel();
        if (!worldIn.isBlockLoaded(pos)) return;
        FluidTaskScheduler.schedule(worldIn,pos, 天圆地方$FINITE$task,天圆地方$FINITE$flowingHandler.fluid);
    }

    @Inject(method = "onBlockAdded",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$onBlockAdded(@Nonnull final World worldIn,
                                      @Nonnull final BlockPos pos,
                                      @Nonnull final IBlockState state,
                                      @Nonnull final CallbackInfo ci) {
        ci.cancel();
        if (!this.checkForMixing(worldIn, pos, state)) MiscUtil.scheduleFluidBlockUpdate(worldIn,pos, this, this.tickRate(worldIn));
    }

    //*********
    // 载流方块
    //*********

    @Nonnull
    @Override
    @Unique
    public Fluid 天圆地方$getFluid() {
        return this.天圆地方$FINITE$flowingHandler.fluid;
    }

    //*********
    // Fluid Tasks Responder
    //*********

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean accepts(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final IFluidTask task) {
        return 天圆地方$FINITE$physical;
    }

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void onStaleTask(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nonnull final IFluidTask task,
                            @Nonnull final FluidTaskCollector collector) {
        if(天圆地方$FINITE$physical) collector.schedule(天圆地方$FINITE$task,天圆地方$FINITE$flowingHandler.fluid);
    }
}
