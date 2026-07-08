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

package top.qiguaiaaaa.geocraft.api.atmosphere.layer;

import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.state.GeographyState;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 下垫面层级
 * 所有下垫面层级应当从该类继承
 * @since 0.1
 * @author QiguaiAAAA
 */
public abstract class UnderlyingLayer extends BaseLayer{
    protected long heatCapacity; //热容
    protected final Altitude altitude = new Altitude(63); //表层海拔高度

    /**
     * 创建一个下垫面层级
     * @param atmosphere 该层级所在大气
     */
    public UnderlyingLayer(@Nonnull Atmosphere atmosphere) {
        super(atmosphere);
    }

    /**
     * 基于区块加载自身属性
     * @param chunk 下垫面所在区块
     * @return 自身
     */
    public abstract UnderlyingLayer load(@Nonnull Chunk chunk);

    /**
     * 设置地面海拔，类型为游戏海拔
     * @param altitude 类型为游戏海拔
     */
    public void setAltitude(double altitude) {
        if(altitude<0) return;
        this.altitude.set(altitude);
    }

    /**
     * 设置地面海拔
     * @param altitude 目标海拔高度
     */
    public void setAltitude(@Nonnull Altitude altitude) {
        if(altitude.get()<0) return;
        this.altitude.set(altitude);
    }

    /**
     * 获取地面平均海拔
     * @return 地面平均海拔，类型为游戏海拔
     */
    @Nonnull
    public Altitude getAltitude() {
        return altitude;
    }

    /**
     * {@inheritDoc}
     * @param chunk 层级所在区块
     */
    @Override
    public void onLoad(@Nullable Chunk chunk) {
        onLoadWithoutChunk();
        if(chunk == null) return;
        this.load(chunk);
    }

    public void onLoadWithoutChunk() {
        for(GeographyState state:states.values())
            if(!state.isLoaded())
                state.load(this);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isLoaded() {
        return heatCapacity > 0 && super.isLoaded();
    }

    /**
     * {@inheritDoc}
     * 一般情况下下垫面是最下面的一层，所以默认从-4096开始
     * @return {@inheritDoc}
     */
    @Override
    public double getBeginY() {
        return -4096;
    }

    /**
     * {@inheritDoc}
     * 对下垫面层一般没有太大意义，因为一般都是最低一层
     * @return {@inheritDoc}
     */
    @Override
    public double getDepth() {
        return getTopY()-getBeginY();
    }

    /**
     * 获得层级顶端高度，即海拔高度
     * @return 该下垫面的海拔高度
     */
    @Override
    public double getTopY() {
        return altitude.get();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public double getHeatCapacity() {
        return heatCapacity;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nullable
    @Override
    public FluidState getWater() {
        return upperLayer.getWater();
    }
}
