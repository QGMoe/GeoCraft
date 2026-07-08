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

package top.qiguaiaaaa.geocraft.api.util.math.vec;

import net.minecraft.util.math.Vec3i;
import top.qiguaiaaaa.geocraft.api.util.math.Int21;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static top.qiguaiaaaa.geocraft.api.util.math.Int21.toInt21;

/**
 * @author QGMoe
 */
public final class RelativeMVec3i {
    public static final ThreadLocal<RelativeMVec3i> MUTABLE = ThreadLocal.withInitial(RelativeMVec3i::new);
    public static final int X_LONG_OFFSET = 42;
    public static final long X_LONG_MASK = Int21.ALL_MASK<<X_LONG_OFFSET;
    public static final int Y_LONG_OFFSET = 21;
    public static final long Y_LONG_MASK = Int21.ALL_MASK << Y_LONG_OFFSET;
    public static final int Z_LONG_OFFSET=0;
    public static final long Z_LONG_MASK = Int21.ALL_MASK<<Z_LONG_OFFSET;

    private int x, y, z;

    public RelativeMVec3i() {
        this(0, 0, 0);
    }

    @Override
    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof RelativeMVec3i)) {
            return false;
        }
        final @Nonnull RelativeMVec3i vec = (RelativeMVec3i) obj;

        if (this.x != vec.x) {
            return false;
        } else if (this.y != vec.y) {
            return false;
        } else {
            return this.z == vec.z;
        }
    }

    public RelativeMVec3i(final int xIn, final int yIn, final int zIn) {
        x = xIn;
        y = yIn;
        z = zIn;
    }

    public RelativeMVec3i setPos(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public RelativeMVec3i setPos(final int cx, final int cy, final int cz, final int ax, final int ay, final int az) {
        return this.setPos(ax - cx, ay - cy, az - cz);
    }

    public RelativeMVec3i setPos(@Nonnull final Vec3i vec3i) {
        return this.setPos(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public RelativeMVec3i setPos(@Nonnull final Vec3i center, @Nonnull final Vec3i to) {
        return this.setPos(center.getX(), center.getY(), center.getZ(), to.getX(), to.getY(), to.getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long toLong() {
        return toInt21(this.x) << RelativeMVec3i.X_LONG_OFFSET | toInt21(this.y) << RelativeMVec3i.Y_LONG_OFFSET | toInt21(this.z);
    }
}
