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
import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.scheduler.BlockTickScheduler;
import moe.qingu.geocraft.configs.GeneralConfig;
import moe.qingu.geocraft.handler.CapabilityHandler;
import moe.qingu.geocraft.util.math.MathUtil;
import moe.qingu.geocraft.api.world.tick.validator.BlockTickValidator;
import moe.qingu.geocraft.api.world.tick.validator.IdentityBlockTickValidator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author QGMoe
 */
public abstract class ChunkyBlockTickScheduler<T extends ChunkyBlockTickDatum> extends BlockTickScheduler {
    protected final int maxUpdateNum;
    protected final Long2ObjectOpenHashMap<T> data = new Long2ObjectOpenHashMap<>();
    protected final ConcurrentLinkedQueue<T> dirties = new ConcurrentLinkedQueue<>();
    protected final LongOpenHashSet schedules = new LongOpenHashSet();
    protected BlockTickValidator validator = new IdentityBlockTickValidator(this);
    protected long[] temp = new long[0];

    protected ChunkyBlockTickScheduler(@Nonnull final World world) {
        super(world);
        this.maxUpdateNum = GeneralConfig.BLOCK_UPDATER_MAX_UPDATES_BLOCK.getValue();
    }

    @Nonnull
    @Override
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public final Set<IScheduledTick> query(final int x,final int y,final int z,final int dx,final int dy,final int dz) {
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
        for(int chunkX = minChunkX;chunkX<=maxChunkX;chunkX++)
            for(int chunkZ = minChunkZ;chunkZ<=maxChunkZ;chunkZ++)
                if(MathUtil.inRangeOpen(chunkX,minChunkX,maxChunkX) && MathUtil.inRangeOpen(chunkZ,minChunkZ,maxChunkZ)) collect(chunkX,chunkZ,minY,maxY,collector);
                else collect(chunkX,chunkZ,minX,maxX,minY,maxY,minZ,maxZ,collector);
        return collector;
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract void collect(final int chunkX,final int chunkZ,
                        final int minX,final int maxX,
                        final int minY,final int maxY,
                        final int minZ,final int maxZ,
                        final @Nonnull Set<IScheduledTick> collector);

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract void collect(final int chunkX,final int chunkZ,final int minY,final int maxY,final @Nonnull Set<IScheduledTick> collector);

    @Override
    public void setValidator(final @Nonnull BlockTickValidator validator) {
        this.validator = Objects.requireNonNull(validator);
    }

    @Nonnull
    @Override
    public final BlockTickValidator getValidator() {
        return validator;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public final T getDatum(final int cx, final int cz){
        T res = data.get(ChunkPos.asLong(cx,cz));
        if(res != null) return res;
        final Chunk chunk = world.getChunk(cx,cz);
        if(chunk.hasCapability(CapabilityHandler.BLOCK_TICK_DATUM,null)){
            data.put(ChunkPos.asLong(cx,cz),res = (T) chunk.getCapability(CapabilityHandler.BLOCK_TICK_DATUM,null));
            return res;
        }else return null;
    }

    @Nonnull
    public final Long2ObjectOpenHashMap<T> getData() {
        return data;
    }

    @Nonnull
    public final LongOpenHashSet getSchedules() {
        return schedules;
    }

    @Nonnull
    public final ConcurrentLinkedQueue<T> getDirties() {
        return dirties;
    }

    @Nonnull
    protected static ReportedException createReport(final @Nonnull Throwable t, final @Nonnull BlockPos pos, final @Nonnull IBlockState state){
        final @Nonnull CrashReport report = CrashReport.makeCrashReport(t, "Exception while BlockTickScheduler ticking a block");
        final @Nonnull CrashReportCategory category = report.makeCategory("Block being ticked");
        CrashReportCategory.addBlockInfo(category, pos.toImmutable(), state);
        return new ReportedException(report);
    }
}
