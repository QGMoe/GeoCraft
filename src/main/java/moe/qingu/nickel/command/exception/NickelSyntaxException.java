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

import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.text.TextFormatting;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.utils.CommandBranch;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QiguaiAAAA
 */
public class NickelSyntaxException extends SyntaxErrorException implements INickelException{
    protected final CommandBranch fromBranch;
    protected final IDocumentaryNode fromNode;
    protected TextBuilder<?,?> appendix;
    protected @Nullable CursorInfo info;

    public NickelSyntaxException(@Nonnull final CommandBranch fromBranch,@Nonnull final IDocumentaryNode fromNode) {
        this.fromBranch = fromBranch;
        this.fromNode = fromNode;
        this.appendix = null;
    }

    @Nonnull
    public NickelSyntaxException withAppendix(final @Nonnull TextBuilder<?,?> appendix){
        this.appendix = appendix;
        return this;
    }

    @Nonnull
    public NickelSyntaxException withCursor(final @Nonnull InputReader input,final int loc){
        this.info = new CursorInfo(input,loc);
        return this;
    }

    @Nonnull
    public CommandBranch getSourceBranch() {
        return fromBranch;
    }

    @Nonnull
    public ICommandNode getSourceNode() {
        return fromNode;
    }

    @Nonnull
    public TextBuilder<?,?> getNodeDocument(){
        return fromNode.getDocument();
    }

    @Nullable
    public TextBuilder<?,?> getDetails() {
        return appendix;
    }

    public void feedbackTo(@Nonnull final ICommandSender sender){
        translation("nickel.command.exception.syntax.node",this.getNodeDocument())
                .color(TextFormatting.RED)
                .sendTo(sender);
        if(info != null) info.showInfo(sender);
        if(this.getDetails()!=null) translation("nickel.command.exception.syntax.details",this.getDetails())
                .color(TextFormatting.RED)
                .sendTo(sender);
        translation("nickel.command.exception.syntax.usage",this.getSourceBranch().getDocument())
                .color(TextFormatting.AQUA)
                .sendTo(sender);
    }

    @Nonnull
    @Override
    public TextBuilder<?, ?> getSuggestInfo() {
        final TextBuilder<?,?> msg = translation("nickel.command.exception.syntax.node",this.getNodeDocument())
                .color(TextFormatting.RED);
        if(this.getDetails() != null) msg.then(": ")
                .then(this.getDetails());
        return msg.hoverTo(HoverEvent.Action.SHOW_TEXT)
                .content((info == null ? plain(""): info.getInfo()
                        .then(plain(" <-- \n")
                                .underlined(false)))
                        .then(translation("nickel.command.exception.syntax.usage",this.getSourceBranch().getDocument())
                                .underlined(false)
                                .color(TextFormatting.AQUA)));
    }
}
