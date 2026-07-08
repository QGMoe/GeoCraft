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

package top.qiguaiaaaa.geocraft.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

public final class WaterUtil {
    /**
     * 获取水蒸发的概率
     * @param accessor 大气访问器
     * @return 一个介于0~1的值，表示概率
     */
    public static double getWaterEvaporatePossibility(IAtmosphereAccessor accessor) {
        final int 单层水质量 = FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
        double temp = accessor.getTemperature(true);
        if(temp>= TemperatureProperty.BOILED_POINT) return 1;
        double 期望质量 = getWaterEvaporateAmount(accessor);
        return  1.0 - Math.exp(-期望质量 / 单层水质量);
    }

    /**
     * 获取水蒸发量
     * @param accessor 大气访问器
     * @return 水蒸发量,单位kg
     */
    public static double getWaterEvaporateAmount(IAtmosphereAccessor accessor){
        final double 交换系数 = accessor.getAtmosphereInfo().getVaporExchangeRate();
        double 水汽压 = accessor.getWaterPressure();
        double 饱和水汽压 = 计算饱和水汽压(accessor.getTemperature(true));
        double 水汽压差 = Math.max(饱和水汽压 - 水汽压, 0);
        return 交换系数 * 水汽压差 * 3409.2d;
    }

    /**
     * 获取降雨概率
     * @param accessor 大气访问器
     * @return 一个介于0~1的值，表示概率
     */
    public static double getRainPossibility(IAtmosphereAccessor accessor) {
        if(accessor.getAtmosphereHere() == null) return 0;
        double temp = accessor.getTemperature(false);
        if(temp <= TemperatureProperty.UNAVAILABLE) return 0;
        if(temp>= TemperatureProperty.BOILED_POINT) return 0;
        if(temp<= TemperatureProperty.ICE_POINT-100) return 1;
        double strong = accessor.getAtmosphereHere().getCloudExponent(accessor.getPos());
        return strong/(strong+accessor.getAtmosphereInfo().getRainSmoothingConstant());
    }

    /**
     * 获取冻结概率
     * @param accessor 大气访问器
     * @return 一个介于0~1的值，表示概率
     */
    public static double getFreezePossibility(IAtmosphereAccessor accessor) {
        double temp = accessor.getTemperature();
        if(temp>= TemperatureProperty.ICE_POINT) return 0;
        if(temp< TemperatureProperty.ICE_POINT-20) return 1;
        double diff = TemperatureProperty.ICE_POINT-temp;
        return (diff/20)*0.94f+0.06f;
    }

    /**
     * 是否能够在指定位置放置雪，注意不是降雪，该方法只会检查亮度条件
     * @param world 世界
     * @param pos 位置
     * @return 如果能，则返回true
     */
    public static boolean canPlaceSnow(World world, BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            return Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos);
        }
        return false;
    }

    /**
     * 是否能够在指定位置降雪
     * @param world 世界
     * @param pos 位置
     * @param checkLight 是否检查亮度
     * @return 如果能，则返回true
     */
    public static boolean canSnowAt(World world,BlockPos pos, boolean checkLight) {
        Atmosphere atmosphere = AtmosphereSystemManager.getAtmosphere(world, pos);
        if(atmosphere == null) return world.canSnowAtBody(pos,checkLight);
        if(atmosphere.drainWater(FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,pos,false)< FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME) return false;
        if (atmosphere.getAtmosphereTemperature(pos) >= TemperatureProperty.ICE_POINT) {
            return false;
        } else if (!checkLight) {
            return true;
        } else {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState state = world.getBlockState(pos);

                return state.getBlock().isAir(state, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos);
            }

            return false;
        }
    }

    /**
     * 使用Tetens公式计算指定温度下的饱和水汽压
     * @param temperature 温度，单位K
     * @return 饱和水汽压，单位为帕 Pa
     */
    public static double 计算饱和水汽压(double temperature){
        return 610.78 * Math.exp((17.27 * TemperatureProperty.toCelsiusFromKelvin(temperature)) / (TemperatureProperty.toCelsiusFromKelvin(temperature) + 237.3));
    }
}
