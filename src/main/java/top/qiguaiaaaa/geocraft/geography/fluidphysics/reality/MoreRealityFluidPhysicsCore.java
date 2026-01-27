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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.reality;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.BlockProperties;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.WaterUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class MoreRealityFluidPhysicsCore {

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
                if(water>snow){
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=Blocks.FLOWING_WATER.getDefaultState().withProperty(LEVEL,sum);
                    WATER_SNOW_MIX_TABLE_STATIC[water][snow]=Blocks.WATER.getDefaultState().withProperty(LEVEL,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]=snow*AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;
                }else if(water == snow){
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=WATER_SNOW_MIX_TABLE_STATIC[water][snow]=
                            Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockProperties.MIXTURE,true)
                                    .withProperty(BlockSnow.LAYERS,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]=0d;
                }else {
                    WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow]=WATER_SNOW_MIX_TABLE_STATIC[water][snow]=
                            Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS,sum);
                    WATER_SNOW_MIX_DELTA_HEAT[water][snow]=-water*AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;
                }
            }
        }
    }

    private MoreRealityFluidPhysicsCore(){}

    @Nullable
    public static IBlockState evaporateWater(@Nonnull final World world,
                                             @Nonnull final BlockPos pos,
                                             @Nonnull final IBlockState state,
                                             @Nonnull final Random rand,
                                             @Nonnull final IAtmosphereAccessor accessor){
        if(!world.isAirBlock(pos.up())) return state;

        final int meta = state.getValue(LEVEL);
        if(meta >=8) return null;
        final double possibility = getWaterEvaporatePossibility(world,pos,state,accessor);

        if(!BaseUtil.getRandomResult(rand,possibility)){
            return state;
        }
        if(accessor.fillFluidToAtmosphere(FluidRegistry.WATER,FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true) <= 0)
            return state;
        accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_EVAPORATE_LATENT_HEAT_PER_QUANTA);
        if(meta == 7) return null;
        return state.withProperty(LEVEL,meta+1);
    }
    public static IBlockState freezeWater(@Nonnull final World world,
                                          @Nonnull final BlockPos pos,
                                          @Nonnull final IBlockState state,
                                          @Nonnull final Random rand,
                                          @Nonnull final IAtmosphereAccessor accessor){
        final int meta = state.getValue(LEVEL);
        if(meta >=8) return state;
        if(!accessor.getSystem().getAtmosphereInfo().canWaterFreeze()) return state;

        double possibility  = WaterUtil.getFreezePossibility(accessor);
        if(possibility <= 0) return state;
        if(meta == 7) possibility = Math.min(possibility*8,1);
        else if(meta == 6) possibility = Math.min(possibility*4,1);
        else if(meta == 5) possibility = Math.min(possibility*2,1);
        if(!BaseUtil.getRandomResult(rand,possibility*0.85+0.15)){
            return state;
        }
        if(meta == 0){
            if(!accessor.getSystem().getAtmosphereInfo().canWaterFreeze(pos,true)) return state;
            return Blocks.ICE.getDefaultState();
        }
        if(!WaterUtil.canPlaceSnow(world,pos)) return state;
        final int quanta = 8-meta;
        accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*quanta);
        return Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS,quanta);
    }

    public static double getWaterEvaporatePossibility(@Nonnull final World world,
                                                      @Nonnull final BlockPos pos,
                                                      @Nonnull final IBlockState state,
                                                      @Nonnull final IAtmosphereAccessor accessor){
        final double possibility = WaterUtil.getWaterEvaporatePossibility(accessor);
        if(possibility >= 0.9999d) return 1;
        if(!world.isAreaLoaded(pos,1)) return possibility;

        final int meta = state.getValue(LEVEL);
        if(meta <5) return possibility;

        byte neighborsAir = 0;
        for(final @Nonnull EnumFacing facing:EnumFacing.HORIZONTALS){
            BlockPos facingPos = pos.offset(facing);
            if(world.isAirBlock(facingPos)) neighborsAir++;
        }
        if(neighborsAir <= 1) return possibility;

        if(pos.getY() <= 0) return possibility;
        final @Nonnull IBlockState downState= world.getBlockState(pos.down());
        if(FluidUtil.getFluid(downState) == FluidRegistry.WATER) return possibility;

        return Math.min(possibility*(1<<(neighborsAir-1)),1);
    }

    /**
     * 是否能够在指定位置降水
     * @param world 世界
     * @param pos 位置
     * @return 如果能，则返回true
     */
    public static boolean canRainAt(@Nonnull final World world,@Nonnull final BlockPos pos){
        @Nullable final Atmosphere atmosphere = AtmosphereSystemManager.getAtmosphere(world, pos);
        if(atmosphere == null) return false;
        if(atmosphere.drainWater(FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,pos,false)<FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME) return false;
        final float temp = atmosphere.getAtmosphereTemperature(pos);
        if(temp <= TemperatureProperty.UNAVAILABLE) return false;
        if (!(temp < TemperatureProperty.ICE_POINT) && !(temp > TemperatureProperty.BOILED_POINT)) {
            if (pos.getY() >= 0 && pos.getY() < 256) {
                final @Nonnull IBlockState state = world.getBlockState(pos);

                return state.getBlock().isAir(state, world, pos) && Blocks.FLOWING_WATER.canPlaceBlockAt(world, pos);
            }

        }
        return false;
    }

    public static boolean mixSnowWithWater(@Nonnull final World world,
                                           @Nonnull final BlockPos pos,
                                           @Nullable final IAtmosphereAccessor accessor,
                                           final int quantaWater,
                                           final int quantaSnow,
                                           final int flags){
        if(quantaWater+quantaSnow == 0) return false;
        final @Nonnull IBlockState mixState = getSnowWaterMixStateDynamic(quantaSnow,quantaWater);
        if(!world.setBlockState(pos,mixState,flags)) return false;
        if(accessor != null){
            final double heatChange = WATER_SNOW_MIX_DELTA_HEAT[quantaWater][quantaSnow];
            if(heatChange>0) accessor.drainHeatFromUnderlying(heatChange);
            else if(heatChange<0) accessor.putHeatToUnderlying(-heatChange);
        }
        return true;
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 水量，单位为层，一层 125 mB
     * @param water 雪量，单位为层，一层 125mB
     * @return 混合后的方块状态。若雪量大于水量，则返回纯雪；若雪量=水量，则返回混合雪；若雪量<水量，则返回流动水
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.2.0-alpha.3
     */
    @Nonnull
    public static IBlockState getSnowWaterMixStateDynamic(final int snow,final int water){
        BaseUtil.checkAndReturn(snow+water,0,8);
        return WATER_SNOW_MIX_TABLE_DYNAMIC[water][snow];
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 水量，单位为层，一层 125 mB
     * @param water 雪量，单位为层，一层 125mB
     * @return 混合后的方块状态。若雪量大于水量，则返回纯雪；若雪量=水量，则返回混合雪；若雪量<水量，则返回静态水
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.2.0-alpha.3
     */
    @Nonnull
    public static IBlockState getSnowWaterMixStateStatic(final int snow,final int water){
        BaseUtil.checkAndReturn(snow+water,0,8);
        return WATER_SNOW_MIX_TABLE_STATIC[water][snow];
    }

    /**
     * 获取指定层的水和雪混合后的方块状态
     * @param snow 水量，单位为层，一层 125 mB
     * @param water 雪量，单位为层，一层 125mB
     * @param requireStatic 返回水时,是否需要静态水
     * @return 混合后的方块状态。若雪量大于水量，则返回纯雪；若雪量=水量，则返回混合雪；若雪量<水量，则返回水
     * @throws IllegalArgumentException 当 snow + water >8 时
     * @since 0.2.0-alpha.3
     */
    @Nonnull
    public static IBlockState getSnowWaterMixState(final int snow,final int water,final boolean requireStatic){
        if(requireStatic) return getSnowWaterMixStateStatic(snow,water);
        return getSnowWaterMixStateDynamic(snow,water);
    }
}
