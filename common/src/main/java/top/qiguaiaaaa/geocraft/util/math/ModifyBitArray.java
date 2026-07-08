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

package top.qiguaiaaaa.geocraft.util.math;

import net.minecraft.util.BitArray;
import org.apache.commons.lang3.Validate;

/**
 * 该类基于 {@link BitArray}实现，
 * 用于发往客户端时修改Chunk数据
 */
public class ModifyBitArray {
    protected final long[] longArray;
    protected final int bitsPerEntry;
    protected final long maxEntryValue;
    protected final int arraySize;

    public ModifyBitArray(int bitsPerEntry, int arraySizeIn, long[] longArray) {
        Validate.inclusiveBetween(1, 32, bitsPerEntry);
        this.arraySize = arraySizeIn;
        this.bitsPerEntry = bitsPerEntry;
        this.maxEntryValue = (1L << bitsPerEntry) - 1L;
        this.longArray = longArray;
    }

    public void set(int index, int value) {
        Validate.inclusiveBetween(0, this.arraySize - 1, index);
        Validate.inclusiveBetween(0, this.maxEntryValue, value);
        int beginBitPos = index * this.bitsPerEntry;
        int beginLongIndex = beginBitPos >> 6;
        int endLongIndex = (beginBitPos + this.bitsPerEntry - 1) >> 6;
        int offset = beginBitPos & 63;
        this.longArray[beginLongIndex] = (this.longArray[beginLongIndex] & ~(this.maxEntryValue << offset)) | ((long)value & this.maxEntryValue) << offset;

        if (beginLongIndex != endLongIndex) {
            int bitsInBegin = Long.SIZE - offset;
            int offsetInEnd = this.bitsPerEntry - bitsInBegin;
            this.longArray[endLongIndex] = ((this.longArray[endLongIndex] >>> offsetInEnd) << offsetInEnd) | ((long)value & this.maxEntryValue) >> bitsInBegin;
        }
    }

    public int get(int index) {
        Validate.inclusiveBetween(0, this.arraySize - 1, index);
        int beginBitPos = index * this.bitsPerEntry;
        int beginLongIndex = beginBitPos >> 6;
        int endLongIndex = (beginBitPos + this.bitsPerEntry - 1) >> 6;
        int offset = beginBitPos & 63;

        if (beginLongIndex == endLongIndex) {
            return (int)(this.longArray[beginLongIndex] >>> offset & this.maxEntryValue);
        }
        int offsetInEnd = 64 - offset;
        return (int)((this.longArray[beginLongIndex] >>> offset | this.longArray[endLongIndex] << offsetInEnd) & this.maxEntryValue);
    }

    public long[] getLongArray() {
        return this.longArray;
    }
}
