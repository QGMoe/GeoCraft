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
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author QiguaiAAAA
 */
public class UnmodifiableConfigurableCollection<V> implements IConfigurableCollection<V> {

    protected final Collection<? extends V> collection;

    public UnmodifiableConfigurableCollection(@Nonnull Collection<? extends V> c) {
        this.collection = c;
    }

    public int size(){
        return collection.size();
    }

    public boolean isEmpty(){
        return collection.isEmpty();
    }

    public boolean contains(Object o){
        return collection.contains(o);
    }

    public Object[] toArray(){
        return collection.toArray();
    }

    public <T> T[] toArray(@Nonnull T[] a){
        return collection.toArray(a);
    }

    public String toString(){
        return collection.toString();
    }

    public Iterator<V> iterator() {
        return new Iterator<V>() {
            private final Iterator<? extends V> i = collection.iterator();

            public boolean hasNext() {
                return i.hasNext();
            }

            public V next(){
                return i.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                i.forEachRemaining(action);
            }
        };
    }

    public boolean add(V e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(@Nonnull Collection<?> c) {
        return collection.containsAll(c);
    }
    public boolean addAll(@Nonnull Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }
    public boolean removeAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    public boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        collection.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super V> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<V> spliterator() {
        return (Spliterator<V>) collection.spliterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<V> stream() {
        return (Stream<V>) collection.stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<V> parallelStream() {
        return (Stream<V>) collection.parallelStream();
    }
}
