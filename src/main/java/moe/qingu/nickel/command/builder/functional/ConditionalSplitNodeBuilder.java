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

package moe.qingu.nickel.command.builder.functional;

import moe.qingu.nickel.command.builder.INodeBuilder;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.node.functional.ConditionalSplitNode;
import moe.qingu.nickel.command.node.ICommandNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class ConditionalSplitNodeBuilder implements INodeBuilder<ConditionalSplitNode> {
    protected List<ConditionBuilder> conditions = new ArrayList<>();

    @Nonnull
    public ConditionBuilder as(@Nonnull BiPredicate<CommandContext, List<String>> condition) {
        final ConditionBuilder builder = new ConditionBuilder(condition);
        conditions.add(builder);
        return builder;
    }

    @Nonnull
    @Override
    public ConditionalSplitNode build() {
        final ConditionalSplitNode node = new ConditionalSplitNode();
        conditions.forEach(builder -> node.addCondition(builder.predicate, builder.bakedChild != null ? builder.bakedChild : builder.child.build()));
        return node;
    }

    public class ConditionBuilder {
        final BiPredicate<CommandContext, List<String>> predicate;
        INodeBuilder<?> child;
        ICommandNode bakedChild;

        public ConditionBuilder(@Nonnull BiPredicate<CommandContext, List<String>> predicate) {
            this.predicate = predicate;
        }

        @Nonnull
        public ConditionalSplitNodeBuilder then(@Nonnull INodeBuilder<?> node) {
            child = node;
            return ConditionalSplitNodeBuilder.this;
        }

        @Nonnull
        public ConditionalSplitNodeBuilder then(@Nonnull ICommandNode node) {
            bakedChild = node;
            return ConditionalSplitNodeBuilder.this;
        }
    }
}
