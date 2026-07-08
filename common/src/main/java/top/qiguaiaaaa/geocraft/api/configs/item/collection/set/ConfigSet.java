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

package top.qiguaiaaaa.geocraft.api.configs.item.collection.set;

import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.ConfigCollection;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.ConfigurableSet;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.IConfigurableSet;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.UnmodifiableConfigurableSet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author QiguaiAAAA
 */
public class ConfigSet<V,S extends ConfigSet<V,S>> extends ConfigCollection<IConfigurableSet<V>,V,ConfigSet<V,S>> implements Set<V> {

    protected ConfigSet(final @Nonnull ConfigCategory category,
                     final @Nonnull String configKey,
                     final @Nonnull IConfigurableSet<V> defaultValue,
                     final @Nonnull Function<String,V> parser) {
        super(category, configKey, defaultValue, parser,ConfigurableSet::new);
    }

    protected ConfigSet(final @Nonnull ConfigCategory category,
                     final @Nonnull String configKey,
                     final @Nonnull IConfigurableSet<V> defaultValue,
                     final @Nonnull Function<String,V> parser,
                     final @Nonnull Supplier<IConfigurableSet<V>> factory) {
        super(category, configKey, defaultValue, parser,factory);
    }

    public static <V> ConfigSet<V,? extends ConfigSet<V,?>> create(final @Nonnull ConfigCategory category,
                                                                   final @Nonnull String configKey,
                                                                   final @Nonnull IConfigurableSet<V> defaultValue,
                                                                   final @Nonnull Function<String,V> parser){
        return new Impl<>(category,configKey,defaultValue,parser);
    }

    public static <V> ConfigSet<V,? extends ConfigSet<V,?>> create(final @Nonnull ConfigCategory category,
                                                                   final @Nonnull String configKey,
                                                                   final @Nonnull IConfigurableSet<V> defaultValue,
                                                                   final @Nonnull Function<String,V> parser,
                                                                   final @Nonnull Supplier<IConfigurableSet<V>> factory){
        return new Impl<>(category,configKey,defaultValue,parser,factory);
    }

    @Nonnull
    @Override
    public IConfigurableSet<V> getValue() {
        if(sizeRequire.isSizeFixed()) return (UnmodifiableConfigurableSet<V>) unmodifiable;
        return value;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.set";
    }

    @Nonnull
    @Override
    protected Collection<V> getUnmodifiableCollection() {
        if(unmodifiable == null) unmodifiable = new UnmodifiableConfigurableSet<>(value);
        return unmodifiable;
    }

    private static final class Impl<V> extends ConfigSet<V,Impl<V>>{

        private Impl(@Nonnull final ConfigCategory category,
                     @Nonnull final String configKey,
                     @Nonnull final IConfigurableSet<V> defaultValue,
                     @Nonnull final Function<String, V> parser) {
            super(category, configKey, defaultValue, parser);
        }

        private Impl(@Nonnull final ConfigCategory category,
                     @Nonnull final String configKey,
                     @Nonnull final IConfigurableSet<V> defaultValue,
                     @Nonnull final Function<String, V> parser,
                     @Nonnull final Supplier<IConfigurableSet<V>> factory) {
            super(category, configKey, defaultValue, parser, factory);
        }
    }
}
