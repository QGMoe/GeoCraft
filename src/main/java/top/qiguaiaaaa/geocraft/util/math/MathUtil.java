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

package top.qiguaiaaaa.geocraft.util.math;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import top.qiguaiaaaa.geocraft.api.util.math.Degree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public final class MathUtil {
    public static Degree 计算与水平面夹角(Vec3d vec){
        double len = vec.length();
        double rad = Math.asin(Math.abs(vec.y)/len);
        return new Degree(rad,true);
    }

    public static double tanh(final double x,final double a,final double m,final double d){
        return a*Math.tanh((x-d)/m)+a+1d;
    }

    public static int manhattanDistance(final @Nonnull Vec3i a,final @Nonnull Vec3i b){
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY()- b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * 获取以指定水平方向为正方向的情况下，对应速度的包括方向的大小
     * @param speed 原速度
     * @param dir 指定正方向
     * @return 速度大小，正方向为dir指定方向。需要注意，绝对值不会是原速度在水平方向上的投影，而是其包括了竖直方向分量的实际大小。除非其在水平方向上没有速度，这种情况下一定为0.
     */
    public static double 获得带水平正负方向的速度(Vec3d speed, EnumFacing dir){
        double b = new Vec3d(dir.getDirectionVec()).dotProduct(speed);
        if(b == 0) return 0;
        return speed.length()*(b>0?1:-1);
    }

    public static double getAverage(@Nonnull final long[] arr){
        double sum = 0;
        for(long val : arr) sum +=val;

        return sum/arr.length;
    }

    /**
     * 获取第 k 百分位数。注意这不会在 arr.length*percent 刚好为整数的时候取这个元素和下一个元素的平均值
     * @param arr 需要获取第 k 百分位数的数据，可以是乱序的，但不能为空
     * @param percent 第 k 百分位数，应当介于 [0,1]
     * @return 第 k 百分位数
     */
    public static long getPercent(@Nonnull final long[] arr,final double percent){
        final long[] cp = arr.clone();
        Arrays.sort(cp);
        final int loc = (int) Math.ceil(percent*cp.length);
        if(loc == 0) return cp[0];
        return cp[loc-1];
    }

    public static boolean inRange(final int n,int l,int r){
        if(l>r){
            l = (r = (l = l ^ r) ^ r) ^ l;
        }
        return n>=l && n <= r;
    }

    @Nullable
    public static RayTraceResult rayTrace(final @Nonnull EntityPlayer playerIn,
                                          final boolean useLiquids) {
        final float rotationXZ = playerIn.rotationPitch;
        final float rotationY = playerIn.rotationYaw;
        final double x = playerIn.posX;
        final double y = playerIn.posY + playerIn.getEyeHeight();
        final double z = playerIn.posZ;
        final @Nonnull Vec3d eyePos = new Vec3d(x, y, z);
        final float rotationYCosOpposite = MathHelper.cos(-rotationY * 0.017453292F - (float)Math.PI); // cos( -rotationY 转为弧度 - π ) = -cos(rotationY)
        final float rotationYSin = MathHelper.sin(-rotationY * 0.017453292F - (float)Math.PI); // sin(rotationY)
        final float rotationXZCosOpposite = -MathHelper.cos(-rotationXZ * 0.017453292F); // -cos(rotationXZ)
        final float lookY = MathHelper.sin(-rotationXZ * 0.017453292F);
        final float lookX = rotationYSin * rotationXZCosOpposite;
        final float lookZ = rotationYCosOpposite * rotationXZCosOpposite;
        final double reachDis = playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        final @Nonnull Vec3d eyesightVec = eyePos.add(lookX * reachDis, lookY * reachDis, lookZ * reachDis);
        return playerIn.getEntityWorld().rayTraceBlocks(eyePos, eyesightVec, useLiquids, !useLiquids, false);
    }

}
