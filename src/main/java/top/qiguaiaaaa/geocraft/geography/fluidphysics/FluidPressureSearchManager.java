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

package top.qiguaiaaaa.geocraft.geography.fluidphysics;

import io.netty.util.internal.ConcurrentSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.util.annotation.ConditionalThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTask;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.util.misc.FixedCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager.WorldPressureInfo.CREATE_WORLD_PRESSURE_INFO;
import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager.WorldPressureInfoFastutil.CREATE_WORLD_PRESSURE_INFO_FASTUTIL;
import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager.WorldQueueTaskInfo.CREATE_WORLD_QUEUE_TASK_INFO;
import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager.WorldQueueTaskInfoFastutil.CREATE_WORLD_QUEUE_TASK_INFO_FASTUTIL;
import static top.qiguaiaaaa.geocraft.util.MiscUtil.getValidWorld;

/**
 * 该类管理者压强系统的计算
 * @author QiguaiAAAA
 */
public final class FluidPressureSearchManager implements Runnable{
    public static final String THREAD_NAME = "FluidPressureSystem", CONFIG_CATEGORY_NAME = "pressure_system";

    /**
     * 用于压强系统访问世界的 Lock。当服务器线程需要进行高度敏感操作，需要暂停压强系统时，则需要获取此 Lock。
     * 压强系统也会在运行任务时持有该 Lock。
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    private static final ReentrantLock WorldReadLock = new ReentrantLock(FluidPhysicsConfig.USE_FAIR_LOCK_FOR_PRESSURE_SYSTEM.getValue());

    private static int MAX_UPDATE_TASKS, MAX_UPDATE_BLOCKS;
    private static final FluidPressureSearchManager INSTANCE = new FluidPressureSearchManager();
    private static final Function<WorldServer,ConcurrentLinkedQueue<Pair<BlockPos, Block>>> CREATE_QUEUE = k -> new ConcurrentLinkedQueue<>();
    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    private static final Collection<RunnableFluidPressureSearchTask> QUEUE_SUBMIT_TASKS = new FixedCollection<>(1030);
    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    private static ThreadPoolExecutor threadPool = null;
    @ConditionalThreadOnly({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    private static volatile Status status = Status.STOP;
    private static final AtomicLong totalTimes = new AtomicLong(0);
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    static final Map<WorldServer, WorldPressureInfo> worldMap = new ConcurrentHashMap<>(); //running的task、结果和running的task的poses
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    static final Map<WorldServer,WorldQueueTaskInfo> queueMap = new ConcurrentHashMap<>(); //等待被加入queue的task
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    static final Map<WorldServer,Queue<Pair<BlockPos,Block>>> queueToLoadPos = new ConcurrentHashMap<>();

    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    static Thread thread;

    static {
        readConfig();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    FluidPressureSearchManager(){}

    private static void readConfig(){
        MAX_UPDATE_TASKS = FluidPhysicsConfig.PRESSURE_MAX_TASKS_PER_TICK.getValue();
        MAX_UPDATE_BLOCKS = FluidPhysicsConfig.PRESSURE_MAX_UPDATES_PER_TICK.getValue();
    }

    /**
     * 要求压强系统以异步方式开始运行
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void asyncRun(){
        thread = new Thread(INSTANCE,THREAD_NAME);
        thread.start();
        readConfig();
        GeoCraft.getLogger().info("{} started in async",THREAD_NAME);
    }

    /**
     * 要求压强系统以同步方式运行。若此时压强系统正以异步方式运行，则异步线程会中止
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void syncRun(){
        clear();
        GeoCraft.getLogger().info("{} started in sync",THREAD_NAME);
        if(thread != null && thread.isAlive()){
            GeoCraft.getLogger().warn("{} is requested to run sync while async thread is running.",THREAD_NAME);
            thread.interrupt();
            thread = null;
        }
        readConfig();
        status = Status.RUNNING;
    }

    /**
     * 若压强系统以异步方式运行，则要求压强系统停止运行
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void asyncStop(){
        if(thread != null && thread.isAlive()){
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * 若压强系统以同步方式一下，则要求压强系统停止运行，理论上不应当在压强系统异步运行时调用，尽管这也会终止异步运行
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void syncStop(){
        status = Status.STOP;
        clear();
        if(thread != null && thread.isAlive()){
            GeoCraft.getLogger().warn("{} is requested to stop run sync while async thread is running.",THREAD_NAME);
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * 压强系统是否以异步方式运行
     * @return 若是，则返回true
     */
    public static boolean isRunningAsync(){
        return thread != null;
    }

