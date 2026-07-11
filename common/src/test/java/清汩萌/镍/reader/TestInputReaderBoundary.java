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
import org.junit.jupiter.api.Test;
import 清汩萌.镍.镍测试;

import javax.annotation.Nonnull;

/**
 * InputReader 边界组合测试：getSubInput 派生子 reader 的独立性、各类读取失败后的游标位置、
 * setCursor/unread 连贯性、canRead(n) 极端参数、混合读取序列与空串全方法扫描。
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderBoundary {
    private static final double EPSILON_DOUBLE = 1e-9;

    private static @Nonnull InputReader withContext(final @Nonnull String input){
        final @Nonnull InputReader reader = new InputReader(input);
        new CommandContext(reader,null,null,null);//构造器自动 input.setContext(this)
        return reader;
    }

    /*
     * -------------------
     *  getSubInput 派生子 reader
     * -------------------
     */

    /**
     * Claude Generated
     * 测试从 getSubInput 构造的子 reader 与父 reader 游标完全独立：任一方读取不影响另一方
     */
    @Test
    public void subReaderIndependentCursorTest(){
        final @Nonnull InputReader parent = new InputReader("abc def");
        Assertions.assertEquals('a',parent.read());//父先前进到 1
        final @Nonnull InputReader child = new InputReader(parent.getSubInput(4));//"def"
        Assertions.assertEquals(0,child.getCursor());//子从 0 开始，不继承父游标
        Assertions.assertEquals('d',child.read());
        Assertions.assertEquals(1,parent.getCursor());//子读取不动父
        Assertions.assertEquals('b',parent.read());
        Assertions.assertEquals(1,child.getCursor());//父读取不动子
        Assertions.assertEquals(3,child.getLength());
        Assertions.assertEquals(7,parent.getLength());
    }

    /**
     * Claude Generated
     * 测试子 reader 上再取子输入构造孙 reader，三级游标互不影响
     */
    @Test
    public void subReaderOfSubReaderTest(){
        final @Nonnull InputReader base = new InputReader("one two three");
        final @Nonnull InputReader child = new InputReader(base.getSubInput(4));//"two three"
        final @Nonnull InputReader grand = new InputReader(child.getSubInput(4));//"three"
        Assertions.assertEquals("three",grand.readToken());
        Assertions.assertEquals(0,child.getCursor());//孙读取不动子
        Assertions.assertEquals(0,base.getCursor());//也不动祖
        Assertions.assertEquals("two",child.readToken());
        Assertions.assertEquals("one",base.readToken());
        镍测试.LOGGER.info("subReaderOfSubReaderTest passed, grand=[{}]",grand.getInput());
    }

    /**
     * Claude Generated
     * 测试子 reader 不继承父 reader 的 CommandContext：子无 context 时 panic 走无上下文保护
     */
    @Test
    public void subReaderContextIndependenceTest(){
        final @Nonnull InputReader parent = withContext("\"x");
        final @Nonnull InputReader child = new InputReader(parent.getSubInput(0));
        Assertions.assertThrows(CommandException.class,parent::readString);//父有 context，正常 panic
        Assertions.assertThrows(CommandException.class,child::readString);//子无 context，走无上下文保护
    }

    /**
     * Claude Generated
     * 测试含增补平面字符的区间派生：子 reader 按码点重组，长度与逐码点读取正确
     */
    @Test
    public void subReaderCodepointRangeTest(){
        final @Nonnull InputReader reader = new InputReader("a😀b天");
        final @Nonnull InputReader child = new InputReader(reader.getSubInput(1,3));//"😀b"
        Assertions.assertEquals(2,child.getLength());
        Assertions.assertEquals(0x1F600,child.read());
        Assertions.assertEquals('b',child.read());
        Assertions.assertFalse(child.canRead());
        Assertions.assertEquals(0,reader.getCursor());//派生不消费父输入
        final @Nonnull InputReader full = new InputReader(reader.getSubInput(0));
        Assertions.assertEquals(reader.getInput(),full.getInput());//全区间派生等于原文
        Assertions.assertEquals(reader.getLength(),full.getLength());
    }

    /*
     * -------------------
     *  读取失败后的游标位置
     * -------------------
     */

    /**
     * Claude Generated
     * 预期行为：readString 未闭合引号 panic 后游标停在 EOF（不回滚）
     */
    @Test
    public void readStringUnclosedCursorTest(){
        final @Nonnull InputReader reader = withContext("\"abc");
        Assertions.assertThrows(CommandException.class,reader::readString);
        Assertions.assertEquals(reader.getLength(),reader.getCursor());//停在 EOF
    }

    /**
     * Claude Generated
     * 预期行为：readString 引号内截断转义 panic 后游标停在反斜杠之后
     */
    @Test
    public void readStringTruncatedEscapeCursorTest(){
        final @Nonnull InputReader reader = withContext("\"a\\");
        Assertions.assertThrows(CommandException.class,reader::readString);
        Assertions.assertEquals(3,reader.getCursor());//引号、a、反斜杠均已消费
    }

    /**
     * Claude Generated
     * 预期行为：readEscape 十六进制位数不足 panic 后游标停在前缀字符之后（不回滚）
     */
    @Test
    public void readEscapeTruncatedHexCursorTest(){
        final @Nonnull InputReader reader = withContext("x4");
        Assertions.assertThrows(CommandException.class,reader::readEscape);
        Assertions.assertEquals(1,reader.getCursor());//'x' 已消费，'4' 未动
    }

    /**
     * Claude Generated
     * 预期行为：readInt(len,pow) 遇到非法数字位 panic 后游标停在该非法位之后（已消费）
     */
    @Test
    public void readIntFixedInvalidDigitCursorTest(){
        final @Nonnull InputReader reader = withContext("4G");
        Assertions.assertThrows(CommandException.class,() -> reader.readInt(2,4));
        Assertions.assertEquals(2,reader.getCursor());//非法位 'G' 也被消费
    }

    /**
     * Claude Generated
     * 预期行为：readBoolean 非法 token panic 后游标停在 token 末尾（不回滚，与 readInt 的回滚行为相反）
     */
    @Test
    public void readBooleanFailureCursorTest(){
        final @Nonnull InputReader reader = withContext("yes tail");
        Assertions.assertThrows(CommandException.class,reader::readBoolean);
        Assertions.assertEquals(3,reader.getCursor());//"yes" 已整体消费
        Assertions.assertEquals("tail",reader.readToken());//后续读取仍连贯
    }

    /**
     * Claude Generated
     * 预期行为：readUnicodeName 名字未闭合在 EOF panic，游标停在名字末尾（不触发 ICU 查询）
     */
    @Test
    public void readUnicodeNameUnclosedCursorTest(){
        final @Nonnull InputReader reader = withContext("{ABC");
        Assertions.assertThrows(CommandException.class,reader::readUnicodeName);
        Assertions.assertEquals(4,reader.getCursor());//'{' 与名字字符均已消费
    }

    /**
     * Claude Generated
     * 预期行为：readEscape 走 default 八进制分支失败时，unread 回退后重新消费首位再 panic，游标停在 1
     */
    @Test
    public void readEscapeOctalFallbackFailureCursorTest(){
        final @Nonnull InputReader reader = withContext("q12");
        Assertions.assertThrows(CommandException.class,reader::readEscape);
        Assertions.assertEquals(1,reader.getCursor());//read 'q' → unread → readInt 再 read 'q' 后 panic
    }

    /**
     * Claude Generated
     * 测试 readInt 失败回滚后流仍可用：同一位置换用 readToken 读出原始文本继续解析
     */
    @Test
    public void readIntFailureRecoveryTest() throws Exception {
        final @Nonnull InputReader reader = withContext("1.5 x");
        Assertions.assertThrows(CommandException.class,reader::readInt);
        Assertions.assertEquals(0,reader.getCursor());//回滚到起点
        Assertions.assertEquals(1.5,reader.readDouble(),EPSILON_DOUBLE);//换 readDouble 成功
        Assertions.assertEquals("x",reader.readToken());
        Assertions.assertTrue(reader.isRemainingEmpty());
    }

    /**
     * Claude Generated
     * 预期行为：scanString 未闭合引号与截断转义抛 EOF 信号后游标均停在 EOF（不回滚）
     */
    @Test
    public void scanStringSignalCursorTest(){
        final @Nonnull InputReader unclosed = new InputReader("\"ab");
        Assertions.assertThrows(NickelScanEOFSignal.class,unclosed::scanString);
        Assertions.assertEquals(unclosed.getLength(),unclosed.getCursor());
        final @Nonnull InputReader truncated = new InputReader("\"a\\");
        Assertions.assertThrows(NickelScanEOFSignal.class,truncated::scanString);
        Assertions.assertEquals(truncated.getLength(),truncated.getCursor());
    }

    /*
     * -------------------
     *  setCursor / unread 连贯性
     * -------------------
     */

    /**
     * Claude Generated
     * 测试 setCursor 向前跨越后 peek/read/unread 三者连贯：读后回退再看仍是同一码点
     */
    @Test
    public void setCursorForwardCoherenceTest(){
        final @Nonnull InputReader reader = new InputReader("abcdef");
        reader.setCursor(4);
        Assertions.assertEquals('e',reader.peek());
        Assertions.assertEquals('e',reader.read());
        Assertions.assertEquals(5,reader.getCursor());
        reader.unread();
        Assertions.assertEquals(4,reader.getCursor());
        Assertions.assertEquals('e',reader.peek());//回退后复原
        reader.setCursor(2);
        Assertions.assertEquals('c',reader.read());//向后跳同样连贯
    }

    /**
     * Claude Generated
     * 测试读尽到 EOF 后 setCursor 回跳可重放同一内容，重放结果一致
     */
    @Test
    public void setCursorReplayReadTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertEquals("abc",reader.readRemaining());
        Assertions.assertFalse(reader.canRead());
        reader.setCursor(0);
        Assertions.assertTrue(reader.canRead());
        Assertions.assertEquals("abc",reader.readRemaining());//重放一致
        reader.setCursor(1);
        Assertions.assertEquals("bc",reader.readRemaining());
    }

    /**
     * Claude Generated
     * 测试 unread 一路回退到 0 后再 unread：游标进入 -1，peek 越界，setCursor(0) 可恢复
     */
    @Test
    public void unreadToZeroThenUnreadTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        reader.read();
        reader.read();
        reader.unread();
        reader.unread();
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertEquals('a',reader.peek());//回到起点连贯
        reader.unread();//越过下界
        Assertions.assertEquals(-1,reader.getCursor());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::peek);
        reader.setCursor(0);//显式复位可恢复
        Assertions.assertEquals('a',reader.read());
    }

    /**
     * Claude Generated
     * 测试 setCursor 到长度处（EOF）后 unread 一步即回到最后一个码点，读取恢复可用
     */
    @Test
    public void setCursorToEndThenUnreadTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        reader.setCursor(reader.getLength());
        Assertions.assertFalse(reader.canRead());
        reader.unread();
        Assertions.assertEquals(2,reader.getCursor());
        Assertions.assertEquals('c',reader.read());
        Assertions.assertFalse(reader.canRead());
    }

    /*
     * -------------------
     *  skip 系列边界
     * -------------------
     */

    /**
     * Claude Generated
     * 测试 skipContents 的边界输入：空串不动、纯空白起点不动、跳过后再次调用为无操作
     */
    @Test
    public void skipContentsBoundaryTest(){
        final @Nonnull InputReader empty = new InputReader("");
        empty.skipContents();
        Assertions.assertEquals(0,empty.getCursor());
        final @Nonnull InputReader blank = new InputReader("   ");
        blank.skipContents();//起点即空白，不动
        Assertions.assertEquals(0,blank.getCursor());
        final @Nonnull InputReader mixed = new InputReader("  ab");
        mixed.skipContents();
        Assertions.assertEquals(0,mixed.getCursor());
        mixed.skipWhitespaces();
        mixed.skipContents();
        Assertions.assertEquals(4,mixed.getCursor());//到 EOF
        mixed.skipContents();//EOF 处再调仍无操作
        mixed.skipWhitespaces();
        Assertions.assertEquals(4,mixed.getCursor());
    }

    /**
     * Claude Generated
     * 测试 skipWhitespaces 与 skipContents 交替推进，逐步断言游标落点直至 EOF
     */
    @Test
    public void alternatingSkipWalkTest(){
        final @Nonnull InputReader reader = new InputReader("a bb  ccc ");
        reader.skipContents();
        Assertions.assertEquals(1,reader.getCursor());
        reader.skipWhitespaces();
        Assertions.assertEquals(2,reader.getCursor());
        reader.skipContents();
        Assertions.assertEquals(4,reader.getCursor());
        reader.skipWhitespaces();
        Assertions.assertEquals(6,reader.getCursor());
        reader.skipContents();
        Assertions.assertEquals(9,reader.getCursor());
        reader.skipWhitespaces();
        Assertions.assertEquals(10,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
    }

    /*
     * -------------------
     *  canRead(n) 极端参数
     * -------------------
     */

    /**
     * Claude Generated
     * canRead(Integer.MAX_VALUE)：任意游标位置下 cursor+readLen 的溢出被检出并返回 false
     */
    @Test
    public void canReadOverflowTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertFalse(reader.canRead(Integer.MAX_VALUE));//cursor=0：cursor+MAX 不溢出，正确 false
        Assertions.assertFalse(reader.canRead(100));//普通大 n 正确返回 false
        reader.read();
        Assertions.assertFalse(reader.canRead(Integer.MAX_VALUE));//cursor=1：溢出被检出，返回 false
        reader.read();
        Assertions.assertFalse(reader.canRead(Integer.MAX_VALUE));//cursor=2：同上
        镍测试.LOGGER.info("canReadOverflowTest passed, cursor={}",reader.getCursor());
    }

    /**
     * Claude Generated
     * canRead(非正数) 属非法参数（readLen 必须 ≥1），在任何位置均抛 IllegalArgumentException
     */
    @Test
    public void canReadNegativeLenTest(){
        final @Nonnull InputReader reader = new InputReader("a");
        Assertions.assertThrows(IllegalArgumentException.class,() -> reader.canRead(-1));
        reader.read();
        Assertions.assertFalse(reader.canRead());
        Assertions.assertThrows(IllegalArgumentException.class,() -> reader.canRead(-1));//EOF 处同样校验
        Assertions.assertThrows(IllegalArgumentException.class,() -> new InputReader("").canRead(-5));//空输入同样
    }

    /**
     * Claude Generated
     * 测试 setCursor 落点处 canRead(n) 的剩余边界：n=剩余为 true，n=剩余+1 为 false
     */
    @Test
    public void canReadAfterSetCursorBoundaryTest(){
        final @Nonnull InputReader reader = new InputReader("abcde");
        reader.setCursor(2);
        Assertions.assertTrue(reader.canRead(3));//剩余恰好 3
        Assertions.assertFalse(reader.canRead(4));
        reader.setCursor(5);
        Assertions.assertFalse(reader.canRead(1));
        Assertions.assertThrows(IllegalArgumentException.class,() -> reader.canRead(0));//n=0 属非法参数
    }

    /*
     * -------------------
     *  混合读取序列
     * -------------------
     */

    /**
     * Claude Generated
     * 混合序列压力：同一输入上依次 readToken/readInt/readString/skipWhitespaces/readDouble/readBoolean/readRemaining，每步断言游标
     */
    @Test
    public void mixedSequenceScriptTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("cmd 42 \"a b\" 天圆 -3.5 true  tail rest");
        Assertions.assertEquals(36,reader.getLength());
        Assertions.assertEquals("cmd",reader.readToken());
        Assertions.assertEquals(3,reader.getCursor());
        Assertions.assertEquals(42,reader.readInt());
        Assertions.assertEquals(6,reader.getCursor());
        Assertions.assertEquals("a b",reader.readString());
        Assertions.assertEquals(12,reader.getCursor());//停在闭合引号后
        reader.skipWhitespaces();
        Assertions.assertEquals(13,reader.getCursor());
        Assertions.assertEquals("天圆",reader.readToken());
        Assertions.assertEquals(15,reader.getCursor());
        Assertions.assertEquals(-3.5,reader.readDouble(),EPSILON_DOUBLE);
        Assertions.assertEquals(20,reader.getCursor());
        Assertions.assertTrue(reader.readBoolean());
        Assertions.assertEquals(25,reader.getCursor());
        Assertions.assertEquals("tail",reader.readToken());
        Assertions.assertEquals(31,reader.getCursor());//双空格被 readToken 一并跳过
        Assertions.assertEquals(" rest",reader.readRemaining());//readRemaining 保留前导空白
        Assertions.assertEquals(36,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
        镍测试.LOGGER.info("mixedSequenceScriptTest passed, cursor={}",reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试混合序列中途记录游标、读完后 setCursor 回跳重放，两轮读取结果一致
     */
    @Test
    public void mixedSequenceReplayTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("foo 42 \"x\"");
        Assertions.assertEquals("foo",reader.readToken());
        final int mark = reader.getCursor();
        Assertions.assertEquals(42,reader.readInt());
        Assertions.assertEquals("x",reader.readString());
        Assertions.assertFalse(reader.canRead());
        reader.setCursor(mark);
        Assertions.assertEquals(42,reader.readInt());//重放与首轮一致
        Assertions.assertEquals("x",reader.readString());
        Assertions.assertTrue(reader.isRemainingEmpty());
    }

    /**
     * Claude Generated
     * 测试含增补平面字符的混合序列：read/readInt/readString（带转义）/readToken 交替，游标按码点推进
     */
    @Test
    public void mixedSequenceSurrogateTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("😀 12 \"x\\u5929\" 😀end");
        Assertions.assertEquals(19,reader.getLength());
        Assertions.assertEquals(0x1F600,reader.read());
        Assertions.assertEquals(1,reader.getCursor());
        Assertions.assertEquals(12,reader.readInt());
        Assertions.assertEquals(4,reader.getCursor());
        Assertions.assertEquals("x天",reader.readString());
        Assertions.assertEquals(14,reader.getCursor());
        Assertions.assertEquals("😀end",reader.readToken());
        Assertions.assertEquals(19,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
    }

    /*
     * -------------------
     *  空串 reader 全方法扫描
     * -------------------
     */

    /**
     * Claude Generated
     * 空串扫描（不抛异常组）：所有跳过/读取/查询方法在空输入上安全返回且游标始终为 0
     */
    @Test
    public void emptyReaderNonThrowingSweepTest() throws Exception {
        final @Nonnull InputReader reader = new InputReader("");
        Assertions.assertEquals("",reader.readToken());
        Assertions.assertEquals("",reader.readString());//无引号退化为 readToken
        reader.scanString();//退化为 skipContents，无操作
        Assertions.assertEquals("",reader.readRemaining());
        reader.skip();
        reader.skipWhitespaces();
        reader.skipContents();
        reader.skipCodepoints('a');
        Assertions.assertFalse(reader.skipIf('a'));
        Assertions.assertTrue(reader.isRemainingEmpty());
        Assertions.assertEquals("",reader.getSubInput(0,0));
        Assertions.assertEquals("",reader.getSubInput(0));
        Assertions.assertEquals(0,reader.getCursor());//全程游标不动
    }

    /**
     * Claude Generated
     * 空串扫描（panic 组）：各读取方法在空输入上均抛 CommandException 且失败后游标保持 0
     */
    @Test
    public void emptyReaderFailureCursorSweepTest(){
        final @Nonnull InputReader reader = withContext("");
        Assertions.assertThrows(CommandException.class,reader::readBoolean);
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,reader::readInt);
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,reader::readLong);
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,reader::readDouble);
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,reader::readEscape);
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,() -> reader.readInt(1,4));
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertThrows(CommandException.class,() -> reader.expect('a'));
        Assertions.assertEquals(0,reader.getCursor());
    }

    /**
     * Claude Generated
     * 空串扫描（EOF 信号组）：scan 系列在空输入上抛 NickelScanEOFSignal 单例且游标保持 0
     */
    @Test
    public void emptyReaderScanSignalSweepTest(){
        final @Nonnull InputReader reader = new InputReader("");//信号路径不依赖 context
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,() -> reader.scanInt(1,4))
        );
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,reader::scanEscape)
        );
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertSame(
                NickelScanEOFSignal.INSTANCE,
                Assertions.assertThrows(NickelScanEOFSignal.class,() -> reader.scanUnicodeName())
        );
        Assertions.assertEquals(0,reader.getCursor());
    }
}
