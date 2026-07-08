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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import net.minecraftforge.fluids.Fluid;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public final class QBUtil {
    public static final long BUCKET_VOLUME = 72072000L;
    public static final long MB_VOLUME = BUCKET_VOLUME/Fluid.BUCKET_VOLUME;
    public static final long QUANTA_VOLUME = BUCKET_VOLUME/8L;
    public static final long HALF_QUANTA_VOLUME = BUCKET_VOLUME/16L;
    public static final LongList VOLUMES_1_TO_16;

    static {
        final long[] volumes = new long[17];
        volumes[0] = BUCKET_VOLUME;
        for(int i=1;i<17;i++){
            volumes[i] = volumes[0]/(long) i;
        }
        VOLUMES_1_TO_16 = LongLists.unmodifiable(new LongArrayList(volumes));
    }

    public static int toMB(long QB){
        return (int) (QB/MB_VOLUME);
    }

    public static int toQuanta(long QB){
        return (int) (QB/QUANTA_VOLUME);
    }

    public static double toPreciseQuanta(long QB){
        return QB/(double)QUANTA_VOLUME;
    }

    public static long toQBFromMB(int mB){
        return mB*MB_VOLUME;
    }

    public static long toQBFromQuanta(int quanta){
        return quanta*QUANTA_VOLUME;
    }
}
