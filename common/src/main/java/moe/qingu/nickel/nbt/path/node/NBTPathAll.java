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

import com.google.common.collect.Lists;
import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTPathAll implements NBTPathModifiableNode, NBTPathProvidableNode, NBTPathInitableNode {
    public static final NBTPathAll ALL = new NBTPathAll();

    private NBTPathAll(){}

    @Nonnull
    @Override
    public String getLocalName() {
        return I18nKeys.NBTPath.NODE_ALL;
    }

    @Override
    @Nonnull
    public String toString() {
        return "[]";
    }

    @Nonnull
    @Override
    public Collection<NBTBase> filter(final @Nonnull NBTBase nbtBase) {
        if(nbtBase instanceof NBTTagList){
            return Lists.newArrayList((NBTTagList) nbtBase);
        }else if(nbtBase instanceof NBTTagByteArray){
            final ArrayList<NBTBase> tags = new ArrayList<>();
            for(final byte b:((NBTTagByteArray) nbtBase).getByteArray()) tags.add(new NBTTagByte(b));
            return tags;
        }else if(nbtBase instanceof NBTTagIntArray){
            final ArrayList<NBTBase> tags = new ArrayList<>();
            for(final int i: ((NBTTagIntArray) nbtBase).getIntArray()) tags.add(new NBTTagInt(i));
            return tags;
        }else if(nbtBase instanceof NBTTagLongArray){
            return NBTUtils.streamOf((NBTTagLongArray) nbtBase).mapToObj(NBTTagLong::new).collect(Collectors.toList());
        }else return Collections.emptyList();
    }

    @Override
    public int set(@Nonnull final NBTBase base, @Nonnull final NBTBase replacement) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            int size = NBTUtils.empty(list);
            while (size-->0) list.appendTag(replacement.copy());
            return list.tagCount();
        }else if(base instanceof NBTTagByteArray){
            if(replacement.getId() != Constants.NBT.TAG_BYTE) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_ALL_NO_BYTE));
            final byte[] arr = ((NBTTagByteArray)base).getByteArray();
            Arrays.fill(arr, ((NBTTagByte)replacement).getByte());
            return arr.length;
        }else if(base instanceof NBTTagIntArray){
            if(replacement.getId() != 0 && replacement.getId() <= Constants.NBT.TAG_INT) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_ALL_NO_INT));
            final int[] arr = ((NBTTagIntArray)base).getIntArray();
            Arrays.fill(arr, ((NBTPrimitive)replacement).getInt());
            return arr.length;
        }else if(base instanceof NBTTagLongArray){
            if(replacement.getId() != 0 && replacement.getId() <= Constants.NBT.TAG_LONG)
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_ALL_NO_LONG));
            final long[] array = NBTUtils.getLongArray((NBTTagLongArray) base);
            Arrays.fill(array, ((NBTPrimitive)replacement).getLong());
            return array.length;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_ALL_MISMATCH));
    }

    @Override
    public int remove(@Nonnull final NBTBase base) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            return NBTUtils.empty((NBTTagList) base);
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_ALL_MISMATCH));
    }

    @Nonnull
    @Override
    public NBTBase provide() {
        return new NBTTagList();
    }

    @Override
    public void init(@Nonnull final NBTBase base, @Nonnull final NBTPathProvidableNode next) {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            if(!list.isEmpty()) return;
            list.appendTag(next.provide());
        }
    }
}
