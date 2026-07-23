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

import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.configs.GeneralConfig;
import moe.qingu.geocraft.util.math.MathUtil;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * @author QGMoe
 */
public final class PackedBlockTickScheduler extends ChunkyBlockTickScheduler<PackedBlockTickDatum> {
    private final Consumer consumer = new Consumer();

    public PackedBlockTickScheduler(final @Nonnull World world) {
        super(world);
    }

    @Override
    @SuppressWarnings("OctalInteger")
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean schedule(final @Nonnull BlockPos pos, final @Nonnull Block block,final int delay,final @Nonnull TickPriority priority){
        if(pos.getY()>255 || pos.getY()<0) return false;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final PackedBlockTickDatum datum = getDatum(chunkX,chunkZ);
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
        final PackedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return Collections.emptySet();
        return datum.query(pos.getX() & 0xF,pos.getY(), pos.getZ() & 0xF);
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void collect(final int chunkX,final int chunkZ,
                        final int minX,final int maxX,
                        final int minY,final int maxY,
                        final int minZ,final int maxZ,
                        final @Nonnull Set<IScheduledTick> collector){
        final PackedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return;
        final int baseX = chunkX<<4;
        final int baseZ = chunkZ<<4;
        datum.queue.forEach(t ->{
            final int tickX = baseX + (int) ((t >>> 12)&0xFL);
            final int tickZ = baseZ + (int) ((t >>> 16)&0xFL);
            final long tickY = (t>>>20) & 0xFFL;
            if(!MathUtil.inRangeClose(tickX,minX,maxX) || !MathUtil.inRangeClose(tickZ,minZ,maxZ) || tickY < minY || tickY > maxY) return;
            collector.add(datum.queue.toScheduledTick(t));
        });
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void collect(final int chunkX,final int chunkZ,final int minY,final int maxY,final @Nonnull Set<IScheduledTick> collector){
        final PackedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return;
        datum.queue.forEach(t ->{
            final long y = (t>>>20) & 0xFFL;
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
            final PackedBlockTickDatum datum = data.get(pos);
            if(datum == null) {
                schedules.remove(pos);
                continue;
            }
            final int z = (int) (pos>>Integer.SIZE);
            final int x = (int) pos;
            int cot = 0;
            final Chunk chunk = world.getChunk(x,z);
            consumer.ebs = chunk.getBlockStorageArray();
            consumer.baseX = chunk.x <<4;
            consumer.baseZ = chunk.z <<4;
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
        consumer.ebs = null;
    }

    private class Consumer extends PackedBlockTickConsumer {
        private final MBlockPos posContainer = new MBlockPos();
        private int baseX;
        private int baseZ;
        private ExtendedBlockStorage[] ebs;

        @Override
        public void consume(final int x,final int y,final int z, @Nonnull final Block block) {
            final @Nullable ExtendedBlockStorage storage = ebs[y>>4];
            if(storage != Chunk.NULL_BLOCK_STORAGE){
                final IBlockState state = storage.get(x,y & 0xF,z);
                posContainer.setPos(baseX + x, y, baseZ + z);
                if(PackedBlockTickScheduler.this.validator.accepts(posContainer,block,state)) return;
                final World world = PackedBlockTickScheduler.this.world;
                try {
                    block.updateTick(world,posContainer,state,world.rand);
                } catch (final Throwable t) {
                    throw createReport(t,posContainer,state);
                }
            }
        }
    }
}
