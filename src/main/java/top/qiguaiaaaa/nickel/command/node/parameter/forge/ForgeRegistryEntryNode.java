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

package top.qiguaiaaaa.nickel.command.node.parameter.forge;

import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import top.qiguaiaaaa.nickel.command.context.CommandContext;
import top.qiguaiaaaa.nickel.command.context.SuggestContext;
import top.qiguaiaaaa.nickel.command.node.parameter.SmartParameterNode;
import top.qiguaiaaaa.nickel.command.utils.Matchers;
import top.qiguaiaaaa.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author QiguaiAAAA
 */
public abstract class ForgeRegistryEntryNode<E extends IForgeRegistryEntry<E>> extends SmartParameterNode<E> {
    public ForgeRegistryEntryNode(@Nonnull String name) {
        super(name);
        setMatcher(Matchers.RESOURCE_LOCATION);
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        return ValidChecker.MATCH_RESOURCE_LOCATION.check(this,args,context);
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public abstract Class<E> getType();

    @Nonnull
    @Override
    public Class<E> getTypeClass() {
        return getType();
    }

    @Nonnull
    public static <E extends IForgeRegistryEntry<E>> BiFunction<List<String>, SuggestContext,List<String>> createSuggestProviderFromRegistry(@Nonnull final IForgeRegistry<E> registry){
        final List<String> entries = registry.getKeys().stream().map(Objects::toString).collect(Collectors.toList());
        return (args,context)-> entries;
    }

    @Nonnull
    public static <K,E extends IForgeRegistryEntry<E>> BiFunction<List<String>, SuggestContext,List<String>> createSuggestProviderFromRegistry(@Nonnull final IRegistry<K,E> registry){
        final List<String> entries = registry.getKeys().stream().map(Objects::toString).collect(Collectors.toList());
        return (args,context)-> entries;
    }
}
