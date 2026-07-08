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

package top.qiguaiaaaa.geocraft.geography.state;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.UnderlyingLayer;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.geography.property.DefaultTemperature;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;

import javax.annotation.Nonnull;

/**
 * Temperature的默认实现
 * 注意Temperature的初始化依赖大气本身层级，因此这里没有初始化方法
 */
public class DefaultTemperatureState extends TemperatureState {
    protected static final int TEMPERATURE_MULTI = 30;
    protected static final int TEMPERATURE_TRANSFER_OFFSET = 3;
    public DefaultTemperatureState(float temp){
        super(temp);
    }

    @Override
    public boolean isLoaded() {
        return temperature > 0 && !Float.isInfinite(temperature) && temperature < 10000;
    }

    @Nonnull
    @Override
    public DefaultTemperature getProperty() {
        return DefaultTemperature.TEMPERATURE;
    }

    @Nonnull
    @Override
    public String getNBTTagKey() {
        return "temp";
    }

    public static float calculateBaseTemperature(Chunk chunk , UnderlyingLayer underlying){
        Biome mainBiome = ChunkUtil.getMainBiome(chunk);
        float biomeTemp = mainBiome.getTemperature(new BlockPos((chunk.x<<4)+8, underlying.getAltitude().get(),(chunk.z<<4)+8));
        return toRealTemperature(biomeTemp);
    }

    public static float toRealTemperature(float biomeTemperature){
        if(biomeTemperature <= 0.15){
            return TemperatureProperty.ICE_POINT +(biomeTemperature*TEMPERATURE_MULTI)-TEMPERATURE_TRANSFER_OFFSET-10;
        }else{
            return TemperatureProperty.ICE_POINT +(biomeTemperature*TEMPERATURE_MULTI)-TEMPERATURE_TRANSFER_OFFSET;
        }
    }
}
