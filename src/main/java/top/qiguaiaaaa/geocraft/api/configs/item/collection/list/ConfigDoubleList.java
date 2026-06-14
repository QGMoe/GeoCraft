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
import top.qiguaiaaaa.geocraft.api.configs.value.collection.ConfigurableList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public class ConfigDoubleList<S extends ConfigDoubleList<S>> extends ConfigList<Double,S>{
    protected double minValue = Double.NEGATIVE_INFINITY;
    protected double maxValue = Double.POSITIVE_INFINITY;

    protected ConfigDoubleList(final @Nonnull ConfigCategory category,
                            final @Nonnull String configKey,
                            final @Nonnull ConfigurableList<Double> defaultValue) {
        super(category, configKey, defaultValue, Double::parseDouble);
    }

    @Nonnull
    public static ConfigDoubleList<?> create(final @Nonnull ConfigCategory category,
                                             final @Nonnull String configKey,
                                             final @Nonnull ConfigurableList<Double> defaultValue){
        return new Impl(category, configKey, defaultValue);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S setMinValue(final double minValue) {
        this.minValue = minValue;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
        return (S) this;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public void save() {
        if(property == null) return;
        property.setValues(toDoubleList(value));
        property.setComment(getConstructedComment());
    }

    @Override
    public void load(@Nonnull Configuration config) {
        property = config.get(category.getPath(),key,toDoubleList(defaultValue),null,minValue,maxValue, sizeRequire.isSizeFixed(), sizeRequire.getMaxListSize());
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

    protected static double[] toDoubleList(@Nonnull final Collection<Double> c){
        double[] doubles = new double[c.size()];
        int i=0;
        for(Double d:c){
            doubles[i++]=d;
        }
        return doubles;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.list.double";
    }

    private static final class Impl extends ConfigDoubleList<Impl>{

        private Impl(@Nonnull final ConfigCategory category, @Nonnull final String configKey, @Nonnull final ConfigurableList<Double> defaultValue) {
            super(category, configKey, defaultValue);
        }
    }
}
