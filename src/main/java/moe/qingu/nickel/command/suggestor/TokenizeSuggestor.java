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

import moe.qingu.nickel.command.reader.InputReader;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.parameter.ParameterNode;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * @author QGMoe
 */
public final class TokenizeSuggestor<P> implements Suggestor<P> {

    private final int counts;
    private final @Nonnull BiFunction<String[], SuggestContext, Stream<String>> provider;

    private TokenizeSuggestor(final int counts, @Nonnull BiFunction<String[], SuggestContext, Stream<String>> provider) {
        this.counts = counts;
        this.provider = provider;
    }

    @Nonnull
    public static <P> TokenizeSuggestor<P> of(final int counts, final @Nonnull BiFunction<String[], SuggestContext, Stream<String>> provider) {
        return new TokenizeSuggestor<>(counts, provider);
    }

    @Nonnull
    public static <P> TokenizeSuggestor<P> of(final @Nonnull BiFunction<String, SuggestContext, Stream<String>> provider) {
        return new TokenizeSuggestor<>(1, (tokens, context) -> provider.apply(tokens[0], context));
    }

    @Nonnull
    @Override
    public Stream<String> provide(final @Nonnull ParameterNode<P> node, @Nonnull final InputReader inputReader, final int beginIndex, @Nonnull final SuggestContext suggestContext) {
        try {
            inputReader.setCursor(beginIndex);
            final String[] tokens = new String[counts];
            int lastTokenBeginIndex = beginIndex;
            for (int i = 0; i < counts; i++) {
                if (i == counts - 1) {
                    inputReader.skipIfWhitespace();
                    lastTokenBeginIndex = inputReader.getCursor();
                }
                tokens[i] = inputReader.readToken();
            }
            if (inputReader.canRead()) return Stream.empty();
            return Suggestor.cleanup(this.provider.apply(tokens, suggestContext), inputReader, lastTokenBeginIndex);
        } finally {
            inputReader.setCursor(inputReader.getLength());
        }
    }
}
