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

package top.qiguaiaaaa.geocraft.api.command.node.generic;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.functional.SmartSplitNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * 一个智能化的参数节点，可以用于智能分支{@link SmartSplitNode}。<br/>
 * 该智能参数节点默认使用 {@link this#checkValid(List, CommandContext)} 作为 {@link this#match(List, CommandContext)} 方法的逻辑。
 */
public abstract class SmartParameterNode<P> extends ParameterNode<P> implements ISmartNode {
    public static final ValidChecker MATCH_ONE_PARAMETER = (self,args,context) -> {
        if(args.size()>=1) return true;
        else if(self.isOptional()) return false;
        else throw new WrongUsageException("api.geo.command.parameter.smart.checker1", self.getLocalizedParameter());
    };
    public static final ValidChecker MATCH_THREE_PARAMETER = ((self, args, context) -> {
        if(args.size()>=3) return true;
        else if(args.size()>=1) throw new WrongUsageException("api.geo.command.parameter.smart.checker3",I18n.format(self.getLocalizedName())); //只有一到两个参数，填了一半，不能用默认值
        else if(self.isOptional()) return false; //可以用默认值
        else throw new WrongUsageException("api.geo.command.parameter.smart.checker3",self.getLocalizedParameter());
    });
    public static final ValidChecker MATCH_FOUR_PARAMETER = ((self, args, context) -> {
        if(args.size()>=4) return true;
        else if(args.size()>=1) throw new WrongUsageException("api.geo.command.parameter.smart.checker4",I18n.format(self.getLocalizedName())); //只有一到三个参数，填了一半，不能用默认值
        else if(self.isOptional()) return false; //可以用默认值
        else throw new WrongUsageException("api.geo.command.parameter.smart.checker4",self.getLocalizedParameter());
    });

    protected @Nullable BiPredicate<List<String>, CommandContext> matchChecker;

    public SmartParameterNode(@Nonnull String name) {
        super(name);
    }

    @Override
    public final <T extends List<String> & Deque<String>> boolean checkValid(@Nonnull T args, @Nonnull ExecuteContext context) throws SyntaxErrorException, NumberInvalidException, InvalidBlockStateException {
        return checkValid(args,(CommandContext) context);
    }

    @Override
    public void setMatcher(@Nullable BiPredicate<List<String>, CommandContext> matcher) {
        this.matchChecker = matcher;
    }

    @Override
    public boolean match(@Nonnull List<String> args, @Nonnull CommandContext context) {
        if(matchChecker==null){
            try {
                return checkValid(args,context);
            }catch (SyntaxErrorException | NumberInvalidException | InvalidBlockStateException ignore){
                return false;
            }
        }else return matchChecker.test(args,context);
    }

    /**
     * @see ParameterNode#checkValid(List, ExecuteContext)
     * @param args 未解析的参数列表
     * @param context 命令通用上下文
     * @return 为 true 表示可以解析，为 false 表示不能解析，但可以用默认值，仅当 {@link #isOptional()} 为 true 时使用。
     * @throws SyntaxErrorException 当{@link #isOptional()} 为 false 时，且语法错误，则抛出该错误表示无法解析。
     * @throws NumberInvalidException 同上
     * @throws InvalidBlockStateException 同上
     * 请注意，当在 {@link #match(List, CommandContext)} 时，抛出错误不会终止命令执行，而是会返回匹配失败，继续匹配下一个智能节点。
     */
    public abstract boolean checkValid(@Nonnull List<String> args,@Nonnull CommandContext context)
            throws SyntaxErrorException, NumberInvalidException, InvalidBlockStateException;

    public interface ValidChecker{
        boolean check(@Nonnull SmartParameterNode<?> self,@Nonnull List<String> args,@Nonnull CommandContext context)
                throws SyntaxErrorException, NumberInvalidException, InvalidBlockStateException;
    }
}
