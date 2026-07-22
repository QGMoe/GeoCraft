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

package moe.qingu.geocraft.geography.fluidphysics.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTaskResponder;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskCollector;
import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskRegistry;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.configs.FluidPhysicsConfig;
import moe.qingu.geocraft.handler.CapabilityHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;

/**
 * @author QGMoe
 */
public final class ChunkyFluidTaskScheduler extends FluidTaskScheduler implements ICapabilityProvider {
    private final int maxUpdateNum;
    private final Long2ObjectOpenHashMap<ChunkyFluidTaskDatum> data = new Long2ObjectOpenHashMap<>();
    private final ConcurrentLinkedQueue<ChunkyFluidTaskDatum> dirties = new ConcurrentLinkedQueue<>();
    private final LongOpenHashSet schedules = new LongOpenHashSet();
    private final Consumer consumer = new Consumer();
    private long[] temp = new long[0];

    public ChunkyFluidTaskScheduler(final @Nonnull World world) {
        super(world);
        maxUpdateNum = FluidPhysicsConfig.FLUID_UPDATER_MAX_TASKS_PER_TICK.getValue();
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean schedule(final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid){
        if(pos.getY()>255 || pos.getY()<0) return false;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final ChunkyFluidTaskDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return false;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        if(datum.isScheduled(cx,pos.getY(),cz)) return false;
        final int taskID = FluidTaskRegistry.getID(task);
        if(taskID <0 || taskID > 65535) return false;
        if(fluid.getDensity() > 0) datum.scheduleHeavy(cx, pos.getY(), cz, taskID);
        else datum.scheduleLight(cx,pos.getY(),cz,taskID);
        schedules.add(ChunkPos.asLong(chunkX,chunkZ));
        if(!datum.isDirty() && datum.markDirty()){
            final Chunk chunk = world.getChunk(chunkX,chunkZ);
            chunk.markDirty();
            dirties.add(datum);
        }
        return true;
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
            final ChunkyFluidTaskDatum datum = data.get(pos);
            if(datum == null) {
                schedules.remove(pos);
                continue;
            }
            final int z = (int) (pos>>Integer.SIZE);
            final int x = (int) pos;
            int cot = 0;
            final Chunk chunk = world.getChunk(x,z);
            consumer.chunk = chunk;
            datum.lock.lock();
            try {
                do {
                    int n = 0;
                    consumer.flip = datum.flip;
                    if(consumer.flip){
                        if(datum.queueLight != null) n += datum.queueLight.forNext(consumer);
                        consumer.flip = datum.flip = false;
                        if(datum.queueHeavy != null) n += datum.queueHeavy.forNext(consumer);
                    }else {
                        if(datum.queueHeavy != null) n += datum.queueHeavy.forNext(consumer);
                        consumer.flip = datum.flip = true;
                        if(datum.queueLight != null) n += datum.queueLight.forNext(consumer);
                    }
                    cot += n;
                    count += n;
                } while (datum.hasLeft() && count < maxUpdateNum);
            }finally {
                datum.lock.unlock();
            }
            consumer.collector.cleanup(datum);
            if(cot != 0 && datum.markDirty()){
                chunk.markDirty();
                dirties.add(datum);
            }
            if(!datum.hasLeft()) schedules.remove(pos);
            if(System.nanoTime() - beginTime > maxTime) break;
        }
        consumer.chunk = null;
    }

