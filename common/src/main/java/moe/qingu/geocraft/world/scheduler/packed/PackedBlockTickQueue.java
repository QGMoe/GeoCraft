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

package moe.qingu.geocraft.world.scheduler.packed;

import moe.qingu.geocraft.api.world.tick.IScheduledTick;
import moe.qingu.geocraft.api.world.tick.TickPriority;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.LongConsumer;

/**
 * @author QGMoe
 */
@SuppressWarnings("OctalInteger")
public abstract class PackedBlockTickQueue {
    public long baseTime;

    public boolean isEmpty(){
        return size() == 0;
    }

    public abstract int size();

    public abstract void queue(final int cx,final int cy,final int cz,final int blockID,final long delay,final int priority);

    public void queue(final long tick){
        queue((int) ((tick>>>12)&0xFL), (int) ((tick>>>20)&0xFFL), (int) ((tick>>>16)&0xFL), (int) (tick&0_7777L),tick>>>32, (int) ((tick>>>28)&0xFL));
    }

    public abstract boolean contains(final int cx,final int cy,final int cz,final int blockID);

    public abstract int forNext(final long worldTotalTime, final @Nonnull PackedBlockTickConsumer consumer, final @Nonnull long[] temp);

    public abstract void forEach(final @Nonnull LongConsumer consumer);

    public abstract void updateBaseTime(final long newBaseTime);

    public final @Nonnull IScheduledTick toScheduledTick(final long t){
        final long x = (t >>> 12) & 0xFL;
        final long y = (t >>> 20) & 0xFFL;
        final long z = (t >>> 16) & 0xFL;
        final long scheduledTime = this.baseTime + (t >>> 32);
        final @Nonnull Block block = Block.getBlockById((int)(t &0_7777L));
        final @Nonnull TickPriority priority = TickPriority.of((int)((t >>> 28)&0xFL));
        return IScheduledTick.of(block,new BlockPos((int) x,(int) y,(int) z),scheduledTime,priority);
    }
}
