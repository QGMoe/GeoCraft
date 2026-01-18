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

package top.qiguaiaaaa.geocraft.api.command.builder.literal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import top.qiguaiaaaa.geocraft.api.command.builder.NoSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.literal.LiteralNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static top.qiguaiaaaa.geocraft.api.command.node.functional.PermitNode.*;

/**
 * @author QiguaiAAAA
 */
public class LiteralNodeBuilder extends NoSplitNodeBuilder<LiteralNode,LiteralNodeBuilder> {
    @SuppressWarnings("deprecation")
    protected static final BiConsumer<LiteralNodeBuilder,SmartSplitNodeBuilder.Inner<LiteralNodeBuilder>> ON_SMART_DONE =
            (self, smart) -> self.bakedChildNode = smart.build();
    protected @Nullable BiPredicate<List<String>, CommandContext> matchChecker;
    private final @Nonnull String name;
    protected Predicate<CommandContext> funcCheckPermission = PERMIT_ALL;
    protected int requiredPermissionLevel = 0;
    protected Set<PermissionAPINodeInnerBuilder> requiredPermissions = null;
    protected boolean autoRegisterMissingPermissions = false;
    protected boolean passIfNotPlayer = true;

    public LiteralNodeBuilder(@Nonnull final String name) {
        this.name = name.trim();
        if(this.name.contains(" ") || this.name.isEmpty()) throw new IllegalArgumentException();
    }

    @Nonnull
    public LiteralNodeBuilder require(final int requiredPermissionLevel) {
        this.requiredPermissionLevel = requiredPermissionLevel;
        return this;
    }

    @Nonnull
    public LiteralNodeBuilder require(@Nonnull final Predicate<CommandContext> funcCheckPermission) {
        if(this.funcCheckPermission == REJECT_ALL) return this;
        this.funcCheckPermission = combinePredicates(this.funcCheckPermission,funcCheckPermission);
        return this;
    }

    @Nonnull
    public PermissionAPINodeInnerBuilder require(@Nonnull final String permissionNode){
        return require(new PermissionAPINodeInnerBuilder(permissionNode));
    }

    @Nonnull
    public PermissionAPINodeInnerBuilder require(@Nonnull final PermissionAPINodeInnerBuilder builder){
        if(this.requiredPermissions == null) this.requiredPermissions = new HashSet<>();
        this.requiredPermissions.add(builder);
        return builder;
    }

    @Nonnull
    public LiteralNodeBuilder requirePlayer(final boolean needPlayer){
        this.passIfNotPlayer = !needPlayer;
        return this;
    }

    @Nonnull
    public LiteralNodeBuilder registerMissingPermissions(){
        this.autoRegisterMissingPermissions = true;
        return this;
    }

    /**
     * 设置该字面量节点作为智能节点的匹配函数。
     * @see ISmartNode#match(List, CommandContext)
     * @param matcher 一个匹配函数，用于智能分支节点根据尚未解析的参数和命令上下文匹配当前节点
     * @return {@link LiteralNodeBuilder} 自身
     */
    @Nonnull
    public LiteralNodeBuilder matchIf(@Nullable BiPredicate<List<String>, CommandContext> matcher) {
        this.matchChecker = matcher;
        return this;
    }

    @Nonnull
    public SmartSplitNodeBuilder.Inner<LiteralNodeBuilder> smart(){
        ensureFirstChild();
        return new SmartSplitNodeBuilder.Inner<>(this,ON_SMART_DONE);
    }

    @Nonnull
    @Override
    public LiteralNode build() {
        final LiteralNode node = new LiteralNode(name);
        node.setChecker(buildPermitPredicate());
        node.setChildNode(buildChildNode());
        node.setMatcher(matchChecker);
        return node;
    }

    // PROTECT AREA

    @Nonnull
    protected Predicate<CommandContext> buildPermitPredicate(){
        if(funcCheckPermission == REJECT_ALL) return REJECT_ALL;
        Predicate<CommandContext> permitFunc = funcCheckPermission;
        if(requiredPermissionLevel>=0){
            permitFunc = combinePredicates(permitFunc,buildPermitByLevel());
        }
        if(requiredPermissions != null){
            final Set<String> permissions = requiredPermissions.stream().map(s -> s.node).collect(Collectors.toSet());
            final Predicate<CommandContext> checkNodePermissions = ctx -> {
                for(String node:permissions){
                    if(!PermissionAPI.hasPermission((EntityPlayer) ctx.getSender(),node)) return false;
                }
                return true;
            };
            final Predicate<CommandContext> bakedChecker = passIfNotPlayer?
                    PASS_IF_NOT_PLAYER.or(checkNodePermissions):PASS_IF_PLAYER.and(checkNodePermissions);
            permitFunc = combinePredicates(permitFunc,bakedChecker);
        }
        return permitFunc;
    }

    @Nonnull
    protected Predicate<CommandContext> buildPermitByLevel(){
        return (ctx) -> ctx.getSender().canUseCommand(requiredPermissionLevel,ctx.getCommand().getName());
    }

    @Nonnull
    protected static Predicate<CommandContext> combinePredicates(@Nonnull final Predicate<CommandContext> current,
                                                                 @Nonnull final Predicate<CommandContext> toBeCombined){
        return current == PERMIT_ALL?toBeCombined:current.and(toBeCombined);
    }

    // INNER BUILDER

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
        public LiteralNodeBuilder register(){
            PermissionAPI.registerNode(node,level,comment);
            return LiteralNodeBuilder.this;
        }

        @Nonnull
        public LiteralNodeBuilder done(){
            return LiteralNodeBuilder.this;
        }
    }
}
