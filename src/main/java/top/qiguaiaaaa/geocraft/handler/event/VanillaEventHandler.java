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

package top.qiguaiaaaa.geocraft.handler.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereUpdateEvent;
import top.qiguaiaaaa.geocraft.api.event.block.StaticLiquidUpdateEvent;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.VanillaFluidPhysicsCore;
import top.qiguaiaaaa.geocraft.mixin.common.entity.EntityFallingBlockAccessor;
import top.qiguaiaaaa.geocraft.util.WaterUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidMixinUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VanillaEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void afterStaticWaterUpdate(StaticLiquidUpdateEvent.After event){
        if(event.getLiquid() != FluidRegistry.WATER) return;
        if(!event.isRandomTick()) return;
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState newState = VanillaFluidPhysicsCore.evaporateWater(worldIn,pos,event.getState(), worldIn.rand);
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
            atmosphere.drainWater(FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,randPos,true);
            event.setResult(Event.Result.ALLOW);
            event.setSnowy(true);
            event.setState(Blocks.SNOW_LAYER.getDefaultState());
        }
    }

    @SubscribeEvent
    public void onPlayerPlacedBlock(BlockEvent.PlaceEvent event){
        if(!onBlockReplaced(event.getWorld(), event.getPos(),event.getBlockSnapshot().getReplacedBlock(),event.getBlockSnapshot().getCurrentBlock(), MoreRealityEventHandler.PlaceSource.PLAYER,event.getEntity())){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityPlacedBlock(BlockEvent.EntityPlaceEvent event){
        if(event instanceof BlockEvent.PlaceEvent) return;
        MoreRealityEventHandler.PlaceSource source = MoreRealityEventHandler.PlaceSource.OTHERS;
        Entity entity = event.getEntity();
        if(entity instanceof EntityFallingBlock) source = MoreRealityEventHandler.PlaceSource.FALLING_BLOCK;
        else if(entity instanceof EntityEnderman) source = MoreRealityEventHandler.PlaceSource.ENDER_MAN;
        if(!onBlockReplaced(event.getWorld(),event.getPos(),event.getBlockSnapshot().getCurrentBlock(),event.getBlockSnapshot().getReplacedBlock(),source,entity)){
            event.setCanceled(true);
        }
    }

    public static boolean onBlockReplaced(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState currentState, @Nonnull IBlockState replacedState, @Nonnull MoreRealityEventHandler.PlaceSource source, @Nullable Entity sourceEntity){
        final Fluid fluid = FluidUtil.getFluid(currentState);
        if(fluid == null) return true;
        final Block block = currentState.getBlock();
        if(block instanceof BlockLiquid){
            int quanta = FluidUtil.getFluidQuanta(world, pos,currentState);
            int curQuanta,canFillQuanta;
            Block placeBlock = replacedState.getBlock();
            ILayeredFluidHost permeable;

            permeable:{
                switch (source){
                    case OTHERS:
                    case ENDER_MAN:
                    default: break permeable;
                    case PLAYER:
                    case FALLING_BLOCK:
                }

                if(placeBlock instanceof ILayeredFluidHost){
                    permeable = (ILayeredFluidHost) placeBlock;
                }else break permeable;

                curQuanta = permeable.getLayers(world,pos,replacedState,fluid);
                canFillQuanta = permeable.addLayer(world,pos,replacedState,fluid,quanta,false);
                if(canFillQuanta <=0) {
                    break permeable;
                }
                IBlockState quantaState = null;
                if(source == MoreRealityEventHandler.PlaceSource.FALLING_BLOCK){
                    if(!(permeable instanceof IBlockStateLayeredFluidHost)){
                        break permeable;
                    }
                    if(!(sourceEntity instanceof EntityFallingBlock)){
                        break permeable;
                    }
                    quantaState = ((IBlockStateLayeredFluidHost)permeable).getLayerState(replacedState,fluid,curQuanta+canFillQuanta);
                    if(quantaState == null){
                        break permeable;
                    }
                }

                switch (source) {
                    case PLAYER:
                        permeable.addLayer(world,pos,replacedState,fluid,quanta,true);
                        break;
                    case FALLING_BLOCK:
                        ((EntityFallingBlockAccessor)sourceEntity).setFallTile(quantaState);
                        break;
                    default: {
                        break permeable;
                    }
                }
                return true;
            }
        }else if(block instanceof BlockFluidBase){
            long amount = FluidMixinUtil.getQBForBlockFluidBase(currentState);
            long canFillAmount;
            Block placeBlock = replacedState.getBlock();
            ILayeredFluidHost permeable;

            permeable:{
                switch (source){
                    case OTHERS:
                    case ENDER_MAN:
                    case FALLING_BLOCK:
                    default: break permeable;
                    case PLAYER:

                }

                if(placeBlock instanceof ILayeredFluidHost){
                    permeable = (ILayeredFluidHost) placeBlock;
                }else break permeable;

                canFillAmount = permeable.addAmountInQB(world,pos,replacedState,fluid,amount,false);
                if(canFillAmount <=0) {
                    break permeable;
                }

                switch (source) {
                    case PLAYER:
                        permeable.addAmountInQB(world,pos,replacedState,fluid,amount,true);
                        break;
                    default: {
                        break permeable;
                    }
                }
                return true;
            }
        }
        return true;
    }
}
