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

package top.qiguaiaaaa.geocraft.api.command.node.forge;

import com.google.common.reflect.TypeToken;
import net.minecraft.command.CommandException;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.generic.SmartParameterNode;
import top.qiguaiaaaa.geocraft.api.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author QiguaiAAAA
 */
public class OreSelectorNode extends SmartParameterNode<NonNullList<ItemStack>> {

    public static final BiFunction<List<String>, SuggestContext,List<String>> DEFAULT_SUGGESTOR = (args,context) -> Arrays.stream(OreDictionary.getOreNames()).collect(Collectors.toList());

    public OreSelectorNode(@Nonnull String name) {
        super(name);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    protected static final TypeToken<NonNullList<ItemStack>> Token = new TypeToken<NonNullList<ItemStack>>(NonNullList.class) {};

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Type getType() {
        return Token.getType();
    }

    @Nonnull
    @Override
    public String getLocalizedType() {
        return "api.geo.command.parameter.forge.oreDirectory";
    }

    @Override
    public <T extends List<String> & Deque<String>> NonNullList<ItemStack> parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(!OreDictionary.doesOreNameExist(args.getFirst())) throw new InvalidOreDirectoryException(args.getFirst());
        return OreDictionary.getOres(args.getFirst());
    }

    /**
     * @see OreDictionary#registerOreImpl(String, ItemStack)
     */
    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException, InvalidBlockStateException {
        if(!ValidChecker.MATCH_ONE_PARAMETER.check(this,args,context)) return false;
        return !"Unknown".equals(args.get(0));
    }

    public class InvalidOreDirectoryException extends CommandException{

        public InvalidOreDirectoryException(@Nullable final String invalidOre){
            super("api.geo.command.parameter.oreDirectory.invalid",invalidOre,OreSelectorNode.this.getLocalizedParameter());
        }

        public InvalidOreDirectoryException(String message, Object... objects) {
            super(message, objects);
        }

        @Nonnull
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
