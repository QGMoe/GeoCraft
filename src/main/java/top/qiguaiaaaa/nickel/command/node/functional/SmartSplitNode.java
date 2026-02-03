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

package top.qiguaiaaaa.nickel.command.node.functional;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import top.qiguaiaaaa.nickel.command.context.CommandContext;
import top.qiguaiaaaa.nickel.command.context.ExecuteContext;
import top.qiguaiaaaa.nickel.command.context.SuggestContext;
import top.qiguaiaaaa.nickel.command.exception.NickelSyntaxException;
import top.qiguaiaaaa.nickel.command.node.ICommandNode;
import top.qiguaiaaaa.nickel.command.node.IDocumentaryNode;
import top.qiguaiaaaa.nickel.command.node.ISmartNode;
import top.qiguaiaaaa.nickel.command.node.NoSplitNode;
import top.qiguaiaaaa.nickel.command.node.execute.ExecuteNode;
import top.qiguaiaaaa.nickel.command.utils.CommandBranch;
import top.qiguaiaaaa.nickel.command.utils.SplitCommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 智能分支节点，可以根据{@link ISmartNode#match(List, CommandContext)}自动推断出下一个节点。
 * @see ISmartNode
 * @author QiguaiAAAA
 */
public class SmartSplitNode extends NoSplitNode implements IDocumentaryNode {
    protected final List<ISmartNode> nodeList = new ArrayList<>();
    protected SplitCommandBranch curBranch;
    protected ITextComponent document;

    /**
     * 添加下一个智能节点
     * @param node 智能节点
     * @throws NullPointerException 当传入的智能节点参数为 null 时
     */
    public void addSmartNode(@Nonnull final ISmartNode node){
        nodeList.add(Objects.requireNonNull(node));
    }

    @Nullable
    protected ICommandNode findNextNode(@Nonnull final List<String> args, @Nonnull final CommandContext context){
        for(final @Nonnull ISmartNode node:nodeList){
            if(node.match(args,context)){
                return node;
            }
        }
        return childNode;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException {
        final ICommandNode node = findNextNode(args, context);
        if(node == null || node instanceof ExecuteNode && !((ExecuteNode) node).keepArguments() && !args.isEmpty() && !args.get(0).trim().isEmpty())
            throw new NickelSyntaxException(curBranch,this);
        node.execute(args,context);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context) {
        //GeoCraft.getLogger().info("[Smart] Provide Suggest For [len={}] : {}",args.size(),String.join(" ",args));
        if(args.size()>1){ //Smart 的位置不需要建议
            final ICommandNode node = findNextNode(args, context);
            if (node != null) return node.suggest(args, context);
            return Collections.emptyList();
        }

        return (childNode==null?nodeList.stream():Stream.concat(nodeList.stream(),Stream.of(childNode)))
                .map(node -> node.suggest(args,context))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::trim)
                .distinct() //去重
                .filter(s->s.startsWith(args.isEmpty()?"":args.getLast().trim()))
                .sorted()
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        final Set<CommandBranch> branchSet = new HashSet<>();
        final List<ITextComponent> choices = new ArrayList<>();
        final CommandBranch defaultBranch = childNode == null?null:childNode.branch();
        final boolean optional = defaultBranch != null && defaultBranch.isEmpty();
        if(defaultBranch != null && !optional){
            branchSet.add(defaultBranch);
            choices.add(defaultBranch.getDocuments().get(0));
        }
        branchSet.addAll(nodeList.stream()
                .map(ICommandNode::branch)
                .filter(branch -> !branch.isEmpty())
                .peek(branch -> choices.add(branch.getDocuments().get(0)))
                .collect(Collectors.toSet()));

        this.document = new TextComponentString(IDocumentaryNode.getFormatBegin(optional));
        this.curBranch = new SplitCommandBranch(branchSet);
        for(int i=0;i<choices.size();i++){
            if(i>0) this.document.appendText(IDocumentaryNode.SPLIT_NODE_SPLIT);
            this.document.appendSibling(choices.get(i).createCopy());
        }
        this.document.appendText(IDocumentaryNode.getFormatEnd(optional));
        curBranch.setEndDocument(this.document.createCopy());
        return this.curBranch;
    }

    @Nonnull
    @Override
    public ITextComponent getDocument() {
        return document.createCopy();
    }
}
