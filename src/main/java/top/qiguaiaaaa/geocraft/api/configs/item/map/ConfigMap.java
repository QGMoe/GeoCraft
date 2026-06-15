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

package top.qiguaiaaaa.geocraft.api.configs.item.map;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.GeoCraftAPI;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.api.configs.value.map.ConfigurableLinkedHashMap;
import top.qiguaiaaaa.geocraft.api.configs.value.map.entry.ConfigEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ConfigMap<K,V> extends ConfigItem<ConfigurableLinkedHashMap<K,V>,ConfigMap<K,V>> implements Map<K,V> {

    protected final Function<String,K> parserK;
    protected final Function<String,V> parserV;

    protected Class<K> keyClass;
    protected Class<V> valClass;

    protected String keyComment,valueComment;

    protected boolean keyFixed = false;

    @SafeVarargs
    public ConfigMap(final @Nonnull ConfigCategory category,
                     final @Nonnull String configKey,
                     final @Nonnull Function<String,K> parserK,
                     final @Nonnull Function<String,V> parserV,
                     final @Nonnull ConfigEntry<K,V>... entries) {
        super(category, configKey, new ConfigurableLinkedHashMap<>());
        this.parserK = parserK;
        this.parserV = parserV;
        for(final @Nullable ConfigEntry<K,V> entry:entries){
            if(entry == null) continue;
            defaultValue.put(entry.getKey(),entry.getValue());
        }
    }

    @Nonnull
    public ConfigMap<K,V> setKeyClass(@Nonnull final Class<K> cls){
        this.keyClass = cls;
        return this;
    }

    @Nonnull
    public ConfigMap<K,V> setValueClass(@Nonnull final Class<V> cls){
        this.valClass = cls;
        return this;
    }

    @Nonnull
    public ConfigMap<K,V> setKeyComment(@Nonnull final String comment){
        this.keyComment = comment;
        return this;
    }

    @Nonnull
    public ConfigMap<K,V> setValueComment(@Nonnull final String comment){
        this.valueComment = comment;
        return this;
    }

    @Nonnull
    public ConfigMap<K,V> setKeyFixed(final boolean keyFixed) {
        this.keyFixed = keyFixed;
        return this;
    }

    public boolean isKeyFixed() {
        return keyFixed;
    }

    @Override
    public void save() {
        if(property == null) return;
        property.setValues(value.toStringList());
        property.setComment(getConstructedComment());
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.map";
    }

    @Override
    public void load(@Nonnull final Configuration config) {
        property = config.get(category.getPath(),key,defaultValue.toStringList(),getConstructedComment());
        load(property);
        if(keyFixed){
            defaultValue.forEach(value::putIfAbsent);
            value.keySet().retainAll(defaultValue.keySet());
        }
    }

    @Override
    protected void load(@Nonnull final Property property) {
        value = new ConfigurableLinkedHashMap<>();
        String[] strings = property.getStringList();
        StringBuilder pair = new StringBuilder();
        boolean insideQuote = false;
        boolean isBegin = true;
        for(int loc =0;loc<strings.length;loc++){
            String content = strings[loc];
            boolean ignore = false;
            int i,begin;
            for(i=0,begin=0;i<content.length();i++){
                int c = content.charAt(i);
                if(ignore){ //若在括号中，前面带有\的会省略
                    ignore = false;
                    continue;
                }
                if(insideQuote && c == '\\'){ //判断下一个是否需要省略
                    ignore = true;
                    continue;
                }
                if(Character.isWhitespace(c)) continue;
                if(c == ConfigurableLinkedHashMap.BEGIN_CHAR){
                    if(isBegin){
                        insideQuote = true;
                        isBegin = false;
                        continue;
                    }
                }else if(c == ConfigurableLinkedHashMap.END_CHAR){
                    if(insideQuote){
                        insideQuote = false;
                        continue;
                    }
                }

                isBegin = false;

                if(insideQuote) continue;

                if(c == ConfigurableLinkedHashMap.PAIR_END){ //不在Quote里，发现、则表示接下来是新的键值对
                    isBegin = true;
                    pair.append(content, begin, i);
                    begin = i+1;
                    loadKeyPair(pair.toString());

                    pair = new StringBuilder();
                }
            }
            if(begin > i) continue;
            pair.append(content,begin,i);
            if(pair.length() == 0) continue; //空的，不需要加\n
            if(loc < strings.length-1) pair.append('\n');
        }

        if(pair.toString().trim().isEmpty()) return;

        loadKeyPair(pair.toString());
    }

    protected void loadKeyPair(@Nonnull final String pair){
        String[] spilt;
        try {
            spilt = ConfigurableLinkedHashMap.spiltKeyPair(pair);
        }catch (Throwable t){
            GeoCraftAPI.LOGGER.warn("Loading configuration {} error: {} is not valid key-value pair",category,pair);
            GeoCraftAPI.LOGGER.warn("Error Detailed:",t);
            return;
        }

        if(spilt == null){
            GeoCraftAPI.LOGGER.warn("Loading configuration {} error: {} is not valid key-value pair",category,pair);
            return;
        }

        try{
            K k = parserK.apply(spilt[0]);
            V v = parserV.apply(spilt[1]);
            if(k == null || v == null) return;
            GeoCraftAPI.LOGGER.debug("Successfully loaded {} -> {} in config {}",k,v,getPath());
            this.value.put(k,v);
        }catch (Throwable e){
            GeoCraftAPI.LOGGER.warn("Loading configuration {} in {} error",pair,category);
            GeoCraftAPI.LOGGER.warn("Error Detailed:",e);
        }
    }

    @Nonnull
    @Override
    protected List<Pair<String, String>> getCommentProperties() {
        final List<Pair<String,String>> list = super.getCommentProperties();
        if(keyClass != null && valClass != null)
            list.add(Pair.of("类型 Type", "Map< " +
                    keyClass.getSimpleName() +
                    " -> " +
                    valClass.getSimpleName() +
                    " >"));
        if(keyComment != null) list.add(Pair.of("键说明 Key Info",keyComment));
        if(valueComment != null) list.add(Pair.of("值说明 Value Info",valueComment));
        return list;
    }


    //*****************
    // Map
    //*****************

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean containsKey(final @Nonnull Object key) {
        return value.containsKey(key);
    }

    @Override
    public boolean containsValue(final @Nonnull Object value) {
        return this.value.containsValue(value);
    }

    @Override
    public V get(final @Nonnull Object key) {
        return this.value.get(key);
    }

    @Override
    public V put(final @Nonnull K key,final @Nonnull V value) {
        if(keyFixed && !this.value.containsKey(key)) throw new UnsupportedOperationException();
        return this.value.put(key,value);
    }

    @Override
    public V remove(final @Nonnull Object key) {
        if(keyFixed) throw new UnsupportedOperationException();
        return this.value.remove(key);
    }

    @Override
    public void putAll(@Nonnull final Map<? extends K, ? extends V> m) {
        if(keyFixed) throw new UnsupportedOperationException();
        this.value.putAll(m);
    }

    @Override
    public void clear() {
        if(keyFixed) throw new UnsupportedOperationException();
        this.value.clear();
    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        return this.value.keySet();
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        return this.value.values();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.value.entrySet();
    }
}