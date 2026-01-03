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

package top.qiguaiaaaa.geocraft.api.command.builder.functional;

import top.qiguaiaaaa.geocraft.api.command.builder.INodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.CommandRunFunction;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.ExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.LiteralNode;
import top.qiguaiaaaa.geocraft.api.command.node.SmartSplitNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author QiguaiAAAA
 */
public class SmartSplitNodeBuilder implements INodeBuilder<SmartSplitNode> {
    protected List<SmartNodeBuilder<?>> smarts = new ArrayList<>();
    protected ICommandNode bakedDefault;
    protected INodeBuilder<?> defaultBuilder;

    @Nonnull
    public <T extends ISmartNode> SmartNodeBuilder<T> then(@Nonnull final INodeBuilder<T> builder){
        final SmartNodeBuilder<T> b = new SmartNodeBuilder<>(Objects.requireNonNull(builder));
        smarts.add(b);
        return b;
    }

    @Nonnull
    public SmartLiteralNodeBuilder literal(@Nonnull final String val){
        final SmartLiteralNodeBuilder b = new SmartLiteralNodeBuilder(Objects.requireNonNull(val));
        smarts.add(b);
        return b;
    }

    @Nonnull
    public SmartSplitNodeBuilder execute(@Nonnull final CommandRunFunction func){
        final ExecuteNodeBuilder builder = new ExecuteNodeBuilder();
        builder.run(Objects.requireNonNull(func));
        defaultBuilder = builder;
        return this;
    }

    @Nonnull
    public SmartSplitNodeBuilder defaultAs(@Nonnull final ICommandNode node){
        bakedDefault = node;
        return this;
    }

    @Nonnull
    public SmartSplitNodeBuilder defaultAs(@Nonnull final INodeBuilder<?> node){
        defaultBuilder = node;
        return this;
    }

    @Nonnull
    @Override
    public SmartSplitNode build() {
        final SmartSplitNode node = new SmartSplitNode();
        smarts.forEach(builder -> node.addSmartNode(builder.build()));
        if(bakedDefault != null) node.setChildNode(bakedDefault);
        else if(defaultBuilder != null) node.setChildNode(defaultBuilder.build());
        return node;
    }

    public class SmartNodeBuilder<T extends ISmartNode> implements INodeBuilder<T>{
        final INodeBuilder<? extends T> child;
        final T bakedChild;

        BiPredicate<List<String>, CommandContext> checker;

        public SmartNodeBuilder(@Nonnull final INodeBuilder<? extends T> nodeBuilder){
            this.child = nodeBuilder;
            this.bakedChild = null;
        }

        public SmartNodeBuilder(@Nonnull final T bakedChild){
            this.child = null;
            this.bakedChild = bakedChild;
        }

        @Nonnull
        public SmartNodeBuilder<T> chooseIf(@Nonnull final BiPredicate<List<String>,CommandContext> checker){
            this.checker = checker;
            return this;
        }

        @Nonnull
        public SmartSplitNodeBuilder done(){
            return SmartSplitNodeBuilder.this;
        }

        @Nonnull
        @Override
        @Deprecated
        public T build() {
            final T bakedNode = bakedChild==null?Objects.requireNonNull(child).build():bakedChild;
            if(checker != null){
                bakedNode.setMatcher(checker);
            }
            return bakedNode;
        }
    }

    public class SmartLiteralNodeBuilder extends SmartNodeBuilder<LiteralNode>{

        protected final LiteralNodeBuilder literalBuilder;

        public SmartLiteralNodeBuilder(@Nonnull final String name) {
            super(new LiteralNodeBuilder(name));
            literalBuilder = (LiteralNodeBuilder) defaultBuilder;
        }

        @Nonnull
        public SmartLiteralNodeBuilder then(@Nonnull INodeBuilder<?> childNode) {
            literalBuilder.then(childNode);
            return this;
        }

        @Nonnull
        public SmartLiteralNodeBuilder then(@Nonnull ICommandNode childNode) {
            literalBuilder.then(childNode);
            return this;
        }

        @Nonnull
        public SmartLiteralNodeBuilder passIf(@Nonnull Function<CommandContext, Boolean> funcCheckPermission) {
            literalBuilder.passIf(funcCheckPermission);
            return this;
        }

        @Nonnull
        @Override
        public SmartLiteralNodeBuilder chooseIf(@Nonnull BiPredicate<List<String>, CommandContext> checker) {
            return (SmartLiteralNodeBuilder) super.chooseIf(checker);
        }
    }
}
