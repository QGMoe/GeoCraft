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
import org.junit.jupiter.api.Assertions;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.映射.映射器;
import 清汩萌.造.词块.下标工具;
import 清汩萌.造.词块.主体工具;
import 清汩萌.造.词块.词块;
import 清汩萌.造.造;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public IBlockState[][][] 构造(final int $边长,final @Nonnull String[] $原始层){
        return 构造($边长, $边长, $原始层);
    }

    @Nonnull
    public IBlockState[][][] 构造(final int $行数,final int $列数,final @Nonnull String[] $原始层){
        if($行数 == 0 || $列数 == 0) return new IBlockState[0][0][0];
        return 构造(Arrays.stream($原始层)
                .map(layer -> Arrays.stream(layer.split("\n"))
                        .limit($行数)
                        .map(StringUtil::removeWhites)
                        .map(line ->{
                            final @Nonnull List<词块> $词块列表 = 解析行(line);
                            Assertions.assertEquals($词块列表.size(),$列数,"Line "+ line +" has different length of "+$列数);
                            return $词块列表;
                        }).collect(Collectors.toList())
                ).collect(Collectors.toList())
        );
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
    public static List<词块> 解析行(final @Nonnull String $行){
        return 解析行($行.codePoints());
    }

    @Nonnull
    public static List<词块> 解析行(final @Nonnull IntStream $行){
        final List<词块> $词块列表 = new ArrayList<>();
        final int[] codePoints = $行.toArray();
        int i = 0;

        final @Nonnull StringBuilder $当前词块 = new StringBuilder();
        boolean $处于主体中 = true;
        boolean $处于括号中 = false;

        while (i < codePoints.length) {
            final int cp = codePoints[i];

            if ($处于主体中) {
                if(cp == (int)'['){
                    $处于括号中 = true;
                    i++;
                    continue;
                }else if($处于括号中 && cp == (int)']'){
                    $处于括号中 = false;
                    $处于主体中 = false;
                    i++;
                    continue;
                } else if(下标工具.是下标字符(cp)) throw new IllegalArgumentException(new String(codePoints,0,codePoints.length) +" 主体中不能包含下标字符!");
                else if(!主体工具.是主体字符(cp))
                    throw new IllegalArgumentException(new String(codePoints,0,codePoints.length) + " 包含非法主体字符 "+new String(new int[]{cp},0,1));
                $当前词块.appendCodePoint(cp);
                if(!$处于括号中) $处于主体中 = false;
                i++;
            } else {
                if (下标工具.是下标字符(cp)) { //下标，属于当前序列化状态
                    $当前词块.appendCodePoint(cp);
                    i++;
                } else {
                    $词块列表.add(词块.of($当前词块.toString()));
                    $当前词块.setLength(0);
                    $处于主体中 = true;
                }
            }
        }
        if ($当前词块.length() > 0) $词块列表.add(词块.of($当前词块.toString())); //处理最后一个

        return $词块列表;
    }

    @Nonnull
    public String[] 序列化(final @Nonnull IBlockState[][][] $网格){
        final String[] serialised = new String[$网格.length];
        for(int y=0;y<$网格.length;y++){
            final StringBuilder builder = new StringBuilder();
            for(int z =0;z<$网格[y].length;z++){
                for(int x=0;x<$网格[y][z].length;x++)
                    builder.append(进行映射($网格[y][z][x]));
                builder.append('\n');
            }
            serialised[y] = builder.toString();
        }
        return serialised;
    }

    public void 打印(final @Nonnull IBlockState[][][] 网格){
        打印(网格,造.LOGGER);
    }

    public void 打印(final @Nonnull IBlockState[][][] 网格, final @Nonnull Logger logger){
        logger.info("构造:\n{}",String.join("\n",序列化(网格)));
    }

    @Nonnull
    public List<映射器> 获取映射表(){
        return this.$映射表;
    }

}
