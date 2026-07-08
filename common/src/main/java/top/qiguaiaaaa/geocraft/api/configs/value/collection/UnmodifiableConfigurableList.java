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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * @author QiguaiAAAA
 */
public class UnmodifiableConfigurableList<V> extends UnmodifiableConfigurableCollection<V> implements IConfigurableList<V> {
    protected final List<? extends V> list;

    public UnmodifiableConfigurableList(@Nonnull List<? extends V> c) {
        super(c);
        list = c;
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(int index) {
        return list.get(index);
    }

    @Override
    public V set(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<V> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<V> listIterator(final int index) {
        return new ListIterator<V>() {
            private final ListIterator<? extends V> i = list.listIterator(index);

            @Override
            public boolean hasNext(){
                return i.hasNext();
            }
            @Override
            public V next(){
                return i.next();
            }
            @Override
            public boolean hasPrevious(){
                return i.hasPrevious();
            }
            @Override
            public V previous(){
                return i.previous();
            }
            @Override
            public int nextIndex(){
                return i.nextIndex();
            }
            @Override
            public int previousIndex(){
                return i.previousIndex();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void set(V e) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void add(V e) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                i.forEachRemaining(action);
            }
        };
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(list.subList(fromIndex,toIndex));
    }
}
