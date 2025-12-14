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

package top.qiguaiaaaa.geocraft.api.command.builder;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public class CommandBuilder {
    public static final Function<CommandContext,Boolean> PERMIT_ALL = context -> Boolean.TRUE,
    REJECT_ALL = context -> Boolean.FALSE;

    public CommandBuilder(@Nonnull String name){
        this.name = name;
    }
    public CommandBuilder(){}

    protected String name;
    protected final List<String> aliases = new ArrayList<>();
    protected ICommandNode rootNode;
    protected Nodes.INodeBuilder<?> rootNodeBuilder;
    protected BiFunction<MinecraftServer, ICommandSender,Boolean> funcPermissionCheck;
    protected int requiredPermissionLevel = 0;


    @Nonnull
    public CommandBuilder setCommandName(@Nonnull String name){
        this.name = name.trim();
        return this;
    }

    @Nonnull
    public CommandBuilder setRootNode(@Nonnull ICommandNode rootNode) {
        this.rootNode = rootNode;
        return this;
    }

    @Nonnull
    public CommandBuilder setRootNode(@Nonnull Nodes.INodeBuilder<?> rootNodeBuilder){
        this.rootNodeBuilder = rootNodeBuilder;
        return this;
    }

    @Nonnull
    public CommandBuilder requirePermissionLevel(int requiredPermissionLevel) {
        this.requiredPermissionLevel = requiredPermissionLevel;
        return this;
    }

    @Nonnull
    public CommandBuilder permitIf(@Nullable BiFunction<MinecraftServer, ICommandSender, Boolean> funcPermissionCheck) {
        this.funcPermissionCheck = funcPermissionCheck;
        return this;
    }

    @Nonnull
    public CommandBuilder addAlias(@Nonnull String alias){
        if(alias.trim().isEmpty()) throw new IllegalArgumentException(alias);
        aliases.add(alias);
        return this;
    }

    @Nonnull
    public ICommand build(){
        final BuiltCommand command = new BuiltCommand();
        command.name = name;
        command.aliases = aliases;
        command.node = rootNode==null?rootNodeBuilder.build():rootNode;
        command.permissionLevel = requiredPermissionLevel;
        command.funcCheckPermission = funcPermissionCheck == null?((server, sender) -> sender.canUseCommand(requiredPermissionLevel,name)):funcPermissionCheck;
        return command;
    }
}
