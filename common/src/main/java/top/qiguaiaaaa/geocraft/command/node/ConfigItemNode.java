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

package top.qiguaiaaaa.geocraft.command.node;

import moe.qingu.nickel.command.builder.parameter.FastParameterNodeBuilder;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.node.parameter.TokenizeParameterNode;
import moe.qingu.nickel.command.suggestor.DirectSuggestor;
import net.minecraft.command.CommandException;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.configs.ConfigurationLoader;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static moe.qingu.nickel.text.Texts.*;

/**
 * @author QGMoe
 */
public final class ConfigItemNode extends TokenizeParameterNode.Single<ConfigItem<?,?>> {
    private static final List<String> configItemPaths = ConfigurationLoader.getConfigItems().stream().map(ConfigItem::getPath).collect(Collectors.toList());

    public ConfigItemNode(@Nonnull final String name) {
        super(name);
        setSuggestProvider(DirectSuggestor.of(configItemPaths));
    }

    @Nonnull
    public static FastParameterNodeBuilder<ConfigItem<?,?>,ConfigItemNode> configItem(final @Nonnull String name){
        return new FastParameterNodeBuilder<>(name,ConfigItemNode::new);
    }

    @Override
    public boolean accepts(@Nonnull final String arg, @Nonnull final CommandContext context){
        return true;
    }

    @Override
    public ConfigItem<?, ?> parse(@Nonnull final String entry, @Nonnull final CommandContext context) throws CommandException {
        final @Nonnull ConfigItem<?,?> item = ConfigurationLoader.getConfigItems()
                .stream()
                .filter(t -> t.getPath().equals(entry))
                .findFirst()
                .orElseThrow(() -> new NickelCommandException(this.currentBranch)
                        .withSource(this)
                        .withAppendix(translation("geocraft.command.config_item_not_found",entry)));
        return item;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public Class<ConfigItem<?, ?>> getTypeClass() {
        return (Class<ConfigItem<?,?>>) (Class<?>) ConfigItem.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.geocraft.config_item";
    }

    @Override
    public String serialise(@Nonnull final ConfigItem<?, ?> item) {
        return item.getPath();
    }
}
