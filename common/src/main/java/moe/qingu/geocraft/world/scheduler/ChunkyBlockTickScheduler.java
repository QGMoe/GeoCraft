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

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.api.world.tick.scheduler.BlockTickScheduler;
import moe.qingu.geocraft.configs.GeneralConfig;
import moe.qingu.geocraft.handler.CapabilityHandler;
import moe.qingu.geocraft.util.math.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author QGMoe
 */
public final class ChunkyBlockTickScheduler extends BlockTickScheduler {
    private final int maxUpdateNum;
    private final Long2ObjectOpenHashMap<ChunkyBlockTickDatum> data = new Long2ObjectOpenHashMap<>();
    private final ConcurrentLinkedQueue<ChunkyBlockTickDatum> dirties = new ConcurrentLinkedQueue<>();
    private final LongOpenHashSet schedules = new LongOpenHashSet();
    private final Consumer consumer = new Consumer();
    private long[] temp = new long[0];

    public ChunkyBlockTickScheduler(final @Nonnull World world) {
        super(world);
        this.maxUpdateNum = GeneralConfig.BLOCK_UPDATER_MAX_UPDATES_BLOCK.getValue();
    }

    @Override
    @SuppressWarnings("OctalInteger")
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean schedule(final @Nonnull BlockPos pos, final @Nonnull Block block,final int delay,final @Nonnull TickPriority priority){
        if(pos.getY()>255 || pos.getY()<0) return false;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final ChunkyBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return false;
        final int blockID = Block.getIdFromBlock(block);
        if(blockID < 0 || blockID > 0_7777) return false;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        if(datum.isScheduled(cx,pos.getY(),cz,blockID)) return false;
        datum.schedule(world.getTotalWorldTime(), cx, pos.getY(), cz, blockID, delay, priority);
        schedules.add(ChunkPos.asLong(chunkX,chunkZ));
        if(!datum.isDirty() && datum.markDirty()){
            final Chunk chunk = world.getChunk(chunkX,chunkZ);
            chunk.markDirty();
            dirties.add(datum);
        }
        return true;
    }

