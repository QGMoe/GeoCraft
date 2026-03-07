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

package top.qiguaiaaaa.geocraft.util;

import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Random;

/**
 * 该类会在 Early Mixin 时访问；
 * 一个基础的类，存放了一些很基础的函数
 * @author QiguaiAAAA
 */
public final class BaseUtil {
    private static final Random random = new Random();

    private BaseUtil(){}

    public static File getSuggestedConfigurationFile(){
        final @Nonnull File configurationDir = new File("config");
        if(!configurationDir.exists()) return null;
        return new File(configurationDir+File.separator+ GeoCraft.MODID+".cfg");
    }

    /**
     * 调用给定的 Random，从而创造一个指定百分比的条件
     * @param rand 给定的 Random
     * @param possibility 指定百分比的条件
     * @return 一个 boolean。有 possibility 的概率为 true，有 1-possibility 的概率为 false
     */
    public static boolean getRandomResult(final @Nonnull Random rand,
                                          final double possibility){
        return rand.nextDouble() <= possibility;
    }

    /**
     * 将整个 String 数组转换为 int 数组
     * @param array 一个 String 数组，里面应该由可以转换为整型的字符串组成
     * @return 转换后的 int 数组
     * @see Integer#parseInt(String)
     * @throws NumberFormatException 存在一个无法转换成 int 的字符串
     */
    public static int[] toIntArray(final @Nonnull String[] array){
        final int[] ints = new int[array.length];
        for(int i=0;i<array.length;i++){
            ints[i] = Integer.parseInt(array[i]);
        }
        return ints;
    }

    /**
     * 将整个 String 数组转换为 long 数组
     * @param array 一个 String 数组，里面应该由可以转换为长整型的字符串组成
     * @return 转换后的 long 数组
     * @see Long#parseLong(String)
     * @throws NumberFormatException 存在一个无法转换成 long 的字符串
     */
    public static long[] toLongArray(final @Nonnull String[] array){
        final long[] longs = new long[array.length];
        for(int i=0;i<array.length;i++){
            longs[i] = Long.parseLong(array[i]);
        }
        return longs;
    }

    /**
     * 将整个 String 数组转换为 boolean 数组
     * @param array 一个 String 数组，里面应该全部由 "true" 和 "false" （不区分大小写） 组成
     * @return 转换后的 boolean 数组
     * @see BaseUtil#parseBoolean(String)
     * @throws IllegalArgumentException 存在一个无法转换成 boolean 的字符串
     */
    public static boolean[] toBooleanArray(final @Nonnull String[] array){
        final boolean[] booleans = new boolean[array.length];
        for(int i=0;i<array.length;i++){
            booleans[i] = BaseUtil.parseBoolean(array[i]);
        }
        return booleans;
    }

    /**
     * 将整个 String 数组转换为 double 数组
     * @param array 一个 String 数组，里面应该由可以转换为 double 的字符串组成
     * @return 转换后的 double 数组
     * @see Double#parseDouble(String)
     * @throws NumberFormatException 存在一个无法转换成 double 的字符串
     */
    public static double[] toDoubleArray(final @Nonnull String[] array){
        double[] doubles = new double[array.length];
        for(int i=0;i<array.length;i++){
            doubles[i] = Double.parseDouble(array[i]);
        }
        return doubles;
    }

    public static float checkAndReturn(final float num,final float min,final float max) throws IllegalArgumentException{
        if(num>=min && num<=max){
            return num;
        }
        throw new IllegalArgumentException("Value "+num+" must be in range ["+min+","+max+"]");
    }

    public static double checkAndReturn(final double num,final double min,final double max) throws IllegalArgumentException{
        if(num>=min && num<=max){
            return num;
        }
        throw new IllegalArgumentException("Value "+num+" must be in range ["+min+","+max+"]");
    }

    public static int checkAndReturn(final int num,final int min,final int max) throws IllegalArgumentException{
        if(num<min || num>max){
            throw new IllegalArgumentException("Value "+num+" must be in range ["+min+","+max+"]");
        }
        return num;
    }

    public static long checkAndReturn(final long num,final long min,final long max) throws IllegalArgumentException{
        if(num<min || num>max){
            throw new IllegalArgumentException("Value "+num+" must be in range ["+min+","+max+"]");
        }
        return num;
    }

    public static int getRandomPressureSearchRange() {
        return FluidPhysicsConfig.WEIGHT_DISTRIBUTION_FOR_PRESSURE_SEARCH_RANGE.getRandomResult(random);
    }

    /**
     * 将一个字符串转换为 boolean，要求字符串是 true 或 false，不考虑大小写
     * @param str 字符串
     * @return 一个 boolean。
     * @throws IllegalArgumentException 输入不能转换为一个 boolean
     */
    public static boolean parseBoolean(final @Nullable String str){
        final @Nonnull String FORMATTER = "%s is not a valid boolean!";
        if(str == null) throw new IllegalArgumentException("Input is NULL");
        else if("true".equalsIgnoreCase(str)) return true;
        else if("false".equalsIgnoreCase(str)) return false;
        throw new IllegalArgumentException(String.format(FORMATTER, str));
    }

}
