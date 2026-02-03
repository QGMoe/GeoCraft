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

package top.qiguaiaaaa.nickel.command.utils;

import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.text.TextComponentTranslation;
import top.qiguaiaaaa.nickel.command.context.CommandContext;
import top.qiguaiaaaa.nickel.command.exception.NickelSyntaxException;
import top.qiguaiaaaa.nickel.command.node.parameter.SmartParameterNode;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
@FunctionalInterface
public interface ValidChecker {
    ValidChecker MATCH_ONE_PARAMETER = (self, args, context) -> {
        if(args.size()>=1 && !args.get(0).isEmpty()) return true;
        else if(self.isOptional()) return false;
        else throw new NickelSyntaxException(self.getCurrentBranch(), self, new TextComponentTranslation("nickel.command.parameter.smart.checker1"));
    };
    ValidChecker MATCH_TWO_PARAMETER = matchMultiParas(2);
    ValidChecker MATCH_THREE_PARAMETER = matchMultiParas(3);
    ValidChecker MATCH_FOUR_PARAMETER = matchMultiParas(4);
    ValidChecker MATCH_RESOURCE_LOCATION = MATCH_ONE_PARAMETER.and((self, args, context) -> {
        final String[] split = args.get(0).split(":");
        if(split.length>2) throw new NickelSyntaxException(self.getCurrentBranch(),self,
                new TextComponentTranslation("nickel.command.parameter.checker.resource_location.invalid.repeat"));
        else if(split.length==2&&split[0].contains("/"))
            throw new NickelSyntaxException(self.getCurrentBranch(),self,
                    new TextComponentTranslation("nickel.command.parameter.checker.resource_location.invalid.slash"));
        return true;
    });

    boolean check(@Nonnull SmartParameterNode<?> self, @Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException;

    @Nonnull
    default ValidChecker and(@Nonnull final ValidChecker after) {
        return (self, args, context) -> this.check(self, args, context) && after.check(self, args, context);
    }

    @Nonnull
    default ValidChecker or(@Nonnull final ValidChecker condition) {
        return (self, args, context) -> this.check(self, args, context) || condition.check(self, args, context);
    }

    @Nonnull
    static ValidChecker matchMultiParas(final int paraNum) {
        if (paraNum < 2) throw new IllegalArgumentException();
        return ((self, args, context) -> {
            if (args.size() >= paraNum && !args.get(paraNum - 1).isEmpty()) return true;
            else if (args.size() >= 1 && !args.get(0).isEmpty()) //只有一到三个参数，填了一半，不能用默认值
                throw new NickelSyntaxException(self.getCurrentBranch(),self,new TextComponentTranslation("nickel.command.parameter.smart.checkers",paraNum));
            else if (self.isOptional()) return false; //可以用默认值
            else throw new NickelSyntaxException(self.getCurrentBranch(),self,new TextComponentTranslation("nickel.command.parameter.smart.checkers",paraNum));
        });
    }
}
