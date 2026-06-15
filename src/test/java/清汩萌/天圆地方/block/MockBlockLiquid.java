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

package 清汩萌.天圆地方.block;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.Consumer;

/**
 * @author QiguaiAAAA
 */
public final class MockBlockLiquid {

    @Nonnull
    public static Pair<MockBlockDynamicLiquid,MockBlockStaticLiquid> create(final @Nonnull Consumer<MockBlockInfoBuilder<?>> builderConsumer){
        final MockBlockInfoBuilder<?> builder = new MockBlockInfoBuilder.Impl();
        builderConsumer.accept(builder);
        final MockBlockDynamicLiquid dynamic = new MockBlockDynamicLiquid(builder);
        final MockBlockStaticLiquid _static = new MockBlockStaticLiquid(builder);
        return Pair.of(dynamic,_static);
    }

    /**
     * @author QiguaiAAAA
     */
    public static class MockBlockDynamicLiquid extends BlockDynamicLiquid {
        protected MockBlockDynamicLiquid(final @Nonnull MockBlockInfoBuilder<?> builder) {
            super(builder.material);
            this.setRegistryName(new ResourceLocation(builder.id.getNamespace(),"flowing_"+builder.id.getPath()));
        }

        @Override
        public void updateTick(@Nonnull final World worldIn,@Nonnull final BlockPos pos,@Nonnull final IBlockState state,@Nonnull final Random rand) {
            // do nothing
        }
    }

    /**
     * @author QiguaiAAAA
     */
    public static class MockBlockStaticLiquid extends BlockStaticLiquid{
        protected MockBlockStaticLiquid(final @Nonnull MockBlockInfoBuilder<?> builder){
            super(builder.material);
            this.setRegistryName(builder.id);
        }

        @Override
        public void updateTick(@Nonnull final World worldIn,@Nonnull final BlockPos pos,@Nonnull final IBlockState state,@Nonnull final Random rand) {
            // do nothing
        }
    }
}
