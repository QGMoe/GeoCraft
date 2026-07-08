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

package top.qiguaiaaaa.geocraft.api.configs.value.collection;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 一个用于列表配置项的配置类型
 * @param <V> 列表存储的对象类型，存储的对象应当覆写{@link Object#toString()}
 */
public class ConfigurableList<V> extends ArrayList<V> implements IConfigurableList<V> {
    /**
     * @see ArrayList#ArrayList()
     */
    public ConfigurableList() {
        super();
    }

    public ConfigurableList(int initialCapacity){
        super(initialCapacity);
    }

    public ConfigurableList(@Nonnull Collection<? extends V> c){
        super(c);
    }

    @SafeVarargs
    public ConfigurableList(@Nonnull V... elements){
        super(Arrays.asList(elements));
    }

    @Override
    public V set(int index,@Nonnull V element) {
        return super.set(index, element);
    }

    @Override
    public boolean add(@Nonnull V v) {
        return super.add(v);
    }

    @Override
    public void add(int index,@Nonnull V element) {
        super.add(index, element);
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends V> c) {
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index,@Nonnull Collection<? extends V> c) {
        return super.addAll(index, c);
    }
}
