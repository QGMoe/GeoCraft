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

package moe.qingu.geocraft.handler.event;

import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.event.EventFactory;
import moe.qingu.geocraft.api.event.fluidphysics.FluidTaskSchedulerEvent;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.EmptyFluidTaskScheduler;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import moe.qingu.geocraft.geography.fluidphysics.scheduler.ChunkyFluidTaskScheduler;
import moe.qingu.geocraft.geography.fluidphysics.scheduler.ChunkyFluidTaskDatum;
import moe.qingu.geocraft.handler.CapabilityHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author QGMoe
 */
@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
public final class FluidPhysicsEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void createFluidTaskScheduler(final @Nonnull FluidTaskSchedulerEvent.Create event){
        if(event.getCandidate() == null && !event.getWorld().isRemote && event.getResult() != Event.Result.ALLOW){
            final World world = event.getWorld();
            event.setCandidate(() -> new ChunkyFluidTaskScheduler(world));
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(final @Nonnull ChunkEvent.Load event){
        final Chunk chunk = event.getChunk();
        final ChunkyFluidTaskDatum datum = chunk.getCapability(CapabilityHandler.CHUNKY_FLUID_TASK_DATA,null);
        if(datum == null || !datum.hasLeft()) return;
        final ChunkyFluidTaskScheduler scheduler = ChunkyFluidTaskScheduler.getChunkyScheduler(event.getWorld());
        if(scheduler == null || scheduler.getWorld() != event.getWorld()) return;
        final long pos = ChunkPos.asLong(chunk.x,chunk.z);
        scheduler.getData().put(pos,datum);
        scheduler.getSchedules().add(pos);
    }

    @SubscribeEvent
    public static void onChunkUnload(final @Nonnull ChunkEvent.Unload event){
        final ChunkyFluidTaskScheduler scheduler = ChunkyFluidTaskScheduler.getChunkyScheduler(event.getWorld());
        if(scheduler == null || scheduler.getWorld() != event.getWorld()) return;
        final Chunk chunk = event.getChunk();
        final long pos = ChunkPos.asLong(chunk.x,chunk.z);
        scheduler.getSchedules().remove(pos);
        scheduler.getData().remove(pos);
    }

    public static void onWorldAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<World> event,final @Nonnull World world){
        final Supplier<FluidTaskScheduler> supplier = EventFactory.onFluidUpdaterManagerCreate(world);
        event.addCapability(FluidTaskScheduler.ID,supplier == null?new EmptyFluidTaskScheduler(world): supplier.get());
    }

    public static void onChunkAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<Chunk> event,final @Nonnull World world){
        if(ChunkyFluidTaskScheduler.getChunkyScheduler(world) != null) event.addCapability(ChunkyFluidTaskDatum.ID, new ChunkyFluidTaskDatum());
    }
}
