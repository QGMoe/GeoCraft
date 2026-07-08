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

package top.qiguaiaaaa.geocraft.geography.atmosphere.layer.surface;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.UnderlyingLayer;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.api.setting.GeoAtmosphereSetting;
import top.qiguaiaaaa.geocraft.api.setting.GeoBlockSetting;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.geography.property.AltitudeProperty;
import top.qiguaiaaaa.geocraft.geography.state.AltitudeState;
import top.qiguaiaaaa.geocraft.geography.state.HeatCapacityState;
import top.qiguaiaaaa.geocraft.geography.state.ReflectivityState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static top.qiguaiaaaa.geocraft.util.ChunkUtil.getSameLiquidDepth;

public class SurfaceUnderlying extends UnderlyingLayer {
    public static final int 过渡距离 = 3;
    public static final float 地底温度受地表影响系数 = 0.001f;
    public double 平均返照率;
    protected final TemperatureState temperature = GeoCraftProperties.TEMPERATURE.getStateInstance();
    protected TemperatureState deepTemperature = GeoCraftProperties.DEEP_TEMPERATURE.getStateInstance();
    protected AltitudeState altitudeState = new AltitudeState(altitude);
    protected HeatCapacityState heatCapacityState = new HeatCapacityState();
    protected ReflectivityState reflectivityState = new ReflectivityState();
    protected double 周围区块最高平均海拔 = -100000,周围区块最低平均海拔 = -100000;
    boolean afterFirstTick = false;

    public SurfaceUnderlying(Atmosphere atmosphere) {
        super(atmosphere);
        altitude.set(AltitudeProperty.UNAVAILABLE);
        states.put(GeoCraftProperties.TEMPERATURE, temperature);
        states.put(GeoCraftProperties.DEEP_TEMPERATURE,deepTemperature);
        states.put(altitudeState.getProperty(),altitudeState);
        states.put(heatCapacityState.getProperty(),heatCapacityState);
        states.put(reflectivityState.getProperty(),reflectivityState);
    }

    @Override
    public void putHeat(double quanta, @Nullable BlockPos pos) {
        if(pos != null){
            if(pos.getY()<=altitude.get()- 过渡距离) return;
            else if(pos.getY()<altitude.get()){
                super.putHeat(quanta*(pos.getY()-altitude.get()+ 过渡距离)/ 过渡距离,pos);
                return;
            }
        }
        super.putHeat(quanta, pos);
    }

    @Override
    public double drainHeat(double quanta, @Nullable BlockPos pos) {
        if(pos != null){
            if(pos.getY()<=altitude.get()- 过渡距离) return quanta;
            else if(pos.getY()<altitude.get()){
                double 修饰比 = (pos.getY()-altitude.get()+ 过渡距离)/ 过渡距离;
                return super.drainHeat(quanta*修饰比,pos)/修饰比;
            }
        }
        return super.drainHeat(quanta, pos);
    }

    public void updateAltitude(Chunk chunk){
        setAltitude(Altitude.getMiddleHeight(chunk));
    }

