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

package top.qiguaiaaaa.geocraft.api.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidFrom;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public final class LayeredFluidHostUtil {
    /**
     * 默认最大的水位高度,控制着其他方块的同种液体是否能够流入此方块<br/>
     * 此值为1~16所有整数的最小公倍数
     */
    public static final int ONE_BLOCK_HEIGHT = 720720;
    public static final int SECOND_HEIGHT = ONE_BLOCK_HEIGHT /2,
            THIRD_HEIGHT = ONE_BLOCK_HEIGHT /3,
            FORTH_HEIGHT = ONE_BLOCK_HEIGHT /4,
            FIFTH_HEIGHT = ONE_BLOCK_HEIGHT /5,
            SIXTH_HEIGHT = ONE_BLOCK_HEIGHT /6,
            SEVENTH_HEIGHT = ONE_BLOCK_HEIGHT /7,
            EIGHTH_HEIGHT = ONE_BLOCK_HEIGHT /8,
            NINTH_HEIGHT = ONE_BLOCK_HEIGHT /9,
            TENTH_HEIGHT = ONE_BLOCK_HEIGHT /10,
            ELEVENTH_HEIGHT = ONE_BLOCK_HEIGHT /11,
            TWELFTH_HEIGHT = ONE_BLOCK_HEIGHT /12,
            THIRTEEN_HEIGHT = ONE_BLOCK_HEIGHT /13,
            FOURTEENTH_HEIGHT = ONE_BLOCK_HEIGHT /14,
            FIFTEENTH_HEIGHT = ONE_BLOCK_HEIGHT /15,
            SIXTEENTH_HEIGHT = ONE_BLOCK_HEIGHT /16,
            EMPTY_HEIGHT = 0;

    public static final IntList HEIGHTS = IntLists.unmodifiable(new IntArrayList(new int[]{
            ONE_BLOCK_HEIGHT
            , ONE_BLOCK_HEIGHT
            ,SECOND_HEIGHT
            ,THIRD_HEIGHT
            ,FORTH_HEIGHT
            ,FIFTH_HEIGHT
            ,SIXTH_HEIGHT
            ,SEVENTH_HEIGHT
            ,EIGHTH_HEIGHT
            ,NINTH_HEIGHT
            ,TENTH_HEIGHT
            ,ELEVENTH_HEIGHT
            ,TWELFTH_HEIGHT
            ,THIRTEEN_HEIGHT
            ,FOURTEENTH_HEIGHT
            ,FIFTEENTH_HEIGHT
            ,SIXTEENTH_HEIGHT}));

    private static final ThreadLocal<Set<FlowChoice>> FULL_FLOW_CHOICES = ThreadLocal.withInitial(HashSet::new);
    private static final ToIntFunction<FlowChoice> SORT_BY_NEXT_HEIGHT = FlowChoice::getNextLayerHeight;

    @Deprecated
    public static boolean isFluidAccepted(@Nonnull final World world,
                                          @Nonnull final BlockPos pos,
                                          @Nonnull final IBlockState state,
                                          @Nonnull final Fluid fluid,
                                          final boolean allowAir) {
        Block block = state.getBlock();

        if(block instanceof ILayeredFluidHost){
            return ((ILayeredFluidHost)block).isAcceptedFluid(world,pos,state,null,fluid);
        }

        if(allowAir){
            Fluid curFluid = FluidUtil.getFluid(state);
            return curFluid == null || curFluid == fluid;
        }
        return false;
    }

    @Deprecated
    public static int getFluidLayers(@Nonnull final World worldIn,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final IBlockState state,
                                     @Nonnull final Fluid fluid,
                                     final boolean allowAir){
        if(state.getBlock() instanceof ILayeredFluidHost){
            return ((ILayeredFluidHost)state.getBlock()).getLayers(worldIn,pos,state,null,fluid);
        }
        return allowAir? FluidUtil.getFluidQuanta(worldIn, pos, state):0;
    }

    /**
     * 自中心向四周进行平均流动
     * @param centralLayers 中心层数
     * @param heightPerLayer 中心每层高度
     * @param QBPerLayer 中心每层液体量，单位QB
     * @param minLayers 中心最低层数
     * @param choices 四周的流动选择
     * @return 中心剩下的层数
     */
    public static int averageFlow(int centralLayers,
                                  int heightPerLayer,
                                  final long QBPerLayer,
                                  final int minLayers,
                                  final @Nonnull List<FlowChoice> choices) {
        final @Nonnull Set<FlowChoice> fullChoices = FULL_FLOW_CHOICES.get();
        fullChoices.clear();
        if (choices.isEmpty()) return centralLayers;
        if(centralLayers <= minLayers) return centralLayers;
        final int oldCentralLayers = centralLayers;

        choices.sort(Comparator.comparingInt(SORT_BY_NEXT_HEIGHT));

        int centralHeight = centralLayers*heightPerLayer;
        FlowChoice choice;
        while ((choice = choices.get(0)).getNextLayerHeight() <= centralHeight-heightPerLayer) { //向四周分配流量
            if (choice.isFull()) {
                fullChoices.add(choices.remove(0));
                if (choices.isEmpty()) break;
                continue;
            }
            choice.addAmountInQB(QBPerLayer);
            centralLayers--;
            centralHeight -= heightPerLayer;
            if (centralLayers <= minLayers) break;
            choices.sort(Comparator.comparingInt(SORT_BY_NEXT_HEIGHT));
        }
        if (centralLayers == oldCentralLayers) return centralLayers;

        choices.addAll(fullChoices);
        fullChoices.clear();
        return centralLayers;
    }

    /**
     * 指定方块状态是否是一个载流方块的方块状态
     * @param state 需要检查的方块状态
     * @return 若该方块状态是一个载流方块的方块状态,则返回true.否则返回false
     */
    public static boolean isLayeredFluidHost(final IBlockState state){
        return state.getBlock() instanceof ILayeredFluidHost;
    }

    /**
     * 指定方块是否是一个载流方块
     * @param block 需要检查的方块
     * @return 若是,则返回true,否则返回false
     */
    public static boolean isLayeredFluidHost(final Block block){
        return block instanceof ILayeredFluidHost;
    }

    @Deprecated
    public static class Sources{
        private static final IBlockState ATMOSPHERE = Blocks.AIR.getDefaultState();
        public static boolean isAtmosphere(final IFluidFrom source){
            return source == ATMOSPHERE;
        }
        public static boolean isFluid(final IBlockState source){
            if(source == null) return false;
            return FluidUtil.isFluid(source.getBlock());
        }
        public static boolean isLayeredFluidHost(final IBlockState source){
            return LayeredFluidHostUtil.isLayeredFluidHost(source);
        }
    }

}
