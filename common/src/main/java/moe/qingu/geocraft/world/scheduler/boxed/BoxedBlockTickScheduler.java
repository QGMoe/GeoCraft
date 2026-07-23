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

import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import moe.qingu.geocraft.configs.GeneralConfig;
import moe.qingu.geocraft.util.math.MathUtil;
import moe.qingu.geocraft.world.scheduler.ChunkyBlockTickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @since 0.1
 * @version 0.2.0-alpha.3
 * @author QiguaiAAAA
 */
public final class BoxedBlockTickScheduler extends ChunkyBlockTickScheduler<BoxedBlockTickDatum> {

    public BoxedBlockTickScheduler(final @Nonnull World world){
        super(world);
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public boolean schedule(@Nonnull final BlockPos pos,
                            @Nonnull final Block block,
                            final int delay,
                            @Nonnull final TickPriority priority) {
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final @Nullable BoxedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return false;
        final IScheduledTick tick = IScheduledTick.of(block,pos,this.world.getTotalWorldTime()+delay,priority);
        if(datum.isScheduled(tick)) return false;
        datum.schedule(tick);
        schedules.add(ChunkPos.asLong(chunkX,chunkZ));
        if(!datum.isDirty() && datum.markDirty()) dirties.add(datum);
        return true;
    }

    @Nonnull
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<IScheduledTick> query(@Nonnull final BlockPos pos) {
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final BoxedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return Collections.emptySet();
        return datum.query(pos);
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void collect(final int chunkX,final int chunkZ,
                        final int minX,final int maxX,
                        final int minY,final int maxY,
                        final int minZ,final int maxZ,
                        @Nonnull final Set<IScheduledTick> collector) {
        final BoxedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return;
        for(final IScheduledTick tick:datum.queue)
            if(MathUtil.inRangeClose(tick.pos().getX(),minX,maxX) && MathUtil.inRangeClose(tick.pos().getZ(),minZ,maxZ) && MathUtil.inRangeClose(tick.pos().getY(),minY,maxY))
                collector.add(tick);
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void collect(final int chunkX,final int chunkZ,final int minY,final int maxY,final @Nonnull Set<IScheduledTick> collector){
        final BoxedBlockTickDatum datum = getDatum(chunkX,chunkZ);
        if(datum == null) return;
        for(final IScheduledTick tick:datum.queue) if(MathUtil.inRangeClose(tick.pos().getY(),minY,maxY)) collector.add(tick);
    }

    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void update() {
        final long beginTime = System.currentTimeMillis(),maxTime = GeneralConfig.BLOCK_UPDATER_MAX_TIME_USAGE.getValue();
        final long totalWorldTime = world.getTotalWorldTime();
        final IScheduledTick[] tempArr = new IScheduledTick[100];
        temp = schedules.toLongArray(temp);
        final int size = schedules.size();
        long count = 0;
        int i = 0;
        while (count < maxUpdateNum && i < size){
            final long pos = temp[i++];
            final BoxedBlockTickDatum datum = data.get(pos);
            if(datum == null) {
                schedules.remove(pos);
                continue;
            }
            int cot = 0;
            datum.lock.lock();
            final Chunk chunk = datum.getChunk();
            final ExtendedBlockStorage[] ebs= chunk.getBlockStorageArray();
            try {
                int n = 0;
                do {
                    while (!datum.queue.isEmpty() && n < tempArr.length && datum.queue.peek().triggeredTick() <= totalWorldTime) tempArr[n++] = datum.queue.poll();
                    cot += n;
                    count += n;
                    int j = n;
                    while (j>0) consume(chunk,ebs,tempArr[--j]);
                } while (n>0 && count < maxUpdateNum);
            }finally {
                datum.lock.unlock();
            }
            if(cot != 0 && datum.markDirty()) dirties.add(datum);
            if(datum.queue.isEmpty()) schedules.remove(pos);
            if(System.currentTimeMillis() - beginTime > maxTime) break;
        }
    }

    private void consume(final @Nonnull Chunk chunk,
                         final @Nonnull ExtendedBlockStorage[] ebs,
                         final @Nonnull IScheduledTick tick){
        final BlockPos position = tick.pos();
        final @Nonnull IBlockState state = getBlockState(chunk,ebs,position);

        if(validator.accepts(position,tick.block(),state)) return;
        try {
            state.getBlock().updateTick(world,position,state,world.rand);
        } catch (final Throwable t) {
            throw createReport(t,position,state);
        }
    }

    private static @Nonnull IBlockState getBlockState(final @Nonnull Chunk chunk,final @Nonnull ExtendedBlockStorage[] ebs,final @Nonnull BlockPos pos){
        final int y = pos.getY();
        if(y<0 || y > 255) return chunk.getBlockState(pos);
        else {
            final ExtendedBlockStorage storage = ebs[y>>4];
            if(storage == Chunk.NULL_BLOCK_STORAGE) return Blocks.AIR.getDefaultState();
            else return storage.get(pos.getX() & 0xF,y &0xF,pos.getZ() &0xF);
        }
    }

    @Deprecated
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void scheduleUpdate(@Nonnull final World world,@Nonnull final BlockPos pos,@Nonnull final Block block,final int delay){
        schedule(world,pos,block,delay);
    }
}
