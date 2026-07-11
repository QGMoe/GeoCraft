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

package 清汩萌.镍.reader;

import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.command.utils.CommandBranch;
import moe.qingu.nickel.reader.InputReader;
import net.minecraft.command.CommandException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import 清汩萌.镍.镍测试;

import javax.annotation.Nonnull;

/**
 * InputReader panic 分派、expect/expectOrEnd 与 CommandContext 绑定测试
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderPanic {

    private static @Nonnull InputReader withContext(final @Nonnull String input){
        final @Nonnull InputReader reader = new InputReader(input);
        new CommandContext(reader,null,null,null);//构造器自动 input.setContext(this)
        return reader;
    }

    /**
     * Claude Generated
     * 未 setContext 时 panic 抛携带报错文本的 CommandException
     */
    @Test
    public void panicWithoutContextThrowsCommandExceptionTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertThrows(CommandException.class,() -> reader.expect('x'));
    }

    /**
     * Claude Generated
     * readBoolean 非法 token 在未绑定 context 时抛 CommandException（走 panic 的无 context 保护）
     */
    @Test
    public void readBooleanWithoutContextCommandExceptionTest(){
        final @Nonnull InputReader reader = new InputReader("yes");
        Assertions.assertThrows(CommandException.class,reader::readBoolean);
    }

    /**
     * Claude Generated
     * readInt 解析失败在未绑定 context 时抛 CommandException（走 panic 的无 context 保护）
     */
    @Test
    public void readIntWithoutContextCommandExceptionTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertThrows(CommandException.class,reader::readInt);
    }

    /**
     * Claude Generated
     * readString 未闭合引号在未绑定 context 时抛 CommandException（panic 的无 context 保护）
     */
    @Test
    public void readStringWithoutContextCommandExceptionTest(){
        final @Nonnull InputReader reader = new InputReader("\"a");
        Assertions.assertThrows(CommandException.class,reader::readString);
    }

    /**
     * Claude Generated
     * 测试无分支上下文时 panic 抛 NickelRuntimeException（属 CommandException 体系）
     */
    @Test
    public void panicNoBranchRuntimeExceptionTest(){
        final @Nonnull InputReader reader = withContext("abc");
        final @Nonnull NickelRuntimeException e = Assertions.assertThrows(
                NickelRuntimeException.class,
                () -> reader.panic(0,"nickel.command.syntax.eof")
        );
        Assertions.assertTrue(e instanceof CommandException);
        Assertions.assertEquals("nickel.command.exception.base.message",e.getMessage());
        镍测试.LOGGER.info("panicNoBranchRuntimeExceptionTest passed: {}",e.getClass().getName());
    }

    /**
     * Claude Generated
     * 测试进入分支后 panic 分派为 NickelCommandException（节点非 documentary），退出分支后回落 NickelRuntimeException
     */
    @Test
    public void panicDispatchByBranchTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        final @Nonnull CommandContext ctx = new CommandContext(reader,null,null,null);
        final @Nonnull CommandBranch branch = new CommandBranch();
        final CommandContext.ContextStack<CommandBranch> stack = ctx.enter(branch);
        final @Nonnull NickelCommandException e = Assertions.assertThrows(
                NickelCommandException.class,
                () -> reader.panic(0,"nickel.command.syntax.eof")
        );
        Assertions.assertSame(branch,e.getSourceBranch());
        Assertions.assertNull(e.getSourceNode());
        stack.close();//退出分支
        Assertions.assertThrows(
                NickelRuntimeException.class,
                () -> reader.panic(0,"nickel.command.syntax.eof")
        );
    }

    /**
     * Claude Generated
     * 测试 expect 命中时消费该码点且不抛异常
     */
    @Test
    public void expectHitTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("abc");//命中路径不依赖 context
        reader.expect('a');
        Assertions.assertEquals(1,reader.getCursor());
        Assertions.assertEquals('b',reader.peek());
    }

    /**
     * Claude Generated
     * 测试 expect 遇到不匹配字符时 panic
     */
    @Test
    public void expectWrongCharTest(){
        final @Nonnull InputReader reader = withContext("abc");
        Assertions.assertThrows(NickelRuntimeException.class,() -> reader.expect('x'));
        Assertions.assertEquals(0,reader.getCursor());//未消费
    }

    /**
     * Claude Generated
     * 测试 expect 在 EOF 时 panic（EOF 键）而非抛信号
     */
    @Test
    public void expectEOFTest(){
        final @Nonnull InputReader reader = withContext("");
        Assertions.assertThrows(CommandException.class,() -> reader.expect('a'));
    }

    /**
     * Claude Generated
     * 测试 expectOrEnd 命中时消费该码点
     */
    @Test
    public void expectOrEndHitTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("ab");
        reader.expectOrEnd('a');
        Assertions.assertEquals(1,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试 expectOrEnd 遇到不匹配字符时仍 panic（与 EOF 区分）
     */
    @Test
    public void expectOrEndWrongCharTest(){
        final @Nonnull InputReader reader = withContext("b");
        Assertions.assertThrows(CommandException.class,() -> reader.expectOrEnd('a'));
    }

    /**
     * Claude Generated
     * 测试 expectOrEnd 在 EOF 抛 NickelScanEOFSignal：单例、无消息、栈剥离
     */
    @Test
    public void expectOrEndEOFSignalTest(){
        final @Nonnull InputReader reader = new InputReader("");//信号路径不依赖 context
        final @Nonnull NickelScanEOFSignal signal = Assertions.assertThrows(
                NickelScanEOFSignal.class,
                () -> reader.expectOrEnd('a')
        );
        Assertions.assertSame(NickelScanEOFSignal.INSTANCE,signal);
        Assertions.assertNull(signal.getMessage());
        Assertions.assertEquals(0,signal.getStackTrace().length);//writableStackTrace=false
    }

    /**
     * Claude Generated
     * 测试 CommandContext 构造器自动把自身绑定到 reader，setContext 可替换
     */
    @Test
    public void contextBindingTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        final @Nonnull CommandContext ctx = new CommandContext(reader,null,null,null);
        Assertions.assertSame(ctx,reader.getContext());
        Assertions.assertSame(reader,ctx.getInput());
        final @Nonnull CommandContext another = new CommandContext(new InputReader(""),null,null,null);
        reader.setContext(another);
        Assertions.assertSame(another,reader.getContext());
    }
}
