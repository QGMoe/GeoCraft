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

package top.qiguaiaaaa.geocraft.geography.fluidphysics;

import net.minecraft.util.math.BlockPos;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.vec.RelativeBlockPosI;
import top.qiguaiaaaa.geocraft.api.util.math.vec.RelativeBlockPosS;

/**
 * @author QiguaiAAAA
 */
public final class ThreadLocalHelper {
    @MultiThread({ThreadType.MINECRAFT_SERVER,ThreadType.FLUID_PRESSURE_TASKS})
    public static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS_FOR_RES =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    public static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS_FOR_QUEUE =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    public static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS_FOR_REALITY_BFS =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    public static final ThreadLocal<RelativeBlockPosI.Mutable> MUTABLE_POS_I_FOR_REALITY_BFS_RES =
            ThreadLocal.withInitial(RelativeBlockPosI.Mutable::new);

    @MultiThread(ThreadType.FLUID_PRESSURE_TASKS)
    public static final ThreadLocal<RelativeBlockPosS.Mutable> MUTABLE_POS_S_FOR_REALITY_BFS_RES =
            ThreadLocal.withInitial(RelativeBlockPosS.Mutable::new);

    public static void clear(){
        MUTABLE_BLOCK_POS_FOR_RES.remove();
        MUTABLE_BLOCK_POS_FOR_QUEUE.remove();
        MUTABLE_BLOCK_POS_FOR_REALITY_BFS.remove();
        MUTABLE_POS_I_FOR_REALITY_BFS_RES.remove();
        MUTABLE_POS_S_FOR_REALITY_BFS_RES.remove();
    }
}
