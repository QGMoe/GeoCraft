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

package 清汩萌.天圆地方.util;

import org.junit.jupiter.api.Assertions;
import 清汩萌.造.空间.网格参数;
import 清汩萌.造.空间.词块网格;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author QGMoe
 */
public final class 网格工具 {
    private 网格工具(){}

    @Nonnull
    public static Object[] 打包网格数据(final @Nonnull 词块网格 $网格){
        return new Object[]{$网格.获取原始网格数据(),
                $网格.获取参数(),
                MessyUtil.toNullableString($网格.获取默认填充方块()),
                $网格.获取默认构造器名称(),
                $网格.获取默认映射器名称集合()};
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static 词块网格 恢复网格数据(final @Nonnull Object[] raw){
        Assertions.assertEquals(5,raw.length);
        return 恢复网格数据((List<List<List<String>>>) raw[0], (int[]) raw[1], (String) raw[2],(String) raw[3],(Set<String>) raw[4]);
    }

    @Nonnull
    public static 词块网格 恢复网格数据(final @Nonnull List<List<List<String>>> $原始网格数据,
                                        final int[] $原始尺寸数据,
                                        final @Nullable String $原始默认方块数据,
                                        final @Nullable String $默认构造器名称,
                                        final @Nullable Set<String> $默认构造器名称集合){
        final 词块网格 $网格 = 词块网格.从原始网格数据恢复($原始网格数据);
        for(final 网格参数 $参数:网格参数.列表) $网格.期望($原始尺寸数据[$参数.ordinal()],$参数);
        if($原始默认方块数据 != null) $网格.默认用($原始默认方块数据);
        if($默认构造器名称 != null) $网格.基于($默认构造器名称);
        if($默认构造器名称集合 != null) $网格.基于($默认构造器名称集合);
        return $网格;
    }
}
