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

package top.qiguaiaaaa.nickel.command.node.functional;

import com.google.common.base.Throwables;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import top.qiguaiaaaa.nickel.command.context.CommandContext;
import top.qiguaiaaaa.nickel.command.context.ExecuteContext;
import top.qiguaiaaaa.nickel.command.context.SuggestContext;
import top.qiguaiaaaa.nickel.command.node.ICommandNode;
import top.qiguaiaaaa.nickel.command.node.IDocumentaryNode;
import top.qiguaiaaaa.nickel.command.utils.CommandBranch;
import top.qiguaiaaaa.nickel.event.NickelCommandEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author QiguaiAAAA
 */
public class RunCommandNode implements ICommandNode{
    protected static final String[] EmptyStringArr = new String[0];

    protected BiFunction<ICommand,ExecuteContext, ICommandSender> modifier;

    public void setModifier(final @Nullable BiFunction<ICommand, ExecuteContext, ICommandSender> modifier) {
        this.modifier = modifier;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(args.isEmpty()) throw new SyntaxErrorException();
        final String commandName = args.getFirst();
        try {
            args.pop();
            final ICommand icommand = getCommand(commandName,context);
            runCommand(icommand,args,context);
        }finally {
            args.addFirst(commandName);
        }
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context) {
        if(args.isEmpty()){
            return context.getServer().getCommandManager().getCommands().keySet().stream().sorted().collect(Collectors.toList());
        }else if(args.size() == 1){
            return context.getServer().getCommandManager().getCommands().keySet().stream()
                    .filter(s -> s.startsWith(args.getFirst()))
                    .sorted()
                    .collect(Collectors.toList());
        }else{
            final String commandName = args.getFirst();
            try {
                args.pop();
                final ICommand icommand = context.getServer().getCommandManager().getCommands().get(args.getFirst());
                if(icommand == null) return null;
                return getCommandSuggest(icommand,args,context);
            }finally {
                args.addFirst(commandName);
            }
        }
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        final CommandBranch branch = new CommandBranch();
        branch.appendDocument(new TextComponentString(String.format(IDocumentaryNode.REQUIRED_FORMAT, "command")));
        return branch;
    }

    protected <T extends List<String> & Deque<String>> void runCommand(@Nonnull final ICommand command,@Nonnull final T args,@Nonnull final ExecuteContext context) throws CommandException {
        final @Nonnull ICommandSender sender = modifier == null?context.getSender():modifier.apply(command,context);

        if(!command.checkPermission(context.getServer(),sender)){
            TextComponentTranslation noPermissionInfo = new TextComponentTranslation("commands.generic.permission");
            noPermissionInfo.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(noPermissionInfo);
            return;
        }

        @Nonnull final String[] rawArgs = postGeoCommandEvent(command,context,args.toArray(EmptyStringArr));

        final int usernameIndex = getUsernameIndex(command, rawArgs);

        if (usernameIndex > -1) {
            final List<Entity> list = EntitySelector.matchEntities(sender, rawArgs[usernameIndex], Entity.class);
            final String rawSelector = rawArgs[usernameIndex];

            if (list.isEmpty()) {
                throw new PlayerNotFoundException("commands.generic.selector.notFound", rawArgs[usernameIndex]);
            }

            for (final @Nonnull Entity entity : list) {
                rawArgs[usernameIndex] = entity.getCachedUniqueIdString();
                command.execute(context.getServer(), sender,rawArgs);
            }

            rawArgs[usernameIndex] = rawSelector;
        } else {
            command.execute(context.getServer(),sender,rawArgs);
        }
    }

    @Nonnull
    protected static String[] postGeoCommandEvent(@Nonnull final ICommand command,@Nonnull final ExecuteContext context,@Nonnull final String[] rawArgs){
        final @Nonnull NickelCommandEvent event = new NickelCommandEvent(command,context, rawArgs);
        if (MinecraftForge.EVENT_BUS.post(event) && event.getException() != null) {
            Throwables.throwIfUnchecked(event.getException());
        }
        if (event.getParameters() != null) return event.getParameters();
        return rawArgs;
    }

    @Nullable
    protected static <T extends List<String> & Deque<String>> List<String> getCommandSuggest(@Nonnull final ICommand command,@Nonnull final T args, @Nonnull final SuggestContext context){
        if(command instanceof ICommandNode){
            return ((ICommandNode)command).suggest(args,context);
        }else {
            return command.getTabCompletions(context.getServer(),context.getSender(),args.toArray(EmptyStringArr),context.getTargetPos());
        }
    }

    @Nonnull
    protected static ICommand getCommand(@Nonnull final String name, @Nonnull final CommandContext context) throws CommandNotFoundException {
        final ICommand command = context.getServer().getCommandManager().getCommands().get(name);
        if (command == null) throw new CommandNotFoundException();
        return command;
    }

    protected static int getUsernameIndex(final ICommand command,final @Nonnull String[] args) throws CommandException {
        if (command != null) {
            for (int i = 0; i < args.length; ++i) {
                if (command.isUsernameIndex(args, i) && EntitySelector.matchesMultiplePlayers(args[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
}
