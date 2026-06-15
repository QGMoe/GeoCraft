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

package moe.qingu.nickel.command.node;

import moe.qingu.nickel.command.exception.NickelRuntimeException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import moe.qingu.nickel.command.builder.CommandBuilder;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class CommandNode extends NoSplitNode implements ICommand,ICommandNode {
    protected static final String EXAMPLE_USAGE = "example usage";
    protected final String name;
    protected List<String> aliases = Collections.emptyList();
    protected BiPredicate<MinecraftServer,ICommandSender> funcCheckPermission = CommandBuilder.PERMIT_ALL;
    protected String usage = EXAMPLE_USAGE;
    protected CommandBranch branch;

    public CommandNode(final @Nonnull String name){
        this.name = name;
    }

    public void setAliases(@Nonnull final List<String> aliases){
        this.aliases = aliases;
    }

    public void setCheckPermissionFunction(@Nonnull final BiPredicate<MinecraftServer,ICommandSender> function){
        this.funcCheckPermission = function;
    }

    public void setUsage(final @Nonnull String usage) {
        this.usage = usage;
    }

    public void init(){
        if(this.childNode == null) return;
        this.branch = this.childNode.branch();
        this.branch.finish(this);
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull final ICommandSender sender) {
        if(usage == EXAMPLE_USAGE){
            return branch.getDocument().getFormattedText();
        }
        return usage;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return funcCheckPermission.test(server,sender);
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if(childNode == null) return Collections.emptyList();
        final SuggestContext context = new SuggestContext(this,server,sender);
        context.setTargetPos(targetPos);
        final List<String> suggests = childNode.suggest(new LinkedList<>(Arrays.asList(args)),context);
        if(suggests == null) return Collections.emptyList();
        return suggests;
    }

    @Override
    public boolean isUsernameIndex(@Nonnull String[] args, int index) {
        return false;
    }

    @Override
    public void execute(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender, @Nonnull final String[] args) throws CommandException {
        if(childNode == null) return;
        try {
            childNode.execute(new LinkedList<>(Arrays.asList(args)),new ExecuteContext(this,server,sender));
        }catch (final @Nonnull NickelSyntaxException | NickelCommandException | NickelRuntimeException e){
            e.feedbackTo(sender);
        }
    }

    @Override
    public int compareTo(@Nonnull final ICommand o) {
        return getName().compareTo(o.getName());
    }

    // ----------
    //   Node
    // ----------

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(childNode == null) return;
        childNode.execute(args,context);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        return childNode == null?null:childNode.suggest(args,context);
    }
}
