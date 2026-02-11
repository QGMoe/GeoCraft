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

package top.qiguaiaaaa.geocraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.IWorldGenerator;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.setting.GeoSoilSetting;
import top.qiguaiaaaa.geocraft.api.util.BlockFlagsModifier;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.block.IBlockSoil;
import top.qiguaiaaaa.geocraft.api.util.math.vec.BlockPosI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.configs.SoilConfig.*;

public class GeoCraftPostPopulatingGenerator implements IWorldGenerator {

    private boolean enableProtection,shouldContinue;

    private boolean needProtection = false;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        populateHumidity(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        if(world instanceof WorldServer){
            EventFactory.preAtmosphereGenerate((WorldServer) world,world.getChunk(chunkX,chunkZ));
        }
    }

    protected void populateHumidity(Random random,int chunkX,int chunkZ, World world,IChunkGenerator chunkGenerator,IChunkProvider chunkProvider){
        if(!ENABLE_GENERATION.getValue()) return;
        if(GENERATION_DIMENSION_BLACK_LIST.contains(world.provider.getDimension())){
            return;
        }
        enableProtection = ENABLE_PRE_PROTECTION_OF_WATER_FALLING.getValue();
        BlockPosI.Mutable pos = new BlockPosI.Mutable();
        int beginX = chunkX<<4;
        int beginZ = chunkZ<<4;
        for(int x=0;x<16;x++){
            for(int z=0;z<16;z++){
                Biome biome = world.getBiome(pos.setPos(beginX+x,1,beginZ+z));
                if(GeoSoilSetting.isBiomeInGenerationBlacklist(biome)) continue;
                boolean waterFlag = false;
                for (int y=world.getHeight(beginX+x,beginZ+z);y>=0;y--){
                    pos.setPos(beginX+x,y,beginZ+z);
                    IBlockState state = world.getBlockState(pos);
                    Fluid fluid = FluidUtil.getFluid(state);
                    if(fluid == FluidRegistry.WATER){
                        waterFlag = true;
                        continue;
                    }else if(fluid != null){
                        waterFlag = false;
                        continue;
                    }
                    state = addHumidity(world,biome,pos,state,waterFlag);
                    if(needProtection){
                        world.setBlockState(pos,GeoSoilSetting.getBiomeWaterProtectionBlock(biome),Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.NO_OBSERVERS);
                        waterFlag = false;
                        needProtection = false;
                        continue;
                    }
                    if(shouldContinue){
                        shouldContinue = false;
                        continue;
                    }
                    if(state == null){ //不透水
                        waterFlag = false;
                        continue;
                    }
                    world.setBlockState(pos,state,Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.NO_OBSERVERS );
                }
            }
        }
    }

    @Nullable
    protected IBlockState addHumidity(final @Nonnull World world,
                                      final @Nonnull Biome biome,
                                      final @Nonnull BlockPos pos,
                                      final @Nonnull IBlockState state,
                                      final boolean waterFlag){
        if(waterFlag && state.getMaterial() == Material.AIR){
            if(enableProtection){
                needProtection = true;
                return null;
            }
            return Blocks.WATER.getDefaultState();
        }
        final @Nonnull Block block = state.getBlock();
        if(block instanceof IBlockSoil){
            final @Nonnull IBlockSoil soil = (IBlockSoil) block;
            if(waterFlag) return soil.getLayerState(state,null,FluidRegistry.WATER,4);
            if(!biome.canRain()) return null;
            return soil.getLayerState(state,null,FluidRegistry.WATER,
                    (int)MathHelper.clamp(4*biome.getRainfall()+1,0,soil.getMaxStableHumidity(state)));
        }
        if(block instanceof ILayeredFluidHost){ //之前已经处理过本身为流体的可能性，这里一定不会是流体
            final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost) block;
            final int maxQuanta = host.getMaxLayers(world,pos,state,null,FluidRegistry.WATER,false);
            if(waterFlag){
                host.addLayer(world,pos,state,null,FluidRegistry.WATER,maxQuanta, FluidHostOperation.DO_WITH_CERTAINTY,null,null,
                        BlockFlagsModifier.build(Constants.BlockFlags.NO_OBSERVERS | Constants.BlockFlags.NO_RERENDER,Constants.BlockFlags.DEFAULT_AND_RERENDER));
                shouldContinue = true;
                return null;
            }
            return null;
        }
        return null;
    }
}
