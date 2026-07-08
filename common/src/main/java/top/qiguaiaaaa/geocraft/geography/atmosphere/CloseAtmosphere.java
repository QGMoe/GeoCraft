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

package top.qiguaiaaaa.geocraft.geography.atmosphere;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.UnderlyingLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.atmosphere.weather.Weather;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.util.math.ExtendedChunkPos;
import top.qiguaiaaaa.geocraft.geography.atmosphere.layer.close.ClosedAtmosphereLayer;
import top.qiguaiaaaa.geocraft.geography.atmosphere.layer.close.ConstantUnderlying;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class CloseAtmosphere extends QiguaiAtmosphere {
    protected float constTemperature;
    protected final ConstantUnderlying underlying = new ConstantUnderlying(this);
    protected final ClosedAtmosphereLayer atmosphereLayer = new ClosedAtmosphereLayer(this);
    public CloseAtmosphere(float temp){
        this.constTemperature = temp;
        layers.add(underlying);
        layers.add(atmosphereLayer);
        underlying.getTemperature().set(constTemperature);
        atmosphereLayer.getTemperature().set(constTemperature);
        underlying.setUpperLayer(atmosphereLayer);
        atmosphereLayer.setLowerLayer(underlying);
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos, boolean notAir) {
        return constTemperature;
    }

    @Nonnull
    @Override
    public Weather getWeather(@Nonnull BlockPos pos) {
        return Weather.SUNNY;
    }

    @Nullable
    @Override
    public AtmosphereLayer getBottomAtmosphereLayer(@Nonnull BlockPos pos) {
        return atmosphereLayer;
    }

    @Nonnull
    @Override
    public UnderlyingLayer getUnderlying(@Nonnull BlockPos pos) {
        return underlying;
    }

    @Override
    public double getCloudExponent(@Nonnull BlockPos pos) {
        return 0;
    }

    public void setConstTemperature(float temperature){
        constTemperature = temperature;
        underlying.getTemperature().set(temperature);
        atmosphereLayer.getTemperature().set(temperature);
    }

    public void setMaxWindSpeed(double maxWindSpeed){
        atmosphereLayer.setMaxWindSpeed(maxWindSpeed);
    }

    public void setPressure(double pressure){
        atmosphereLayer.setPressure(pressure);
    }

    @Override
    public void updateTick(@Nullable Chunk chunk) {
        tickTimes++;
        if(debug) GeoCraft.getLogger().info("{} {} Atmosphere updated {}",x,z,tickTimes);
        ExtendedChunkPos chunkPos = new ExtendedChunkPos(x,z);
        final Map<EnumFacing, Triple<Atmosphere,Chunk,EnumFacing>> neighbors = new EnumMap<>(EnumFacing.class);
        final World world = worldInfo.getWorld();
        final IAtmosphereSystem system = worldInfo.getSystem();
        for(EnumFacing facing: ChunkUtil.HORIZONTALS){
            ExtendedChunkPos facingPos = chunkPos.offset(facing);
            if(!system.isAtmosphereLoaded(facingPos)) continue;
            Chunk neighborChunk = null;
            if(world.isAreaLoaded(facingPos.getBlock(8,64,8),1)){
                neighborChunk = world.getChunk(facingPos.x,facingPos.z);
            }
            Atmosphere neighborAtmosphere = system.getAtmosphere(facingPos.x,facingPos.z);
            if(neighborAtmosphere == null) continue;
            Triple<Atmosphere,Chunk,EnumFacing> triple = new ImmutableTriple<>(neighborAtmosphere,neighborChunk,facing);
            neighbors.put(facing,triple);
        }
        //从下往上依次更新
        for(Layer layer:layers){
            if(debug) GeoCraft.getLogger().info("{} {} Atmosphere is updating layer {} ,i = {}",x,z,layer.getTagName(),layers.indexOf(layer));
            layer.tick(chunk,neighbors,x,z);
        }
        if(debug) GeoCraft.getLogger().info("{} {} Atmosphere updated successfully, now status:\n {}",x,z,this.toString());
        //更新Listener
        this.updateListeners();
        //Post Event
        EventFactory.postAtmosphereUpdate(chunk,this,x,z);
    }
}
