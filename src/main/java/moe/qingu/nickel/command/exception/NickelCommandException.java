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

package moe.qingu.nickel.command.exception;

import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public class NickelCommandException extends CommandException implements INickelException{
    protected final CommandBranch fromBranch;
    protected final IDocumentaryNode fromNode;
    protected final ITextComponent appendix;

    public NickelCommandException(@Nonnull final CommandBranch fromBranch) {
        this(fromBranch,null,(ITextComponent) null);
    }

    public NickelCommandException(@Nonnull final CommandBranch fromBranch,@Nonnull final ITextComponent appendix) {
        this(fromBranch, null,appendix);
    }

    public NickelCommandException(@Nonnull final CommandBranch fromBranch,@Nonnull final TextBuilder<?,?> appendix) {
        this(fromBranch, null,appendix);
    }

    public NickelCommandException(@Nonnull final CommandBranch fromBranch, @Nullable final IDocumentaryNode fromNode) {
        this(fromBranch,fromNode,(ITextComponent) null);
    }

    public NickelCommandException(@Nonnull final CommandBranch fromBranch, @Nullable final IDocumentaryNode fromNode, @Nullable final TextBuilder<?,?> appendix) {
        this(fromBranch,fromNode,appendix == null?null: appendix.done());
    }

    public NickelCommandException(@Nonnull final CommandBranch fromBranch, @Nullable final IDocumentaryNode fromNode, @Nullable final ITextComponent appendix) {
        super("nickel.command.exception.base.message");
        this.fromBranch = fromBranch;
        this.fromNode = fromNode;
        this.appendix = appendix;
    }

    @Nonnull
    public CommandBranch getSourceBranch() {
        return fromBranch;
    }

    @Nullable
    public ICommandNode getSourceNode() {
        return fromNode;
    }

    @Nullable
    public ITextComponent getNodeDocument(){
        return fromNode == null?null:fromNode.getDocument();
    }

    @Nullable
    public ITextComponent getDetails() {
        return appendix;
    }

    @Override
    public void feedbackTo(@Nonnull final ICommandSender sender){
        final ITextComponent node;
        if(this.getNodeDocument() == null) node = new TextComponentTranslation("nickel.command.exception.base.message");
        else node = new TextComponentTranslation("nickel.command.exception.base.node.pre")
                .appendSibling(this.getNodeDocument()
                        .appendSibling(new TextComponentTranslation("nickel.command.exception.base.node.sub")));
        node.getStyle().setColor(TextFormatting.RED);
        final ITextComponent details = this.getDetails()==null?null:new TextComponentTranslation("nickel.command.exception.base.details")
                .appendSibling(this.getDetails());
        if(details != null) details.getStyle().setColor(TextFormatting.RED);
        final ITextComponent document = new TextComponentTranslation("nickel.command.exception.base.branch")
                .appendSibling(this.getSourceBranch().getDocument());
        document.getStyle().setColor(TextFormatting.AQUA);
        sender.sendMessage(node);
        if(details != null) sender.sendMessage(details);
        sender.sendMessage(document);
    }
}
