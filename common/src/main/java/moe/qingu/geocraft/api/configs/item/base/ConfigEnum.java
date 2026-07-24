/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.geocraft.api.configs.item.base;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import moe.qingu.geocraft.api.configs.ConfigCategory;
import moe.qingu.geocraft.api.configs.item.ConfigItem;
import moe.qingu.geocraft.api.util.annotation.EarlyLoaded;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author QGMoe
 */
@EarlyLoaded
public class ConfigEnum<T extends Enum<T>> extends ConfigItem<T,ConfigEnum<T>> {
    protected final Class<T> cls;
    protected Int2ObjectOpenHashMap<ObjectArraySet<String>> alias;
    protected boolean ignoredCase = true;

    /**
     * 创建一个配置项
     *
     * @param category     配置所在目录
     * @param configKey    配置的key
     * @param defaultValue 配置的默认值，不应为null，因为会调用{@link Object#toString()}
     * @param cls 枚举的类
     */
    public ConfigEnum(@Nonnull final ConfigCategory category, @Nonnull final String configKey, @Nonnull final T defaultValue,@Nonnull final Class<T> cls) {
        super(category, configKey, defaultValue);
        this.cls = cls;
        if(!cls.isEnum()) throw new IllegalArgumentException(cls.getName());
    }

    @Nonnull
    public ConfigEnum<T> ignoredCase(final boolean ignored){
        this.ignoredCase = ignored;
        return this;
    }

    @Nonnull
    public ConfigEnum<T> withAlias(final @Nonnull T t,final @Nonnull String... aliases){
        if(alias == null) alias = new Int2ObjectOpenHashMap<>();
        if(!alias.containsKey(t.ordinal())) alias.put(t.ordinal(),new ObjectArraySet<>());
        final ObjectArraySet<String> set = alias.get(t.ordinal());
        set.addAll(Arrays.asList(aliases));
        return this;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.enum";
    }

    @Override
    protected void load(@Nonnull final Property property) {
        final T[] values = getEnumConstants(cls);
        final String s = property.getString();
        for(final T t:values)
            if(ignoredCase && s.equalsIgnoreCase(t.name()) || !ignoredCase && s.equals(t.name())){
                this.value = t;
                return;
            }
        if(alias == null) return;
        for(final T t:values){
            final ObjectArraySet<String> set = alias.get(t.ordinal());
            if(set == null) continue;
            for(final String a:set) if(ignoredCase && a.equalsIgnoreCase(s) || !ignoredCase && a.equals(s)){
                this.value = t;
                return;
            }
        }
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    protected static <T extends Enum<T>> T[] getEnumConstants(final @Nonnull Class<T> cls){
        final List<T> list = new ArrayList<>();
        for (final Field field: cls.getDeclaredFields()){
            if(!field.isEnumConstant()) continue;
            try {
                list.add((T) field.get(null));
            } catch (final IllegalAccessException | ClassCastException impossible) {
                throw new RuntimeException(impossible);
            }
        }
        list.sort(Comparator.comparingInt(Enum::ordinal));
        return list.toArray((T[]) Array.newInstance(cls,list.size()));
    }
}
