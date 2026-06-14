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

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.GeoCraftAPI;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.IConfigurableCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author QiguaiAAAA
 */
public abstract class ConfigCollection<CollectionType extends IConfigurableCollection<ValueType>,
        ValueType,
        SELF extends ConfigCollection<CollectionType,ValueType,SELF>>
        extends ConfigItem<CollectionType,SELF> implements Collection<ValueType> {
    protected final Function<String,ValueType> parser;
    protected final Supplier<CollectionType> factory;

    protected SizeRequirement sizeRequire = SizeRequirement.NONE;
    protected @Nullable Pattern validatedPattern = null;

    protected @Nullable Collection<ValueType> unmodifiable = null;

    public ConfigCollection(final @Nonnull ConfigCategory category,
                            final @Nonnull String configKey,
                            final @Nonnull CollectionType defaultValue,
                            final @Nonnull Function<String,ValueType> parser,
                            final @Nonnull Supplier<CollectionType> factory) {
        super(category, configKey, defaultValue);
        this.parser = parser;
        this.factory = factory;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF requireSize(final @Nonnull SizeRequirement requirement) {
        this.sizeRequire = Objects.requireNonNull(requirement);
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF setPattern(final @Nonnull Pattern validatedPattern) {
        this.validatedPattern = validatedPattern;
        return (SELF) this;
    }

    @Override
    public void save() {
        if(property == null) return;
        property.setValues(value.toStringList());
        property.setComment(getConstructedComment());
    }

    @Nonnull
    @Override
    public abstract CollectionType getValue();

    @Override
    public void setValue(@Nonnull final CollectionType newValue) {
        if(!sizeRequire.validate(newValue.size())) throw new IllegalArgumentException();
        this.unmodifiable = null;
        super.setValue(newValue);
    }

    @Override
    public void load(@Nonnull final Configuration config) {
        property = config.get(category.getPath(),key,defaultValue.toStringList(),getConstructedComment(),sizeRequire.isSizeFixed(),sizeRequire.getMaxListSize(),validatedPattern);
        load(property);
    }

    @Override
    protected void load(@Nonnull final Property property) {
        this.unmodifiable = null;
        value = factory.get();

        final @Nonnull String[] strings = property.getStringList();
        for(final @Nonnull String string:strings){
            try {
                final @Nonnull ValueType loadedVal = parser.apply(string);
                value.add(loadedVal);
            }catch (final @Nonnull Exception e){
                GeoCraftAPI.LOGGER.warn("loading configuration {} in {} error",string,category);
                GeoCraftAPI.LOGGER.warn("Error Detailed:",e);
            }
        }
    }

    @Nonnull
    protected Collection<ValueType> getUnmodifiableCollection(){
        if(unmodifiable == null) unmodifiable = Collections.unmodifiableCollection(this.value);
        return unmodifiable;
    }

    @Nonnull
    @Override
    protected List<Pair<String, String>> getCommentProperties() {
        final List<Pair<String,String>> list = super.getCommentProperties();
        if(sizeRequire instanceof SizeRequirement.Fixed) list.add(Pair.of("固定大小 Size Fixed",null));
        else if(sizeRequire instanceof SizeRequirement.Range) list.add(Pair.of("大小范围 Size Range",
                ((SizeRequirement.Range) sizeRequire).min +" ~ "+ ((SizeRequirement.Range) sizeRequire).max));
        if(validatedPattern != null) list.add(Pair.of("正则表达式限制 Regex Limitation",validatedPattern.toString()));
        return list;
    }

    //**********
    // Collection
    //**********

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean contains(final @Nullable Object o) {
        return value.contains(o);
    }

    @Nonnull
    @Override
    public Iterator<ValueType> iterator() {
        if(sizeRequire.getMaxListSize() != -1) return getUnmodifiableCollection().iterator();
        return value.iterator();
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull final T[] a) {
        return value.toArray(a);
    }

    @Override
    public boolean add(@Nonnull final ValueType type) {
        if(!sizeRequire.validate(value.size()+1)) throw new UnsupportedOperationException();
        return value.add(type);
    }

    @Override
    public boolean remove(final @Nonnull Object o) {
        if(!sizeRequire.validate(value.size()-1)) throw new UnsupportedOperationException();
        return value.remove(o);
    }

    @Override
    public boolean containsAll(@Nonnull final Collection<?> c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(@Nonnull final Collection<? extends ValueType> c) {
        if(!sizeRequire.validate(value.size()+c.size())) throw new UnsupportedOperationException();
        return value.addAll(c);
    }

    @Override
    public boolean removeAll(@Nonnull final Collection<?> c) {
        if(sizeRequire.isSizeFixed()) throw new UnsupportedOperationException();
        return value.removeAll(c);
    }

    @Override
    public boolean removeIf(final @Nonnull Predicate<? super ValueType> filter) {
        if(sizeRequire.isSizeFixed()) throw new UnsupportedOperationException();
        return value.removeIf(filter);
    }

    @Override
    public boolean retainAll(@Nonnull final Collection<?> c) {
        if(sizeRequire.isSizeFixed()) throw new UnsupportedOperationException();
        return value.retainAll(c);
    }

    @Override
    public void clear() {
        if(!sizeRequire.validate(0)) throw new UnsupportedOperationException();
        value.clear();
    }
}
