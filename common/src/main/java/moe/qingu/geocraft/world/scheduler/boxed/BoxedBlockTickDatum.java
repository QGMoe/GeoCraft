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

package moe.qingu.geocraft.world.scheduler.boxed;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.packed.PackedBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.packed.PackedBlockTickQueue;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public final class BoxedBlockTickDatum extends ChunkyBlockTickDatum {
    private final @Nonnull Chunk chunk;
    final PriorityQueue<IScheduledTick> queue = new PriorityQueue<>();
    final ObjectOpenHashSet<IScheduledTick> set = new ObjectOpenHashSet<>();

    public BoxedBlockTickDatum(@Nonnull final Chunk chunk) {
        this.chunk = chunk;
    }

    @Nonnull
    public Chunk getChunk() {
        return chunk;
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(final @Nonnull IScheduledTick tick){
        lock.lock();
        try {
            this.queue.add(tick);
        }finally {
            lock.unlock();
        }
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean isScheduled(final @Nonnull IScheduledTick tick){
        return set.contains(tick);
    }

    @Nonnull
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<IScheduledTick> query(final @Nonnull BlockPos pos) {
        final ObjectOpenHashSet<IScheduledTick> ticks = new ObjectOpenHashSet<>();
        for(final IScheduledTick tick:queue) if(tick.pos().equals(pos)) ticks.add(tick);
        return ticks;
    }

    /* -------------------------------
         Serialisation Area
       ------------------------------- */

    @Override
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS,ThreadType.GEO_MISC_DAEMON})
    protected void serialiseNBT(@Nonnull NBTTagCompound compound) {
        compound.setByte(KEY_TYPE,TYPE_BOXED);
        final long totalTime = chunk.getWorld().getTotalWorldTime();
        compound.setLong(KEY_BASE_TIME,totalTime);

        final NBTTagList updateLists = new NBTTagList();
        for(@Nonnull IScheduledTick tick:queue){
            final @Nonnull NBTTagCompound c = new NBTTagCompound();
            c.setInteger("i",Block.REGISTRY.getIDForObject(tick.block()));
            c.setLong("p",pack(tick.pos(),tick.priority()));
            c.setInteger("t",(int)(tick.triggeredTick()-totalTime));
            updateLists.appendTag(c);
        }
        compound.setTag("entries",updateLists);
    }

    @Override
    public void deserializeNBT(final @Nonnull NBTTagCompound nbt) {
        final int v = nbt.getInteger(KEY_VERSION);
        if(v <= 1){
            deserializeNBTV1(this.queue,nbt);
            this.set.addAll(this.queue);
        } else if(v > 2) throw new RuntimeException(
                String.format("Chunk (%s,%s) at world %s has more higher version nbt data! Chunk's Version: %s, Accepted Version: %s",
                        chunk.x,chunk.z,chunk.getWorld().provider.getDimension(),v,2));
        else {
            final byte type = nbt.getByte(KEY_TYPE);
            switch (type){
                case TYPE_BOXED:{
                    deserializeNBTV2(this.chunk.x,this.chunk.z,this.queue,nbt);
                    this.set.addAll(this.queue);
                    return;
                } case TYPE_PACKED:{
                    deserializeNBTV2Boxed(this.chunk,this.queue, PackedBlockTickDatum.deserializeNBTV2(nbt));
                    this.set.addAll(this.queue);
                }//其他类型的忽略
            }
        }
    }

    public static void deserializeNBTV1(final @Nonnull PriorityQueue<IScheduledTick> queue,final @Nonnull NBTTagCompound nbt){
        final NBTTagList updateLists = nbt.getTagList("block_updating_entries", Constants.NBT.TAG_COMPOUND);
        final long savedTime = nbt.getLong("totalWorldTime");
        for(@Nonnull NBTBase base:updateLists){
            final @Nonnull NBTTagCompound compound = (NBTTagCompound) base;
            final @Nonnull Block block = Block.REGISTRY.getObjectById(compound.getInteger("id"));
            final int[] posArray = compound.getIntArray("pos");
            final @Nonnull BlockPos pos = new BlockPos(posArray[0],posArray[1],posArray[2]);
            final int time = compound.getInteger("time");
            queue.add(IScheduledTick.of(block,pos,savedTime+time, TickPriority.DEFAULT));
        }
    }

    public static void deserializeNBTV2(final int chunkX,final int chunkZ,final @Nonnull PriorityQueue<IScheduledTick> queue,final @Nonnull NBTTagCompound nbt){
        final long time = nbt.getLong(KEY_BASE_TIME);
        final NBTTagList updateLists = nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for(@Nonnull NBTBase base:updateLists){
            final @Nonnull NBTTagCompound compound = (NBTTagCompound) base;
            final @Nonnull Block block = Block.REGISTRY.getObjectById(compound.getInteger("i"));
            final long triggeredTick = compound.getInteger("t") + time;
            final long packed = compound.getLong("p");
            final @Nonnull BlockPos pos = new BlockPos(
                    (chunkX<<4)+(int)((packed >>> 36) & 0xFL),
                    (int) (packed&0xFFFF_FFFFL) ,
                    (chunkZ<<4)+(int)((packed >>> 32) & 0xFL));
            final @Nonnull TickPriority priority = TickPriority.of((int)((packed>>>40)&0xFL));
            queue.add(IScheduledTick.of(block,pos,triggeredTick,priority));
        }
    }

    @SuppressWarnings("OctalInteger")
    public static void deserializeNBTV2Boxed(final @Nonnull Chunk chunk, final @Nonnull PriorityQueue<IScheduledTick> queue, final @Nullable PackedBlockTickQueue raw){
        if(raw == null) return;
        raw.forEach(t ->{
            final int x = (int)((t >>> 12) & 0xFL);
            final int y = (int)((t >>> 20) & 0xFFL);
            final int z = (int)((t >>> 16) & 0xFL);
            final int blockID = (int)(t & 0_7777L);
            final int priority = (int)((t >>> 28)&0xFL);
            final int delay = (int)(t>>>32);
            final long time = raw.baseTime + delay;
            queue.add(IScheduledTick.of(Block.getBlockById(blockID),
                    new BlockPos((chunk.x<<4)+x,y,(chunk.z<<4)+z),
                    time,
                    TickPriority.of(priority)));
        });
    }

    private static long pack(final @Nonnull BlockPos pos,final @Nonnull TickPriority priority){
        final int x = pos.getX() & 0xF;
        final int z = pos.getZ() & 0xF;
        return ((long) priority.ordinal() <<40) | ((long) x << 36) | ((long) z << 32) | Integer.toUnsignedLong(pos.getY());
    }

    /* -------------------------------
              Getter And Setter
       ------------------------------- */

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean markDirty(){
        this.chunk.markDirty();
        return dirty.compareAndSet(false,true);
    }
}
