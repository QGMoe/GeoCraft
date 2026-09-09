/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.geography.snow;

import moe.qingu.orbtellus.api.OTCFluids;
import moe.qingu.orbtellus.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.orbtellus.api.block.BlockProperties;
import moe.qingu.orbtellus.api.util.AtmosphereUtil;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import moe.qingu.orbtellus.geography.fluidphysics.vanilla.VanillaFlowingVanilla;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @author QiguaiAAAA
 */
public final class SnowFlowing {

    public static final Fluid[] FILL_ORDER = {FluidRegistry.WATER, OTCFluids.SNOW};
    private static final IBlockState[][] WATER_SNOW_MIX_TABLE_DYNAMIC = new IBlockState[9][9];
    private static final IBlockState[][] WATER_SNOW_MIX_TABLE_STATIC = new IBlockState[9][9];

    private static final double[][] WATER_SNOW_MIX_DELTA_HEAT = new double[9][9];

    static {
        for(int water=0;water<=8;water++){
            for (int snow=0;snow<=8;snow++){
                if(water+snow>8){
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=null;
                    WATER_SNOW_MIX_TABLE_STATIC[water][snow]=null;
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow] = 0d;
                    continue;
                }
                if(water == snow && snow ==0){
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=WATER_SNOW_MIX_TABLE_STATIC[water][snow]=Blocks.AIR.getDefaultState();
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow] = 0d;
                    continue;
                }
                final int sum = water+snow;
                final int costWater = snow * 2; //雪全化成水的代价
                final int costSnow = water * 2; //水全冻成雪的代价
                final int costMixture = Math.abs(water-snow); //变成混雪的代价
                if(costMixture < costWater && costMixture < costSnow){
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=WATER_SNOW_MIX_TABLE_STATIC[water][snow]=
                            Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockProperties.MIXTURE,true)
                                    .withProperty(BlockSnow.LAYERS,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]= (snow-water)/2d * AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;
                }else if(water>=snow){ //旧规则，可以发现这里直接包含了 costWater 最低或与 costMixture 相等的情况，另外这里肯定取不到等号
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=Blocks.FLOWING_WATER.getDefaultState().withProperty(LEVEL,sum);
                    WATER_SNOW_MIX_TABLE_STATIC[water][snow]=Blocks.WATER.getDefaultState().withProperty(LEVEL,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]=snow*AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;
                }else { //同理，包含 costSnow 最低或与 costMixture 相等的情况
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=WATER_SNOW_MIX_TABLE_STATIC[water][snow]=
                            Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]=-water*AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;
                }
            }
        }
    }

    public static boolean isBlocked(final @Nonnull IBlockState downState){
        if(VanillaFlowingVanilla.isBlocked(downState)) return true;
        if(downState.getBlock() == Blocks.SNOW_LAYER){
            return downState.getValue(BlockSnow.LAYERS) >= 8;
        }else return false;
    }

    /* -----------------------------
                 雪 水 混 合
       ----------------------------- */

    /**
     * 在指定地点存在指定 quanta 的水和雪，计算两者混合后的状态并在有大气交互能力的情况下发生能量变化
     * @param world 世界
     * @param pos 位置
     * @param accessor 大气访问器
     * @param quantaWater 该位置的水量，单位 {@link moe.qingu.orbtellus.api.fluid.unit.FluidUnit#QUANTA}
     * @param quantaSnow 该位置的雪量，单位 {@link moe.qingu.orbtellus.api.fluid.unit.FluidUnit#QUANTA}
     * @param flags 放置方块的 flags
     * @return 混合状态
     */
    @Nonnull
    public static IBlockState mixSnowWithWater(@Nonnull final World world,
                                           @Nonnull final BlockPos pos,
                                           @Nullable final IAtmosphereAccessor accessor,
                                           final int quantaWater,
                                           final int quantaSnow,
                                           final int flags){
        final @Nonnull IBlockState mixState = getSnowWaterMixStateDynamic(quantaSnow,quantaWater);
        world.setBlockState(pos,mixState,flags);
        if(accessor != null){
            final double heatChange = WATER_SNOW_MIX_DELTA_HEAT[quantaWater][quantaSnow];
            if(heatChange>0) accessor.drainHeatFromUnderlying(heatChange);
            else if(heatChange<0) accessor.putHeatToUnderlying(-heatChange);
        }
        return mixState;
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 雪量，单位为层 Quanta
     * @param water 水量，单位为层 Quanta
     * @return 混合后的方块状态。水为动态
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.3.0-alpha.2
     */
    @Nonnull
    public static IBlockState getSnowWaterMixStateDynamic(final int snow,final int water){
        Validate.inclusiveBetween(0L,8L,snow + water);
        return WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow];
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 雪量，单位为层 Quanta
     * @param water 水量，单位为层 Quanta
     * @return 混合后的方块状态。水为静态。
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.3.0-alpha.2
     */
    @Nonnull
    public static IBlockState getSnowWaterMixStateStatic(final int snow,final int water){
        Validate.inclusiveBetween(0L,8L,snow + water);
        return WATER_SNOW_MIX_TABLE_STATIC[water][snow];
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 雪量，单位为层 Quanta
     * @param water 水量，单位为层 Quanta
     * @param requireStatic 返回水时,是否需要静态水
     * @return 混合后的方块状态。
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.3.0-alpha.2
     */
    @Nonnull
    public static IBlockState getSnowWaterMixState(final int snow,final int water,final boolean requireStatic){
        if(requireStatic) return getSnowWaterMixStateStatic(snow,water);
        return getSnowWaterMixStateDynamic(snow,water);
    }
}
