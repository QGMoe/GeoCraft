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

package top.qiguaiaaaa.geocraft.api.command.node.functional;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.ICommandNode;
import top.qiguaiaaaa.geocraft.api.function.TriPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author QiguaiAAAA
 */
public class ConditionalSplitNode implements ICommandNode {
    protected final Map<BiPredicate<CommandContext,List<String>>,ICommandNode> nodeList = new LinkedHashMap<>();

    public void addCondition(@Nonnull final BiPredicate<CommandContext,List<String>> condition,@Nonnull final ICommandNode node){
        nodeList.put(Objects.requireNonNull(condition),Objects.requireNonNull(node));
    }

    @Nullable
    protected ICommandNode findNextNode(@Nonnull final List<String> args, @Nonnull final CommandContext context){
        for(Map.Entry<BiPredicate<CommandContext,List<String>>,ICommandNode> entry:nodeList.entrySet()){
            if(entry.getKey().test(context,args)){
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException {
        final ICommandNode node = findNextNode(args, context);
        if(node!=null) node.execute(args,context);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull final T args, @Nonnull final SuggestContext context) {
        GeoCraft.getLogger().info("[Conditional] Provide Suggest For [len={}] : {}",args.size(),String.join(" ",args));
        if(args.size()>1){ //Conditional 的位置不需要建议，只要分支
            final ICommandNode node = findNextNode(args, context);
            if (node != null) return node.suggest(args, context);
            return Collections.emptyList();
        }
        return nodeList.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .map(entry -> entry.getValue().suggest(args, context))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(String::trim)
                .distinct() //去重
                .filter(s->s.startsWith(args.isEmpty()?"":args.getLast().trim()))
                .sorted()
                .collect(Collectors.toList());
    }
}
