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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QGMoe
 */
@FunctionalInterface
public interface Suggestor<P> {
    @Nonnull
    List<String> provide(final @Nonnull ParameterNode<P> node, final @Nonnull InputReader inputReader, final int beginIndex, final @Nonnull SuggestContext suggestContext);

    @Nonnull
    default Suggestor<P> with(final @Nonnull Suggestor<P> suggestor){
        return ((node, inputReader, beginIndex, suggestContext) ->
                Stream.concat(this.provide(node,inputReader,beginIndex,suggestContext).stream(),
                        suggestor.provide(node,inputReader,beginIndex,suggestContext).stream())
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList()));
    }

    @Nonnull
    static List<String> cleanup(final @Nonnull Stream<String> suggestions,final @Nonnull InputReader input,final int beginIndex){
        final @Nonnull String subInput = input.getSubInput(beginIndex).replaceAll("\\s+"," ");
        final int lastTokenBegin = subInput.lastIndexOf(' ')+1;
        return suggestions.map(String::trim)
                .filter(s -> !s.isEmpty() && s.startsWith(subInput))
                .map(s -> s.substring(lastTokenBegin).split("\\s+",2)[0])
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
