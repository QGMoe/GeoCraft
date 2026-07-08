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

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author QiguaiAAAA
 */
public final class GeoSoilSetting {
    private static final HashSet<Biome> BIOMES_IN_GENERATION_BLACKLIST = new HashSet<>();
    private static final HashMap<Biome, IBlockState> BIOMES_TO_WATER_PROTECTION = new HashMap<>();

    public static void addBiomeToGenerationBlacklist(@Nonnull Biome biome){
        BIOMES_IN_GENERATION_BLACKLIST.add(biome);
    }

    public static void setBiomeWaterProtectionBlock(@Nonnull Biome biome,@Nonnull IBlockState state){
        BIOMES_TO_WATER_PROTECTION.put(biome,state);
    }

    public static boolean removeBiomeFromGenerationBlacklist(@Nonnull Biome biome){
        return BIOMES_IN_GENERATION_BLACKLIST.remove(biome);
    }

    public static boolean isBiomeInGenerationBlacklist(@Nonnull Biome biome){
        return BIOMES_IN_GENERATION_BLACKLIST.contains(biome);
    }

    public static IBlockState getBiomeWaterProtectionBlock(@Nonnull Biome biome) {
        IBlockState res = BIOMES_TO_WATER_PROTECTION.get(biome);
        return res == null? Blocks.STONE.getDefaultState():res;
    }
}
