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

package top.qiguaiaaaa.geocraft.api.configs.item.base;

import net.minecraftforge.common.config.Property;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

/**
 * 提供更多自定义的配置项目,需要传入一个方法用于获取值的实例
 * @param <V> 值类型,注意要实现{@link Object#toString()}以写入配置文件
 */
public class ConfigCustom<V> extends ConfigItem<V,ConfigCustom<V>> {
    protected final Function<String,V> parser; //转换器，将字符串反序列化为对应配置值

    /**
     * 创建一个配置项
     * @param category 配置所在目录
     * @param configKey 配置的key
     * @param defaultValue 配置的默认值，不应为null，因为会调用{@link Object#toString()}
     * @param parser 反序列化器，用于将字符串反序列化为对应配置值
     */
    public ConfigCustom(@Nonnull final ConfigCategory category,@Nonnull final String configKey,@Nonnull final V defaultValue,@Nonnull final Function<String,V> parser) {
        super(category, configKey, defaultValue);
        this.parser = Objects.requireNonNull(parser);
    }

    @Nonnull
    public Function<String, V> getParser() {
        return parser;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.custom";
    }

    /**
     * {@inheritDoc}
     * @param property {@inheritDoc}
     */
    @Override
    protected void load(@Nonnull final Property property) {
        this.value = parser.apply(property.getString());
        if(this.value == null) this.value = defaultValue;
    }
}
