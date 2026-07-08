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

package top.qiguaiaaaa.geocraft.util;

import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.math.vec.MBlockPos;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ChunkUtil {
    public static final List<EnumFacing> HORIZONTALS = Collections.unmodifiableList(Arrays.asList(
            EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH));

    public static int getSameLiquidDepth(final @Nonnull Chunk chunk,final int x,int y,final int z,final @Nonnull Fluid fluid,final int maxDepth){
        int ans = 0;
        while (FluidUtil.getFluid(chunk.getBlockState(x,y,z))== fluid){
            ans++;
            if(ans >maxDepth) break;
            if(y>0) y--;
        }
        return ans;
    }

    public static int getNeighborsLightFor(final @Nonnull World world,final @Nonnull EnumSkyBlock type,final @Nonnull BlockPos pos) {
        if (!world.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
            return 0;
        }
        final int x = pos.getX();
        final int y = Math.max(pos.getY(), 0);
        final int z = pos.getZ();
        final MBlockPos mutable = new MBlockPos(x,y,z);

        if (!world.isValid(mutable)) {
            return type.defaultLightValue;
        } else if (!world.isBlockLoaded(mutable)) {
            return type.defaultLightValue;
        }
        int light = 0;
        for(final @Nonnull EnumFacing facing:EnumFacing.VALUES){
            light = Math.max(light,world.getLightFor(type,mutable.setPos(x,y,z).offsetM(facing)));
        }
        return light;
    }

    @Nonnull
    public static Biome getMainBiome(final @Nonnull Chunk chunk){
        final byte[] biomes = chunk.getBiomeArray();
        final short[] frequency = new short[biomes.length+256];
        for (int biome : biomes) {
            if(biome<0) biome = biome+256;
            frequency[biome+256]++;
        }
        int maxPosition = 0,maxFrequency = -1;
        for(int i=0;i< frequency.length;i++){
            if(frequency[i]>maxFrequency){
                maxFrequency = frequency[i];
                maxPosition = i;
            }
        }
        return Biome.getBiome(maxPosition-256, Biomes.PLAINS);
    }
}
