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

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;
import 清汩萌.天圆地方.util.网格工具;
import 清汩萌.天圆地方.world.MockSimpleWorld;
import 清汩萌.天圆地方.world.MockWorldProvider;
import 清汩萌.天圆地方.world.light.SimpleLightGrid;
import 清汩萌.天圆地方.world.sandbox.MockHashSandbox;
import 清汩萌.天圆地方.world.sandbox.SandboxTestCase;
import 清汩萌.天圆地方.world.sandbox.TestArg;
import 清汩萌.天圆地方.world.storage.MockWorldInfo;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.亮度构造器;
import 清汩萌.造.空间.词块网格;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.assets.MockBlocks.Bases.〇;
import static 清汩萌.天圆地方.util.网格工具.打包网格数据;

/**
 * @author QiguaiAAAA
 */
public class TestUtils {
    private static final double EPSILON_DOUBLE = 1e-9;
    private static final float EPSILON_FLOAT = 1e-6f;

    /**
     * @author QiguaiAAAA, ChatGPT
     * @see MathUtil
     */
    public final static class TestMathUtil{

        /**
         * ChatGPT Generated Code
         * @see MathUtil#getAverage(long[]) 
         */
        @Test
        public void getAverageTest(){
            // ChatGPT Generated
            final Object[][] cases = new Object[][]{
                    {new long[]{0,1,2,3,4,5,6}, 3.0, "basic integer"},
                    {new long[]{1,2,3,4}, 2.5, "decimal result"},
                    {new long[]{42}, 42.0, "single element"},
                    {new long[]{0,0,0,0}, 0.0, "all zero"},
                    {new long[]{-1,-2,-3}, -2.0, "negative"},
                    {new long[]{-3,-1,1,3}, 0.0, "mixed"},
            };

            for (final @Nonnull Object[] c : cases) {
                final long[] input = (long[]) c[0];
                final double expected = (double) c[1];
                final @Nonnull String name = (String) c[2];

                final double actual = MathUtil.getAverage(input);

                天圆地方测试.LOGGER.info(
                        "Test case [{}] input={} expected={} actual={}",
                        name,
                        Arrays.toString(input),
                        expected,
                        actual
                );

                Assertions.assertEquals(
                        expected,
                        actual,
                        EPSILON_DOUBLE,
                        "Failed case: " + name
                );
            }
        }

        /**
         * ChatGPT Generated Code
         * @see MathUtil#getPercent(long[], double) 
         */
        @Test
        public void getPercentTest(){
            // ChatGPT Generated
            final Object[][] cases = new Object[][]{
                    {new long[]{1,2,3,4,5}, 0.2, 1L, "20% basic"},
                    {new long[]{1,2,3,4,5}, 0.5, 3L, "50% median-like"},
                    {new long[]{1,2,3,4,5}, 1.0, 5L, "100% max"},
                    {new long[]{5,4,3,2,1}, 0.4, 2L, "unsorted input"},
                    {new long[]{1,1,1,1,10}, 0.8, 1L, "duplicate values"},
                    {new long[]{1,2,3,4}, 0.25, 1L, "exact integer boundary"},
                    {new long[]{1,2,3,4}, 0.26, 2L, "ceil behavior test"},
                    {new long[]{1,2,3}, 0.0 , 1L, "zero test"}
            };

            for (final @Nonnull Object[] c : cases) {
                final long[] input = (long[]) c[0];
                final double percent = (double) c[1];
                final long expected = (long) c[2];
                final String name = (String) c[3];

                final long actual = MathUtil.getPercent(input, percent);

                天圆地方测试.LOGGER.info(
                        "Percent case [{}] input={} percent={} expected={} actual={}",
                        name,
                        Arrays.toString(input),
                        percent,
                        expected,
                        actual
                );

                Assertions.assertEquals(
                        expected,
                        actual,
                        "Failed case: " + name
                );
            }
        }

        @Test
        public void inRangeTest(){
            // {最小值, 检查值, 最大值} -> expected output
            final Object2BooleanArrayMap<int[]> map = createRangeDataMap(
                    new Object[]{1,3,6,true},
                    new Object[]{1,4,3,false},
                    new Object[]{2,1,4,false},
                    new Object[]{-19349553,20055,1953939,true},
                    new Object[]{-288842324,-29194,-2,true},
                    new Object[]{1991,24,43924,false}
            );

            for(final @Nonnull Object2BooleanMap.Entry<int[]> data:map.object2BooleanEntrySet()){
                天圆地方测试.LOGGER.info("Testing if {} is in [{},{}]",data.getKey()[1],data.getKey()[0],data.getKey()[2]);
                Assertions.assertEquals(data.getBooleanValue(),MathUtil.inRange(
                        data.getKey()[1],
                        data.getKey()[0],
                        data.getKey()[2]
                ));
            }
        }

        private static Object2BooleanArrayMap<int[]> createRangeDataMap(final @Nonnull Object[]... entries){
            final @Nonnull Object2BooleanArrayMap<int[]> map = new Object2BooleanArrayMap<>();
            for(final @Nonnull Object[] entry:entries){
                map.put(new int[]{(Integer)entry[0],(Integer)entry[1],(Integer) entry[2]},((Boolean) entry[3]).booleanValue());
            }
            return map;
        }
    }

    /**
     * ChatGPT Generated Tests
     * @author QiguaiAAAA, ChatGPT
     * @see BaseUtil
     */
    public final static class TestBaseUtil{
        /**
         * ChatGPT Generated
         */
        @Test
        public void checkAndReturnIntTest() {

            Assertions.assertEquals(5, BaseUtil.checkAndReturn(5,1,10));
            Assertions.assertEquals(1, BaseUtil.checkAndReturn(1,1,10));
            Assertions.assertEquals(10, BaseUtil.checkAndReturn(10,1,10));

            Assertions.assertEquals(Integer.MAX_VALUE,
                    BaseUtil.checkAndReturn(Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE));

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(0,1,10)
            );

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(11,1,10)
            );