    @Nullable
    public static Thread getThread() {
        return thread;
    }

    @Nonnull
    public static FluidPressureSearchManager getInstance() {
        return INSTANCE;
    }

    /**
     * 尝试获取世界的读取锁，以阻止压强系统在不当的时候读取世界状态
     * @param waitTime 等待时长，单位为毫秒
     * @return 是否成功暂停
     * @throws InterruptedException 若在暂停过程中线程被中断，则抛出此错误
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static boolean tryLockWorldRead(int waitTime) throws InterruptedException {
        if(!isRunningAsync()) return true;
        if(waitTime == 0){
            WorldReadLock.lock();
            return true;
        }
        return WorldReadLock.tryLock(waitTime,TimeUnit.MILLISECONDS);
    }

    /**
     * 如果持有世界读写锁，则释放，以允许压强系统运行
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void releaseWorldReadLock(){
        if(!isRunningAsync()) return;
        if(WorldReadLock.isHeldByCurrentThread()){
            WorldReadLock.unlock();
        }else GeoCraft.getLogger().warn("Someone tried resuming FluidPressureManager while not holding the lock!");
    }

    /**
     * 检测指定位置是否有任务在运行
     * @param world 世界
     * @param pos 位置
     * @return 若有，则返回true
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static boolean isTaskRunning(@Nonnull final World world,@Nonnull final BlockPos pos){
        final WorldServer validWorld = getValidWorld(world);
        if(validWorld == null) return false;
        final WorldPressureInfo runningInfo = getOrCreateWorldInfo(validWorld);
        final WorldQueueTaskInfo queueInfo = getOrCreateWorldQueue(validWorld);
        return runningInfo.isPosLocked(pos) || queueInfo.isPosQueued(pos);
    }

    /**
     * 获取指定位置的压强搜寻任务结果
     * @param world 世界
     * @param pos 位置
     * @return 若有结果，则返回{@link IFluidPressureSearchTaskResult}，否则返回null
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    @Nullable
    public static IFluidPressureSearchTaskResult getTaskResult(@Nonnull final World world,@Nonnull final BlockPos pos){
        final WorldServer validWorld = getValidWorld(world);
        if(validWorld == null) return null;
        final WorldPressureInfo info = getOrCreateWorldInfo(validWorld);
        return info.pullResult(pos);
    }

    /**
     * 在指定世界添加一个压强搜寻任务
     * @param world 世界
     * @param task 压强搜寻任务
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void addTask(@Nonnull final World world,@Nonnull final IFluidPressureSearchTask task){
        final WorldServer validWorld = getValidWorld(world);
        if(validWorld == null) return;
        final WorldQueueTaskInfo info = getOrCreateWorldQueue(validWorld);
        info.queueTask(task);
    }

    /**
     * 该方法会将等待更新的方块加入{@link BlockUpdater}的更新队列
     * @param world 需要tick的世界
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void onWorldTick(@Nonnull final WorldServer world){
        if(status == Status.STOP) return;
        final Queue<Pair<BlockPos,Block>> posesToLoad = queueToLoadPos.get(world);
        final int dispersion = FluidPhysicsConfig.PRESSURE_SCHEDULE_UPDATES_DISPERSION.getValue();
        if(posesToLoad != null){
            for(int i=0;i<MAX_UPDATE_BLOCKS;i++){
                final Pair<BlockPos, Block> task = posesToLoad.poll();
                if(task == null) break;
                BlockUpdater.scheduleUpdate(world,task.getLeft(),task.getRight(),world.rand.nextInt(dispersion));
            }
            posesToLoad.clear();
        }

        if(world.getTotalWorldTime()%FluidPhysicsConfig.PRESSURE_EMPTY_RESULTS_PERIOD.getValue() == 0){
            WorldPressureInfo info = getOrCreateWorldInfo(world);
            info.clearResults();
        }
    }

    /**
     * 若压强系统以同步方式运行，该方法会执行压强任务
     * @param event Server Tick事件
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void onServerTick(@Nonnull final TickEvent.ServerTickEvent event){
        if(isRunningAsync()) return;
        if(status == Status.STOP) return;
        try {
            core();
        }catch (InterruptedException ignore){}
    }

    /**
     * 异步执行时，压强系统线程的运行方法
     */
    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    @Override
    public void run() {  //自身线程调用
        status = Status.RUNNING;
        boolean running = true;
        if(FluidPhysicsConfig.PRESSURE_USING_THREAD_POOL.getValue()){
            if(threadPool != null && !threadPool.isShutdown()){
                GeoCraft.getLogger().warn("What's the f**k? Found existing thread pool! Shutdown it.");
                threadPool.shutdownNow();
            }
            threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(FluidPhysicsConfig.PRESSURE_THREAD_COUNT.getValue());
        }
        while (running){
            final long startTime = System.currentTimeMillis();
            try {
                core();
            }catch (InterruptedException ignored){
                running = false;
                break;
            }

            if(Thread.interrupted()){
                running = false;
                break;
            }
            final long usedTime = System.currentTimeMillis()-startTime;
            final int duration = FluidPhysicsConfig.PRESSURE_TICK_DURATION.getValue();
            if(usedTime<duration){
                try {
                    status = Status.SLEEPING;
                    Thread.sleep(duration-usedTime);
                    status = Status.RUNNING;
                } catch (InterruptedException ignored) {
                    running = false;
                }
            }
        }
        GeoCraft.getLogger().info("{} quited",THREAD_NAME);
        quit();
        status = Status.STOP;
    }

