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

package top.qiguaiaaaa.geocraft.api.setting;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * 查询天圆地方关于流体的配置
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class GeoFluidSetting {
    private static final HashSet<String> FLUIDS_NOT_TO_BE_PHYSICAL = new HashSet<>();
    private static final HashSet<String> FLUIDS_BUCKET_TO_BE_VANILLA = new HashSet<>();

    private static final Int2DoubleOpenHashMap GRAVITIES = new Int2DoubleOpenHashMap();

    /**
     * 设置指定流体是否需要被物理化
     * @since 0.1
     * @param fluidName 流体名
     * @param physical 是否需要物理化
     */
    public static void setFluidToBePhysical(String fluidName,boolean physical){
        if(physical) FLUIDS_NOT_TO_BE_PHYSICAL.remove(fluidName);
        else FLUIDS_NOT_TO_BE_PHYSICAL.add(fluidName);
    }

    public static void setFluidToUseVanillaBucketMode(String fluidName,boolean vanilla){
        if(vanilla) FLUIDS_BUCKET_TO_BE_VANILLA.add(fluidName);
        else FLUIDS_BUCKET_TO_BE_VANILLA.remove(fluidName);
    }

    /**
     * 指定流体是否需要物理化
     * @since 0.1
     * @param fluid 流体
     * @return 若需要,则返回true
     */
    public static boolean isFluidToBePhysical(Fluid fluid){
        if(fluid == null) return false;
        return !FLUIDS_NOT_TO_BE_PHYSICAL.contains(fluid.getName());
    }

    /**
     * @see #isFluidToBePhysical(Fluid)
     * @since 0.1
     */
    public static boolean isFluidToBePhysical(BlockLiquid fluid){
        return isFluidToBePhysical(FluidUtil.getFluid(fluid));
    }

    /**
     * 指定流体是否需要使用原版的桶行为
     * @since 0.1
     * @param fluid 流体
     * @return 若需要，则返回true
     */
    public static boolean isFluidToUseVanillaBucketMode(Fluid fluid){
        if(fluid == null) return true;
        return FLUIDS_BUCKET_TO_BE_VANILLA.contains(fluid.getName());
    }

    /**
     * 设置指定维度的重力大小
     * @since 0.2.0-beta.4
     * @param dimensionId 维度 ID
     * @param gravity 重力相对于主世界的倍数。
     * @throws IllegalArgumentException 当重力为负值时
     */
    public static void setGravity(final int dimensionId,final double gravity){
        if(gravity < 0.002d) GRAVITIES.put(dimensionId,0d);
        if(gravity >0){
            GRAVITIES.put(dimensionId,1d/gravity);
        }else throw new IllegalArgumentException("Gravity "+gravity+" can't be negative!");
    }

    /**
     * 指定世界是否有重力
     * @since 0.2.0-beta.4
     * @param world 世界
     * @return 若有重力则返回 true
     */
    public static boolean hasGravity(final @Nonnull World world){
        return getGravity(world.provider.getDimension()) > 0d;
    }

    /**
     * 获取指定世界的重力相对于主世界的大小的倒数。
     * @see #getGravity(int)
     * @param world 世界
     * @since 0.2.0-beta.4
     * @return 一个双精度浮点数，表示重力大小的倒数。当值为 0 时，表示无重力。
     */
    public static double getGravity(final @Nonnull World world){
        return getGravity(world.provider.getDimension());
    }

    /**
     * 获取指定维度的重力相对于主世界的大小的倒数。
     * 例如，返回 100 表示该维度的重力是主世界的 100 分之一，返回 0.1 表示该维度的重力是主世界的 10 倍。
     * 特别的，返回 0 表示无重力。
     * 值的大小（目前）不应该是负的。
     * @param dimensionId 主世界
     * @since 0.2.0-beta.4
     * @return 一个双精度浮点数，表示重力大小的倒数。当值为 0 时，表示无重力。
     */
    public static double getGravity(final int dimensionId){
        return GRAVITIES.containsKey(dimensionId)?GRAVITIES.get(dimensionId):1d;
    }
}
