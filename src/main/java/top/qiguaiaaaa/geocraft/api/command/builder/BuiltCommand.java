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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class BuiltCommand extends CommandBase {
    String name;
    List<String> aliases;
    ICommandNode node;
    int permissionLevel;
    BiFunction<MinecraftServer,ICommandSender,Boolean> funcCheckPermission;

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "example usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return permissionLevel;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return funcCheckPermission.apply(server,sender);
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if(node == null) return super.getTabCompletions(server, sender, args, targetPos);
        final SuggestContext context = new SuggestContext(this,server,sender);
        context.setTargetPos(targetPos);
        final List<String> suggests = node.suggest(new LinkedList<>(Arrays.asList(args)),context);
        if(suggests == null) return super.getTabCompletions(server, sender, args, targetPos);
        return suggests;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if(node != null){
            node.execute(new LinkedList<>(Arrays.asList(args)),new ExecuteContext(this,server,sender));
        }
    }
}