    /**
     * 压强系统的核心运行方法，若同步运行时则由{@link MinecraftServer}调用，若异步运行则由{@link FluidPressureSearchManager}线程调用
     * @throws InterruptedException 线程中断时调用
     */
    @ConditionalThreadOnly({ThreadType.FLUID_PRESSURE_MANAGER,ThreadType.MINECRAFT_SERVER})
    static void core() throws InterruptedException {
        totalTimes.incrementAndGet();
        final WorldServer[] loadedWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
        for(final WorldServer world:loadedWorld){
            final WorldPressureInfo info = getOrCreateWorldInfo(world);
            pushNewTasks(world,info);
            updateTasks(world,info);
            info.clearIfClean();
            if(totalTimes.get()%FluidPhysicsConfig.PRESSURE_DROP_EXCESS_TASKS_PERIOD.getValue() == 0){
                int size = info.getRunningTasks().size();
                final Deque<IFluidPressureSearchTask> deque = info.getRunningTasks();
                while (size>FluidPhysicsConfig.PRESSURE_CLEAN_UP_THRESHOLD.getValue()){
                    if(deque.isEmpty()) break;
                    final IFluidPressureSearchTask task = deque.poll();
                    if(task == null) break;
                    info.unlockPos(task);
                    size--;
                }
            }
        }
    }

    /**
     * 将指定世界处于{@link #queueMap}等待队列的任务加入运行任务队列{@link WorldPressureInfo#runningTasks}
     * @param world 世界
     * @param info 该世界的压强任务信息
     */
    @ConditionalThreadOnly({ThreadType.FLUID_PRESSURE_MANAGER,ThreadType.MINECRAFT_SERVER})
    static void pushNewTasks(final @Nonnull WorldServer world,final @Nonnull WorldPressureInfo info){  //自身线程调用
        final WorldQueueTaskInfo queueInfo = queueMap.get(world);

        if(queueInfo != null){
            queueInfo.queuedTasks.forEach(info::pushNewTask);
            queueInfo.clear();
        }
    }

