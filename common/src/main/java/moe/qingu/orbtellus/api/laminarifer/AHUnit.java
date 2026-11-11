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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

/**
 * 层对齐高度单位（Layer-Aligned Height Unit）
 * @since API-0.3.5
 * @author QGMoe
 */
public final class AHUnit {
    public static final long ONE_BLOCK          = 5_3150_2171_2000L;
    public static final long HALF_BLOCK         = ONE_BLOCK  /  2;
    public static final long THIRD_BLOCK        = ONE_BLOCK  /  3;
    public static final long FOURTH_BLOCK       = ONE_BLOCK  /  4;
    public static final long FIFTH_BLOCK        = ONE_BLOCK  /  5;
    public static final long SIXTH_BLOCK        = ONE_BLOCK  /  6;
    public static final long SEVENTH_BLOCK      = ONE_BLOCK  /  7;
    public static final long EIGHTH_BLOCK       = ONE_BLOCK  /  8;
    public static final long NINTH_BLOCK        = ONE_BLOCK  /  9;
    public static final long TENTH_BLOCK        = ONE_BLOCK  / 10;
    public static final long ELEVENTH_BLOCK     = ONE_BLOCK  / 11;
    public static final long TWELFTH_BLOCK      = ONE_BLOCK  / 12;
    public static final long THIRTEENTH_BLOCK   = ONE_BLOCK  / 13;
    public static final long FOURTEENTH_BLOCK   = ONE_BLOCK  / 14;
    public static final long FIFTEENTH_BLOCK    = ONE_BLOCK  / 15;
    public static final long SIXTEENTH_BLOCK    = ONE_BLOCK  / 16;
    public static final long SEVENTEENTH_BLOCK  = ONE_BLOCK  / 17;

    public static final long DEFAULT_FULL_FLUID = 4_7244_6374_4000L;
    public static final long HALF_FLUID         = DEFAULT_FULL_FLUID /  2;
    public static final long THIRD_FLUID        = DEFAULT_FULL_FLUID /  3;
    public static final long FOURTH_FLUID       = DEFAULT_FULL_FLUID /  4;
    public static final long FIFTH_FLUID        = DEFAULT_FULL_FLUID /  5;
    public static final long SIXTH_FLUID        = DEFAULT_FULL_FLUID /  6;
    public static final long SEVENTH_FLUID      = DEFAULT_FULL_FLUID /  7;
    public static final long EIGHTH_FLUID       = DEFAULT_FULL_FLUID /  8;
    public static final long NINTH_FLUID        = DEFAULT_FULL_FLUID /  9;
    public static final long TENTH_FLUID        = DEFAULT_FULL_FLUID / 10;
    public static final long ELEVENTH_FLUID     = DEFAULT_FULL_FLUID / 11;
    public static final long TWELFTH_FLUID      = DEFAULT_FULL_FLUID / 12;
    public static final long THIRTEENTH_FLUID   = DEFAULT_FULL_FLUID / 13;
    public static final long FOURTEENTH_FLUID   = DEFAULT_FULL_FLUID / 14;
    public static final long FIFTEENTH_FLUID    = DEFAULT_FULL_FLUID / 15;
    public static final long SIXTEENTH_FLUID    = DEFAULT_FULL_FLUID / 16;

    public static final LongList BLOCK_HEIGHTS;
    public static final LongList FLUID_HEIGHTS;

    static {
        final long[] block_heights = new long[18];
        block_heights[0] = block_heights[1] = ONE_BLOCK;
        for(int i=2;i<=17;i++) block_heights[i] = ONE_BLOCK/i;
        BLOCK_HEIGHTS = LongLists.unmodifiable(new LongArrayList(block_heights));
        final long[] fluid_heights = new long[17];
        fluid_heights[0] = fluid_heights[1] = DEFAULT_FULL_FLUID;
        for(int i=2;i<=16;i++) fluid_heights[i] = DEFAULT_FULL_FLUID /i;
        FLUID_HEIGHTS = LongLists.unmodifiable(new LongArrayList(fluid_heights));
    }

    private AHUnit(){}
}