            天圆地方测试.LOGGER.info("checkAndReturnIntTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void checkAndReturnLongTest() {
            Assertions.assertEquals(5L,
                    BaseUtil.checkAndReturn(5L,1L,10L));

            Assertions.assertEquals(Long.MAX_VALUE,
                    BaseUtil.checkAndReturn(Long.MAX_VALUE,Long.MIN_VALUE,Long.MAX_VALUE));

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(0L,1L,10L)
            );

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(Long.MIN_VALUE,0,Long.MAX_VALUE)
            );

            天圆地方测试.LOGGER.info("checkAndReturnLongTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void checkAndReturnDoubleTest() {

            Assertions.assertEquals(5.0,
                    BaseUtil.checkAndReturn(5.0,1.0,10.0),
                    EPSILON_DOUBLE);

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(0.5,1.0,10.0)
            );

            // NaN 特性：比较时永远为 false
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(Double.NaN,1.0,10.0)
            );

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(Double.POSITIVE_INFINITY,1.0,10.0)
            );

            天圆地方测试.LOGGER.info("checkAndReturnDoubleTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void checkAndReturnFloatTest() {

            Assertions.assertEquals(5.0f,
                    BaseUtil.checkAndReturn(5.0f,1.0f,10.0f),
                    EPSILON_FLOAT);

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(Float.NaN,1.0f,10.0f)
            );

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.checkAndReturn(0.5f,1.0f,10.0f)
            );

            天圆地方测试.LOGGER.info("checkAndReturnFloatTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void parseBooleanTest() {

            // 正常 true
            Assertions.assertTrue(BaseUtil.parseBoolean("true"));
            Assertions.assertTrue(BaseUtil.parseBoolean("TRUE"));
            Assertions.assertTrue(BaseUtil.parseBoolean("TrUe"));

            // 正常 false
            Assertions.assertFalse(BaseUtil.parseBoolean("false"));
            Assertions.assertFalse(BaseUtil.parseBoolean("FALSE"));
            Assertions.assertFalse(BaseUtil.parseBoolean("FaLsE"));

            天圆地方测试.LOGGER.info("parseBoolean normal cases passed");

            // 非法字符串
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.parseBoolean("hello")
            );

            // 接近但错误
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.parseBoolean("Truee")
            );

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.parseBoolean("0")
            );

            // 空字符串
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.parseBoolean("")
            );

            // null
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.parseBoolean(null)
            );

            天圆地方测试.LOGGER.info("parseBooleanTest passed");
        }
    }

    /**
     * @author QGMoe
     * @see top.qiguaiaaaa.geocraft.util.ChunkUtil
     */
    public final static class TestChunkUtil extends 天圆地方测试{
        @ParameterizedTest
        @MethodSource("prepareForGetNeighborsLightFor")
        public void testGetNeighborsLightFor(final @Nonnull NeighborsLightTestCase c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
            test(new Object[]{打包网格数据(c.$网格),
                    c.posToTest,
                    c.isBlockLight,
                    c.expected});
        }

        public static final class NeighborsLightTestCase extends SandboxTestCase {
            private static final Object2BooleanArrayMap<String> validType = new Object2BooleanArrayMap<>(
                    new String[]{"block","方块","sky","天空"},
                    new boolean[]{true,true,false,false}
            );
            @TestArg(value = "at",type = TestArg.Type.BLOCK_POS) int[] posToTest;
            @TestArg String type;
            @TestArg int expected;
            final boolean isBlockLight;


            public NeighborsLightTestCase(final @Nonnull 格文件 $格文件) {
                super($格文件);
                this.isBlockLight = validType.computeIfAbsent(StringUtil.strip(type).toLowerCase(Locale.ROOT), k -> Assertions.fail("unknown light type "+k));
            }
        }

        @Nonnull
        public static Stream<NeighborsLightTestCase> prepareForGetNeighborsLightFor(){
            return SandboxTestCase.findInputs("data/util/chunk/LightTest/",NeighborsLightTestCase::new);
        }

        @SuppressWarnings("unused")
        public static void testGetNeighborsLightFor_Inner(final @Nonnull Object[] $打包网格数据,
                                                          final @Nonnull int[] posToTestRaw,
                                                          final boolean typeRaw,
                                                          final int expected){
            final BlockPos postToTest = new BlockPos(posToTestRaw[0],posToTestRaw[1],posToTestRaw[2]);
            final 词块网格 $网格 = 网格工具.恢复网格数据($打包网格数据);
            天圆地方测试.LOGGER.info("Pos To Test:{} \n Data: {}",postToTest,$网格);
            final MockSimpleWorld world = MockSimpleWorld.create(MockWorldInfo.create(b-> b.withGameType(GameType.CREATIVE))
                    ,new MockWorldProvider().setSkyLight(true),false);
            final MockHashSandbox sandbox = new MockHashSandbox(〇);
            world.setSandbox(sandbox);
            if(typeRaw) world.setLightGridBlock(new SimpleLightGrid(EnumSkyBlock.BLOCK,亮度构造器.$亮度构造器.构造($网格)));//方块
            else world.setLightGridSky(new SimpleLightGrid(EnumSkyBlock.SKY,亮度构造器.$亮度构造器.构造($网格)));
            final int val = ChunkUtil.getNeighborsLightFor(world,typeRaw?EnumSkyBlock.BLOCK:EnumSkyBlock.SKY,postToTest);
            Assertions.assertEquals(expected,val);
        }
    }
}
