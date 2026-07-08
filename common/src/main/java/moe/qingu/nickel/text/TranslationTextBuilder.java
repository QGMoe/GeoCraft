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

package moe.qingu.nickel.text;

import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author QGMoe
 */
public final class TranslationTextBuilder extends TextBuilder<TextComponentTranslation,TranslationTextBuilder> {

    private final @Nonnull String key;
    private final @Nonnull List<Object> objs = new ArrayList<>();

    public TranslationTextBuilder(final @Nonnull String translationKey) {
        this.key = translationKey;
    }

    @Nonnull
    public TranslationTextBuilder arg(final @Nonnull Object arg){
        if(arg instanceof TextBuilder<?,?>){
            this.objs.add(((TextBuilder<?, ?>) arg).done());
        }else this.objs.add(Objects.requireNonNull(arg));
        return this;
    }

    @Nonnull
    public TranslationTextBuilder arg(final @Nonnull Object... args){
        for (final Object arg : args) arg(arg);
        return this;
    }

    @Nonnull
    @Override
    protected TextComponentTranslation build() {
        return new TextComponentTranslation(key,objs.toArray());
    }

    @Nonnull
    @Override
    protected TranslationTextBuilder buildCopy() {
        final TranslationTextBuilder copy =  new TranslationTextBuilder(key);
        copy.objs.addAll(this.objs);
        return copy;
    }
}
