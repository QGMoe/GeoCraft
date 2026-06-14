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

package top.qiguaiaaaa.geocraft.block.soil;

import net.minecraft.block.*;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.HUMIDITY;

/**
 * @author QGMoe
 */
@SuppressWarnings("deprecation")
public final class BlockSoilExtends {
    private BlockSoilExtends(){}

    public static class Clay extends BlockClay {

        public Clay(){
            this.setDefaultState(this.blockState.getBaseState().withProperty(HUMIDITY, 0));
            this.setSoundType(SoundType.GROUND);
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(final int meta) {
            if(meta>=5) return this.getDefaultState();
            return this.getDefaultState().withProperty(HUMIDITY,meta);
        }

        @Override
        public int getMetaFromState(@Nonnull final IBlockState state) {
            return 0;
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this,HUMIDITY);
        }
    }


    /**
     * @author QGMoe
     */
    public static class Dirt extends BlockDirt {

        public Dirt(){
            this.setDefaultState((this.blockState.getBaseState().
                    withProperty(VARIANT, BlockDirt.DirtType.DIRT)
                    .withProperty(SNOWY, Boolean.FALSE)
                    .withProperty(HUMIDITY, 0)));
            this.setSoundType(SoundType.GROUND);
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(final int meta) {
            return this.getDefaultState()
                    .withProperty(VARIANT, BlockDirt.DirtType.byMetadata(meta%3))
                    .withProperty(HUMIDITY,Math.min(meta/3,4));
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, VARIANT, SNOWY, HUMIDITY);
        }
    }

    public static class Grass extends BlockGrass {

        public Grass(){
            this.setDefaultState(this.blockState.getBaseState()
                    .withProperty(SNOWY, Boolean.FALSE)
                    .withProperty(HUMIDITY, 0));
            this.setSoundType(SoundType.PLANT);
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(int meta) {
            if(meta>4) return this.getDefaultState();
            return this.getDefaultState().withProperty(HUMIDITY,meta);
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, SNOWY, HUMIDITY);
        }
    }

    public static class GrassPath extends BlockGrassPath {

        public GrassPath(){
            this.setDefaultState((this.blockState.getBaseState().withProperty(HUMIDITY, 0)));
            this.setSoundType(SoundType.PLANT);
            this.disableStats();
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(final int meta) {
            if(meta>4) return getDefaultState();
            return this.getDefaultState().withProperty(HUMIDITY,meta);
        }

        @Override
        public int getMetaFromState(@Nonnull final IBlockState state) {
            return 0;
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this,HUMIDITY);
        }
    }

    public static class Gravel extends BlockGravel {

        public Gravel(){
            this.setDefaultState(this.blockState.getBaseState().withProperty(HUMIDITY,0));
            this.setSoundType(SoundType.GROUND);
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(final int meta) {
            if(meta>=5) return this.getDefaultState();
            return this.getDefaultState().withProperty(HUMIDITY,meta);
        }

        @Override
        public int getMetaFromState(@Nonnull final IBlockState state) {
            return 0;
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this,HUMIDITY);
        }
    }

    public static class Sand extends BlockSand {

        public Sand(){
            this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockSand.EnumType.SAND).withProperty(HUMIDITY,0));
            this.setSoundType(SoundType.SAND);
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(int meta) {
            if(meta>=10) return this.getDefaultState();
            return this.getDefaultState().withProperty(VARIANT,BlockSand.EnumType.byMetadata(meta%2)).withProperty(HUMIDITY,meta/2);
        }

        @Nonnull
        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, VARIANT,HUMIDITY);
        }
    }
}
