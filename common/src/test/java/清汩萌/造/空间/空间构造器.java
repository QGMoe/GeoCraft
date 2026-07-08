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
import org.apache.logging.log4j.Logger;
import 清汩萌.造.映射.映射器;
import 清汩萌.造.词块.词块;
import 清汩萌.造.造;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author QiguaiAAAA
 */
public final class 空间构造器 {

    private final List<映射器> $映射表 = new ArrayList<>();

    public 空间构造器 添加映射(final @Nonnull 映射器 $映射器){
        $映射表.add($映射器);
        return this;
    }

    @Nonnull
    public IBlockState 进行映射(final @Nonnull 词块 $词块){
        for(final 映射器 $映射器:$映射表){
            try {
                return $映射器.进行映射($词块);
            }catch (final @Nonnull IllegalArgumentException ignored){}
        }
        throw new IllegalArgumentException($词块 + " 无可用映射");
    }

    @Nonnull
    public 词块 进行映射(final @Nonnull IBlockState state){
        for(final 映射器 $映射器:$映射表){
            try {
                return $映射器.进行映射(state);
            }catch (final @Nonnull IllegalArgumentException ignored){}
        }
        throw new IllegalArgumentException(state + " 无可用映射");
    }

    @Nonnull
    public IBlockState[][][] 构造(final List<List<List<词块>>> $原始词块数据){
        return $原始词块数据.stream()
                .map($单层 -> $单层.stream()
                        .map($单行 -> $单行.stream()
                                .map(this::进行映射)
                                .toArray(IBlockState[]::new))
                        .toArray(IBlockState[][]::new)
                ).toArray(IBlockState[][][]::new);
    }

    @Nonnull
    public 词块网格 构造(){
        return new 词块网格().基于(this);
    }

    @Nonnull
    public 词块[][][] 序列化(final @Nonnull IBlockState[][][] $网格){
        final 词块[][][] $汉字网格 = new 词块[$网格.length][][];
        for(int y=0;y<$网格.length;y++){
            $汉字网格[y] = new 词块[$网格[y].length][];
            for(int z =0;z<$网格[y].length;z++){
                $汉字网格[y][z] = new 词块[$网格[y][z].length];
                for(int x=0;x<$网格[y][z].length;x++)
                    $汉字网格[y][z][x] = 进行映射($网格[y][z][x]);
            }
        }
        return $汉字网格;
    }

    public void 打印(final @Nonnull IBlockState[][][] $网格){
        打印($网格,造.LOGGER);
    }

    public void 打印(final @Nonnull IBlockState[][][] $网格, final @Nonnull Logger logger){
        logger.info("构造:\n{}", Arrays.stream(序列化($网格))
                .map($层 -> Arrays.stream($层)
                        .map($行 -> Arrays.stream($行)
                                .map(Objects::toString)
                                .collect(Collectors.joining()))
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n")));
    }

    @Nonnull
    public List<映射器> 获取映射表(){
        return this.$映射表;
    }

}
