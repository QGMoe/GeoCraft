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

package 清汩萌.天圆地方.tests;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.assets.MockFluids;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static 清汩萌.天圆地方.assets.MockBlocks.LayeredFluidHosts.FLUID_HOST_COMMON;
import static 清汩萌.天圆地方.block.MockBlockFluidHostCommon.LAYERS;

/**
 * @author QiguaiAAAA
 */
public class TestLayeredFluidHost extends 天圆地方测试 {

    @Test
    public void testQB() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test();
    }

    @SuppressWarnings("unused")
    public static void testQB_Inner(){
        final @Nonnull ILayeredFluidHost host = FLUID_HOST_COMMON;
        final @Nonnull IBlockState defaultState = FLUID_HOST_COMMON.getDefaultState();
        long filled = host.addAmountInQB(null,BlockPos.ORIGIN,defaultState.withProperty(LAYERS,7), MockFluids.SNOW,QBUtil.QUANTA_VOLUME,false);
        Assertions.assertEquals(QBUtil.QUANTA_VOLUME,filled);
        filled = host.addAmountInQB(null,BlockPos.ORIGIN,defaultState.withProperty(LAYERS,3), MockFluids.SNOW,QBUtil.BUCKET_VOLUME,false);
        Assertions.assertEquals(QBUtil.BUCKET_VOLUME-3*QBUtil.QUANTA_VOLUME,filled);
        filled = host.addAmountInQB(null,BlockPos.ORIGIN,defaultState.withProperty(LAYERS,1), MockFluids.SNOW,QBUtil.BUCKET_VOLUME,true);
        Assertions.assertEquals(QBUtil.BUCKET_VOLUME-QBUtil.QUANTA_VOLUME,filled);
    }

    @Test
    public void testAverageFlow() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test();
    }

    @SuppressWarnings("unused")
    public static void testAverageFlow_Inner(){
        int T = 5000;
        while (T-->0){
            LOGGER.info("Test {} begin!",T+1);
            final @Nonnull Map<EnumFacing,IBlockState> facingState = new HashMap<>();
            final @Nonnull Random random = new Random(System.nanoTime());
            for(@Nonnull final EnumFacing facing:EnumFacing.HORIZONTALS){
                if(random.nextDouble()<0.2) continue;
                final @Nonnull IBlockState state = FLUID_HOST_COMMON.getDefaultState().withProperty(LAYERS,random.nextInt(8)+1);
                facingState.put(facing,state);
                LOGGER.info("Dir {} is state {}",facing,state);
            }

            final @Nonnull List<FlowChoice> averageModeFlowDirections = new ArrayList<>();
            facingState.forEach((facing, state) -> averageModeFlowDirections.add(
                    new FlowChoice(null,BlockPos.ORIGIN,state,FLUID_HOST_COMMON,facing, MockFluids.SNOW)));

            final int centralLayers = random.nextInt(8)+1;
            LOGGER.info("Central layers is {}",centralLayers);
            final int left = LayeredFluidHostUtil.averageFlow(centralLayers,
                    FLUID_HOST_COMMON.getHeightPerLayer(null,null,null),
                    FLUID_HOST_COMMON.getAmountInQBPerLayer(null,null,null, MockFluids.SNOW),
                    0,
                    averageModeFlowDirections
            );

            LOGGER.info("Central left : {}",left);

            for(final @Nonnull FlowChoice choice:averageModeFlowDirections){
                Assertions.assertNotNull(choice);
                Assertions.assertEquals(0,choice.apply(null,BlockPos.ORIGIN,facingState.get(choice.direction), MockFluids.SNOW));
            }
        }
    }
}
