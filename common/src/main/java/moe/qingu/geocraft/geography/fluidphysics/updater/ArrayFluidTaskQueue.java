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

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

/**
 * @author QGMoe
 */
@SuppressWarnings("OctalInteger")
public class ArrayFluidTaskQueue extends FluidTaskQueue{
    protected final IntArrayList[] layers = new IntArrayList[256];
    protected final long[] bitmap = new long[4];
    protected final long[] presence = new long[1024];
    protected int curY = 0;
    protected int count = 0;

    @Override
    public int size() {
        return count;
    }

    @Override
    public void queue(final int cx, final int cy, final int cz, final short taskID) {
        if(layers[cy] == null) layers[cy] = new IntArrayList();
        bitmap[cy >>>6] |= 1L<<(cy & 0_77);
        final int xz = cx <<4 | cz;
        presence[(cy <<2)|(xz>>>6)] |= 1L<<(xz & 0_77);
        layers[cy].add(taskID<< 8 | cx << 4 | cz);
        count ++;
    }

    @Override
    public boolean contains(final int cx, final int cy, final int cz) {
        final int xz = cx <<4 | cz;
        return (presence[(cy <<2)|(xz>>>6)] & 1L<<(xz & 0_77)) != 0L;
    }

    @Override
    public int forNext(@Nonnull final FluidTaskConsumer consumer) {
        curY = getLowestY();
        if(curY > 255 || curY<0) return 0;
        try {
            final IntArrayList tasks = layers[curY];
            if(tasks == null) return 0;
            final int size = tasks.size();
            try {
                for(int i=0;i<tasks.size();i++){
                    final int task = tasks.getInt(i);
                    final int x = (task >>> 4) & 0xF;
                    final int z = task & 0xF;
                    consumer.consume(x,curY,z,FluidTasks.getTask((task>>8)&0xFFFF));
                }
                return size;
            }finally {
                count -= size;
                tasks.clear();
            }
        }finally {
            final int base = curY<<2;
            presence[base] = presence[base+1] = presence[base+2] = presence[base+3] = 0L;
            bitmap[curY >>> 6] &= ~(1L<<(curY & 0_77));
        }
    }

    @Override
    public void forEach(@Nonnull final IntConsumer consumer) {
        for(int i=0;i<4;i++){
            long l = bitmap[i];
            if(l == 0) continue;
            int lowest = 0;
            while (lowest < 64){
                lowest = Long.numberOfTrailingZeros(l);
                l &= ~(1L<<lowest);
                final int y = lowest+ (i<<6);
                final IntArrayList tasks = layers[y];
                if(tasks == null) continue;
                for(int j=0;j<tasks.size();j++) consumer.accept(y<<24 | tasks.getInt(j));
            }
        }
    }

    public int getCurrentY() {
        return curY;
    }

    public int getLowestY(){
        for(int i=0;i<4;i++) if(bitmap[i] != 0) return (i<<6)+Long.numberOfTrailingZeros(bitmap[i]);
        return 256;
    }
}
