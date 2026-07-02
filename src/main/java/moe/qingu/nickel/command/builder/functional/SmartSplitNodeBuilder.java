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

package moe.qingu.nickel.command.builder.functional;

import moe.qingu.nickel.command.reader.InputReader;
import moe.qingu.nickel.command.utils.Matcher;
import net.minecraft.command.SyntaxErrorException;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import moe.qingu.nickel.command.Nodes;
import moe.qingu.nickel.command.builder.INodeBuilder;
import moe.qingu.nickel.command.builder.execute.CommandExecutor;
import moe.qingu.nickel.command.builder.literal.LiteralNodeBuilder;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.node.functional.SmartSplitNode;
import moe.qingu.nickel.command.node.literal.LiteralNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * {@link SmartSplitNode}的构建器
 * @author QiguaiAAAA
 */
public abstract class SmartSplitNodeBuilder<S extends SmartSplitNodeBuilder<S>> {

    public static final CommandExecutor DEFAULT_EXECUTE_FUNC_CHECKER = ctx -> {
        if(!ctx.getInput().isRemainingEmpty()) throw new SyntaxErrorException();
    };

    protected List<SmartNodeInnerBuilder<?>> smarts = new ArrayList<>();
    protected ICommandNode bakedDefault;
    protected INodeBuilder<?> defaultBuilder;

    private SmartSplitNodeBuilder(){}

    /**
     * 向{@link SmartSplitNode}添加下一个智能节点
     * @param builder 智能节点的构建器
     * @return 一个 {@link SmartNodeInnerBuilder}。
     * 该构建器没有实现 {@link INodeBuilder<LiteralNode>}，以防止不小心将该构建器直接连接到父构建器的下方。也就是说，下方的写法是不允许的：
     * <blockquote><pre> then(smart().then(一个构建器)) </pre></blockquote>
     * 请在构建完这个子节点后调用 {@link SmartNodeInnerBuilder#done()} 方法以返回 {@link SmartSplitNodeBuilder}。即：
     * <blockquote><pre> then(smart().then(一个构建器).done()) </pre></blockquote>
     * @param <T> 添加个节点类型，必须是 {@link ISmartNode} 的实现类
     */
    @Nonnull
    public <T extends ISmartNode> SmartNodeInnerBuilder<T> append(@Nonnull final INodeBuilder<T> builder){
        final SmartNodeInnerBuilder<T> b = new SmartNodeInnerBuilder<>(Objects.requireNonNull(builder));
        smarts.add(b);
        return b;
    }

    @Nonnull
    public <T extends ISmartNode> SmartNodeInnerBuilder<T> append(@Nonnull final T node){
        final SmartNodeInnerBuilder<T> b = new SmartNodeInnerBuilder<>(node);
        smarts.add(b);
        return b;
    }

    /**
     * 向{@link SmartSplitNode}添加下一个单字面量节点
     * @param val 字面量
     * @return 专门用于在 {@link SmartSplitNodeBuilder} 中快速添加字面量节点的 {@link LiteralNodeInnerBuilder} 构建器的一个实例，
     * 该构建器没有实现 {@link INodeBuilder<LiteralNode>}，以防止不小心将该构建器直接连接到父构建器的下方。也就是说，下方的写法是不允许的：
     * <blockquote><pre> then(smart().literal("foo")) </pre></blockquote>
     * 请在构建完这个字面量节点后调用 {@link LiteralNodeInnerBuilder#done()} 方法以返回 {@link SmartSplitNodeBuilder}。即：
     * <blockquote><pre> then(smart().literal("foo").done()) </pre></blockquote>
     */
    @Nonnull
    public LiteralNodeInnerBuilder literal(@Nonnull final String val){
        final LiteralNodeInnerBuilder b = new LiteralNodeInnerBuilder(Objects.requireNonNull(val));
        smarts.add(b);
        return b;
    }

