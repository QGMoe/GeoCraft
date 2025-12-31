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

package top.qiguaiaaaa.geocraft.mixin.reality.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnowBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;

import javax.annotation.Nullable;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.FLUID_PHYSICS_INFO;
import static top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPhysicsInfo.CREATE_INFO_FUNC;

@Mixin(value = BlockSnowBlock.class)
public class BlockSnowBlockMixin extends Block {
    public BlockSnowBlockMixin(Material materialIn) {
        super(materialIn);
    }
    @Inject(method = "updateTick",at =@At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        ci.cancel();
        try(@Nullable IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(worldIn,pos,true)){
            if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11) {
                this.天圆地方$turnIntoWater(worldIn,pos, accessor); //用的是发光Block产生的热量,所以不扣地表温度
                return;
            }

            if(accessor == null) return;
            int light = ChunkUtil.getNeighborsLightFor(worldIn,EnumSkyBlock.SKY,pos);
            if(light == 0 && FLUID_PHYSICS_INFO.computeIfAbsent(worldIn.provider.getDimension(), CREATE_INFO_FUNC).getSkyLight().checkWhenSnowSmelting)
                return;
            accessor.setSkyLight(light);
            double temp = accessor.getTemperature();
            if(temp > TemperatureProperty.ICE_POINT){
                this.天圆地方$turnIntoWater(worldIn,pos,accessor);
                accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA*8);
            }
        }

    }

    @Unique
    protected void 天圆地方$turnIntoWater(World worldIn, BlockPos pos, @Nullable IAtmosphereAccessor accessor) {
        if (worldIn.provider.doesWaterVaporize()) {
            if(accessor != null && accessor.canAccessAtmosphere()){
                accessor.fillFluidToAtmosphere(FluidRegistry.WATER,Fluid.BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true);
            }
            worldIn.setBlockToAir(pos);
        } else {
            worldIn.setBlockState(pos, Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,0));
            worldIn.neighborChanged(pos, Blocks.WATER, pos);
        }
    }
}
