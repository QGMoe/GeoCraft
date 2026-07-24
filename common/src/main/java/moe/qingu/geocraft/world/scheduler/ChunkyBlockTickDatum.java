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

import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.util.annotation.MultiThread;
import moe.qingu.geocraft.api.util.annotation.ThreadType;
import moe.qingu.geocraft.api.world.tick.scheduler.BlockTickScheduler;
import moe.qingu.geocraft.handler.CapabilityHandler;
import moe.qingu.geocraft.world.scheduler.boxed.BoxedBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.boxed.BoxedBlockTickScheduler;
import moe.qingu.geocraft.world.scheduler.packed.PackedBlockTickDatum;
import moe.qingu.geocraft.world.scheduler.packed.PackedBlockTickScheduler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author QGMoe
 */
public abstract class ChunkyBlockTickDatum implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"scheduled_ticks_data");
    protected static final String KEY_VERSION = "version";
    protected static final String KEY_TYPE = "type";
    protected static final String KEY_BASE_TIME = "baseTime";
    protected static final byte TYPE_BOXED = 0;
    protected static final byte TYPE_PACKED = 1;
    public final ReentrantLock lock = new ReentrantLock();
    protected final AtomicBoolean dirty = new AtomicBoolean(false);
    protected volatile SoftReference<NBTTagCompound> save = new SoftReference<>(new NBTTagCompound());

    public static @Nullable ChunkyBlockTickDatum createByScheduler(final @Nullable BlockTickScheduler scheduler, final @Nonnull Chunk chunk){
        if(scheduler instanceof BoxedBlockTickScheduler) return new BoxedBlockTickDatum(chunk);
        else if(scheduler instanceof PackedBlockTickScheduler) return new PackedBlockTickDatum();
        else return null;
    }

    /* -------------------------------
         Serialisation Area
       ------------------------------- */

    @Override
    @Nonnull
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.CHUNK_IO_THREADS,ThreadType.GEO_MISC_DAEMON})
    public final NBTTagCompound serializeNBT() {
        final @Nullable NBTTagCompound cache;
        if(isDirty() || (cache = this.save.get()) == null){
            lock.lock();
            try {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger(KEY_VERSION,2);
                this.serialiseNBT(compound);
                this.save = new SoftReference<>(compound);
                return compound;
            }finally {
                this.clearDirty();
                lock.unlock();
            }
        }
        return cache;
    }

    protected abstract void serialiseNBT(final @Nonnull NBTTagCompound compound);

    /* -------------------------------
               Capability Area
       ------------------------------- */

    @Override
    public final boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityHandler.CHUNKY_BLOCK_TICK_DATUM;
    }

    @Nullable
    @Override
    public final <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if(hasCapability(capability,facing)){
            return CapabilityHandler.CHUNKY_BLOCK_TICK_DATUM.cast(this);
        }else return null;
    }

    /* -------------------------------
              Getter And Setter
       ------------------------------- */

    public abstract boolean isEmpty();

    public final boolean isDirty() {
        return dirty.get();
    }

    public final void clearDirty(){
        dirty.set(false);
    }
}