    public void updateNeighborAltitudeInfo(Map<EnumFacing, Triple<Atmosphere,Chunk,EnumFacing>>  neighbors){
        周围区块最高平均海拔 = altitude.get();
        周围区块最低平均海拔 = altitude.get();
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            周围区块最高平均海拔 = Math.max(周围区块最高平均海拔,neighbor.getLeft().getUnderlying(new BlockPos(0,getBeginY(),0)).getAltitude().get());
            周围区块最低平均海拔 = Math.min(周围区块最低平均海拔,neighbor.getLeft().getUnderlying(new BlockPos(0,getBeginY(),0)).getAltitude().get());
        }
    }

    /**
     * 更新下垫面属性
     * @param chunk 下垫面所属区块
     * @return 自身
     */
    @Override
    public SurfaceUnderlying load(@Nonnull Chunk chunk) {
        long heatCapacity = 0;
        double averageReflectivity = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = chunk.getHeightValue(x, z);
                IBlockState state;
                int maxDeep = 10;
                boolean canSunReach = true;
                double upReflectivity = 0;
                int blockC = 0;
                while(maxDeep>0){
                    maxDeep--;
                    state = chunk.getBlockState(x, height, z);
                    if (state.getBlock() == Blocks.AIR && height > 0) {
                        height--;
                        continue;
                    }
                    if(canSunReach){
                        double thisReflectivity = GeoBlockSetting.getBlockReflectivity(state)*(1-upReflectivity);
                        averageReflectivity += thisReflectivity;
                        upReflectivity += thisReflectivity;
                        canSunReach = false;
                    }
                    int C = GeoBlockSetting.getBlockHeatCapacity(state);
                    blockC += C;
                    if(FluidUtil.isFluid(state) && height > 0){
                        blockC += C * getSameLiquidDepth(chunk, x, height - 1, z, FluidUtil.getFluid(state),5);
                        break;
                    }
                    if(!state.isOpaqueCube()){
                        canSunReach = true;
                        height--;
                        continue;
                    }
                    if(state.isFullBlock()) break;
                    height--;
                }
                heatCapacity += blockC * 1000L;
            }
        }
        reflectivityState.reflectivity = 平均返照率 = averageReflectivity / 256;
        heatCapacityState.heatCapacity = this.heatCapacity = heatCapacity;
        if(upperLayer != null) upperLayer.setLowerLayer(this); //刷新缓存高度
        return this;
    }

    @Override
    public void onLoad(@Nullable Chunk chunk) {
        if(chunk != null){
            updateAltitude(chunk);
            周围区块最低平均海拔 = 周围区块最高平均海拔 = altitude.get();
        }
        super.onLoad(chunk);
    }

    @Override
    public void onLoadWithoutChunk() {
        周围区块最低平均海拔 = 周围区块最高平均海拔 = altitude.get();
        super.onLoadWithoutChunk();
    }

    @Override
    public boolean isLoaded() {
        return this.altitudeState.isLoaded() && super.isLoaded();
    }

    @Override
    public void tick(@Nullable Chunk chunk, @Nonnull Map<EnumFacing, Triple<Atmosphere,Chunk,EnumFacing>> neighbors, int x, int z) {
        if(!afterFirstTick){
            updateNeighborAltitudeInfo(neighbors);
            afterFirstTick = true;
        }
        if(altitude.get() <= 0){
            return; //空的，下垫面都没有
        }

        if(atmosphere.tickTime()+x+5L*z % GeoAtmosphereSetting.getUnderlyingReloadGap() == 0 ){
            if(chunk != null) load(chunk);
            updateNeighborAltitudeInfo(neighbors);
        }

        float newTemp = (float) AtmosphereUtil.calcRadiatedTemp(temperature.get(),heatCapacity, AtmosphereUtil.Constants.大气单元底面积,GeoAtmosphereSetting.getSimulationGap(),1d);

        double 地面长波辐射 = heatCapacity*(temperature.get()-newTemp);

        temperature.set(newTemp);
        if(upperLayer == null) return;
        upperLayer.sendHeat(new HeatPack(HeatPack.HeatType.LONG_WAVE,地面长波辐射), EnumFacing.UP);

        //接触式热量传递
        if(!(upperLayer instanceof AtmosphereLayer)) return;
        double tempMin = Math.min(temperature.get(),upperLayer.getTemperature().get());
        double tempDiff = temperature.get()-upperLayer.getTemperature().get();
        double 传递热量 = Math.min(heatCapacity,upperLayer.getHeatCapacity())*
                MathHelper.clamp(
                MathHelper.clamp(tempDiff,-tempMin/8,tempMin/8) *Math.min(获取上面平均风速()+1,20) /32
                        ,-Math.abs(tempDiff)/3,Math.abs(tempDiff)/3);
        temperature.addHeat(-传递热量,heatCapacity);
        upperLayer.putHeat(传递热量,null);

        //更新地底温度
        deepTemperature.set(deepTemperature.get()*(1-地底温度受地表影响系数)+地底温度受地表影响系数*temperature.get());
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable Vec3d direction) {
        if(direction == null || pack.getType() == null || direction.y == 0){
            this.putHeat(pack.getAmount(),null);
            return;
        }
        if(direction.y<0){
            switch (pack.getType()){
                case SHORT_WAVE:
                    temperature.addHeat(pack.drawHeat(pack.getAmount()*(1-平均返照率)),heatCapacity);
                    break;
                case LONG_WAVE:
                    temperature.addHeat(pack.drawHeat(pack.getAmount()),heatCapacity);
                    break;
            }
            if(upperLayer == null) return;
            upperLayer.sendHeat(pack,new Vec3d(direction.x,-direction.y,direction.z));
        }else if(direction.y >0){
            if(upperLayer == null) return;
            upperLayer.sendHeat(pack,direction);
        }
    }


    @Override
    public double getDepth() {
        return altitude.get()-getBeginY();
    }

    @Nonnull
    @Override
    public TemperatureState getTemperature() {
        return temperature;
    }

    @Override
    public float getTemperature(@Nonnull BlockPos pos) {
        if(pos.getY()<-0.1) return getTemperature(new BlockPos(pos.getX(),0,pos.getZ()));
        if(pos.getY()<=altitude.get()- 过渡距离){
            double 深度 = Altitude.to物理高度((altitude.get()- 过渡距离)-pos.getY());
            return (float) (deepTemperature.get()+深度* AtmosphereUtil.Constants.地下温度直增率);
        }
        if(pos.getY()<=altitude.get()){
            return (float) ((temperature.get()-deepTemperature.get())*(pos.getY()-altitude.get()+ 过渡距离)/ 过渡距离 +deepTemperature.get());
        }
        double 高差 = Altitude.to物理高度(pos.getY()-altitude.get());
        return (float) Math.max(temperature.get()-高差* AtmosphereUtil.Constants.对流层温度直减率, TemperatureProperty.MIN);
    }

    @Nonnull
    @Override
    public String getTagName() {
        return "g";
    }

    protected double 获取上面平均风速(){
        if(!(upperLayer instanceof AtmosphereLayer)) return 0;
        AtmosphereLayer layer = (AtmosphereLayer) upperLayer;
        double wind = layer.getWind(new BlockPos(4,altitude.get(),4)).length()+layer.getWind(new BlockPos(4,altitude.get(),8)).length()
                +layer.getWind(new BlockPos(8,altitude.get(),4)).length()+layer.getWind(new BlockPos(8,altitude.get(),8)).length();
        return wind/4;
    }
}
