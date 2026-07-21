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

package moe.qingu.geocraft.geography.fluidphysics.updater;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moe.qingu.geocraft.api.fluidphysics.updater.scheduler.FluidTaskScheduler;
import moe.qingu.geocraft.api.fluidphysics.updater.task.FluidTaskCollector;
import moe.qingu.geocraft.api.fluidphysics.updater.task.FluidTaskRegistry;
import moe.qingu.geocraft.api.fluidphysics.updater.task.IFluidTask;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.configs.FluidPhysicsConfig;
import moe.qingu.geocraft.handler.CapabilityHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;

/**
 * @author QGMoe
 */
public final class ChunkyFluidTaskScheduler extends FluidTaskScheduler implements ICapabilityProvider {
    private final MBlockPos posContainer = new MBlockPos();
    private final int maxUpdateNum;
    private final Long2ObjectOpenHashMap<FluidUpdater> updaters = new Long2ObjectOpenHashMap<>();
    private final ConcurrentLinkedQueue<FluidUpdater> dirties = new ConcurrentLinkedQueue<>();
    private final LongOpenHashSet schedules = new LongOpenHashSet();
    private final ChunkyCollector collector = new ChunkyCollector();
    private long[] temp = new long[0];

    public ChunkyFluidTaskScheduler(final @Nonnull World world) {
        super(world);
        maxUpdateNum = FluidPhysicsConfig.FLUID_UPDATER_MAX_TASKS_PER_TICK.getValue();
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid){
        if(pos.getY()>255 || pos.getY()<0) return;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final FluidUpdater updater = getUpdater(chunkX,chunkZ);
        if(updater == null) return;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        if(updater.isScheduled(cx,pos.getY(),cz)) return;
        final int taskID = FluidTaskRegistry.getID(task);
        if(taskID <0 || taskID > 65535) throw new IllegalArgumentException();
        if(fluid.getDensity() > 0) updater.scheduleHeavy(cx, pos.getY(), cz, taskID);
        else updater.scheduleLight(cx,pos.getY(),cz,taskID);
        schedules.add(ChunkPos.asLong(chunkX,chunkZ));
        if(!updater.isDirty() && updater.markDirty()){
            final Chunk chunk = world.getChunk(chunkX,chunkZ);
            chunk.markDirty();
            dirties.add(updater);
        }
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void update(){
        final long beginTime = System.nanoTime(),maxTime = FluidPhysicsConfig.FLUID_UPDATER_MAX_TIME_USAGE.getValue();
        temp = schedules.toLongArray(temp);
        final int size = schedules.size();
        long count = 0;
        int i = 0;
        while (count < maxUpdateNum && i < size){
            final long pos = temp[i++];
            final FluidUpdater updater = updaters.get(pos);
            if(updater == null) {
                schedules.remove(pos);
                continue;
            }
            final int z = (int) (pos>>Integer.SIZE);
            final int x = (int) pos;
            int cot = 0;
            final Chunk chunk = world.getChunk(x,z);
            do {
                final int n = updater.update(posContainer,chunk,collector);
                count += n;
                cot += n;
            } while (updater.hasLeft() && count < maxUpdateNum);
            collector.cleanup(updater);
            if(cot != 0 && updater.markDirty()){
                chunk.markDirty();
                dirties.add(updater);
            }
            if(!updater.hasLeft()) schedules.remove(pos);
            if(System.nanoTime() - beginTime > maxTime) break;
        }
    }

    @Nullable
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public IFluidTask query(@Nonnull final BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0) return null;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final FluidUpdater updater = getUpdater(chunkX,chunkZ);
        if(updater == null) return null;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        IFluidTask task = updater.queryHeavy(cx,pos.getY(),cz);
        if(task == null) task = updater.queryLight(cx,pos.getY(),cz);
        return task;
    }

    @Nullable
    public FluidUpdater getUpdater(final int cx,final int cz){
        FluidUpdater res = updaters.get(ChunkPos.asLong(cx,cz));
        if(res != null) return res;
        final Chunk chunk = world.getChunk(cx,cz);
        if(chunk.hasCapability(CapabilityHandler.FLUID_UPDATER,null)){
            updaters.put(ChunkPos.asLong(cx,cz),res = chunk.getCapability(CapabilityHandler.FLUID_UPDATER,null));
            return res;
        }else return null;
    }

    @Nonnull
    public Long2ObjectOpenHashMap<FluidUpdater> getUpdaters() {
        return updaters;
    }

    @Nonnull
    public LongOpenHashSet getSchedules() {
        return schedules;
    }

    @Nonnull
    public ConcurrentLinkedQueue<FluidUpdater> getDirties() {
        return dirties;
    }

    @Nullable
    public static ChunkyFluidTaskScheduler getChunkyScheduler(final @Nonnull World world){
        final FluidTaskScheduler scheduler = getScheduler(world);
        if(scheduler instanceof ChunkyFluidTaskScheduler) return (ChunkyFluidTaskScheduler) scheduler;
        else return null;
    }

    static final class ChunkyCollector extends FluidTaskCollector{
        private final LinearFluidTaskQueue heavy = new LinearFluidTaskQueue();
        private final LinearFluidTaskQueue light = new LinearFluidTaskQueue();
        private final FluidTaskTransformer transformer = new FluidTaskTransformer();
        int x;
        int y;
        int z;

        private boolean isEmpty(){
            return heavy.isEmpty() && light.isEmpty();
        }

        @Override
        public void schedule(@Nonnull final IFluidTask task, @Nonnull final Fluid fluid) {
            final int taskID = FluidTaskRegistry.getID(task);
            if(taskID <0 || taskID > 65535) throw new IllegalArgumentException();
            (fluid.getDensity() >0 ? heavy:light).queue(x, y, z, taskID);
        }

        private void cleanup(@Nonnull final FluidUpdater updater){
            if(isEmpty()) return;
            transformer.updater = updater;
            transformer.light = false;
            heavy.forEach(transformer);
            heavy.clear();
            transformer.light = true;
            light.forEach(transformer);
            light.clear();
        }
    }

    private static final class FluidTaskTransformer implements IntConsumer{
        private FluidUpdater updater;
        private boolean light = false;

        @Override
        public void accept(final int task) {
            final int x = (task >>> 4) & 0xF;
            final int y = task >>> 24;
            final int z = task & 0xF;
            if(!updater.isScheduled(x,y,z)){
                final int taskID = (task >> 8) & 0xFFFF;
                if(light) updater.scheduleLight(x,y,z,taskID); else updater.scheduleHeavy(x,y,z,taskID);
            }
        }
    }
}
