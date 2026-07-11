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
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.reader.InputReader;
import net.minecraft.command.CommandException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import 清汩萌.镍.镍测试;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * InputReader 空白跳过、token 读取、引号字符串与剩余输入查询测试
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderToken {

    private static @Nonnull InputReader withContext(final @Nonnull String input){
        final @Nonnull InputReader reader = new InputReader(input);
        new CommandContext(reader,null,null,null);//构造器自动 input.setContext(this)
        return reader;
    }

    /**
     * Claude Generated
     * 测试 skipWhitespaces 跳过空格/制表符/换行/全角空格，停在首个非空白
     */
    @Test
    public void skipWhitespacesTest(){
        final @Nonnull InputReader reader = new InputReader("  \t\n x");
        reader.skipWhitespaces();
        Assertions.assertEquals(5,reader.getCursor());
        Assertions.assertEquals('x',reader.peek());
        final @Nonnull InputReader ideographic = new InputReader("　x");//全角空格
        ideographic.skipWhitespaces();
        Assertions.assertEquals('x',ideographic.peek());
    }

    /**
     * Claude Generated
     * 测试 skipWhitespaces 在空输入与纯空白输入上安全走到 EOF
     */
    @Test
    public void skipWhitespacesAtEOFTest(){
        final @Nonnull InputReader empty = new InputReader("");
        empty.skipWhitespaces();
        Assertions.assertEquals(0,empty.getCursor());
        final @Nonnull InputReader blank = new InputReader("   ");
        blank.skipWhitespaces();
        Assertions.assertEquals(3,blank.getCursor());
        Assertions.assertFalse(blank.canRead());
    }

    /**
     * Claude Generated
     * 测试 skipContents 跳到下一个空白处，起点即空白时不动，EOF 安全
     */
    @Test
    public void skipContentsTest(){
        final @Nonnull InputReader reader = new InputReader("abc def");
        reader.skipContents();
        Assertions.assertEquals(3,reader.getCursor());
        reader.skipContents();//已在空白处，不动
        Assertions.assertEquals(3,reader.getCursor());
        final @Nonnull InputReader noWs = new InputReader("abc");
        noWs.skipContents();
        Assertions.assertEquals(3,noWs.getCursor());
        Assertions.assertFalse(noWs.canRead());
    }

    public static @Nonnull Stream<Arguments> pullDataForReadToken(){
        return Stream.of(
                Arguments.of("foo bar","foo",3),
                Arguments.of("  foo","foo",5),
                Arguments.of("","",0),
                Arguments.of("   ","",3),
                Arguments.of("\tabc","abc",4),
                Arguments.of("天圆地方 之书","天圆地方",4),
                Arguments.of("😀x y","😀x",2)
        );
    }

    /**
     * Claude Generated
     * 测试 readToken：先跳空白读到下一空白或 EOF，空输入返回空串不报错
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadToken")
    public void readTokenTest(final @Nonnull String input,final @Nonnull String expected,final int cursorAfter){
        final @Nonnull InputReader reader = new InputReader(input);
        final @Nonnull String token = reader.readToken();
        镍测试.LOGGER.info("readTokenTest input=[{}] token=[{}] cursor={}",input,token,reader.getCursor());
        Assertions.assertEquals(expected,token);
        Assertions.assertEquals(cursorAfter,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试 readToken 连续读取多个 token，读尽后返回空串
     */
    @Test
    public void readTokenSequenceTest(){
        final @Nonnull InputReader reader = new InputReader("a bb  ccc");
        Assertions.assertEquals("a",reader.readToken());
        Assertions.assertEquals("bb",reader.readToken());
        Assertions.assertEquals("ccc",reader.readToken());
        Assertions.assertEquals("",reader.readToken());//EOF 后仍安全
    }

    public static @Nonnull Stream<Arguments> pullDataForIsRemainingEmpty(){
        return Stream.of(
                Arguments.of("",true),
                Arguments.of("   \t\n ",true),
                Arguments.of("　",true),
                Arguments.of("a",false),
                Arguments.of("  x",false)
        );
    }

    /**
     * Claude Generated
     * 测试 isRemainingEmpty 判断剩余是否只有空白，且游标在 finally 中恢复不动
     */
    @ParameterizedTest
    @MethodSource("pullDataForIsRemainingEmpty")
    public void isRemainingEmptyTest(final @Nonnull String input,final boolean expected){
        final @Nonnull InputReader reader = new InputReader(input);
        Assertions.assertEquals(expected,reader.isRemainingEmpty());
        Assertions.assertEquals(0,reader.getCursor());//游标不动
    }

    /**
     * Claude Generated
     * 测试消费完毕后 isRemainingEmpty 为 true
     */
    @Test
    public void isRemainingEmptyAfterConsumeTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        Assertions.assertFalse(reader.isRemainingEmpty());
        reader.read();
        reader.read();
        Assertions.assertTrue(reader.isRemainingEmpty());
        Assertions.assertEquals(2,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试 readRemaining 返回剩余子串并推游标到 EOF，EOF 处返回空串
     */
    @Test
    public void readRemainingTest(){
        final @Nonnull InputReader reader = new InputReader("hello");
        reader.read();
        reader.read();
        Assertions.assertEquals("llo",reader.readRemaining());
        Assertions.assertEquals(reader.getLength(),reader.getCursor());
        Assertions.assertEquals("",reader.readRemaining());//EOF 再读为空串
        Assertions.assertEquals("",new InputReader("").readRemaining());
    }

    /**
     * Claude Generated
     * 预期行为：readRemaining 不跳前导空白，原样返回
     */
    @Test
    public void readRemainingKeepsWhitespaceTest(){
        final @Nonnull InputReader reader = new InputReader("a  b");
        reader.read();
        Assertions.assertEquals("  b",reader.readRemaining());
    }

    public static @Nonnull Stream<Arguments> pullDataForReadString(){
        return Stream.of(
                Arguments.of("abc def","abc"),//无引号退化为 readToken
                Arguments.of("",""),
                Arguments.of("\"a b\" c","a b"),
                Arguments.of("'a b' c","a b"),
                Arguments.of("'a\"b'","a\"b"),//单引号内的双引号是字面
                Arguments.of("\"a'b\"","a'b"),//双引号内的单引号是字面
                Arguments.of("\"\"",""),
                Arguments.of("''",""),
                Arguments.of("  \"x\"","x"),//前导空白被跳过
                Arguments.of("\"a\\nb\"","a\nb"),//转义在引号串内生效
                Arguments.of("\"a\\\\b\"","a\\b"),
                Arguments.of("\"天 圆\"","天 圆")
        );
    }

    /**
     * Claude Generated
     * 测试 readString：引号串（含互嵌引号与转义）与无引号 token 两种形态
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadString")
    public void readStringTest(final @Nonnull String input,final @Nonnull String expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(input);
        final @Nonnull String result = reader.readString();
        镍测试.LOGGER.info("readStringTest input=[{}] result=[{}]",input,result);
        Assertions.assertEquals(expected,result);
    }

    /**
     * Claude Generated
     * 测试 readString 读完引号串后游标停在闭合引号之后
     */
    @Test
    public void readStringCursorAfterQuoteTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("\"a b\" c");
        reader.readString();
        Assertions.assertEquals(5,reader.getCursor());
        Assertions.assertEquals(' ',reader.peek());
    }

    /**
     * Claude Generated
     * 测试 readString 未闭合引号 panic（无分支上下文时为 NickelRuntimeException）
     */
    @Test
    public void readStringUnclosedTest(){
        final @Nonnull InputReader reader = withContext("\"abc");
        Assertions.assertThrows(NickelRuntimeException.class,reader::readString);
    }

    /**
     * Claude Generated
     * 测试 readString 引号内转义在 EOF 处截断时 panic
     */
    @Test
    public void readStringTruncatedEscapeTest(){
        final @Nonnull InputReader reader = withContext("\"a\\");
        Assertions.assertThrows(CommandException.class,reader::readString);
    }

    /**
     * Claude Generated
     * 测试 scanString 引号串：不构造字符串，游标停在闭合引号之后
     */
    @Test
    public void scanStringQuotedCursorTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("\"a b\" tail");
        reader.scanString();
        Assertions.assertEquals(5,reader.getCursor());
        Assertions.assertEquals(' ',reader.peek());
    }

    /**
     * Claude Generated
     * 测试 scanString 越过转义引号不误判闭合
     */
    @Test
    public void scanStringEscapedQuoteTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("\"a\\\"b\" t");
        reader.scanString();
        Assertions.assertEquals(6,reader.getCursor());//停在真正闭合引号后
        Assertions.assertEquals(' ',reader.peek());
    }

    /**
     * Claude Generated
     * 测试 scanString 无引号时先跳空白再等价于 skipContents
     */
    @Test
    public void scanStringUnquotedTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("abc def");
        reader.scanString();
        Assertions.assertEquals(3,reader.getCursor());
        final @Nonnull InputReader padded = new InputReader("  ab c");
        padded.scanString();
        Assertions.assertEquals(4,padded.getCursor());
    }

    /**
     * Claude Generated
     * 测试 scanString 未闭合引号在 EOF 抛 NickelScanEOFSignal 单例而非 panic（与 readString 对比）
     */
    @Test
    public void scanStringUnclosedEOFSignalTest(){
        final @Nonnull InputReader reader = new InputReader("\"abc");//无需 context，信号在 panic 之前抛出
        final @Nonnull NickelScanEOFSignal signal = Assertions.assertThrows(NickelScanEOFSignal.class,reader::scanString);
        Assertions.assertSame(NickelScanEOFSignal.INSTANCE,signal);
    }

    /**
     * Claude Generated
     * 测试 scanString 引号内截断转义同样以 EOF 信号收场
     */
    @Test
    public void scanStringTruncatedEscapeEOFSignalTest(){
        final @Nonnull InputReader reader = new InputReader("\"a\\");
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,reader::scanString)
        );
    }
}
