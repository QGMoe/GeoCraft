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

package moe.qingu.nickel.command.suggestor;

import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.parameter.ParameterNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author QGMoe
 */
@FunctionalInterface
public
interface SerialiseSuggestor<P> extends Suggestor<P> {

    @Nonnull
    static <P> SerialiseSuggestor<P> of(final @Nonnull SerialiseSuggestor<P> provider) {
        return provider;
    }

    @Nonnull
    static <P> Static<P> of(final @Nonnull List<P> data) {
        return new Static<>(data);
    }

    @Nonnull
    static <P> Static<P> of(final @Nonnull Stream<P> data) {
        return new Static<>(data.collect(Collectors.toList()));
    }

    @Nonnull
    @SafeVarargs
    static <P> Static<P> of(final @Nonnull P... data) {
        return of(Arrays.asList(data));
    }

    @Nonnull
    static <P> Static<P> of(final @Nonnull Iterable<P> data) {
        return of(StreamSupport.stream(data.spliterator(), false));
    }

    @Nonnull
    static <P> Static<P> of(final @Nonnull Iterator<P> data) {
        return of(StreamSupport.stream(Spliterators.spliteratorUnknownSize(data, Spliterator.ORDERED), false));
    }

    @Nonnull
    @Override
    default List<String> provide(final @Nonnull ParameterNode<P> node, @Nonnull final InputReader inputReader, final int beginIndex, @Nonnull final SuggestContext suggestContext) {
        return Suggestor.cleanup(this.provideRaw(inputReader, suggestContext).stream().map(node::serialise), inputReader, beginIndex);
    }

    @Nonnull
    List<P> provideRaw(final @Nonnull InputReader input, final @Nonnull SuggestContext context);

    final class Static<P> implements SerialiseSuggestor<P> {

        private final @Nonnull List<P> data;
        private @Nullable List<String> serialisedData;

        private Static(@Nonnull final List<P> data) {
            this.data = data;
        }

        @Nonnull
        @Override
        public List<String> provide(@Nonnull final ParameterNode<P> node, @Nonnull final InputReader inputReader, final int beginIndex, @Nonnull final SuggestContext suggestContext) {
            if(serialisedData == null) serialisedData = this.provideRaw(inputReader, suggestContext)
                    .stream()
                    .map(node::serialise)
                    .collect(Collectors.toList());
            return Suggestor.cleanup(serialisedData.stream(), inputReader, beginIndex);
        }

        @Nonnull
        @Override
        public List<P> provideRaw(@Nonnull final InputReader input, @Nonnull final SuggestContext context) {
            return data;
        }

        @Nonnull
        public List<P> getData() {
            return data;
        }
    }
}
