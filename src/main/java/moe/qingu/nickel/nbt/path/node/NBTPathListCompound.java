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
import moe.qingu.nickel.nbt.matcher.NBTCompoundMatcher;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTPathListCompound implements NBTPathModifiableNode, NBTPathProvidableNode {
    final NBTCompoundMatcher filter;

    public NBTPathListCompound(final @Nonnull NBTCompoundMatcher filter) {
        this.filter = filter;
    }

    @Nonnull
    public NBTCompoundMatcher getFilter() {
        return filter;
    }

    @Nonnull
    @Override
    public Collection<NBTBase> filter(final @Nonnull NBTBase nbtBase) {
        if(nbtBase instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) nbtBase;
            return StreamSupport.stream(list.spliterator(),false)
                    .filter(filter::match)
                    .collect(Collectors.toList());
        }else return Collections.emptyList();
    }

    @Override
    public int set(@Nonnull final NBTBase base, @Nonnull final NBTBase replacement) throws NickelRuntimeException {
        if(replacement.getId() != Constants.NBT.TAG_COMPOUND) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_LIST_COM_NO_COMPOUND));
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            if(list.getTagType() != 0 && list.getTagType() != Constants.NBT.TAG_COMPOUND)
                throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_LIST_COM_NO_LIST_COM));
            int cot = 0;
            for(int i=0;i<list.tagCount();i++)
                if(filter.match(list.get(i))){
                    list.set(i,replacement.copy());
                    cot++;
                }
            return cot;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_LIST_COM_MISMATCH));
    }

    @Override
    public int remove(@Nonnull final NBTBase base) throws NickelRuntimeException {
        if(base instanceof NBTTagList){
            final NBTTagList list = (NBTTagList) base;
            if(list.getTagType() != 0 && list.getTagType() != Constants.NBT.TAG_COMPOUND) return 0;
            int cot = 0;
            for(int i=0;i<list.tagCount();)
                if(filter.match(list.get(i))){
                    list.removeTag(i);
                    cot++;
                }
                else i++;
            return cot;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_LIST_COM_MISMATCH));
    }

    @Nonnull
    @Override
    public NBTBase provide() {
        final NBTTagList list = new NBTTagList();
        list.appendTag(this.filter.toNBT());
        return list;
    }

    @Nonnull
    @Override
    public String getLocalName() {
        return I18nKeys.NBTPath.NODE_LIST_COMPOUND;
    }

    @Override
    @Nonnull
    public String toString() {
        return "["+filter+"]";
    }
}
