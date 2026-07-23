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

package moe.qingu.geocraft.world.scheduler;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.handler.CapabilityHandler;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author QGMoe
 */
@SuppressWarnings("OctalInteger")
public final class ChunkyBlockTickDatum implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"bt_datum");
    private static final ThreadLocal<LongArrayList> TEMP = ThreadLocal.withInitial(LongArrayList::new);
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private volatile SoftReference<NBTTagCompound> save = new SoftReference<>(new NBTTagCompound());
    final ReentrantLock lock = new ReentrantLock();
    BlockTickQueue queue = null;

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(final long worldTotalTime,final int cx, final int cy, final int cz, final int blockID, final long delay, final @Nonnull TickPriority priority){
        lock.lock();
        try {
            if(queue == null){
                queue = new HeapBlockTickQueue();
                queue.baseTime = worldTotalTime;
            }else if(worldTotalTime - queue.baseTime > 2147483647L) queue.updateBaseTime(worldTotalTime);
            queue.queue(cx,cy,cz,blockID,worldTotalTime+delay-queue.baseTime,priority.ordinal());
        }finally {
            lock.unlock();
        }
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean isScheduled(final int cx,final int cy,final int cz,final int blockID){
        return queue != null && queue.contains(cx, cy, cz, blockID);
    }

    @Nonnull
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<IScheduledTick> query(final int cx,final int cy,final int cz) {
        final ObjectOpenHashSet<IScheduledTick> ticks = new ObjectOpenHashSet<>();
        queue.forEach(t -> {
            final long x = (t >>> 4) & 0xFL;
            final long y = (t >>> 20) & 0xFFL;
            final long z = t & 0xFL;
            if(x != cx || y != cy || z != cz) return;
            final long scheduledTime = queue.baseTime + (t >>> 32);
            final @Nonnull Block block = Block.getBlockById((int)((t >>> 8)&0_7777L));
            final @Nonnull TickPriority priority = TickPriority.of((int)((t >>> 28)&0xFL));
            ticks.add(IScheduledTick.of(block,new BlockPos(x,y,z),scheduledTime,priority));
        });
        return ticks;
    }

    /* -------------------------------
         Serialisation Area
       ------------------------------- */

    @Override
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS,ThreadType.GEO_MISC_DAEMON})
    public NBTTagCompound serializeNBT() {
        final @Nullable NBTTagCompound cache;
        if(isDirty() || (cache = this.save.get()) == null){
            lock.lock();
            try {
                final NBTTagCompound s = new NBTTagCompound();
                s.setInteger("v",1);
                if(queue != null) s.setTag("queue", serializeQueue());
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
        if(nbt.hasKey("queue")){
            final NBTBase tag = nbt.getTag("queue");
            try {
                final long[] dat = tag instanceof NBTTagLongArray? NBTUtils.getLongArray((NBTTagLongArray) tag): new long[0];
                if(queue == null && dat.length != 0) queue = new HeapBlockTickQueue();
                for(final long t:dat) queue.queue(t);
            } catch (final @Nonnull NickelRuntimeException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private @Nonnull NBTTagLongArray serializeQueue(){
        final LongArrayList list = TEMP.get();
        list.clear();
        queue.forEach(list::add);
        final long[] arr = list.toLongArray();
        return new NBTTagLongArray(arr);
    }

    /* -------------------------------
               Capability Area
       ------------------------------- */

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityHandler.BLOCK_TICK_DATUM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityHandler.BLOCK_TICK_DATUM ? CapabilityHandler.BLOCK_TICK_DATUM.cast(this) : null;
    }

    /* -------------------------------
              Getter And Setter
       ------------------------------- */

    public boolean isDirty() {
        return dirty.get();
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
}
