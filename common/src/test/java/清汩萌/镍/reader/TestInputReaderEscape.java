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
import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.reader.InputReader;
import net.minecraft.command.CommandException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import 清汩萌.镍.镍测试;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * InputReader 转义序列（readEscape/scanEscape/readUnicodeName/定长整数）测试。
 * 输入串均为反斜杠之后的转义体，直接调用 readEscape/scanEscape。
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderEscape {

    private static @Nonnull InputReader withContext(final @Nonnull String input){
        final @Nonnull InputReader reader = new InputReader(input);
        new CommandContext(reader,null,null,null);//构造器自动 input.setContext(this)
        return reader;
    }

    public static @Nonnull Stream<Arguments> pullDataForSimpleEscape(){
        return Stream.of(
                Arguments.of("\\",(int)'\\'),
                Arguments.of("s",(int)' '),
                Arguments.of("e",0x1B),
                Arguments.of("b",(int)'\b'),
                Arguments.of("f",(int)'\f'),
                Arguments.of("n",(int)'\n'),
                Arguments.of("t",(int)'\t'),
                Arguments.of("r",(int)'\r'),
                Arguments.of("'",(int)'\''),
                Arguments.of("/",(int)'/'),
                Arguments.of("\"",(int)'"'),
                Arguments.of("a",0x07),
                Arguments.of("v",0x0B)
        );
    }

    /**
     * Claude Generated
     * 测试单字符转义表逐项（\\ s e b f n t r ' / " a v）
     */
    @ParameterizedTest
    @MethodSource("pullDataForSimpleEscape")
    public void readEscapeSimpleTest(final @Nonnull String body,final int expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(body);
        Assertions.assertEquals(expected,reader.readEscape());
        Assertions.assertEquals(1,reader.getCursor());//单字符转义只消费一位
    }

    public static @Nonnull Stream<Arguments> pullDataForHexEscape(){
        return Stream.of(
                Arguments.of("x41",0x41),
                Arguments.of("xff",0xFF),
                Arguments.of("x0A",0x0A),
                Arguments.of("u5929",0x5929),//天
                Arguments.of("u0041",0x41),
                Arguments.of("U0001F600",0x1F600),//增补平面
                Arguments.of("U00005929",0x5929)
        );
    }

    /**
     * Claude Generated
     * 测试 \xHH、u 前缀四位、U 前缀八位定长十六进制转义（大小写数字均可）
     */
    @ParameterizedTest
    @MethodSource("pullDataForHexEscape")
    public void readEscapeHexTest(final @Nonnull String body,final int expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(body);
        final int cp = reader.readEscape();
        镍测试.LOGGER.info("readEscapeHexTest body=[{}] cp={}",body,cp);
        Assertions.assertEquals(expected,cp);
        Assertions.assertFalse(reader.canRead());//恰好消费完
    }

    public static @Nonnull Stream<Arguments> pullDataForOctalEscape(){
        return Stream.of(
                Arguments.of("101",0x41),//'A'
                Arguments.of("777",511),
                Arguments.of("123",0123)
        );
    }

    /**
     * Claude Generated
     * 测试 default 分支回退的三位八进制转义（\101 → 'A'）
     */
    @ParameterizedTest
    @MethodSource("pullDataForOctalEscape")
    public void readEscapeOctalTest(final @Nonnull String body,final int expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(body);
        Assertions.assertEquals(expected,reader.readEscape());
        Assertions.assertEquals(3,reader.getCursor());
    }

    /**
     * Claude Generated
     * \0 前缀按三位八进制整体读取（与 \101 同走 readInt(3,3)）：\012 得 \n 并消费三位，位数不足报错
     */
    @Test
    public void readEscapeZeroTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("012");
        Assertions.assertEquals(0x0A,reader.readEscape());//八进制 012 = 10
        Assertions.assertEquals(3,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
        Assertions.assertThrows(CommandException.class,() -> withContext("0").readEscape());//位数不足
    }

    /**
     * Claude Generated
     * \0 前缀的读扫一致性：scanEscape 与 readEscape 同按三位八进制消费，截断时扫描发 EOF 信号
     */
    @Test
    public void scanEscapeZeroConsistencyTest() throws Exception {
        final @Nonnull InputReader scanner = new InputReader("012");
        Assertions.assertEquals(0x0A,scanner.scanEscape());
        Assertions.assertEquals(3,scanner.getCursor());//与 readEscape 消费长度一致
        Assertions.assertThrows(NickelScanEOFSignal.class,() -> new InputReader("0").scanEscape());//截断发信号
    }

    public static @Nonnull Stream<Arguments> pullDataForUnicodeName(){
        return Stream.of(
                Arguments.of("N{LATIN SMALL LETTER A}",(int)'a'),
                Arguments.of("N{latin small letter a}",(int)'a'),//名字大小写不敏感
                Arguments.of("N{CJK UNIFIED IDEOGRAPH-5929}",0x5929),//天，名字含数字与连字符
                Arguments.of("N{SPACE}",0x20)
        );
    }

    /**
     * Claude Generated
     * 测试 \N{UNICODE NAME} 经 ICU 按名查字符，大小写不敏感
     */
    @Disabled("测试环境缺少 ICU 字符名数据（UCharacter 初始化 MissingResourceException），完整运行环境下可启用")
    @ParameterizedTest
    @MethodSource("pullDataForUnicodeName")
    public void readEscapeUnicodeNameTest(final @Nonnull String body,final int expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(body);
        Assertions.assertEquals(expected,reader.readEscape());
        Assertions.assertFalse(reader.canRead());
    }

    /**
     * Claude Generated
     * 测试 readUnicodeName 直接调用：读 {NAME} 后游标停在闭括号之后
     */
    @Disabled("测试环境缺少 ICU 字符名数据（UCharacter 初始化 MissingResourceException），完整运行环境下可启用")
    @Test
    public void readUnicodeNameDirectTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("{LATIN SMALL LETTER A}xyz");
        Assertions.assertEquals('a',reader.readUnicodeName());
        Assertions.assertEquals('x',reader.peek());
    }

    /**
     * Claude Generated
     * 测试 isValidUnicodeNameCP 的合法字符集 [A-Za-z0-9 空格 连字符]
     */
    @Test
    public void isValidUnicodeNameCPTest(){
        final @Nonnull InputReader reader = new InputReader("");
        Assertions.assertTrue(reader.isValidUnicodeNameCP('A'));
        Assertions.assertTrue(reader.isValidUnicodeNameCP('z'));
        Assertions.assertTrue(reader.isValidUnicodeNameCP('0'));
        Assertions.assertTrue(reader.isValidUnicodeNameCP(' '));
        Assertions.assertTrue(reader.isValidUnicodeNameCP('-'));
        Assertions.assertFalse(reader.isValidUnicodeNameCP('{'));
        Assertions.assertFalse(reader.isValidUnicodeNameCP('_'));
        Assertions.assertFalse(reader.isValidUnicodeNameCP('天'));
    }

    public static @Nonnull Stream<String> pullDataForEscapeError(){
        return Stream.of(
                "",//EOF：STR_TRUNCATED_ESCAPE
                "x4",//十六进制位数不足
                "xZ1",//非法十六进制位
                "q",//default 八进制分支：剩余不足三位
                "q12",//default 八进制分支：'q' 非八进制数字
                "888",//'8' 非八进制数字
                "N",//\N 后无 {：expect('{') 在 EOF panic
                "N[",//\N 后不是 {
                //按名查询失败（如 N{ZZZZ NOT A NAME}）不收录：需 ICU 数据，测试环境缺失
                "N{LATIN SMALL LETTER A"//未闭合 }
        );
    }

    /**
     * Claude Generated
     * 测试非法转义体逐项 panic 为 CommandException（需绑定 CommandContext）
     */
    @ParameterizedTest
    @MethodSource("pullDataForEscapeError")
    public void readEscapeErrorTest(final @Nonnull String body){
        final @Nonnull InputReader reader = withContext(body);
        Assertions.assertThrows(CommandException.class,reader::readEscape,"body: "+body);
    }

    /**
     * Claude Generated
     * \N{} 空名字应被拒绝：无论 panic 还是 ICU 侧拒绝，都以异常收场
     */
    @Disabled("测试环境缺少 ICU 字符名数据（UCharacter 初始化 MissingResourceException），完整运行环境下可启用")
    @Test
    public void readEscapeEmptyUnicodeNameTest(){
        final @Nonnull InputReader reader = withContext("N{}");
        Assertions.assertThrows(Exception.class,reader::readEscape);
    }

    public static @Nonnull Stream<String> pullDataForScanEscapeEOF(){
        return Stream.of(
                "",//转义体直接 EOF
                "x4",//scanInt 剩余不足
                "u123",
                "U0001F60",
                "q",//default 八进制分支剩余不足
                "N{LATIN SMALL LETTER A"//scanUnicodeName 的 expectOrEnd('}') 在 EOF
        );
    }

    /**
     * Claude Generated
     * 测试 scanEscape 的 EOF 一律抛 NickelScanEOFSignal 单例而非 panic
     */
    @ParameterizedTest
    @MethodSource("pullDataForScanEscapeEOF")
    public void scanEscapeEOFSignalTest(final @Nonnull String body){
        final @Nonnull InputReader reader = new InputReader(body);//无需 context
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,reader::scanEscape)
        );
    }

    /**
     * Claude Generated
     * 测试 scanEscape 正常路径与 readEscape 同样返回码点
     */
    @Test
    public void scanEscapeReturnsValueTest() throws Exception {
        Assertions.assertEquals('\n',new InputReader("n").scanEscape());
        Assertions.assertEquals(0x41,new InputReader("x41").scanEscape());
    }

    /**
     * Claude Generated
     * 测试 scanEscape 的语法错误（非 EOF）仍然 panic 而非 EOF 信号
     */
    @Test
    public void scanEscapeSyntaxErrorStillPanicsTest(){
        final @Nonnull InputReader reader = withContext("xZ1");
        Assertions.assertThrows(CommandException.class,reader::scanEscape);
    }

    /**
     * Claude Generated
     * 测试 readInt(len,pow) 定长定进制读取，恰好消费 len 位
     */
    @Test
    public void readIntFixedTest() throws Exception {
        final @Nonnull InputReader hex = new InputReader("4F");
        Assertions.assertEquals(0x4F,hex.readInt(2,4));
        Assertions.assertEquals(2,hex.getCursor());
        Assertions.assertEquals(0x5929,new InputReader("5929").readInt(4,4));
        Assertions.assertEquals(511,new InputReader("777").readInt(3,3));
        final @Nonnull InputReader tail = new InputReader("41xyz");
        Assertions.assertEquals(0x41,tail.readInt(2,4));
        Assertions.assertEquals('x',tail.peek());//不多消费
    }

    /**
     * Claude Generated
     * 测试 readInt(len,pow) 剩余不足与非法数字位均 panic
     */
    @Test
    public void readIntFixedErrorTest(){
        Assertions.assertThrows(
                CommandException.class,
                () -> withContext("4").readInt(2,4)
        );
        Assertions.assertThrows(
                CommandException.class,
                () -> withContext("4G").readInt(2,4)
        );
    }

    /**
     * Claude Generated
     * 测试 scanInt：长度足够时委托 readInt，不足时抛 EOF 信号
     */
    @Test
    public void scanIntTest() throws Exception {
        Assertions.assertEquals(0x4F,new InputReader("4F").scanInt(2,4));
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,() -> new InputReader("4").scanInt(2,4))
        );
    }

    /**
     * Claude Generated
     * 集成测试：引号串内混合各类转义经 readString 一次读出
     */
    @Test
    public void escapesViaReadStringTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("\"\\u5929\\x41\\n\\101\"");//\N{NAME} 段因测试环境缺 ICU 数据未纳入
        Assertions.assertEquals("天A\nA",reader.readString());
        Assertions.assertFalse(reader.canRead());
    }
}
