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

package moe.qingu.nickel.text.hover;

import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static moe.qingu.nickel.text.Texts.plain;

/**
 * @author QGMoe
 */
public interface HoverEventBuilder<S extends HoverEventBuilder<S>> {

    @Nonnull
    HoverEvent.Action getAction();

    @Nonnull
    HoverEvent build();

    final class ShowText implements HoverEventBuilder<ShowText>{
        private ITextComponent component;

        @Nonnull
        @Override
        public HoverEvent.Action getAction() {
            return HoverEvent.Action.SHOW_TEXT;
        }

        @Nonnull
        public ShowText then(final @Nonnull TextBuilder<?, ?> text){
            return then(text.done());
        }

        @Nonnull
        public ShowText then(final @Nonnull ITextComponent text){
            if(component == null) this.component = text;
            else this.component.appendSibling(text);
            return this;
        }

        @Nonnull
        @Override
        @Deprecated
        public HoverEvent build() {
            if(component == null) throw new IllegalStateException();
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT,component);
        }
    }

    final class ShowItem implements HoverEventBuilder<ShowItem>{
        private ItemStack stack;

        @Nonnull
        @Override
        public HoverEvent.Action getAction() {
            return HoverEvent.Action.SHOW_ITEM;
        }

        @Nonnull
        public HoverEventBuilder<?> of(@Nonnull final Item item){
            this.stack = new ItemStack(item,1);
            return this;
        }

        @Nonnull
        public HoverEventBuilder<?> of(@Nonnull final Item item,final int count){
            this.stack = new ItemStack(item,count);
            return this;
        }

        @Nonnull
        public HoverEventBuilder<?> of(@Nonnull final ItemStack stack){
            this.stack = stack;
            return this;
        }

        @Nonnull
        public HoverEventBuilder<?> of(@Nonnull final Block block){
            return of(Item.getItemFromBlock(block));
        }

        @Nonnull
        @Override
        @Deprecated
        public HoverEvent build() {
            if(stack == null) throw new IllegalStateException();
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT,plain(stack.writeToNBT(new NBTTagCompound()).toString()).done());
        }
    }

    final class ShowEntity implements HoverEventBuilder<ShowEntity>{
        private Entity entity;

        @Nonnull
        @Override
        public HoverEvent.Action getAction() {
            return HoverEvent.Action.SHOW_ENTITY;
        }

        @Nonnull
        public HoverEventBuilder<?> of(@Nonnull final Entity entity){
            this.entity = entity;
            return this;
        }

        /**
         * @see Entity#getHoverEvent()
         */
        @Nonnull
        @Override
        public HoverEvent build() {
            if(entity == null) throw new IllegalStateException();
            final NBTTagCompound nbt = new NBTTagCompound();
            final ResourceLocation id = EntityList.getKey(entity);

            nbt.setString("id", entity.getCachedUniqueIdString());
            if (id != null) nbt.setString("type", id.toString());
            nbt.setString("name", entity.getName());

            return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, plain(nbt.toString()).done());
        }
    }

    final class Inner<P>{
        private final @Nonnull HoverEvent.Action action;
        private final Function<HoverEvent,P> finalisation;

        public Inner(@Nonnull final HoverEvent.Action action,
                     @Nonnull final Function<HoverEvent,P> finalisation) {
            this.action = action;
            this.finalisation = finalisation;
        }

        @Nonnull
        public P content(final @Nonnull TextBuilder<?, ?> text){
            return finalisation.apply(new HoverEvent(action,text.done()));
        }

        @Nonnull
        public P content(final @Nonnull ITextComponent text){
            return finalisation.apply(new HoverEvent(action,text));
        }

        @Nonnull
        public P content(final @Nonnull String text){
            return finalisation.apply(new HoverEvent(action,plain(text).done()));
        }
    }
}
