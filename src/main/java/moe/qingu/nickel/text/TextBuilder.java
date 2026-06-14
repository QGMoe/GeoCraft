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

import moe.qingu.nickel.text.hover.HoverEventBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.nickel.text.Texts.plain;

/**
 * @author QGMoe
 */
public abstract class TextBuilder<T extends ITextComponent,S extends TextBuilder<T,S>> {
    protected final @Nonnull Style style = new Style();
    protected @Nullable TextBuilder<?,?> then;

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S color(@Nonnull final TextFormatting color){
        if(!color.isColor()) throw new IllegalArgumentException();

        this.style.setColor(color);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S bold(final boolean yes){
        this.style.setBold(yes);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S italic(final boolean yes){
        this.style.setItalic(yes);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S underlined(final boolean yes){
        this.style.setUnderlined(yes);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S obfuscated(final boolean yes){
        this.style.setObfuscated(yes);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S strikethrough(final boolean yes){
        this.style.setStrikethrough(yes);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S insert(@Nonnull final String insertion){
        this.style.setInsertion(insertion);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final HoverEventBuilder.Inner<S> hoverTo(@Nonnull final HoverEvent.Action action){
        return new HoverEventBuilder.Inner<>(action,event -> {
            TextBuilder.this.style.setHoverEvent(event);
            return (S) TextBuilder.this;
        });
    }

    @Nonnull
    public final S hoverTo(@Nullable final HoverEventBuilder<?> builder){
        return hoverTo(builder==null?null:builder.build());
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S hoverTo(@Nullable final HoverEvent event){
        this.style.setHoverEvent(event);
        return (S) this;
    }

    @Nonnull
    public final ClickEventBuilder clickTo(@Nonnull final ClickEvent.Action action){
        return new ClickEventBuilder(action);
    }

    @Nonnull
    public final S then(@Nonnull final String text){
        return then(plain(text));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public final S then(@Nonnull final TextBuilder<?,?> builder){
        if(then != null) then.then(builder);
        else then = builder;
        return (S) this;
    }

    @Nonnull
    public final T done(){
        final @Nonnull T t = build();
        t.setStyle(this.style);
        if(then != null) t.appendSibling(then.done());
        return t;
    }

    @Nonnull
    protected abstract T build();

    public final class ClickEventBuilder{
        private final @Nonnull ClickEvent.Action action;

        public ClickEventBuilder(@Nonnull final ClickEvent.Action action) {
            this.action = action;
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        public S then(final @Nonnull String value){
            TextBuilder.this.style.setClickEvent(new ClickEvent(action,value));
            return (S) TextBuilder.this;
        }
    }
}
