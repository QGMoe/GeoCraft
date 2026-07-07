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

import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public abstract class NBTMatcher<T extends NBTBase> {

    @Nonnull
    public abstract Class<T> getMatchType();

    public final boolean match(final @Nonnull NBTBase nbt){
        if(nbt.getClass() != getMatchType()) return false;
        else return _match(getMatchType().cast(nbt));
    }

    protected abstract boolean _match(final @Nonnull T t);

    @Nonnull
    public static NBTCompoundMatcher toMatcher(final @Nonnull NBTTagCompound compound){
        final NBTCompoundMatcher matcher = new NBTCompoundMatcher();
        for(final String k:compound.getKeySet()) matcher.expect(k,toMatcher(compound.getTag(k)));
        return matcher;
    }

    @Nonnull
    public static NBTListMatcher toMatcher(final @Nonnull NBTTagList list){
        final NBTListMatcher matcher = new NBTListMatcher();
        for(final NBTBase nbt:list) matcher.expect(toMatcher(nbt));
        return matcher;
    }

    @Nonnull
    public static NBTMatcher<?> toMatcher(final @Nonnull NBTBase nbt){
        if(nbt instanceof NBTTagCompound) return toMatcher((NBTTagCompound) nbt);
        else if(nbt instanceof NBTTagByteArray){
            final NBTTagByteArray arr = (NBTTagByteArray) nbt;
            final NBTArrayMatcher matcher = new NBTArrayMatcher(arr.getClass());
            for(final byte b:arr.getByteArray()) matcher.expect(b);
            return matcher;
        }else if(nbt instanceof NBTTagIntArray){
            final NBTTagIntArray arr = (NBTTagIntArray) nbt;
            final NBTArrayMatcher matcher = new NBTArrayMatcher(arr.getClass());
            for(final int b:arr.getIntArray()) matcher.expect(b);
            return matcher;
        }else if(nbt instanceof NBTTagLongArray){
            final NBTTagLongArray arr = (NBTTagLongArray) nbt;
            final NBTArrayMatcher matcher = new NBTArrayMatcher(arr.getClass());
            NBTUtils.streamOf(arr).forEach(matcher::expect);
            return matcher;
        }else if(nbt instanceof NBTTagList) return toMatcher((NBTTagList) nbt);
        else if(nbt instanceof NBTTagByte) return new NBTByteMatcher(((NBTTagByte) nbt).getByte());
        else if(nbt instanceof NBTTagShort) return new NBTShortMatcher(((NBTTagShort) nbt).getShort());
        else if(nbt instanceof NBTTagInt) return new NBTIntMatcher(((NBTTagInt) nbt).getInt());
        else if(nbt instanceof NBTTagLong) return new NBTLongMatcher(((NBTTagLong) nbt).getLong());
        else if(nbt instanceof NBTTagString) return new NBTStringMatcher(((NBTTagString) nbt).getString());
        else if(nbt instanceof NBTTagFloat) return new NBTFloatMatcher(((NBTTagFloat)nbt).getFloat());
        else if(nbt instanceof NBTTagDouble) return new NBTDoubleMatcher(((NBTTagDouble)nbt).getDouble());
        else throw new IllegalArgumentException();
    }
}
