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

package top.qiguaiaaaa.geocraft.api.configs.item.collection;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public interface SizeRequirement {
    None NONE = new None();

    boolean validate(final int size);

    boolean isSizeFixed();

    int getMaxListSize();

    @Nonnull
    static None none(){
        return NONE;
    }

    @Nonnull
    static Range range(final int min,final int max){
        return new Range().min(min).max(max);
    }

    @Nonnull
    static Fixed fixed(final int size){
        return new Fixed().size(size);
    }

    final class None implements SizeRequirement{

        private None(){}

        @Override
        public boolean validate(final int size) {
            return true;
        }

        @Override
        public boolean isSizeFixed() {
            return false;
        }

        @Override
        public int getMaxListSize() {
            return -1;
        }
    }

    final class Range implements SizeRequirement{
        int min = 0;
        int max = Integer.MAX_VALUE;

        @Nonnull
        public Range min(final int min){
            if(min > max || min < 0) throw new IllegalArgumentException();
            this.min = min;
            return this;
        }

        @Nonnull
        public Range max(final int max){
            if(min > max) throw new IllegalArgumentException();
            this.max = max;
            return this;
        }

        @Override
        public boolean validate(final int size) {
            return size >= min && size <= max;
        }

        @Override
        public boolean isSizeFixed() {
            return false;
        }

        @Override
        public int getMaxListSize() {
            return max;
        }
    }

    final class Fixed implements SizeRequirement{
        int size = 0;

        @Nonnull
        public Fixed size(final int size){
            if(size < 0) throw new IllegalArgumentException();
            this.size = size;
            return this;
        }

        @Override
        public boolean validate(final int size) {
            return this.size == size;
        }

        @Override
        public boolean isSizeFixed() {
            return true;
        }

        @Override
        public int getMaxListSize() {
            return size;
        }
    }
}
