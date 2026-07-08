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

package top.qiguaiaaaa.geocraft.api.event.block;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 当静止流体更新的时候发布,目前只有原版流体,即{@link BlockLiquid}<br/>
 * 该事件通过Mixin实现
 * @author QiguaiAAAA
 */
public class StaticLiquidUpdateEvent extends BlockEvent {
    private final Fluid liquid;
    private final boolean randomTick;
    public StaticLiquidUpdateEvent(@Nonnull Fluid liquid,@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,boolean randomTick) {
        super(world, pos, state);
        this.liquid = liquid;
        this.randomTick = randomTick;
    }
    @Nonnull
    public Fluid getLiquid() {
        return liquid;
    }

    public boolean isRandomTick() {
        return randomTick;
    }

    /**
     * 在流体更新之后发布<br/>
     * 若Result的结果为Allow，且设置了新的方块状态，则流体会调用{@link World#setBlockState(BlockPos, IBlockState)}来将自己变成指定的方块
     */
    @HasResult
    public static class After extends StaticLiquidUpdateEvent{
        private IBlockState newState;
        public After(@Nonnull Fluid liquid,@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,boolean randomTick) {
            super(liquid, world, pos, state,randomTick);
        }

        /**
         * 设置流体将会变成的方块状态，设置为null表示不更新
         * @param newState 新方块状态
         */
        public void setNewState(@Nullable IBlockState newState) {
            this.newState = newState;
        }

        /**
         * 获取新的方块状态
         * @return 新的方块状态
         */
        @Nullable
        public IBlockState getNewState() {
            return newState;
        }
    }
}