    /**
     * 定义当{@link SmartSplitNode} 的所有智能节点匹配失败时，默认运行的逻辑。
     * 该方法会将默认节点设置为一个执行节点，并覆盖在此之前执行的 {@link #defaultAs(INodeBuilder)} 定义的构建器。
     * @param func 默认运行的逻辑。
     * @return {@link SmartSplitNodeBuilder}自身
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public S execute(@Nonnull final CommandExecutor func){
        defaultBuilder = Nodes.execute(DEFAULT_EXECUTE_FUNC_CHECKER.then(func));
        return (S) this;
    }

    /**
     * 定义当{@link SmartSplitNode} 的所有智能节点匹配失败时，默认的子节点，可以是任意 {@link ICommandNode}。
     * 该方法会覆盖 {@link #defaultAs(INodeBuilder)} 和 {@link #execute(CommandExecutor)}
     * @param node 默认子节点
     * @return {@link SmartSplitNodeBuilder}自身
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public S defaultAs(@Nonnull final ICommandNode node){
        bakedDefault = node;
        return (S) this;
    }

    /**
     * 定义当{@link SmartSplitNode} 的所有智能节点匹配失败时，默认的子节点，可以是任意 {@link ICommandNode} 的构建器 {@link INodeBuilder}。
     * 该方法会覆盖在此之前执行的 {@link #execute(CommandExecutor)}
     * @param node 默认子节点的构建器
     * @return {@link SmartSplitNodeBuilder}自身
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public S defaultAs(@Nonnull final INodeBuilder<?> node){
        defaultBuilder = node;
        return (S) this;
    }

    @Nonnull
    public SmartSplitNode build() {
        final SmartSplitNode node = new SmartSplitNode();
        smarts.forEach(builder -> node.addSmartNode(builder.build()));
        if(bakedDefault != null) node.setChildNode(bakedDefault);
        else if(defaultBuilder != null) node.setChildNode(defaultBuilder.build());
        return node;
    }

    public class SmartNodeInnerBuilder<T extends ISmartNode>{
        final INodeBuilder<? extends T> child;
        final T bakedChild;

        Matcher checker;

        SmartNodeInnerBuilder(@Nonnull final INodeBuilder<? extends T> nodeBuilder){
            this.child = nodeBuilder;
            this.bakedChild = null;
        }

        SmartNodeInnerBuilder(@Nonnull final T bakedChild){
            this.child = null;
            this.bakedChild = bakedChild;
        }

        /**
         * 设置当前智能节点的匹配函数。
         * @see ISmartNode#match(InputReader)
         * @param checker 一个匹配函数，用于智能分支节点根据尚未解析的参数和命令上下文匹配当前节点
         * @return {@link SmartNodeInnerBuilder} 自身
         */
        @Nonnull
        public SmartNodeInnerBuilder<T> matchIf(@Nonnull final Matcher checker){
            this.checker = checker;
            return this;
        }

        /**
         * 完成当前子节点的构建，并返回智能分支节点的构建。
         * @return 返回最初的 {@link SmartSplitNodeBuilder} 实例
         */
        @Nonnull
        @SuppressWarnings("unchecked")
        public S done(){
            return (S) SmartSplitNodeBuilder.this;
        }

