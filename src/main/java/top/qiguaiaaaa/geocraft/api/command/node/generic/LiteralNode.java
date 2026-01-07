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

package top.qiguaiaaaa.geocraft.api.command.node.generic;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.functional.PermitNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class LiteralNode extends PermitNode implements ISmartNode {

    protected final @Nonnull String literal;

    protected @Nullable BiPredicate<List<String>, CommandContext> matchChecker;

    public LiteralNode(@Nonnull String literal){
        this.literal= literal;
    }

    @Override
    public boolean match(@Nonnull List<String> args, @Nonnull CommandContext context) {
        if(matchChecker!=null) return matchChecker.test(args,context);
        return args.size()>0 && literal.equals(args.get(0));
    }

    @Override
    public void setMatcher(@Nullable BiPredicate<List<String>, CommandContext> checker) {
        this.matchChecker = checker;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(!checkPermission(context)) throw new WrongUsageException("Permission not enough!");
        if(!match(args,context)) throw new WrongUsageException("Wrong");
        if(childNode != null) childNode.execute(args,context);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        return Lists.newArrayList(literal);
    }
}
