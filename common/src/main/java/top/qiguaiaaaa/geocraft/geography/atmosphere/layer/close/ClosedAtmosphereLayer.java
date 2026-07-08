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

package top.qiguaiaaaa.geocraft.geography.atmosphere.layer.close;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.geography.atmosphere.QiguaiAtmosphere;
import top.qiguaiaaaa.geocraft.geography.atmosphere.layer.QiguaiAtmosphereLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class ClosedAtmosphereLayer extends QiguaiAtmosphereLayer {
    protected static final Random random = new Random();
    protected TemperatureState temperature = GeoCraftProperties.FINAL_TEMPERATURE.getStateInstance();
    protected double maxWindSpeed = 4
            ,pressure = 2e5;
    public ClosedAtmosphereLayer(QiguaiAtmosphere atmosphere) {
        super(atmosphere);
        this.heatCapacity = 1e10;
        states.put(GeoCraftProperties.FINAL_TEMPERATURE,temperature);
    }

    public void setMaxWindSpeed(double maxWindSpeed) {
        if(maxWindSpeed<0) throw new IllegalArgumentException();
        this.maxWindSpeed = maxWindSpeed;
    }

    public void setPressure(double pressure) {
        if(pressure <0) throw new IllegalArgumentException();
        this.pressure = pressure;
    }

    @Override
    public Vec3d 计算水平风速分量(Atmosphere to, EnumFacing dir) {
        return new Vec3d(dir.getDirectionVec()).scale(random.nextDouble()*maxWindSpeed-maxWindSpeed/2).add(super.计算水平风速分量(to,dir));
    }

    @Override
    protected void 对流() {}

    @Override
    protected double[] 对外长波辐射() {
        return new double[]{0,0};
    }

    @Override
    public double getPressure(@Nonnull BlockPos pos) {
        return pressure;
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos, boolean notAir) {
        return temperature.get();
    }

    @Override
    public void tick(@Nullable Chunk chunk, @Nonnull Map<EnumFacing, Triple<Atmosphere, Chunk, EnumFacing>> neighbors, int x, int z) {
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            EnumFacing direction = neighbor.getRight();
            Vec3d newWindSpeed = 计算水平风速分量(neighbor.getLeft(),direction);
            if(((QiguaiAtmosphere)atmosphere).isDebug()) GeoCraft.getLogger().info("{} calculated wind {} as {} ",getTagName(),direction.name(),newWindSpeed);
            winds.put(direction,newWindSpeed);
        }
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            大气平流(chunk,neighbor);
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
        return 256;
    }

    @Nonnull
    @Override
    public TemperatureState getTemperature() {
        return temperature;
    }

    @Nonnull
    @Override
    public String getTagName() {
        return "ca";
    }

    @Override
    public void putHeat(double quanta, @Nullable BlockPos pos) {}

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable EnumFacing direction) {}

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable Vec3i direction) {}

    @Override
    public double drainHeat(double quanta, @Nullable BlockPos pos) {
        return quanta;
    }
}
