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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.assets.MockBlocks;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSimpleSandbox;

import java.lang.reflect.InvocationTargetException;

import static top.qiguaiaaaa.geocraft_test.world.sandbox.MockSandboxEnvBuilder.layer;

/**
 * @author QiguaiAAAA
 */
public class TestSandbox {

    /**
     * @author QiguaiAAAA, ChatGPT
     * @see MockBlocks.Bases
     */
    public static final class TestBases extends GeoCraftTest{

        private static final String SELF_CLS_NAME = TestBases.class.getName();

        public static void doTest() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
            GeoCraftTest.run(SELF_CLS_NAME,Thread.currentThread().getStackTrace()[2].getMethodName()+"_Inner");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void generateFromCharactersTest() throws Exception {
            doTest();
        }

        /**
         * ChatGPT Generated
         */
        public static void generateFromCharactersTest_Inner(){

            final int size = 3;

            final String[] structure = new String[]{
                    layer(
                            "",
                            "石石石",
                            "石〇石",
                            "石石石"
                    ),
                    layer(
                            "",
                            "〇〇〇",
                            "〇水〇",
                            "〇〇〇"
                    )
            };

            final IBlockState[][][] result = MockBlocks.Bases.generateFromCharacters(size, structure);

            GeoCraftTest.LOGGER.info("Generated structure height={}", result.length);

            Assertions.assertEquals(2, result.length);
            Assertions.assertEquals(size, result[0].length);

            final MockSimpleSandbox world = new MockSimpleSandbox(result);

            Assertions.assertEquals(
                    MockBlocks.Bases.〇,
                    world.getBlockState(new BlockPos(1,0,1))
            );

            Assertions.assertEquals(
                    MockBlocks.Bases.石,
                    world.getBlockState(new BlockPos(0,0,0))
            );

            Assertions.assertEquals(
                    MockBlocks.Bases.水,
                    world.getBlockState(new BlockPos(1,1,1))
            );

            Assertions.assertEquals(
                    MockBlocks.Bases.〇,
                    world.getBlockState(new BlockPos(2,1,2))
            );

            GeoCraftTest.LOGGER.info("generateFromCharactersTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void generateSingleLayerTest() throws Exception {
            doTest();
        }

        /**
         * ChatGPT Generated
         */
        public static void generateSingleLayerTest_Inner(){

            final int size = 3;

            final String[] structure = new String[]{
                    layer(
                            "",
                            "石石石",
                            "石水石",
                            "石石石"
                    )
            };

            final IBlockState[][][] result = MockBlocks.Bases.generateFromCharacters(size, structure);

            GeoCraftTest.LOGGER.info("Single layer height={}", result.length);

            Assertions.assertEquals(1, result.length);

            final MockSimpleSandbox world = new MockSimpleSandbox(result);

            Assertions.assertEquals(
                    MockBlocks.Bases.水,
                    world.getBlockState(new BlockPos(1,0,1))
            );

            Assertions.assertEquals(
                    MockBlocks.Bases.石,
                    world.getBlockState(new BlockPos(0,0,0))
            );

            GeoCraftTest.LOGGER.info("generateSingleLayerTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void generateAllAirTest() throws Exception {
            doTest();
        }

        /**
         * ChatGPT Generated
         */
        public static void generateAllAirTest_Inner(){

            final int size = 3;

            final String[] structure = new String[]{
                    layer(
                            "",
                            "〇〇〇",
                            "〇〇〇",
                            "〇〇〇"
                    )
            };

            final IBlockState[][][] result = MockBlocks.Bases.generateFromCharacters(size, structure);

            final MockSimpleSandbox world = new MockSimpleSandbox(result);

            for(int x=0;x<size;x++){
                for(int z=0;z<size;z++){
                    Assertions.assertEquals(
                            MockBlocks.Bases.〇,
                            world.getBlockState(new BlockPos(x,0,z))
                    );
                }
            }

            GeoCraftTest.LOGGER.info("generateAllAirTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void characterMappingTest() throws Exception {
            doTest();
        }

        /**
         * ChatGPT Generated
         */
        public static void characterMappingTest_Inner(){

            final int size = 3;

            final String[] structure = new String[]{
                    layer(
                            "",
                            "石水〇",
                            "〇石水",
                            "水〇石"
                    )
            };

            final IBlockState[][][] result = MockBlocks.Bases.generateFromCharacters(size, structure);

            final MockSimpleSandbox world = new MockSimpleSandbox(result);

            Assertions.assertEquals(MockBlocks.Bases.石, world.getBlockState(new BlockPos(0,0,0)));
            Assertions.assertEquals(MockBlocks.Bases.水, world.getBlockState(new BlockPos(1,0,0)));
            Assertions.assertEquals(MockBlocks.Bases.〇, world.getBlockState(new BlockPos(2,0,0)));

            Assertions.assertEquals(MockBlocks.Bases.石, world.getBlockState(new BlockPos(1,0,1)));

            GeoCraftTest.LOGGER.info("characterMappingTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void multiLayerHeightTest() throws Exception {
            doTest();
        }

        /**
         * ChatGPT Generated
         */
        public static void multiLayerHeightTest_Inner(){

            final int size = 3;

            final String[] structure = new String[]{
                    layer(
                            "",
                            "石石石",
                            "石石石",
                            "石石石"
                    ),
                    layer(
                            "",
                            "〇〇〇",
                            "〇〇〇",
                            "〇〇〇"
                    ),
                    layer(
                            "",
                            "水水水",
                            "水水水",
                            "水水水"
                    )
            };

            final IBlockState[][][] result = MockBlocks.Bases.generateFromCharacters(size, structure);

            final MockSimpleSandbox world = new MockSimpleSandbox(result);

            Assertions.assertEquals(MockBlocks.Bases.石, world.getBlockState(new BlockPos(1,0,1)));
            Assertions.assertEquals(MockBlocks.Bases.〇, world.getBlockState(new BlockPos(1,1,1)));
            Assertions.assertEquals(MockBlocks.Bases.水, world.getBlockState(new BlockPos(1,2,1)));

            GeoCraftTest.LOGGER.info("multiLayerHeightTest passed");
        }
    }
}
