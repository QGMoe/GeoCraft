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

package top.qiguaiaaaa.nickel.command.node.parameter.generic;

import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import top.qiguaiaaaa.nickel.command.context.CommandContext;
import top.qiguaiaaaa.nickel.command.context.ExecuteContext;
import top.qiguaiaaaa.nickel.command.context.SuggestContext;
import top.qiguaiaaaa.nickel.command.node.parameter.SmartParameterNode;
import top.qiguaiaaaa.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class UUIDNode extends SmartParameterNode<UUID> {
    public static final DefaultParser<UUID> DEFAULT_PARSER = (node, context) -> UUID.randomUUID();
    public static final BiFunction<List<String>, SuggestContext,List<String>> DEFAULT_SUGGESTOR =
            ((args, context) -> Collections.singletonList(UUID.randomUUID().toString()));


    public UUIDNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }

    @Nonnull
    @Override
    public Class<UUID> getTypeClass() {
        return getType();
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.generic.uuid";
    }

    @Override
    public <T extends List<String> & Deque<String>> UUID parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        return parseUUID(args);
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        if(!ValidChecker.MATCH_ONE_PARAMETER.check(this,args,context)) return false;
        parseUUID(args);
        return true;
    }

    @Nonnull
    protected UUID parseUUID(@Nonnull List<String> args) throws WrongUsageException{
        try {
            return UUID.fromString(args.get(0));
        }catch (IllegalArgumentException e){
            throw new WrongUsageException("wrong uuid!");
        }
    }
}
