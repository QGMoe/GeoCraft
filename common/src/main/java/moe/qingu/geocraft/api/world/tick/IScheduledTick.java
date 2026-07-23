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

package moe.qingu.geocraft.api.world.tick;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

import static moe.qingu.geocraft.api.world.tick.ScheduledTickFactory.CONSTRUCTOR;

/**
 * @author QGMoe
 */
public interface IScheduledTick extends Comparable<IScheduledTick> {
    @Nonnull BlockPos pos();

    @Nonnull Block block();

    long triggeredTick();

    @Nonnull
    TickPriority priority();

    @Override
    default int compareTo(final @Nonnull IScheduledTick o){
        if (this.triggeredTick() != o.triggeredTick()) return Long.compare(this.triggeredTick(),o.triggeredTick());
        else if (this.priority() != o.priority()) return Integer.compare(this.priority().ordinal(),o.priority().ordinal());
        final BlockPos a = this.pos();
        final BlockPos b = o.pos();
        if(a.getY() != b.getY()) return Integer.compare(a.getY(),b.getY());
        else if(a.getZ() != b.getZ()) return Integer.compare(a.getZ(),b.getZ());
        else if(a.getX() != b.getX()) return Integer.compare(a.getX(),b.getX());
        else if(this.block() != o.block()) return Integer.compare(Block.getIdFromBlock(this.block()),Block.getIdFromBlock(o.block()));
        else return 0;
    }

    static boolean equals(final @Nonnull IScheduledTick a,final @Nonnull Object obj){
        if(obj instanceof IScheduledTick){
            final IScheduledTick b = (IScheduledTick) obj;
            return a.block() == b.block() && a.pos().equals(b.pos());
        }return false;
    }


    @Nonnull
    static IScheduledTick of(final @Nonnull Block block,final @Nonnull BlockPos pos,final long triggeredTick){
        return CONSTRUCTOR.create(block,pos,triggeredTick,TickPriority.DEFAULT);
    }

    @Nonnull
    static IScheduledTick of(final @Nonnull Block block,final @Nonnull BlockPos pos,final long triggeredTick,final @Nonnull TickPriority priority){
        return CONSTRUCTOR.create(block,pos,triggeredTick,priority);
    }

}
