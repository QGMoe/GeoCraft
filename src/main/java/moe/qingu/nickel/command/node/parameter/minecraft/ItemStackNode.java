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

import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.node.parameter.ParameterNode;
import moe.qingu.nickel.nbt.SNBTReader;
import moe.qingu.nickel.command.suggestor.DirectSuggestor;
import moe.qingu.nickel.command.utils.Claimer;
import moe.qingu.nickel.nbt.SNBTScanner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.utils.Matchers;
import moe.qingu.nickel.command.utils.Acceptor;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public class ItemStackNode extends ParameterNode<ItemStack> {
    public static final DefaultParser<ItemStack> DEFAULT_PARSER = (node, context) -> new ItemStack(Items.AIR,1);
    public static final Claimer CLAIMER_WITH_NBT = Matchers.matchOnlyFirstToken(arg->{
        final String[] split = arg.split("\\{",2);
        return split.length == 1 && Matchers.isResourceLocation(arg) || split.length == 2 && Matchers.isResourceLocation(split[0]);
    });
    public static final DirectSuggestor<ItemStack> DEFAULT_SUGGESTOR = DirectSuggestor.of(ItemSelectorNode.DEFAULT_SUGGESTOR.getData());

    protected boolean allowNBT = true;
    protected int count = 1;
    protected int meta = 0;

    public ItemStackNode(@Nonnull final String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
        setClaimer(CLAIMER_WITH_NBT);
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

    @Nonnull
    @Override
    public Class<ItemStack> getTypeClass() {
        return ItemStack.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.item_stack";
    }

    @Override
    public ItemStack parse(@Nonnull final InputReader input) throws CommandException {
        final CommandContext context = input.getContext();
        if(!allowNBT){
            final @Nonnull Item item = CommandBase.getItemByText(context.getSender(),input.readToken());
            return new ItemStack(item,count,meta);
        }else return readStackWithNBT(input,context);
    }


    @Override
    public void scan(@Nonnull final InputReader input) throws CommandException, NickelScanEOFSignal {
        if(!allowNBT) input.skipContents();
        else scanStackWithNBT(input);
    }

    @Nonnull
    public ItemStack readStackWithNBT(@Nonnull final InputReader input, @Nonnull final CommandContext context) throws CommandException {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        while (input.canRead() && !Character.isWhitespace(input.peek()) && input.peek() != '{') input.skip();
        final String itemId = input.getSubInput(begin,input.getCursor());
        final @Nonnull Item item = CommandBase.getItemByText(context.getSender(),itemId);

        final NBTTagCompound compound = !input.canRead() || Character.isWhitespace(input.peek())?null:SNBTReader.readSingleNBTFromInput(input);
        final ItemStack stack = new ItemStack(item,count,meta);
        stack.setTagCompound(compound);
        return stack;
    }

    public static void scanStackWithNBT(@Nonnull final InputReader input) throws CommandException, NickelScanEOFSignal {
        input.skipWhitespaces();
        while (input.canRead() && !Character.isWhitespace(input.peek()) && input.peek() != '{') input.skip();
        if(input.canRead() && !Character.isWhitespace(input.peek())) SNBTScanner.scanSingleNBTFromInput(input);
    }

    @Override
    public boolean accepts(@Nonnull final InputReader input) throws CommandException {
        return Acceptor.REQUIRE_ONE_TOKEN.check(this, input); //由于具体检查比较复杂，这里就简单检查一下
    }
}
