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

package top.qiguaiaaaa.geocraft.mixin.finite.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.block.finite.IBlockFluidClassicFinite;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidUpdateManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingClassic;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.update.FiniteFluidClassicUpdateTask;
import top.qiguaiaaaa.geocraft.util.MiscUtil;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author QiguaiAAAA
 */
@Mixin(value = BlockFluidClassic.class)
public abstract class BlockFluidClassicMixin extends BlockFluidBase implements IBlockFluidClassicFinite {

    @Unique
    private FiniteFlowingClassic 天圆地方$FINITE$flowing;

    public BlockFluidClassicMixin(Fluid fluid, Material material, MapColor mapColor) {
        super(fluid, material, mapColor);
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
        if(!GeoFluidSetting.isFluidToBePhysical(this.getFluid())) return;
        ci.cancel();
        if(world.isRemote) return;
        if(!GeoFluidSetting.hasGravity(world)){
            return;
        }
        FluidUpdateManager.addTask(world,new FiniteFluidClassicUpdateTask(pos, 天圆地方$FINITE$flowing));
    }

    @Inject(method = "drain",at = @At("HEAD"),cancellable = true,remap = false)
    private void 天圆地方$drain(final @Nonnull World world,
                                final @Nonnull BlockPos pos,
                                final boolean doDrain,
                                final @Nonnull CallbackInfoReturnable<FluidStack> cir) {
        if(!GeoFluidSetting.isFluidToBePhysical(this.getFluid())) return;
        final FluidStack fluidStack = new FluidStack(this.getFluid(), MathHelper.floor((this.getQuantaPercentage(world, pos) * Fluid.BUCKET_VOLUME)));

        if (doDrain) {
            world.setBlockToAir(pos);
        }
        cir.setReturnValue(fluidStack);
        cir.cancel();
    }

    /**
     * 参考自Forge的BlockFluidFinite类
     */
    @Inject(method = "place",at =@At("HEAD"),cancellable = true,remap = false)
    private void 天圆地方$place(final @Nonnull World world,
                                final @Nonnull BlockPos pos,
                                final @Nonnull FluidStack fluidStack,
                                final boolean doPlace,
                                final @Nonnull CallbackInfoReturnable<Integer> cir) {
        if(!GeoFluidSetting.isFluidToBePhysical(this.getFluid())) return;
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
        if(!GeoFluidSetting.isFluidToBePhysical(this.getFluid())) return;
        cir.setReturnValue(true);
        cir.cancel();
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
    public void 天圆地方$FINITE$init() {
        this.天圆地方$FINITE$flowing = new FiniteFlowingClassic((BlockFluidClassic) (Block)this);
    }

    @Nonnull
    @Override
    public FiniteFlowingClassic 天圆地方$FINITE$getFlowingHandler() {
        return this.天圆地方$FINITE$flowing;
    }
}
