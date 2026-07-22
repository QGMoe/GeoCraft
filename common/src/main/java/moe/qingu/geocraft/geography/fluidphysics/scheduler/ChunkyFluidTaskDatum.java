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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.handler.CapabilityHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author QGMoe
 */
public final class ChunkyFluidTaskDatum implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"ft_datum");
    public static final int SWITCH_ARRAY_THRESHOLD = 200;
    public static final int SWITCH_LINEAR_THRESHOLD = 60;
    private static final ThreadLocal<IntOpenHashSet> TEMP = ThreadLocal.withInitial(IntOpenHashSet::new);
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private volatile SoftReference<NBTTagCompound> save = new SoftReference<>(new NBTTagCompound());
    final ReentrantLock lock = new ReentrantLock();
    boolean flip = false;
    FluidTaskQueue queueHeavy = null;
    FluidTaskQueue queueLight = null;

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean isScheduled(final int chunkX,final int chunkY,final int chunkZ){
        return this.queueHeavy != null && this.queueHeavy.contains(chunkX, chunkY, chunkZ)
                || this.queueLight != null && this.queueLight.contains(chunkX,255-chunkY,chunkZ);
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void scheduleHeavy(final int chunkX, final int chunkY, final int chunkZ, final int taskID){
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
    public void scheduleLight(final int chunkX,final int chunkY,final int chunkZ,final int taskID){
        lock.lock();
        try {
            if(this.queueLight == null) this.queueLight = new LinearFluidTaskQueue();
            this.queueLight.queue(chunkX, 255 - chunkY, chunkZ, taskID);
            this.queueLight = switchQueue(queueLight);
        }finally {
            lock.unlock();
        }
    }

    @Nullable
    public IFluidTask queryHeavy(final int chunkX,final int chunkY,final int chunkZ){
        if(this.queueHeavy == null) return null;
        return this.queueHeavy.query(chunkX, chunkY, chunkZ);
    }

    @Nullable
    public IFluidTask queryLight(final int chunkX,final int chunkY,final int chunkZ){
        if(this.queueLight == null) return null;
        return this.queueLight.query(chunkX, 255 - chunkY, chunkZ);
    }

    /* -------------------------------
         Serialisation Area
       ------------------------------- */

    @Override
    @Nonnull
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS,ThreadType.GEO_MISC_DAEMON})
    public NBTTagCompound serializeNBT() {
        final @Nullable NBTTagCompound cache;
        if(isDirty() || (cache = this.save.get()) == null){
            lock.lock();
            try {
                final NBTTagCompound s = new NBTTagCompound();
                s.setInteger("v",1);
                if(queueHeavy != null) s.setTag("heavy",serialize(queueHeavy));
                if(queueLight != null) s.setTag("light",serialize(queueLight));
                this.save = new SoftReference<>(s);
                return s;
            }finally {
                this.clearDirty();
                lock.unlock();
            }
        }
        return cache;
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

    /* -------------------------------
               Capability Area
       ------------------------------- */

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityHandler.CHUNKY_FLUID_TASK_DATA;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityHandler.CHUNKY_FLUID_TASK_DATA ? CapabilityHandler.CHUNKY_FLUID_TASK_DATA.cast(this):null;
    }

    /* -------------------------------
              Getter And Setter
       ------------------------------- */

    public boolean isDirty() {
        return dirty.get();
    }

    public boolean hasLeft(){
        return queueHeavy != null && !queueHeavy.isEmpty() || queueLight != null && !queueLight.isEmpty();
    }

    @Nonnull
    public ReentrantLock getLock() {
        return lock;
    }

    public boolean markDirty(){
        return dirty.compareAndSet(false,true);
    }

    public void clearDirty(){
        dirty.set(false);
    }

   /* -------------------------------
                Static Area
       ------------------------------- */

    @Nonnull
    private static FluidTaskQueue switchQueue(final @Nonnull FluidTaskQueue current){
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
}
