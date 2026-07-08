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

package top.qiguaiaaaa.geocraft.api.atmosphere.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气数据。由{@link IAtmosphereDataLoader}从磁盘中加载，{@link IAtmosphereDataProvider}提供和管理，{@link IAtmosphereSystem}处理，并将{@link #data}交给大气的{@link Atmosphere#serializeNBT()}和{@link Atmosphere#deserializeNBT(NBTBase)}处理
 * @since 0.1
 * @author QiguaiAAAA
 */
public class AtmosphereData {
    public static final String TAG_POS_X = "posX",TAG_POS_Z = "posZ",TAG_DATA = "Level";

    public final ChunkPos pos;
    public NBTTagCompound data;
    protected Atmosphere atmosphere;
    protected Chunk chunk;
    protected boolean unloadQueued = false,unloaded=false;
    protected long lastSaveTime;

    /**
     * 创建一个大气数据
     * @param data 具体NBT数据
     * @param x 区块X坐标
     * @param z 区块Z坐标
     */
    public AtmosphereData(@Nonnull NBTTagCompound data, int x, int z) {
        this.data = data;
        data.setInteger(TAG_POS_X,x);
        data.setInteger(TAG_POS_Z,z);
        pos = new ChunkPos(x,z);
    }

    /**
     * 若该实例持有一个大气，则将大气实例序列化为NBT保存在大气数据的NBT中
     */
    public void saveAtmosphere(){
        if(atmosphere != null){
            this.data.setTag(TAG_DATA,atmosphere.serializeNBT());
        }
    }

    /**
     * 标记该大气数据是否将要被卸载
     * @param isQueuedToBeUnloaded 是否将要被卸载
     */
    public void setUnloadQueued(boolean isQueuedToBeUnloaded){
        unloadQueued = isQueuedToBeUnloaded;
    }

    /**
     * 标记该大气数据是否被卸载<br/>
     * 若一个大气数据实例已经被标记为被卸载，则不应该再次使用该大气数据实例
     * @since 0.1
     */
    public void markUnloaded() {
        this.unloaded = true;
    }

    public void setLastSaveTime(long time){
        lastSaveTime = time;
    }

    public void setAtmosphere(@Nullable Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    /**
     * 设置该大气数据对应的区块实例
     * @since 0.1
     * @param chunk 区块实例
     */
    public void setChunk(@Nullable Chunk chunk) {
        this.chunk = chunk;
    }

    public boolean isUnloadQueued() {
        return unloadQueued;
    }

    /**
     * 该大气数据是否已被卸载<br/>
     * 应尽可能避免持有和使用已被卸载的大气数据
     * @return 若已被卸载，则返回true
     */
    public boolean isUnloaded() {
        return unloaded;
    }

    /**
     * 该大气数据是否没有持有任何数据
     * @return 若没有，则返回true
     */
    public boolean isEmpty(){
        return getSaveCompound().isEmpty();
    }

    /**
     * 获取该大气数据保存具体数据的NBT复合标签
     * @return 一个NBT复合标签，内为大气序列化后的NBT数据
     */
    @Nonnull
    public NBTTagCompound getSaveCompound() {
        return this.data.getCompoundTag(TAG_DATA);
    }

    public long getLastSaveTime() {
        return lastSaveTime;
    }

    /**
     * 获取链接到的大气实例
     * @since 0.1
     * @return 大气实例
     */
    @Nullable
    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    /**
     * 获取该大气数据对应的区块，若对应区块未加载则返回null
     * @return 一个区块
     */
    @Nullable
    public Chunk getChunk() {
        return chunk;
    }
}
