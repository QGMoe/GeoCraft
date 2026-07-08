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

package 清汩萌.造.管理;

import net.minecraft.util.ResourceLocation;
import 清汩萌.造.空间.空间构造器;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author QGMoe
 */
public final class 空间构造局 {
    private static final HashMap<ResourceLocation, 空间构造器> $构造注册表 = new HashMap<>();

    public static void 登记(final ResourceLocation id, final 空间构造器 $空间构造器){
        final 空间构造器 $旧的 = $构造注册表.put(id,$空间构造器);
        if($旧的 != null) throw new IllegalArgumentException(id+" 已被占用");
    }

    @Nullable
    public static 空间构造器 查询(final @Nonnull ResourceLocation id){
        return $构造注册表.get(Objects.requireNonNull(id));
    }

    @Nonnull
    public static 空间构造器 需要(final @Nonnull ResourceLocation id){
        final @Nullable 空间构造器 $器 = $构造注册表.get(id);
        if ($器 == null) throw new IllegalArgumentException("未知的空间构造器："+id);
        return $器;
    }
}
