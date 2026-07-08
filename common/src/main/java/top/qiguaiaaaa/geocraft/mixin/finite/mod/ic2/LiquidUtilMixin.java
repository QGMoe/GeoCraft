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

package top.qiguaiaaaa.geocraft.mixin.finite.mod.ic2;

import ic2.core.util.LiquidUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

@Mixin(value = LiquidUtil.class,remap = false)
public class LiquidUtilMixin {
    @Inject(method = "drainBlock",at = @At("HEAD"),cancellable = true,remap = false)
    private static void drainBlock(World world, BlockPos pos, boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            cir.cancel();
            IFluidBlock liquid = (IFluidBlock)block;
            if (liquid.canDrain(world, pos)) {
                cir.setReturnValue(liquid.drain(world, pos, !simulate));
                return;
            }
        } else if (block instanceof BlockLiquid) {
            FluidStack fluid = null;
            if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
                    if(!GeoFluidSetting.isFluidToBePhysical(FluidRegistry.LAVA)) return;
                    fluid = new FluidStack(FluidRegistry.LAVA, FluidUtil.getFluidQuanta(world,pos,state)*FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME);
                }
            } else {
                if(!GeoFluidSetting.isFluidToBePhysical(FluidRegistry.WATER)) return;
                fluid = new FluidStack(FluidRegistry.WATER, FluidUtil.getFluidQuanta(world,pos,state)*FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME);
            }
            cir.cancel();
            if(fluid != null && !simulate){
                world.setBlockToAir(pos);
            }
            cir.setReturnValue(fluid);
            return;
        }
        cir.cancel();
        cir.setReturnValue(null);
    }
}