        @Nonnull
        T build() {
            final T bakedNode = bakedChild==null?Objects.requireNonNull(child).build():bakedChild;
            if(checker != null){
                bakedNode.setMatcher(checker);
            }
            return bakedNode;
        }
    }

    public class LiteralNodeInnerBuilder extends SmartNodeInnerBuilder<LiteralNode> {

        protected final LiteralNodeBuilder literalBuilder;

        LiteralNodeInnerBuilder(@Nonnull final String name) {
            super(new LiteralNodeBuilder(name));
            literalBuilder = (LiteralNodeBuilder) super.child;
        }

        @Nonnull
        public LiteralNodeInnerBuilder then(@Nonnull final INodeBuilder<?> childNode) {
            literalBuilder.then(childNode);
            return this;
        }

        @Nonnull
        public LiteralNodeInnerBuilder then(@Nonnull final ICommandNode childNode) {
            literalBuilder.then(childNode);
            return this;
        }

        /**
         * @see LiteralNodeBuilder#require(Predicate) )
         * @param funcCheckPermission 权限检查函数
         * @return {@link LiteralNodeInnerBuilder} 自身
         */
        @Nonnull
        public LiteralNodeInnerBuilder require(@Nonnull final Predicate<CommandContext> funcCheckPermission) {
            literalBuilder.require(funcCheckPermission);
            return this;
        }

        @Nonnull
        public LiteralNodeInnerBuilder require(final int requiredPermissionLevel) {
            literalBuilder.require(requiredPermissionLevel);
            return this;
        }

        @Nonnull
        public LiteralInnerPermissionAPINodeInnerBuilder require(@Nonnull final String permissionNode) {
            return new LiteralInnerPermissionAPINodeInnerBuilder(permissionNode);
        }

        @Nonnull
        public LiteralNodeInnerBuilder requirePlayer(final boolean needPlayer){
            literalBuilder.requirePlayer(needPlayer);
            return this;
        }

        @Nonnull
        public LiteralNodeInnerBuilder registerMissingPermissions(){
            this.literalBuilder.registerMissingPermissions();
            return this;
        }

//        @Nonnull
//        public SmartSplitNodeBuilder.Inner<LiteralNodeInnerBuilder> smart(){
//            return new SmartSplitNodeBuilder.Inner<>(this,(self,smart)->self.literalBuilder.then(smart.build()));
//        }

        /**
         * {@inheritDoc}
         * @param checker 一个匹配函数，用于智能分支节点根据尚未解析的参数和命令上下文匹配当前节点
         * @return 返回 {@link LiteralNodeInnerBuilder} 自身
         */
        @Nonnull
        @Override
        public LiteralNodeInnerBuilder matchIf(@Nonnull final Matcher checker) {
            return (LiteralNodeInnerBuilder) super.matchIf(checker);
        }

        public class LiteralInnerPermissionAPINodeInnerBuilder {
            protected final String node;
            protected DefaultPermissionLevel level = DefaultPermissionLevel.NONE;
            protected String comment = "";

            public LiteralInnerPermissionAPINodeInnerBuilder(final @Nonnull String node) {
                this.node = Objects.requireNonNull(node);
            }

            @Nonnull
            public LiteralInnerPermissionAPINodeInnerBuilder comment(@Nonnull final String comment){
                this.comment = comment;
                return this;
            }

            @Nonnull
            public LiteralInnerPermissionAPINodeInnerBuilder allow(@Nonnull final DefaultPermissionLevel level){
                this.level = level;
                return this;
            }

            @Nonnull
            public LiteralNodeInnerBuilder register(){
                PermissionAPI.registerNode(node,level,comment);
                return done();
            }

            @Nonnull
            public LiteralNodeInnerBuilder done(){
                literalBuilder.require(this.node).allow(level).comment(comment).done();
                return LiteralNodeInnerBuilder.this;
            }
        }
    }

    public static class Outer extends SmartSplitNodeBuilder<Outer> implements INodeBuilder<SmartSplitNode>{}

    public static class Inner<Parent> extends SmartSplitNodeBuilder<Inner<Parent>>{

        protected final Parent parentBuilder;
        protected final BiConsumer<Parent, Inner<Parent>> onDone;

        public Inner(@Nonnull final Parent parentBuilder) {
            this.parentBuilder = parentBuilder;
            onDone=null;
        }

        public Inner(@Nonnull final Parent parentBuilder,@Nonnull final BiConsumer<Parent,SmartSplitNodeBuilder.Inner<Parent>> onDoneFunc){
            this.parentBuilder = parentBuilder;
            this.onDone = onDoneFunc;
        }

        @Nonnull
        public Parent done(){
            if(onDone != null) this.onDone.accept(parentBuilder,this);
            return parentBuilder;
        }

        @Deprecated
        @Nonnull
        @Override
        public SmartSplitNode build() {
            return super.build();
        }
    }
}
