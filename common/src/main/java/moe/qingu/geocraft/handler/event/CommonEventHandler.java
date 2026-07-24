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

package moe.qingu.geocraft.handler.event;

import moe.qingu.geocraft.api.GeoCraftAPI;
import moe.qingu.geocraft.api.event.EventFactory;
import moe.qingu.geocraft.api.event.world.BlockTickSchedulerEvent;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import moe.qingu.geocraft.api.world.tick.scheduler.BlockTickScheduler;
import moe.qingu.geocraft.api.world.tick.scheduler.MojangBlockTickScheduler;
import moe.qingu.geocraft.api.world.tick.validator.BlockTickValidator;
import moe.qingu.geocraft.configs.GeneralConfig;
import moe.qingu.geocraft.handler.CapabilityHandler;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickScheduler;
import moe.qingu.geocraft.world.scheduler.GeoBlockTickType;
import net.minecraft.block.Block;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.property.IGeographyProperty;
import moe.qingu.geocraft.handler.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
public final class CommonEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void createBlockTickScheduler(final @Nonnull BlockTickSchedulerEvent.Create event){
        if(!GeneralConfig.ENABLE_BLOCK_UPDATER.getValue()) return;
        if(event.getCandidate() == null && !event.getWorld().isRemote && event.getResult() != Event.Result.ALLOW){
            final World world = event.getWorld();
            final GeoBlockTickType tickType = GeneralConfig.BLOCK_TICK_SCHEDULER_TYPE.getValue();
            event.setCandidate(tickType.supplier(world));
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SuppressWarnings("unchecked")
    public static void onChunkLoad(final @Nonnull ChunkEvent.Load event){
        final Chunk chunk = event.getChunk();
        final ChunkyBlockTickDatum datum = chunk.getCapability(CapabilityHandler.CHUNKY_BLOCK_TICK_DATUM,null);
        if(datum == null || datum.isEmpty()) return;
        final ChunkyBlockTickScheduler<ChunkyBlockTickDatum> scheduler = (ChunkyBlockTickScheduler<ChunkyBlockTickDatum>) ChunkyBlockTickScheduler.getChunkyScheduler(event.getWorld());
        if(scheduler == null || scheduler.getWorld() != event.getWorld() || scheduler.getStorageType() != datum.getClass()) return;
        final long pos = ChunkPos.asLong(chunk.x,chunk.z);
        scheduler.getVolume().data.put(pos,datum);
        scheduler.getVolume().schedules.add(pos);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onChunkUnload(final @Nonnull ChunkEvent.Unload event){
        final ChunkyBlockTickScheduler<?> scheduler = ChunkyBlockTickScheduler.getChunkyScheduler(event.getWorld());
        if(scheduler == null || scheduler.getWorld() != event.getWorld()) return;
        final Chunk chunk = event.getChunk();
        final long pos = ChunkPos.asLong(chunk.x,chunk.z);
        scheduler.getVolume().schedules.remove(pos);
        scheduler.getVolume().data.remove(pos);
    }

    @SubscribeEvent
    public static void onRegisterBlocks(final @Nonnull RegistryEvent.Register<Block> event){
        RegistryHandler.mapMissingStates();
    }

    @SubscribeEvent
    public static void onRegisterAtmosphereProperty(final @Nonnull RegistryEvent.Register<IGeographyProperty> event){
        RegistryHandler.registerGeographyProperties(event);
    }

    @SubscribeEvent
    public static void onWorldAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<World> event){
        if(event.getObject().isRemote) return;
        final Supplier<BlockTickScheduler> supplier = EventFactory.onBlockTickSchedulerCreate(event.getObject());
        final BlockTickScheduler scheduler = supplier == null?new MojangBlockTickScheduler(event.getObject()):supplier.get();
        final Supplier<BlockTickValidator> validatorSupplier = EventFactory.onBlockTickValidatorInit(scheduler);
        if(validatorSupplier != null){
            try {
                scheduler.setValidator(validatorSupplier.get());
            } catch (final @Nonnull UnsupportedOperationException e) {
                GeoCraftAPI.LOGGER.error(scheduler.getClass().getName() + " doesn't support validator set!",e);
            }
        }
        event.addCapability(BlockTickScheduler.ID, scheduler);
        FluidPhysicsEventHandler.onWorldAttachCapabilities(event,event.getObject());
    }

    @SubscribeEvent
    @SuppressWarnings("ConstantValue")
    public static void onChunkAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<Chunk> event){
        final @Nullable World world = event.getObject().getWorld(); //??? 为什么在逻辑客户端这会是null
        if(world == null || world.isRemote) return;
        final @Nullable ChunkyBlockTickDatum datum = ChunkyBlockTickDatum.createByScheduler(BlockTickScheduler.getScheduler(world),event.getObject());
        if(datum != null) event.addCapability(ChunkyBlockTickDatum.ID,datum);
        FluidPhysicsEventHandler.onChunkAttachCapabilities(event,world);
    }

    @SubscribeEvent
    public static void onWorldUnload(final @Nonnull WorldEvent.Unload event){
        final World world = event.getWorld();
        if(world.isRemote) return;
        FluidTaskScheduler.getSchedulers().remove(world.provider.getDimension());
        BlockTickScheduler.getSchedulers().remove(world.provider.getDimension());
    }
}
