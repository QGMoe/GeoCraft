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

package moe.qingu.geocraft.api.fluidphysics.task.scheduler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import moe.qingu.geocraft.api.GeoCraftAPI;
import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskRegistry;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * 流体任务调度器，用于管理一个维度的流体任务 {@link IFluidTask} 的调度
 * 一个位置一个时间只能最多有一个任务，因此 {@link #query(BlockPos)} 返回的是单个任务
 * 流体任务的发布不应该在流动逻辑期间。流体首先是方块，所以需要先通过计划刻更新方块，
 * 然后在{@link net.minecraft.block.Block#updateTick(World, BlockPos, IBlockState, Random)} 中或其他地方发布流体任务
 * 这是因为流体任务的调度方式和方块计划刻并不一样，流体任务的调度的顺序、特性之类的有很大不同。例如，流体任务没有优先级的概念，但计划刻任务有。同样流体任务也没有延时多少刻的概念。
 * 但流体不能只用流体任务调度器更新，因为首先，调度器调用流体任务期间直接用 {@link #schedule(BlockPos, IFluidTask, Fluid)} 可能引发 {@link java.util.ConcurrentModificationException}
 * 其次，流体方块同样需要计划刻的延迟功能，这直接影响到流体的流动速度和粘度。所以流体需要先通过方块更新再通过流体调度器更新。
 * 流体当然也可以只用方块更新，但是这意味着更新顺序之类的不会根据流体本身的特性定制，这在拟真流体流动的情况下会是很大的问题。
 * @since GeoCraft-API 0.3.4
 * @author QGMoe
 */
public abstract class FluidTaskScheduler implements ICapabilityProvider {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraftAPI.MODID,"fluid_task_scheduler");
    public static @CapabilityInject(FluidTaskScheduler.class) Capability<FluidTaskScheduler> FLUID_TASK_SCHEDULER;
    private static final Int2ObjectOpenHashMap<FluidTaskScheduler> schedulers = new Int2ObjectOpenHashMap<>();
    protected final World world;

    protected FluidTaskScheduler(final @Nonnull World world) {
        this.world = world;
    }

    /**
     * 计划一个流体任务
     * @param pos 位置
     * @param task 流体任务，必须已经在 {@link FluidTaskRegistry} 中注册
     * @param fluid 流体，最好是 {@link net.minecraftforge.fluids.FluidRegistry} 中已经注册的实例
     * @return 是否成功计划
     * @apiNote 任务需要根据位置去重
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract boolean schedule(final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid);

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract void update();

    @Nullable
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract IFluidTask query(final @Nonnull BlockPos pos);

    /* ------------------
            Getter
       ------------------ */

    @Nonnull
    public final World getWorld(){
        return world;
    }

    /* ------------------
           Capability
       ------------------ */

    @Override
    public final boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == FLUID_TASK_SCHEDULER;
    }

    @Nullable
    @Override
    public final <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == FLUID_TASK_SCHEDULER ? FLUID_TASK_SCHEDULER.cast(this):null;
    }

    /* ------------------
            Static
       ------------------ */

    public static void onServerStop(){
        schedulers.clear();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void onWorldTick(@Nonnull final WorldServer world){
        final FluidTaskScheduler scheduler = getScheduler(world);
        if(scheduler == null) return;
        scheduler.update();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void schedule(final @Nonnull World world,final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid){
        final FluidTaskScheduler scheduler = getScheduler(world);
        if(scheduler != null) scheduler.schedule(pos, task, fluid);
    }

    @Nullable
    public static FluidTaskScheduler getScheduler(final @Nonnull World world){
        @Nullable FluidTaskScheduler manager = schedulers.get(world.provider.getDimension());
        if(manager != null) return manager;
        if(world.hasCapability(FLUID_TASK_SCHEDULER,null)){
            schedulers.put(world.provider.getDimension(),manager = world.getCapability(FLUID_TASK_SCHEDULER,null));
            return manager;
        }else return null;
    }

    @Nonnull
    public static Int2ObjectMap<FluidTaskScheduler> getSchedulers() {
        return schedulers;
    }
}
