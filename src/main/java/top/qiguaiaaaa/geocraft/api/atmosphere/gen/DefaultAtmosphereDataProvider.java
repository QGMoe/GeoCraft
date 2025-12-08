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

package top.qiguaiaaaa.geocraft.api.atmosphere.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.IAtmosphereDataLoader;
import top.qiguaiaaaa.geocraft.api.util.APIUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 类似{@link ChunkProviderServer}，为大气数据提供器
 * 该提供器管理大气数据的加载、保存和卸载操作
 */
public class DefaultAtmosphereDataProvider implements IAtmosphereDataProvider {
    private static final Logger LOGGER = LogManager.getLogger("AtmosphereDataProvider");
    protected final LongSet droppedAtmospheres = new LongOpenHashSet();
    public final WorldServer world;
    public final IAtmosphereDataLoader atmosphereLoader;
    protected final Long2ObjectMap<AtmosphereData> loadedAtmosphere = new Long2ObjectOpenHashMap<>(65536);

    protected List<EntityPlayerMP> playerList;

    protected int maxLoadDis = 100;

    /**
     * 创建一个大气数据提供器实例
     * @param world 管理的维度对应世界，要求{@link WorldServer}
     * @param loader 大气数据加载器，用于从文件存储中加载大气数据到内存中
     */
    public DefaultAtmosphereDataProvider(@Nonnull WorldServer world,@Nonnull IAtmosphereDataLoader loader) {
        this.world = world;
        this.atmosphereLoader = loader;
    }

