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

package top.qiguaiaaaa.geocraft.api.command.node;

import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author QiguaiAAAA
 */
public abstract class ParameterNode<P> extends NoSplitNode implements IOptionalNode{
    private static final ThreadLocal<Stack<String>> ParasStack = ThreadLocal.withInitial(Stack::new);
    protected final String name;
    protected boolean optional;
    protected DefaultParser<P> defaultParser;
    protected BiFunction<List<String>,SuggestContext,List<String>> suggestProvider;

    public ParameterNode(@Nonnull String name) {
        this.name = name;
    }

    public void setDefaultParser(DefaultParser<P> defaultParser) {
        this.defaultParser = defaultParser;
    }

    public void setSuggestProvider(BiFunction<List<String>, SuggestContext, List<String>> suggestProvider) {
        this.suggestProvider = suggestProvider;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public <T extends List<String> & Deque<String>> void execute(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        final boolean parsed = checkAndParse(args,context);
        final Stack<String> paras = parsed?ParasStack.get():null;
        if(childNode != null){
            if(parsed){
                for(int i=0;i<getParametersLength();i++){
                    paras.push(args.pollFirst());
                }
            }
            try {
                childNode.execute(args,context);
            }finally {
                if(parsed){
                    for(int i=0;i<getParametersLength();i++){
                        args.addFirst(paras.pop());
                    }
                }
            }
        }
        context.remove(name);
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        if(args.size()>getParametersLength()){
            final Stack<String> paras = ParasStack.get();
            for(int i=0;i<getParametersLength();i++){
                paras.push(args.pollFirst());
            }
            try {
                return childNode.suggest(args, context);
            }finally {
                for(int i=0;i<getParametersLength();i++){
                    args.addFirst(paras.pop());
                }
            }
        }else if(args.size()==0){
            return null;
        }else if(suggestProvider!=null){
            final List<String> suggests = suggestProvider.apply(args,context);
            return suggests==null?null:suggests.stream().filter(s -> s.startsWith(args.getLast().trim())).collect(Collectors.toList());
        }else return null;
    }

    protected final <T extends List<String> & Deque<String>> boolean checkAndParse(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException{
        final P parsedArg;
        if(checkValid(args,context)){
            parsedArg = parseParameter(args,context);
            context.put(name,parsedArg);
            return true;
        }else if(defaultParser!=null){
            parsedArg = defaultParser.parser(this,context);
            context.put(name,parsedArg);
        }
        return false;
    }

    public abstract int getParametersLength();

    public abstract <T extends List<String> & Deque<String>> boolean checkValid(@Nonnull T args, @Nonnull ExecuteContext context) throws WrongUsageException;

    public abstract <T extends List<String> & Deque<String>> P parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException;

    @FunctionalInterface
    public interface DefaultParser<T>{
        T parser(@Nonnull ParameterNode<T> node,@Nonnull ExecuteContext context) throws CommandException;
    }
}
