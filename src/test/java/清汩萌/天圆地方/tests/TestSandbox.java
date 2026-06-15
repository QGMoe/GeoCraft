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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.assets.MockBlocks;
import 清汩萌.天圆地方.world.MockSimpleWorld;
import 清汩萌.天圆地方.world.sandbox.MockSimpleSandbox;
import 清汩萌.天圆地方.world.storage.MockWorldInfo;
import 清汩萌.造.空间.空间假设;

import javax.annotation.Nonnull;

import static 清汩萌.天圆地方.assets.MockBlocks.BUILDER;
import static 清汩萌.天圆地方.assets.MockBlocks.Bases.*;
import static 清汩萌.造.空间.IBlockStateGridBuilder.grid;

/**
 * @author QiguaiAAAA
 */
public class TestSandbox {

    /**
     * @author QiguaiAAAA, ChatGPT
     * @see MockBlocks.Bases
     */
    public static final class TestBases extends 天圆地方测试 {

        @Test
        public void generateFromCharactersTest() throws Exception {
            test();
        }

        @SuppressWarnings("unused")
        public static void generateFromCharactersTest_Inner(){
            空间假设.假设构造相同(grid().layer()
                            .row(石,石,石)
                            .row(石,〇,石)
                            .row(石,石,石).done()
                            .layer()
                            .row(〇,〇,〇)
                            .row(〇,崗,〇)
                            .row(〇,〇,〇)
                            .done().build()
                    ,BUILDER.构造().层()
                            .行("石石石")
                            .行("石〇石")
                            .行("石石石").完成()
                            .层()
                            .行("〇〇〇")
                            .行("〇崗〇")
                            .行("〇〇〇")
                            .完成().构造()
            );
        }

        @Test
        public void generateSingleLayerTest() throws Exception {
            test();
        }

        public static void generateSingleLayerTest_Inner(){
            空间假设.假设构造相同(grid().layer()
                            .row(石,石,石)
                            .row(石,崗,石)
                            .row(石,石,石).done().build()
                    ,BUILDER.构造().层()
                            .行("石石石")
                            .行("石崗石")
                            .行("石石石").完成().构造()
            );
        }

        @Test
        public void generateAllAirTest() throws Exception {
            test();
        }

        public static void generateAllAirTest_Inner(){
            空间假设.假设构造相同(grid().layer()
                            .row(〇,〇,〇)
                            .row(〇,〇,〇)
                            .row(〇,〇,〇).done().build()
                    ,BUILDER.构造().层()
                            .行("〇〇〇")
                            .行("〇〇〇")
                            .行("〇〇〇").完成().构造()
            );
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void characterMappingTest() throws Exception {
            test();
        }

        /**
         * ChatGPT Generated
         */
        public static void characterMappingTest_Inner(){
            final IBlockState[][][] A = BUILDER.构造().层()
                    .行("石土 〇")
                    .行("〇土1崗")
                    .行("崗〇 石")
                    .完成().构造();
            空间假设.假设构造相同(A,grid().layer()
                    .row(石,土0,〇)
                    .row(〇,土1,崗)
                    .row(崗,〇,石).done().build()
            );
            final @Nonnull MockSimpleWorld world = MockSimpleWorld.create(MockWorldInfo.create(b -> b.withGameType(GameType.CREATIVE)),false);
            world.setSandbox(new MockSimpleSandbox(A));
            Assertions.assertEquals(world.getBlockState(new BlockPos(1,0,1)),土1); //中间
            Assertions.assertEquals(world.getBlockState(new BlockPos(2,0,0)),〇); //右上角
            Assertions.assertEquals(world.getBlockState(new BlockPos(2,0,2)),石); //右下角
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void multiLayerHeightTest() throws Exception {
            test();
        }

        /**
         * ChatGPT Generated
         */
        public static void multiLayerHeightTest_Inner(){
            空间假设.假设构造相同(grid().layer()
                    .row(石,石,石)
                    .row(石,石,石)
                    .row(石,石,石).done()
                    .layer()
                    .row(〇,〇,〇)
                    .row(〇,〇,〇)
                    .row(〇,〇,〇).done()
                    .layer()
                    .row(閃,閃,閃)
                    .row(崗,崗,崗)
                    .row(䒚,粆,砂).done().build()
                    ,BUILDER.构造().层()
                    .行("石石石")
                    .行("石石石")
                    .行("石石石").完成()
                    .层()
                    .行("〇〇〇")
                    .行("〇〇〇")
                    .行("〇〇〇").完成()
                    .层()
                    .行("閃 閃 閃")
                    .行("崗 崗 崗")
                    .行("䒚0粆0砂0").完成().构造()
            );
        }
    }
}
