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

package top.qiguaiaaaa.geocraft.api.atmosphere.system;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class BaseAtmosphereSystem implements IAtmosphereSystem{
    protected final AtmosphereInfo worldInfo;
    protected final IAtmosphereDataProvider dataProvider;
    protected boolean stopped = false;

    public BaseAtmosphereSystem(AtmosphereInfo info, IAtmosphereDataProvider provider) {
        this.worldInfo = info;
        this.dataProvider = provider;
    }

    @Override
    public void onChunkGenerated(@Nonnull Chunk chunk) {
        AtmosphereData data = dataProvider.provideAtmosphereData(chunk.x,chunk.z);
        data.setChunk(chunk);
        data.setAtmosphere(generateAtmosphere(chunk,data));
    }

    @Override
    public void onChunkLoaded(@Nonnull Chunk chunk) {
        AtmosphereData data = dataProvider.provideAtmosphereData(chunk.x,chunk.z);
        data.setChunk(chunk);
        if(data.getAtmosphere() == null){
            data.setAtmosphere(generateAtmosphere(chunk,data));
        }
    }

    @Override
    public void onChunkUnloaded(@Nonnull Chunk chunk) {
        AtmosphereData data = dataProvider.getLoadedAtmosphereData(chunk.x,chunk.z);
        if(data == null) return;
        data.saveAtmosphere();
        data.setChunk(null);
    }

    @Override
    public void onWorldSave() {
        dataProvider.saveAllAtmosphereData();
    }

    @Override
    public void onServerStopping(@Nonnull FMLServerStoppingEvent event) {
        Collection<AtmosphereData> dataList = dataProvider.getLoadedAtmosphereDataCollection();
        for(AtmosphereData data:dataList){
            if(data.getAtmosphere() == null) continue;
            Atmosphere atmosphere = data.getAtmosphere();
            if(atmosphere.isLoaded()){
                data.saveAtmosphere();
            }
        }
        onWorldSave();
    }

    @Override
    public boolean isAtmosphereLoaded(@Nonnull ChunkPos pos) {
        Atmosphere atmosphere =  getAtmosphere(pos.x, pos.z);
        return atmosphere != null && atmosphere.isLoaded();
    }

    @Override
    public void setStop(boolean status) {
        stopped = status;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Nonnull
    @Override
    public IAtmosphereDataProvider getDataProvider() {
        return dataProvider;
    }

    @Nullable
    @Override
    public Atmosphere getAtmosphere(@Nonnull BlockPos pos) {
        return getAtmosphere(pos.getX()>>4,pos.getZ()>>4);
    }

    @Nullable
    @Override
    public Atmosphere getAtmosphere(@Nonnull Chunk chunk) {
        return getAtmosphere(chunk.x,chunk.z);
    }

    @Override
    public Atmosphere getAtmosphere(int x,int z) {
        AtmosphereData data = dataProvider.getLoadedAtmosphereData(x,z);
        if(data == null) return null;
        Atmosphere atmosphere = data.getAtmosphere();
        if(atmosphere != null && atmosphere.isLoaded()) return atmosphere;
        return null;
    }

    @Nonnull
    @Override
    public AtmosphereInfo getAtmosphereInfo() {
        return worldInfo;
    }

    @Nullable
    protected AtmosphereData getChunkLoadedAtmosphereData(@Nonnull Chunk chunk){
        AtmosphereData data = dataProvider.getLoadedAtmosphereData(chunk.x,chunk.z);
        if(data == null) return null;
        Atmosphere atmosphere = data.getAtmosphere();
        if(atmosphere ==null) return null;
        if(data.getChunk() == null){
            this.onChunkLoaded(chunk); //若大气处于无区块加载状态,先处理大气
        }
        if(!atmosphere.isLoaded()) return null;
        return data;
    }

    @Nullable
    protected abstract Atmosphere generateAtmosphere(@Nullable Chunk chunk,@Nonnull AtmosphereData data);
}
