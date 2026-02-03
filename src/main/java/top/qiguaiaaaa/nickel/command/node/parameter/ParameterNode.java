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

package top.qiguaiaaaa.nickel.command.node.parameter;

import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import top.qiguaiaaaa.nickel.command.context.ExecuteContext;
import top.qiguaiaaaa.nickel.command.context.SuggestContext;
import top.qiguaiaaaa.nickel.command.exception.NickelCommandException;
import top.qiguaiaaaa.nickel.command.node.IDocumentaryNode;
import top.qiguaiaaaa.nickel.command.node.IOptionalNode;
import top.qiguaiaaaa.nickel.command.node.NoSplitNode;
import top.qiguaiaaaa.nickel.command.node.parameter.minecraft.BlockPosNode;
import top.qiguaiaaaa.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 当参数为必选的时候，例如对于一个数字参数，检测的方法比较简单，直接检测当前字符串是否是一个数字。如果不是就不匹配。
 * 当参数为可选的时候，情况就比较复杂。对于例如：<br/>
 * /give [数量] <玩家> <br/>
 * 我们希望 /give 111 @p 和 /give @p 能够同时工作。数量会发现 @p 不匹配，于是采用默认值，走 /give <玩家>。而 111 会被认为是一个合理的数值，走 /give [数量] <玩家>。
 * 但更多情况下，例如 /give [玩家] <物品> 就不能很好的工作，因为对于 apple，其可能被认为是叫做 apple 的玩家，也有可能是 {@link net.minecraft.init.Items#APPLE}
 * 所以非必要，应确保可选参数之后都是可选的参数，以避免混淆。
 * @author QiguaiAAAA
 */
public abstract class ParameterNode<P> extends NoSplitNode implements IOptionalNode, IDocumentaryNode {
    public static final String PARAMETER_INNER_NAME_SPLIT = "￥";
    private static final ThreadLocal<Stack<String>> ParasStack = ThreadLocal.withInitial(Stack::new);
    protected final String name;
    protected String localizedName = null;
    protected String comment = null;
    protected boolean optional;
    protected DefaultParser<P> defaultParser;
    protected BiFunction<List<String>,SuggestContext,List<String>> suggestProvider;
    protected Decorator<P> decorator;
    protected CommandBranch currentBranch;

    public ParameterNode(@Nonnull final String name) {
        this.name = name;
    }

    public static String getInnerParameterName(@Nonnull final String parent,@Nonnull final String child){
        return parent+PARAMETER_INNER_NAME_SPLIT+child;
    }

    public void setDefaultParser(final DefaultParser<P> defaultParser) {
        this.defaultParser = defaultParser;
    }

    public void setSuggestProvider(final BiFunction<List<String>, SuggestContext, List<String>> suggestProvider) {
        this.suggestProvider = suggestProvider;
    }

    public void setDecorator(final Decorator<P> decorator) {
        this.decorator = decorator;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getTranslationKey(){
        return localizedName==null?name:localizedName;
    }

    @Nonnull
    public CommandBranch getCurrentBranch() {
        return currentBranch;
    }

    public void setTranslationKey(@Nonnull final String localizedName) {
        this.localizedName = localizedName;
    }

    public void setComment(@Nullable final String comment) {
        this.comment = comment;
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
    public <T extends List<String> & Deque<String>> void execute(@Nonnull final T args, @Nonnull final ExecuteContext context) throws CommandException {
        if(childNode == null) return;
        final boolean parsed = checkAndParse(args,context);
        this.executeChild(args,context,parsed?getParametersLength():0);
    }

    protected <T extends List<String> & Deque<String>> void executeChild(@Nonnull final T args,@Nonnull final ExecuteContext context,final int usedElements) throws CommandException{
        final boolean parsed = usedElements>0;
        final Stack<String> paras = parsed?ParasStack.get():null;
        if(parsed){
            for(int i=0;i<usedElements;i++){
                paras.push(args.pollFirst());
            }
        }
        try {
            childNode.execute(args,context);
        }finally {
            if(parsed){
                for(int i=0;i<usedElements;i++){
                    args.addFirst(paras.pop());
                }
            }
            context.remove(name);
        }
    }

    @Nullable
    @Override
    public <T extends List<String> & Deque<String>> List<String> suggest(@Nonnull T args, @Nonnull SuggestContext context) {
        //GeoCraft.getLogger().info("[{}] Provide Suggest For [len={}] : {}",getLocalizedParameter(),args.size(),String.join(" ",args));
        if(args.size()>getParametersLength()){
            final Stack<String> paras = ParasStack.get();
            try {
                for(int i=0;i<getParametersLength();i++){
                    paras.push(args.pollFirst());
                }
                return childNode.suggest(args, context);
            }finally {
                for(int i=0;i<getParametersLength();i++){
                    args.addFirst(paras.pop());
                }
            }
        }else if(suggestProvider!=null){
            final List<String> suggests = suggestProvider.apply(args,context);
            return suggests==null?null:suggests.stream()
                    .filter(s -> s.startsWith(args.isEmpty()?"":args.getLast().trim()))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }else return null;
    }

    protected final <T extends List<String> & Deque<String>> boolean checkAndParse(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException{
        final P parsedArg;
        if(checkValid(args,context)){
            parsedArg = parseParameter(args,context);
            putParsedArgument(parsedArg,context);
            return true;
        }else if(defaultParser!=null){
            parsedArg = defaultParser.parser(this,context);
            putParsedArgument(parsedArg,context);
            return false;
        }
        throw new NickelCommandException(currentBranch,this,new TextComponentTranslation("nickel.command.parameter.base.default_not_found"));
    }

    protected final void putParsedArgument(final P parsedArgument,final @Nonnull ExecuteContext context) throws CommandException {
        context.put(name,decorator==null?parsedArgument:decorator.decorate(parsedArgument,context));
    }

    /**
     * 获取需要解析参数的预期长度。例如数字返回 1，坐标返回 3。
     * 如果需要动态解析，则应当继承并重写 {@link #execute(List, ExecuteContext)} 方法，此时该项返回 -1。
     * @return 返回预期解析的参数长度，执行时会 pop 掉指定长度的已解析参数。如果长度动态变化，则需要返回 -1，并一定要重写 {@link #execute(List, ExecuteContext)}。
     */
    public abstract int getParametersLength();

    /**
     * 获取参数的类型
     * @return 参数的类型
     */
    @Nonnull
    public Type getType(){
        return getTypeClass();
    }

    @Nonnull
    public abstract Class<P> getTypeClass();

    /**
     * 获取参数的类型的本地化键名，用于聊天框信息展示
     * @return 类型的本地化键名
     */
    @Nonnull
    public String getTypeTranslationKey(){
        final Type type = getType();
        if(type instanceof Class<?>){
            return ((Class<?>) type).getSimpleName();
        }else return type.getTypeName();
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public final String getLocalizedParameter(){
        return IDocumentaryNode.getFormatBegin(isOptional()) +
                I18n.translateToLocal(getTranslationKey()) +
                IDocumentaryNode.FORMAT_SPLIT +
                I18n.translateToLocal(getTypeTranslationKey()) +
                IDocumentaryNode.getFormatEnd(isOptional());
    }

    @Nonnull
    @Override
    public final ITextComponent getDocument(){
        final ITextComponent component = new TextComponentString(IDocumentaryNode.getFormatBegin(isOptional()))
                .appendSibling(new TextComponentTranslation(getTranslationKey()))
                .appendText(FORMAT_SPLIT)
                .appendSibling(new TextComponentTranslation(getTypeTranslationKey()))
                .appendText(IDocumentaryNode.getFormatEnd(isOptional()));
        if(comment != null) component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TextComponentTranslation(comment)));
        return component;
    }

    /**
     * 检查当前的命令参数是否符合当前参数的语法格式。
     * 请注意，最好仅检查语法以保证命令的确定性。例如，物品参数应当只检查是否有至少一个参数，至于参数内写的 ID 对应的物品是否存在，应在 {@link #parseParameter(List, ExecuteContext)} 中检查。
     * @param args 提供的未解析参数
     * @param context 执行时上下文
     * @return 返回 true 表示参数合法，可以解析。返回 false 表示参数非法，但可以使用默认值，仅当 {@link #isOptional()} 为 true 时使用。
     * @param <T> 未解析参数的列表与队列共同类型
     * @throws SyntaxErrorException 当参数非法，且 {@link #isOptional()} 为 false 时，抛出该错误，以说明命令语法错误。此时命令解析会中断。
     * 当然如果{@link #isOptional()} 为 true 时也可以使用，例如对于多参数节点，比如对于 {@link BlockPosNode}
     * 如果玩家输入 3 4 却没有输入 Z 坐标，这时候用默认值就不太合适了，应当告诉玩家输错了。
     * @throws NumberInvalidException 同上
     */
    public abstract <T extends List<String> & Deque<String>> boolean checkValid(@Nonnull T args, @Nonnull ExecuteContext context)
            throws SyntaxErrorException, NumberInvalidException;

    public abstract <T extends List<String> & Deque<String>> P parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException;

    @Nonnull
    @Override
    public CommandBranch branch() {
        this.currentBranch = super.branch();
        currentBranch.appendDocument(getDocument());
        return currentBranch;
    }

    @FunctionalInterface
    public interface DefaultParser<T>{
        T parser(@Nonnull ParameterNode<T> node,@Nonnull ExecuteContext context) throws CommandException;
    }
}
