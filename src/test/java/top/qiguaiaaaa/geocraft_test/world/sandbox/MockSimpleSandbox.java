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

package top.qiguaiaaaa.geocraft_test.world.sandbox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public class MockSimpleSandbox implements IMockSandbox {

    protected final IBlockState[][][] structure; //y,z,x

    protected @Nullable IBlockState outerBlock;

    public MockSimpleSandbox(final @Nonnull IBlockState[][][] structure) {
        this.structure = structure;
    }

    public void setOuterBlock(final @Nullable IBlockState outer){
        this.outerBlock = outer;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final @Nonnull BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(final @Nonnull BlockPos pos,final int lightValue) {
        return lightValue;
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(final @Nonnull BlockPos pos) {
        if(isOutOfRange(pos)){
            if(outerBlock != null) return outerBlock;
            return Assertions.fail("Out of Range");
        }
        return structure[pos.getY()][pos.getZ()][pos.getX()];
    }

    @Override
    public boolean isAirBlock(final @Nonnull BlockPos pos) {
        final @Nonnull IBlockState state = getBlockState(pos);
        return state.getBlock().isAir(state,this,pos);
    }

    @Override
    @Nonnull
    public Biome getBiome(final @Nonnull BlockPos pos) {
        return Assertions.fail("Don't support biome");
    }

    @Override
    public int getStrongPower(final @Nonnull BlockPos pos,final @Nonnull EnumFacing direction) {
        return 0;
    }

    @Override
    @Nonnull
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideSolid(@Nonnull final BlockPos pos,
                               @Nonnull final EnumFacing side,
                               final boolean _default) {
        final @Nonnull IBlockState state = getBlockState(pos);
        if (state.isTopSolid() && side == EnumFacing.UP) return true;
        return state.isNormalCube();
    }

    protected boolean isOutOfRange(final int x,final int y,final int z){
        return !(y < structure.length && z < structure[y].length && x < structure[y][x].length);
    }

    @Override
    public boolean isOutOfRange(@Nonnull final BlockPos pos) {
        return isOutOfRange(pos.getX(),pos.getY(),pos.getZ());
    }

    @Override
    public boolean canSeeSky(@Nonnull BlockPos pos) {
        while (pos.getY()<=structure.length){
            if(!isAirBlock(pos)) return false;
            pos = pos.up();
        }
        return true;
    }

    @Override
    public IBlockState setBlockState(@Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        Assertions.assertNotNull(state);
        Assertions.assertFalse(isOutOfRange(pos));
        final IBlockState old = structure[pos.getY()][pos.getZ()][pos.getX()];
        structure[pos.getY()][pos.getZ()][pos.getX()] = state;
        return old;
    }
}
