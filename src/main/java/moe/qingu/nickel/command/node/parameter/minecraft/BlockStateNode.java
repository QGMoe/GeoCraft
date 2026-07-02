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

package moe.qingu.nickel.command.node.parameter.minecraft;

import moe.qingu.nickel.command.node.parameter.TokenizeParameterNode;
import moe.qingu.nickel.command.suggestor.TokenizeSuggestor;
import moe.qingu.nickel.command.suggestor.Suggestor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QiguaiAAAA
 */
public class BlockStateNode extends TokenizeParameterNode.Single<IBlockState> {
    public static final Suggestor<IBlockState> FULL_SUGGESTOR = TokenizeSuggestor.of((arg, context) -> {
        if(arg.isEmpty()) return BlockSelectorNode.DEFAULT_SUGGESTOR.getData();
        if(arg.endsWith("[")){
            final String blockName = arg.substring(0,arg.length()-1);
            final Block block = Block.REGISTRY.getObject(new ResourceLocation(blockName));
            if(block == Blocks.AIR) return null;
            final List<String> suggests = block.getBlockState().getProperties().stream()
                    .map(property -> arg+property.getName()+"=")
                    .collect(Collectors.toList());
            suggests.add(arg+"default]");
            return suggests.stream();
        }else if(arg.endsWith("]")){
            return Stream.empty();
        }else if(arg.endsWith("[default")){
            return Stream.of(arg+"]");
        }else if(arg.contains("[")){
            return suggestProperties(arg);
        }else if(Block.REGISTRY.containsKey(new ResourceLocation(arg))){
            final List<String> suggests = Block.REGISTRY.getKeys().stream().map(Objects::toString).collect(Collectors.toList());
            suggests.add(arg+"[");
            return suggests.stream();
        }
        return BlockSelectorNode.DEFAULT_SUGGESTOR.getData();
    });

    public BlockStateNode(@Nonnull final String name) {
        super(name);
        setSuggestProvider(FULL_SUGGESTOR);
    }

    @Nonnull
    @Override
    public Class<IBlockState> getTypeClass() {
        return IBlockState.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.block_state";
    }

    @Override
    public IBlockState parse(@Nonnull final String arg, @Nonnull final CommandContext context) throws CommandException {
        if(arg.endsWith("[")) throw new NickelSyntaxException(currentBranch,this,
                new TextComponentTranslation("nickel.command.parameter.block_state.invalid_end", arg));
        final String[] split = arg.split("\\[",2);
        if(split.length == 0) throw new NickelSyntaxException(currentBranch,this);
        if(split.length == 1) return CommandBase.getBlockByText(context.getSender(), arg).getDefaultState();
        if(!split[1].endsWith("]")) throw new NickelSyntaxException(currentBranch,this,
                new TextComponentTranslation("nickel.command.parameter.block_state.invalid_end", arg));
        return CommandBase.convertArgToBlockState(CommandBase.getBlockByText(context.getSender(),split[0]),split[1].substring(0,split[1].length()-1));

    }

    @Override
    public boolean checkValid(@Nonnull final String arg, @Nonnull final CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        return true;
    }

    @Nullable
    protected static Stream<String> suggestProperties(final @Nonnull String input){
        final String[] split = input.split("\\[",2);
        final Block block = Block.REGISTRY.getObject(new ResourceLocation(split[0]));
        if(block == Blocks.AIR) return null;
        final String[] states = split[1].split(",");
        if(input.endsWith(",")){
            final Set<String> inputProperties = new HashSet<>();
            for(final String s:states){
                final String[] propertyPair = s.split("=",2);
                inputProperties.add(propertyPair[0]);
            }
            return block.getBlockState().getProperties().stream()
                    .filter(property -> !inputProperties.contains(property.getName()))
                    .map(property -> input+property.getName()+"=");
        }else if(!states[states.length-1].contains("=")){
            final Set<String> inputProperties = new HashSet<>();
            for(int i=0;i<states.length-1;i++){
                final String[] propertyPair = states[i].split("=",2);
                inputProperties.add(propertyPair[0]);
            }
            final int curPropertyBegin = input.contains(",")?input.lastIndexOf(",")+1:input.lastIndexOf("[");
            final String baseInput = input.substring(0,curPropertyBegin);
            final String curInputProperty = input.substring(curPropertyBegin);
            final List<String> suggests = block.getBlockState().getProperties().stream()
                    .filter(property -> !inputProperties.contains(property.getName()))
                    .map(property -> baseInput+property.getName()+"=")
                    .collect(Collectors.toList());
            if(block.getBlockState().getProperty(curInputProperty) != null){
                suggests.add(input+"=");
            }
            return suggests.stream();
        }else {
            final String[] propertyPair = states[states.length-1].split("=",2);
            final IProperty<?> property = block.getBlockState().getProperty(propertyPair[0]);
            if(property==null) return null;
            final String baseInput = propertyPair.length>1?input.substring(0,input.length()-propertyPair[1].length()):input;
            final List<String> suggests = property.getAllowedValues().stream()
                    .sorted()
                    .map(value -> baseInput+value.toString())
                    .collect(Collectors.toList());
            if(propertyPair.length>1 && property.getAllowedValues().stream().map(Objects::toString).collect(Collectors.toSet()).contains(propertyPair[1])){
                suggests.add(input+"]");
                suggests.add(input+",");
            }
            return suggests.stream();
        }
    }
}
