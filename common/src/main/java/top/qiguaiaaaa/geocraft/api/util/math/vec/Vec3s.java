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
import top.qiguaiaaaa.geocraft.api.util.math.Int10;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static top.qiguaiaaaa.geocraft.api.util.math.Int10.toInt10;

/**
 * @author QiguaiAAAA
 */
public class Vec3s{
    public static final int X_INT_OFFSET = 20;
    public static final int X_INT_MASK = Int10.ALL_MASK<< X_INT_OFFSET;
    public static final int Y_INT_OFFSET = 10;
    public static final int Y_INT_MASK = Int10.ALL_MASK<< Y_INT_OFFSET;
    public static final int Z_INT_OFFSET = 0;
    public static final int Z_INT_MASK = Int10.ALL_MASK<< Z_INT_OFFSET;
    protected short x,y,z;

    public Vec3s(final short x,final short y,final short z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3s(final @Nonnull Vec3s vec){
        this(vec.x, vec.y,vec.z);
    }

    public short getSX() {
        return this.x;
    }

    public short getSY() {
        return this.y;
    }

    public short getSZ() {
        return this.z;
    }

    public int toInt(){
        return toInt10(x)<<20 | toInt10(y) << 10 | toInt10(z);
    }

    @Override
    public int hashCode() {
        return (this.y + this.z * 31) * 31 + this.x;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if(obj instanceof Vec3s){
            Vec3s b = (Vec3s) obj;
            return b.x == x && b.y == y && b.z == z;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getName()+"("+ getSX()+","+ getSY()+","+ getSZ()+")";
    }

    public static class RelativeMVec3s extends Vec3s{
        public static final ThreadLocal<RelativeMVec3s> MUTABLE = ThreadLocal.withInitial(RelativeMVec3s::new);
        public RelativeMVec3s(){
            super((short) 0, (short) 0, (short) 0);
        }

        public RelativeMVec3s(final short ax, final short ay, final short az) {
            super(ax, ay, az);
        }

        public RelativeMVec3s(final int x, final int y, final int z, final int tx, final int ty, final int tz) {
            this((short) (tx-x), (short) (ty-y), (short) (tz-z));
        }
        public RelativeMVec3s(final @Nonnull Vec3i origin, final @Nonnull Vec3i target) {
            this(origin.getX(),origin.getY(),origin.getZ(),target.getX(),target.getY(),target.getZ());
        }

        public RelativeMVec3s setPos(final short x, final short y, final short z){
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public RelativeMVec3s setPos(final int cx, final int cy, final int cz, final int ax, final int ay, final int az){
            return this.setPos((short) (ax-cx),(short) (ay-cy),(short) (az-cz));
        }

        public RelativeMVec3s setPos(@Nonnull final Vec3i center, @Nonnull final Vec3i to){
            return this.setPos(center.getX(),center.getY(),center.getZ(),to.getX(),to.getY(),to.getZ());
        }
    }
}