    @Nullable
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public IFluidTask query(@Nonnull final BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0) return null;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final ChunkyFluidTaskDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return null;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        IFluidTask task = datum.queryHeavy(cx,pos.getY(),cz);
        if(task == null) task = datum.queryLight(cx,pos.getY(),cz);
        return task;
    }

    @Nullable
    public ChunkyFluidTaskDatum getDatum(final int cx, final int cz){
        ChunkyFluidTaskDatum res = data.get(ChunkPos.asLong(cx,cz));
        if(res != null) return res;
        final Chunk chunk = world.getChunk(cx,cz);
        if(chunk.hasCapability(CapabilityHandler.CHUNKY_FLUID_TASK_DATA,null)){
            data.put(ChunkPos.asLong(cx,cz),res = chunk.getCapability(CapabilityHandler.CHUNKY_FLUID_TASK_DATA,null));
            return res;
        }else return null;
    }

    @Nonnull
    public Long2ObjectOpenHashMap<ChunkyFluidTaskDatum> getData() {
        return data;
    }

    @Nonnull
    public LongOpenHashSet getSchedules() {
        return schedules;
    }

    @Nonnull
    public ConcurrentLinkedQueue<ChunkyFluidTaskDatum> getDirties() {
        return dirties;
    }

    @Nullable
    public static ChunkyFluidTaskScheduler getChunkyScheduler(final @Nonnull World world){
        final FluidTaskScheduler scheduler = getScheduler(world);
        if(scheduler instanceof ChunkyFluidTaskScheduler) return (ChunkyFluidTaskScheduler) scheduler;
        else return null;
    }

    private static final class Consumer extends FluidTaskConsumer{
        private final MBlockPos posContainer = new MBlockPos();
        private final ChunkyCollector collector = new ChunkyCollector();
        private Chunk chunk;
        private boolean flip = false;

        void prepareForResponder(final int x,final int y,final int z){
            posContainer.setPos((chunk.x << 4) + x, y, (chunk.z << 4) + z);
            collector.x = x;
            collector.y = y;
            collector.z = z;
        }

        @Override
        public void consume(final int x, int y, final int z, @Nonnull final IFluidTask task) {
            if(flip) y = 255-y;
            final @Nullable ExtendedBlockStorage storage = chunk.getBlockStorageArray()[y>>4];
            if(storage != Chunk.NULL_BLOCK_STORAGE){
                final IBlockState state = storage.get(x,y & 0xF,z);
                final World world = chunk.getWorld();
                try {
                    final Block block = state.getBlock();
                    final IFluidTaskResponder responder = block instanceof IFluidTaskResponder ?(IFluidTaskResponder) block:null;
                    if (!task.accepts(world, state)){
                        if(responder != null) {
                            prepareForResponder(x,y,z);
                            responder.onStaleTask(world,posContainer,state,task,collector);
                        }
                        return;
                    }

                    if (responder != null && !responder.accepts(world,state,task)){
                        prepareForResponder(x,y,z);
                        responder.onRefused(world,posContainer,state,task,collector);
                        return;
                    }
                    posContainer.setPos((chunk.x << 4) + x, y, (chunk.z << 4) + z);
                }catch (final Throwable t){
                    final Logger logger = GeoCraft.getLogger();
                    logger.warn("When preparing update fluid {} at {} in world {},",state,posContainer,world.provider.getDimension());
                    logger.warn("FluidUpdater caught an error:",t);
                    return;
                }
                try {
                    task.onUpdate(world,state,posContainer,world.rand);
                } catch (final Throwable t) {
                    final Logger logger = GeoCraft.getLogger();
                    logger.warn("When updating fluid {} at {} in world {},",state,posContainer,world.provider.getDimension());
                    logger.warn("FluidUpdater caught an error:",t);
                    try {
                        task.onFailure(world,state,posContainer,world.rand);
                    }catch (final Throwable t2){
                        logger.error("When restoring failure of fluid {} at {} in world {},",state,posContainer,world.provider.getDimension());
                        logger.error("FluidUpdater caught an error:",t2);
                    }
                }
            }
        }
    }

    private static final class ChunkyCollector extends FluidTaskCollector{
        private final LinearFluidTaskQueue heavy = new LinearFluidTaskQueue();
        private final LinearFluidTaskQueue light = new LinearFluidTaskQueue();
        private final FluidTaskTransformer transformer = new FluidTaskTransformer();
        private int x;
        private int y;
        private int z;

        private boolean isEmpty(){
            return heavy.isEmpty() && light.isEmpty();
        }

        @Override
        public void schedule(@Nonnull final IFluidTask task, @Nonnull final Fluid fluid) {
            final int taskID = FluidTaskRegistry.getID(task);
            if(taskID <0 || taskID > 65535) throw new IllegalArgumentException();
            (fluid.getDensity() >0 ? heavy:light).queue(x, y, z, taskID);
        }

        private void cleanup(@Nonnull final ChunkyFluidTaskDatum datum){
            if(isEmpty()) return;
            transformer.datum = datum;
            transformer.light = false;
            datum.lock.lock();
            try {
                heavy.forEach(transformer);
                heavy.clear();
                transformer.light = true;
                light.forEach(transformer);
                light.clear();
            }finally {
                datum.lock.unlock();
            }
        }
    }

    private static final class FluidTaskTransformer implements IntConsumer{
        private ChunkyFluidTaskDatum datum;
        private boolean light = false;

        @Override
        public void accept(final int task) {
            final int x = (task >>> 4) & 0xF;
            final int y = task >>> 24;
            final int z = task & 0xF;
            if(!datum.isScheduled(x,y,z)){
                final int taskID = (task >> 8) & 0xFFFF;
                if(light) datum.scheduleLight(x,y,z,taskID); else datum.scheduleHeavy(x,y,z,taskID);
            }
        }
    }
}
