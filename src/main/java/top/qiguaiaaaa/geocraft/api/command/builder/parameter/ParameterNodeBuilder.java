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

package top.qiguaiaaaa.geocraft.api.command.builder.parameter;

import top.qiguaiaaaa.geocraft.api.command.builder.INodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;
import top.qiguaiaaaa.geocraft.api.command.node.generic.ParameterNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 参数节点构建器
 * @author QiguaiAAAA
 * @param <P> 参数类型
 * @param <T> 参数类型对应的参数节点类型
 * @param <SELF> 自身类型，用于 {@link #smart()}
 */
public abstract class ParameterNodeBuilder<P, T extends ParameterNode<P>,SELF extends ParameterNodeBuilder<P,T,SELF>> implements INodeBuilder<T> {
    protected final String name;
    protected INodeBuilder<?> childNode;
    protected ICommandNode bakedChildNode;
    protected SmartSplitNodeBuilder.Inner<SELF> smartNode;
    protected boolean optional;
    protected ParameterNode.DefaultParser<P> parser;
    protected BiFunction<List<String>, SuggestContext, List<String>> suggestProvider;

    public ParameterNodeBuilder(@Nonnull String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF asOptional() {
        optional = true;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF defaultAs(@Nullable ParameterNode.DefaultParser<P> parser) {
        this.parser = parser;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF then(@Nonnull INodeBuilder<?> childNode) {
        this.childNode = childNode;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF then(@Nonnull ICommandNode childNode) {
        this.bakedChildNode = childNode;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF suggest(BiFunction<List<String>, SuggestContext, List<String>> suggestProvider) {
        this.suggestProvider = suggestProvider;
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SmartSplitNodeBuilder.Inner<SELF> smart(){
        return smartNode = new SmartSplitNodeBuilder.Inner<>((SELF) this);
    }

    @Nonnull
    @Override
    public T build() {
        final T instance = buildInstance();
        if(smartNode == null) instance.setChildNode(bakedChildNode != null ? bakedChildNode : childNode.build());
        else instance.setChildNode(smartNode.build());
        instance.setDefaultParser(parser);
        instance.setOptional(optional);
        instance.setSuggestProvider(suggestProvider);
        return instance;
    }

    @Nonnull
    protected abstract T buildInstance();
}
