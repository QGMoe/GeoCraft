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

package top.qiguaiaaaa.geocraft_test.tests;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.assets.MockBlocks;
import top.qiguaiaaaa.geocraft_test.world.MockSimpleWorld;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSimpleSandbox;
import top.qiguaiaaaa.geocraft_test.world.storage.MockWorldInfo;

import javax.annotation.Nonnull;

import static top.qiguaiaaaa.geocraft_test.world.sandbox.MockSandboxEnvBuilder.layer;
import static top.qiguaiaaaa.geocraft_test.world.sandbox.MockSandboxEnvBuilder.line;
import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.Bases.*;

/**
 * @author QiguaiAAAA
 */
public class TestSandbox {

    /**
     * @author QiguaiAAAA, ChatGPT
     * @see MockBlocks.Bases
     */
    public static final class TestBases extends GeoCraftTest{

        @Test
        public void generateFromCharactersTest() throws Exception {
            test();
        }

        public static void generateFromCharactersTest_Inner(){
            BUILDER.assertEqualStructure(new IBlockState[][][]{
                    layer(null,
                            line(石,石,石),
                            line(石,〇,石),
                            line(石,石,石)),
                    layer(null,
                            line(〇,〇,〇),
                            line(〇,崗,〇),
                            line(〇,〇,〇))
            },new String[]{
                    layer("",
                            "石石石",
                            "石〇石",
                            "石石石"
                    ),
                    layer("",
                            "〇〇〇",
                            "〇崗〇",
                            "〇〇〇"
                    )
            },3);
        }

        @Test
        public void generateSingleLayerTest() throws Exception {
            test();
        }

        public static void generateSingleLayerTest_Inner(){
            BUILDER.assertEqualStructure(new IBlockState[][][]{
                    layer(null,
                            line(石,石,石),
                            line(石,崗,石),
                            line(石,石,石))
            },new String[]{
                    layer("",
                            "石石石",
                            "石崗石",
                            "石石石"
                    )
            },3);
        }

        @Test
        public void generateAllAirTest() throws Exception {
            test();
        }

        public static void generateAllAirTest_Inner(){
            BUILDER.assertEqualStructure(new IBlockState[][][]{
                    layer(null,
                            line(〇,〇,〇),
                            line(〇,〇,〇),
                            line(〇,〇,〇))
            },new String[]{
                    layer("",
                            "〇〇〇",
                            "〇〇〇",
                            "〇〇〇"
                    )
            },3);
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
            final String[] structure = new String[]{
                    layer("",
                            "石土 〇",
                            "〇土1崗",
                            "崗〇 石"
                    )
            };
            final IBlockState[][][] result = BUILDER.generateFromCharacters(3,structure);
            BUILDER.assertEqualStructure(result,new IBlockState[][][]{
                    layer(null,
                            line(石,土0,〇),
                            line(〇,土1,崗),
                            line(崗,〇,石))
            });
            final @Nonnull MockSimpleWorld world = MockSimpleWorld.create(MockWorldInfo.create(b -> b.withGameType(GameType.CREATIVE)),false);
            world.setSandbox(new MockSimpleSandbox(result));
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
            BUILDER.assertEqualStructure(new IBlockState[][][]{
                    layer(null,
                            line(石,石,石),
                            line(石,石,石),
                            line(石,石,石)),
                    layer(null,
                            line(〇,〇,〇),
                            line(〇,〇,〇),
                            line(〇,〇,〇)),
                    layer(null,
                            line(閃,閃,閃),
                            line(崗,崗,崗),
                            line(䒚,粆,砂))
            },new String[]{
                    layer("",
                            "石石石",
                            "石石石",
                            "石石石"
                    ),
                    layer("",
                            "〇〇〇",
                            "〇〇〇",
                            "〇〇〇"
                    ),
                    layer("",
                            "閃 閃 閃",
                            "崗 崗 崗",
                            "䒚0粆0砂0"
                    )
            },3);
        }
    }
}
