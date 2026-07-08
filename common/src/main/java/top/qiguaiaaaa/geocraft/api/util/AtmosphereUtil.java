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

package top.qiguaiaaaa.geocraft.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.setting.GeoAtmosphereSetting;
import top.qiguaiaaaa.geocraft.api.util.math.Degree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气的相关工具
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class AtmosphereUtil {
    public static double getSunEnergyPerChunk(@Nonnull WorldInfo worldInfo){
        return Math.sin(getSunHeight(worldInfo).getRadian())* Constants.每秒区块获得能量* GeoAtmosphereSetting.getSimulationGap();
    }

    public static double calcRadiatedTemp(final double T0,final double heatCapacity,final double area,final double time,final double emissivity){
        return Math.pow(1d/Math.pow(T0,3)+3d*area*emissivity*Constants.斯特藩_玻尔兹曼常数*time/heatCapacity,-1d/3d);
    }

    public static double calcRadiation(final double T0,final double heatCapacity,final double area,final double time,final double emissivity){
        return heatCapacity*(T0-calcRadiatedTemp(T0, heatCapacity, area, time, emissivity));
    }

    /**
     * 获取太阳高度角
     * @since 0.1
     * @return 太阳高度角
     */
    @Nonnull
    public static Degree getSunHeight(@Nonnull WorldInfo worldInfo){
        long dayTime= (worldInfo.getWorldTime()+6000)%24000;
        if(dayTime<6000 || dayTime>18000) return new Degree(0);
        return new Degree((Math.PI*((6000-Math.abs(12000-dayTime))/6000.0d))/2.0,true);
    }

    /**
     * 获取经过天光处理的的 Accessor
     * @since 0.2.0
     * @param world 世界
     * @param pos 位置
     * @param notAir 是否不为空气
     * @return 一个 {@link IAtmosphereAccessor}实例
     */
    @Nullable
    public static IAtmosphereAccessor getLightedAtmosphereAccessor(@Nonnull World world, @Nonnull BlockPos pos, boolean notAir){
        @Nullable IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(world,pos,notAir);
        if(accessor != null){
            int light = world.getLightFor(EnumSkyBlock.SKY,pos);
            accessor.setSkyLight(light);
        }
        return accessor;
    }

    /**
     * 根据太阳高度角和方位角计算太阳方向向量
     * 注意：这个向量是从太阳指向地面的方向
     *
     * @since 0.1
     * @param 高度角 太阳光线与水平面的夹角
     * @param 方位角 太阳相对于正北的方向角（0°为正北，90°为正东，180°为正南，270°为正西）
     * @return 单位方向向量
     */
    @Nonnull
    public static Vec3d calculateSunDirection(@Nonnull Degree 高度角,@Nonnull Degree 方位角) {
        double sunH = 高度角.getRadian();
        double direction = 方位角.getRadian();

        // X: 东-西方向（东为正）
        // Z: 北-南方向（南为正）
        // Y: 垂直方向（上为正）
        double x = Math.sin(direction) * Math.cos(sunH);
        double z = -Math.cos(direction) * Math.cos(sunH);
        double y = -Math.sin(sunH);

        return new Vec3d(x, y, z).normalize();
    }

    public static final class Constants {
        public static final double 大气单元边长 = 16.0;
        public static final double 大气单元底面积 = 大气单元边长 * 大气单元边长;
        public static final int 太阳常数 = 1361;
        public static final double 斯特藩_玻尔兹曼常数 = 5.670374419e-8; // W·m^-2·K^-4
        public static final double 每秒区块获得能量 = 太阳常数 *大气单元底面积;
        public static final double 每秒损失能量常数 = 斯特藩_玻尔兹曼常数*大气单元底面积;
        public static final int WATER_MELT_LATENT_HEAT_PER_QUANTA = 41750000;
        public static final int 水汽化热 = 2266*1000; //FE/kg
        public static final int WATER_EVAPORATE_LATENT_HEAT_PER_QUANTA = 水汽化热*125;

        public static final double 质量消光系数_云 = 0.1;
        public static final double 质量消光系数_气体 = 1e-5;
        public static final double 质量消光系数_水汽 = 0.005;
        // 标准大气常数（对流层内）
        public static final double 海平面气压 = 101325.0;    // Pa
        public static final double 重力加速度 = 9.80665;      // m/s²
        public static final double 对流层温度直减率 = 0.0065;       // K/m
        public static final double 地下温度直增率 = 0.01; // K/m //Minecraft中地底比较浅，不宜太高直减率
        public static final double 干绝热温度直减率 = 0.0098;
        public static final double 气体常数  = 8.314462618;  // FE/(mol·K)
        public static final double 干空气比热容 = 287; // FE/(kg·K)
        public static final double 干空气摩尔质量 = 0.0289644;    // kg/mol
        public static final double 湿空气比热容 = 461.5;
        public static final double 水摩尔质量 = 0.01801528;
        public static final double 水汽长波吸收系数 = 0.1;
        public static final double 液态水长波吸收系数 = 0.2;
        public static final double 温室气体浓度 = 400.0/1000000;
        public static final double 温室气体吸收系数 = 0.0004;

    }
}
