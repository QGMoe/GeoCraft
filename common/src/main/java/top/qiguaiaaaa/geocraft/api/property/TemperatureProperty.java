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

package top.qiguaiaaaa.geocraft.api.property;

import top.qiguaiaaaa.geocraft.api.state.TemperatureState;

import javax.annotation.Nonnull;

/**
 * 温度属性
 * @author QiguaiAAAA
 */
public abstract class TemperatureProperty extends GeographyProperty implements INumberProperty<Float>{
    public static final int BOILED_POINT = 373;
    public static final int ICE_POINT = 273;
    public static final int STANDARD_TEMP = 298;
    public static final int MIN = 3;
    public static final int UNAVAILABLE = -100;

    @Nonnull
    @Override
    public Class<Float> getNumberType() {
        return Float.TYPE;
    }

    @Nonnull
    @Override
    public abstract TemperatureState getStateInstance() ;

    /**
     * 将指定温度（单位开尔文）转换为摄氏温度
     * @param temperature 单位为开尔文的温度
     * @return 摄氏温度
     */
    public static double toCelsiusFromKelvin(double temperature){
        return temperature-TemperatureProperty.ICE_POINT;
    }
}
