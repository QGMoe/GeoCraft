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

package top.qiguaiaaaa.geocraft.mixin.finite;

import net.minecraft.block.BlockLiquid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.wrappers.PhysicsBlockLiquidWrapper;
import top.qiguaiaaaa.geocraft.util.wrappers.PhysicsFluidBlockWrapper;

import javax.annotation.Nonnull;

@Mixin(value = FluidUtil.class,remap = false)
public class ForgeFluidUtilMixin {
    @Redirect(method = "getFluidBlockHandler",
            at= @At(value = "NEW",
                    target = "net/minecraftforge/fluids/capability/wrappers/FluidBlockWrapper"),remap = false)
    private static FluidBlockWrapper getFluidBlockHandlerMod(final @Nonnull IFluidBlock fluidBlock,final @Nonnull World world,final @Nonnull BlockPos blockPos) {
        if(GeoFluidSetting.isFluidToUseVanillaBucketMode(fluidBlock.getFluid())
        || !GeoFluidSetting.isFluidToBePhysical(fluidBlock.getFluid())) return new FluidBlockWrapper(fluidBlock,world,blockPos);
        return new PhysicsFluidBlockWrapper(fluidBlock, world, blockPos);
    }
    @Redirect(method = "getFluidBlockHandler",
            at=@At(value = "NEW",
                    target = "net/minecraftforge/fluids/capability/wrappers/BlockLiquidWrapper"),remap = false)
    private static BlockLiquidWrapper getFluidBlockHandlerVanilla(final @Nonnull BlockLiquid blockLiquid, final @Nonnull World world,final @Nonnull BlockPos blockPos) {
        if(!GeoFluidSetting.isFluidToBePhysical(blockLiquid)) return new BlockLiquidWrapper(blockLiquid,world,blockPos);
        return new PhysicsBlockLiquidWrapper(FiniteFlowingVanilla.getFlowingByMaterial(blockLiquid.getDefaultState().getMaterial()),world,blockPos);
    }
}
