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

package moe.qingu.geocraft.geography.fluidphysics.updater;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.util.annotation.ThreadOnly;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.util.math.vec.MBlockPos;
import moe.qingu.geocraft.capability.FluidUpdaterCapability;
import moe.qingu.geocraft.capability.FluidUpdaterManagerCapability;
import moe.qingu.geocraft.configs.FluidPhysicsConfig;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author QGMoe
 */
public class FluidUpdaterManager implements ICapabilityProvider {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"fluid_updater_manager");
    private static final Int2ObjectOpenHashMap<FluidUpdaterManager> managers = new Int2ObjectOpenHashMap<>();
    private final MBlockPos posContainer = new MBlockPos();
    private final int maxUpdateNum;
    private final World world;
    private final Long2ObjectOpenHashMap<FluidUpdater> updaters = new Long2ObjectOpenHashMap<>();
    private final ConcurrentLinkedQueue<FluidUpdater> dirties = new ConcurrentLinkedQueue<>();
    private final LongOpenHashSet schedules = new LongOpenHashSet();

    public FluidUpdaterManager(final @Nonnull World world) {
        this.world = world;
        maxUpdateNum = FluidPhysicsConfig.FLUID_UPDATER_MAX_TASKS_PER_TICK.getValue();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void schedule(final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid){
        if(pos.getY()>255 || pos.getY()<0) return;
        final int chunkX = pos.getX()>>4;
        final int chunkZ = pos.getZ()>>4;
        final FluidUpdater updater = getUpdater(chunkX,chunkZ);
        if(updater == null) return;
        final int cx = pos.getX() & 0xF;
        final int cz = pos.getZ() & 0xF;
        if(updater.isScheduled(cx,pos.getY(),cz)) return;
        final int taskID = FluidTasks.getID(task);
        if(fluid.getDensity() > 0) updater.scheduleHeavy(cx, pos.getY(), cz, (short) taskID);
        else updater.scheduleLight(cx,pos.getY(),cz,(short) taskID);
        schedules.add((long) chunkX << Integer.SIZE | chunkZ);
        if(!updater.isDirty() && updater.markDirty()){
            dirties.add(updater);
        }
    }

    public void update(){
        final long beginTime = System.nanoTime(),maxTime = FluidPhysicsConfig.FLUID_UPDATER_MAX_TIME_USAGE.getValue();
        int count = 0;
        final LongIterator iterator = schedules.iterator();
        while (count < maxUpdateNum){
            if(!iterator.hasNext()) break;
            final long pos = iterator.nextLong();
            final FluidUpdater updater = updaters.get(pos);
            if(updater == null) continue;
            final int x = (int) (pos>>Integer.SIZE);
            final int z = (int) pos;
            int cot;
            do{
                cot = updater.update(world,posContainer,x,z);
            }while (updater.hasLeft() && (count += cot) < maxUpdateNum);
            if(cot != 0 && updater.markDirty()) dirties.add(updater);
            if(!updater.hasLeft()) iterator.remove();
            if(System.nanoTime() - beginTime > maxTime) break;
        }
    }

    @Nullable
    public FluidUpdater getUpdater(final int cx,final int cz){
        FluidUpdater res = updaters.get((long) cx <<Integer.SIZE|cz);
        if(res != null) return res;
        final Chunk chunk = world.getChunk(cx,cz);
        if(chunk.hasCapability(FluidUpdaterCapability.FLUID_UPDATER,null)){
            updaters.put((long) cx <<Integer.SIZE|cz,res = chunk.getCapability(FluidUpdaterCapability.FLUID_UPDATER,null));
            return res;
        }else return null;
    }

    @Nonnull
    public Long2ObjectOpenHashMap<FluidUpdater> getUpdaters() {
        return updaters;
    }

    @Nonnull
    public LongOpenHashSet getSchedules() {
        return schedules;
    }

    @Nonnull
    public ConcurrentLinkedQueue<FluidUpdater> getDirties() {
        return dirties;
    }

    @Nonnull
    public World getWorld() {
        return world;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == FluidUpdaterManagerCapability.FLUID_UPDATER_MANAGER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == FluidUpdaterManagerCapability.FLUID_UPDATER_MANAGER ? FluidUpdaterManagerCapability.FLUID_UPDATER_MANAGER.cast(this):null;
    }

    public static void onServerStop(){
        managers.clear();
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public static void schedule(final @Nonnull World world,final @Nonnull BlockPos pos, final @Nonnull IFluidTask task, final @Nonnull Fluid fluid){
        final FluidUpdaterManager manager = getManager(world);
        if(manager != null) manager.schedule(pos, task, fluid);
    }

    @Nullable
    public static FluidUpdaterManager getManager(final @Nonnull World world){
        @Nullable FluidUpdaterManager manager = managers.get(world.provider.getDimension());
        if(manager != null) return manager;
        if(world.hasCapability(FluidUpdaterManagerCapability.FLUID_UPDATER_MANAGER,null)){
            managers.put(world.provider.getDimension(),manager = world.getCapability(FluidUpdaterManagerCapability.FLUID_UPDATER_MANAGER,null));
            return manager;
        }else return null;
    }

    @Nonnull
    public static Int2ObjectOpenHashMap<FluidUpdaterManager> getManagers() {
        return managers;
    }
}
