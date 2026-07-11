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

import moe.qingu.nickel.reader.InputReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import 清汩萌.镍.镍测试;

import javax.annotation.Nonnull;

/**
 * InputReader 游标读取原语与码点语义测试
 * @author Claude
 * @see InputReader
 */
public final class TestInputReaderCursor {

    /**
     * Claude Generated
     * 测试构造器传 null 抛 NullPointerException
     */
    @Test
    public void constructorNullTest(){
        Assertions.assertThrows(NullPointerException.class,() -> new InputReader(null));
    }

    /**
     * Claude Generated
     * 测试空输入的基本状态：不可读、长度为零、剩余为空
     */
    @Test
    public void emptyInputTest(){
        final @Nonnull InputReader reader = new InputReader("");
        Assertions.assertFalse(reader.canRead());
        Assertions.assertFalse(reader.canRead(1));
        Assertions.assertEquals(0,reader.getLength());
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertTrue(reader.isRemainingEmpty());
        Assertions.assertEquals("",reader.getInput());
    }

    /**
     * Claude Generated
     * canRead(0) 属非法参数（readLen 必须 ≥1），在任何位置（包括空输入与 EOF）都抛 IllegalArgumentException
     */
    @Test
    public void canReadZeroAtEOFTest(){
        Assertions.assertThrows(IllegalArgumentException.class,() -> new InputReader("").canRead(0));
        final @Nonnull InputReader reader = new InputReader("ab");
        reader.read();
        reader.read();
        Assertions.assertFalse(reader.canRead());
        Assertions.assertThrows(IllegalArgumentException.class,() -> reader.canRead(0));
    }

