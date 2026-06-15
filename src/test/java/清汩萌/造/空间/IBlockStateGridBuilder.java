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

package 清汩萌.造.空间;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public final class IBlockStateGridBuilder {
    private final List<IBlockStateLayerBuilder> layers = new ArrayList<>();

    @Nonnull
    public static IBlockStateGridBuilder grid(){
        return new IBlockStateGridBuilder();
    }

    @Nonnull
    public IBlockStateLayerBuilder layer(){
        return new IBlockStateLayerBuilder();
    }

    @Nonnull
    public IBlockState[][][] build(){
        return layers.stream().map(IBlockStateLayerBuilder::build).toArray(IBlockState[][][]::new);
    }

    public final class IBlockStateLayerBuilder {
        private final List<IBlockState[]> rows = new ArrayList<>();

        @Nonnull
        public IBlockStateLayerBuilder row(@Nonnull final IBlockState... row){
            rows.add(row);
            return this;
        }

        @Nonnull
        public IBlockStateGridBuilder done(){
            layers.add(this);
            return IBlockStateGridBuilder.this;
        }

        @Nonnull
        public IBlockState[][] build(){
            return rows.toArray(new IBlockState[0][0]);
        }
    }
}
