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

package 清汩萌.天圆地方.world;

import com.google.common.annotations.Beta;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import org.junit.jupiter.api.Assertions;
import 清汩萌.天圆地方.world.light.ILightGrid;
import 清汩萌.天圆地方.world.light.NoneLightGrid;
import 清汩萌.天圆地方.world.sandbox.IMockSandbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
@Beta
public class MockSimpleWorld extends World {

    protected IMockSandbox sandbox;
    protected ILightGrid lightGridSky = NoneLightGrid.SKY;
    protected ILightGrid lightGridBlock = NoneLightGrid.BLOCK;
    protected IBlockState air;

    protected MockSimpleWorld(final @Nonnull WorldInfo info,
                              final @Nonnull WorldProvider providerIn,
                              final @Nonnull Profiler profilerIn,
                              final boolean client) {
        super(new SaveHandlerMP(), info, providerIn, profilerIn, client);
    }

    @Nonnull
    public static MockSimpleWorld create(final @Nonnull WorldInfo info,final boolean isClient){
        return create(info,new MockWorldProvider(),isClient);
    }

    @Nonnull
    public static MockSimpleWorld create(final @Nonnull WorldInfo info,final @Nonnull MockWorldProvider provider,final boolean isClient){
        return new MockSimpleWorld(
                info,
                provider,
                new Profiler(),
                isClient
        );
    }

    public void setSandbox(final @Nonnull IMockSandbox sandbox){
        this.sandbox = sandbox;
    }

    public void setLightGridSky(final @Nonnull ILightGrid lightGridSky) {
        Assertions.assertEquals(EnumSkyBlock.SKY, lightGridSky.getType());
        this.lightGridSky = lightGridSky;
    }

    public void setLightGridBlock(final @Nonnull ILightGrid lightGridBlock) {
        Assertions.assertEquals(EnumSkyBlock.BLOCK, lightGridBlock.getType());
        this.lightGridBlock = lightGridBlock;
    }

    public void setAirBlock(final @Nonnull IBlockState air) {
        this.air = air;
    }

    public final @Nonnull IMockSandbox getSandbox() {
        return sandbox;
    }

    @Nonnull
    @Override
    protected IChunkProvider createChunkProvider() {
        return Assertions.fail("Unsupported ChunkProvider");
    }

    @Override
    public boolean isAirBlock(final @Nonnull BlockPos pos) {
        return sandbox.isAirBlock(pos);
    }

    @Override
    protected boolean isChunkLoaded(final int x,final int z,final boolean allowEmpty) {
        return true;
    }

    @Override
    public boolean setBlockState(final @Nonnull BlockPos pos,final @Nonnull IBlockState newState,final int flags) {
        if(sandbox.isOutOfRange(pos)) return false;
        final @Nonnull IBlockState oldState = sandbox.setBlockState(pos,newState);
        this.markAndNotifyBlock(pos, null, oldState, newState, flags);
        return true;
    }

    @Override
    public boolean setBlockToAir(final @Nonnull BlockPos pos) {
        Assertions.assertNotNull(air);
        return this.setBlockState(pos, air);
    }

    @Override
    public void markBlocksDirtyVertical(final int x,final int z,final int y1,final int y2) {
        // do nothing
    }

    @Override
    public void markBlockRangeForRenderUpdate(final @Nonnull BlockPos rangeMin,final @Nonnull BlockPos rangeMax) {
        // do nothing
    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x1,final int y1,final int z1,final int x2,final int y2,final int z2) {
        // do nothing
    }

    @Override
    public void neighborChanged(final @Nonnull BlockPos pos,final @Nonnull Block blockIn,final @Nonnull BlockPos fromPos) {
        if (!this.isRemote) {
            final @Nonnull IBlockState iblockstate = this.getBlockState(pos);

            try {
                iblockstate.neighborChanged(this, pos, blockIn, fromPos);
            } catch (final Throwable throwable) {
                Assertions.fail(throwable);
            }
        }
    }

    @Override
    public void observedNeighborChanged(final @Nonnull BlockPos pos,
                                        final @Nonnull Block changedBlock,
                                        final @Nonnull BlockPos changedBlockPos) {
        if (!this.isRemote) {
            final @Nonnull IBlockState iblockstate = this.getBlockState(pos);

            try {
                iblockstate.getBlock().observedNeighborChange(iblockstate, this, pos, changedBlock, changedBlockPos);
            } catch (final Throwable throwable) {
                Assertions.fail(throwable);
            }
        }
    }

    @Override
    public boolean canSeeSky(@Nonnull final BlockPos pos) {
        return sandbox.canSeeSky(pos);
    }

    @Override
    public int getLight(final @Nonnull BlockPos pos) {
        return Math.max(lightGridSky.getLight(pos),lightGridBlock.getLight(pos));
    }

    @Override
    public int getLight(@Nonnull final BlockPos pos,final boolean checkNeighbors) {
        if (checkNeighbors && this.getBlockState(pos).useNeighborBrightness()) {
            final int up = this.getLight(pos.up(), false);
            final int east = this.getLight(pos.east(), false);
            final int west = this.getLight(pos.west(), false);
            final int south = this.getLight(pos.south(), false);
            final int north = this.getLight(pos.north(), false);
            return Math.max(up,Math.max(east,Math.max(west,Math.max(south,north))));
        } else{
            return getLight(pos);
        }
    }

    @Override
    public int getHeight(final int x,final int z) {
        return Assertions.fail("TO DO"); // TODO : support getHeight()
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getChunksLowestHorizon(final int x,final int z) {
        return Assertions.fail("TO DO"); // TODO: support this
    }

    @Override
    @Deprecated
    public int getLightFromNeighborsFor(final @Nonnull EnumSkyBlock type, final @Nonnull BlockPos pos) {
        if (!this.isValid(pos)) {
            return type.defaultLightValue;
        } else if (!this.isBlockLoaded(pos)) {
            return type.defaultLightValue;
        } else if (this.getBlockState(pos).useNeighborBrightness()) {
            final int up = this.getLightFor(type,pos.up());
            final int east = this.getLightFor(type,pos.east());
            final int west = this.getLightFor(type,pos.west());
            final int south = this.getLightFor(type,pos.south());
            final int north = this.getLightFor(type,pos.north());
            return Math.max(up,Math.max(east,Math.max(west,Math.max(south,north))));
        } else return getLightFor(type,pos);
    }

    @Override
    public int getLightFor(@Nonnull final EnumSkyBlock type,final @Nonnull BlockPos pos) {
        switch (type){
            case SKY:return lightGridSky.getLight(pos);
            case BLOCK:return lightGridBlock.getLight(pos);
            default:return Assertions.fail("NULL Light TYPE!");
        }
    }

    @Override
    public void setLightFor(@Nonnull final EnumSkyBlock type,final @Nonnull BlockPos pos,final int lightValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCombinedLight(@Nonnull final BlockPos pos,final int lightValue) {
        return sandbox.getCombinedLight(pos,lightValue);
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(final @Nonnull BlockPos pos) {
        return sandbox.getBlockState(pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final @Nonnull BlockPos pos) {
        return sandbox.getTileEntity(pos);
    }

    @Override
    public int getStrongPower(final @Nonnull BlockPos pos,final @Nonnull EnumFacing direction) {
        return sandbox.getStrongPower(pos,direction);
    }

    @Override
    public boolean isSideSolid(@Nonnull final BlockPos pos,final @Nonnull EnumFacing side,final boolean _default) {
        return sandbox.isSideSolid(pos,side,_default);
    }
}
