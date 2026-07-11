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
 * InputReader 数字读取（readInt/readLong/readDouble/readBoolean/isNumber）测试
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderNumber {
    private static final double EPSILON_DOUBLE = 1e-9;

    private static @Nonnull InputReader withContext(final @Nonnull String input){
        final @Nonnull InputReader reader = new InputReader(input);
        new CommandContext(reader,null,null,null);//构造器自动 input.setContext(this)
        return reader;
    }

    public static @Nonnull Stream<Arguments> pullDataForReadIntValid(){
        return Stream.of(
                Arguments.of("0",0),
                Arguments.of("42",42),
                Arguments.of("+5",5),//前导正号合法
                Arguments.of("-5",-5),
                Arguments.of("2147483647",Integer.MAX_VALUE),
                Arguments.of("-2147483648",Integer.MIN_VALUE),
                Arguments.of("  42",42),//先跳空白
                Arguments.of("12abc",12),//停在首个非数字字符
                Arguments.of("7 8",7),
                Arguments.of("114514",114514)
        );
    }

    /**
     * Claude Generated
     * 测试 readInt 合法输入：正负号、边界值、前导空白与非数字截断
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadIntValid")
    public void readIntValidTest(final @Nonnull String input,final int expected) throws Exception {
        final @Nonnull InputReader reader = new InputReader(input);
        final int value = reader.readInt();
        镍测试.LOGGER.info("readIntValidTest input=[{}] value={}",input,value);
        Assertions.assertEquals(expected,value);
    }

    public static @Nonnull Stream<String> pullDataForReadIntInvalid(){
        return Stream.of(
                "1.5",//'.' 在 isNumber 集合内，整体交给 parseInt 失败
                "--5",
                "++1",
                "+",
                ".",
                "2147483648",//溢出
                "abc",//无数字字符，空串解析失败
                "",
                "   "
        );
    }

    /**
     * Claude Generated
     * 测试 readInt 非法输入逐项 panic 为 CommandException
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadIntInvalid")
    public void readIntInvalidTest(final @Nonnull String input){
        final @Nonnull InputReader reader = withContext(input);
        Assertions.assertThrows(CommandException.class,reader::readInt,"input: "+input);
    }

    /**
     * Claude Generated
     * 测试 readInt 解析失败时游标回滚到跳过空白后的起点
     */
    @Test
    public void readIntRollbackTest(){
        final @Nonnull InputReader overflow = withContext("  2147483648");
        Assertions.assertThrows(CommandException.class,overflow::readInt);
        Assertions.assertEquals(2,overflow.getCursor());//回滚到空白之后
        final @Nonnull InputReader dotted = withContext("1.5");
        Assertions.assertThrows(CommandException.class,dotted::readInt);
        Assertions.assertEquals(0,dotted.getCursor());
    }

    public static @Nonnull Stream<Arguments> pullDataForReadLongValid(){
        return Stream.of(
                Arguments.of("9223372036854775807",Long.MAX_VALUE),
                Arguments.of("-9223372036854775808",Long.MIN_VALUE),
                Arguments.of("100",100L),
                Arguments.of("+7",7L),
                Arguments.of("1919810",1919810L)
        );
    }

    /**
     * Claude Generated
     * 测试 readLong 合法输入与长整型边界
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadLongValid")
    public void readLongValidTest(final @Nonnull String input,final long expected) throws Exception {
        Assertions.assertEquals(expected,new InputReader(input).readLong());
    }

    /**
     * Claude Generated
     * 测试 readLong 溢出与非法输入 panic 并回滚游标
     */
    @Test
    public void readLongInvalidTest(){
        final @Nonnull InputReader overflow = withContext("9223372036854775808");
        Assertions.assertThrows(CommandException.class,overflow::readLong);
        Assertions.assertEquals(0,overflow.getCursor());
        Assertions.assertThrows(CommandException.class,withContext("1.0")::readLong);
        Assertions.assertThrows(CommandException.class,withContext("")::readLong);
    }

    public static @Nonnull Stream<Arguments> pullDataForReadDoubleValid(){
        return Stream.of(
                Arguments.of("1.5",1.5),
                Arguments.of(".5",0.5),
                Arguments.of("5.",5.0),
                Arguments.of("-2.5",-2.5),
                Arguments.of("+3.25",3.25),
                Arguments.of("0",0.0),
                Arguments.of("  1.5",1.5),
                Arguments.of("-0.0",-0.0)
        );
    }

    /**
     * Claude Generated
     * 测试 readDouble 合法输入：小数点前后省略、正负号、前导空白
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadDoubleValid")
    public void readDoubleValidTest(final @Nonnull String input,final double expected) throws Exception {
        Assertions.assertEquals(expected,new InputReader(input).readDouble(),EPSILON_DOUBLE,"input: "+input);
    }

    /**
     * Claude Generated
     * 预期行为：readDouble 不支持科学计数法——'e' 不在 isNumber 集合，消费停在 'e'
     */
    @Test
    public void readDoubleNoScientificNotationTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("1e5");
        Assertions.assertEquals(1.0,reader.readDouble(),EPSILON_DOUBLE);
        Assertions.assertEquals("e5",reader.readRemaining());//指数部分留在流中
        final @Nonnull InputReader upper = new InputReader("1E5");
        Assertions.assertEquals(1.0,upper.readDouble(),EPSILON_DOUBLE);
        final @Nonnull InputReader frac = new InputReader("1.5e-3");
        Assertions.assertEquals(1.5,frac.readDouble(),EPSILON_DOUBLE);
        Assertions.assertEquals('e',frac.peek());
    }

    public static @Nonnull Stream<String> pullDataForReadDoubleInvalid(){
        return Stream.of(
                "..",
                "1.2.3",
                "1.5.",
                "-",
                "+",
                "",
                "abc"
        );
    }

    /**
     * Claude Generated
     * 测试 readDouble 非法输入逐项 panic 为 CommandException
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadDoubleInvalid")
    public void readDoubleInvalidTest(final @Nonnull String input){
        final @Nonnull InputReader reader = withContext(input);
        Assertions.assertThrows(CommandException.class,reader::readDouble,"input: "+input);
    }

    /**
     * Claude Generated
     * 测试 readDouble 解析失败时游标回滚到跳过空白后的起点
     */
    @Test
    public void readDoubleRollbackTest(){
        final @Nonnull InputReader reader = withContext(" ..");
        Assertions.assertThrows(CommandException.class,reader::readDouble);
        Assertions.assertEquals(1,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试静态 isNumber：0-9、+、-、. 属数字字符集，e/E 与其他字符不属
     */
    @Test
    public void isNumberTest(){
        for(int cp='0';cp<='9';cp++) Assertions.assertTrue(InputReader.isNumber(cp),"digit: "+(char)cp);
        Assertions.assertTrue(InputReader.isNumber('+'));
        Assertions.assertTrue(InputReader.isNumber('-'));
        Assertions.assertTrue(InputReader.isNumber('.'));
        Assertions.assertFalse(InputReader.isNumber('e'));
        Assertions.assertFalse(InputReader.isNumber('E'));
        Assertions.assertFalse(InputReader.isNumber(' '));
        Assertions.assertFalse(InputReader.isNumber('a'));
        Assertions.assertFalse(InputReader.isNumber('天'));
    }

    public static @Nonnull Stream<Arguments> pullDataForReadBooleanValid(){
        return Stream.of(
                Arguments.of("true",true),
                Arguments.of("1",true),
                Arguments.of("false",false),
                Arguments.of("0",false),
                Arguments.of("  true",true),//先跳空白
                Arguments.of("false rest",false)//按 token 截断
        );
    }

    /**
     * Claude Generated
     * 测试 readBoolean 合法输入：true/1/false/0（合法路径不依赖 context）
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadBooleanValid")
    public void readBooleanValidTest(final @Nonnull String input,final boolean expected) throws Exception {
        Assertions.assertEquals(expected,new InputReader(input).readBoolean(),"input: "+input);
    }

    public static @Nonnull Stream<String> pullDataForReadBooleanInvalid(){
        return Stream.of(
                "True",//大小写敏感
                "FALSE",
                "yes",
                "2",
                "tru",
                ""
        );
    }

    /**
     * Claude Generated
     * 测试 readBoolean 非法 token（含大小写不符与空输入）panic 为 CommandException
     */
    @ParameterizedTest
    @MethodSource("pullDataForReadBooleanInvalid")
    public void readBooleanInvalidTest(final @Nonnull String input){
        final @Nonnull InputReader reader = withContext(input);
        Assertions.assertThrows(CommandException.class,reader::readBoolean,"input: "+input);
    }

    /**
     * Claude Generated
     * 测试 readBoolean 顺序读取多个布尔值
     */
    @Test
    public void readBooleanSequenceTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("true false 1 0");
        Assertions.assertTrue(reader.readBoolean());
        Assertions.assertFalse(reader.readBoolean());
        Assertions.assertTrue(reader.readBoolean());
        Assertions.assertFalse(reader.readBoolean());
        Assertions.assertTrue(reader.isRemainingEmpty());
    }

    /**
     * Claude Generated
     * 测试数字读取的顺序组合：int、long、double 依次从同一流读出
     */
    @Test
    public void mixedNumberSequenceTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("42 1919810 -2.5");
        Assertions.assertEquals(42,reader.readInt());
        Assertions.assertEquals(1919810L,reader.readLong());
        Assertions.assertEquals(-2.5,reader.readDouble(),EPSILON_DOUBLE);
        Assertions.assertTrue(reader.isRemainingEmpty());
    }
}