    /**
     * Claude Generated
     * 预期行为：空输入上 peek/read 无保护，抛 ArrayIndexOutOfBoundsException
     */
    @Test
    public void peekReadOnEmptyThrowTest(){
        final @Nonnull InputReader reader = new InputReader("");
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::peek);
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::read);
    }

    /**
     * Claude Generated
     * 测试 peek 不移动游标，重复 peek 结果一致
     */
    @Test
    public void peekDoesNotAdvanceTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        Assertions.assertEquals('a',reader.peek());
        Assertions.assertEquals('a',reader.peek());
        Assertions.assertEquals(0,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试 read 逐码点前进，读尽后越界抛 ArrayIndexOutOfBoundsException
     */
    @Test
    public void readAdvancesTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertEquals('a',reader.read());
        Assertions.assertEquals('b',reader.read());
        Assertions.assertEquals('c',reader.read());
        Assertions.assertEquals(3,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::read);
    }

    /**
     * Claude Generated
     * 测试 canRead(n) 的边界：恰好可读与恰好越界
     */
    @Test
    public void canReadLenBoundaryTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        Assertions.assertTrue(reader.canRead(3));
        Assertions.assertFalse(reader.canRead(4));
        reader.read();
        Assertions.assertTrue(reader.canRead(2));
        Assertions.assertFalse(reader.canRead(3));
    }

    /**
     * Claude Generated
     * 测试 skip 前进一位，EOF 处 skip 为无操作（有 canRead 保护）
     */
    @Test
    public void skipTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        reader.skip();
        Assertions.assertEquals(1,reader.getCursor());
        reader.skip();
        Assertions.assertEquals(2,reader.getCursor());
        reader.skip();//EOF 处无操作
        Assertions.assertEquals(2,reader.getCursor());
        final @Nonnull InputReader empty = new InputReader("");
        empty.skip();
        Assertions.assertEquals(0,empty.getCursor());
    }

    /**
     * Claude Generated
     * 测试 read 后 unread 使 peek 复原
     */
    @Test
    public void unreadRestoresTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        Assertions.assertEquals('a',reader.read());
        reader.unread();
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertEquals('a',reader.peek());
    }

    /**
     * Claude Generated
     * 预期行为：unread 由调用方以 canUnread 自行保证，越界回退后 read 抛越界
     */
    @Test
    public void unreadNoLowerBoundTest(){
        final @Nonnull InputReader reader = new InputReader("a");
        Assertions.assertFalse(reader.canUnread());//游标 0 处不可回退
        reader.unread();
        Assertions.assertEquals(-1,reader.getCursor());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::read);
    }

    /**
     * Claude Generated
     * 测试 canUnread：游标 0 处 false，前进后 true，setCursor(0) 复位后 false
     */
    @Test
    public void canUnreadTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        Assertions.assertFalse(reader.canUnread());
        reader.read();
        Assertions.assertTrue(reader.canUnread());
        reader.read();
        Assertions.assertTrue(reader.canUnread());//EOF 处仍可回退
        reader.setCursor(0);
        Assertions.assertFalse(reader.canUnread());
    }

    /**
     * Claude Generated
     * 测试 skipIf 命中前进返回 true、未命中与 EOF 返回 false 且不动
     */
    @Test
    public void skipIfTest(){
        final @Nonnull InputReader reader = new InputReader("ab");
        Assertions.assertFalse(reader.skipIf('x'));
        Assertions.assertEquals(0,reader.getCursor());
        Assertions.assertTrue(reader.skipIf('a'));
        Assertions.assertEquals(1,reader.getCursor());
        reader.read();
        Assertions.assertFalse(reader.skipIf('b'));//EOF
        Assertions.assertEquals(2,reader.getCursor());
    }

    /**
     * Claude Generated
     * 测试 setCursor/getCursor 往返，越界值不校验（预期行为）
     */
    @Test
    public void setCursorRoundTripTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        reader.setCursor(2);
        Assertions.assertEquals(2,reader.getCursor());
        Assertions.assertEquals('c',reader.peek());
        reader.setCursor(0);
        Assertions.assertEquals('a',reader.peek());
        reader.setCursor(114);//无校验
        Assertions.assertEquals(114,reader.getCursor());
        Assertions.assertFalse(reader.canRead());
        reader.setCursor(-3);//负值同样不校验
        Assertions.assertEquals(-3,reader.getCursor());
    }

    /**
     * Claude Generated
     * 预期行为：setCursor 到长度处后 peek 抛越界
     */
    @Test
    public void setCursorBeyondPeekThrowsTest(){
        final @Nonnull InputReader reader = new InputReader("abc");
        reader.setCursor(reader.getLength());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,reader::peek);
    }

    /**
     * Claude Generated
     * 测试中文输入的码点语义：每字一码点
     */
    @Test
    public void chineseCodepointTest(){
        final @Nonnull InputReader reader = new InputReader("天圆地方");
        Assertions.assertEquals(4,reader.getLength());
        Assertions.assertEquals('天',reader.read());
        Assertions.assertEquals('圆',reader.peek());
        Assertions.assertEquals("圆地",reader.getSubInput(1,3));
        镍测试.LOGGER.info("chineseCodepointTest passed, length={}",reader.getLength());
    }

    /**
     * Claude Generated
     * 测试代理对（增补平面字符）算一个游标单位，getLength 为码点数而非 char 数
     */
    @Test
    public void surrogatePairTest(){
        final @Nonnull String raw = "a😀b";
        final @Nonnull InputReader reader = new InputReader(raw);
        Assertions.assertEquals(4,raw.length());//char 数
        Assertions.assertEquals(3,reader.getLength());//码点数
        Assertions.assertEquals('a',reader.read());
        Assertions.assertEquals(0x1F600,reader.read());//😀
        Assertions.assertEquals('b',reader.read());
        Assertions.assertFalse(reader.canRead());
        Assertions.assertEquals("😀",reader.getSubInput(1,2));
        Assertions.assertEquals(raw,reader.getInput());
    }

    /**
     * Claude Generated
     * 测试 getSubInput 各区间：全段、中段、空段与单参重载
     */
    @Test
    public void getSubInputTest(){
        final @Nonnull InputReader reader = new InputReader("hello");
        Assertions.assertEquals("hello",reader.getSubInput(0,5));
        Assertions.assertEquals("ell",reader.getSubInput(1,4));
        Assertions.assertEquals("",reader.getSubInput(2,2));
        Assertions.assertEquals("lo",reader.getSubInput(3));
        Assertions.assertEquals("hello",reader.getSubInput(0));
    }

    /**
     * Claude Generated
     * 测试 getSubInput 区间 end&lt;begin 抛 IllegalArgumentException
     */
    @Test
    public void getSubInputInvalidRangeTest(){
        final @Nonnull InputReader reader = new InputReader("hello");
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> reader.getSubInput(3,1)
        );
    }

    /**
     * Claude Generated
     * 测试 skipCodepoints 连续跳过指定码点（含增补平面），未命中与 EOF 安全
     */
    @Test
    public void skipCodepointsTest(){
        final @Nonnull InputReader reader = new InputReader("aaab");
        reader.skipCodepoints('a');
        Assertions.assertEquals(3,reader.getCursor());
        reader.skipCodepoints('x');//未命中不动
        Assertions.assertEquals(3,reader.getCursor());
        reader.skipCodepoints('b');//跳完到 EOF 安全停下
        Assertions.assertEquals(4,reader.getCursor());
        final @Nonnull InputReader emoji = new InputReader("😀😀x");
        emoji.skipCodepoints(0x1F600);
        Assertions.assertEquals(2,emoji.getCursor());
        Assertions.assertEquals('x',emoji.peek());
    }

    /**
     * Claude Generated
     * 测试 getInput 返回原始字符串本体
     */
    @Test
    public void getInputTest(){
        final @Nonnull String raw = "  天 'a\"b' 114514";
        final @Nonnull InputReader reader = new InputReader(raw);
        reader.readToken();
        Assertions.assertEquals(raw,reader.getInput());//消费不影响原文
    }
}
