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
/*
 * This file contains code derived from fastutil (https://fastutil.di.unimi.it/)
 * Original class: it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue (partial)
 * Copyright (C) 2003-2024 Paolo Boldi and Sebastiano Vigna
 * Licensed under the Apache License, Version 2.0
 *
 * Modifications: Extracted heap operations and merged into this class
 * to enable direct array access for iteration and serialization.
 *
 * 本文件包含源自 fastutil (https://fastutil.di.unimi.it/) 的代码
 * 原始类：it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue（部分）
 * 版权所有 (C) 2003-2024 Paolo Boldi 与 Sebastiano Vigna
 * 根据 Apache 许可证第 2.0 版许可
 *
 * 修改内容：提取了堆操作并整合至该类以支持直接数组访问来实现遍历与序列化。
 */

package moe.qingu.geocraft.world.scheduler;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongHeaps;
import net.minecraft.block.Block;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

/**
 * @author QGMoe
 */
@SuppressWarnings("OctalInteger")
public final class HeapBlockTickQueue extends BlockTickQueue{
    private static final LongComparator COMPARE_UNSIGNED_LOW_FIRST = new LongComparator() {
        @Override
        public int compare(final long k1,final long k2) {
            return Long.compareUnsigned(k1, k2);
        }

        @Override
        public int compare(final @Nonnull Long o1,final @Nonnull Long o2) {
            return Long.compareUnsigned(o1, o2);
        }
    };

    private final IntOpenHashSet set = new IntOpenHashSet();
    private long[] heap = LongArrays.EMPTY_ARRAY;
    private int size;

    @Override
    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    @Override
    public void queue(final int cx,final int cy,final int cz,final int blockID,final long delay,final int priority) {
        if (size == heap.length) heap = LongArrays.grow(heap, size + 1);
        heap[size++] = ((long) priority <<28) | ((long) cy << 20) | ((long) blockID << 8) | ((long) cx << 4) | cz;
        LongHeaps.upHeap(heap, size, size - 1, COMPARE_UNSIGNED_LOW_FIRST);
        set.add((cy<<20) | (blockID << 8) | (cx << 4) | cz);
    }

    public long dequeue() {
        if (size == 0) throw new NoSuchElementException();
        final long result = heap[0];
        heap[0] = heap[--size];
        if (size != 0) LongHeaps.downHeap(heap, size, 0, COMPARE_UNSIGNED_LOW_FIRST);
        return result;
    }

    public void trim() {
        heap = LongArrays.trim(heap, size);
    }

    @Override
    public boolean contains(final int cx,final int cy,final int cz,final int blockID) {
        return set.contains((cy<<20) | (blockID << 8) | (cx << 4) | cz);
    }

    @Override
    public int forNext(final long worldTotalTime, @Nonnull final BlockTickConsumer consumer,final @Nonnull long[] temp) {
        if(worldTotalTime<baseTime) return 0;
        final long maxDelay = Math.min(worldTotalTime - baseTime, 0xFFFF_FFFFL);
        final long maxValue = (maxDelay<<32) | 0xFFFF_FFFFL;
        int count = 0;
        while (size > 0 && count < temp.length && Long.compareUnsigned(heap[0],maxValue)<=0) temp[count++] = dequeue();
        if(maxDelay > 2147483647L) updateBaseTime(worldTotalTime);
        for(int i=0;i<count;i++){
            final long tick = temp[i];
            final int x = (int) ((tick >>> 4) & 0xF);
            final int y = (int) ((tick >>> 20) & 0xFF);
            final int z = (int) (tick & 0xF);
            final Block block = Block.getBlockById((int) ((tick >>> 8) & 0_7777));
            consumer.consume(x,y,z,block);
        }
        return count;
    }

    @Override
    public void forEach(@Nonnull final LongConsumer consumer) {
        for(int i=0;i<size;i++) consumer.accept(heap[i]);
    }

    @Override
    public void updateBaseTime(final long newBaseTime) {
        for(int i=0;i<size;i++){
            final long task = heap[i];
            final long time = baseTime + (task>>>32);
            final long delay = newBaseTime>time?newBaseTime-time:0L;
            heap[i] = (task & 0xFFFF_FFFFL) | (delay<<32);
        }
        LongHeaps.makeHeap(heap,size,COMPARE_UNSIGNED_LOW_FIRST);
    }
}
