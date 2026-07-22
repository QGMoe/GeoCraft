/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.geocraft.api.command.node;

import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskRegistry;
import moe.qingu.geocraft.api.fluidphysics.task.IFluidTask;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.parameter.TokenizeParameterNode;
import moe.qingu.nickel.command.suggestor.DirectSuggestor;
import moe.qingu.nickel.command.utils.Acceptor;
import net.minecraft.command.CommandException;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public class FluidTaskNode extends TokenizeParameterNode.Single<IFluidTask> {
    public static final DirectSuggestor<IFluidTask> DEFAULT_SUGGESTOR = DirectSuggestor.of(FluidTaskRegistry.getTasks().keySet().stream().map(Objects::toString));

    public FluidTaskNode(@Nonnull final String name) {
        super(name);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Override
    public String serialise(@Nonnull final IFluidTask task) {
        return Optional.ofNullable(FluidTaskRegistry.getName(task)).map(Object::toString).orElse(":InvalidTask"+task+":");
    }

    @Override
    public boolean accepts(@Nonnull final String token, @Nonnull final CommandContext context) throws CommandException {
        return Acceptor.matchResourceLocation(token,context);
    }

    @Override
    public IFluidTask parse(@Nonnull final String token, @Nonnull final CommandContext context) throws CommandException {
        final IFluidTask task = FluidTaskRegistry.getTaskByName(new ResourceLocation(token));
        if(task == null) throw new NickelSyntaxException(this.currentBranch,this)
                .withAppendix(translation("geocraft.command.fluid_task.not_found",token));
        return task;
    }

    @Nonnull
    @Override
    public Class<IFluidTask> getTypeClass() {
        return IFluidTask.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.geocraft.fluid_task";
    }
}
