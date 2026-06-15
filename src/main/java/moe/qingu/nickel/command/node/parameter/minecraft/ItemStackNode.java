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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.parameter.SmartParameterNode;
import moe.qingu.nickel.command.utils.Matchers;
import moe.qingu.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author QiguaiAAAA
 */
public class ItemStackNode extends SmartParameterNode<ItemStack> {
    public static final DefaultParser<ItemStack> DEFAULT_PARSER = (node, context) -> new ItemStack(Items.AIR,1);
    public static final BiPredicate<List<String>, CommandContext> MATCHER_WITH_NBT = Matchers.matchOnlyFirstArg((arg,context)->{
        final String[] split = arg.split("\\{",2);
        return split.length == 1 && Matchers.isResourceLocation(arg,context) || split.length == 2 && Matchers.isResourceLocation(split[0],context);
    });

    protected boolean allowNBT = true;
    protected int count = 1;
    protected int meta = 0;

    public ItemStackNode(@Nonnull final String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(ItemSelectorNode.DEFAULT_SUGGESTOR);
        setMatcher(MATCHER_WITH_NBT);
    }

    public void setAllowNBT(final boolean allowNBT) {
        this.allowNBT = allowNBT;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public void setMeta(final int meta) {
        this.meta = meta;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException {
        if(childNode == null) return;
        if(!allowNBT){
            super.execute(args,context);
            return;
        }

        int usedParas = 0;
        if(checkValid(args,context)){
            if(!args.get(0).contains("\\{")){
                usedParas = 1;
                putParsedArgument(parseParameter(args,context),context);
            } else usedParas = parseStackWithNBT(args,context);
        }else if(defaultParser!=null){
            putParsedArgument(defaultParser.parser(this,context),context);
        }else throw new NickelSyntaxException(currentBranch,this,new TextComponentTranslation("nickel.command.parameter.base.default_not_found"));

        this.executeChild(args,context,usedParas);
    }

    @Override
    public int getParametersLength() {
        return allowNBT?-1:1; //NBT会包含空格，所以是动态的
    }

    @Nonnull
    @Override
    public Class<ItemStack> getType() {
        return ItemStack.class;
    }

    @Nonnull
    @Override
    public Class<ItemStack> getTypeClass() {
        return getType();
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.item_stack";
    }

    @Override
    public <T extends List<String> & Deque<String>> ItemStack parseParameter(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException {
        final @Nonnull Item item = CommandBase.getItemByText(context.getSender(),args.getFirst());
        return new ItemStack(item,count,meta);
    }

    public <T extends List<String> & Deque<String>> int parseStackWithNBT(@Nonnull final T args,@Nonnull final ExecuteContext context) throws CommandException {
        final String[] split = args.getFirst().split("\\{",2);
        if(split.length <= 1) throw new IllegalArgumentException();
        final @Nonnull Item item = CommandBase.getItemByText(context.getSender(),split[0]);

        final int[] prefix = new int[args.size()]; //长度前缀和
        prefix[0] = split[1].length() + 1; // + 1 表示左大括号
        final StringBuilder builder = new StringBuilder("{").append(split[1]);
        for(int i=1;i<args.size();i++){
            prefix[i] = prefix[i-1] + 1 + args.get(i).length(); // +1 表示空格
            builder.append(" ").append(args.get(i));
        }
        NBTTagCompound compound = null;
        int nbtEndIndex = args.size()-1;
        for(;nbtEndIndex>=0;nbtEndIndex--){
            try {
                compound = JsonToNBT.getTagFromJson(builder.substring(0,prefix[nbtEndIndex]));
                break;
            }catch (NBTException e) {
                if(nbtEndIndex>0) continue;
                throw new CommandException("commands.give.tagError", e.getMessage());
            }
        }
        if(compound == null) throw new CommandException("commands.give.tagError","NULL");
        final ItemStack stack = new ItemStack(item,count,meta);
        stack.setTagCompound(compound);
        putParsedArgument(stack,context);
        return nbtEndIndex+1;
    }

    @Override
    public boolean checkValid(@Nonnull final List<String> args, @Nonnull final CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        return ValidChecker.MATCH_ONE_PARAMETER.check(this, args, context); //由于具体检查比较复杂，这里就简单检查一下
    }
}
