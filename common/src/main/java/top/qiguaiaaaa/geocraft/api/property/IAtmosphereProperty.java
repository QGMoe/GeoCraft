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

package top.qiguaiaaaa.geocraft.api.property;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public interface IAtmosphereProperty extends IGeographyProperty{
    boolean haveWindEffect();
    boolean isFlowable();

    /**
     * 计算本层朝向邻居大气的风速在这一属性上的分量
     * 若windEffect为true则会调用
     * @param self 本层大气
     * @param neighbor 邻居大气
     * @param direction B相对A的方向
     * @return 风速分量
     */
    @Nonnull
    default Vec3d getWind(@Nonnull AtmosphereLayer self, @Nonnull Atmosphere neighbor, @Nonnull EnumFacing direction){
        return Vec3d.ZERO;
    }

    /**
     * 当大气平流的时候
     * 若flowable为true则会调用
     * @param from 源大气层
     * @param to 朝向大气
     * @param direction 朝向方位
     * @param windSpeed 风速，包含垂直分量
     */
    default void onFlow(@Nonnull AtmosphereLayer from, @Nullable Chunk fromChunk, Atmosphere to , @Nullable Chunk toChunk, @Nonnull EnumFacing direction, @Nonnull Vec3d windSpeed){}

    /**
     * 当大气对流的时候
     * 若flowable为true则会调用
     * @param lower 低层
     * @param upper 高层
     * @param speed 风速,正为低层往高层,负为高层往低层
     */
    default void onConvect(@Nonnull AtmosphereLayer lower,@Nonnull AtmosphereLayer upper,double speed){}
}
