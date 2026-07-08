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

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public final class MBlockPos extends BlockPos {
    private int x, y, z;

    public MBlockPos() {
        this(0, 0, 0);
    }

    public MBlockPos(final int xIn, final int yIn, final int zIn) {
        super(xIn, yIn, zIn);
        setPos(xIn, yIn, zIn);
    }

    public MBlockPos(final double xIn, final double yIn, final double zIn) {
        super(xIn, yIn, zIn);
        setPos(xIn, yIn, zIn);
    }

    public MBlockPos(@Nonnull final Entity source) {
        super(source);
        setPos(source);
    }

    public MBlockPos(@Nonnull final Vec3d vec) {
        super(vec);
        setPos(vec);
    }

    public MBlockPos(@Nonnull final Vec3i vec) {
        super(vec);
        setPos(vec);
    }

    @Nonnull
    public MBlockPos addM(final double x, final double y, final double z) {
        return setPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    @Nonnull
    public MBlockPos addM(final int x, final int y, final int z) {
        return setPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    @Nonnull
    public MBlockPos addM(@Nonnull final Vec3i vec) {
        return addM(vec.getX(), vec.getY(), vec.getZ());
    }

    @Nonnull
    public MBlockPos subtractM(@Nonnull final Vec3i vec) {
        return this.addM(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    @Nonnull
    @Override
    public BlockPos toImmutable() {
        return new BlockPos(this);
    }

    @Nonnull
    public MBlockPos upM() {
        return upM(1);
    }

    @Nonnull
    public MBlockPos upM(final int n) {
        return this.offsetM(EnumFacing.UP, n);
    }

    @Nonnull
    public MBlockPos downM() {
        return downM(1);
    }

    @Nonnull
    public MBlockPos downM(final int n) {
        return offsetM(EnumFacing.DOWN, n);
    }

    @Nonnull
    public MBlockPos northM() {
        return this.northM(1);
    }

    @Nonnull
    public MBlockPos northM(final int n) {
        return offsetM(EnumFacing.NORTH, n);
    }

    @Nonnull
    public MBlockPos southM() {
        return southM(1);
    }

    @Nonnull
    public MBlockPos southM(final int n) {
        return offsetM(EnumFacing.SOUTH, n);
    }

    @Nonnull
    public MBlockPos westM() {
        return westM(1);
    }

    @Nonnull
    public MBlockPos westM(final int n) {
        return offsetM(EnumFacing.WEST, n);
    }

    @Nonnull
    public MBlockPos eastM() {
        return eastM(1);
    }

    @Nonnull
    public MBlockPos eastM(final int n) {
        return offsetM(EnumFacing.EAST, n);
    }

    @Nonnull
    public MBlockPos offsetM(@Nonnull final EnumFacing facing) {
        return offsetM(facing, 1);
    }

    @Nonnull
    public MBlockPos offsetM(@Nonnull final EnumFacing facing, int n) {
        return n == 0 ? this :
                addM(facing.getXOffset() * n, facing.getYOffset() * n, facing.getZOffset() * n);
    }

    @Nonnull
    public MBlockPos rotateM(@Nonnull final Rotation rotationIn) {
        switch (rotationIn) {
            case NONE:
            default:
                return this;
            case CLOCKWISE_90:
                return setPos(-this.getZ(), this.getY(), this.getX());
            case CLOCKWISE_180:
                return setPos(-this.getX(), this.getY(), -this.getZ());
            case COUNTERCLOCKWISE_90:
                return setPos(this.getZ(), this.getY(), -this.getX());
        }
    }

    @Nonnull
    public MBlockPos crossProductM(@Nonnull final Vec3i vec) {
        return setPos(this.getY() * vec.getZ() - this.getZ() * vec.getY(),
                this.getZ() * vec.getX() - this.getX() * vec.getZ(),
                this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public MBlockPos setPos(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public MBlockPos setPos(final double x, final double y, final double z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
        return this;
    }

    @Nonnull
    public MBlockPos setPos(@Nonnull final Vec3d vec3d) {
        return this.setPos(vec3d.x, vec3d.y, vec3d.z);
    }

    @Nonnull
    public MBlockPos setPos(@Nonnull final Vec3i vec3i) {
        return this.setPos(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Nonnull
    public MBlockPos setPos(@Nonnull final Entity entity) {
        return this.setPos(entity.posX, entity.posY, entity.posZ);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }
}
