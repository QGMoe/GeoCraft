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

package top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.node.parament.SmartParameterNode;
import top.qiguaiaaaa.geocraft.api.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public class BlockStateNode extends SmartParameterNode<IBlockState> {
    protected final @Nullable String blockNodeName;

    public BlockStateNode(@Nullable final String blockNodeName,@Nonnull final String name) {
        super(name);
        this.blockNodeName = blockNodeName;
    }

    public BlockStateNode(@Nonnull final String name) {
        this(null,name);
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Class<IBlockState> getType() {
        return IBlockState.class;
    }

    @Nonnull
    @Override
    public Class<IBlockState> getTypeClass() {
        return getType();
    }

    @Override
    public <T extends List<String> & Deque<String>> IBlockState parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(blockNodeName != null){
            final Block block = context.get(blockNodeName);
            String arg = args.get(0);
            if(arg.startsWith("[")) {
                if(arg.endsWith("]")) arg = arg.substring(1,arg.length()-1);
                else throw new SyntaxErrorException();
            }
            return CommandBase.convertArgToBlockState(block,arg);
        }else {
            final String arg = args.get(0);
            final String[] split = arg.split("\\[");
            if(split.length == 0) throw new SyntaxErrorException();
            if(split.length == 1) return CommandBase.getBlockByText(context.getSender(),arg).getDefaultState();
            if(!split[1].endsWith("]")) throw new SyntaxErrorException();
            return CommandBase.convertArgToBlockState(CommandBase.getBlockByText(context.getSender(),split[0]),split[1].substring(0,split[1].length()-1));
        }
    }

    @Override
    public boolean checkValid(@Nonnull final List<String> args, @Nonnull final CommandContext context) throws SyntaxErrorException, NumberInvalidException, InvalidBlockStateException {
        if(!ValidChecker.MATCH_ONE_PARAMETER.check(this,args,context)) return false;
        if(blockNodeName == null) return true;
        try {
            CommandBase.parseInt(args.get(0));
        }catch (NumberInvalidException e){
            return "default".equals(args.get(0)) || args.get(0).startsWith("[");
        }
        return true;
    }
}
