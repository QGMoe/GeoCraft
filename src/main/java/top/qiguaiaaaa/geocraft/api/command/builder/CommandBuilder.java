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

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.node.CommandNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public class CommandBuilder extends NoSplitNodeBuilder<CommandNode,CommandBuilder> {
    public static final BiPredicate<MinecraftServer,ICommandSender> PERMIT_ALL = (server, sender) -> true;
    public static final BiPredicate<MinecraftServer,ICommandSender> REJECT_ALL = (server, sender) -> false;
    public static final BiPredicate<MinecraftServer,ICommandSender> PASS_IF_NOT_PLAYER = (server, sender) -> !(sender instanceof EntityPlayer);
    public static final BiPredicate<MinecraftServer,ICommandSender> PASS_IF_PLAYER = (server, sender) -> sender instanceof EntityPlayer;
    @SuppressWarnings("deprecation")
    protected static final BiConsumer<CommandBuilder,SmartSplitNodeBuilder.Inner<CommandBuilder>> ON_SMART_DONE =
            (self,smart) -> self.bakedChildNode = smart.build();

    protected final String name;
    protected final @Nonnull List<String> aliases = new ArrayList<>();
    protected @Nonnull BiPredicate<MinecraftServer,ICommandSender> funcPermissionCheck = PERMIT_ALL;
    protected int requiredPermissionLevel = 0;
    protected Set<PermissionAPINodeInnerBuilder> requiredPermissions = null;
    protected boolean autoRegisterMissingPermissions = false;
    protected boolean passIfNotPlayer = true;
    protected String usage;

    public CommandBuilder(@Nonnull final String name){
        if(name.contains(" ")) throw new IllegalArgumentException("Command name couldn't contain any whitespaces!");
        this.name = name;
    }

    @Nonnull
    public SmartSplitNodeBuilder.Inner<CommandBuilder> smart(){
        ensureFirstChild();
        return new SmartSplitNodeBuilder.Inner<>(this,ON_SMART_DONE);
    }

    @Nonnull
    public CommandBuilder require(final int requiredPermissionLevel) {
        this.requiredPermissionLevel = requiredPermissionLevel;
        return this;
    }

    @Nonnull
    public CommandBuilder require(@Nonnull final BiPredicate<MinecraftServer,ICommandSender> funcPermissionCheck) {
        if(funcPermissionCheck == REJECT_ALL) this.funcPermissionCheck = REJECT_ALL;
        this.funcPermissionCheck = combinePredicates(this.funcPermissionCheck,funcPermissionCheck);
        return this;
    }

    @Nonnull
    public PermissionAPINodeInnerBuilder require(@Nonnull final String permissionNode){
        final PermissionAPINodeInnerBuilder builder = new PermissionAPINodeInnerBuilder(permissionNode);
        if(this.requiredPermissions == null) this.requiredPermissions = new HashSet<>();
        this.requiredPermissions.add(builder);
        return builder;
    }

    @Nonnull
    public CommandBuilder registerMissingPermissions(){
        this.autoRegisterMissingPermissions = true;
        return this;
    }

    @Nonnull
    public CommandBuilder requirePlayer(final boolean needPlayer){
        this.passIfNotPlayer = !needPlayer;
        return this;
    }

    @Nonnull
    public CommandBuilder addAlias(@Nonnull final String alias){
        if(alias.trim().isEmpty()) throw new IllegalArgumentException(alias);
        aliases.add(alias);
        return this;
    }

    @Nonnull
    public CommandBuilder addAlias(@Nonnull final String... aliases){
        for (String alias : aliases) {
            addAlias(alias);
        }
        return this;
    }

    @Nonnull
    public CommandBuilder addAlias(@Nonnull final List<String> aliases){
        for (String alias : aliases) {
            addAlias(alias);
        }
        return this;
    }

    @Nonnull
    public CommandBuilder usage(@Nonnull final String usage){
        this.usage = usage;
        return this;
    }

    @Nonnull
    @Override
    public CommandNode build(){
        doRegisterPermissions();
        final CommandNode command = new CommandNode(name);
        command.setAliases(aliases.stream().distinct().sorted().collect(Collectors.toList()));
        command.setChildNode(buildChildNode());
        command.setCheckPermissionFunction(buildPermitPredicate());
        if(usage != null) command.setUsage(usage);
        return command;
    }

    protected void doRegisterPermissions(){
        if(!this.autoRegisterMissingPermissions) return;
        for(@Nonnull PermissionAPINodeInnerBuilder builder:requiredPermissions){
            if(PermissionAPI.getPermissionHandler().getRegisteredNodes().contains(builder.node)) continue;
            PermissionAPI.registerNode(builder.node,builder.level,builder.comment);
        }
    }

    @Nonnull
    protected BiPredicate<MinecraftServer,ICommandSender> buildPermitPredicate(){
        if(funcPermissionCheck == REJECT_ALL) return REJECT_ALL;
        BiPredicate<MinecraftServer,ICommandSender> permitFunc = funcPermissionCheck;
        if(requiredPermissionLevel>=0){
            permitFunc = combinePredicates(permitFunc,buildPermitByLevel());
        }
        if(requiredPermissions != null){
            final Set<String> permissions = requiredPermissions.stream().map(s -> s.node).collect(Collectors.toSet());
            final BiPredicate<MinecraftServer,ICommandSender> checkNodePermissions = (server, sender) -> {
                for(String node:permissions){
                    if(!PermissionAPI.hasPermission((EntityPlayer) sender,node)) return false;
                }
                return true;
            };
            final BiPredicate<MinecraftServer,ICommandSender> bakedChecker = passIfNotPlayer?
                    PASS_IF_NOT_PLAYER.or(checkNodePermissions):PASS_IF_PLAYER.and(checkNodePermissions);
            permitFunc = combinePredicates(permitFunc,bakedChecker);
        }
        return permitFunc;
    }

    @Nonnull
    protected BiPredicate<MinecraftServer,ICommandSender> buildPermitByLevel(){
        return (server, sender) -> sender.canUseCommand(requiredPermissionLevel,name);
    }

    @Nonnull
    protected static BiPredicate<MinecraftServer,ICommandSender> combinePredicates(@Nonnull final BiPredicate<MinecraftServer,ICommandSender> current,
                                                                                   @Nonnull final BiPredicate<MinecraftServer,ICommandSender> toBeCombined){
        return current == PERMIT_ALL?toBeCombined:current.and(toBeCombined);
    }

    public class PermissionAPINodeInnerBuilder {
        protected final String node;
        protected DefaultPermissionLevel level = DefaultPermissionLevel.NONE;
        protected String comment = "";

        public PermissionAPINodeInnerBuilder(final @Nonnull String node) {
            this.node = Objects.requireNonNull(node);
        }

        @Nonnull
        public PermissionAPINodeInnerBuilder comment(@Nonnull final String comment){
            this.comment = comment;
            return this;
        }

        @Nonnull
        public PermissionAPINodeInnerBuilder allow(@Nonnull final DefaultPermissionLevel level){
            this.level = level;
            return this;
        }

        @Nonnull
        public CommandBuilder register(){
            PermissionAPI.registerNode(node,level,comment);
            return CommandBuilder.this;
        }

        @Nonnull
        public CommandBuilder done(){
            return CommandBuilder.this;
        }
    }
}
