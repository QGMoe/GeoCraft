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

package moe.qingu.geocraft.world.scheduler.packed;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.boxed.BoxedBlockTickDatum;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * @author QGMoe
 */
@SuppressWarnings("OctalInteger")
public final class PackedBlockTickDatum extends ChunkyBlockTickDatum {
    private static final ThreadLocal<LongArrayList> TEMP = ThreadLocal.withInitial(LongArrayList::new);
    PackedBlockTickQueue queue = null;

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(final long worldTotalTime,final int cx, final int cy, final int cz, final int blockID, final long delay, final @Nonnull TickPriority priority){
        lock.lock();
        try {
            if(queue == null){
                queue = new HeapPackedBlockTickQueue();
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
            final long x = (t >>> 12) & 0xFL;
            final long y = (t >>> 20) & 0xFFL;
            final long z = (t >>> 16) & 0xFL;
            if(x != cx || y != cy || z != cz) return;
            final long scheduledTime = queue.baseTime + (t >>> 32);
            final @Nonnull Block block = Block.getBlockById((int)(t & 0_7777L));
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
    protected void serialiseNBT(@Nonnull final NBTTagCompound compound) {
        compound.setByte(KEY_TYPE,TYPE_PACKED);
        if(queue != null){
            compound.setTag("queue", serializeQueue());
            compound.setLong(KEY_BASE_TIME,queue.baseTime);
        }
    }

    @Override
    public void deserializeNBT(final @Nonnull NBTTagCompound nbt) {
        final int v = nbt.getInteger(KEY_VERSION);
        if(v <= 1) deserializeNBTV1(nbt);
        else if(v > 2) throw new RuntimeException(String.format("Deserializing NBT Error: Higher version nbt data! Chunk's Version: %s, Accepted Version: %s", v,2));
        else {
            final byte type = nbt.getByte(KEY_TYPE);
            switch (type){
                case TYPE_BOXED:{
                    this.queue = deserializeNBTV2Boxed(nbt);
                    return;
                } case TYPE_PACKED: this.queue = deserializeNBTV2(nbt);
                //其他类型的忽略
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

    private void deserializeNBTV1(final @Nonnull NBTTagCompound nbt){
        final PriorityQueue<IScheduledTick> ticks = new PriorityQueue<>();
        BoxedBlockTickDatum.deserializeNBTV1(ticks,nbt);
        if(ticks.isEmpty()) return;
        if(this.queue == null) queue = new HeapPackedBlockTickQueue();
        this.queue.baseTime = nbt.getLong("totalWorldTime");
        for(final @Nonnull IScheduledTick tick:ticks){
            final @Nonnull BlockPos pos = tick.pos();
            if(pos.getY() <0 || pos.getY() > 255) continue;
            final int blockID = Block.REGISTRY.getIDForObject(tick.block());
            if(blockID < 0 || blockID > 0_7777) continue;
            this.queue.queue(pos.getX()&0xF,pos.getY(),pos.getZ() & 0xF,blockID,tick.triggeredTick()-this.queue.baseTime,tick.priority().ordinal());
        }
    }

    @Nullable
    public static PackedBlockTickQueue deserializeNBTV2(final @Nonnull NBTTagCompound nbt){
        if(nbt.hasKey("queue")){
            final NBTBase tag = nbt.getTag("queue");
            try {
                final PackedBlockTickQueue queue;
                if(!(tag instanceof NBTTagLongArray)) return null;
                final long[] dat = NBTUtils.getLongArray((NBTTagLongArray) tag);
                if(dat.length > 0) queue = new HeapPackedBlockTickQueue();
                else return null;
                for(final long t:dat) queue.queue(t);
                queue.baseTime = nbt.getLong(KEY_BASE_TIME);
                return queue;
            } catch (final @Nonnull NickelRuntimeException e) {
                throw new RuntimeException(e.getCause());
            }
        }else return null;
    }

    @Nullable
    public static PackedBlockTickQueue deserializeNBTV2Boxed(final @Nonnull NBTTagCompound nbt){
        final PriorityQueue<IScheduledTick> ticks = new PriorityQueue<>();
        BoxedBlockTickDatum.deserializeNBTV2(0,0,ticks,nbt);
        if(ticks.isEmpty()) return null;
        final @Nonnull PackedBlockTickQueue queue = new HeapPackedBlockTickQueue();
        queue.baseTime = nbt.getLong(KEY_BASE_TIME);
        for(final @Nonnull IScheduledTick tick: ticks){
            final BlockPos pos = tick.pos();
            if(pos.getY()>255 || pos.getY() < 0) continue;
            final int blockID = Block.REGISTRY.getIDForObject(tick.block());
            if(blockID > 0_7777 || blockID < 0) continue;
            queue.queue(pos.getX(),pos.getY(),pos.getZ(),blockID,tick.triggeredTick()-queue.baseTime,tick.priority().ordinal());
        }
        return queue;
    }

    /* -------------------------------
              Getter And Setter
       ------------------------------- */

    public boolean markDirty(){
        return dirty.compareAndSet(false,true);
    }
}
