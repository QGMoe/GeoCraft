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
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTPathTag implements NBTPathModifiableNode, NBTPathProvidableNode, NBTPathInitableNode {

    final String key;
    final NBTCompoundMatcher valueFilter;

    public NBTPathTag(final @Nonnull String key,final @Nullable NBTCompoundMatcher valueFilter) {
        this.key = key;
        this.valueFilter = valueFilter;
    }

    @Nonnull
    @Override
    public Collection<NBTBase> filter(final @Nonnull NBTBase nbtBase) {
        if(nbtBase instanceof NBTTagCompound){
            final NBTTagCompound compound = (NBTTagCompound) nbtBase;
            if(!compound.hasKey(key)) return Collections.emptyList();
            final @Nonnull NBTBase tag = compound.getTag(key);
            return valueFilter == null || valueFilter.match(tag)? Collections.singletonList(tag): Collections.emptyList();
        }else return Collections.emptyList();
    }

    @Override
    public int set(@Nonnull final NBTBase base, @Nonnull final NBTBase replacement) throws NickelRuntimeException {
        if(base instanceof NBTTagCompound){
            final NBTTagCompound compound = (NBTTagCompound) base;
            if(!compound.hasKey(key)){
                compound.setTag(key,replacement.copy());
                return 1;
            }
            final @Nonnull NBTBase tag = compound.getTag(key);
            if(valueFilter != null && !valueFilter.match(tag)) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_TAG_MISVALUE));
            compound.setTag(key,replacement.copy());
            return 1;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_TAG_MISMATCH));
    }

    @Override
    public int remove(@Nonnull final NBTBase base) throws NickelRuntimeException {
        if(base instanceof NBTTagCompound){
            final NBTTagCompound compound = (NBTTagCompound) base;
            if(!compound.hasKey(key)){
                return 0;
            }
            final @Nonnull NBTBase tag = compound.getTag(key);
            if(valueFilter != null && !valueFilter.match(tag)) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_TAG_MISVALUE));
            compound.removeTag(key);
            return 1;
        }else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_TAG_MISMATCH));
    }

    @Override
    public void init(@Nonnull final NBTBase base, @Nonnull final NBTPathProvidableNode next) {
        if(!(base instanceof NBTTagCompound)) return;
        final NBTTagCompound compound = (NBTTagCompound) base;
        if(compound.hasKey(key)) return;
        compound.setTag(key,next.provide());
    }

    @Nonnull
    @Override
    public NBTBase provide() {
        return valueFilter==null?new NBTTagCompound():valueFilter.toNBT();
    }

    @Nonnull
    @Override
    public String getLocalName() {
        return valueFilter == null?I18nKeys.NBTPath.NODE_TAG:I18nKeys.NBTPath.NODE_TAG_COMPOUND;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public NBTCompoundMatcher getValueFilter() {
        return valueFilter;
    }

    @Override
    @Nonnull
    public String toString() {
        return valueFilter == null?key:key+valueFilter;
    }
}
