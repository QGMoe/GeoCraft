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

package moe.qingu.nickel.command.builder.literal;

import moe.qingu.nickel.command.builder.INodeBuilder;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.functional.PermitNode;
import moe.qingu.nickel.command.node.literal.LiteralsNode;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author QiguaiAAAA
 */
public class LiteralsNodeBuilder implements INodeBuilder<LiteralsNode> {
    private final Map<String, Object> literal2NodeMap = new LinkedHashMap<>();
    private ICommandNode bakedDefaultChild = null;
    private INodeBuilder<?> defaultChild = null;
    private boolean optional = false;
    private Predicate<CommandContext> funcCheckPermission = PermitNode.PERMIT_ALL;

    @Nonnull
    public LiteralsNodeBuilder asOptional() {
        this.optional = true;
        return this;
    }

    @Nonnull
    public LiteralsNodeBuilder defaultThen(@Nonnull final ICommandNode node) {
        bakedDefaultChild = node;
        return this;
    }

    @Nonnull
    public LiteralsNodeBuilder defaultThen(@Nonnull final INodeBuilder<?> nodeBuilder) {
        defaultChild = nodeBuilder;
        return this;
    }

    @Nonnull
    public LiteralsNodeBuilder defaultAs(@Nonnull final String name) {
        final Object node = literal2NodeMap.get(name);
        if (node instanceof ICommandNode) bakedDefaultChild = (ICommandNode) node;
        else if (node instanceof INodeBuilder<?>) defaultChild = (INodeBuilder<?>) node;
        else throw new IllegalArgumentException(name);
        return this;
    }

    @Nonnull
    public LiteralsChoiceInnerBuilder when(@Nonnull final String literal){
        return new LiteralsChoiceInnerBuilder(literal);
    }

    @Nonnull
    public LiteralsChoiceInnerBuilder when(@Nonnull final String... literals){
        return new LiteralsChoiceInnerBuilder(literals);
    }

    @Nonnull
    public LiteralsNodeBuilder permitIf(@Nonnull final Predicate<CommandContext> funcCheckPermission) {
        this.funcCheckPermission = funcCheckPermission;
        return this;
    }

    @Nonnull
    @Override
    public LiteralsNode build() {
        final LiteralsNode node = new LiteralsNode();
        node.setChecker(funcCheckPermission);
        literal2NodeMap.forEach((l, n) -> {
            if (n instanceof ICommandNode) node.addLiteral(l, (ICommandNode) n);
            else if (n instanceof INodeBuilder<?>) node.addLiteral(l, ((INodeBuilder<?>) n).build());
            else throw new IllegalArgumentException(l);
        });
        node.setOptional(optional);
        if(optional){
            node.setChildNode(bakedDefaultChild != null ? bakedDefaultChild : defaultChild.build());
        }
        return node;
    }

    public class LiteralsChoiceInnerBuilder{
        protected final String[] names;

        LiteralsChoiceInnerBuilder(@Nonnull final String... literals){
            this.names = Objects.requireNonNull(literals);
        }

        @Nonnull
        public LiteralsNodeBuilder then(@Nonnull final INodeBuilder<?> nodeBuilder){
            Arrays.stream(names).forEach(name -> literal2NodeMap.put(name,nodeBuilder));
            return LiteralsNodeBuilder.this;
        }

        @Nonnull
        public LiteralsNodeBuilder then(@Nonnull final ICommandNode node) {
            Arrays.stream(names).forEach(name -> literal2NodeMap.put(name,node));
            return LiteralsNodeBuilder.this;
        }
    }
}