    /**
     * 更新指定世界的压强任务
     * @param world 世界
     * @param info 该世界的压强信息
     * @throws InterruptedException 线程中断时抛出
     */
    @ConditionalThreadOnly({ThreadType.FLUID_PRESSURE_MANAGER,ThreadType.MINECRAFT_SERVER})
    static void updateTasks(@Nonnull final WorldServer world,@Nonnull final WorldPressureInfo info) throws InterruptedException { //自身线程调用
        if(isRunningAsync() && isRunningWithThreadPool()){
            updateTasksInPool(info);
            return;
        }
        final Deque<IFluidPressureSearchTask> queue = info.getRunningTasks();
        final int 颗粒度 = FluidPhysicsConfig.MINIMUM_GRANULARITY_OF_TASK_EXECUTION_VOLUME.getValue();
        for(int i=0;i<MAX_UPDATE_TASKS;){
            boolean locked = false;
            try { //在 try-finish 内保证锁被正确释放
                if(isRunningAsync()){
                    locked = checkInterruptStatus();
                }

                for(int j=0;j<颗粒度;i++,j++){ //每次锁定时最多取出 一定量 任务
                    if(queue.isEmpty()) break;
                    if(i>=MAX_UPDATE_TASKS) break;

                    final IFluidPressureSearchTask task = queue.poll();
                    if(task == null) continue;
                    if(task.isFinished()){
                        info.unlockPos(task);
                        continue;
                    }
                    final IBlockState beginState;
                    try {
                        if(!world.isBlockLoaded(task.getBeginPos())){
                            info.unlockPos(task);
                            continue;
                        }
                        beginState = world.getBlockState(task.getBeginPos());
                        if(!task.isEqualState(beginState)){
                            info.unlockPos(task);
                            continue;
                        }
                    }catch (IndexOutOfBoundsException e) { //Fastutil的多线程错误
                        info.pushNewTask(task);
                        continue;
                    }catch (Throwable e){
                        GeoCraft.getLogger().warn("When preparing loading pressure for fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getBeginPos(),world.provider.getDimension());
                        GeoCraft.getLogger().warn("FluidPressureSearchManager caught an unknown error:",e);
                        continue;
                    }

                    try {
                        final IFluidPressureSearchTaskResult res = task.search(world);
                        if(task.isFinished()){
                            if(res != null) info.putResultAndUnlock(task.getBeginPos(),res);
                            scheduleUpdate(world,task.getBeginPos(),beginState.getBlock());
                            task.finish();
                        }else{
                            queue.add(task);
                        }
                    }catch (IndexOutOfBoundsException e){  //Fastutil的多线程错误
                        info.pushNewTask(task);
                    }catch (Throwable e){
                        GeoCraft.getLogger().warn("When loading pressure for fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getBeginPos(),world.provider.getDimension());
                        GeoCraft.getLogger().warn("FluidPressureSearchManager caught an unknown error:",e);
                        task.cancel();
                        info.unlockPos(task);
                    }
                }
            }finally {
                if(locked){
                    WorldReadLock.unlock();
                }
            }
        }
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    static void updateTasksInPool(@Nonnull WorldPressureInfo info) throws InterruptedException{
        int maxUpdateTasksNum = MAX_UPDATE_TASKS;
        final Deque<IFluidPressureSearchTask> queue = info.getRunningTasks();
        final int maxUpdateNumPerLock = FluidPhysicsConfig.MINIMUM_GRANULARITY_OF_TASK_EXECUTION_VOLUME_WHEN_POOLS.getValue();
        while (maxUpdateTasksNum>0){
            if(queue.isEmpty()) break;
            int nextUpdateNum = Math.min(maxUpdateTasksNum,maxUpdateNumPerLock);
            maxUpdateTasksNum -= nextUpdateNum;

            boolean locked = false;
            try {
                locked = checkInterruptStatus();
                if(!locked && FluidPhysicsConfig.DO_NOT_RUN_TASKS_WHEN_FAILING_GETTING_READ_LOCK.getValue()){
                    GeoCraft.getLogger().warn("PressureSystem can't get Read Lock in time, skipping running tasks!");
                    continue;
                }

                while (nextUpdateNum>0){
                    nextUpdateNum--;
                    if(queue.isEmpty()) break;
                    final IFluidPressureSearchTask task = queue.poll();
                    if(task == null) continue;
                    if(task.isFinished()){
                        info.unlockPos(task);
                        continue;
                    }
                    try {
                        if(!info.world.isBlockLoaded(task.getBeginPos())){
                            info.unlockPos(task);
                            continue;
                        }
                        IBlockState beginState = info.world.getBlockState(task.getBeginPos());
                        if(!task.isEqualState(beginState)){
                            info.unlockPos(task);
                            continue;
                        }
                    }catch (IndexOutOfBoundsException e){
                        info.pushNewTask(task);
                        continue;
                    }catch (Throwable e){
                        GeoCraft.getLogger().warn("When preparing loading pressure for fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getBeginPos(),info.world.provider.getDimension());
                        GeoCraft.getLogger().warn("FluidPressureSearchManager caught an unknown error:",e);
                        continue;
                    }

                    QUEUE_SUBMIT_TASKS.add(new RunnableFluidPressureSearchTask(info,task));
                }
                threadPool.invokeAll(QUEUE_SUBMIT_TASKS);
                QUEUE_SUBMIT_TASKS.clear();
            }finally {
                if(locked) WorldReadLock.unlock();
            }

        }
    }

    /**
     * 尝试获得世界读写锁
     * @author QiguaiAAAA
     * @return 当获得锁或为单线程运行时，返回 true
     * @throws InterruptedException 等待过程中若线程中断，则抛出
     */
    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    static boolean checkInterruptStatus() throws InterruptedException {
        if(!isRunningAsync()) return true;
        status = Status.INTERRUPT;
        final boolean locked;
        if(FluidPhysicsConfig.PAUSE_TIME_FOR_PRESSURE_SYSTEM.getValue() == 0){
            WorldReadLock.lock();
            locked = true;
        }else locked = WorldReadLock.tryLock(FluidPhysicsConfig.PAUSE_TIME_FOR_PRESSURE_SYSTEM.getValue(),TimeUnit.MILLISECONDS);
        status = Status.RUNNING;
        if(!locked){
            GeoCraft.getLogger().warn("FluidPressureSystem resumed after being paused {} ms because reaching the max pause time.",FluidPhysicsConfig.PAUSE_TIME_FOR_PRESSURE_SYSTEM.getValue());
        }
        return locked;
    }

    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    static boolean isRunningWithThreadPool(){
        return threadPool != null;
    }

    /**
     * 将指定位置列入计划更新
     * @param world 世界
     * @param pos 更新位置
     * @param block 预期方块
     * @author QiguaiAAAA
     */
    @ConditionalThreadOnly({ThreadType.FLUID_PRESSURE_MANAGER,ThreadType.MINECRAFT_SERVER})
    static void scheduleUpdate(WorldServer world,BlockPos pos,Block block){
        Queue<Pair<BlockPos, Block>> scheduleSet = queueToLoadPos.computeIfAbsent(world, CREATE_QUEUE);
        scheduleSet.add(Pair.of(pos,block));
    }

    /**
     * 当压强线程退出的时候，进行退出操作
     * @author QiguaiAAAA
     */
    @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
    static void quit(){  //自身线程调用
        clear();
        ThreadLocalHelper.clear();
        if(threadPool != null && !threadPool.isShutdown()){
            threadPool.shutdownNow();
            threadPool = null;
        }
        thread = null;
    }

    /**
     * 清除当前的全部数据
     */
    static void clear(){
        worldMap.clear();
        queueMap.clear();
        queueToLoadPos.clear();
        totalTimes.set(0);
    }

    /**
     * 返回指定世界的{@link WorldPressureInfo}<br/>
     * 该方法会被{@link MinecraftServer}和{@link FluidPressureSearchManager}多线程调用
     * @param world 世界，需要为{@link WorldServer}类型
     * @return 当前世界的压强信息
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    @Nonnull
    static WorldPressureInfo getOrCreateWorldInfo(@Nonnull WorldServer world){
        if(FluidPhysicsConfig.USE_COMPRESSED_COORDINATE.getValue()){
            return worldMap.computeIfAbsent(world,CREATE_WORLD_PRESSURE_INFO_FASTUTIL);
        }
        return worldMap.computeIfAbsent(world,CREATE_WORLD_PRESSURE_INFO);
    }

    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    @Nonnull
    static WorldQueueTaskInfo getOrCreateWorldQueue(@Nonnull WorldServer world){
        if(FluidPhysicsConfig.USE_COMPRESSED_COORDINATE.getValue()){
            return queueMap.computeIfAbsent(world,CREATE_WORLD_QUEUE_TASK_INFO_FASTUTIL);
        }
        return queueMap.computeIfAbsent(world,CREATE_WORLD_QUEUE_TASK_INFO);
    }

    /**
     * 世界压强信息<br/>
     * 该类会被{@link MinecraftServer}和{@link FluidPressureSearchManager}多线程调用
     * @author QiguaiAAAA
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER,ThreadType.FLUID_PRESSURE_TASKS})
    static class WorldPressureInfo{
        static final Function<WorldServer,WorldPressureInfo> CREATE_WORLD_PRESSURE_INFO = WorldPressureInfo::new;

        public final WorldServer world;

        private WorldPressureInfo(WorldServer world){
            this.world = world;
        }

        public final Deque<IFluidPressureSearchTask> runningTasks = new ConcurrentLinkedDeque<>();
        private final Map<BlockPos,IFluidPressureSearchTaskResult> taskResults = new ConcurrentHashMap<>();
        private final Set<BlockPos> runningTaskLocks = new ConcurrentSet<>();

        public final Deque<IFluidPressureSearchTask> getRunningTasks() {
            return runningTasks;
        }

        public void clearResults(){
            taskResults.clear();
        }

        public void pushNewTask(IFluidPressureSearchTask task){
            runningTasks.add(task);
            runningTaskLocks.add(task.getBeginPos());
        }

        public void unlockPos(IFluidPressureSearchTask task){
            runningTaskLocks.remove(task.getBeginPos());
        }

        public boolean isPosLocked(@Nonnull BlockPos pos){
            return runningTaskLocks.contains(pos);
        }

        public void putResultAndUnlock(@Nonnull BlockPos pos, @Nonnull IFluidPressureSearchTaskResult result){
            taskResults.put(pos,result);
            runningTaskLocks.remove(pos);
        }

        @Nullable
        @ThreadOnly(ThreadType.MINECRAFT_SERVER)
        public IFluidPressureSearchTaskResult pullResult(@Nonnull BlockPos pos){
            runningTaskLocks.remove(pos);
            return taskResults.remove(pos);
        }

        public void clearIfClean(){
            if(runningTasks.isEmpty())
                runningTaskLocks.clear();
        }
    }

    static class WorldPressureInfoFastutil extends WorldPressureInfo{
        static final Function<WorldServer,WorldPressureInfoFastutil> CREATE_WORLD_PRESSURE_INFO_FASTUTIL = WorldPressureInfoFastutil::new;

        private WorldPressureInfoFastutil(WorldServer world) {
            super(world);
        }

        private final Long2ObjectMap<IFluidPressureSearchTaskResult> taskResults = new Long2ObjectOpenHashMap<>();
        private final LongSet runningTaskLocks = new LongOpenHashSet();

        @Override
        public synchronized void clearResults() {
            taskResults.clear();
        }

        @Override
        public synchronized void pushNewTask(IFluidPressureSearchTask task) {
            runningTasks.add(task);
            runningTaskLocks.add(task.getBeginPos().toLong());
        }

        @Override
        public synchronized void unlockPos(IFluidPressureSearchTask task) {
            runningTaskLocks.remove(task.getBeginPos().toLong());
        }

        @Override
        public synchronized boolean isPosLocked(@Nonnull BlockPos pos) {
            return runningTaskLocks.contains(pos.toLong());
        }

        @Override
        public synchronized void putResultAndUnlock(@Nonnull BlockPos pos, @Nonnull IFluidPressureSearchTaskResult result) {
            taskResults.put(pos.toLong(),result);
            runningTaskLocks.remove(pos.toLong());
        }

        @Nullable
        @Override
        public synchronized IFluidPressureSearchTaskResult pullResult(@Nonnull BlockPos pos) {
            return taskResults.remove(pos.toLong());
        }

        @Override
        public synchronized void clearIfClean() {
            if(runningTasks.isEmpty())
                runningTaskLocks.clear();
        }
    }

    /**
     * 世界压强任务等待信息<br/>
     * 该类会被{@link MinecraftServer}和{@link FluidPressureSearchManager}多线程调用
     * @author QiguaiAAAA
     */
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_MANAGER})
    static class WorldQueueTaskInfo{
        static final Function<WorldServer,WorldQueueTaskInfo> CREATE_WORLD_QUEUE_TASK_INFO = k-> new WorldQueueTaskInfo();
        public final Set<IFluidPressureSearchTask> queuedTasks = new ConcurrentSet<>();
        private final Set<BlockPos> queuedTaskLocks = new ConcurrentSet<>();

        public void queueTask(IFluidPressureSearchTask task){
            queuedTasks.add(task);
            queuedTaskLocks.add(task.getBeginPos());
        }

        public void clear(){
            queuedTasks.clear();
            queuedTaskLocks.clear();
        }

        public boolean isPosQueued(@Nonnull BlockPos pos){
            return queuedTaskLocks.contains(pos);
        }
    }

    static class WorldQueueTaskInfoFastutil extends WorldQueueTaskInfo{
        static final Function<WorldServer,WorldQueueTaskInfo> CREATE_WORLD_QUEUE_TASK_INFO_FASTUTIL = k-> new WorldQueueTaskInfo();
        private final LongSet queuedTaskLocks = new LongOpenHashSet();

        @Override
        public synchronized void queueTask(IFluidPressureSearchTask task) {
            queuedTasks.add(task);
            queuedTaskLocks.add(task.getBeginPos().toLong());
        }

        @Override
        public synchronized void clear() {
            queuedTasks.clear();
            queuedTaskLocks.clear();
        }

        @Override
        public boolean isPosQueued(@Nonnull BlockPos pos) {
            return queuedTaskLocks.contains(pos.toLong());
        }
    }

    /**
     * {@link FluidPressureSearchManager}的运行状态
     * @author QiguaiAAAA
     */
    enum Status{
        /**
         * {@link FluidPressureSearchManager}处于停止状态，任何任务都不会运行
         */
        STOP,
        /**
         * {@link FluidPressureSearchManager}处于正常运行状态
         */
        RUNNING,
        SLEEPING,
        /**
         * {@link FluidPressureSearchManager}处于运行状态，但被要求暂停运行
         */
        @Deprecated
        INTERRUPT_REQUESTED,
        /**
         * {@link FluidPressureSearchManager}处于暂停状态
         */
        INTERRUPT
    }

    private static class RunnableFluidPressureSearchTask implements Callable<Void>{

        private final WorldPressureInfo info;
        private final IFluidPressureSearchTask task;

        @ThreadOnly(ThreadType.FLUID_PRESSURE_MANAGER)
        public RunnableFluidPressureSearchTask(@Nonnull WorldPressureInfo info,@Nonnull IFluidPressureSearchTask task) {
            this.info = info;
            this.task = task;
        }

        @Override
        @ThreadOnly(ThreadType.FLUID_PRESSURE_TASKS)
        public Void call() {
            try {
                IFluidPressureSearchTaskResult res = task.search(info.world);
                if(task.isFinished()){
                    if(res != null){
                        info.putResultAndUnlock(task.getBeginPos(),res);
                    }
                    scheduleUpdate(info.world,task.getBeginPos(),task.getBeginState().getBlock());
                    task.finish();
                }else{
                    info.getRunningTasks().add(task);
                }
            }catch (IndexOutOfBoundsException e){  //Fastutil的多线程错误
                task.cancel();
                info.unlockPos(task);
            }catch (Throwable e){
                GeoCraft.getLogger().warn("When loading pressure for fluid {} at {} in world {},",
                        task.getFluid().getUnlocalizedName(),task.getBeginPos(),info.world.provider.getDimension());
                GeoCraft.getLogger().warn("FluidPressureSearchManager caught an error:",e);
                task.cancel();
                info.unlockPos(task);
            }
            return null;
        }
    }
}
