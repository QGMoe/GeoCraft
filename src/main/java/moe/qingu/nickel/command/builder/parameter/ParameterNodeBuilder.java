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

package moe.qingu.nickel.command.builder.parameter;

import moe.qingu.nickel.command.builder.NoSplitNodeBuilder;
import moe.qingu.nickel.command.builder.functional.SmartSplitNodeBuilder;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.parameter.Decorator;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import moe.qingu.nickel.command.suggestor.DirectSuggestor;
import moe.qingu.nickel.command.suggestor.SerialiseSuggestor;
import moe.qingu.nickel.command.suggestor.Suggestor;
import moe.qingu.nickel.command.utils.Matcher;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * 参数节点构建器
 * @author QiguaiAAAA
 * @param <P> 参数类型
 * @param <T> 参数类型对应的参数节点类型
 * @param <S> 自身类型，用于 {@link #smart()}
 */
public abstract class ParameterNodeBuilder<P, T extends ParameterNode<P>, S extends ParameterNodeBuilder<P,T, S>> extends NoSplitNodeBuilder<T, S> {
    public static final Suggestor<?> USE_DEFAULT_SUGGESTOR = DirectSuggestor.of("");
    public static final ParameterNode.DefaultParser<?> USE_DEFAULT_PARSER = (node,context) -> null;
    @SuppressWarnings("deprecation")
    protected static final BiConsumer<ParameterNodeBuilder<?,?,?>,SmartSplitNodeBuilder.Inner<ParameterNodeBuilder<?,?,?>>> ON_SMART_DONE =
            (self,smart) -> self.bakedChildNode = smart.build();
    protected final String name;
    protected String langKey;
    protected String commentKey;
    protected boolean optional;
    @SuppressWarnings("unchecked")
    protected ParameterNode.DefaultParser<P> parser = (ParameterNode.DefaultParser<P>) USE_DEFAULT_PARSER;
    @SuppressWarnings("unchecked")
    protected Suggestor<P> suggestProvider = (Suggestor<P>) USE_DEFAULT_SUGGESTOR;
    protected Decorator<P> decorator;
    protected Matcher matcher;

    public ParameterNodeBuilder(@Nonnull final String name) {
        this.name = name;
    }

    public ParameterNodeBuilder(@Nonnull final String parentName,@Nonnull final String childName){
        this(ParameterNode.getInnerParameterName(parentName, childName));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public S asOptional() {
        optional = true;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S asNecessary(){
        optional = false;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public S defaultAs(@Nonnull final ParameterNode.DefaultParser<P> parser) {
        this.parser = parser;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S defaultAs(@Nonnull final P defaultValue){
        this.parser = (node, context) -> defaultValue;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public S suggest(final Suggestor<P> suggestProvider) {
        this.suggestProvider = suggestProvider;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public S suggest(final SerialiseSuggestor<P> suggestProvider) {
        this.suggestProvider = suggestProvider;
        return (S) this;
    }

    @Nonnull
    public S suggest(final Function<SuggestContext, Stream<P>> suggestProvider) {
        return suggest((input,ctx)->suggestProvider.apply(ctx));
    }

    @Nonnull
    public S suggest(final Supplier<Stream<P>> suggestProvider) {
        return suggest((input,ctx)->suggestProvider.get());
    }

    @Nonnull
    public S suggest(final Stream<P> suggests) {
        return suggest(SerialiseSuggestor.of(suggests));
    }

    @Nonnull
    public S suggest(final Iterable<P> suggests) {
        return suggest(SerialiseSuggestor.of(suggests));
    }

    @Nonnull
    public S suggest(final Iterator<P> suggests) {
        return suggest(SerialiseSuggestor.of(suggests));
    }

    @SafeVarargs
    @Nonnull
    public final S suggest(final P... suggests) {
        return suggest(SerialiseSuggestor.of(suggests));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public S suggestRaw(final DirectSuggestor<P> suggestProvider) {
        this.suggestProvider = suggestProvider;
        return (S) this;
    }

    @Nonnull
    public S suggestRaw(final Function<SuggestContext, Stream<String>> suggestProvider) {
        return suggestRaw((input,ctx)->suggestProvider.apply(ctx));
    }

    @Nonnull
    public S suggestRaw(final Supplier<Stream<String>> suggestProvider) {
        return suggestRaw((input,ctx)->suggestProvider.get());
    }

    @Nonnull
    public S suggestRaw(final Stream<String> suggests) {
        return suggestRaw(DirectSuggestor.of(suggests));
    }

    @Nonnull
    public S suggestRaw(final Iterable<String> suggests) {
        return suggestRaw(DirectSuggestor.of(suggests));
    }

    @Nonnull
    public S suggestRaw(final Iterator<String> suggests) {
        return suggestRaw(DirectSuggestor.of(suggests));
    }

    @Nonnull
    public S suggestRaw(final String... suggests){
        return suggest(DirectSuggestor.of(suggests));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S translate(@Nonnull final String key){
        this.langKey = key;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S comment(@Nonnull final String key){
        this.commentKey = key;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S clearDecorators(){
        this.decorator = null;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S decorate(@Nonnull final Decorator<P> decorator){
        if(this.decorator == null) this.decorator = decorator;
        else this.decorator = this.decorator.andThen(decorator);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S decorate(@Nonnull final Decorator.Simple<P> decorator){
        if(this.decorator == null) this.decorator = decorator.toFull();
        else this.decorator = this.decorator.andThen(decorator);
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S matchIf(@Nonnull final Matcher matcher){
        this.matcher = matcher;
        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SmartSplitNodeBuilder.Inner<S> smart(){
        return new SmartSplitNodeBuilder.Inner<>((S) this,(BiConsumer<S, SmartSplitNodeBuilder.Inner<S>>) (BiConsumer<?,?>) ON_SMART_DONE);
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
        if(commentKey != null){
            instance.setComment(commentKey);
        }
        return instance;
    }

    @Nonnull
    protected abstract T buildInstance();
}
