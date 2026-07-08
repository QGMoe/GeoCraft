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

package moe.qingu.nickel.command.node.literal;

import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.suggestor.Suggestion;
import moe.qingu.nickel.command.utils.Claimer;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.CommandException;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.node.IOptionalNode;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.builder.literal.LiteralsNodeBuilder;
import moe.qingu.nickel.command.node.functional.PermitNode;
import moe.qingu.nickel.command.utils.CommandBranch;
import moe.qingu.nickel.command.utils.SplitCommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.translation;

/**
 * 多字面量节点，可以通过不同的字面量做到不同的分支。<br/>
 * 其作为智能节点时，不可以自定义{@link #claims(InputReader)}，使用{@link #setClaimer(Claimer)}会抛出{@link UnsupportedOperationException}。
 * 当提供的参数（args的首个 String）满足以下条件时，匹配会成功：<br/>
 * - 提供的参数是该节点的字面量之一<br/>
 * - 没有提供参数，且当前节点可选<br/>
 *   - 若默认节点是{@link ISmartNode}，则会检查默认节点是否匹配<br/>
 *   - 否则总是认为匹配成功<br/>
 * 当为可选的时候，应当通过 {@link #setChildNode(ICommandNode)} 来设置默认的子节点
 * @see LiteralNode
 * @see LiteralsNodeBuilder
 * @since GeoCraft API-0.2.0
 * @author QiguaiAAAA
 */
public class LiteralsNode extends PermitNode implements IOptionalNode, ISmartNode, IDocumentaryNode {
    protected final Map<String, ICommandNode> literal2Node = new LinkedHashMap<>();
    protected final Map<ICommandNode, CommandBranch> literal2Branch = new HashMap<>();

    protected boolean optional;
    protected SplitCommandBranch curBranch;

    public void addLiteral(@Nonnull final String literal,@Nonnull final ICommandNode node){
        literal2Node.put(literal,node);
    }

    @Override
    public void execute(@Nonnull final InputReader input, @Nonnull final ExecuteContext context) throws CommandException {
        if(!checkPermission(context)) throw new CommandException("nickel.command.functional.permit.denied");
        final ICommandNode node;
        if(!input.isRemainingEmpty()){
            node = literal2Node.get(input.readToken());
        }else if(!isOptional()) throw new NickelSyntaxException(curBranch,this);
        else node = childNode;
        if(node != null){
            final CommandBranch branch = literal2Branch.get(node);
            if(branch == null || branch == curBranch) context.enter(node);
            else try(final CommandContext.ContextStack<?> ignored = context.enter(literal2Branch.get(node))){
                context.enter(node);
            }
        }else if(isOptional()) throw new NickelCommandException(curBranch)
                .withSource(this)
                .withAppendix(translation("nickel.command.literals.exception.default"));
        else throw new NickelSyntaxException(curBranch,this);
    }

    @Nullable
    @Override
    public Suggestion suggest(@Nonnull final InputReader input, @Nonnull final SuggestContext context) throws CommandException {
        if(!checkPermission(context)) return null;
        if(!input.isRemainingEmpty()){
            final String token = input.readToken();
            if(!input.canRead()){
                return new Suggestion(this,literal2Node.keySet().stream()
                        .filter(literal -> literal.startsWith(token))
                        .collect(Collectors.toList()));
            }else {
                ICommandNode nextNode = literal2Node.get(token);
                if(nextNode == null && isOptional()) nextNode = childNode;
                if(nextNode == null) return null;
                final CommandBranch branch = literal2Branch.get(nextNode);
                if(branch == null || branch == curBranch) return context.enter(nextNode);
                else try(final CommandContext.ContextStack<?> ignored = context.enter(literal2Branch.get(nextNode))){
                    return context.enter(nextNode);
                }
            }
        }else {
            return new Suggestion(this, new ArrayList<>(literal2Node.keySet()));
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
    public boolean claims(@Nonnull final InputReader input) {
        if(!input.isRemainingEmpty()) return literal2Node.containsKey(input.readToken());
        else if(isOptional() && childNode != null)
            if(childNode instanceof ISmartNode) return ((ISmartNode) childNode).claims(input);//检查默认节点是否匹配，注意这时候已经没有还未解析的参数了
            else return true;//否则认为始终匹配
        else return false;
    }

    @Override
    public void setClaimer(@Nullable final Claimer checker) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        literal2Node.forEach((key, value) -> {
            if(literal2Branch.containsKey(value)) return;
            final CommandBranch branch = value.branch();
            branch.appendDocument(plain(key));
            literal2Branch.put(value, branch);
        });
        if(!literal2Branch.containsKey(childNode)) Optional.ofNullable(childNode).map(ICommandNode::branch)
                .filter(branch -> !branch.isEmpty())
                .ifPresent(b -> literal2Branch.put(childNode,b));
        this.curBranch = new SplitCommandBranch(literal2Branch.values());
        curBranch.setEndDocument(getDocument());
        return curBranch;
    }

    @Nonnull
    @Override
    public TextBuilder<?,?> getDocument() {
        return plain(IDocumentaryNode.getFormatBegin(isOptional()) +
                        String.join(SPLIT_NODE_SPLIT, literal2Node.keySet()) +
                        IDocumentaryNode.getFormatEnd(isOptional()));
    }
}
