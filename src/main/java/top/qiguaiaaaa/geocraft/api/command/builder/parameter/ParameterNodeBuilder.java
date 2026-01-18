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

import top.qiguaiaaaa.geocraft.api.command.builder.NoSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.parament.Decorator;
import top.qiguaiaaaa.geocraft.api.command.node.parament.ParameterNode;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 参数节点构建器
 * @author QiguaiAAAA
 * @param <P> 参数类型
 * @param <T> 参数类型对应的参数节点类型
 * @param <SELF> 自身类型，用于 {@link #smart()}
 */
public abstract class ParameterNodeBuilder<P, T extends ParameterNode<P>,SELF extends ParameterNodeBuilder<P,T,SELF>> extends NoSplitNodeBuilder<T,SELF> {
    public static final BiFunction<List<String>,SuggestContext,List<String>> USE_DEFAULT_SUGGESTOR = (strings, context) -> null;
    public static final ParameterNode.DefaultParser<?> USE_DEFAULT_PARSER = (node,context) -> null;
    @SuppressWarnings("deprecation")
    protected static final BiConsumer<ParameterNodeBuilder<?,?,?>,SmartSplitNodeBuilder.Inner<ParameterNodeBuilder<?,?,?>>> ON_SMART_DONE =
            (self,smart) -> self.bakedChildNode = smart.build();
    protected final String name;
    protected String langKey;
    protected boolean optional;
    @SuppressWarnings("unchecked")
    protected ParameterNode.DefaultParser<P> parser = (ParameterNode.DefaultParser<P>) USE_DEFAULT_PARSER;
    protected BiFunction<List<String>, SuggestContext, List<String>> suggestProvider = USE_DEFAULT_SUGGESTOR;
    protected Decorator<P> decorator;

    public ParameterNodeBuilder(@Nonnull final String name) {
        this.name = name;
    }

    public ParameterNodeBuilder(@Nonnull final String parentName,@Nonnull final String childName){
        this(ParameterNode.getInnerParameterName(parentName, childName));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF asOptional() {
        optional = true;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF defaultAs(@Nonnull final ParameterNode.DefaultParser<P> parser) {
        this.parser = parser;
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF defaultAs(@Nonnull final P defaultValue){
        this.parser = (node, context) -> defaultValue;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF suggest(final BiFunction<List<String>, SuggestContext, List<String>> suggestProvider) {
        this.suggestProvider = suggestProvider;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF suggest(final Function<SuggestContext, List<String>> suggestProvider) {
        this.suggestProvider = (args,context) -> suggestProvider.apply(context);
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF suggest(final Supplier<List<String>> suggestProvider) {
        this.suggestProvider = (args,context) -> suggestProvider.get();
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public SELF suggest(final List<String> suggests) {
        this.suggestProvider = (args,context) -> suggests;
        return (SELF) this;
    }

    @Nonnull
    public SELF suggest(final String... suggests){
        return suggest(Arrays.asList(suggests));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF translate(@Nonnull final String key){
        this.langKey = key;
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF clearDecorators(){
        this.decorator = null;
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF decorate(@Nonnull final Decorator<P> decorator){
        if(this.decorator == null) this.decorator = decorator;
        else this.decorator = this.decorator.andThen(decorator);
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SELF decorate(@Nonnull final Decorator.Simple<P> decorator){
        if(this.decorator == null) this.decorator = decorator.toFull();
        else this.decorator = this.decorator.andThen(decorator);
        return (SELF) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SmartSplitNodeBuilder.Inner<SELF> smart(){
        ensureFirstChild();
        return new SmartSplitNodeBuilder.Inner<>((SELF) this,(BiConsumer<SELF, SmartSplitNodeBuilder.Inner<SELF>>) (BiConsumer<?,?>) ON_SMART_DONE);
    }

    @Nonnull
    @Override
    public T build() {
        final T instance = buildInstance();
        instance.setChildNode(buildChildNode());
        if(parser != USE_DEFAULT_PARSER){
            instance.setDefaultParser(parser);
        }
        instance.setDecorator(decorator);
        instance.setOptional(optional);
        if(suggestProvider != USE_DEFAULT_SUGGESTOR){
            instance.setSuggestProvider(suggestProvider);
        }
        if(langKey != null){
            instance.setTranslationKey(langKey);
        }
        return instance;
    }

    @Nonnull
    protected abstract T buildInstance();
}
