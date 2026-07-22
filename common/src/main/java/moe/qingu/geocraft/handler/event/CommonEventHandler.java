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

import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.property.IGeographyProperty;
import moe.qingu.geocraft.handler.RegistryHandler;
import moe.qingu.geocraft.world.BlockUpdater;
import moe.qingu.geocraft.world.storage.ScheduledTicksData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
public final class CommonEventHandler {

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
        final BlockUpdater updater = new BlockUpdater();
        updater.setWorld(event.getObject());
        event.addCapability(BlockUpdater.ID, updater);
        FluidPhysicsEventHandler.onWorldAttachCapabilities(event,event.getObject());
    }

    @SubscribeEvent
    @SuppressWarnings("ConstantValue")
    public static void onChunkAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<Chunk> event){
        final @Nullable World world = event.getObject().getWorld(); //??? 为什么在逻辑客户端这会是null
        if(world == null || world.isRemote) return;
        event.addCapability(ScheduledTicksData.ID, new ScheduledTicksData().setChunk(event.getObject()));
        FluidPhysicsEventHandler.onChunkAttachCapabilities(event,world);
    }

    @SubscribeEvent
    public static void onWorldUnload(final @Nonnull WorldEvent.Unload event){
        final World world = event.getWorld();
        if(world.isRemote) return;
        FluidTaskScheduler.getSchedulers().remove(world.provider.getDimension());
    }
}
