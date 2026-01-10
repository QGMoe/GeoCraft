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

package top.qiguaiaaaa.geocraft.api.command.node.generic.literal;

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
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * 多字面量节点，可以通过不同的字面量做到不同的分支。<br/>
 * 其作为智能节点时，不可以自定义{@link #match(List, CommandContext)}，使用{@link #setMatcher(BiPredicate)}会抛出{@link UnsupportedOperationException}。
 * 当提供的参数（args的首个 String）满足以下条件时，匹配会成功：<br/>
 * - 提供的参数是该节点的字面量之一<br/>
 * - 没有提供参数，且当前节点可选<br/>
 *   - 若默认节点是{@link ISmartNode}，则会检查默认节点是否匹配<br/>
 *   - 否则总是认为匹配成功<br/>
 * 当为可选的时候，应当通过 {@link #setChildNode(ICommandNode)} 来设置默认的子节点
 * @see LiteralNode
 * @see top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralsNodeBuilder
 * @since GeoCraft API-0.2.0
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
        final ICommandNode node;
        if(args.size()>0){
            node = literal2Node.get(args.getFirst());
        }else if(!isOptional()) throw new WrongUsageException("wrong!");
        else node = childNode;
        if(node != null){
            String first = null;
            try {
                first = args.pollFirst();
                node.execute(args,context);
            }finally {
                if(first != null) args.addFirst(first);
            }
        }else throw new WrongUsageException("Wrong!");
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context) {
        if(args.size()>1){
            String first = null;
            try {
                first = args.pollFirst();
                ICommandNode nextNode = literal2Node.get(first);
                if(nextNode == null && isOptional()) nextNode = childNode;
                return nextNode == null?null:nextNode.suggest(args, context);
            }finally {
                if(first != null) args.addFirst(first);
            }
        }else if(args.size()>0){
            return literal2Node.keySet().stream()
                    .filter(literal -> literal.startsWith(args.getFirst().trim()))
                    .sorted()
                    .collect(Collectors.toList());
        }else {
            return Lists.newArrayList(literal2Node.keySet());
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
        if(args.size()>0){
            final String first = args.get(0);
            return literal2Node.containsKey(first);
        }else if(isOptional() && childNode != null){
            if(childNode instanceof ISmartNode){
                return ((ISmartNode) childNode).match(args,context);//检查默认节点是否匹配，注意这时候已经没有还未解析的参数了
            }else return true;//否则认为始终匹配
        }else return false;
    }

    @Override
    public void setMatcher(@Nullable BiPredicate<List<String>, CommandContext> checker) {
        throw new UnsupportedOperationException();
    }
}
