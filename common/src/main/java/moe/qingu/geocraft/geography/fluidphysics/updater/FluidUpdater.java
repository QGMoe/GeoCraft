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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.capability.FluidUpdaterCapability;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author QGMoe
 */
public class FluidUpdater implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"fluid_updater");
    public static final int SWITCH_ARRAY_THRESHOLD = 200;
    public static final int SWITCH_LINEAR_THRESHOLD = 60;
    private static final ThreadLocal<IntOpenHashSet> TEMP = ThreadLocal.withInitial(IntOpenHashSet::new);
    protected final ReentrantLock lock = new ReentrantLock();
    protected final AtomicBoolean dirty = new AtomicBoolean(false);
    protected FluidTaskQueue queueHeavy = null;
    protected FluidTaskQueue queueLight = null;
    protected Consumer consumer = new Consumer();
    protected volatile NBTTagCompound save = new NBTTagCompound();

    public boolean isDirty() {
        return dirty.get();
    }

    public boolean hasLeft(){
        return queueHeavy != null && !queueHeavy.isEmpty() || queueLight != null && !queueLight.isEmpty();
    }

    public boolean markDirty(){
        return dirty.compareAndSet(false,true);
    }

    public void clearDirty(){
        dirty.set(false);
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public int update(final @Nonnull World world, final @Nonnull MBlockPos container, final int chunkX,final int chunkZ){
        lock.lock();
        try {
            int count = 0;
            consumer.setChunk(world.getChunk(chunkX,chunkZ));
            consumer.setPosContainer(container);
            consumer.setFlip(false);
            if(queueHeavy != null) count += queueHeavy.forNext(consumer);
            consumer.setFlip(true);
            if(queueLight != null) count += queueLight.forNext(consumer);
            return count;
        }finally {
            consumer.clear();
            lock.unlock();
        }
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void scheduleHeavy(final int chunkX, final int chunkY, final int chunkZ, final short taskID){
        lock.lock();
        try {
            if(this.queueHeavy == null) this.queueHeavy = new LinearFluidTaskQueue();
            this.queueHeavy.queue(chunkX, chunkY, chunkZ, taskID);
            this.queueHeavy = switchQueue(queueHeavy);
        }finally {
            lock.unlock();
        }
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void scheduleLight(final int chunkX,final int chunkY,final int chunkZ,final short taskID){
        lock.lock();
        try {
            if(this.queueLight == null) this.queueLight = new LinearFluidTaskQueue();
            this.queueLight.queue(chunkX, 255 - chunkY, chunkZ, taskID);
            this.queueLight = switchQueue(queueLight);
        }finally {
            lock.unlock();
        }
    }

    @Override
    @Nonnull
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS,ThreadType.FLUID_DAEMON})
    public NBTTagCompound serializeNBT() {
        if(isDirty()){
            lock.lock();
            try {
                this.save = new NBTTagCompound();
                save.setInteger("v",1);
                if(queueHeavy != null) save.setTag("heavy",serialize(queueHeavy));
                if(queueLight != null) save.setTag("light",serialize(queueLight));
                return this.save;
            }finally {
                this.clearDirty();
                lock.unlock();
            }
        }
        return this.save;
    }

    @Override
    public void deserializeNBT(final @Nonnull NBTTagCompound nbt) {
        if(nbt.getInteger("v")>1) throw new IllegalArgumentException();
        if(nbt.hasKey("heavy")){
            if(queueHeavy == null) queueHeavy = new LinearFluidTaskQueue();
            for (final int task: nbt.getIntArray("heavy")) this.queueHeavy.queue(task);
        }
        if(nbt.hasKey("light")){
            if(queueLight == null) queueLight = new LinearFluidTaskQueue();
            for (final int task: nbt.getIntArray("light")) this.queueLight.queue(task);
        }
    }

    private @Nonnull NBTTagIntArray serialize(final @Nonnull FluidTaskQueue queue){
        final IntOpenHashSet set = TEMP.get();
        set.clear();
        queue.forEach(set::add);
        final int[] arr = set.toIntArray();
        return new NBTTagIntArray(arr);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == FluidUpdaterCapability.FLUID_UPDATER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == FluidUpdaterCapability.FLUID_UPDATER?FluidUpdaterCapability.FLUID_UPDATER.cast(this):null;
    }

    @Nonnull
    public ReentrantLock getLock() {
        return lock;
    }

    @Nonnull
    protected static FluidTaskQueue switchQueue(final @Nonnull FluidTaskQueue current){
        if(current.size()>SWITCH_ARRAY_THRESHOLD && current instanceof LinearFluidTaskQueue){
            final FluidTaskQueue queue = new ArrayFluidTaskQueue();
            current.forEach(queue::queue);
            return queue;
        }else if(current.size()<SWITCH_LINEAR_THRESHOLD && current instanceof ArrayFluidTaskQueue){
            final LinearFluidTaskQueue queue = new LinearFluidTaskQueue();
            current.forEach(queue::queue);
            return queue;
        }else return current;
    }

    protected static final class Consumer extends FluidTaskConsumer{

        private Chunk chunk;
        private MBlockPos posContainer;
        private boolean flip = false;

        public void setChunk(final @Nonnull Chunk chunk) {
            this.chunk = chunk;
        }

        public void setPosContainer(final @Nonnull MBlockPos posContainer) {
            this.posContainer = posContainer;
        }

        public void setFlip(final boolean flip) {
            this.flip = flip;
        }

        public void clear(){
            this.chunk = null;
            this.posContainer = null;
        }

        @Override
        public void consume(final int x,int y,final int z, final @Nonnull IFluidTask task) {
            if(flip) y = 255-y;
            final @Nullable ExtendedBlockStorage storage = chunk.getBlockStorageArray()[y>>4];
            if(storage != Chunk.NULL_BLOCK_STORAGE){
                final IBlockState state = storage.get(x,y & 0xF,z);
                final World world = chunk.getWorld();
                if(!task.accepts(world,state)) return;
                posContainer.setPos((chunk.x<<4)+x,y,(chunk.z<<4)+z);
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
                        logger.error("FluidUpdateManager caught an error:",t2);
                    }
                }
            }

        }
    }
}
