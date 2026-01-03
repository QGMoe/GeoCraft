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

package top.qiguaiaaaa.geocraft.api.command.node;

import net.minecraft.command.CommandException;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 智能分支节点，可以根据{@link ISmartNode#match(List, CommandContext)}自动推断出下一个节点。
 * @see ISmartNode
 * @author QiguaiAAAA
 */
public class SmartSplitNode extends NoSplitNode{
    protected final List<ISmartNode> nodeList = new ArrayList<>();

    /**
     * 添加下一个智能节点
     * @param node 智能节点
     * @throws NullPointerException 当传入的智能节点参数为 null 时
     */
    public void addSmartNode(@Nonnull final ISmartNode node){
        nodeList.add(Objects.requireNonNull(node));
    }

    @Nullable
    protected ICommandNode findNextNode(@Nonnull List<String> args, @Nonnull CommandContext context){
        for(final ISmartNode node:nodeList){
            if(node.match(args,context)){
                return node;
            }
        }
        return childNode;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        final ICommandNode node = findNextNode(args, context);
        if(node!=null) node.execute(args,context);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        final ICommandNode node = findNextNode(args, context);
        if (node != null) return node.suggest(args, context);

        final List<String> suggests = new ArrayList<>();
        for(ISmartNode n:nodeList){
            final List<String> suggest = n.suggest(args, context);
            if(suggest != null) suggests.addAll(suggest);
        }
        final List<String> suggest = childNode.suggest(args, context);
        if(suggest != null) suggests.addAll(suggest);
        return suggests.stream().filter(s->s.startsWith(args.getLast().trim())).collect(Collectors.toList());
    }
}
