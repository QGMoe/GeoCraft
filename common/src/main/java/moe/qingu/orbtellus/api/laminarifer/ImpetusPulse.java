/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.api.laminarifer;

/**
 * 流强（Impetus），压强和时间的乘积，属于向量，方向为流体流动的趋向。
 * 压强和时间组成的二元组叫做一个流强脉冲（Impetus Pulse）
 * 流强的标准单位是 布里丹（Buridan），缩写 布（Bd）。1 布 = 1 kPa×simh = 1 kPa×kgt。
 * 流强的符号为 Π
 * @author QGMoe
 */
public final class ImpetusPulse {

    private ImpetusPulse(){}

    /**
     * 将压强和时长信息打包成一个流强脉冲
     * @param pressure 压强，单位 kPa
     * @param time 时长，单位 模拟时 simh 或 千游戏刻 kgt
     * @return 打包的 long，高 32 位为压强，低 32 位为时间
     */
    public static long of(final float pressure, final float time){
        return (Integer.toUnsignedLong(Float.floatToRawIntBits(pressure))<<32) | Integer.toUnsignedLong(Float.floatToRawIntBits(time));
    }

    public static float pressure(final long pulse){
        return Float.intBitsToFloat((int)(pulse>>>32));
    }

    public static float time(final long pulse){
        return Float.intBitsToFloat((int)pulse);
    }

    public static float toImpetus(final long pulse){
        return pressure(pulse) * time(pulse);
    }
}
