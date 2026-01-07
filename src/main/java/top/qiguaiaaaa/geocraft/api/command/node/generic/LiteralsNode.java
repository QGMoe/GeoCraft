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
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;
import top.qiguaiaaaa.geocraft.api.command.node.IOptionalNode;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.functional.PermitNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class LiteralsNode extends PermitNode implements IOptionalNode, ISmartNode {
    protected final Map<String, ICommandNode> literal2Node = new LinkedHashMap<>();

    protected boolean optional;

    public void addLiteral(@Nonnull String literal,@Nonnull ICommandNode node){
        literal2Node.put(literal,node);
    }


    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(!checkPermission(context)) throw new WrongUsageException("Not enough permission!");
        ICommandNode node;
        if(args.size()>0){
            node = literal2Node.get(args.getFirst());
        }else if(!isOptional()) throw new WrongUsageException("wrong!");
        else node = childNode;
        if(node != null){
            final String first = args.pollFirst();
            try {
                node.execute(args,context);
            }finally {
                args.addFirst(first);
            }
        }
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        if(args.size()>1){
            final String first = args.pollFirst();
            try {
                ICommandNode nextNode = literal2Node.get(first);
                return nextNode.suggest(args, context);
            }finally {
                args.addFirst(first);
            }
        }else if(args.size()>0){
            return Lists.newArrayList(literal2Node.keySet());
        }else {
            return null;
        }
    }

    @Override
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean match(@Nonnull List<String> args, @Nonnull CommandContext context) {
        if(childNode!=null) return true;
        if(args.size()>0){
            final String first = args.get(0);
            return literal2Node.containsKey(first);
        }else return false;
    }

    @Override
    public void setMatcher(@Nullable BiPredicate<List<String>, CommandContext> checker) {
        throw new UnsupportedOperationException();
    }
}
