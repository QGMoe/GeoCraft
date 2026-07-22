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

package moe.qingu.geocraft.handler;

import moe.qingu.geocraft.geography.fluidphysics.scheduler.ChunkyFluidTaskDatum;
import moe.qingu.geocraft.api.fluidphysics.task.scheduler.FluidTaskScheduler;
import moe.qingu.geocraft.world.BlockUpdater;
import moe.qingu.geocraft.world.storage.ScheduledTicksData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * @author QGMoe
 */
public final class CapabilityHandler {
    public static @CapabilityInject(BlockUpdater.class) Capability<BlockUpdater> BLOCK_UPDATER;
    public static @CapabilityInject(ChunkyFluidTaskDatum.class) Capability<ChunkyFluidTaskDatum> CHUNKY_FLUID_TASK_DATA;
    public static @CapabilityInject(ScheduledTicksData.class) Capability<ScheduledTicksData> SCHEDULED_TICKS_DATA;

    private CapabilityHandler(){}

    public static void register(){
        CapabilityManager.INSTANCE.register(FluidTaskScheduler.class,new UselessCapabilityStorage<>(),unsupported());
        CapabilityManager.INSTANCE.register(BlockUpdater.class,new UselessCapabilityStorage<>(),BlockUpdater::new);
        CapabilityManager.INSTANCE.register(ChunkyFluidTaskDatum.class,new NBTCompoundCapabilityStorage<>(), ChunkyFluidTaskDatum::new);
        CapabilityManager.INSTANCE.register(ScheduledTicksData.class, new NBTCompoundCapabilityStorage<>(),ScheduledTicksData::new);
    }

    @Nonnull
    private static <T> Callable<T> unsupported(){
        return () -> {throw new UnsupportedOperationException();};
    }

    private static final class UselessCapabilityStorage<T> implements Capability.IStorage<T> {
        @Override
        @Nonnull
        public NBTBase writeNBT(final @Nonnull Capability<T> capability, final @Nonnull T instance,final @Nullable EnumFacing side) {
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(final @Nonnull Capability<T> capability,final @Nonnull T instance,final @Nullable EnumFacing side,final @Nonnull NBTBase nbt) {}
    }

    private static final class NBTCompoundCapabilityStorage<T extends INBTSerializable<NBTTagCompound>> implements Capability.IStorage<T>{

        @Nullable
        @Override
        public NBTBase writeNBT(final @Nonnull Capability<T> capability,final @Nonnull T instance,final @Nullable EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(final @Nonnull Capability<T> capability,final @Nonnull T instance,final @Nullable EnumFacing side,final @Nonnull NBTBase nbt) {
            if(nbt instanceof NBTTagCompound){
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }
    }
}
