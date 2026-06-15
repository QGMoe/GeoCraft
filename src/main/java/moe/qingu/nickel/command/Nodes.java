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

package moe.qingu.nickel.command;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.EntityEntry;
import moe.qingu.nickel.command.builder.execute.CommandExecutor;
import moe.qingu.nickel.command.builder.execute.ExecuteNodeBuilder;
import moe.qingu.nickel.command.builder.execute.RelayExecuteNodeBuilder;
import moe.qingu.nickel.command.builder.execute.SimpleCommandExecutor;
import moe.qingu.nickel.command.builder.functional.ConditionalSplitNodeBuilder;
import moe.qingu.nickel.command.builder.functional.ForEachNodeBuilder;
import moe.qingu.nickel.command.builder.functional.PermitNodeBuilder;
import moe.qingu.nickel.command.builder.functional.RunCommandNodeBuilder;
import moe.qingu.nickel.command.builder.functional.SmartSplitNodeBuilder;
import moe.qingu.nickel.command.builder.literal.LiteralNodeBuilder;
import moe.qingu.nickel.command.builder.literal.LiteralsNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.FastParameterNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.StringNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.minecraft.EntitySelectorNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.minecraft.ItemStackNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.minecraft.MinecraftVec3NodeBuilder;
import moe.qingu.nickel.command.builder.parameter.num.NumberNodeBuilder;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import moe.qingu.nickel.command.node.parameter.forge.EntityEntrySelectorNode;
import moe.qingu.nickel.command.node.parameter.forge.FluidSelectorNode;
import moe.qingu.nickel.command.node.parameter.forge.OreSelectorNode;
import moe.qingu.nickel.command.node.parameter.generic.BooleanNode;
import moe.qingu.nickel.command.node.parameter.generic.UUIDNode;
import moe.qingu.nickel.command.node.parameter.generic.number.NumberNode;
import moe.qingu.nickel.command.node.parameter.minecraft.BlockSelectorNode;
import moe.qingu.nickel.command.node.parameter.minecraft.BlockStateNode;
import moe.qingu.nickel.command.node.parameter.minecraft.DimensionNode;
import moe.qingu.nickel.command.node.parameter.minecraft.ItemSelectorNode;
import moe.qingu.nickel.command.node.parameter.minecraft.ItemStackNode;
import moe.qingu.nickel.util.oredict.OreDictionaryEntry;

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
    public static LiteralsNodeBuilder.LiteralsChoiceInnerBuilder literals(@Nonnull final String... names){
        return new LiteralsNodeBuilder().when(names);
    }

    @Nonnull
    public static LiteralNodeBuilder literal(@Nonnull String name){
        return new LiteralNodeBuilder(name);
    }

    @Nonnull
    public static ExecuteNodeBuilder.Impl execute(){
        return new ExecuteNodeBuilder.Impl();
    }

    @Nonnull
    public static ExecuteNodeBuilder.Impl execute(@Nonnull final CommandExecutor func){
        return execute().run(func);
    }

    @Nonnull
    public static ExecuteNodeBuilder.Impl execute(@Nonnull final SimpleCommandExecutor func){
        return execute().run(func);
    }

    @Nonnull
    public static RunCommandNodeBuilder.Redirect runCommand(@Nonnull final String commandName){
        return new RunCommandNodeBuilder.Redirect(commandName);
    }

    @Nonnull
    public static RunCommandNodeBuilder runCommands(){
        return new RunCommandNodeBuilder();
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(){
        return new RelayExecuteNodeBuilder();
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final CommandExecutor func){
        return relay().run(func);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final SimpleCommandExecutor func){
        return relay((CommandExecutor) func);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final CommandExecutor func, @Nonnull final CommandExecutor funcAfter){
        return relay().run(func).after(funcAfter);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final SimpleCommandExecutor func,@Nonnull final CommandExecutor funcAfter){
        return relay((CommandExecutor) func,funcAfter);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final SimpleCommandExecutor func,@Nonnull final SimpleCommandExecutor funcAfter){
        return relay((CommandExecutor) func,(CommandExecutor) funcAfter);
    }

    @Nonnull
    public static RelayExecuteNodeBuilder relay(@Nonnull final CommandExecutor func,@Nonnull final SimpleCommandExecutor funcAfter){
        return relay(func,(CommandExecutor) funcAfter);
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

    // Minecraft

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
    public static ItemStackNodeBuilder itemStack(@Nonnull final String name){
        return new ItemStackNodeBuilder(name);
    }

    @Nonnull
    public static NumberNodeBuilder<Integer,NumberNode<Integer>> itemStack$count(@Nonnull final String itemStackNodeName){
        return integer(ParameterNode.getInnerParameterName(itemStackNodeName,"count"))
                .min(1)
                .decorate((count,context) ->{
                    context.get(itemStackNodeName,ItemStackNode.class).setCount(count);
                    return count;
                });
    }

    @Nonnull
    public static NumberNodeBuilder<Integer,NumberNode<Integer>> itemStack$meta(@Nonnull final String itemStackNodeName){
        return integer(ParameterNode.getInnerParameterName(itemStackNodeName,"meta"))
                .min(0)
                .max((int) Short.MAX_VALUE)
                .decorate((meta,context)->{
                    context.get(itemStackNodeName,ItemStackNode.class).setItemDamage(meta);
                    return meta;
                });
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<Block, BlockSelectorNode> block(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,BlockSelectorNode::new);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<IBlockState, BlockStateNode> blockState(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name, BlockStateNode::new);
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<IBlockState, BlockStateNode> block$blockState(@Nonnull final String blockNodeName,@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name, s -> new BlockStateNode(blockNodeName,s));
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

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<World, DimensionNode> dimension(@Nonnull final String name){
        return new FastParameterNodeBuilder.FastSmart<>(name,DimensionNode::new);
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
}
