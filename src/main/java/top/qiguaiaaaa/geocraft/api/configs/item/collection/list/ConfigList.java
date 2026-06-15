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

package top.qiguaiaaaa.geocraft.api.configs.item.collection.list;

import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.ConfigCollection;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.ConfigurableList;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.IConfigurableList;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.UnmodifiableConfigurableList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigList<ValueType,SELF extends ConfigList<ValueType,SELF>>
        extends ConfigCollection<IConfigurableList<ValueType>,ValueType,SELF> implements List<ValueType>{
    protected ConfigList(final @Nonnull ConfigCategory category,
                      final @Nonnull String configKey,
                      final @Nonnull IConfigurableList<ValueType> defaultValue,
                      final @Nonnull Function<String,ValueType> parser) {
        this(category, configKey, defaultValue,parser,ConfigurableList::new);
    }

    protected ConfigList(final @Nonnull ConfigCategory category,
                      final @Nonnull String configKey,
                      final @Nonnull IConfigurableList<ValueType> defaultValue,
                      final @Nonnull Function<String,ValueType> parser,
                      final @Nonnull Supplier<IConfigurableList<ValueType>> factory) {
        super(category, configKey, defaultValue,parser,factory);
    }

    public static <V> ConfigList<V,? extends ConfigList<V,?>> create(final @Nonnull ConfigCategory category,
                         final @Nonnull String configKey,
                         final @Nonnull IConfigurableList<V> defaultValue,
                         final @Nonnull Function<String,V> parser) {
        return new Impl<>(category, configKey, defaultValue,parser,ConfigurableList::new);
    }

    public static <V> ConfigList<V,? extends ConfigList<V,?>> create(final @Nonnull ConfigCategory category,
                                                                     final @Nonnull String configKey,
                                                                     final @Nonnull IConfigurableList<V> defaultValue,
                                                                     final @Nonnull Function<String,V> parser,
                                                                     final @Nonnull Supplier<IConfigurableList<V>> factory) {
        return new Impl<>(category, configKey, defaultValue,parser,factory);
    }

    @Nonnull
    @Override
    protected Collection<ValueType> getUnmodifiableCollection() {
        if(unmodifiable == null) unmodifiable = Collections.unmodifiableList(value);
        return unmodifiable;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.list";
    }

    //***********
    // List
    //***********

    @Override
    public boolean addAll(final int index,final @Nonnull Collection<? extends ValueType> c) {
        if(!sizeRequire.validate(this.size() + c.size())) throw new UnsupportedOperationException();
        return value.addAll(c);
    }

    /**
     * @see List#get(int)
     */
    public ValueType get(final int index){
        return value.get(index);
    }

    @Override
    public ValueType set(final int index,final @Nonnull ValueType element) {
        return value.set(index,element);
    }

    @Override
    public void add(final int index,final @Nonnull ValueType element) {
        if(!sizeRequire.validate(this.size() + 1)) throw new UnsupportedOperationException();
        value.add(index,element);
    }

    @Override
    public ValueType remove(final int index) {
        if(!sizeRequire.validate(this.size()-1)) throw new UnsupportedOperationException();
        return value.remove(index);
    }

    @Override
    public int indexOf(final @Nonnull Object o) {
        return value.indexOf(o);
    }

    @Override
    public int lastIndexOf(final @Nonnull Object o) {
        return value.lastIndexOf(o);
    }

    @Nonnull
    @Override
    public ListIterator<ValueType> listIterator() {
        if(sizeRequire.isSizeFixed()) return ((List<ValueType>)getUnmodifiableCollection()).listIterator();
        return value.listIterator();
    }

    @Nonnull
    @Override
    public ListIterator<ValueType> listIterator(final int index) {
        if(sizeRequire.isSizeFixed()) return ((List<ValueType>)getUnmodifiableCollection()).listIterator(index);
        return value.listIterator(index);
    }

    @Nonnull
    @Override
    public List<ValueType> subList(final int fromIndex,final int toIndex) {
        return value.subList(fromIndex, toIndex);
    }

    @Nonnull
    @Override
    public IConfigurableList<ValueType> getValue() {
        if(sizeRequire.isSizeFixed()) return new UnmodifiableConfigurableList<>(value);
        return value;
    }

    private static final class Impl<V> extends ConfigList<V,Impl<V>>{

        public Impl(@Nonnull final ConfigCategory category,
                    @Nonnull final String configKey,
                    @Nonnull final IConfigurableList<V> defaultValue,
                    @Nonnull final Function<String, V> parser,
                    @Nonnull final Supplier<IConfigurableList<V>> factory) {
            super(category, configKey, defaultValue, parser, factory);
        }
    }
}
