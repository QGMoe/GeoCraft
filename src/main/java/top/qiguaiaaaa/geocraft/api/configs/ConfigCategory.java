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

package top.qiguaiaaaa.geocraft.api.configs;

import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author QiguaiAAAA
 */
public final class ConfigCategory {
    public final String name;
    public final @Nullable ConfigCategory parent;
    private final HashMap<String,ConfigCategory> children = new HashMap<>();
    private @Nullable String comment;

    public ConfigCategory(final @Nonnull String name) {
        this(null,name);
    }

    public ConfigCategory(final @Nullable ConfigCategory parent, final @Nonnull String name){
        this.parent = parent;
        this.name = name;
    }

    @Nonnull
    public ConfigCategory setComment(final @Nullable String comment) {
        this.comment = comment;
        return this;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @Nonnull
    public ConfigCategory getChildCategory(final @Nonnull String name){
        return children.computeIfAbsent(name,k -> new ConfigCategory(this,k));
    }

    @Nonnull
    public Collection<ConfigCategory> getChildren() {
        return children.values();
    }

    @Nonnull
    public String getPath(){
        if(parent == null) return name;
        return parent.getPath()+ Configuration.CATEGORY_SPLITTER + name;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
