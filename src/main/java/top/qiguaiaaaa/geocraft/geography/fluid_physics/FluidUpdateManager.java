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

package top.qiguaiaaaa.geocraft.geography.fluid_physics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluid_physics.task.update.IFluidUpdateTask;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

import static top.qiguaiaaaa.geocraft.util.MiscUtil.getValidWorld;

/**
 * 流体更新管理器，为保证最佳效果所有流体的更新应该走这个管理器
 * @author QiguaiAAAA
 * @since 0.1
 */
public final class FluidUpdateManager {
    private static int MAX_UPDATE_NUM;
    static final Map<WorldServer, Pair<PriorityQueue<IFluidUpdateTask>,PriorityQueue<IFluidUpdateTask>>> updateTaskQueuesMap = new HashMap<>();
    static final Map<WorldServer, Set<BlockPos>> updateTaskLock = new HashMap<>();

    static {
        MAX_UPDATE_NUM = FluidPhysicsConfig.FLUID_UPDATER_MAX_TASKS_PER_TICK.getValue();
    }

    public static void addTask(@Nonnull World world,@Nonnull IFluidUpdateTask task){
        WorldServer validWorld = getValidWorld(world);
        if(validWorld == null) return;
        final Pair<PriorityQueue<IFluidUpdateTask>,PriorityQueue<IFluidUpdateTask>> queuePair = getOrCreateQueues(validWorld);
        final Set<BlockPos> locks = getOrCreateSet(validWorld);
        if(locks.contains(task.getPos())){
            return;
        }
        if(task.getFluid().getDensity()>=0){
            queuePair.getLeft().add(task);
        }else{
            queuePair.getRight().add(task);
        }
        locks.add(task.getPos());
    }

    public static void onServerStop(){
        updateTaskQueuesMap.clear();
        updateTaskLock.clear();
        MAX_UPDATE_NUM = FluidPhysicsConfig.FLUID_UPDATER_MAX_TASKS_PER_TICK.getValue();
    }

    public static void onWorldTick(@Nonnull WorldServer world){
        Pair<PriorityQueue<IFluidUpdateTask>,PriorityQueue<IFluidUpdateTask>> queues = getOrCreateQueues(world);
        updateTasks(world,queues.getLeft());
        updateTasks(world,queues.getRight());
    }

    static void updateTasks(@Nonnull final WorldServer world,@Nonnull final PriorityQueue<IFluidUpdateTask> queue){
        final Set<BlockPos> locks = getOrCreateSet(world);
        final long beginTime = System.nanoTime(),maxTime = FluidPhysicsConfig.FLUID_UPDATER_MAX_TIME_USAGE.getValue();
        final int cleanPeriod = FluidPhysicsConfig.FLUID_UPDATER_CLEAN_PERIOD.getValue()-1;
        for(int i=0;i<MAX_UPDATE_NUM;i++){
            if(queue.isEmpty()) break;
            final IFluidUpdateTask task = queue.poll();
            if(task == null) continue;
            locks.remove(task.getPos());
            if(!world.isBlockLoaded(task.getPos())) continue;
            IBlockState state = world.getBlockState(task.getPos());
            if(state.getBlock() != task.getBlock()) continue;
            try {
                task.onUpdate(world,state,world.rand);
            }catch (Throwable e){
                GeoCraft.getLogger().warn("When updating fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getPos(),world.provider.getDimension());
                GeoCraft.getLogger().warn("FluidUpdateManager caught an error:",e);
                try {
                    task.onFailure(world,state,world.rand);
                }catch (Throwable e2){
                    GeoCraft.getLogger().error("When restoring failure of fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getPos(),world.provider.getDimension());
                    GeoCraft.getLogger().error("FluidUpdateManager caught an error:",e2);
                }
            }
            if((i & 127) == 0){
                if(System.nanoTime() - beginTime > maxTime) break;
            }
        }
        if(FluidPhysicsConfig.FLUID_UPDATER_DROP_EXCESS_TASKS.getValue() && (world.getTotalWorldTime() & cleanPeriod) == 0){
            while (!queue.isEmpty()){
                final IFluidUpdateTask task = queue.poll();
                if(task == null) continue;
                locks.remove(task.getPos());
                if(!world.isBlockLoaded(task.getPos())) continue;
                IBlockState state = world.getBlockState(task.getPos());
                if(state.getBlock() != task.getBlock()) continue;
                try {
                    task.onFailure(world,state,world.rand);
                }catch (Throwable e){
                    GeoCraft.getLogger().warn("When updating fluid {} at {} in world {},",task.getFluid().getUnlocalizedName(),task.getPos(),world.provider.getDimension());
                    GeoCraft.getLogger().warn("FluidUpdateManager caught an error:",e);
                }
            }
            locks.clear();
        }
    }

    static Pair<PriorityQueue<IFluidUpdateTask>,PriorityQueue<IFluidUpdateTask>> getOrCreateQueues(@Nonnull WorldServer world){
        return updateTaskQueuesMap.computeIfAbsent(world,CREATE_QUEUES);
    }

    static Set<BlockPos> getOrCreateSet(@Nonnull WorldServer world){
        return updateTaskLock.computeIfAbsent(world,CREATE_SET);
    }

    private static final Function<WorldServer,Pair<PriorityQueue<IFluidUpdateTask>,PriorityQueue<IFluidUpdateTask>>> CREATE_QUEUES =
            k->Pair.of(
                    new PriorityQueue<>(Comparator.comparingInt(value -> value.getPos().getY())) //小根堆，向下流的流体放这里
                    ,new PriorityQueue<>((v1,v2) -> v2.getPos().getY()-v1.getPos().getY()) //大根堆，向上流的流体放这里
            );

    private static final Function<WorldServer,Set<BlockPos>> CREATE_SET = k -> new HashSet<>();
}
