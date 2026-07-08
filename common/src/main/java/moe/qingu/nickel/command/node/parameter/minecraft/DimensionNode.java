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

package moe.qingu.nickel.command.node.parameter.minecraft;

import moe.qingu.nickel.command.node.parameter.TokenizeParameterNode;
import moe.qingu.nickel.command.suggestor.SerialiseSuggestor;
import moe.qingu.nickel.command.suggestor.Suggestor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import moe.qingu.nickel.command.context.CommandContext;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QiguaiAAAA
 */
public class DimensionNode extends TokenizeParameterNode.Single<World> {
    public static final DefaultParser<World> DEFAULT_PARSER = (node, context) -> context.getWorld();
    public static final Suggestor<World> DEFAULT_SUGGESTOR = SerialiseSuggestor.of(DimensionManager.getWorlds());

    public DimensionNode(@Nonnull final String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Override
    public World parse(@Nonnull final String token, @Nonnull final CommandContext context) throws CommandException {
        final int dimension = CommandBase.parseInt(token);
        final World world = DimensionManager.getWorld(dimension);
        if(world == null) return context.input.panic(translation("nickel.command.parameter.dimension.not_found",dimension));
        return world;
    }

    @Nonnull
    @Override
    public Class<World> getTypeClass() {
        return World.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.dimension";
    }

    @Override
    public String serialise(@Nonnull final World world) {
        return String.valueOf(world.provider.getDimension());
    }

    @Override
    public boolean accepts(@Nonnull final String arg, @Nonnull final CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        CommandBase.parseInt(arg);
        return true;
    }
}
