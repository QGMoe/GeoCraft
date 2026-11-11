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

package moe.qingu.orbtellus.api.util;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public final class APIMathUtil {
    public static long clamp(final long num,final long min,final long max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static long nextLong(final @Nonnull Random rand, final long bound){
        final long mask = bound - 1L;
        long res = rand.nextLong();
        if ((bound & mask) == 0L){
            res &= mask;
        } else for (long sample = res >>> 1; // sample 是样本空间中的一个点
                    sample + mask - (res = sample % bound) < 0L;
                    sample = rand.nextLong() >>> 1); // sample 无法作为样本，重新采样
        return res;
    }

    /**
     * 标准正态分布的右尾概率 Q(x) = P(Z > x)，Z ~ N(0,1)。
     * 使用 Abramowitz-Stegun 的算法近似。
     * @since API-0.3.6
     */
    public static double normalTailProbability(final double x){
        if(x < 0) return 1d - normalTailProbability(-x);
        final double t = 1d / (1d + 0.2316419d * x);
        final double poly = t * (0.319381530d
                + t * (-0.356563782d
                + t * (1.781477937d
                + t * (-1.821255978d
                + t * 1.330274429d))));
        return Math.exp(-x * x / 2d) / Math.sqrt(2d * Math.PI) * poly;
    }
}