    @Override
    public void setMaxLoadDistance(int distance) {
        this.maxLoadDis = distance;
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return {@inheritDoc}
     */
    @Nonnull
    @Override
    public AtmosphereData provideAtmosphereData(int x, int z) {
        AtmosphereData data = this.loadAtmosphereData(x, z);
        if (data == null) {
            data = new AtmosphereData(new NBTTagCompound(),x,z);
            loadedAtmosphere.put(ChunkPos.asLong(x,z),data);
            return data;
        }
        return data;
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     */
    @Override
    public void queueUnloadAtmosphereData(int x, int z) {
        AtmosphereData data = loadedAtmosphere.get(ChunkPos.asLong(x,z));
        if(data == null){
            APIUtil.LOGGER.warn("Someone trying to unload an atmosphere that haven't been loaded.");
            return;
        }
        queueUnloadAtmosphereData(data);
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     */
    @Override
    public void saveAtmosphereData(int x, int z) {
        AtmosphereData data = loadedAtmosphere.get(ChunkPos.asLong(x,z));
        if(data == null){
            APIUtil.LOGGER.warn("Someone trying to save an atmosphere {} {} in DIM{} that haven't been loaded.",x,z,world.provider.getDimension());
            return;
        }
        saveAtmosphereData(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAllAtmosphereData() {
        for(AtmosphereData data:loadedAtmosphere.values()){
            saveAtmosphereData(data);
//            atmosphereLoader.flush();
        }
        APIUtil.LOGGER.debug("All Atmosphere Data in DIM{} has saved!",world.provider.getDimension());
    }

    /**
     * {@inheritDoc}
     * 每游戏刻会处理将要被卸载的大气数据，将处于常加载区块的大气数据排除，然后卸载已被标记为将要卸载的大气数据
     * @return {@inheritDoc}
     */
    @Override
    public boolean tick() {
        if (this.world.disableLevelSaving) return false;
        if (this.droppedAtmospheres.isEmpty()) return false;

        for (ChunkPos forced : this.world.getPersistentChunks().keySet()) {
            this.droppedAtmospheres.remove(ChunkPos.asLong(forced.x, forced.z));
        }
        LongIterator iterator = this.droppedAtmospheres.iterator();
        for (int i = 0; i < 1000 && iterator.hasNext(); iterator.remove()) {
            long id = iterator.next();
            final AtmosphereData data = this.loadedAtmosphere.get(id);
            if(data == null) continue;
            if(data.getChunk() != null && data.getChunk().isLoaded()) continue;

            if (data.isUnloadQueued()) {
                if(data.getAtmosphere() != null) {
                    data.getAtmosphere().onUnload();
                }
                this.saveAtmosphereData(data);
                this.loadedAtmosphere.remove(id);
                data.markUnloaded();
                i++;
            }
        }

        if(world.getTotalWorldTime()%8000 == 0){
            final int loadRangeMax = maxLoadDis<<4;
            playerList = getPlayers();
            for(AtmosphereData data:this.loadedAtmosphere.values()){
                if(getClosestPlayerDistance(data)>loadRangeMax){
                    queueUnloadAtmosphereData(data);
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nonnull
    @Override
    public String makeString() {
        return "ServerAtmosphereCache: " + this.loadedAtmosphere.size() + " Drop: " + this.droppedAtmospheres.size();
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return {@inheritDoc}
     */
    @Override
    public boolean isAtmosphereGeneratedAt(int x, int z) {
        return this.loadedAtmosphere.containsKey(ChunkPos.asLong(x, z)) || this.atmosphereLoader.doesAtmosphereExistsAt(x, z);
    }

    /**
     * 加载指定区块的大气数据，若对应区块的大气数据没有加载，则会尝试从文件中加载
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 加载的大气数据。若为null则意味对应区块的大气尚未生成
     */
    @Nullable
    public AtmosphereData loadAtmosphereData(int x, int z) {
        AtmosphereData data = this.getLoadedAtmosphereData(x, z);
        if (data == null) {
            data = this.loadAtmosphereDataFromLoader(x, z);

            if (data != null) {
                this.loadedAtmosphere.put(ChunkPos.asLong(x, z), data);
            }
        }
        return data;
    }

    /**
     * {@inheritDoc}
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return {@inheritDoc}
     */
    @Nullable
    @Override
    public AtmosphereData getLoadedAtmosphereData(int x, int z) {
        long i = ChunkPos.asLong(x, z);
        AtmosphereData data = this.loadedAtmosphere.get(i);

        if (data != null) data.setUnloadQueued(false);

        return data;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<AtmosphereData> getLoadedAtmosphereDataCollection() {
        return loadedAtmosphere.values();
    }

    /**
     * 从{@link IAtmosphereDataLoader}中加载大气数据
     * @param x 区块X坐标
     * @param z 区块Z坐标
     * @return 加载的大气数据，可能为null
     */
    @Nullable
    protected AtmosphereData loadAtmosphereDataFromLoader(int x, int z) {
        try {
            AtmosphereData data = this.atmosphereLoader.loadAtmosphereData(this.world, x, z);

            if (data != null) {
                data.setLastSaveTime(this.world.getTotalWorldTime());
            }

            return data;
        } catch (Exception exception) {
            LOGGER.error("Couldn't load atmosphere data:", exception);
            return null;
        }
    }

    /**
     * 将指定大气数据标记为待卸载
     * @param data 要被卸载的大气数据
     */
    public void queueUnloadAtmosphereData(@Nonnull AtmosphereData data) {
        if (this.world.provider.canDropChunk(data.pos.x, data.pos.z)) {
            this.droppedAtmospheres.add(ChunkPos.asLong(data.pos.x, data.pos.z));
            data.setUnloadQueued(true);
        }
    }

    /**
     * 保存指定的大气数据
     * @param data 要保存的大气数据
     */
    public void saveAtmosphereData(@Nonnull AtmosphereData data) {
        try {
            data.setLastSaveTime(this.world.getTotalWorldTime());
            this.atmosphereLoader.saveAtmosphereData(this.world, data);
        } catch (IOException e) {
            LOGGER.error("Couldn't save atmosphere", e);
        } catch (MinecraftException e) {
            LOGGER.error("Couldn't save atmosphere; already in use by another instance of Minecraft?", e);
        }
    }

    protected List<EntityPlayerMP> getPlayers(){
        final List<EntityPlayerMP> list = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
        final List<EntityPlayerMP> players = Lists.newArrayList();
        list.forEach(player-> {
            if(player.getServerWorld() == world) players.add(player);
        });
        return players;
    }

    protected double getClosestPlayerDistance(@Nonnull AtmosphereData data) {
        double dis = Double.MAX_VALUE;
        int x = 8|(data.pos.x<<4);
        int z = 8|(data.pos.z<<4);

        for (EntityPlayerMP player : this.playerList) {
            double d = Math.abs(player.posX-x)+Math.abs(player.posZ-z);

            if (d < dis) {
                dis = d;
            }
        }

        return dis;
    }
}
