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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import moe.qingu.geocraft.api.setting.GeoFluidSetting;
import moe.qingu.geocraft.api.util.FluidUtil;
import moe.qingu.geocraft.util.MiscUtil;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author QiguaiAAAA
 */
@Mixin(value = BlockFluidClassic.class)
public abstract class BlockFluidClassicMixin extends BlockFluidBase implements IFluidTaskResponder {
    @Unique private Fluid 天圆地方$FINITE$fluid;
    @Unique private boolean 天圆地方$FINITE$physical = true;

    public BlockFluidClassicMixin(final @Nonnull Fluid fluid,final @Nonnull Material material,final @Nonnull MapColor mapColor) {
        super(fluid, material, mapColor);
    }

    @Inject(method = "Lnet/minecraftforge/fluids/BlockFluidClassic;<init>(Lnet/minecraftforge/fluids/Fluid;Lnet/minecraft/block/material/Material;Lnet/minecraft/block/material/MapColor;)V",
            at = @At("TAIL"))
    private void 天圆地方$FINITE$init(final @Nonnull Fluid fluid,final @Nonnull Material material,final @Nonnull MapColor color,final @Nonnull CallbackInfo ci) {
        DeferredActions.onInited(() -> this.天圆地方$FINITE$fluid = this.getFluid());
        DeferredActions.onServerAboutToStart(() -> 天圆地方$FINITE$physical = GeoFluidSetting.isFluidToBePhysical(this.天圆地方$FINITE$fluid));
    }

    /**
     * @reason 复写模组流体的更新
     */
    @Inject(method = "updateTick",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(final @Nonnull World world,
                                    final @Nonnull BlockPos pos,
                                    final @Nonnull IBlockState state,
                                    final @Nonnull Random rand,
                                    final @Nonnull CallbackInfo ci) {
        if(!天圆地方$FINITE$physical) return;
        ci.cancel();
        if(world.isRemote) return;
        FluidTaskScheduler.schedule(world,pos, FluidTasks.CLASSIC_TASK,天圆地方$FINITE$fluid);
    }

    @Inject(method = "drain",at = @At("HEAD"),cancellable = true,remap = false)
    private void 天圆地方$drain(final @Nonnull World world,
                                final @Nonnull BlockPos pos,
                                final boolean doDrain,
                                final @Nonnull CallbackInfoReturnable<FluidStack> cir) {
        if(!天圆地方$FINITE$physical) return;
        final FluidStack fluidStack = new FluidStack(this.getFluid(), MathHelper.floor((this.getQuantaPercentage(world, pos) * Fluid.BUCKET_VOLUME)));

        if (doDrain) {
            world.setBlockToAir(pos);
        }
        cir.setReturnValue(fluidStack);
    }

    /**
     * 参考自Forge的BlockFluidFinite类
     * @see BlockFluidFinite#place(World, BlockPos, FluidStack, boolean)
     */
    @Inject(method = "place",at =@At("HEAD"),cancellable = true,remap = false)
    private void 天圆地方$place(final @Nonnull World world,
                                final @Nonnull BlockPos pos,
                                final @Nonnull FluidStack fluidStack,
                                final boolean doPlace,
                                final @Nonnull CallbackInfoReturnable<Integer> cir) {
        if(!天圆地方$FINITE$physical) return;
        cir.cancel();
        IBlockState existing = world.getBlockState(pos);
        float amountPerQuanta = Fluid.BUCKET_VOLUME / this.quantaPerBlockFloat;

        int amountInFact = Fluid.BUCKET_VOLUME;
        int quantaExpected = this.quantaPerBlock;
        if (fluidStack.amount < amountInFact) {
            amountInFact = MathHelper.floor(amountPerQuanta * MathHelper.floor(fluidStack.amount / amountPerQuanta));
            quantaExpected = MathHelper.floor(amountInFact / amountPerQuanta);
        }
        if (existing.getBlock() == this) {
            int existingQuanta = FluidUtil.getFluidQuanta(world,pos,existing);
            int missingQuanta = this.quantaPerBlock - existingQuanta;
            amountInFact = Math.min(amountInFact, MathHelper.floor(missingQuanta * amountPerQuanta));
            quantaExpected = Math.min(quantaExpected + existingQuanta,this.quantaPerBlock);
        }

        if (quantaExpected < 1 || quantaExpected > 16)
            cir.setReturnValue(0);

        if (doPlace) {
            net.minecraftforge.fluids.FluidUtil.destroyBlockOnFluidPlacement(world, pos);
            world.setBlockState(pos,this.getDefaultState().withProperty(LEVEL, this.quantaPerBlock-quantaExpected), Constants.BlockFlags.SEND_TO_CLIENTS);
        }

        cir.setReturnValue(amountInFact);
    }

    @Inject(method = "canDrain",at = @At("HEAD"),cancellable = true,remap = false)
    private void 天圆地方$canDrain(final @Nonnull World world,
                                   final @Nonnull BlockPos pos,
                                   final @Nonnull CallbackInfoReturnable<Boolean> cir) {
        if(!天圆地方$FINITE$physical) return;
        cir.setReturnValue(true);
    }

    /**
     * @reason 变更RayTrace时检测液体的逻辑，允许选中LEVEL != 0的液体
     */
    @Redirect(method = "canCollideCheck",at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I",ordinal = 0))
    public int 天圆地方$redirectCollideCheck(final @Nonnull Integer instance){
        return 0;
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

    @Override
    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean accepts(@Nonnull final World world,@Nonnull final IBlockState state,@Nonnull final IFluidTask task) {
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
        if(天圆地方$FINITE$physical) collector.schedule(FluidTasks.CLASSIC_TASK,天圆地方$FINITE$fluid);
    }
}
