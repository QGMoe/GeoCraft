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

package top.qiguaiaaaa.geocraft.api.command.builder.literal;

import top.qiguaiaaaa.geocraft.api.command.builder.CommandBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.INodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;
import top.qiguaiaaaa.geocraft.api.command.node.LiteralNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author QiguaiAAAA
 */
public class LiteralNodeBuilder implements INodeBuilder<LiteralNode> {

    private Function<CommandContext, Boolean> funcCheckPermission = CommandBuilder.PERMIT_ALL;

    private INodeBuilder<?> childNode;
    private ICommandNode bakedChildNode;
    protected @Nullable BiPredicate<List<String>, CommandContext> matchChecker;
    private final @Nonnull String name;

    public LiteralNodeBuilder(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public LiteralNodeBuilder matchIf(@Nullable BiPredicate<List<String>, CommandContext> matcher) {
        this.matchChecker = matcher;
        return this;
    }

    @Nonnull
    public LiteralNodeBuilder then(@Nonnull INodeBuilder<?> childNode) {
        this.childNode = childNode;
        return this;
    }

    @Nonnull
    public LiteralNodeBuilder then(@Nonnull ICommandNode childNode) {
        this.bakedChildNode = childNode;
        return this;
    }

    @Nonnull
    public LiteralNodeBuilder passIf(@Nonnull Function<CommandContext, Boolean> funcCheckPermission) {
        this.funcCheckPermission = funcCheckPermission;
        return this;
    }

    @Nonnull
    @Override
    public LiteralNode build() {
        final LiteralNode node = new LiteralNode(name);
        node.setChecker(funcCheckPermission);
        node.setChildNode(bakedChildNode != null ? bakedChildNode : childNode.build());
        node.setMatcher(matchChecker);
        return node;
    }
}
