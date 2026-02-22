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

package top.qiguaiaaaa.geocraft.block.finite;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidFrom;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.BlockFlagsModifier;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.FluidPhysicsCoreFinite;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.IPostEventInitFinite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.block.BlockLiquid.LEVEL;

public interface IBlockLiquidFinite extends IPostEventInitFinite, IBlockStateLayeredFluidHost {
    int HEIGHT_PER_QUANTA = LayeredFluidHostUtil.EIGHTH_HEIGHT;

    @Nonnull
    FiniteFlowingVanilla 天圆地方$FINITE$getFlowingHandler();

    @Nullable
    @Override
    default EnumFacing getDefaultSide(@Nonnull final IBlockState state){
        return null;
    }

    @Override
    default boolean isAcceptedFluid(@Nonnull final IBlockState state,
                                    @Nullable final EnumFacing side,
                                    @Nonnull final Fluid fluid){
        return fluid == 天圆地方$FINITE$getFlowingHandler().fluid || fluid == GeoFluids.SNOW;
    }

    @Override
    default int getLayers(@Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid){
        if(fluid == 天圆地方$FINITE$getFlowingHandler().fluid) return Math.max(8-state.getValue(LEVEL),1);
        return 0;
    }

    @Override
    default int getMaxLayers(@Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        final @Nonnull FiniteFlowingVanilla flowing = 天圆地方$FINITE$getFlowingHandler();
        if(fluid == GeoFluids.SNOW){
            return 8- getLayers(state,null,flowing.fluid);
        }else if(fluid == flowing.fluid) return 8;
        return 0;
    }

    @Override
    default int getEmptyHeight(@Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid){
        if(fluid == GeoFluids.SNOW){
            return getHeight(state,null,天圆地方$FINITE$getFlowingHandler().fluid);
        }
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default int getMaxHeight(@Nonnull final IBlockState state, @Nullable final EnumFacing side, @Nonnull final Fluid fluid){
        return getMaxLayers(state,side,fluid)*LayeredFluidHostUtil.EIGHTH_HEIGHT;
    }

    @Override
    default int getHeightPerLayer(@Nonnull final IBlockState state,@Nullable final EnumFacing side){
        return LayeredFluidHostUtil.EIGHTH_HEIGHT;
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid){
        return QBUtil.QUANTA_VOLUME;
    }

    // TODO: Fix Add layer With Water
    @Override
    default int addLayer(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state,
                         @Nullable final EnumFacing side,
                         @Nonnull final Fluid fluid,
                         final int layer,
                         @Nonnull final FluidHostOperation operation,
                         @Nullable final NBTTagCompound nbt,
                         @Nullable final IFluidFrom from,
                         final long blockFlagsModifier){
        if(!isAcceptedFluid(world,pos,state,side,fluid)) return 0;
        final @Nonnull FiniteFlowingVanilla handler = 天圆地方$FINITE$getFlowingHandler();
        final int quanta = getLayers(state,null,handler.fluid);
        final int layerInFact = Math.max(layer,8 - quanta);
        if(layerInFact <= 0) return 0;
        if(fluid == GeoFluids.SNOW){
            if(handler.fluid == FluidRegistry.WATER) return handleAddSnowToWater(world,pos,quanta,layerInFact,operation,blockFlagsModifier);
            else return handleAddSnowToLava(world,pos,side,layerInFact,operation,blockFlagsModifier);
        } else {
            final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.SEND_TO_CLIENTS,blockFlagsModifier);
            if(operation.doOperate){
                world.setBlockState(pos,state.withProperty(LEVEL,8-(quanta+layerInFact)),flags);
            }
            return layerInFact;
        }
    }

