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

package moe.qingu.nickel.nbt.matcher;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLongArray;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public final class NBTArrayMatcher extends NBTMatcher<NBTBase> {
    private final Class<? extends NBTBase> arrayType;
    private final LongOpenHashSet expectations = new LongOpenHashSet();

    public NBTArrayMatcher(final @Nonnull Class<? extends NBTBase> arrayType) {
        if(arrayType != NBTTagByteArray.class && arrayType != NBTTagIntArray.class && arrayType != NBTTagLongArray.class) throw new IllegalArgumentException();
        this.arrayType = arrayType;
    }

    public void expect(final long num){
        expectations.add(num);
    }

    @Override
    public int hashCode() {
        return expectations.hashCode() ^ arrayType.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof NBTArrayMatcher){
            final NBTArrayMatcher matcher = (NBTArrayMatcher) obj;
            return this.arrayType == matcher.arrayType && this.expectations.equals(matcher.expectations);
        }else return false;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("[");
        if(arrayType == NBTTagByteArray.class) builder.append('B');
        else if(arrayType == NBTTagIntArray.class) builder.append('I');
        else builder.append('L');
        builder.append(';');
        boolean first = true;
        for(final long l:expectations){
            if(first) first = false;
            else builder.append(',');
            builder.append(l);
        }
        return builder.append(']').toString();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Class<NBTBase> getMatchType() {
        return (Class<NBTBase>) arrayType;
    }

    @Nonnull
    @Override
    public NBTBase toNBT() {
        if(arrayType == NBTTagByteArray.class){
            final LongIterator iterator = expectations.iterator();
            final byte[] arr = new byte[expectations.size()];
            int i=0;
            while (iterator.hasNext()) arr[i++] = (byte) iterator.nextLong();
            return new NBTTagByteArray(arr);
        }else if(arrayType == NBTTagIntArray.class){
            final LongIterator iterator = expectations.iterator();
            final int[] arr = new int[expectations.size()];
            int i=0;
            while (iterator.hasNext()) arr[i++] = (int) iterator.nextLong();
            return new NBTTagIntArray(arr);
        }else {
            return new NBTTagLongArray(expectations.toLongArray());
        }
    }

    @Override
    protected boolean _match(@Nonnull final NBTBase nbt) {
        final LongOpenHashSet nbts = new LongOpenHashSet();
        if(nbt instanceof NBTTagByteArray){
            for(final byte b: ((NBTTagByteArray)nbt).getByteArray()) nbts.add(b);
        }else if(nbt instanceof NBTTagIntArray){
            for(final int i: ((NBTTagIntArray)nbt).getIntArray()) nbts.add(i);
        }else {
            NBTUtils.streamOf((NBTTagLongArray) nbt)
                    .forEach(nbts::add);
        }
        if(nbts.size() < expectations.size()) return false;
        final LongIterator iterator = expectations.iterator();
        while (iterator.hasNext()) if(!nbts.contains(iterator.nextLong())) return false;
        return true;
    }
}
