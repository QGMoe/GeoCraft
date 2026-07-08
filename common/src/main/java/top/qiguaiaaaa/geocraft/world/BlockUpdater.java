/*
 * Copyright 2025 QiguaiAAAA
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
 * 版权所有 2025 QiguaiAAAA
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

package top.qiguaiaaaa.geocraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.capability.SchedulingTicksCapability;
import top.qiguaiaaaa.geocraft.configs.GeneralConfig;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;
import top.qiguaiaaaa.geocraft.util.misc.ExtendedNextTickListEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static top.qiguaiaaaa.geocraft.configs.GeneralConfig.BLOCK_UPDATER_MAX_UPDATES_BLOCK;
import static top.qiguaiaaaa.geocraft.configs.GeneralConfig.ENABLE_BLOCK_UPDATER;

/**
 * @since 0.1
 * @version 0.2.0-alpha.3
 * @author QiguaiAAAA
 */
@ThreadSafe
@MultiThread({ThreadType.CHUNK_IO_THREADS,ThreadType.MINECRAFT_SERVER})
public class BlockUpdater {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"block_updater");
    private static final Function<World,BlockUpdater> putBlockUpdateToCache = w -> w.hasCapability(SchedulingTicksCapability.BLOCK_UPDATER,null)?
            w.getCapability(SchedulingTicksCapability.BLOCK_UPDATER,null):null;
    private static final Comparator<ExtendedNextTickListEntry> compareByDistanceToPlayer =
            Comparator.comparingDouble(ExtendedNextTickListEntry::getDisSqToNearestPlayer);
    static final int MAX_UPDATE_NUM = BLOCK_UPDATER_MAX_UPDATES_BLOCK.getValue();
    static final Map<World,BlockUpdater> UPDATERS_CACHE = new ConcurrentHashMap<>();

    final ReentrantLock scheduleLock = new ReentrantLock();
    final Set<ExtendedNextTickListEntry> schedules = new LinkedHashSet<>();
    final ReentrantLock readyTickLock = new ReentrantLock();
    final LinkedList<ExtendedNextTickListEntry> readyTicks = new LinkedList<>();
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    final ExtendedNextTickListEntry[] ticksTempEntry = new ExtendedNextTickListEntry[100];
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    int tempLoc = 0;

    World world;
    Consumer<ExtendedNextTickListEntry> calcDistanceToClosestPlayer;

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void setWorld(@Nonnull World world) {
        this.world = world;
        calcDistanceToClosestPlayer = entry -> entry.calcDisSqToNearestPlayer(world);
    }

    public World getWorld() {
        return world;
    }

    @Nonnull
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public Set<ExtendedNextTickListEntry> getPendingTicks() {
        return schedules;
    }

    /**
     * @since 0.2.0
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(@Nonnull BlockPos pos,@Nonnull Block block,int delay){
        ExtendedNextTickListEntry entry = new ExtendedNextTickListEntry(world,pos,block,delay,0);
        schedules.add(entry);
    }

    /**
     * @since 0.2.0
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS})
    public void scheduleAll(@Nonnull Collection<ExtendedNextTickListEntry> entries){
        scheduleLock.lock();
        try {
            schedules.addAll(entries);
        }finally {
            scheduleLock.unlock();
        }
    }

    /**
     * @since 0.2.0
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void updateTick(){
        final long beginTime = System.currentTimeMillis();

        scheduleLock.lock();
        try {
            final Iterator<ExtendedNextTickListEntry> iterator = schedules.iterator();
            readyTickLock.lock(); //只要其他方法没有出现获取 scheduleLock 还要获取 readyTickLock 的情况，这里就是安全的
            try {
                while (iterator.hasNext()) {
                    final ExtendedNextTickListEntry entry = iterator.next();
                    if(world.getTotalWorldTime()<entry.scheduledTime){
                        continue;
                    }
                    iterator.remove();
                    readyTicks.add(entry);
                    if(readyTicks.size()>MAX_UPDATE_NUM) break;
                }
            }finally {
                readyTickLock.unlock();
            }
        }finally {
            scheduleLock.unlock();
        }

        if(GeneralConfig.SORT_UPDATE_TASKS_BY_DISTANCE_TO_PLAYERS.getValue()){
            readyTickLock.lock();
            try {
                readyTicks.forEach(calcDistanceToClosestPlayer);
                readyTicks.sort(compareByDistanceToPlayer);
            }finally {
                readyTickLock.unlock();
            }
        }

        final int maxTimeUsage = GeneralConfig.BLOCK_UPDATER_MAX_TIME_USAGE.getValue();
        readyTickLock.lock();
        try {
            while (!readyTicks.isEmpty()){
                tempLoc = -1;
                for (int j=0;j<ticksTempEntry.length;j++){
                    ticksTempEntry[++tempLoc] = readyTicks.poll();
                    if(readyTicks.isEmpty()) break;
                }
                readyTickLock.unlock(); //释放锁，避免触发区块加载导致死锁
                try {
                    while (tempLoc>-1){
                        final ExtendedNextTickListEntry entry = ticksTempEntry[tempLoc--];
                        if(!world.isBlockLoaded(entry.position)) continue;
                        final IBlockState state = world.getBlockState(entry.position);
                        if(isInvalidTickEntry(entry,state)) continue;
                        state.getBlock().updateTick(world,entry.position,state,world.rand);
                    }
                    if(maxTimeUsage <0) continue;
                    if(System.currentTimeMillis()-beginTime>maxTimeUsage) break;
                }finally {
                    readyTickLock.lock();
                }
            }
        }finally {
            scheduleLock.lock();
            try {
                schedules.addAll(readyTicks);
                readyTicks.clear();
            }finally {
                scheduleLock.unlock();
            }
            readyTickLock.unlock();
        }
    }

    /**
     * 当前的 NTE 是否需要丢弃，否则就会被更新
     * @param entry NTE 计划
     * @param curState 目前该位置的真正方块状态
     * @return 如果要被丢弃，返回 true
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    protected boolean isInvalidTickEntry(@Nonnull final NextTickListEntry entry,
                                         @Nonnull final IBlockState curState){
        return curState.getBlock() != entry.getBlock();
    }

    /**
     * @since 0.2.0
     */
    @Nonnull
    @MultiThread({ThreadType.CHUNK_IO_THREADS,ThreadType.MINECRAFT_SERVER})
    public Set<ExtendedNextTickListEntry> queryEntries(@Nonnull final BlockPos pos,final boolean doRemove){
        Set<ExtendedNextTickListEntry> set = null;
        Collection<ExtendedNextTickListEntry> collectionToQuery;
        ReentrantLock lock;
        for(byte i =(byte) 0;i<2;i++){
            final Iterator<ExtendedNextTickListEntry> iterator;
            if (i == 0) {
                collectionToQuery = schedules;
                lock = scheduleLock;
            } else {
                collectionToQuery = readyTicks;
                lock = readyTickLock;
            }
            lock.lock();
            try {
                iterator = collectionToQuery.iterator();
                while (iterator.hasNext()){
                    final ExtendedNextTickListEntry entry = iterator.next();
                    if(entry.position.equals(pos)){
                        if(doRemove) iterator.remove();
                        if(set == null) set = new HashSet<>();
                        set.add(entry);
                    }
                }
            }finally {
                lock.unlock();
            }
        }
        return set == null?Collections.emptySet():set;
    }

    /**
     * @since 0.2.0
     */
    @Nonnull
    @MultiThread({ThreadType.CHUNK_IO_THREADS,ThreadType.MINECRAFT_SERVER})
    public Set<ExtendedNextTickListEntry> queryEntries(@Nonnull final Chunk chunk,final boolean doRemove){
        return queryEntries(chunk.x<<4,0,chunk.z<<4,16,256,16,doRemove);
    }

    /**
     * @since 0.2.0
     */
    @Nonnull
    @MultiThread({ThreadType.CHUNK_IO_THREADS,ThreadType.MINECRAFT_SERVER})
    public Set<ExtendedNextTickListEntry> queryEntries(final int x,final int y,final int z,final int dx,final int dy,final int dz,final boolean doRemove){
        final int toX = x + dx,toY = y+dy,toZ=z+dz;
        Set<ExtendedNextTickListEntry> set = null;
        Collection<ExtendedNextTickListEntry> collectionToQuery;
        ReentrantLock lock;
        for(byte i =(byte) 0;i<2;i++){
            final Iterator<ExtendedNextTickListEntry> iterator;
            if (i == 0) {
                collectionToQuery = schedules;
                lock = scheduleLock;
            } else {
                collectionToQuery = readyTicks;
                lock = readyTickLock;
            }
            lock.lock();
            try {
                iterator = collectionToQuery.iterator();
                while (iterator.hasNext()){
                    final ExtendedNextTickListEntry entry = iterator.next();
                    if(MathUtil.inRange(entry.position.getX(),x,toX) && MathUtil.inRange(entry.position.getY(),y,toY) && MathUtil.inRange(entry.position.getZ(),z,toZ)){
                        if(doRemove) iterator.remove();
                        if(set == null) set = new HashSet<>();
                        set.add(entry);
                    }
                }
            }finally {
                lock.unlock();
            }
        }
        return set == null?Collections.emptySet():set;
    }

    @Nullable
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS})
    public static BlockUpdater getBlockUpdater(@Nonnull World world){
        return UPDATERS_CACHE.computeIfAbsent(world,putBlockUpdateToCache);
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void scheduleUpdate(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull Block block, int delay){
        if(!ENABLE_BLOCK_UPDATER.getValue()){
            world.scheduleUpdate(pos,block,delay);
            return;
        }
        final BlockUpdater updater = getBlockUpdater(world);
        if(updater == null) return;
        updater.schedule(pos,block,delay);
    }

    /**
     * @since 0.2.0
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS})
    public static void scheduleUpdates(@Nonnull World world,@Nonnull Set<ExtendedNextTickListEntry> entries){
        if(!ENABLE_BLOCK_UPDATER.getValue()){
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(()->{
                for(ExtendedNextTickListEntry entry:entries){
                    world.scheduleBlockUpdate(entry.position,entry.getBlock(),(int) (entry.scheduledTime-world.getTotalWorldTime()),entry.priority);
                }
            });
            return;
        }
        final BlockUpdater updater = getBlockUpdater(world);
        if(updater == null) return;
        updater.scheduleAll(entries);
    }

    @Nonnull
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static Set<ExtendedNextTickListEntry> getEntries(@Nonnull final World world){
        final BlockUpdater updater = getBlockUpdater(world);
        if(updater == null) return Collections.emptySet();
        return updater.getPendingTicks();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void onWorldTick(@Nonnull WorldServer world){
        if(!ENABLE_BLOCK_UPDATER.getValue()) return;
        final BlockUpdater updater = UPDATERS_CACHE.computeIfAbsent(world,putBlockUpdateToCache);
        if(updater == null) return;
        updater.updateTick();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void onServerStop(){
        UPDATERS_CACHE.clear();
    }
}
