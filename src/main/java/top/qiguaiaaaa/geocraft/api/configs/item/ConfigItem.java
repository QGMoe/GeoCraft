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

package top.qiguaiaaaa.geocraft.api.configs.item;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.EffectiveMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * 配置项目
 * @param <V> 配置的值类型,需要支持{@link Object#toString()}以写入配置文件
 * @param <S> CRTP
 */
public abstract class ConfigItem<V,S extends ConfigItem<V,S>> {
    protected final ConfigCategory category;
    protected final String key;
    protected final V defaultValue;
    protected final Map<String,String> additionalProperties = new HashMap<>();
    protected EffectiveMode mode;
    protected @Nullable String comment;
    protected @Nullable String translationKey;
    protected V value;
    protected @Nullable Property property;
    protected boolean isDeprecated;
    protected boolean isBeta;

    /**
     * 创建一个配置项
     * @param category 配置所在目录
     * @param configKey 配置的key
     * @param defaultValue 配置的默认值，不应为null，因为会调用{@link Object#toString()}
     */
    public ConfigItem(@Nonnull final ConfigCategory category, final @Nonnull String configKey, final @Nonnull V defaultValue){
        this.category = Objects.requireNonNull(category);
        this.key = Objects.requireNonNull(configKey);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.value = defaultValue;
    }

    @Nonnull
    public final ConfigCategory getCategory() {
        return category;
    }

    @Nonnull
    public final V getDefaultValue(){
        return defaultValue;
    }

    @Nonnull
    public final String getKey(){
        return key;
    }

    @Nonnull
    public EffectiveMode getEffectiveMode() {
        if(mode == null) return EffectiveMode.INSTANT;
        return mode;
    }

    @Nonnull
    public V getValue(){
        return value;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @Nonnull
    public String getConstructedComment(){
        final @Nonnull List<Pair<String,String>> props = this.getCommentProperties();
        props.sort(Map.Entry.comparingByKey());
        final StringBuilder builder = new StringBuilder();
        if(hasComment()) builder.append(getComment()).append('\n');
        for(int i=0;i<props.size();i++){
            final @Nonnull Pair<String,String> pair = props.get(i);
            builder.append('[').append(pair.getKey());
            if(pair.getValue() != null) builder.append(" : ").append(pair.getValue());
            builder.append(']');
            if(i < props.size()-1) builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * 获取该配置项的配置路径
     * @return 配置路径，例如 exampleCategory.exampleItem
     */
    @Nonnull
    public String getPath(){
        return category.getPath() + Configuration.CATEGORY_SPLITTER + key;
    }

    @Nonnull
    public abstract String getTypeTranslationKey();

    @Nullable
    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * 更新配置项的值
     * @param newValue 新值，注意不能为 null
     */
    public void setValue(@Nonnull final V newValue){
        this.value = newValue;
    }

    /**
     * 标记当前配置项目是否已被弃用
     * @param deprecated 是否被弃用
     */
    @SuppressWarnings("unchecked")
    public S setDeprecated(final boolean deprecated) {
        isDeprecated = deprecated;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S setBeta(final boolean beta) {
        isBeta = beta;
        return (S) this;
    }

    /**
     * 设置当前配置项的生效模式
     * @param mode 生效模式
     * @throws IllegalStateException 如果当前配置项已经设置生效模式则抛出
     */
    @SuppressWarnings("unchecked")
    public S setMode(final @Nonnull EffectiveMode mode) {
        if(this.mode != null) throw new IllegalStateException();
        this.mode = mode;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S setComment(@Nullable final String comment) {
        this.comment = comment;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S setTranslationKey(@Nullable final String translationKey) {
        this.translationKey = translationKey;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S addCommentProperty(final @Nonnull String key,final @Nullable String value){
        this.additionalProperties.put(key,value);
        return (S) this;
    }

    /**
     * 该配置项是否已被弃用
     * @return 若已被弃用,则返回 true
     */
    public boolean isDeprecated() {
        return isDeprecated;
    }

    public boolean isBeta() {
        return isBeta;
    }

    /**
     * 该配置项是否拥有注释
     * @return 若有，则返回true
     */
    public boolean hasComment(){
        return comment != null;
    }

    /**
     * 提供指定的配置文件,以加载当前配置项目
     * @param config 指定的配置文件
     */
    public void load(@Nonnull final Configuration config){
        property = config.get(category.getPath(),key,defaultValue.toString(),getConstructedComment());
        load(property);
    }

    /**
     * 通过{@link Property}的内容来初始化配置项
     * @param property 属性配置
     */
    protected abstract void load(@Nonnull final Property property);

    @Nonnull
    protected List<Pair<String,String>> getCommentProperties(){
        final ArrayList<Pair<String,String>> list = new ArrayList<>();
        if(isDeprecated) list.add(Pair.of("已弃用 Deprecated",null));
        if(isBeta) list.add(Pair.of("实验性项目 Experimental",null));
        additionalProperties.forEach((a,b)->list.add(Pair.of(a,b)));
        return list;
    }

    /**
     * 保存当前配置项目。若在保存前没有{@link #load(Configuration)}则不会生效
     */
    public void save(){
        if(property == null) return;
        property.setValue(value.toString());
    }
}
