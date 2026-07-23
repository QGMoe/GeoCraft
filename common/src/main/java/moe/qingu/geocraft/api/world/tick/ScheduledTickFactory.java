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
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

/**
 * @author QGMoe
 */
@SuppressWarnings("unchecked")
public final class ScheduledTickFactory {
    static final Class<? extends IScheduledTick> PROVIDER;
    static final Constructor CONSTRUCTOR;

    static {
        if(SystemUtils.IS_JAVA_1_8){
            CONSTRUCTOR = BasicScheduledTick::new;
            PROVIDER = BasicScheduledTick.class;
        }else {
            try {
                final Class<?> cls = Class.forName("moe.qingu.geocraft.api.world.scheduler.RecordScheduledTick");
                CONSTRUCTOR = (Constructor) cls.getMethod("getFactory").invoke(null);
                PROVIDER = (Class<? extends IScheduledTick>) cls;
            } catch (final NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ScheduledTickFactory(){}

    @Nonnull
    public static Constructor getConstructor() {
        return CONSTRUCTOR;
    }

    @Nonnull
    public static Class<? extends IScheduledTick> getProvider() {
        return PROVIDER;
    }

    @FunctionalInterface
    public interface Constructor{
        @Nonnull IScheduledTick create(final @Nonnull Block block,
                                       final @Nonnull BlockPos pos,
                                       final long triggeredTick,
                                       final @Nonnull TickPriority priority);
    }
}
