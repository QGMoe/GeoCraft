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

package top.qiguaiaaaa.geocraft.api.command.node.minecraft;

import net.minecraft.command.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.forge.ForgeRegistryEntryNode;
import top.qiguaiaaaa.geocraft.api.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class ItemSelectorNode extends ForgeRegistryEntryNode<Item> {

    public static final DefaultParser<Item> DEFAULT_PARSER = (node, context) -> Items.AIR;
    public static final BiFunction<List<String>,SuggestContext,List<String>> DEFAULT_SUGGESTOR = createSuggestProviderFromRegistry(Item.REGISTRY);
    public ItemSelectorNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Override
    public <T extends List<String> & Deque<String>> Item parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        return CommandBase.getItemByText(context.getSender(),args.getFirst());
    }

    @Nonnull
    @Override
    public Class<Item> getType() {
        return Item.class;
    }

    @Nonnull
    @Override
    public String getLocalizedType() {
        return "api.geo.command.parameter.minecraft.item";
    }
}
