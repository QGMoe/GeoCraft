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
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public final class Texts {
    private Texts(){}

    @Nonnull
    public static PlainTextBuilder plain(final @Nonnull String text){
        return new PlainTextBuilder(text);
    }

    @Nonnull
    public static TranslationTextBuilder translation(final @Nonnull String key){
        return new TranslationTextBuilder(key);
    }

    public final static class Shows{

        @Nonnull
        public static PlainTextBuilder block(final @Nonnull Block block){
            return plain(block.getLocalizedName()).hoverTo(Hovers.block(block));
        }

        @Nonnull
        public static TranslationTextBuilder item(final @Nonnull Item item){
            return translation(item.getTranslationKey()).hoverTo(Hovers.item(item));
        }

        @Nonnull
        public static TranslationTextBuilder itemStack(final @Nonnull ItemStack stack){
            return translation(stack.getTranslationKey()).hoverTo(Hovers.itemStack(stack));
        }

        @Nonnull
        public static PlainTextBuilder entity(final @Nonnull Entity entity){
            return plain(entity.getName()).hoverTo(Hovers.entity(entity));
        }
    }

    public final static class Hovers{
        private Hovers(){}

        @Nonnull
        public static HoverEventBuilder.ShowItem item(){
            return new HoverEventBuilder.ShowItem();
        }

        @Nonnull
        public static HoverEventBuilder<?> item(final @Nonnull Item item){
            return new HoverEventBuilder.ShowItem().of(item);
        }

        @Nonnull
        public static HoverEventBuilder<?> item(final @Nonnull Item item,final int count){
            return new HoverEventBuilder.ShowItem().of(item,count);
        }

        @Nonnull
        public static HoverEventBuilder<?> itemStack(final @Nonnull ItemStack stack){
            return new HoverEventBuilder.ShowItem().of(stack);
        }

        @Nonnull
        public static HoverEventBuilder<?> block(final @Nonnull Block block){
            return new HoverEventBuilder.ShowItem().of(block);
        }

        @Nonnull
        public static HoverEventBuilder.ShowText text(){
            return new HoverEventBuilder.ShowText();
        }

        @Nonnull
        public static HoverEventBuilder.ShowText text(final @Nonnull TextBuilder<?,?> builder){
            return new HoverEventBuilder.ShowText().then(builder);
        }

        @Nonnull
        public static HoverEventBuilder.ShowEntity entity(){
            return new HoverEventBuilder.ShowEntity();
        }

        @Nonnull
        public static HoverEventBuilder<?> entity(final @Nonnull Entity entity){
            return new HoverEventBuilder.ShowEntity().of(entity);
        }
    }
}
