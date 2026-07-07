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
import moe.qingu.nickel.nbt.NBTUtils;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author QGMoe
 */
public final class NBTPathIndex extends NBTPathNode{
    private final int index;

    public NBTPathIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Collection<NBTBase> apply(final @Nonnull NBTBase nbtBase) {
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

    @Nonnull
    @Override
    public String getLocalName() {
        return I18nKeys.NBTPath.NODE_INDEX;
    }
}
