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

package top.qiguaiaaaa.geocraft.api.command;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.CommandRunFunction;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.ExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.RelayExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.ConditionalSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.PermitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralsNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.FastParameterNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.ParameterNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.num.NumberNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.node.*;
import top.qiguaiaaaa.geocraft.api.command.node.generic.BooleanNode;
import top.qiguaiaaaa.geocraft.api.command.node.generic.NumberNode;
import top.qiguaiaaaa.geocraft.api.command.node.generic.StringNode;
import top.qiguaiaaaa.geocraft.api.command.node.minecraft.BlockPosNode;
import top.qiguaiaaaa.geocraft.api.command.node.minecraft.ItemSelectorNode;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public final class Nodes {

    private Nodes(){}

    @Nonnull
    public static ConditionalSplitNodeBuilder split(){
        return new ConditionalSplitNodeBuilder();
    }

    @Nonnull
    public static LiteralsNodeBuilder literals(){
        return new LiteralsNodeBuilder();
    }

    @Nonnull
    public static LiteralNodeBuilder literal(@Nonnull String name){
        return new LiteralNodeBuilder(name);
    }

    @Nonnull
    public static ExecuteNodeBuilder execute(){
        return new ExecuteNodeBuilder();
    }

    @Nonnull
    public static ExecuteNodeBuilder execute(@Nonnull final CommandRunFunction func){
        return execute().run(func);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(){
        return new RelayExecuteNodeBuilder();
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final CommandRunFunction func){
        return relay().run(func);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final CommandRunFunction func,@Nonnull final CommandRunFunction funcAfter){
        return relay().run(func).after(funcAfter);
    }

    @Nonnull
    public static <N extends Number> NumberNodeBuilder<N,NumberNode<N>> number(@Nonnull final String name, @Nonnull NumberType<N> type){
        return type.create(name);
    }

    @Nonnull
    public static NumberNodeBuilder<Integer,NumberNode<Integer>> integer(@Nonnull final String name){
        return NumberType.INTEGER.create(name);
    }

    @Nonnull
    public static ParameterNodeBuilder<Boolean, BooleanNode> bool(@Nonnull final String name){
        return new FastParameterNodeBuilder<>(name,BooleanNode::new);
    }

    @Nonnull
    public static ParameterNodeBuilder<String, StringNode> string(@Nonnull final String name){
        return new FastParameterNodeBuilder<>(name,StringNode::new);
    }

    @Nonnull
    public static ParameterNodeBuilder<BlockPos, BlockPosNode> blockPos(@Nonnull final String name){
        return new FastParameterNodeBuilder<>(name,BlockPosNode::new);
    }

    @Nonnull
    public static ParameterNodeBuilder<Item, ItemSelectorNode> selectItem(@Nonnull final String name){
        return new FastParameterNodeBuilder<>(name,ItemSelectorNode::new);
    }

    @Nonnull
    public static ParameterNodeBuilder<IAtmosphereAccessor, AtmosphereAccessorNode> selectAtmosphere(@Nonnull final String name){
        return new FastParameterNodeBuilder<>(name, AtmosphereAccessorNode::new);
    }

    @Nonnull
    public static PermitNodeBuilder permit(){
        return new PermitNodeBuilder();
    }

    @Nonnull
    public static SmartSplitNodeBuilder smart(){
        return new SmartSplitNodeBuilder();
    }

}
