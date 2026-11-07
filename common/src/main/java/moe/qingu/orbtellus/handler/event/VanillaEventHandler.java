/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.handler.event;

import moe.qingu.orbtellus.api.fluid.unit.MillibucketUnit;
import moe.qingu.orbtellus.api.fluid.unit.QuantaUnit;
import moe.qingu.orbtellus.api.laminarifer.Laminarifers;
import moe.qingu.orbtellus.api.laminarifer.flow.source.FlowSources;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import moe.qingu.orbtellus.api.atmosphere.Atmosphere;
import moe.qingu.orbtellus.api.laminarifer.IBlockStateLaminarifer;
import moe.qingu.orbtellus.api.event.atmosphere.AtmosphereUpdateEvent;
import moe.qingu.orbtellus.api.event.block.StaticLiquidUpdateEvent;
import moe.qingu.orbtellus.api.util.FluidUtil;
import moe.qingu.orbtellus.geography.fluidphysics.vanilla.FluidPhysicsCoreVanilla;
import moe.qingu.orbtellus.mixin.common.entity.EntityFallingBlockAccessor;
import moe.qingu.orbtellus.util.WaterUtil;
import moe.qingu.orbtellus.util.fluid.FluidMixinUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VanillaEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void afterStaticWaterUpdate(StaticLiquidUpdateEvent.After event){
        if(event.getLiquid() != FluidRegistry.WATER) return;
        if(!event.isRandomTick()) return;
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState newState = FluidPhysicsCoreVanilla.evaporateWater(worldIn,pos,event.getState(), worldIn.rand);
        if(newState != event.getState()){
            event.setNewState(newState);
            event.setResult(Event.Result.ALLOW);
        }
    }
    @SubscribeEvent
    public void onAtmosphereRainAndSnow(AtmosphereUpdateEvent.RainAndSnow event){
        Atmosphere atmosphere = event.getAtmosphere();
        World world = event.getWorld();
        BlockPos randPos = event.getRandPos();
        if (WaterUtil.canSnowAt(world,randPos, true)) {
            atmosphere.drainWater(MillibucketUnit.QUANTA_VOLUME_INT,randPos,true);
            event.setResult(Event.Result.ALLOW);
            event.setSnowy(true);
            event.setState(Blocks.SNOW_LAYER.getDefaultState());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onEntityPlacedBlock(final BlockEvent.EntityPlaceEvent event){
        if(event instanceof BlockEvent.PlaceEvent){
            implace(event.getWorld(), event.getPos(),event.getBlockSnapshot().getReplacedBlock(),event.getBlockSnapshot().getCurrentBlock(), FiniteEventHandler.PlaceSource.PLAYER,event.getEntity());
            return;
        }
        FiniteEventHandler.PlaceSource source = FiniteEventHandler.PlaceSource.OTHERS;
        Entity entity = event.getEntity();
        if(entity instanceof EntityFallingBlock) source = FiniteEventHandler.PlaceSource.FALLING_BLOCK;
        else if(entity instanceof EntityEnderman) source = FiniteEventHandler.PlaceSource.ENDER_MAN;
        implace(event.getWorld(),event.getPos(),event.getBlockSnapshot().getCurrentBlock(),event.getBlockSnapshot().getReplacedBlock(),source,entity);
    }

    /**
     * 纳流（无排流）
     * @param currentState 旧方块
     * @param replacedState 新方块
     * @param source 放置来源
     * @param sourceEntity 放置来源实体
     */
    @SuppressWarnings("DefaultNotLastCaseInSwitch")
    public static void implace(@Nonnull final World world,
                               @Nonnull final BlockPos pos,
                               @Nonnull final IBlockState currentState,
                               @Nonnull final IBlockState replacedState,
                               @Nonnull final FiniteEventHandler.PlaceSource source,
                               @Nullable final Entity sourceEntity){
        final Fluid fluid = FluidUtil.getFluid(currentState);
        if(fluid == null) return;
        final Block block = currentState.getBlock();
        final long amount;
        if(block instanceof BlockLiquid){
            final int quanta = FluidUtil.getFluidQuanta(world, pos,currentState);
            if(quanta != 8) return; //非源块
            amount = QuantaUnit.toQB(quanta);
        }else if(block instanceof BlockFluidBase){
            if(block instanceof BlockFluidClassic && currentState.getValue(BlockFluidBase.LEVEL) != 0) return; //非源块
            amount = FluidMixinUtil.getQBForBlockFluidBase(currentState);
        }else return;
        final Block placeBlock = replacedState.getBlock();
        final IBlockStateLaminarifer laminarifer; //保险起见，只适配 BlockState 版本的载流方块

        switch (source){
            case OTHERS:
            case ENDER_MAN:
            default: return;
            case FALLING_BLOCK:
            case PLAYER:

        }

        if(placeBlock instanceof IBlockStateLaminarifer){
            laminarifer = (IBlockStateLaminarifer) placeBlock;
        }else return;

        final long currentAmount = Laminarifers.getAmountInQB(laminarifer, replacedState, fluid);

        switch (source) {
            case PLAYER:
                Laminarifers.addAmountInQB(laminarifer, world, pos, replacedState,
                        fluid, null, amount, true, 0L, FlowSources.PLAYER,
                        BlockFlagModifier.build(Constants.BlockFlags.NO_OBSERVERS, Constants.BlockFlags.NOTIFY_NEIGHBORS));
                break;
            case FALLING_BLOCK:
                if(!(sourceEntity instanceof EntityFallingBlock)){
                    return;
                }
                final @Nullable IBlockState quantaState = Laminarifers.getAmountState(laminarifer, replacedState, fluid, null,currentAmount+ amount);
                if(quantaState == null || quantaState == replacedState) return;
                ((EntityFallingBlockAccessor)sourceEntity).setFallTile(quantaState);
                break;
        }
    }
}
