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

package top.qiguaiaaaa.geocraft.util.misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author QiguaiAAAA
 */
public class FixedCollection<E> implements Collection<E> {
    protected final int maxSize;
    protected final Object[] arr;

    protected int size = 0;

    public FixedCollection(final int size){
        maxSize = size;
        arr = new Object[size];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        for(int i=0;i<size;i++){
            if(arr[i] == null && o == null) return true;
            if(o == null) continue;
            if (o.equals(arr[i])) return true;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        int cur = 0;

        @Override
        public boolean hasNext() {
            return cur < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            return (E) arr[cur++];
        }
    }

    @Override
    public Object[] toArray() {
        return arr.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(@Nonnull T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(arr, size, a.getClass());
        System.arraycopy(arr, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        arr[size++] = e;
        return true;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        boolean found = false;
        for(int i=0;i<size;i++){
            if(!found){
                if(arr[i] == null && o == null) found = true;
                else if(o != null && o.equals(arr[i])) found = true;
            }
            if(found && i < size-1){
                arr[i] = arr[i+1];
            }
        }
        if(!found) return false;
        arr[size--] = null;
        return true;
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        for(Object o:c){
            if(!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        for(E o:c){
            if(!add(o)) return false;
        }
        return false;
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        for(Object o:c){
            remove(o);
        }
        return false;
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        size = 0;
    }
}
