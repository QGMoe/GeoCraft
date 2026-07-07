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

package moe.qingu.nickel.command.node.parameter.generic;

import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import moe.qingu.nickel.command.suggestor.SerialiseSuggestor;
import moe.qingu.nickel.command.utils.Claimer;
import net.minecraft.command.CommandException;
import moe.qingu.nickel.command.utils.Acceptor;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public class BooleanNode extends ParameterNode<Boolean> {
    public static final DefaultParser<Boolean> DEFAULT_PARSER = (node, context) -> Boolean.FALSE;
    public static final Claimer DEFAULT_CLAIMER = input -> {
        try {
            input.readBoolean();
            return true;
        }catch (final CommandException e){
            return false;
        }
    };

    public static final SerialiseSuggestor<Boolean> DEFAULT_SUGGESTOR = SerialiseSuggestor.of(true,false);

    public BooleanNode(@Nonnull final String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
        setClaimer(DEFAULT_CLAIMER);
    }

    @Nonnull
    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.generic.boolean";
    }

    @Override
    public Boolean parse(@Nonnull final InputReader input) throws CommandException {
        return input.readBoolean();
    }

    @Override
    public void scan(@Nonnull final InputReader input) {
        input.skipWhitespaces();
        input.skipContents();
    }

    @Override
    public boolean accepts(@Nonnull final InputReader input) throws CommandException {
        if(!Acceptor.REQUIRE_ONE_TOKEN.check(this,input)) return false;
        input.readBoolean();
        return true;
    }
}
