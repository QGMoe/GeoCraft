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

package 清汩萌.天圆地方.api;

import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft.api.util.math.Int10;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QGMoe
 */
public final class Int10Test {

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithPositiveByte() {
        final byte value = 100;
        final int result = Int10.toInt10(value);

        assertEquals(value, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithNegativeByte() {
        final byte value = -100;
        final int result = Int10.toInt10(value);

        assertEquals(Int10.SIGN_MASK | ((-value) & Int10.CONTENT_MASK), result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithPositiveShort() {
        final short value = 200;
        final int result = Int10.toInt10(value);

        assertEquals(value, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithNegativeShort() {
        final short value = -200;
        final int result = Int10.toInt10(value);

        assertEquals(Int10.SIGN_MASK | ((-value) & Int10.CONTENT_MASK), result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithPositiveInt() {
        final int value = 400;
        final int result = Int10.toInt10(value);

        assertEquals(value, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToInt10WithNegativeInt() {
        final int value = -400;
        final int result = Int10.toInt10(value);

        assertEquals(Int10.SIGN_MASK | ((-value) & Int10.CONTENT_MASK), result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToIntWithPositiveInt10() {
        final int value = 256;
        final int result = Int10.toInt(value);

        assertEquals(value, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToIntWithNegativeInt10() {
        final int value = Int10.SIGN_MASK | 256;
        final int result = Int10.toInt(value);

        assertEquals(-256, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRoundTripPositiveValues() {
        final int value = 511;

        final int encoded = Int10.toInt10(value);
        final int decoded = Int10.toInt(encoded);

        assertEquals(value, decoded);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRoundTripNegativeValues() {
        final int value = -511;

        final int encoded = Int10.toInt10(value);
        final int decoded = Int10.toInt(encoded);

        assertEquals(value, decoded);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testConstantMasks() {
        assertEquals(0x200, Int10.SIGN_MASK);
        assertEquals(0x1FF, Int10.CONTENT_MASK);
        assertEquals(0x3FF, Int10.ALL_MASK);
    }
}
