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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import top.qiguaiaaaa.geocraft.api.property.FluidProperty;
import top.qiguaiaaaa.geocraft.api.property.IFluidProperty;
import top.qiguaiaaaa.geocraft.api.state.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气层级
 */
public interface AtmosphereLayer extends Layer {

    /**
     * 向该层大气添加气态形式的水
     * @param pos 添加位置
     * @param amount 水量，单位mB,可为负数
     * @return 成功添加的量
     */
    int addSteam(@Nullable BlockPos pos,int amount,final boolean doAdd);

    /**
     * 向该层大气添加液态形式的水
     * @param pos 添加位置
     * @param amount 水量，单位mB,可为负数
     * @return 成功添加的量
     */
    int addWater(@Nullable BlockPos pos,int amount,final boolean doAdd);

    /**
     * 获得某地的气压
     * @param pos 某地
     * @return 大气压，单位帕 Pa
     */
    double getPressure(@Nonnull BlockPos pos);

    /**
     * 获得某位置的大气水汽压
     * @return 大气水汽压，单位帕 Pa
     */
    double getWaterPressure(@Nonnull BlockPos pos);

    /**
     * 获取整体水汽压
     * @return 大气水汽压,单位帕 Pa
     */
    @Deprecated
    double getWaterPressure();

    /**
     * 获取大气温度
     * @param pos 某处
     * @return 大气温度
     */
    @Override
    default float getTemperature(@Nonnull BlockPos pos){
        return getTemperature(pos,false);
    }

    /**
     * 获得某处的温度
     * @param pos 位置
     * @param notAir 如果需要获取非气体温度，则为true。否则返回气体温度。
     * @return 温度
     */
    float getTemperature(@Nonnull BlockPos pos,boolean notAir);

    /**
     * 获得某地的风
     * @param pos 位置
     * @return 代表风速的三维向量。若没有风或无风则返回零向量。
     */
    @Nonnull
    Vec3d getWind(@Nonnull BlockPos pos);

    /**
     * 获取大气成分状态
     * @param property 大气成分
     * @return 对应的大气状态
     */
    @Nullable
    FluidState getFluid(@Nonnull IFluidProperty property);

    /**
     * 获取该大气层的气态水水量状态
     * @return 气态水水量状态,不一定存在
     */
    @Nullable
    FluidState getSteam();
}
