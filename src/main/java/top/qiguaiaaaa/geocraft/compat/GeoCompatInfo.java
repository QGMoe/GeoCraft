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

package top.qiguaiaaaa.geocraft.compat;

import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author QiguaiAAAA
 */
public final class GeoCompatInfo {
    private static final BooleanSupplier ENABLED = () -> true;
    public final @Nonnull String modid;
    public final @Nullable String compatClass;
    public final @Nonnull Consumer<List<String>> getMixins;
    private BooleanSupplier isEnabled = ENABLED;

    public GeoCompatInfo(@Nonnull final String modid,
                         @Nullable final String compatClass,
                         @Nonnull final Consumer<List<String>> getMixins) {
        this.modid = modid;
        this.compatClass = compatClass;
        this.getMixins = getMixins;
    }

    public GeoCompatInfo(@Nonnull final String modid,
                         @Nullable final String compatClass,
                         @Nonnull final String mixin) {
        this(modid,compatClass,mixinList -> mixinList.add(mixin));
    }

    public GeoCompatInfo(@Nonnull final String modid,
                         @Nullable final String compatClass,
                         @Nonnull final String... mixins) {
        this(modid,compatClass,mixinList -> mixinList.addAll(Arrays.asList(mixins)));
    }

    public @Nonnull GeoCompatInfo enableIf(final @Nonnull BooleanSupplier enableCondition){
        isEnabled = Objects.requireNonNull(enableCondition);
        return this;
    }

    public @Nonnull BooleanSupplier getEnableCondition(){
        return this.isEnabled;
    }

    public boolean isValid(){
        return Loader.isModLoaded(this.modid) && isEnabled.getAsBoolean();
    }

    @Override
    public int hashCode() {
        return modid.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if(!(obj instanceof GeoCompatInfo)) return false;
        final @Nonnull GeoCompatInfo info = (GeoCompatInfo) obj;
        return this.modid.equals(info.modid) &&
                Objects.equals(this.compatClass, info.compatClass) &&
                getMixins == info.getMixins;
    }
}
