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
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.*;
/**
 * @author QGMoe
 */
public class NickelRuntimeException extends CommandException implements INickelException{
    protected final ITextComponent information;

    public NickelRuntimeException(final @Nonnull ITextComponent information) {
        super("nickel.command.exception.base.message");
        this.information = information;
    }

    public NickelRuntimeException(final @Nonnull TextBuilder<?,?> information) {
        super("nickel.command.exception.base.message");
        this.information = information.done();
    }

    @Nonnull
    public ITextComponent getInformation() {
        return information;
    }

    @Override
    public void feedbackTo(@Nonnull final ICommandSender sender) {
        sender.sendMessage(translation("nickel.command.exception.runtime.message")
                .arg(information)
                .color(TextFormatting.RED)
                .done());
    }
}
