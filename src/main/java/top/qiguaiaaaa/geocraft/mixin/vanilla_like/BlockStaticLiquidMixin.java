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

package top.qiguaiaaaa.geocraft.mixin.vanilla_like;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.mixinapi.FluidSettable;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(value = BlockStaticLiquid.class)
public class BlockStaticLiquidMixin extends BlockLiquid implements FluidSettable {
    @Unique
    private Fluid 天圆地方$thisFluid;
    @Unique
    private boolean 天圆地方$curRandomTick = false;

    protected BlockStaticLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "<init>",at = @At("RETURN"))
    private void 天圆地方$onInit(Material materialIn, CallbackInfo ci) {
        this.setTickRandomly(true);
    }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        天圆地方$curRandomTick = true;
        super.randomTick(worldIn, pos, state, random);
        天圆地方$curRandomTick = false;
    }

    @Inject(method = "updateTick",at = @At("RETURN"))
    public void 天圆地方$updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        IBlockState newState = EventFactory.afterBlockLiquidStaticUpdate(天圆地方$thisFluid,worldIn,pos,state, 天圆地方$curRandomTick);
        if(newState != null){
            worldIn.setBlockState(pos,newState);
        }
    }

    /**
     * 保证流体流动受重力影响，且使用 BlockUpdater
     */
    @Redirect(method = "updateLiquid",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/world/World;scheduleUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void 天圆地方$scheduleLiquidUpdate(@Nonnull final World instance, final BlockPos pos, final Block blockIn, final int delay){
        MiscUtil.scheduleFluidBlockUpdate(instance, pos, blockIn, delay);
    }

    @Override
    @Unique
    public void 天圆地方$setCorrespondingFluid(Fluid fluid) {
        this.天圆地方$thisFluid = fluid;
    }
}
