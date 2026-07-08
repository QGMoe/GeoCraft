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

package top.qiguaiaaaa.geocraft.api.event.atmosphere;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 当大气更新的时候会调用
 * @since 0.1
 * @author QiguaiAAAA
 */
public class AtmosphereUpdateEvent extends AtmosphereEvent {
    private final Chunk chunk;
    public AtmosphereUpdateEvent(@Nullable Chunk chunk,@Nonnull Atmosphere atmosphere) {
        super(atmosphere);
        this.chunk =chunk;
    }
    @Nullable
    public Chunk getChunk() {
        return chunk;
    }
    @Nullable
    public World getWorld(){
        if(chunk == null) return null;
        return chunk.getWorld();
    }

    /**
     * 当大气可能在指定地方下雨或雪的时候调用<br/>
     * @since 0.1
     * @author QiguaiAAAA
     */
    @HasResult
    public static class RainAndSnow extends AtmosphereUpdateEvent{
        private final BlockPos randPos;
        private final double rainPossibility;
        private final IAtmosphereAccessor accessor;
        private IBlockState newState;

        private boolean isSnowy = false;

        /**
         * 创建一个雨雪事件
         * @since 0.1
         * @param chunk 发生的区块
         * @param atmosphere 发生的大气
         * @param randPos 预计会下的地方
         * @param possibility 用于参考的下雨、下雪概率，介于0和1之间
         */
        public RainAndSnow(@Nonnull Chunk chunk,@Nonnull Atmosphere atmosphere,@Nonnull IAtmosphereAccessor accessor,@Nonnull BlockPos randPos, double possibility) {
            super(chunk, atmosphere);
            this.accessor = accessor;
            this.randPos = randPos;
            this.rainPossibility = possibility;
        }

        /**
         * 获取将要下的位置
         * @return 一个位置，表示可以放置水或雪的位置
         */
        public BlockPos getRandPos() {
            return randPos;
        }

        /**
         * 获取大气提供的参考概率
         * @return 下雨、雪的概率
         */
        public double getRainPossibility() {
            return rainPossibility;
        }

        /**
         * 设置{@link #randPos}即下雨、雪的位置的方块状态
         * @param newState 新的方块状态
         */
        public void setState(@Nullable IBlockState newState) {
            this.newState = newState;
        }

        /**
         * 获取当前新的方块状态
         * @return 新的方块状态，若没有设置则为null
         */
        @Nullable
        public IBlockState getState() {
            return newState;
        }

        @Nonnull
        @Override
        public Chunk getChunk() {
            assert super.getChunk() != null;
            return super.getChunk();
        }

        @Nonnull
        @Override
        public World getWorld() {
            assert super.getWorld() != null;
            return super.getWorld();
        }

        @Nonnull
        public IAtmosphereAccessor getAccessor() {
            return accessor;
        }

        public void setSnowy(boolean snowy) {
            isSnowy = snowy;
        }

        public boolean isSnowy() {
            return isSnowy;
        }
    }

    /**
     * 当大气更新完成之后发布
     * @since 0.1
     * @author QiguaiAAAA
     */
    public static class Post extends AtmosphereUpdateEvent{
        public final int x,z;

        /**
         * 创建一个大气更新之后发布的事件
         * @since 0.1
         * @param chunk 大气所在区块,若未加载则为null
         * @param atmosphere 大气
         * @param x 大气所在的区块X坐标
         * @param z 大气所在区块的Z坐标
         */
        public Post(@Nullable Chunk chunk,@Nonnull Atmosphere atmosphere, int x, int z) {
            super(chunk, atmosphere);
            this.x = x;
            this.z = z;
        }
    }
}
