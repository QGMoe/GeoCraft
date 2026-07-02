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

package moe.qingu.nickel.command.utils;

import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.reader.InputReader;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QiguaiAAAA
 */
@FunctionalInterface
public interface ValidChecker {
    ValidChecker REQUIRE_ONE_TOKEN = (self, input) -> {
        if(!input.isRemainingEmpty()) return true;
        else if(self.isOptional()) return false;
        else return input.getContext().panic(translation("nickel.command.parameter.smart.checker1"));
    };
    ValidChecker MATCH_TWO_PARAMETER = matchMultiTokens(2);
    ValidChecker MATCH_THREE_PARAMETER = matchMultiTokens(3);
    ValidChecker MATCH_FOUR_PARAMETER = matchMultiTokens(4);
    ValidChecker MATCH_RESOURCE_LOCATION = REQUIRE_ONE_TOKEN.and((self, input) ->
            matchResourceLocation(input.readToken(),input.getContext()));

    default boolean check(@Nonnull final ParameterNode<?> self, @Nonnull final InputReader input) throws CommandException{
        final int cur = input.getCursor();
        try{
            return run(self,input);
        }finally {
            input.setCursor(cur);
        }
    }

    boolean run(@Nonnull final ParameterNode<?> self, @Nonnull final InputReader input) throws CommandException;

    @Nonnull
    default ValidChecker and(@Nonnull final ValidChecker after) {
        return (self, input) -> this.check(self, input) && after.check(self, input);
    }

    @Nonnull
    default ValidChecker or(@Nonnull final ValidChecker condition) {
        return (self, input) -> this.check(self, input) || condition.check(self, input);
    }

    @Nonnull
    static ValidChecker matchMultiTokens(final int paraNum) {
        if (paraNum < 2) throw new IllegalArgumentException();
        return (self, input) -> matchMultiTokens(self,input,paraNum);
    }

    static boolean matchMultiTokens(final @Nonnull ParameterNode<?> self, final @Nonnull InputReader input, final int paraNum) throws NickelCommandException, NickelSyntaxException, NickelRuntimeException {
        int num = paraNum;
        check:{
            while (num-->0) if(input.readToken().isEmpty()) break check;
            return true;
        }
        if(num < paraNum-1) return input.getContext().panic(translation("nickel.command.parameter.smart.checkers").arg(paraNum));
        else if (self.isOptional()) return false; //可以用默认值
        else return input.getContext().panic(translation("nickel.command.parameter.smart.checkers").arg(paraNum));
    }

    static boolean matchResourceLocation(final @Nonnull String token, final @Nonnull CommandContext context) throws NickelCommandException, NickelSyntaxException, NickelRuntimeException {
        final String[] split = token.split(":");
        if(split.length>2) return context.panic(translation("nickel.command.parameter.checker.resource_location.invalid.repeat"));
        else if(split.length==2&&split[0].contains("/"))
            return context.panic(translation("nickel.command.parameter.checker.resource_location.invalid.slash"));
        return true;
    }
}
