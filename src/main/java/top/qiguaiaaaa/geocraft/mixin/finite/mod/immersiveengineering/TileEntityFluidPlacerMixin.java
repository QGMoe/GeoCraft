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

package top.qiguaiaaaa.geocraft.mixin.finite.mod.immersiveengineering;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPlacer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.wrappers.PhysicsBlockLiquidWrapper;
import top.qiguaiaaaa.geocraft.util.wrappers.PhysicsFluidBlockWrapper;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
@Mixin(value = TileEntityFluidPlacer.class,remap = false)
public class TileEntityFluidPlacerMixin {

    @Inject(method = "tryPlaceFluid",at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/Fluid;getEmptySound(Lnet/minecraftforge/fluids/FluidStack;)Lnet/minecraft/util/SoundEvent;"),
            cancellable = true)
    private static void 天圆地方$tryPlaceFluid(final @Nonnull EntityPlayer player,
                                               final @Nonnull World worldIn,
                                               final @Nonnull FluidStack fluidStack,
                                               final @Nonnull BlockPos pos,
                                               final @Nonnull CallbackInfoReturnable<Boolean> cir) {
        final @Nonnull Fluid fluid = fluidStack.getFluid();
        if(fluid.getBlock() instanceof BlockLiquid){
            final BlockLiquid block = (BlockLiquid) fluid.getBlock();
            final FiniteFlowingVanilla flowing = FiniteFlowingVanilla.getFlowingByMaterial(block.getDefaultState().getMaterial());
            final PhysicsBlockLiquidWrapper wrapper = new PhysicsBlockLiquidWrapper(flowing,worldIn,pos);
            cir.setReturnValue(天圆地方$tryPlaceFluidByWrapper(player,worldIn,new FluidStack(fluid,Fluid.BUCKET_VOLUME),pos,wrapper));
        }else if(fluid.getBlock() instanceof BlockFluidClassic){
            final BlockFluidClassic block = (BlockFluidClassic) fluid.getBlock();
            final PhysicsFluidBlockWrapper wrapper = new PhysicsFluidBlockWrapper(block,worldIn,pos);
            cir.setReturnValue(天圆地方$tryPlaceFluidByWrapper(player,worldIn,new FluidStack(fluid,Fluid.BUCKET_VOLUME),pos,wrapper));
        }
    }

    @Unique
    @Nonnull
    private static Boolean 天圆地方$tryPlaceFluidByWrapper(final @Nonnull EntityPlayer player,
                                                           final @Nonnull World world,
                                                           final @Nonnull FluidStack stack,
                                                           final @Nonnull BlockPos pos,
                                                           final @Nonnull IFluidHandler wrapper){
        final int simulateAmount = wrapper.fill(stack,false);
        if(simulateAmount < Fluid.BUCKET_VOLUME){
            return Boolean.FALSE;
        }
        final @Nonnull SoundEvent soundevent = stack.getFluid().getEmptySound(stack);
        world.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0f, 1.0f);
        wrapper.fill(stack,true);
        return Boolean.TRUE;
    }
}
