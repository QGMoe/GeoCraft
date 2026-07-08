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
import top.qiguaiaaaa.geocraft.api.util.math.Int21;
import top.qiguaiaaaa.geocraft.api.util.math.vec.RelativeMVec3i;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QGMoe
 */
public final class RelativeMVec3iTest {

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testDefaultConstructor() {
        final RelativeMVec3i vec = new RelativeMVec3i();

        assertEquals(0, vec.getX());
        assertEquals(0, vec.getY());
        assertEquals(0, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testConstructorWithCoordinates() {
        final RelativeMVec3i vec =
                new RelativeMVec3i(10, -20, 30);

        assertEquals(10, vec.getX());
        assertEquals(-20, vec.getY());
        assertEquals(30, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosWithCoordinates() {
        final RelativeMVec3i vec =
                new RelativeMVec3i();

        final RelativeMVec3i result =
                vec.setPos(1, 2, 3);

        assertSame(vec, result);
        assertEquals(1, vec.getX());
        assertEquals(2, vec.getY());
        assertEquals(3, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosRelativeCoordinates() {
        final RelativeMVec3i vec =
                new RelativeMVec3i();

        vec.setPos(10, 20, 30, 15, 25, 50);

        assertEquals(5, vec.getX());
        assertEquals(5, vec.getY());
        assertEquals(20, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosWithVec3i() {
        final Vec3i target =
                new Vec3i(100, 200, 300);

        final RelativeMVec3i vec =
                new RelativeMVec3i();

        final RelativeMVec3i result =
                vec.setPos(target);

        assertSame(vec, result);
        assertEquals(100, vec.getX());
        assertEquals(200, vec.getY());
        assertEquals(300, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosWithCenterAndTarget() {
        final Vec3i center =
                new Vec3i(10, 20, 30);

        final Vec3i target =
                new Vec3i(25, 40, 60);

        final RelativeMVec3i vec =
                new RelativeMVec3i();

        vec.setPos(center, target);

        assertEquals(15, vec.getX());
        assertEquals(20, vec.getY());
        assertEquals(30, vec.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToLongPositiveValues() {
        final RelativeMVec3i vec =
                new RelativeMVec3i(1, 2, 3);

        final long result = vec.toLong();

        final long expected =
                Int21.toInt21(1) << RelativeMVec3i.X_LONG_OFFSET |
                        Int21.toInt21(2) << RelativeMVec3i.Y_LONG_OFFSET |
                        Int21.toInt21(3);

        assertEquals(expected, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToLongNegativeValues() {
        final RelativeMVec3i vec =
                new RelativeMVec3i(-1, -2, -3);

        final long result = vec.toLong();

        final long expected =
                Int21.toInt21(-1) << RelativeMVec3i.X_LONG_OFFSET |
                        Int21.toInt21(-2) << RelativeMVec3i.Y_LONG_OFFSET |
                        Int21.toInt21(-3);

        assertEquals(expected, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsSameObject() {
        final RelativeMVec3i vec =
                new RelativeMVec3i(1, 2, 3);

        assertEquals(vec, vec);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsSameValue() {
        final RelativeMVec3i a =
                new RelativeMVec3i(1, 2, 3);

        final RelativeMVec3i b =
                new RelativeMVec3i(1, 2, 3);

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
    public void testEqualsDifferentValue() {
        final RelativeMVec3i a =
                new RelativeMVec3i(1, 2, 3);

        final RelativeMVec3i b =
                new RelativeMVec3i(1, 2, 4);

        assertNotEquals(a, b);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testEqualsOtherObject() {
        final RelativeMVec3i vec =
                new RelativeMVec3i(1, 2, 3);

        assertNotEquals(null, vec);
        assertNotEquals("Vec3i", vec);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testMutableThreadLocal() {
        final RelativeMVec3i a =
                RelativeMVec3i.MUTABLE.get();

        final RelativeMVec3i b =
                RelativeMVec3i.MUTABLE.get();

        assertSame(a, b);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testConstants() {
        assertEquals(42, RelativeMVec3i.X_LONG_OFFSET);
        assertEquals(21, RelativeMVec3i.Y_LONG_OFFSET);
        assertEquals(0, RelativeMVec3i.Z_LONG_OFFSET);

        assertEquals(Int21.ALL_MASK << 42,
                RelativeMVec3i.X_LONG_MASK);
        assertEquals(Int21.ALL_MASK << 21,
                RelativeMVec3i.Y_LONG_MASK);
        assertEquals(Int21.ALL_MASK,
                RelativeMVec3i.Z_LONG_MASK);
    }
}
