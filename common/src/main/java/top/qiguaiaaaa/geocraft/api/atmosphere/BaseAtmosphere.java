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

package top.qiguaiaaaa.geocraft.api.atmosphere;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import top.qiguaiaaaa.geocraft.api.GeoCraftAPI;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.tracker.IAtmosphereTracker;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.property.GeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseAtmosphere implements Atmosphere{
    public static final String CAPABILITIES_TAG = "Capabilities",VERSION_TAG = "Version",LAYERS_TAG = "Layers";
    protected AtmosphereInfo worldInfo = null;
    protected long tickTimes = 0;
    /**
     * 从下往上的层状列表
     */
    protected final List<Layer> layers = new ArrayList<>();
    protected final Set<IAtmosphereTracker> trackers = new HashSet<>();

    protected final CapabilityDispatcher capabilities = EventFactory.gatherCapabilities(this);

    @Override
    public void onLoad(@Nullable Chunk chunk, @Nonnull AtmosphereInfo info) {
        this.setAtmosphereWorldInfo(info);
        for(Layer layer:layers){
            layer.onLoad(chunk);
        }
        for(IGeographyProperty property: GeographyProperty.MANAGER.getProperties()){
            property.onAtmosphereInitialise(this,chunk);
        }
    }

    @Override
    public void addTracker(@Nonnull IAtmosphereTracker tracker){
        trackers.add(tracker);
    }
    @Override
    public void removeTracker(@Nonnull IAtmosphereTracker tracker){
        trackers.remove(tracker);
    }

    public void setAtmosphereWorldInfo(@Nonnull AtmosphereInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Nonnull
    @Override
    public AtmosphereInfo getAtmosphereInfo() {
        return worldInfo;
    }

    @Override
    public boolean isLoaded(){
        for(Layer layer:layers){
            if(!layer.isLoaded()) return false;
        }
        return true;
    }

    @Override
    public void onUnload() {
        trackers.forEach(tracker -> tracker.onAtmosphereUnload(this));
    }

    @Override
    public long tickTime() {
        return tickTimes;
    }

    @Nonnull
    @Override
    public Layer getTopLayer() {
        return layers.get(layers.size()-1);
    }

    @Nonnull
    @Override
    public Layer getBottomLayer() {
        return layers.get(0);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capabilities != null && capabilities.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capabilities == null? null : capabilities.getCapability(capability,facing);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound layerCompound = new NBTTagCompound();
        for(Layer layer:layers){
            layerCompound.setTag(layer.getTagName(),layer.serializeNBT());
        }

        compound.setTag(LAYERS_TAG,layerCompound);

        if(capabilities != null){
            compound.setTag(CAPABILITIES_TAG,capabilities.serializeNBT());
        }
        compound.setInteger(VERSION_TAG,1);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        final NBTTagCompound layerCompound;
        switch (nbt.getInteger(VERSION_TAG)){
            case 0:
                layerCompound = nbt;
                break;
            case 1:
            default:
                layerCompound = nbt.getCompoundTag(LAYERS_TAG);
                break;
        }

        for(Layer layer:layers) {
            NBTBase base = layerCompound.getTag(layer.getTagName());
            if (!(base instanceof NBTTagCompound)) {
                GeoCraftAPI.LOGGER.error("Loading Atmosphere at ({},{}) error: NBT of Atmosphere Layer {} isn't a valid compound tag!", getChunkX(), getChunkZ(), layer.getTagName());
                continue;
            }
            layer.deserializeNBT((NBTTagCompound) base);
        }

        if(capabilities != null && nbt.hasKey(CAPABILITIES_TAG)){
            NBTTagCompound compound = nbt.getCompoundTag(CAPABILITIES_TAG);
            capabilities.deserializeNBT(compound);
        }
    }
}
