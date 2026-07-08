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

package moe.qingu.nickel.command.node.parameter;

import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.suggestor.Suggestion;
import moe.qingu.nickel.command.utils.Claimer;
import moe.qingu.nickel.command.suggestor.Suggestor;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.text.event.HoverEvent;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.node.IOptionalNode;
import moe.qingu.nickel.command.node.NoSplitNode;
import moe.qingu.nickel.command.node.parameter.minecraft.BlockPosNode;
import moe.qingu.nickel.command.utils.CommandBranch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collections;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.translation;

/**
 * 当参数为必选的时候，例如对于一个数字参数，检测的方法比较简单，直接检测当前字符串是否是一个数字。如果不是就不匹配。
 * 当参数为可选的时候，情况就比较复杂。对于例如：<br/>
 * /give [数量] <玩家> <br/>
 * 我们希望 /give 111 @p 和 /give @p 能够同时工作。数量会发现 @p 不匹配，于是采用默认值，走 /give <玩家>。而 111 会被认为是一个合理的数值，走 /give [数量] <玩家>。
 * 但更多情况下，例如 /give [玩家] <物品> 就不能很好的工作，因为对于 apple，其可能被认为是叫做 apple 的玩家，也有可能是 {@link net.minecraft.init.Items#APPLE}
 * 所以非必要，应确保可选参数之后都是可选的参数，以避免混淆。
 * @author QiguaiAAAA
 */
public abstract class ParameterNode<P> extends NoSplitNode implements IOptionalNode, IDocumentaryNode, ISmartNode {
    public static final String PARAMETER_INNER_NAME_SPLIT = "￥";
    protected final String name;
    protected @Nullable String localizedName = null;
    protected @Nullable String comment = null;
    protected boolean optional;

    protected @Nullable DefaultParser<P> defaultParser;
    protected @Nullable Suggestor<P> suggestProvider;
    protected @Nullable Decorator<P> decorator;
    protected @Nullable Claimer claimer;

    protected CommandBranch currentBranch;

    public ParameterNode(@Nonnull final String name) {
        this.name = name;
    }

    /*
     * ------------------
     *       功能区
     * ------------------
     */

    @Override
    public void execute(@Nonnull final InputReader input, @Nonnull final ExecuteContext context) throws CommandException {
        if(childNode == null) return;
        bindArgument(input,context);
        this.executeChild(context);
    }

    protected void executeChild(@Nonnull final ExecuteContext context) throws CommandException{
        context.enter(this.childNode);
    }

    @Nullable
    @Override
    public Suggestion suggest(@Nonnull final InputReader input, @Nonnull final SuggestContext context) throws CommandException {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        if(!input.canRead()) if(suggestProvider!=null) return new Suggestion(this,suggestProvider.provide(this,input,begin,context));
        else return new Suggestion(this, Collections.emptyList());

        try {
            scan(input);
        } catch (final NickelScanEOFSignal ignored) {}
        if(input.canRead()) return this.childNode != null ? context.enter(childNode):null; //存在之后的节点内容

        input.setCursor(begin);
        if(suggestProvider != null) return new Suggestion(this,suggestProvider.provide(this,input,begin,context));
        else return new Suggestion(this,Collections.emptyList());
    }

    protected final boolean bindArgument(@Nonnull final InputReader input, @Nonnull final ExecuteContext context) throws CommandException{
        final P parsedArg;
        final boolean valid;

        final int cur = input.getCursor();
        try {
            valid = accepts(input);
        }finally {
            input.setCursor(cur);
        }

        if(valid){
            parsedArg = parse(input);
            putParsedArgument(parsedArg,context);
            return true;
        }else if(defaultParser!=null){
            parsedArg = defaultParser.parser(this,context);
            putParsedArgument(parsedArg,context);
            return false;
        }
        throw new NickelCommandException(currentBranch).withSource(this).withAppendix(translation("nickel.command.parameter.base.default_not_found"));
    }

    protected final void putParsedArgument(final P parsedArgument,final @Nonnull ExecuteContext context) throws CommandException {
        context.put(name,decorator==null?parsedArgument:decorator.decorate(parsedArgument,context));
    }

    /**
     * 检查当前的命令参数是否符合当前参数的语法格式。
     * 请注意，最好仅检查语法以保证命令的确定性。例如，物品参数应当只检查是否有至少一个参数，至于参数内写的 ID 对应的物品是否存在，应在 {@link #parse(InputReader)} 中检查。
     * @param input 提供的输入（内含上下文）
     * @return 返回 true 表示参数合法，可以解析。返回 false 表示参数非法，但可以使用默认值，仅当 {@link #isOptional()} 为 true 时使用。
     * @throws SyntaxErrorException 当参数非法，且 {@link #isOptional()} 为 false 时，抛出该错误，以说明命令语法错误。此时命令解析会中断。
     * 当然如果{@link #isOptional()} 为 true 时也可以使用，例如对于多参数节点，比如对于 {@link BlockPosNode}
     * 如果玩家输入 3 4 却没有输入 Z 坐标，这时候用默认值就不太合适了，应当告诉玩家输错了。
     * @throws NumberInvalidException 同上
     */
    public abstract boolean accepts(@Nonnull final InputReader input) throws CommandException;

    public abstract P parse(@Nonnull final InputReader input) throws CommandException;

    public abstract void scan(@Nonnull final InputReader input) throws CommandException, NickelScanEOFSignal;

    public String serialise(@Nonnull final P p){
        return p.toString();
    }

    @Nonnull
    @Override
    public CommandBranch branch() {
        this.currentBranch = super.branch();
        currentBranch.appendDocument(getDocument());
        return currentBranch;
    }

    /*
     * ---------------------------
     *
     *    Getter And Setter
     *
     * ---------------------------
     */

    public static String getInnerParameterName(@Nonnull final String parent,@Nonnull final String child){
        return parent+PARAMETER_INNER_NAME_SPLIT+child;
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

    public void setDefaultParser(@Nullable final DefaultParser<P> defaultParser) {
        this.defaultParser = defaultParser;
    }

    public void setSuggestProvider(@Nullable final Suggestor<P> suggestProvider) {
        this.suggestProvider = suggestProvider;
    }

    public void setDecorator(@Nullable final Decorator<P> decorator) {
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
    @Override
    public final TextBuilder<?,?> getDocument(){
        final TextBuilder<?,?> text = plain(IDocumentaryNode.getFormatBegin(isOptional()))
                .then(translation(this.getTranslationKey()))
                .then(FORMAT_SPLIT)
                .then(translation(this.getTypeTranslationKey()))
                .then(IDocumentaryNode.getFormatEnd(isOptional()));
        if(comment != null) text.hoverTo(HoverEvent.Action.SHOW_TEXT).content(translation(comment));
        return text;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    /*
     * ----------------------------
     *          Smart Node
     * ----------------------------
     */

    @Override
    public void setClaimer(@Nullable final Claimer claimer) {
        this.claimer = claimer;
    }

    @Override
    public boolean claims(@Nonnull final InputReader input) {
        if(claimer ==null){
            try {
                return accepts(input);
            }catch (final CommandException ignore){
                return false;
            }
        }else return claimer.test(input);
    }

    @FunctionalInterface
    public interface DefaultParser<T>{
        T parser(@Nonnull final ParameterNode<T> node,@Nonnull final ExecuteContext context) throws CommandException;
    }
}
