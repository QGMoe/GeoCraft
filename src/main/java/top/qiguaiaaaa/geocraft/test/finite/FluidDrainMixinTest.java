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

package top.qiguaiaaaa.geocraft.test.finite;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.test.GeoTestItem;
import top.qiguaiaaaa.geocraft.util.wrappers.FiniteBlockLiquidWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.nickel.text.Texts.*;

/**
 * @see top.qiguaiaaaa.geocraft.mixin.finite.ForgeFluidUtilMixin
 * @author QGMoe
 */
public final class FluidDrainMixinTest extends GeoTestItem {

    public FluidDrainMixinTest(){
        this.id = new ResourceLocation(GeoCraft.MODID,"finite_fluid_drain_mixin_test");
    }

    @Nonnull
    @Override
    public EnumActionResult test(@Nonnull final World world, @Nonnull final BlockPos pos, @Nullable final ICommandSender sender) {
        final @Nullable EnumActionResult validation = validate(world, pos, sender);
        if(validation !=null) return validation;

        final EnumActionResult result;
        final IFluidHandler handler = FluidUtil.getFluidHandler(world,pos,null);
        result = handler instanceof FiniteBlockLiquidWrapper?EnumActionResult.SUCCESS:EnumActionResult.FAIL;
        if(sender != null){
            sender.sendMessage(translation("geocraft.geotest.finite_fluid_drain_mixin_test.handler").arg(pos,handler == null?"NULL":handler.getClass().getName()).done());
            if(result == EnumActionResult.FAIL) sender.sendMessage(translation("geocraft.geotest.finite_fluid_drain_mixin_test.failed")
                    .arg(FiniteBlockLiquidWrapper.class.getName())
                    .color(TextFormatting.RED)
                    .done());
        }
        return result;
    }

    @Nullable
    private static EnumActionResult validate(final @Nonnull World world,final @Nonnull BlockPos pos,final @Nullable ICommandSender sender){
        if(FluidPhysicsMode.getCurrentMode() != FluidPhysicsMode.MORE_REALITY){
            if(sender != null) sender.sendMessage(translation("geocraft.geotest.finite_fluid_drain_mixin_test.pass_because_mode").arg(FluidPhysicsMode.getCurrentMode())
                    .color(TextFormatting.RED).done());
            return EnumActionResult.PASS;
        }
        final IBlockState state = world.getBlockState(pos);
        final Fluid fluid = top.qiguaiaaaa.geocraft.api.util.FluidUtil.getFluid(state);
        if(fluid == null || !(state.getBlock() instanceof BlockLiquid) || (fluid != FluidRegistry.WATER && fluid != FluidRegistry.LAVA)){
            if(sender != null) sender.sendMessage(translation("geocraft.geotest.finite_fluid_drain_mixin_test.pass").arg(pos,state).color(TextFormatting.RED).done());
            return EnumActionResult.PASS;
        }
        if(GeoFluidSetting.isFluidToUseVanillaBucketMode(fluid) || !GeoFluidSetting.isFluidToBePhysical(fluid)){
            if(sender != null) sender.sendMessage(translation("geocraft.geotest.finite_fluid_drain_mixin_test.pass_because_config").arg(pos,fluid.getName())
                    .color(TextFormatting.RED).done());
            return EnumActionResult.PASS;
        }
        return null;
    }
}
