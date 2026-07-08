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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft.api.util.math.vec.MBlockPos;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author QGMoe
 */
public final class MBlockPosTest {

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testDefaultConstructor() {
        final MBlockPos pos = new MBlockPos();

        assertEquals(0, pos.getX());
        assertEquals(0, pos.getY());
        assertEquals(0, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testIntConstructor() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        assertEquals(1, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(3, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testDoubleConstructor() {
        final MBlockPos pos =
                new MBlockPos(1.9, 2.9, 3.9);

        assertEquals(1, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(3, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosInt() {
        final MBlockPos pos = new MBlockPos();

        final MBlockPos result =
                pos.setPos(5, 6, 7);

        assertSame(pos, result);
        assertEquals(5, pos.getX());
        assertEquals(6, pos.getY());
        assertEquals(7, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosDouble() {
        final MBlockPos pos = new MBlockPos();

        pos.setPos(5.8, 6.7, 7.6);

        assertEquals(5, pos.getX());
        assertEquals(6, pos.getY());
        assertEquals(7, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testAddMWithInt() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        final MBlockPos result =
                pos.addM(4, 5, 6);

        assertSame(pos, result);
        assertEquals(5, pos.getX());
        assertEquals(7, pos.getY());
        assertEquals(9, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testAddMWithDouble() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.addM(1.5, 2.5, 3.5);

        assertEquals(2, pos.getX());
        assertEquals(4, pos.getY());
        assertEquals(6, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testAddMWithVec3i() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        final Vec3i vec =
                new Vec3i(4, 5, 6);

        pos.addM(vec);

        assertEquals(5, pos.getX());
        assertEquals(7, pos.getY());
        assertEquals(9, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSubtractM() {
        final MBlockPos pos =
                new MBlockPos(10, 20, 30);

        pos.subtractM(new Vec3i(1, 2, 3));

        assertEquals(9, pos.getX());
        assertEquals(18, pos.getY());
        assertEquals(27, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testOffsetDirections() {
        final MBlockPos pos =
                new MBlockPos(0, 0, 0);

        pos.upM();

        assertEquals(1, pos.getY());

        pos.downM(2);
        assertEquals(-1, pos.getY());

        pos.eastM();
        assertEquals(1, pos.getX());

        pos.westM(2);
        assertEquals(-1, pos.getX());

        pos.southM();
        assertEquals(1, pos.getZ());

        pos.northM(2);
        assertEquals(-1, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testOffsetZeroReturnsSameObject() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        final MBlockPos result =
                pos.offsetM(EnumFacing.UP, 0);

        assertSame(pos, result);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRotateNone() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.rotateM(Rotation.NONE);

        assertEquals(1, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(3, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRotate90() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.rotateM(Rotation.CLOCKWISE_90);

        assertEquals(-3, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(1, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRotate180() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.rotateM(Rotation.CLOCKWISE_180);

        assertEquals(-1, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(-3, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testRotateCounterClockwise90() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.rotateM(Rotation.COUNTERCLOCKWISE_90);

        assertEquals(3, pos.getX());
        assertEquals(2, pos.getY());
        assertEquals(-1, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testCrossProduct() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        pos.crossProductM(new Vec3i(4, 5, 6));

        assertEquals(-3, pos.getX());
        assertEquals(6, pos.getY());
        assertEquals(-3, pos.getZ());
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testToImmutable() {
        final MBlockPos pos =
                new MBlockPos(1, 2, 3);

        final BlockPos immutable =
                pos.toImmutable();

        assertEquals(1, immutable.getX());
        assertEquals(2, immutable.getY());
        assertEquals(3, immutable.getZ());

        assertNotSame(pos, immutable);
    }

    /**
     * ChatGPT Generated
     *
     * @author QGMoe
     * @author ChatGPT
     */
    @Test
    public void testSetPosVec3d() {
        final MBlockPos pos = new MBlockPos();

        pos.setPos(new Vec3d(8.5, 9.5, 10.5));

        assertEquals(8, pos.getX());
        assertEquals(9, pos.getY());
        assertEquals(10, pos.getZ());
    }
}