    static int handleAddSnowToWater(@Nonnull final World world,
                                    @Nonnull final BlockPos pos,
                                    final int waterQuanta,
                                    final int snowQuanta,
                                    @Nonnull final FluidHostOperation operation,
                                    final long blockFlagsModifier){
        if(operation.doOperate){
            try(@Nullable final IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true)) {
                final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
                FluidPhysicsCoreFinite.mixSnowWithWater(world,pos,accessor,waterQuanta,snowQuanta,flags);
            }
        }
        return snowQuanta;
    }

    static int handleAddSnowToLava(@Nonnull final World world,
                                   @Nonnull final BlockPos pos,
                                   @Nullable final EnumFacing side,
                                   final int snowQuanta,
                                   @Nonnull final FluidHostOperation operation,
                                   final long blockFlagsModifier){
        if(operation.doOperate){
            final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
            final @Nonnull EnumFacing actualSide = side == null?EnumFacing.UP:side;
            switch (actualSide){
                case EAST:
                case WEST:
                case NORTH:
                case SOUTH:
                    world.setBlockState(pos,Blocks.COBBLESTONE.getDefaultState(),flags);
                    break;
                case UP:
                case DOWN:
                default:
                    world.setBlockState(pos,Blocks.STONE.getDefaultState(),flags);
            }
        }
        return snowQuanta;
    }

    @Nonnull
    static IBlockState getSnowLavaMixtureState(@Nullable final EnumFacing side){
        final @Nonnull EnumFacing actualSide = side == null?EnumFacing.UP:side;
        switch (actualSide){
            case EAST:
            case WEST:
            case NORTH:
            case SOUTH:
                return Blocks.COBBLESTONE.getDefaultState();
            case UP:
            case DOWN:
            default:
                return Blocks.STONE.getDefaultState();
        }
    }

    @Override
    default boolean setLayer(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid ,
                             int newLayer,
                             @Nullable final NBTTagCompound nbt,
                             final long blockFlagsModifier){
        final @Nonnull FiniteFlowingVanilla handler = 天圆地方$FINITE$getFlowingHandler();
        if(fluid == GeoFluids.SNOW){
            final int quanta = Math.max(8-state.getValue(LEVEL),1);
            if(newLayer<0 || newLayer > 8-quanta) return false;
            if(handler.fluid == FluidRegistry.WATER) return FluidPhysicsCoreFinite.mixSnowWithWater(world,pos,null,quanta,newLayer,BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier));
            else {
                handleAddSnowToLava(world,pos,side,newLayer,FluidHostOperation.DO_WITH_ASSUMPTION,blockFlagsModifier);
                return true;
            }
        }else if(fluid == handler.fluid){
            if(newLayer<0 || newLayer > 8) return false;
            final int flags = BlockFlagsModifier.modify(Constants.BlockFlags.DEFAULT,blockFlagsModifier);
            if(newLayer == 0) {
                return world.setBlockState(pos, Blocks.AIR.getDefaultState(),flags);
            }else return world.setBlockState(pos,state.withProperty(LEVEL,8 - newLayer), flags);
        }else return false;
    }

    @Nullable
    @Override
    default IBlockState getLayerState(@Nonnull final IBlockState state,
                                      @Nullable final EnumFacing side,
                                      @Nonnull final Fluid fluid,
                                      final int layer){
        final @Nonnull FiniteFlowingVanilla handler = 天圆地方$FINITE$getFlowingHandler();
        if(fluid == GeoFluids.SNOW){
            final int quanta = Math.max(8-state.getValue(LEVEL),1);
            if(layer<0 || layer > 8-quanta) return null;
            if(handler.fluid == FluidRegistry.WATER) return FluidPhysicsCoreFinite.getSnowWaterMixStateDynamic(layer,quanta);
            else return getSnowLavaMixtureState(side);

        }else if(fluid == handler.fluid){
            if(layer<0 || layer > 8) return null;
            if(layer == 0) {
                return Blocks.AIR.getDefaultState();
            }else return state.withProperty(LEVEL,8 - layer);
        }else return null;
    }

    @Override
    default boolean isFull(@Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid) {
        if(fluid == GeoFluids.SNOW) return state.getValue(LEVEL) == 0;
        if(fluid != 天圆地方$FINITE$getFlowingHandler().fluid) return true;
        return state.getValue(LEVEL) == 0;
    }
}
