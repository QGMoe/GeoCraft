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

package moe.qingu.nickel.command.node.parameter.minecraft.nbt;

import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.utils.Acceptor;
import moe.qingu.nickel.nbt.path.NBTPathReader;
import moe.qingu.nickel.nbt.path.NBTPathScanner;
import moe.qingu.nickel.nbt.path.NBTPath;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public class NBTPathNode extends ParameterNode<NBTPath> {
    public NBTPathNode(@Nonnull final String name) {
        super(name);
    }

    @Override
    public boolean accepts(@Nonnull final InputReader input) throws CommandException {
        return Acceptor.REQUIRE_ONE_TOKEN.check(this,input);
    }

    @Override
    public NBTPath parse(@Nonnull final InputReader input) throws CommandException {
        input.skipWhitespaces();
        return NBTPathReader.readPathFromInput(input);
    }

    @Override
    public void scan(@Nonnull final InputReader input) throws CommandException, NickelScanEOFSignal {
        input.skipWhitespaces();
        NBTPathScanner.scanPathFromInput(input);
    }

    @Nonnull
    @Override
    public Class<NBTPath> getTypeClass() {
        return NBTPath.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.nbt.path";
    }
}
