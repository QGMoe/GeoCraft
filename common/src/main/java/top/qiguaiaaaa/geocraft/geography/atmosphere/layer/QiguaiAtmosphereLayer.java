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

package top.qiguaiaaaa.geocraft.geography.atmosphere.layer;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.BaseAtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.property.GeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.IAtmosphereProperty;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.state.GeographyState;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.geography.atmosphere.QiguaiAtmosphere;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public abstract class QiguaiAtmosphereLayer extends BaseAtmosphereLayer{
    protected final Map<EnumFacing, Vec3d> winds = new EnumMap<>(EnumFacing.class);
    protected double heatCapacity = Float.MAX_VALUE/2;
    protected final FluidState water = GeoCraftProperties.WATER.getStateInstance();
    protected final FluidState steam = GeoCraftProperties.STEAM.getStateInstance();
    protected boolean isUpperLayerValid,isLowerLayerValid;
    protected AtmosphereLayer up, low;
    public QiguaiAtmosphereLayer(QiguaiAtmosphere atmosphere) {
        super(atmosphere);
        for(EnumFacing facing: ChunkUtil.HORIZONTALS){
            winds.put(facing,Vec3d.ZERO);
        }
        winds.put(EnumFacing.UP,Vec3d.ZERO);
        winds.put(EnumFacing.DOWN,Vec3d.ZERO);
        states.put(GeoCraftProperties.WATER,water);
        states.put(GeoCraftProperties.STEAM,steam);
    }

    // *********
    // 大气更新相关
    // *********

    //水平运动

    protected Vec3d 计算坡度修饰后风速分量(double 水平风,double 海拔差,EnumFacing dir){
        if(Math.abs(水平风)<0.1) return new Vec3d(dir.getDirectionVec()).scale(水平风);
        double 重力风 = Math.sqrt(2 * AtmosphereUtil.Constants.重力加速度 * Math.abs(海拔差)) * 0.01;
        double tan坡角 = 海拔差/16;
        if(水平风*tan坡角<0){
            return new Vec3d(dir.getDirectionVec()).scale(水平风).add(0,Math.max(-重力风,水平风*tan坡角),0);
        }
        double 修正水平风;
        double 坡角 = Math.atan(tan坡角);
        if(海拔差>0){
            水平风 *= -Math.pow(海拔差/getDepth(),2)+1;
        }

        double a = (1+tan坡角*tan坡角)*(水平风*水平风),
                b = 2*水平风*重力风*tan坡角,
                c = 重力风*重力风-水平风*水平风;
        double delta = b*b-4*a*c;
        if(delta <0.1){
            修正水平风 = -水平风*Math.cos(坡角);
        }else{
            修正水平风 = 水平风*MathHelper.clamp((-b+Math.sqrt(delta))/(2*a),-1,1);
        }
        if(Double.isInfinite(修正水平风) || Double.isNaN(修正水平风)) return Vec3d.ZERO;
        return new Vec3d(dir.getDirectionVec()).scale(修正水平风).add(0,修正水平风*tan坡角,0);
    }

    /**
     * 计算朝向目标大气的风速分量(包含垂直分量)
     * @param to 目标大气
     * @param dir 目标大气相对于自身的方向
     * @return 风速分量
     */
    public Vec3d 计算水平风速分量(Atmosphere to, EnumFacing dir){
        Vec3d wind = Vec3d.ZERO;
        for(IAtmosphereProperty property: GeographyProperty.MANAGER.getWindEffectedProperties()){
            wind= wind.add(property.getWind(this,to,dir));
        }
        return wind;
    }

    //垂直运动

    public double 散度(){
        return (winds.get(EnumFacing.EAST).x-winds.get(EnumFacing.WEST).x)/16+
                (winds.get(EnumFacing.SOUTH).z-winds.get(EnumFacing.NORTH).z)/16;
    }

    public double 计算垂直对流速度() {
        return 0;
    }

    // *********
    // 大气热量、物质交换
    // *********

    // 水平热量、物质交换

    /**
     * 计算向目标大气传送的热量
     * @param to 目标大气
     * @param windSpeed 大气间风速绝对值
     * @return 互相传输的热量，正为A向B传输，负为B向A传输
     */
    protected double 计算热量平流量(Atmosphere to, double windSpeed){
        return 0;
    }

    protected void 热量平流(Atmosphere to,EnumFacing dir) {
    }

    protected void 大气平流(Chunk chunk, Triple<Atmosphere,Chunk,EnumFacing> neighbor){
        //能量平流
        热量平流(neighbor.getLeft(),neighbor.getRight());
        //物质和其他属性平流
        for(IAtmosphereProperty property: GeographyProperty.MANAGER.getFlowableProperties()){
            property.onFlow(this,chunk,neighbor.getLeft(),neighbor.getMiddle(),neighbor.getRight(),winds.get(neighbor.getRight()));
        }
    }

    // 垂直方向能量、物质交换

    protected abstract void 对流();

    // *********
    // 大气自身状态更新
    // *********

    /**
     * 计算对外长波辐射向下、上的量
     * @return 两个值，第一个表示向下，第二个表示向上
     */
    protected abstract double[] 对外长波辐射();

    @Override
    public void onLoad(@Nullable Chunk chunk) {
        onLoadWithoutChunk();
    }

    public void onLoadWithoutChunk() {
        for(GeographyState state:states.values())
            if(!state.isLoaded())
                state.load(this);
    }

    @Override
    public void setUpperLayer(Layer layer) {
        super.setUpperLayer(layer);
        isUpperLayerValid = layer instanceof AtmosphereLayer;
        if(isUpperLayerValid) up = (AtmosphereLayer) layer;
        else low = null;
    }

    @Override
    public int addSteam(@Nullable BlockPos pos, int amount,boolean doAdd) {
        return steam.fill(amount,doAdd);
    }

    @Override
    public int addWater(@Nullable BlockPos pos, int amount,boolean doAdd) {
        return water.fill(amount,doAdd);
    }

    @Override
    public double getWaterPressure(@Nonnull BlockPos pos) {
        if(shouldSwitchToLowerLayer(pos)) return low.getWaterPressure(pos);
        if(shouldSwitchToUpperLayer(pos)) return up.getWaterPressure(pos);
        return getWaterPressure();
    }

    /**
     * 获取大气平均水汽压
     * @return 大气平均水汽压,单位为Pa
     */
    @Override
    public double getWaterPressure() {
        FluidState steam = getSteam();
        if(steam == null) return 0;
        // PV=nRT -> P = nRT/V -> P = mRT/MSh
        return steam.getFluidAmount()*
                AtmosphereUtil.Constants.气体常数*
                getTemperature().get()
                / (
                AtmosphereUtil.Constants.水摩尔质量 *
                        AtmosphereUtil.Constants.大气单元底面积 *
                        (Altitude.to物理高度(getDepth()))
        );
    }

    @Nonnull
    @Override
    public Vec3d getWind(@Nonnull BlockPos pos) {
        if(shouldSwitchToLowerLayer(pos)) return low.getWind(pos);
        if(shouldSwitchToUpperLayer(pos)) return up.getWind(pos);
        int x = pos.getX() & 15;
        int z = pos.getZ() & 15;
        double weightS = z/16.0,
                weightN = 1-weightS,
                weightE = x/16.0,
                weightW = 1-weightE,
                weightUP = Math.max(pos.getY()-getBeginY(),0)/getDepth(),
                weightDOWN = 1-weightUP;
        return winds.get(EnumFacing.SOUTH).scale(weightS)
                .add(winds.get(EnumFacing.NORTH).scale(weightN))
                .add(winds.get(EnumFacing.EAST).scale(weightE))
                .add(winds.get(EnumFacing.WEST).scale(weightW))
                .add(winds.get(EnumFacing.UP).scale(weightUP))
                .add(winds.get(EnumFacing.DOWN).scale(weightDOWN));
    }

    public Vec3d getWind(EnumFacing facing){
        return winds.get(facing);
    }

    @Override
    public double getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public FluidState getWater() {
        return water;
    }

    @Nullable
    @Override
    public FluidState getSteam() {
        return steam;
    }

    public double getCenterY(){
        return getBeginY()+getDepth()/2;
    }

    protected boolean shouldSwitchToLowerLayer(BlockPos pos){
        return pos.getY()<getBeginY()&& isLowerLayerValid;
    }
    protected boolean shouldSwitchToUpperLayer(BlockPos pos){
        return pos.getY()>getBeginY()+getDepth() && isUpperLayerValid;
    }
}
