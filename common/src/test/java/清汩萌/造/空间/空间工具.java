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

import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author QGMoe
 */
public final class 空间工具 {
    private 空间工具(){}

    public static int[] 转换为游戏坐标(final @Nonnull int[] $网格坐标){
        if($网格坐标.length != 3) throw new IllegalArgumentException();
        return new int[]{$网格坐标[2]-1,$网格坐标[0]-1,$网格坐标[1]-1};
    }

    public static void 打印元数据(final @Nonnull Logger $日志, final @Nonnull 词块网格 $网格){
        $日志.info("[词块网格信息]");
        $日志.info("网格参数：{}", Arrays.toString($网格.获取参数()));
        $日志.info("网格默认构造器名：{}",$网格.获取默认构造器名称() == null?"NULL":$网格.获取默认构造器名称());
        $日志.info("网格使用/附加映射：{}",$网格.获取默认映射器名称集合()==null?"{}":$网格.获取默认映射器名称集合());
        $日志.info("默认方块：{}",$网格.获取默认填充方块()==null?"NULL":$网格.获取默认填充方块());
    }
}
