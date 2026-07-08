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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.api.util.math.Degree;
import top.qiguaiaaaa.geocraft.geography.atmosphere.SurfaceAtmosphere;
import top.qiguaiaaaa.geocraft.geography.atmosphere.debug.DebugHeatPack;
import top.qiguaiaaaa.geocraft.geography.atmosphere.layer.QiguaiAtmosphereLayer;
import top.qiguaiaaaa.geocraft.util.WaterUtil;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class SurfaceAtmosphereLayer extends QiguaiAtmosphereLayer {
    /**
     * Attention:该温度表示本层下部温度，非中心温度！！！计算请使用中心温度，不然会出现严重失真！！！！
     */
    protected final TemperatureState temperature = GeoCraftProperties.TEMPERATURE.getStateInstance();
    //缓存数值
    protected double 长波吸收率,长波发射率,平均密度 = 1,
            本层气压 = AtmosphereUtil.Constants.海平面气压,
            本层体积 = AtmosphereUtil.Constants.大气单元底面积* Altitude.to物理高度(20),
            中心温度 = temperature.get(),
            起始高度, 相对起始高度;
    public SurfaceAtmosphereLayer(SurfaceAtmosphere atmosphere) {
        super(atmosphere);
        states.put(GeoCraftProperties.TEMPERATURE, temperature);
    }

    // *********
    // 基本参数相关
    // *********

    /**
     * 计算本层大气的短波辐射透过率
     * @return 短波辐射参数，透过率和吸收率，介于0~1之间
     */
    public double[] 计算短波辐射参数(Degree 与水平面夹角) {
        double 液态水每平方质量 = water.getFluidAmount()/ AtmosphereUtil.Constants.大气单元底面积;
        double 气态水每平方质量 = steam.getFluidAmount() / AtmosphereUtil.Constants.大气单元底面积;
        double 空气密度 = getDensity();

        double sin = Math.sin(与水平面夹角.getRadian());
        sin = Math.max(sin, 0.0001);

        //计算各部分光学厚度
        double 云光学厚度 = AtmosphereUtil.Constants.质量消光系数_云*液态水每平方质量;
        double 大气光学厚度 = AtmosphereUtil.Constants.质量消光系数_气体*空气密度*Altitude.to物理高度(getDepth());
        double 水汽光学厚度 = AtmosphereUtil.Constants.质量消光系数_水汽 * 气态水每平方质量;
        double 总光学厚度 = 云光学厚度+大气光学厚度+水汽光学厚度;

        //SSA（单散射比）
        final double SSA_cloud = 0.99;
        final double SSA_gas   = 0.01;
        final double SSA_steam = 0.01;
        double SSA_total = (云光学厚度 * SSA_cloud + 水汽光学厚度 * SSA_steam + 大气光学厚度 * SSA_gas) / 总光学厚度;

        // Beer–Lambert
        double 透过率 = MathHelper.clamp(Math.exp(-总光学厚度/sin), 0.0, 1.0);
        double 吸收率 = (1-透过率) * (1.0 - SSA_total);

        return new double[]{透过率,吸收率};
    }

    /**
     * 对本层大气的长波辐射参数进行更新
     */
    public void 更新长波辐射参数(){
        double 水汽路径 = steam.getFluidAmount() / AtmosphereUtil.Constants.大气单元底面积;
        double 液态水路径 = water.getFluidAmount() / AtmosphereUtil.Constants.大气单元底面积;

        double 温室气体效应 = AtmosphereUtil.Constants.大气单元底面积*
                Altitude.to物理高度(getDepth())*
                平均密度*
                AtmosphereUtil.Constants.温室气体浓度*
                AtmosphereUtil.Constants.温室气体吸收系数;

        长波吸收率 = 1.0 - Math.exp(
                -AtmosphereUtil.Constants.水汽长波吸收系数 * 水汽路径
                        - AtmosphereUtil.Constants.液态水长波吸收系数 * 液态水路径
                        - 温室气体效应
        );

        //基尔霍夫热辐射定律
        长波发射率 = 长波吸收率;

        // 确保值在合理范围内
        长波吸收率 = MathHelper.clamp(长波吸收率, 0.0, 0.999);
        长波发射率 = MathHelper.clamp(长波发射率, 0.0, 0.999);
    }

    public void 更新高度缓存(){
        起始高度 = lowerLayer.getTopY();
        相对起始高度 =起始高度-atmosphere.getUnderlying(new BlockPos(0,getCenterY(),0)).getTopY();
    }

    public void 更新缓存(){
        更新高度缓存();
        本层体积 = AtmosphereUtil.Constants.大气单元底面积*Altitude.to物理高度(getDepth());
        中心温度 = getTemperature(new BlockPos(0,getCenterY(),0),false);
        本层气压 = getPressure();
        平均密度 = getDensity();
    }

    public void updateHeatCapacity(){
        heatCapacity = AtmosphereUtil.Constants.大气单元底面积*
                Altitude.to物理高度(getDepth()) *
                平均密度*
                AtmosphereUtil.Constants.干空气比热容;
        heatCapacity += water.getFluidAmount()* 4200;
        heatCapacity += steam.getFluidAmount()* 1860;
        if(Double.isNaN(heatCapacity) || Double.isInfinite(heatCapacity)){
            heatCapacity = 1e8; //防止出现问题
        }
        if(heatCapacity<1e7) heatCapacity = 1e7;
    }

    // *********
    // 大气更新相关
    // *********

    /**
     * 计算朝向目标大气的风速分量(包含垂直分量)
     * @param to 目标大气
     * @param dir 目标大气相对于自身的方向
     * @return 风速分量
     */
    @Override
    public Vec3d 计算水平风速分量(Atmosphere to, EnumFacing dir){
        Vec3d wind = Vec3d.ZERO;
        Altitude 对方平均海拔 = to.getUnderlying(new BlockPos(0,getCenterY(),0)).getAltitude();
        if(getTopY()>对方平均海拔.get()){
            double 对方大气同高度气压 = to.getPressure(new BlockPos(0,getCenterY(),0));
            double 水平风 = Math.sqrt(Math.abs(本层气压-对方大气同高度气压)/平均密度)/4*(本层气压>对方大气同高度气压?1:-1);

            double 海拔差 = 对方平均海拔.get()-getBeginY();
            if(海拔差<2){
                wind = new Vec3d(dir.getDirectionVec()).scale(水平风);
            }else{
                wind = 计算坡度修饰后风速分量(水平风,海拔差,dir);
            }
        }
        wind.add(super.计算水平风速分量(to,dir));
        return wind;
    }
    @Override
    public double 计算垂直对流速度() {
        if (upperLayer == null) return 0.0;

        double 本层温度 = 中心温度;
        double 上层温度 = upperLayer.getTemperature(new BlockPos(0,upperLayer.getBeginY()+upperLayer.getDepth()/2,0));

        // 假设气块从本层绝热抬升到上层高度
        double 抬升高度 = Altitude.to物理高度(upperLayer.getBeginY()+upperLayer.getDepth()/2 - getCenterY());
        double 气块温度 = 本层温度 - AtmosphereUtil.Constants.干绝热温度直减率 * 抬升高度;

        double 温度差 = 气块温度 - 上层温度;
        double 浮力加速度 = AtmosphereUtil.Constants.重力加速度 * 温度差 / 上层温度;

        return Math.max(浮力加速度,0);
    }

    public abstract Vec3d 计算上风速();
    public abstract Vec3d 计算下风速();

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
    @Override
    protected double 计算热量平流量(Atmosphere to, double windSpeed){
        final double referenceWind = 32.0; // 归一化
        double toTemp = to.getAtmosphereTemperature(new BlockPos(0,getCenterY(),0));
        double diff = 中心温度 - toTemp;
        return MathHelper.clamp(diff * MathHelper.clamp(windSpeed/referenceWind,-1.6,1.6)
                ,-Math.abs(diff/3),Math.abs(diff/3))*heatCapacity/2;
    }

    @Override
    protected void 热量平流(Atmosphere to,EnumFacing dir) {
        if (to.getUnderlying(new BlockPos(0,getCenterY(),0)).getAltitude().get() > getTopY()) return;
        double windSpeedSize = Math.abs(MathUtil.获得带水平正负方向的速度(winds.get(dir),dir));
        if (windSpeedSize == 0) return;
        double heatTransferQuantity = 计算热量平流量(to, windSpeedSize);

        if(((SurfaceAtmosphere)atmosphere).isDebug())
            GeoCraft.getLogger().info("{} flow heat {} FE to {} ({} K changed),wind = {}. temperature diff = {} K .to temp {} , to layer {} ,to pressure {} Pa . me pressure {}, me density {}",
                    getTagName(),heatTransferQuantity,dir.name(),
                    -heatTransferQuantity/heatCapacity,
                    windSpeedSize,
                    中心温度-to.getAtmosphereTemperature(new BlockPos(0,getCenterY(),0)),
                    to.getAtmosphereTemperature(new BlockPos(0,getCenterY(),0)),
                    to.getLayer(new BlockPos(0,getCenterY(),0)).getTagName(),
                    to.getPressure(new BlockPos(0,getCenterY(),0)),
                    本层气压,平均密度);

        to.putHeat(heatTransferQuantity, new BlockPos(0, getCenterY(), 0));
        temperature.addHeat(-heatTransferQuantity, heatCapacity);
    }

    protected void 水汽蒸发(){
        double 期望蒸发量 = water.getFluidAmount();
        if(期望蒸发量 > 64){
            double left = 期望蒸发量 - 64;
            final int maxLeft = 128;
            final double smoothingConstant = 4;
            left /= smoothingConstant;
            期望蒸发量 = 64d + maxLeft*left/(left+maxLeft);
        }
        int 实际蒸发量;
        if( Double.isNaN(期望蒸发量) || Double.isInfinite(期望蒸发量)
                || (实际蒸发量 = Math.min((int)期望蒸发量,water.getFluidAmount()))<=0)
            return;
        if(water.fill(-实际蒸发量,true)==0) return;
        steam.fill(实际蒸发量,true);
        double 能量吸收量 = ((double) 实际蒸发量)* AtmosphereUtil.Constants.水汽化热;
        能量吸收量 = Math.min(能量吸收量,heatCapacity*10);
        temperature.addHeat(-能量吸收量,heatCapacity);
    }

    protected void 水汽凝结(){
        double 饱和水汽压 = WaterUtil.计算饱和水汽压(中心温度)
                ,实际水汽压 = getWaterPressure();
        if(实际水汽压<=饱和水汽压) return;
        double 期望凝结量;
        if(饱和水汽压<=0){
            期望凝结量 = steam.getFluidAmount();
        }else{
            //计算实际需要凝结的水质量。饱和水汽压时，水汽质量 P = mRT/MSh -> m=PMSh/RT
            double 饱和水汽质量 = 饱和水汽压* AtmosphereUtil.Constants.水摩尔质量* AtmosphereUtil.Constants.大气单元底面积*Altitude.to物理高度(getDepth())
                    / Math.max(AtmosphereUtil.Constants.气体常数*中心温度,1);
            期望凝结量 = steam.getFluidAmount()-饱和水汽质量;
            if(期望凝结量 > 32){
                double left = 期望凝结量 - 32;
                final int maxLeft = 64;
                final double smoothingConstant = 8;
                left /= smoothingConstant;
                期望凝结量 = 32d + maxLeft*left/(left+maxLeft);
            }
        }
        int 实际凝结量;
        if( Double.isNaN(期望凝结量) || Double.isInfinite(期望凝结量)
                || (实际凝结量 = Math.min((int)期望凝结量,steam.getFluidAmount()))<=0)
            return;
        if(steam.fill(-实际凝结量,true)==0) return;
        water.fill(实际凝结量,true);
        double 能量释放量 = ((double) 实际凝结量)* AtmosphereUtil.Constants.水汽化热;
        能量释放量 = Math.min(能量释放量,heatCapacity*10);
        temperature.addHeat(能量释放量,heatCapacity);
        if(((SurfaceAtmosphere)atmosphere).isDebug())
            GeoCraft.getLogger().info("{} has water pressure {} Pa > {} Pa ,should transfer {} mB = " +
                            " {} - {} * {} * {} * {} / ({} * {})"+
                            " steam to water",getTagName(),实际水汽压,饱和水汽压,实际凝结量
                    ,steam.getFluidAmount()+实际凝结量,饱和水汽压, AtmosphereUtil.Constants.水摩尔质量, AtmosphereUtil.Constants.大气单元底面积,Altitude.to物理高度(getDepth()),
                    AtmosphereUtil.Constants.气体常数,中心温度);
    }

    @Override
    public void onLoad(@Nullable Chunk chunk) {
        onLoadWithoutChunk();
        if(chunk == null) return;
        更新缓存();
        updateHeatCapacity();
        更新长波辐射参数();
    }

    @Override
    public void tick(@Nullable Chunk chunk, @Nonnull Map<EnumFacing, Triple<Atmosphere,Chunk,EnumFacing>> neighbors, int x, int z) {
        updateHeatCapacity();
        更新长波辐射参数();
        double[] 辐射能量 = 对外长波辐射();
        double 总辐射量 = 辐射能量[0]+辐射能量[1];
        if(((SurfaceAtmosphere)atmosphere).isDebug())
            GeoCraft.getLogger().info("{} send radiation by {} FE (-{} K)",getTagName(),总辐射量,总辐射量/heatCapacity);

        temperature.addHeat(-总辐射量, heatCapacity);

        if (lowerLayer != null) {
            HeatPack 向下辐射包 = ((SurfaceAtmosphere)atmosphere).isDebug()?
                    new DebugHeatPack(HeatPack.HeatType.LONG_WAVE, 辐射能量[0]):
                    new HeatPack(HeatPack.HeatType.LONG_WAVE, 辐射能量[0]);
            lowerLayer.sendHeat(向下辐射包,EnumFacing.DOWN);
        }
        if (upperLayer != null) {
            HeatPack 向上辐射包 = ((SurfaceAtmosphere)atmosphere).isDebug()?
                    new DebugHeatPack(HeatPack.HeatType.LONG_WAVE, 辐射能量[1]):
                    new HeatPack(HeatPack.HeatType.LONG_WAVE, 辐射能量[1]);
            upperLayer.sendHeat(向上辐射包,EnumFacing.UP);
        }
        更新缓存();
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            EnumFacing direction = neighbor.getRight();
            Vec3d newWindSpeed = 计算水平风速分量(neighbor.getLeft(),direction);
            if(((SurfaceAtmosphere)atmosphere).isDebug()) GeoCraft.getLogger().info("{} calculated wind {} as {} ",getTagName(),direction.name(),newWindSpeed);
            winds.put(direction,newWindSpeed);
        }
        //大气平流
        for(Triple<Atmosphere,Chunk,EnumFacing> neighbor:neighbors.values()){
            大气平流(chunk,neighbor);
        }
        //垂直计算
        winds.put(EnumFacing.UP, 计算上风速());
        winds.put(EnumFacing.DOWN,计算下风速());
        对流();
        if(中心温度<TemperatureProperty.BOILED_POINT) 水汽凝结();
        else 水汽蒸发();
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable EnumFacing direction) {
        if(pack.isEmpty()) return;
        if(direction == null || pack.getType() == null){
            this.putHeat(pack.getAmount(),null);
            return;
        }
        double 透过率=0.98;
        double 吸收率=0.01;

        switch (pack.getType()){
            case SHORT_WAVE:
                Degree degree = Degree.RIGHT_ANGLE;
                double[] 参数 = 计算短波辐射参数(degree);
                透过率 = 参数[0];
                吸收率 = 参数[1];
                break;
            case LONG_WAVE:
                吸收率 = 长波吸收率;
                透过率 = 1.0 - 吸收率;
                if(((SurfaceAtmosphere)atmosphere).isDebug())
                    GeoCraft.getLogger().info("{} received LONG WAVE pack, will pass {} and absorb {} ,dir is {}",
                            getTagName(),透过率,吸收率,direction.name());
                break;
        }

        double 吸收量 = pack.getAmount()*吸收率,
                不透过量 = pack.getAmount()*(1-透过率);

        temperature.addHeat(pack.drawHeat(吸收量),heatCapacity);
        pack.drawHeat(Math.max(不透过量-吸收量,0));

        if(pack.isEmpty()) return;

        if(direction == EnumFacing.DOWN && lowerLayer != null){
            lowerLayer.sendHeat(pack,direction);
        }else if(direction == EnumFacing.UP && upperLayer != null){
            upperLayer.sendHeat(pack,direction);
        }
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack, @Nullable Vec3d direction) {
        if(pack.isEmpty()) return;
        if(direction == null || pack.getType() == null){
            this.putHeat(pack.getAmount(),null);
            return;
        }
        double 透过率=0.98;
        double 吸收率=0.01;

        switch (pack.getType()){
            case SHORT_WAVE:
                Degree degree = MathUtil.计算与水平面夹角(direction);
                double[] 参数 = 计算短波辐射参数(degree);
                透过率 = 参数[0];
                吸收率 = 参数[1];
                if(((SurfaceAtmosphere)atmosphere).isDebug())
                    GeoCraft.getLogger().info("{} received SHORT WAVE pack, will pass {} and absorb {} ,degree is {}",
                            getTagName(),透过率,吸收率,degree.getDegree());
                break;
            case LONG_WAVE:
                吸收率 = 长波吸收率;
                透过率 = 1.0 - 吸收率;
                break;
        }

        double 吸收量 = pack.getAmount()*吸收率,
                不透过量 = pack.getAmount()*(1-透过率);

        temperature.addHeat(pack.drawHeat(吸收量),heatCapacity);
        pack.drawHeat(Math.max(不透过量-吸收量,0));

        if(pack.isEmpty()) return;

        if(direction.y<0 && lowerLayer != null){
            lowerLayer.sendHeat(pack,direction);
        }else if(direction.y>0 && upperLayer != null){
            upperLayer.sendHeat(pack,direction);
        }
    }

    @Override
    public double getBeginY() {
        return 起始高度;
    }

    @Override
    public void setLowerLayer(Layer layer) {
        super.setLowerLayer(layer);
        isLowerLayerValid = layer instanceof AtmosphereLayer;
        if(isLowerLayerValid) low = (AtmosphereLayer) layer;
        else low = null;
        更新高度缓存();
        if(upperLayer !=null) upperLayer.setLowerLayer(this); //刷新高度
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
                中心温度
                / (
                AtmosphereUtil.Constants.水摩尔质量 *
                        AtmosphereUtil.Constants.大气单元底面积 *
                        (Altitude.to物理高度(getDepth()))
        );
    }

    /**
     * 获取大气平均大气压
     * @return 平均大气压,单位为Pa
     */
    public double getPressure() {
        return Math.max(AtmosphereUtil.Constants.海平面气压 *
                Math.exp(
                        -AtmosphereUtil.Constants.干空气摩尔质量 *
                                AtmosphereUtil.Constants.重力加速度 *
                                Altitude.get物理海拔(getCenterY()) /
                                (AtmosphereUtil.Constants.气体常数 * 中心温度)
                ),10000);
    }

    @Override
    public double getPressure(@Nonnull BlockPos pos) {
        if(shouldSwitchToLowerLayer(pos)) return low.getPressure(pos);
        if(shouldSwitchToUpperLayer(pos)) return up.getPressure(pos);
        return AtmosphereUtil.Constants.海平面气压 *
                Math.exp(
                        -AtmosphereUtil.Constants.干空气摩尔质量 *
                                AtmosphereUtil.Constants.重力加速度 *
                                Altitude.get物理海拔(pos.getY()) /
                                (AtmosphereUtil.Constants.气体常数 * getTemperature(pos,false))
                );
    }

    /**
     * 获取大气平均密度
     * @return 平均密度,单位 kg/m^3
     */
    public double getDensity(){
        final double eps = 0.622;               // ε = Mv / Md ≈ 0.622
        double P = 本层气压;
        double waterPressure = getWaterPressure();
        double T = 中心温度;

        waterPressure = MathHelper.clamp(waterPressure,0,0.9999 * P);

        //虚温法
        double r = eps * waterPressure / Math.max(1, P);
        double modifiedT = T * (1.0 + 0.61 * r);
        return P / (AtmosphereUtil.Constants.干空气比热容 * modifiedT);       // kg/m^3
    }

    @Nonnull
    @Override
    public TemperatureState getTemperature() {
        return temperature;
    }
}
