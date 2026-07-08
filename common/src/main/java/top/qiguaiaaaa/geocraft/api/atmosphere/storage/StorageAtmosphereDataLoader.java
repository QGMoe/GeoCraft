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

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 仿造{@link AnvilChunkLoader}写的一个大气数据加载器<br/>
 * 该加载器会读取位于每个维度下面的atmosphere文件夹中的数据,并转换为{@link AtmosphereData}<br/>
 * 写入为异步操作,读取支持异步操作
 */
public class StorageAtmosphereDataLoader implements IAtmosphereDataLoader, IThreadedFileIO {
    private static final Logger LOGGER = LogManager.getLogger();
    public final File saveFolder;
    protected final Map<ChunkPos, NBTTagCompound> atmospheresToSave = Maps.newConcurrentMap();
    protected final Set<ChunkPos> atmospheresBeingSaved = Collections.newSetFromMap(Maps.newConcurrentMap());
    protected boolean flushing;

    /**
     * 创建一个大气数据加载器
     * @param atmosphereSaveFolder 大气数据保存的文件夹，应当是类似DIMx的维度保存文件夹。主世界请使用DIM0
     */
    public StorageAtmosphereDataLoader(@Nonnull File atmosphereSaveFolder){
        this.saveFolder = atmosphereSaveFolder;
    }

    /**
     * {@inheritDoc}
     * @param worldIn 区块所在世界
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Nullable
    @Override
    public AtmosphereData loadAtmosphereData(@Nonnull World worldIn, int x, int z) throws IOException {
        ChunkPos chunkpos = new ChunkPos(x, z);
        NBTTagCompound compound = this.atmospheresToSave.get(chunkpos);

        if (compound == null) {
            DataInputStream input = AtmosphereRegionFileCache.getAtmosphereInputStream(saveFolder, x, z);

            if (input == null) {
                return null;
            }

            compound = CompressedStreamTools.read(input);
            input.close();
        }
        return new AtmosphereData(compound,x,z);
    }

    /**
     * {@inheritDoc} <br/>
     * 注意该操作是异步的
     * @param worldIn 大气数据所在世界
     * @param data 需要保存的大气数据
     * @throws MinecraftException {@inheritDoc}
     */
    @Override
    public void saveAtmosphereData(@Nonnull World worldIn, @Nonnull AtmosphereData data) throws MinecraftException {
        worldIn.checkSessionLock();

        try {
            this.addAtmosphereToPending(data.pos, data.data);
        } catch (Exception exception) {
            LOGGER.error("Failed to save atmosphere", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return {@inheritDoc}
     */
    @Override
    public boolean doesAtmosphereExistsAt(int x, int z) {
        ChunkPos chunkpos = new ChunkPos(x, z);
        NBTTagCompound nbttagcompound = this.atmospheresToSave.get(chunkpos);
        return nbttagcompound != null || AtmosphereRegionFileCache.atmosphereExists(this.saveFolder, x, z);
    }

    /**
     * 立刻保存待保存列表中下一个大气数据
     */
    @Override
    public void flush() {
        try {
            this.flushing = true;
            this.writeNextIO();
        } finally {
            this.flushing = false;
        }
    }

    /**
     * 将指定位置的大气数据加入待保存列表
     * @param pos 大气数据所处位置
     * @param compound 大气数据序列化后的NBT
     */
    protected void addAtmosphereToPending(ChunkPos pos, NBTTagCompound compound) {
        if (!this.atmospheresBeingSaved.contains(pos)) {
            this.atmospheresToSave.put(pos, compound);
        }

        ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
    }

    /**
     * 从待保存列表中取出下一个需要保存的大气数据，并保存
     * @return 若保存成功，则返回true
     */
    @Override
    public boolean writeNextIO() {
        if (this.atmospheresToSave.isEmpty()) {
            if (this.flushing) {
                LOGGER.info("AtmosphereStorage ({}): All atmospheres are saved", this.saveFolder.getName());
            }

            return false;
        }
        ChunkPos pos;
        try{
            pos = this.atmospheresToSave.keySet().iterator().next();
        }catch (NoSuchElementException e){
            return false;
        }catch (Throwable e){
            LOGGER.error("Unknown Error stopped atmosphere data from being saved!",e);
            throw new RuntimeException(e);
        }
        boolean succeed;

        try {
            this.atmospheresBeingSaved.add(pos);
            NBTTagCompound compound = this.atmospheresToSave.remove(pos);

            if (compound != null) {
                try {
                    this.writeAtmosphereData(pos, compound);
                } catch (Exception exception) {
                    LOGGER.error("Failed to save atmosphere", exception);
                }
            }

            succeed = true;
        } finally {
            this.atmospheresBeingSaved.remove(pos);
        }

        return succeed;
    }

    /**
     * 获得待保存的大气数据数量
     * @return 待保存的大气数据数量
     */
    public int getPendingSaveCount() {
        return this.atmospheresToSave.size();
    }

    /**
     * 写入指定大气数据
     * @param pos 位置
     * @param compound 序列化后的NBT数据
     * @throws IOException 若出现IO错误，则抛出
     */
    protected void writeAtmosphereData(ChunkPos pos, NBTTagCompound compound) throws IOException {
        DataOutputStream output = AtmosphereRegionFileCache.getAtmosphereOutputStream(this.saveFolder, pos.x, pos.z);
        CompressedStreamTools.write(compound, output);
        output.close();
    }
}
