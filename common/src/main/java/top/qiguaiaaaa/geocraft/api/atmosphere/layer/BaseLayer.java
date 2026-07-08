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

package top.qiguaiaaaa.geocraft.api.atmosphere.layer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.property.GeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.state.GeographyState;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个基本的抽象层,实现了基本的层级结构、热量管理和{@link GeographyState}管理
 */
public abstract class BaseLayer implements Layer{
    protected final Map<IGeographyProperty, GeographyState> states = new HashMap<>();
    protected final Atmosphere atmosphere;
    protected Layer lowerLayer,upperLayer;

    public BaseLayer(@Nonnull Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    @Override
    public boolean isLoaded() {
        for(@Nonnull GeographyState state:states.values()){
            if(!state.isLoaded()) return false;
        }
        return true;
    }

    @Override
    public void putHeat(double quanta, @Nullable BlockPos pos) {
        final TemperatureState temperature = getTemperature();
        double capacity = getHeatCapacity();
        if(temperature.get()+quanta/capacity< TemperatureProperty.MIN){
            temperature.set(TemperatureProperty.MIN);
            return;
        }
        temperature.addHeat(quanta,capacity);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable EnumFacing direction) {
        if(direction == null || pack.getType() == null){
            putHeat(pack.getAmount(),null);
            return;
        }
        sendHeat(pack,direction.getDirectionVec());
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable Vec3i direction) {
        if(direction == null || pack.getType() == null){
            putHeat(pack.getAmount(),null);
            return;
        }
        sendHeat(pack,new Vec3d(direction));
    }

    @Override
    public double drainHeat(double quanta, @Nullable BlockPos pos) {
        final TemperatureState temperature = getTemperature();
        double capacity = getHeatCapacity();
        if(temperature.get()-quanta/capacity< TemperatureProperty.MIN){
            quanta = Math.max(temperature.get()- TemperatureProperty.MIN-0.1,0)*capacity;
            temperature.set(TemperatureProperty.MIN+0.1f);
            return quanta;
        }
        temperature.addHeat(-quanta,capacity);
        return quanta;
    }

    @Nullable
    @Override
    public Layer getLowerLayer() {
        return lowerLayer;
    }

    @Nullable
    @Override
    public Layer getUpperLayer() {
        return upperLayer;
    }

    @Override
    public void setLowerLayer(Layer layer) {
        if(layer == this) return;
        this.lowerLayer = layer;
    }

    @Override
    public void setUpperLayer(Layer layer) {
        if(layer == this) return;
        this.upperLayer = layer;
    }

    @Nonnull
    @Override
    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    @Nullable
    @Override
    public TemperatureState getTemperature(final TemperatureProperty property) {
        final GeographyState state = states.get(property);
        if(state instanceof TemperatureState) return (TemperatureState) state;
        return null;
    }

    @Nullable
    @Override
    public GeographyState getState(@Nonnull final IGeographyProperty property) {
        return states.get(property);
    }

    @Nullable
    @Override
    public GeographyState addState(@Nonnull final IGeographyProperty property) {
        GeographyState oldState = getState(property);
        GeographyState newState = property.getStateInstance();
        states.put(property,newState);
        newState.load(this);
        return oldState;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        for(GeographyState state:states.values()){
            if(!state.canSerialize()) continue;
            ResourceLocation location = state.getProperty().getRegistryName();
            compound.setTag(location == null?state.getNBTTagKey():location.toString(),state.serializeNBT());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        for(GeographyState state:states.values()){
            if(!state.canDeserialize()) continue;
            ResourceLocation location = state.getProperty().getRegistryName();
            NBTBase tag = nbt.getTag(location == null?state.getNBTTagKey():location.toString());
            state.deserializeNBT(tag);
        }
        for(String key:nbt.getKeySet()){
            IGeographyProperty property = GeographyProperty.MANAGER.getProperties().getValue(new ResourceLocation(key));
            if(property == null) continue;
            if(states.containsKey(property)) continue;
            GeographyState state = property.getStateInstance();
            state.deserializeNBT(nbt.getTag(key));
            states.put(property,state);
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        final TemperatureState temperature = getTemperature();
        double capacity = getHeatCapacity();
        if(temperature.get()+maxReceive/capacity< TemperatureProperty.MIN){
            if(!simulate) temperature.set(TemperatureProperty.MIN);
            return (int) ((TemperatureProperty.MIN-temperature.get())*capacity);
        }
        if(!simulate) temperature.addHeat(maxReceive,capacity);
        return maxReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        final TemperatureState temperature = getTemperature();
        double capacity = getHeatCapacity();
        if(temperature.get()-maxExtract/capacity< TemperatureProperty.MIN){
            maxExtract = (int) (Math.max(temperature.get()- TemperatureProperty.MIN-0.1,0)*capacity);
            if(!simulate) temperature.set(TemperatureProperty.MIN+0.1f);
            return maxExtract;
        }
        if(!simulate) temperature.addHeat(-maxExtract,capacity);
        return maxExtract;
    }
}
