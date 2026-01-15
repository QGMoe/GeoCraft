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

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.EntityEntry;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.CommandRunFunction;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.ExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.RelayExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.ConditionalSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.ForEachNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.PermitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.functional.SmartSplitNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.literal.LiteralsNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.FastParameterNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.StringNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.minecraft.EntitySelectorNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.minecraft.MinecraftVec3NodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.num.NumberNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.node.parament.ParameterNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.forge.EntityEntrySelectorNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.forge.FluidSelectorNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.forge.OreSelectorNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.generic.BooleanNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.generic.UUIDNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.generic.number.NumberNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft.BlockSelectorNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft.ItemSelectorNode;
import top.qiguaiaaaa.geocraft.api.util.oredict.OreDictionaryEntry;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

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
    public static <FROM extends Iterable<TO>,TO> ForEachNodeBuilder<FROM,TO> forEach(@Nonnull final String contextToForEach){
        return new ForEachNodeBuilder<>(contextToForEach);
    }

    @Nonnull
    public static <FROM extends Iterable<TO>,TO> ForEachNodeBuilder<FROM,TO> forEach(@Nonnull final String contextToForEach,
                                                                                     @Nonnull final Class<? extends ParameterNode<FROM>> nodeClass){
        return new ForEachNodeBuilder<>(contextToForEach);
    }

    @Nonnull
    public static SmartSplitNodeBuilder.Outer smart(){
        return new SmartSplitNodeBuilder.Outer();
    }

    @Nonnull
    public static PermitNodeBuilder.Impl permit(){
        return new PermitNodeBuilder.Impl();
    }

    @Nonnull
    public static LiteralsNodeBuilder literals(){
        return new LiteralsNodeBuilder();
    }

    @Nonnull
    public static LiteralsNodeBuilder.LiteralsChoiceInnerBuilder literals(@Nonnull String... names){
        return new LiteralsNodeBuilder().when(names);
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
    public static NumberNodeBuilder<BigInteger,NumberNode<BigInteger>> bigInteger(@Nonnull final String name){
        return NumberType.BIG_INTEGER.create(name);
    }

    @Nonnull
    public static NumberNodeBuilder<BigDecimal,NumberNode<BigDecimal>> bigDecimal(@Nonnull final String name){
        return NumberType.BIG_DECIMAL.create(name);
    }

    @Nonnull
    public static NumberNodeBuilder<Double,NumberNode<Double>> doubleArg(@Nonnull final String name){
        return NumberType.DOUBLE.create(name);
    }

    @Nonnull
    public static NumberNodeBuilder<Long,NumberNode<Long>> longArg(@Nonnull final String name){
        return NumberType.LONG.create(name);
    }

    @Nonnull
    public static NumberNodeBuilder<Long,NumberNode<Long>> longlong(@Nonnull final String name){
        return longArg(name);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<Boolean, BooleanNode> bool(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,BooleanNode::new);
    }

    @Nonnull
    public static StringNodeBuilder string(@Nonnull final String name){
        return new StringNodeBuilder(name);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<UUID, UUIDNode> uuid(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,UUIDNode::new);
    }

    @Nonnull
    public static MinecraftVec3NodeBuilder.BlockPos blockPos(@Nonnull final String name){
        return new MinecraftVec3NodeBuilder.BlockPos(name);
    }

    @Nonnull
    public static MinecraftVec3NodeBuilder.BlockPos vec3i(@Nonnull final String name){
        return blockPos(name);
    }

    @Nonnull
    public static MinecraftVec3NodeBuilder.Vec3d vec3d(@Nonnull final String name){
        return new MinecraftVec3NodeBuilder.Vec3d(name);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<Item, ItemSelectorNode> item(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,ItemSelectorNode::new);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<Block, BlockSelectorNode> block(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,BlockSelectorNode::new);
    }

    @Nonnull
    public static EntitySelectorNodeBuilder entity(@Nonnull final String name){
        return new EntitySelectorNodeBuilder(name).asSingle();
    }

    @Nonnull
    public static EntitySelectorNodeBuilder entity(@Nonnull final String name, @Nonnull final Class<? extends Entity> targetClass){
        return new EntitySelectorNodeBuilder(name).asSingle().target(targetClass);
    }

    @Nonnull
    public static EntitySelectorNodeBuilder entities(@Nonnull final String name){
        return new EntitySelectorNodeBuilder(name);
    }

    @Nonnull
    public static EntitySelectorNodeBuilder entities(@Nonnull final String name,@Nonnull final Class<? extends Entity> targetClass){
        return new EntitySelectorNodeBuilder(name).target(targetClass);
    }

    @Nonnull
    public static EntitySelectorNodeBuilder player(@Nonnull final String name){
        return entity(name,EntityPlayer.class);
    }

    @Nonnull
    public static EntitySelectorNodeBuilder players(@Nonnull final String name){
        return entities(name,EntityPlayer.class);
    }

    // Forge

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<EntityEntry, EntityEntrySelectorNode> entityEntry(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,EntityEntrySelectorNode::new);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<Fluid, FluidSelectorNode> fluid(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,FluidSelectorNode::new);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<OreDictionaryEntry, OreSelectorNode> ore(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,OreSelectorNode::new);
    }

    // GeoCraft

    @Nonnull
    public static MinecraftVec3NodeBuilder.AtmosphereAccessor atmosphere(@Nonnull final String name){
        return new MinecraftVec3NodeBuilder.AtmosphereAccessor(name);
    }

}
