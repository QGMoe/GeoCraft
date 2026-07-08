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

package 清汩萌.天圆地方.api.vec;

import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft.api.util.math.Int10;
import top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QGMoe
 */
public final class Vec3sTest {

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testConstructorAndGetters() {
        final short x = 10;
        final short y = -20;
        final short z = 30;

        final Vec3s vec = new Vec3s(x, y, z);

        assertEquals(x, vec.getSX());
        assertEquals(y, vec.getSY());
        assertEquals(z, vec.getSZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testCopyConstructor() {
        final Vec3s original = new Vec3s((short) 1, (short) -2, (short) 3);
        final Vec3s copy = new Vec3s(original);

        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToIntPositiveValues() {
        final Vec3s vec = new Vec3s((short) 1, (short) 2, (short) 3);

        final int result = vec.toInt();

        final int expected =
                Int10.toInt10(1) << 20 |
                        Int10.toInt10(2) << 10 |
                        Int10.toInt10(3);

        assertEquals(expected, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToIntNegativeValues() {
        final Vec3s vec = new Vec3s((short) -1, (short) -2, (short) -3);

        final int result = vec.toInt();

        final int expected =
                Int10.toInt10(-1) << 20 |
                        Int10.toInt10(-2) << 10 |
                        Int10.toInt10(-3);

        assertEquals(expected, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsWithSameValue() {
        final Vec3s a = new Vec3s((short) 5, (short) 6, (short) 7);
        final Vec3s b = new Vec3s((short) 5, (short) 6, (short) 7);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsWithDifferentValue() {
        final Vec3s a = new Vec3s((short) 5, (short) 6, (short) 7);
        final Vec3s b = new Vec3s((short) 5, (short) 6, (short) 8);

        assertNotEquals(a, b);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsWithOtherObject() {
        final Vec3s vec = new Vec3s((short) 1, (short) 2, (short) 3);

        assertNotEquals(null, vec);
        assertNotEquals("Vec3s", vec);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToString() {
        final Vec3s vec = new Vec3s((short) 1, (short) 2, (short) 3);

        assertEquals(
                "top.qiguaiaaaa.geocraft.api.util.math.vec.Vec3s(1,2,3)",
                vec.toString()
        );
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRelativeConstructorWithCoordinates() {
        final Vec3s.RelativeMVec3s vec =
                new Vec3s.RelativeMVec3s(10, 20, 30, 15, 25, 40);

        assertEquals(5, vec.getSX());
        assertEquals(5, vec.getSY());
        assertEquals(10, vec.getSZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRelativeConstructorWithVec3i() {
        final Vec3i origin = new Vec3i(1, 2, 3);
        final Vec3i target = new Vec3i(5, 7, 9);

        final Vec3s.RelativeMVec3s vec =
                new Vec3s.RelativeMVec3s(origin, target);

        assertEquals(4, vec.getSX());
        assertEquals(5, vec.getSY());
        assertEquals(6, vec.getSZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRelativeSetPos() {
        final Vec3s.RelativeMVec3s vec =
                new Vec3s.RelativeMVec3s();

        final Vec3s.RelativeMVec3s result =
                vec.setPos((short) 8, (short) 9, (short) 10);

        assertSame(vec, result);
        assertEquals(8, vec.getSX());
        assertEquals(9, vec.getSY());
        assertEquals(10, vec.getSZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRelativeSetPosWithCoordinates() {
        final Vec3s.RelativeMVec3s vec =
                new Vec3s.RelativeMVec3s();

        vec.setPos(10, 20, 30, 15, 25, 40);

        assertEquals(5, vec.getSX());
        assertEquals(5, vec.getSY());
        assertEquals(10, vec.getSZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testMutableThreadLocal() {
        final Vec3s.RelativeMVec3s a =
                Vec3s.RelativeMVec3s.MUTABLE.get();

        final Vec3s.RelativeMVec3s b =
                Vec3s.RelativeMVec3s.MUTABLE.get();

        assertSame(a, b);
    }
}