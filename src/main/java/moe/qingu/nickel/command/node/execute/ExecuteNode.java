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

package moe.qingu.nickel.command.node.execute;

import net.minecraft.command.CommandException;
import net.minecraft.command.SyntaxErrorException;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
@FunctionalInterface
public interface ExecuteNode extends ICommandNode {
    @Override
    default <T extends List<String> & Deque<String>> void execute(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException{
        throwIfShouldNotHaveArguments(args,keepArguments());
        run(context,args);
    }

    @Nullable
    @Override
    default <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context){
        return null;
    }

    @Nonnull
    @Override
    default CommandBranch branch(){
        return new CommandBranch();
    }

    default boolean keepArguments(){
        return false;
    }

    void run(@Nonnull ExecuteContext context, @Nonnull List<String> args) throws CommandException;

    static void throwIfShouldNotHaveArguments(final @Nonnull List<String> args,final boolean keepArg) throws SyntaxErrorException{
        if(!keepArg && !args.isEmpty() && !args.get(0).trim().isEmpty())
            throw new SyntaxErrorException("nickel.command.execute.argument_not_empty",String.join(" ",args));
    }
}
