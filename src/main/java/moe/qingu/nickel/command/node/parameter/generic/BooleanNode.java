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

package moe.qingu.nickel.command.node.parameter.generic;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.parameter.SmartParameterNode;
import moe.qingu.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class BooleanNode extends SmartParameterNode<Boolean> {
    public static final DefaultParser<Boolean> DEFAULT_PARSER = (node, context) -> Boolean.FALSE;
    public static final BiPredicate<List<String>,CommandContext> DEFAULT_MATCHER = (args,context) -> args.size()>0
            && ("true".equals(args.get(0))) || "false".equals(args.get(0)) || "1".equals(args.get(0)) || "0".equals(args.get(0));

    public static final BiFunction<List<String>,SuggestContext,List<String>> DEFAULT_SUGGESTOR = (args, context) -> Lists.newArrayList(Boolean.TRUE.toString(),Boolean.FALSE.toString());
    public BooleanNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
        setMatcher(DEFAULT_MATCHER);
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Nonnull
    @Override
    public Class<Boolean> getTypeClass() {
        return getType();
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.generic.boolean";
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        if(!ValidChecker.MATCH_ONE_PARAMETER.check(this,args,context)){
            return false;
        }
        parseBoolean(args.get(0));
        return true;
    }

    @Override
    public <T extends List<String> & Deque<String>> Boolean parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        return parseBoolean(args.getFirst());
    }

    public static boolean parseBoolean(final @Nullable String input) throws SyntaxErrorException{
        if("true".equals(input) || "1".equals(input)){
            return true;
        }
        if("false".equals(input) || "0".equals(input)){
            return false;
        }
        throw new SyntaxErrorException("commands.generic.boolean.invalid",input==null?"":input);
    }
}
