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

package top.qiguaiaaaa.geocraft.block;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Random;

import static net.minecraft.block.BlockSnow.LAYERS;
import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.MIXTURE;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.FLUID_PHYSICS_INFO;
import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPhysicsInfo.CREATE_INFO_FUNC;

/**
 * Mixin Snow 的通用逻辑
 * @since 0.2.0
 * @author QiguaiAAAA
 */
@Deprecated
public interface IBlockSnow {

    /**
     * 尝试融化雪
     * @param world 所在世界
     * @param pos 所在位置
     * @param state 一个雪的方块状态
     * @param random 随机数发生器
     * @return 是否变成了非雪方块
     */
    default boolean trySmelt(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random){
        final int layer = state.getValue(BlockSnow.LAYERS);
        final boolean isMixture = state.getValue(MIXTURE);
        try(@Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)){
            if (world.getLightFor(EnumSkyBlock.BLOCK, pos) > 11) {
                this.turnIntoWater(world,pos, accessor,8-layer); //用的是发光Block产生的热量,所以不扣地表温度
                return true;
            }

            if(accessor == null) return false;

            if(accessor.getSkyLight() == 0 && FLUID_PHYSICS_INFO.computeIfAbsent(world.provider.getDimension(), CREATE_INFO_FUNC).getSkyLight().checkWhenSnowLayerSmelting)
                return false;

            double temp = accessor.getTemperature();
            if(temp > TemperatureProperty.ICE_POINT){
                if(isMixture){
                    this.turnIntoWater(world,pos,accessor,8-layer);
                }else{
                    this.turnIntoMixture(world,pos,layer);
                }
                accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*layer/2.0);
                return isMixture;
            }else{
                if(isMixture){
                    if(layer == 8){
                        world.setBlockState(pos, Blocks.ICE.getDefaultState());
                    }else{
                        world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(LAYERS,layer).withProperty(MIXTURE,false));
                    }
                    accessor.putHeatToUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*layer/2.0);
                    return layer==8;
                }
                return false;
            }
        }
    }

    /**
     * 将指定位置的雪方块转换为混合物
     * @param worldIn 世界
     * @param pos 位置
     * @param layer 层数，应介于 1 到8
     * @throws IllegalArgumentException 如果指定的层数不在 1 到 8 之间
     */
    default void turnIntoMixture(@Nonnull World worldIn,@Nonnull BlockPos pos, int layer) {
        worldIn.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(LAYERS,layer).withProperty(MIXTURE,true));
    }

    /**
     * 在指定位置融化指定层数的雪方块
     * @param worldIn 所在世界
     * @param pos 位置
     * @param accessor 大气访问器，可以为空
     * @param level 融化的层数
     * @throws NullPointerException 若世界或位置参数为 null
     */
    default void turnIntoWater(@Nonnull World worldIn,@Nonnull BlockPos pos, @Nullable IAtmosphereAccessor accessor, int level) {
        if (worldIn.provider.doesWaterVaporize()) {
            if(accessor != null && accessor.canAccessAtmosphere()){
                accessor.fillFluidToAtmosphere(FluidRegistry.WATER,(8-level)* FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true);
            }
            worldIn.setBlockToAir(pos);
        } else {
            if(FluidPhysicsMode.getCurrentMode() != FluidPhysicsMode.MORE_REALITY){
                worldIn.setBlockToAir(pos);
                return;
            }
            worldIn.setBlockState(pos, Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,level));
            worldIn.neighborChanged(pos, Blocks.WATER, pos);
        }
    }
}
