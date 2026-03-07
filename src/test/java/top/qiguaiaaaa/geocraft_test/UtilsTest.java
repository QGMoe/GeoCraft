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

package top.qiguaiaaaa.geocraft_test;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author QiguaiAAAA
 */
public class UtilsTest {
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

                GeoCraftTest.LOGGER.info(
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

                GeoCraftTest.LOGGER.info(
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
                GeoCraftTest.LOGGER.info("Testing if {} is in [{},{}]",data.getKey()[1],data.getKey()[0],data.getKey()[2]);
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
        public void toIntArrayTest() {

            // 正常情况
            final String[] input1 = {"1","-2","3"};
            final int[] expected1 = {1,-2,3};
            Assertions.assertArrayEquals(expected1, BaseUtil.toIntArray(input1));

            // 边界值
            final String[] input2 = {
                    String.valueOf(Integer.MAX_VALUE),
                    String.valueOf(Integer.MIN_VALUE)
            };
            final int[] expected2 = {Integer.MAX_VALUE, Integer.MIN_VALUE};
            Assertions.assertArrayEquals(expected2, BaseUtil.toIntArray(input2));

            // 空数组
            final String[] input3 = {};
            Assertions.assertEquals(0, BaseUtil.toIntArray(input3).length);

            // 非法输入
            Assertions.assertThrows(
                    NumberFormatException.class,
                    () -> BaseUtil.toIntArray(new String[]{"a"})
            );

            Assertions.assertThrows(
                    NumberFormatException.class,
                    () -> BaseUtil.toIntArray(new String[]{"1.5"})
            );

            GeoCraftTest.LOGGER.info("toIntArrayTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void toLongArrayTest() {

            final String[] input1 = {"1","-2","3"};
            final long[] expected1 = {1L,-2L,3L};
            Assertions.assertArrayEquals(expected1, BaseUtil.toLongArray(input1));

            final String[] input2 = {
                    String.valueOf(Long.MAX_VALUE),
                    String.valueOf(Long.MIN_VALUE)
            };
            final long[] expected2 = {Long.MAX_VALUE, Long.MIN_VALUE};
            Assertions.assertArrayEquals(expected2, BaseUtil.toLongArray(input2));

            Assertions.assertEquals(0, BaseUtil.toLongArray(new String[]{}).length);

            Assertions.assertThrows(
                    NumberFormatException.class,
                    () -> BaseUtil.toLongArray(new String[]{"1.1"})
            );

            GeoCraftTest.LOGGER.info("toLongArrayTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void toBooleanArrayTest() {

            // 正常情况
            final String[] input1 = {"true","false","TRUE","FaLsE"};
            final boolean[] expected1 = {true,false,true,false};
            final boolean[] actual1 = BaseUtil.toBooleanArray(input1);

            GeoCraftTest.LOGGER.info("toBooleanArray normal result={}", Arrays.toString(actual1));
            Assertions.assertArrayEquals(expected1, actual1);

            // 单元素
            final String[] input2 = {"true"};
            final boolean[] expected2 = {true};
            Assertions.assertArrayEquals(expected2, BaseUtil.toBooleanArray(input2));

            // 空数组
            final String[] input3 = {};
            final boolean[] actual3 = BaseUtil.toBooleanArray(input3);
            Assertions.assertEquals(0, actual3.length);

            // 非法字符串
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.toBooleanArray(new String[]{"true","hello"})
            );

            // 非法布尔拼写
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> BaseUtil.toBooleanArray(new String[]{"Truee"})
            );

            GeoCraftTest.LOGGER.info("toBooleanArrayTest passed");
        }

        /**
         * ChatGPT Generated
         */
        @Test
        public void toDoubleArrayTest() {

            final String[] input = {"1.5","-2.3","3","1e3","NaN","Infinity"};
            final double[] actual = BaseUtil.toDoubleArray(input);

            Assertions.assertEquals(6, actual.length);
            Assertions.assertEquals(1.5, actual[0], EPSILON_DOUBLE);
            Assertions.assertEquals(-2.3, actual[1], EPSILON_DOUBLE);
            Assertions.assertEquals(3.0, actual[2], EPSILON_DOUBLE);
            Assertions.assertEquals(1000.0, actual[3], EPSILON_DOUBLE);
            Assertions.assertTrue(Double.isNaN(actual[4]));
            Assertions.assertEquals(Double.POSITIVE_INFINITY, actual[5]);

            Assertions.assertEquals(0, BaseUtil.toDoubleArray(new String[]{}).length);

            Assertions.assertThrows(
                    NumberFormatException.class,
                    () -> BaseUtil.toDoubleArray(new String[]{"abc"})
            );

            GeoCraftTest.LOGGER.info("toDoubleArrayTest passed");
        }

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

            GeoCraftTest.LOGGER.info("checkAndReturnIntTest passed");
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

            GeoCraftTest.LOGGER.info("checkAndReturnLongTest passed");
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

            GeoCraftTest.LOGGER.info("checkAndReturnDoubleTest passed");
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

            GeoCraftTest.LOGGER.info("checkAndReturnFloatTest passed");
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

            GeoCraftTest.LOGGER.info("parseBoolean normal cases passed");

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

            GeoCraftTest.LOGGER.info("parseBooleanTest passed");
        }
    }
}
