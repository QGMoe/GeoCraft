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

import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.IConfigIntCollection;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.IConfigurableList;

import javax.annotation.Nonnull;

import java.util.List;

import static top.qiguaiaaaa.geocraft.api.configs.item.collection.set.ConfigIntegerSet.toIntList;


public class ConfigIntegerList<S extends ConfigIntegerList<S>> extends ConfigList<Integer,S> implements IConfigIntCollection<S> {
    protected int minValue = Integer.MIN_VALUE;
    protected int maxValue = Integer.MAX_VALUE;

    protected ConfigIntegerList(final @Nonnull ConfigCategory category,
                             final @Nonnull String configKey,
                             final @Nonnull IConfigurableList<Integer> defaultValue) {
        super(category, configKey, defaultValue, Integer::parseInt);
    }

    @Nonnull
    public static ConfigIntegerList<? extends ConfigIntegerList<?>> create(final @Nonnull ConfigCategory category,
                                       final @Nonnull String configKey,
                                       final @Nonnull IConfigurableList<Integer> defaultValue){
        return new Impl(category, configKey, defaultValue);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public S setMinValue(final int minValue) {
        this.minValue = minValue;
        return (S) this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public S setMaxValue(final int maxValue) {
        this.maxValue = maxValue;
        return (S) this;
    }

    @Override
    public int getMinValue() {
        return minValue;
    }
    @Override
    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public void save() {
        if(property == null) return;
        property.setValues(toIntList(value));
        property.setComment(getConstructedComment());
    }

    @Override
    public void load(@Nonnull Configuration config) {
        property = config.get(category.getPath(),key,toIntList(defaultValue),null,minValue,maxValue, sizeRequire.isSizeFixed(), sizeRequire.getMaxListSize());
        property.setComment(getConstructedComment());
        load(property);
    }

    @Nonnull
    @Override
    protected List<Pair<String, String>> getCommentProperties() {
        final List<Pair<String,String>> list = super.getCommentProperties();
        list.add(Pair.of("范围 Range",minValue + " ~ "+maxValue));
        return list;
    }


    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.list.int";
    }

    private static final class Impl extends ConfigIntegerList<Impl>{

        public Impl(@Nonnull final ConfigCategory category, @Nonnull final String configKey, @Nonnull final IConfigurableList<Integer> defaultValue) {
            super(category, configKey, defaultValue);
        }
    }
}
