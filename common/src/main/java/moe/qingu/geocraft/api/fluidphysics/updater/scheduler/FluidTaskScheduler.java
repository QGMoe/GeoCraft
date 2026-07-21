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

package moe.qingu.geocraft.api.fluidphysics.updater.scheduler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import moe.qingu.geocraft.api.GeoCraftAPI;
import moe.qingu.geocraft.api.fluidphysics.updater.task.IFluidTask;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
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

/**
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

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public abstract void schedule(final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid);

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
