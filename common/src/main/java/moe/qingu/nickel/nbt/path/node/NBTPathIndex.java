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

package moe.qingu.nickel.nbt.path.node;

import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTPathIndex implements NBTPathModifiableNode{
    private final static ThreadLocal<ArrayList<NBTBase>> cache = ThreadLocal.withInitial(ArrayList::new);
    final int index;

    public NBTPathIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Nonnull
    @Override
    public Collection<NBTBase> resolve(final @Nonnull NBTBase nbtBase) {
        if(nbtBase instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) nbtBase;
            final int i = index<0?list.tagCount()+index:index;
            if(i<0) return Collections.emptyList();
            return list.tagCount() > i ?Collections.singletonList(list.get(i)): Collections.emptyList();
        }else if(nbtBase instanceof NBTTagByteArray){
            final byte[] array = ((NBTTagByteArray) nbtBase).getByteArray();
            final int i = index<0?array.length+index:index;
            if(i<0) return Collections.emptyList();
            return array.length> i ?Collections.singletonList(new NBTTagByte(array[i])):Collections.emptyList();
        }else if(nbtBase instanceof NBTTagIntArray){
            final int[] array = ((NBTTagIntArray) nbtBase).getIntArray();
            final int i = index<0?array.length+index:index;
            if(i<0) return Collections.emptyList();
            return array.length> i ?Collections.singletonList(new NBTTagInt(array[i])):Collections.emptyList();
        }else if(nbtBase instanceof NBTTagLongArray){
            final long[] array = NBTUtils.streamOf((NBTTagLongArray) nbtBase).toArray();
            final int i = index<0?array.length+index:index;
            if(i<0) return Collections.emptyList();
            return array.length> i ?Collections.singletonList(new NBTTagLong(array[i])):Collections.emptyList();
        }else return Collections.emptyList();
    }

    @Override
    public int set(@Nonnull final NBTBase base, @Nonnull final NBTBase replacement) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            final int i = index<0?list.tagCount()+index:index;
            if(i<0 || i >= list.tagCount()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_LIST_OUT,i));
            if(list.getTagType() != 0 && replacement.getId() != list.getTagType())
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_NO_TYPE)
                        .arg(NBTBase.NBT_TYPES[replacement.getId()],NBTBase.NBT_TYPES[list.getTagType()]));
            list.set(i,replacement.copy());
            return 1;
        }else if(base instanceof NBTTagByteArray){
            final byte[] array = ((NBTTagByteArray) base).getByteArray();
            final int i = index<0?array.length+index:index;
            if(i<0 || i >= array.length) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_ARR_OUT,i));
            if(replacement.getId() != Constants.NBT.TAG_BYTE) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_NO_BYTE));
            array[i] = ((NBTTagByte)replacement).getByte();
            return 1;
        }else if(base instanceof NBTTagIntArray){
            final int[] array = ((NBTTagIntArray) base).getIntArray();
            final int i = index<0?array.length+index:index;
            if(i<0 || i >= array.length) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_ARR_OUT,i));
            if(replacement.getId() != 0 && replacement.getId() > Constants.NBT.TAG_INT)
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_NO_INT));
            array[i] = ((NBTPrimitive)replacement).getInt();
            return 1;
        }else if(base instanceof NBTTagLongArray){
            final long[] array = NBTUtils.getLongArray((NBTTagLongArray) base);
            final int i = index<0?array.length+index:index;
            if(i<0 || i >= array.length) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_ARR_OUT,i));
            if(replacement.getId() != 0 && replacement.getId() > Constants.NBT.TAG_LONG)
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_NO_LONG));
            array[i] = ((NBTPrimitive)replacement).getLong();
            return 1;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_MISMATCH));
    }

    public int insert(@Nonnull final NBTBase base, @Nonnull final NBTBase tag) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            final int i = index<0?list.tagCount()+index:index;
            if(i<0 || i > list.tagCount()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_INDEX_OUT,i));
            if(list.getTagType() != 0 && tag.getId() != list.getTagType())
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_INDEX_NO_TYPE)
                        .arg(NBTBase.NBT_TYPES[tag.getId()],NBTBase.NBT_TYPES[list.getTagType()]));
            final ArrayList<NBTBase> ca = cache.get();
            ca.clear();
            while (list.tagCount()>i) ca.add(list.removeTag(list.tagCount()-1));
            list.appendTag(tag.copy());
            for(int j=ca.size()-1;j>=0;j--) list.appendTag(ca.get(j));
            return 1;
        } else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_INDEX_MISMATCH));
    }

    @Override
    public int remove(@Nonnull final NBTBase base) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            final int i = index<0?list.tagCount()+index:index;
            if(i<0 || i >= list.tagCount()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_INDEX_LIST_OUT,i));
            list.removeTag(i);
            return 1;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_INDEX_MISMATCH));
    }

    @Nonnull
    @Override
    public String getLocalName() {
        return I18nKeys.NBTPath.NODE_INDEX;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(index);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NBTPathIndex && this.index == ((NBTPathIndex) obj).index;
    }

    @Override
    @Nonnull
    public String toString() {
        return "["+index+"]";
    }
}
