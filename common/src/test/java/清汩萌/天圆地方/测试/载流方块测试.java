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

package 清汩萌.天圆地方.测试;

import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.flow.AverageFlow;
import moe.qingu.orbtellus.api.laminarifer.request.FillLaminariferRequest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.laminarifer.flow.FlowChoice;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.原料.流体原料;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.原料.方块原料.LayeredFluidHosts.$有限流体模型;
import static 清汩萌.天圆地方.方块.模拟载流方块.LAYERS;

/**
 * @author QiguaiAAAA
 */
public class 载流方块测试 extends 天圆地方测试 {

    @ParameterizedTest
    @MethodSource("prepareCasesForTestQB")
    public void testQB(final @Nonnull TestQBCase c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(new Object[]{c.layers,c.amount,c.expected});
    }

    public static final class TestQBCase {
        final int layers;
        final long amount;
        final long expected;

        TestQBCase(final int layers, final long amount, final long expected) {
            this.layers = layers;
            this.amount = amount;
            this.expected = expected;
        }
    }

    @Nonnull
    public static Stream<TestQBCase> prepareCasesForTestQB(){
        return Arrays.stream(new long[][]{
                new long[]{7,QBUnit.QUANTA_VOLUME,QBUnit.QUANTA_VOLUME},
                new long[]{3,QBUnit.BUCKET_VOLUME,QBUnit.BUCKET_VOLUME - 3 * QBUnit.QUANTA_VOLUME},
                new long[]{1,QBUnit.BUCKET_VOLUME,QBUnit.BUCKET_VOLUME - QBUnit.QUANTA_VOLUME}
        }).map(c -> new TestQBCase(Math.toIntExact(c[0]),c[1],c[2]));
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public static void testQB_Inner(final int layers,final long amount,final long expected){
        final @Nonnull ILaminarifer laminarifer = $有限流体模型;
        final @Nonnull IBlockState defaultState = $有限流体模型.getDefaultState();
        try (final FillLaminariferRequest request = new FillLaminariferRequest().open()){
            final long filled = request.to(null,BlockPos.ORIGIN,defaultState.withProperty(LAYERS,layers))
                    .target(laminarifer)
                    .specific(流体原料.SNOW)
                    .amount(amount)
                    .side(EnumFacing.UP)
                    .fill(true);
            Assertions.assertEquals(expected,filled);
        }
    }

    @Test
    public void 平均流动测试() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test();
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public static void 平均流动测试_Inner(){
        int T = 5000;
        final  AverageFlow averageFlow = new AverageFlow(LaminariferModelBuffer.createFiniteVanillaLiquidModel());
        while (T-->0){
            final @Nonnull Random random = new Random(System.nanoTime());
            try (final AverageFlow flow = averageFlow){
                LOGGER.trace("Test {} begin!",T+1);

                flow.at(null,BlockPos.ORIGIN).fluid(流体原料.SNOW).centralModel.currentLayers = random.nextInt(8)+1;
                final long centralAmount = flow.centralModel.getAmountInQB();
                LOGGER.debug("Central is {} QB", centralAmount);

                for(@Nonnull final EnumFacing facing:EnumFacing.HORIZONTALS){
                    if(random.nextDouble()<0.2) continue;
                    final @Nonnull IBlockState state = $有限流体模型.getDefaultState().withProperty(LAYERS,random.nextInt(8)+1);
                    flow.addChoice(facing, state);
                    if(!flow.isLastChoiceAvailable()) flow.removeLastChoice();
                    LOGGER.debug("Dir {} is state {}",facing,state);
                }

                flow.resolve();

                final long left = flow.finalLayers * flow.centralModel.amountInQBPerLayer + flow.extraAmountInQB;

                LOGGER.debug("Central left : {} QB",left);

                long nearby = 0L;
                while (flow.hasNext()){
                    final FlowChoice choice = flow.next();
                    Assertions.assertNotNull(choice);
                    nearby += choice.getAddedAmountInQB() + choice.extraAmountInQB;
                    Assertions.assertEquals(0L, choice.extraAmountInQB);
                }

                Assertions.assertEquals(centralAmount, left + nearby);
            }
        }
    }
}
