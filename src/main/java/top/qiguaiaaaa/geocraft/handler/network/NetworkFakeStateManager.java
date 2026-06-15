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

package top.qiguaiaaaa.geocraft.handler.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import top.qiguaiaaaa.geocraft.configs.SoilConfig;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.HUMIDITY;
import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.MIXTURE;

public final class NetworkFakeStateManager {
    public static final Function<IBlockState,IBlockState> DEFAULT_OVERWRITE_RULE = state -> state;
    public static final IntBinaryOperator REMOVE_META_RULE = (id,meta)->id<<4;

    private static final Map<Block, Function<IBlockState,IBlockState>> STATE_OVERWRITE_MAP = new HashMap<>();
    private static final Int2ObjectOpenHashMap<IntBinaryOperator> STATE_OVERWRITE_MAP_2 = new Int2ObjectOpenHashMap<>();

    public static void registerRule(@Nonnull Block block, @Nonnull Function<IBlockState,IBlockState> rule, @Nonnull IntBinaryOperator rule2){
        STATE_OVERWRITE_MAP.put(block,rule);
        STATE_OVERWRITE_MAP_2.put(Block.REGISTRY.getIDForObject(block),rule2);
    }

    /**
     * @since 0.2.0
     * @param block 移除规则对应的方块
     * @return 是否有被移除
     */
    public static boolean clearRule(@Nonnull Block block){
        boolean res = STATE_OVERWRITE_MAP.remove(block) != null;
        res |= STATE_OVERWRITE_MAP_2.remove(Block.REGISTRY.getIDForObject(block)) != null;
        return res;
    }

    public static IBlockState overwriteState(@Nonnull IBlockState state){
        return STATE_OVERWRITE_MAP.getOrDefault(state.getBlock(),DEFAULT_OVERWRITE_RULE).apply(state);
    }

    public static int overwriteState(int state){
        int id = state>>4;
        int meta = state &0b1111;
        final IntBinaryOperator operator = STATE_OVERWRITE_MAP_2.get(id);
        return operator == null?state:operator.applyAsInt(id,meta);
    }

    public static void registerDefaultConfig(){
        registerRule(Blocks.SNOW_LAYER,state -> state.withProperty(MIXTURE,false),(id,meta)->(id<<4)|(meta&7));

        if(!SoilConfig.ENABLE_SOIL_SYSTEM.getValue()) return;

        final Function<IBlockState,IBlockState> removeHumidity = state -> state.withProperty(HUMIDITY,0);
        registerRule(Blocks.DIRT,removeHumidity,(id, meta)->(id<<4)|(meta%3));
        registerRule(Blocks.SAND,removeHumidity,(id,meta)->(id<<4)|(meta&1));
        if(SoilConfig.ALLOW_CLIENT_TO_READ_HUMIDITY_DATA.getValue()) return;
        registerRule(Blocks.GRASS,removeHumidity,REMOVE_META_RULE);
        registerRule(Blocks.GRASS_PATH,removeHumidity,REMOVE_META_RULE);
        registerRule(Blocks.GRAVEL,removeHumidity,REMOVE_META_RULE);
        registerRule(Blocks.CLAY,removeHumidity,REMOVE_META_RULE);
    }
}
