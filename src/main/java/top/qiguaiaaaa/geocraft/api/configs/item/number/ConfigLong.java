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

package top.qiguaiaaaa.geocraft.api.configs.item.number;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * {@link Long}配置项
 */
public class ConfigLong extends ConfigItem<Long,ConfigLong> {
    protected long minValue = Long.MIN_VALUE;
    protected long maxValue = Long.MAX_VALUE;

    /**
     * 创建一个Long类型配置项
     * @param category 配置所在目录
     * @param configKey 配置的key
     * @param defaultValue 配置的默认值
     */
    public ConfigLong(@Nonnull final ConfigCategory category, @Nonnull final String configKey,final long defaultValue) {
        super(category, configKey, defaultValue);
    }

    @Nonnull
    public ConfigLong setMinValue(final long minValue) {
        this.minValue = minValue;
        return this;
    }

    @Nonnull
    public ConfigLong setMaxValue(final long maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public long getMinValue() {
        return minValue;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.long";
    }

    /**
     * {@inheritDoc}
     * @param config {@inheritDoc}
     */
    @Override
    public void load(@Nonnull final Configuration config) {
        property = config.get(category.getPath(),key,defaultValue.toString(),null);
        property.setComment(getConstructedComment());
        load(property);
    }

    @Override
    public void save() {
        if(property == null) return;
        property.setValue(value.toString());
        property.setComment(getConstructedComment());
    }

    /**
     * {@inheritDoc}
     * @param property {@inheritDoc}
     */
    @Override
    protected void load(@Nonnull final Property property) {
        this.value = property.getLong(defaultValue);
        Validate.inclusiveBetween(minValue,maxValue,(long) value);
    }

    @Nonnull
    @Override
    protected List<Pair<String, String>> getCommentProperties() {
        final List<Pair<String,String>> list = super.getCommentProperties();
        list.add(Pair.of("范围 Range",minValue + " ~ "+maxValue));
        return list;
    }

}
