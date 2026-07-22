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

package moe.qingu.geocraft.mixin.finite.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.blocks.BlockIEFluid;
import blusunrize.immersiveengineering.common.blocks.BlockIEFluidConcrete;
import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskCollector;
import moe.qingu.geocraft.api.util.DeferredActions;
import moe.qingu.geocraft.geography.fluidphysics.FluidTasks;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTaskResponder;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moe.qingu.geocraft.api.setting.GeoFluidSetting;
import moe.qingu.geocraft.geography.fluidphysics.finite.update.FiniteIEConcreteFluidTask;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(value = BlockIEFluidConcrete.class,remap = false)
public class BlockIEFluidConcreteMixin extends BlockIEFluid implements IFluidTaskResponder {

    @Unique private boolean 天圆地方$FINITE$沉浸工程$physical = true;

    public BlockIEFluidConcreteMixin(final @Nonnull String name,final @Nonnull Fluid fluid,final @Nonnull Material material) {
        super(name, fluid, material);
    }

    @Inject(method = "Lblusunrize/immersiveengineering/common/blocks/BlockIEFluidConcrete;<init>(Ljava/lang/String;Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/block/material/Material;)V",
            at = @At("TAIL"))
    private void 天圆地方$FINITE$init(final @Nonnull String name,final @Nonnull Fluid fluid,final @Nonnull Material material,final @Nonnull CallbackInfo ci) {
        DeferredActions.onServerAboutToStart(() -> 天圆地方$FINITE$沉浸工程$physical = GeoFluidSetting.isFluidToBePhysical(FiniteIEConcreteFluidTask.IE_CONCRETE_FLOWING_UPDATER.fluid));
    }

    @Inject(method = "updateTick",at = @At("HEAD"),cancellable = true,remap = true)
    public void updateTick(final World world,final BlockPos pos,final IBlockState state,final Random rand,final CallbackInfo ci) {
        if(!天圆地方$FINITE$沉浸工程$physical) return;
        ci.cancel();
        if(world.isRemote) return;
        FluidTaskScheduler.schedule(world,pos, FluidTasks.IE_CONCRETE_TASK, FiniteIEConcreteFluidTask.IE_CONCRETE_FLOWING_UPDATER.fluid);
    }

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean accepts(@Nonnull final World world,@Nonnull final IBlockState state,@Nonnull final IFluidTask task) {
        return 天圆地方$FINITE$沉浸工程$physical && task == FluidTasks.IE_CONCRETE_TASK;
    }

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void onStaleTask(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nonnull final IFluidTask task,
                            @Nonnull final FluidTaskCollector collector) {
        if(天圆地方$FINITE$沉浸工程$physical) collector.schedule(FluidTasks.IE_CONCRETE_TASK,FiniteIEConcreteFluidTask.IE_CONCRETE_FLOWING_UPDATER.fluid);
    }
}
