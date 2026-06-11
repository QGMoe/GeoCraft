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

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.node.functional.PermitNode;
import moe.qingu.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * 单字面量节点，可以通过匹配对应的字面量来确定下一步的执行节点，该节点不是一个可选节点。
 * 其作为智能节点时，可以通过 {@link #setMatcher(BiPredicate)} 设置自己的匹配逻辑。其默认逻辑就是检查传入的参数长度是否大于 0，且第一个参数是否等于字面量。
 * @see LiteralsNode
 * @author QiguaiAAAA
 */
public class LiteralNode extends PermitNode implements ISmartNode, IDocumentaryNode {

    protected final @Nonnull String literal;

    protected @Nullable BiPredicate<List<String>, CommandContext> matchChecker;

    protected CommandBranch currentBranch;

    public LiteralNode(@Nonnull final String literal){
        this.literal= Objects.requireNonNull(literal);
    }

    @Nonnull
    public String getLiteral() {
        return literal;
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
        if(!checkPermission(context)) throw new CommandException("nickel.command.functional.permit.denied");
        final String first = args.getFirst();
        if(!literal.equals(first)){
            throw new NickelSyntaxException(currentBranch,this,
                    new TextComponentTranslation("nickel.command.functional.literal.non_match",this.literal,args.getFirst()));
        }
        try {
            args.pop();
            if(childNode != null) childNode.execute(args,context);
        }finally {
            args.addFirst(first);
        }
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context) {
        if(args.size()>1){
            final String first = args.getFirst();
            try {
                args.pop();
                return childNode == null? null : childNode.suggest(args,context);
            }finally {
                args.addFirst(first);
            }
        }
        if(args.size()==1){
            return literal.startsWith(args.getFirst())?Collections.singletonList(literal):null;
        }
        return Collections.singletonList(literal);
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        this.currentBranch = super.branch();
        currentBranch.appendDocument(getDocument());
        return currentBranch;
    }

    @Nonnull
    @Override
    public ITextComponent getDocument() {
        return new TextComponentString(literal);
    }
}
