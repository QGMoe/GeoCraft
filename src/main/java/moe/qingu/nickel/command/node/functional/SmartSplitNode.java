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

package moe.qingu.nickel.command.node.functional;

import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.reader.InputReader;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.node.NoSplitNode;
import moe.qingu.nickel.command.utils.CommandBranch;
import moe.qingu.nickel.command.utils.SplitCommandBranch;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.wrap;

/**
 * 智能分支节点，可以根据{@link ISmartNode#match(InputReader)}自动推断出下一个节点。
 * @see ISmartNode
 * @author QiguaiAAAA
 */
public class SmartSplitNode extends NoSplitNode implements IDocumentaryNode {
    protected final List<ISmartNode> nodeList = new ArrayList<>();
    protected final Map<ICommandNode,CommandBranch> branchMap = new HashMap<>();
    protected SplitCommandBranch curBranch;
    protected TextBuilder<?,?> document;

    /**
     * 添加下一个智能节点
     * @param node 智能节点
     * @throws NullPointerException 当传入的智能节点参数为 null 时
     */
    public void addSmartNode(@Nonnull final ISmartNode node){
        nodeList.add(Objects.requireNonNull(node));
    }

    @Nullable
    protected ICommandNode findNextNode(@Nonnull final InputReader input){
        for(final @Nonnull ISmartNode node:nodeList){
            final int cur = input.getCursor();
            try {
                if(node.match(input)){
                    return node;
                }
            }finally {
                input.setCursor(cur);
            }
        }
        return childNode;
    }

    @Override
    public void execute(@Nonnull final InputReader input, @Nonnull final ExecuteContext context) throws CommandException {
        final ICommandNode node = findNextNode(input);
        if(node == null) throw new NickelSyntaxException(curBranch,this);
        final CommandBranch branch = branchMap.get(node);
        if(branch == null) context.enter(node);
        else try (final @Nonnull CommandContext.ContextStack<?> ignored = context.enter(branch)){
            context.enter(node);
        }
    }


    @Nullable
    @Override
    public Stream<String> suggest(@Nonnull final InputReader input, @Nonnull final SuggestContext context) {
        //GeoCraft.getLogger().info("[Smart] Provide Suggest For [len={}] : {}",args.size(),String.join(" ",args));
        if(!input.isRemainingEmpty()){ //Smart 的位置不需要建议
            final ICommandNode node = findNextNode(input);
            if (node == null) return null;
            final CommandBranch branch = branchMap.get(node);
            if(branch == null) return context.enter(node);
            try (final @Nonnull CommandContext.ContextStack<?> ignored = context.enter(branch)){
                return context.enter(node);
            }
        }

        return (childNode==null?nodeList.stream():Stream.concat(nodeList.stream(),Stream.of(childNode)))
                .map(context::enter)
                .filter(Objects::nonNull)
                .flatMap(Function.identity())
                .map(String::trim)
                .distinct() //去重
//                .filter(s->s.startsWith(args.isEmpty()?"":args.getLast().trim()))
                .sorted();
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        final List<ITextComponent> choices = new ArrayList<>();
        final CommandBranch defaultBranch = childNode == null?null:childNode.branch();
        final boolean optional = defaultBranch != null && defaultBranch.isEmpty();
        if(defaultBranch != null && !optional){
            branchMap.put(childNode,defaultBranch);
            choices.add(defaultBranch.getDocuments().get(0));
        }
        nodeList.stream()
                .map(node -> Pair.of(node,node.branch()))
                .filter(pair -> !pair.getValue().isEmpty())
                .forEach(pair -> {
                    choices.add(pair.getValue().getDocuments().get(0));
                    branchMap.put(pair.getKey(),pair.getValue());
                });

        this.document = plain(IDocumentaryNode.getFormatBegin(optional));
        this.curBranch = new SplitCommandBranch(branchMap.values());
        for(int i=0;i<choices.size();i++){
            if(i>0) this.document.then(IDocumentaryNode.SPLIT_NODE_SPLIT);
            this.document.then(wrap(choices.get(i).createCopy()));
        }
        this.document.then(IDocumentaryNode.getFormatEnd(optional));
        curBranch.setEndDocument(this.document);
        return this.curBranch;
    }

    @Nonnull
    @Override
    public TextBuilder<?,?> getDocument() {
        return document;
    }
}
