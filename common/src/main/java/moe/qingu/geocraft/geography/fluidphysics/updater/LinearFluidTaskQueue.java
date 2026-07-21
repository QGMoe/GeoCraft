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

package moe.qingu.geocraft.geography.fluidphysics.updater;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import moe.qingu.geocraft.api.fluidphysics.updater.task.FluidTaskRegistry;
import moe.qingu.geocraft.api.fluidphysics.updater.task.IFluidTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntConsumer;

/**
 * @author QGMoe
 */
public class LinearFluidTaskQueue extends FluidTaskQueue{
    private static final IntComparator COMPARE_UNSIGNED = new IntComparator() {
        @Override
        public int compare(final int k1,final int k2) {
            return Integer.compareUnsigned(k1,k2);
        }

        @Override
        public int compare(final @Nonnull Integer o1,final @Nonnull Integer o2) {
            return Integer.compareUnsigned(o1,o2);
        }
    };

    protected final IntArrayList list = new IntArrayList();
    protected final ShortOpenHashSet presence = new ShortOpenHashSet();

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void queue(final int cx, final int cy, final int cz, final int taskID) {
        final int task = cy << 24 | taskID << 8 | cx << 4 | cz;
        presence.add((short) (cy <<8 | cx << 4 | cz));
        list.add(task);
    }

    @Nullable
    @Override
    public IFluidTask query(final int cx,final int cy,final int cz) {
        if(!contains(cx, cy, cz)) return null;
        for(int i=0;i<list.size();i++){
            final int task = list.getInt(i);
            if(task >>> 24 == cy && ((task >>> 4) & 0xF) == cx && (task & 0xF) == cz) return FluidTaskRegistry.getTaskByID((task>>>8)&0xFFFF);
        }
        return null;
    }

    @Override
    public boolean contains(int cx, int cy, int cz) {
        return presence.contains((short) (cy <<8 | cx << 4 | cz));
    }

    @Override
    public int forNext(@Nonnull final FluidTaskConsumer consumer) {
        try {
            IntArrays.quickSort(list.elements(),0,list.size(),COMPARE_UNSIGNED);
            for(int i = 0;i< list.size();i++){
                final int task = list.getInt(i);
                final int taskY = task >>> 24;
                final int taskX = (task >>> 4) & 0xF;
                final int taskZ = task & 0xF;
                final int taskID = (task >>> 8) & 0xFFFF;
                consumer.consume(taskX,taskY,taskZ, FluidTaskRegistry.getTaskByID(taskID));
            }
            return list.size();
        }finally {
            list.clear();
            presence.clear();
        }
    }

    @Override
    public void forEach(@Nonnull final IntConsumer consumer) {
        for(int i=0;i<list.size();i++) consumer.accept(list.getInt(i));
    }

    public void clear(){
        list.clear();
        presence.clear();
    }
}