    @Nonnull
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<IScheduledTick> query(@Nonnull final BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0) return Collections.emptySet();
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final ChunkyBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return Collections.emptySet();
        return datum.query(pos.getX() & 0xF,pos.getY(), pos.getZ() & 0xF);
    }

    @Nonnull
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<IScheduledTick> query(final int x,final int y,final int z,final int dx,final int dy,final int dz) {
        final int tx = x + dx;
        final int ty = y + dy;
        final int tz = z + dz;
        final int minX = Math.min(x,tx);
        final int minY = Math.min(y,ty);
        final int minZ = Math.min(z,tz);
        final int maxX = Math.max(x,tx);
        final int maxY = Math.max(y,ty);
        final int maxZ = Math.max(z,tz);
        final int minChunkX = minX>>4;
        final int minChunkZ = minZ>>4;
        final int maxChunkX = maxX>>4;
        final int maxChunkZ = maxZ>>4;
        final ObjectOpenHashSet<IScheduledTick> collector = new ObjectOpenHashSet<>();
        for(int chunkX = minChunkX;chunkX<=maxChunkX;chunkX++){
            for(int chunkZ = minChunkZ;chunkZ<=maxChunkZ;chunkZ++){
                if(MathUtil.inRangeOpen(chunkX,minChunkX,maxChunkX) && MathUtil.inRangeOpen(chunkZ,minChunkZ,maxChunkZ)) collect(chunkX,chunkZ,minY,maxY,collector);
                else {
                    final ChunkyBlockTickDatum datum = getDatum(chunkX,chunkZ);
                    if(datum == null) continue;
                    final int baseX = chunkX<<4;
                    final int baseZ = chunkZ<<4;
                    datum.queue.forEach(t ->{
                        final int tickX = baseX + (int) ((t >>> 4)&0xFL);
                        final int tickZ = baseZ + (int) ((t >>> 4)&0xFL);
                        final long tickY = (t>>>20) & 0xFF;
                        if(!MathUtil.inRangeClose(tickX,x,tx) || !MathUtil.inRangeClose(tickZ,z,tz) || tickY < minY || tickY > maxY) return;
                        collector.add(datum.queue.toScheduledTick(t));
                    });
                }
            }
        }
        return collector;
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void collect(final int chunkX,final int chunkZ,final int minY,final int maxY,final @Nonnull Set<IScheduledTick> collector){
        final ChunkyBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return;
        datum.queue.forEach(t ->{
            final long y = (t>>>20) & 0xFF;
            if(y < minY || y > maxY) return;
            collector.add(datum.queue.toScheduledTick(t));
        });
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void update(){
        final long beginTime = System.currentTimeMillis(),maxTime = GeneralConfig.BLOCK_UPDATER_MAX_TIME_USAGE.getValue();
        final long totalWorldTime = world.getTotalWorldTime();
        final long[] tempArr = new long[100];
        temp = schedules.toLongArray(temp);
        final int size = schedules.size();
        long count = 0;
        int i = 0;
        while (count < maxUpdateNum && i < size){
            final long pos = temp[i++];
            final ChunkyBlockTickDatum datum = data.get(pos);
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
                int n;
                do {
                    n = datum.queue.forNext(totalWorldTime,consumer,tempArr);
                    cot += n;
                    count += n;
                } while (n>0 && count < maxUpdateNum);
            }finally {
                datum.lock.unlock();
            }
            if(cot != 0 && datum.markDirty()){
                chunk.markDirty();
                dirties.add(datum);
            }
            if(datum.queue.isEmpty()) schedules.remove(pos);
            if(System.currentTimeMillis() - beginTime > maxTime) break;
        }
        consumer.chunk = null;
    }

    @Nullable
    public ChunkyBlockTickDatum getDatum(final int cx, final int cz){
        ChunkyBlockTickDatum res = data.get(ChunkPos.asLong(cx,cz));
        if(res != null) return res;
        final Chunk chunk = world.getChunk(cx,cz);
        if(chunk.hasCapability(CapabilityHandler.BLOCK_TICK_DATUM,null)){
            data.put(ChunkPos.asLong(cx,cz),res = chunk.getCapability(CapabilityHandler.BLOCK_TICK_DATUM,null));
            return res;
        }else return null;
    }

    @Nonnull
    public Long2ObjectOpenHashMap<ChunkyBlockTickDatum> getData() {
        return data;
    }

    @Nonnull
    public LongOpenHashSet getSchedules() {
        return schedules;
    }

    @Nonnull
    public ConcurrentLinkedQueue<ChunkyBlockTickDatum> getDirties() {
        return dirties;
    }

    private static class Consumer extends BlockTickConsumer{
        private final MBlockPos posContainer = new MBlockPos();
        private Chunk chunk;

        @Override
        public void consume(final int x,final int y,final int z, @Nonnull final Block block) {
            final @Nullable ExtendedBlockStorage storage = chunk.getBlockStorageArray()[y>>4];
            if(storage != Chunk.NULL_BLOCK_STORAGE){
                final IBlockState state = storage.get(x,y & 0xF,z);
                if(state.getBlock() != block) return;
                final World world = chunk.getWorld();
                posContainer.setPos((chunk.x << 4) + x, y, (chunk.z << 4) + z);
                try {
                    block.updateTick(world,posContainer,state,world.rand);
                } catch (final Throwable t) {
                    final @Nonnull CrashReport report = CrashReport.makeCrashReport(t, "Exception while BlockTickScheduler ticking a block");
                    final @Nonnull CrashReportCategory category = report.makeCategory("Block being ticked");
                    CrashReportCategory.addBlockInfo(category, posContainer.toImmutable(), state);
                    throw new ReportedException(report);
                }
            }
        }
    }
}
