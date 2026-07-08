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

package top.qiguaiaaaa.geocraft.geography.atmosphere.layer.vanilla;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.BaseAtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.property.GeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.IAtmosphereProperty;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.state.GeographyState;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.geography.atmosphere.QiguaiAtmosphere;
import top.qiguaiaaaa.geocraft.geography.atmosphere.VanillaAtmosphere;
import top.qiguaiaaaa.geocraft.geography.state.DefaultTemperatureState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class VanillaAtmosphereLayer extends BaseAtmosphereLayer {
    protected static final Random random = new Random();
    protected Vec3d wind;
    protected TemperatureState temperature = GeoCraftProperties.TEMPERATURE.getStateInstance();
    public VanillaAtmosphereLayer(VanillaAtmosphere atmosphere) {
        super(atmosphere);
        wind = Vec3d.ZERO;
        states.put(GeoCraftProperties.TEMPERATURE,temperature);
    }

    @Override
    public int addSteam(@Nullable BlockPos pos, int amount,final boolean doAdd) {
        return amount;
    }

    @Override
    public int addWater(@Nullable BlockPos pos, int amount,final boolean doAdd) {
        return amount;
    }

    @Override
    public double getPressure(@Nonnull BlockPos pos) {
       return AtmosphereUtil.Constants.海平面气压 *
                Math.exp(
                        -AtmosphereUtil.Constants.干空气摩尔质量 *
                                AtmosphereUtil.Constants.重力加速度 *
                                Altitude.get物理海拔(pos.getY()) /
                                (AtmosphereUtil.Constants.气体常数 * getTemperature(pos,false))
                );
    }

    @Override
    public double getWaterPressure(@Nonnull BlockPos pos) {
        return atmosphere.getWaterPressure(pos);
    }

    @Override
    public double getWaterPressure() {
        return getWaterPressure(new BlockPos(0,getBeginY(),0));
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos, boolean notAir) {
        if(atmosphere.getAtmosphereInfo().getWorld().isBlockLoaded(pos)){
            Biome curBiome = atmosphere.getAtmosphereInfo().getWorld().getBiome(pos);
            return DefaultTemperatureState.toRealTemperature(curBiome.getTemperature(pos));
        }
        return DefaultTemperatureState.toRealTemperature(((VanillaAtmosphere)atmosphere).getBiome().getTemperature(pos));
    }

    @Nonnull
    @Override
    public Vec3d getWind(@Nonnull BlockPos pos) {
        return wind;
    }

    @Nullable
    @Override
    public FluidState getSteam() {
        return null;
    }

    public Vec3d 计算水平风速分量(Atmosphere to, EnumFacing dir) {
        Vec3d wind = new Vec3d(dir.getDirectionVec()).scale(random.nextDouble()*4-2);
        for(IAtmosphereProperty property: GeographyProperty.MANAGER.getWindEffectedProperties()){
            wind= wind.add(property.getWind(this,to,dir));
        }
        return wind;
    }

    @Override
    public void onLoad(@Nullable Chunk chunk) {
        for(GeographyState state:states.values())
            if(!state.isLoaded())
                state.load(this);
        if(chunk == null) return;
        temperature.set(DefaultTemperatureState.calculateBaseTemperature(chunk,atmosphere.getUnderlying(new BlockPos(0,getBeginY(),0))));
    }

    @Override
    public void tick(@Nullable Chunk chunk, @Nonnull Map<EnumFacing, Triple<Atmosphere, Chunk, EnumFacing>> neighbors, int x, int z) {
        wind = Vec3d.ZERO;
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            EnumFacing direction = neighbor.getRight();
            Vec3d newWindSpeed = 计算水平风速分量(neighbor.getLeft(),direction);
            if(((QiguaiAtmosphere)atmosphere).isDebug()) GeoCraft.getLogger().info("{} calculated wind {} as {} ",getTagName(),direction.name(),newWindSpeed);
            wind = wind.add(newWindSpeed);
        }
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            for(IAtmosphereProperty property: GeographyProperty.MANAGER.getFlowableProperties()){
                property.onFlow(this,chunk,neighbor.getLeft(),neighbor.getMiddle(),neighbor.getRight(),wind);
            }
        }
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable Vec3d direction) {}

    @Override
    public double getBeginY() {
        return lowerLayer!=null?lowerLayer.getTopY():63;
    }

    @Override
    public double getDepth() {
        return 4096;
    }

    @Nonnull
    @Override
    public TemperatureState getTemperature() {
        return temperature;
    }

    @Nullable
    @Override
    public FluidState getWater() {
        return null;
    }

    @Override
    public double getHeatCapacity() {
        return 1e10;
    }

    @Nonnull
    @Override
    public String getTagName() {
        return "va";
    }

    @Override
    public void putHeat(double quanta, @Nullable BlockPos pos) {}

    @Override
    public double drainHeat(double quanta, @Nullable BlockPos pos) {
        return quanta;
    }
}
