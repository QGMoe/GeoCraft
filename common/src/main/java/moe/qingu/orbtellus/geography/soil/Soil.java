/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.geography.soil;

import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.laminarifer.flow.IFlowInitiator;
import moe.qingu.orbtellus.api.util.APIMathUtil;
import moe.qingu.orbtellus.block.soil.IBlockSoil;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * 暂时用中文写比较直观
 * @author QGMoe
 */
public class Soil implements IFlowInitiator<Soil> {
    public static final double _抽签刻期望间隔_ = 4096d / 3d / 1000d * 6; //simh 模拟时 ，抽签刻设计好后要迁移过去，暂时乘以6
    protected static final double _导水系数_ = 46.5; // QB/(Bd×μm^2)
    protected static final double _毛细吸力系数_ = 145.6d; //kPa×μm
    protected static final double _毛细滞后比_ = 0.5d;
    protected static final double _干端吸力上限_ = 1e6; //kPa
    protected static final double _重力压强_ = 9.81d; //kPa 一格高水柱的压强

    protected IBlockSoil block;

    //非常优雅的三个参数
    protected double $孔隙大小; //微米
    protected double $孔隙分布; //无量纲
    protected double $孔隙连通度; //无量纲 0-1

    // 派生量，基于湿度

    /**
     * 计算绝对导水率
     * 计划之后做Q缓存
     * @param humidity 湿度，0-4
     * @return QB/Bd 纤桶每布里丹，这单位太美妙了，返回导水率
     */
    public final double 导水率函数(final int humidity){
        final double Q;
        switch (humidity){
            case 4:
                Q = 1d;
                break;
            case 3:
                Q = APIMathUtil.normalTailProbability(-0.67449d + $孔隙分布);
                break;
            case 2:
                Q = APIMathUtil.normalTailProbability($孔隙分布);
                break;
            case 1:
                Q = APIMathUtil.normalTailProbability(0.67449d + $孔隙分布);
                break;
            case 0:
                Q = 0d;
                break;
            default: throw new IllegalArgumentException("h="+humidity);
        }
        return _导水系数_ * $孔隙连通度 * $孔隙大小 * $孔隙大小 *Math.exp($孔隙分布 * $孔隙分布)*Math.sqrt(humidity/4d)*Q*Q;
    }

    /**
     * 计算基质吸力
     * 基质吸力=中位吸力*e^{孔隙分布*正态分布右尾概率反过来查的函数(湿度/4)}
     * @param humidity 湿度，0-4
     * @return kPa，也是进气阈值
     */
    public final double 基质吸力函数(final int humidity){
        final double Q;
        switch (humidity){
            case 4: return 0d;
            case 3:
                Q = -0.67449d;
                break;
            case 2:
                Q = 0;
                break;
            case 1:
                Q = 0.67449d;
                break;
            default: return _干端吸力上限_;
        }
        return _毛细吸力系数_ / $孔隙大小 *Math.exp($孔隙分布 *Q);
    }

    public final double 进水阈值函数(final int humidity){ //kPa
        return 基质吸力函数(humidity)* _毛细滞后比_;
    }

    // 土壤被推入或被吸走水的交互量，基于派生量和流强

    /**
     * 计算土壤在主动渗入下的理想进水量
     * @param humidity 当前的湿度 0-4
     * @param impetus 有效流强，计算前压强要先扣掉 {@link #进水阈值函数(int)}，需要正值，单位 Bd
     * @return 理想进水量，单位 QB
     */
    public final long 主动渗入水量函数(final int humidity, final float impetus){
        return (long) (导水率函数(4) * impetus);
    }

    /**
     * 计算土壤在被动渗出下的理想出水量
     * @param humidity 当前的湿度 0-4
     * @param impetus 有效流强，计算前压强要先扣掉 {@link #基质吸力函数(int)}，需要正值
     * @return 理想出水量，单位 QB
     */
    public final long 被动渗出水量函数(final int humidity, final float impetus){
        return (long) (导水率函数(humidity) * impetus);
    }

    /**
     *  土壤主动吸入或排出水，
     * @param humidity 当前湿度 0-4
     * @param facing 吸入或排出面
     * @return 打包的压强（float） kPa 和期望抽取量 QB（正为吸入，负数为排出），带正负表示方向
     */
    public final long 土壤交互参数函数(final int humidity, final @Nonnull EnumFacing facing){
        final int yVec = facing.getDirectionVec().getY();
        final double gravity = yVec == 0? -_重力压强_ / 2d: yVec * _重力压强_;

        final double $吸入压 = 进水阈值函数(humidity) + gravity;
        if($吸入压 > 0d){
            final double left = (4 - humidity) * QBUnit.QUANTA_VOLUME_DOUBLE;
            final double amount = Math.min(left, 导水率函数(4) * $吸入压 * _抽签刻期望间隔_);
            return pack($吸入压, (int) amount);
        }

        final double $排出压 = 基质吸力函数(humidity) + gravity;
        if($排出压 < 0d){
            final double cur = humidity * QBUnit.QUANTA_VOLUME_DOUBLE;
            final double amount = Math.min(cur, 导水率函数(humidity) * -$排出压 * _抽签刻期望间隔_);
            return pack(-$排出压, -(int) amount);
        }

        return 0L;
    }

    private static long pack(final double p, final int fluidAmount){
        if(fluidAmount == 0) return 0L;
        return ((long) Float.floatToRawIntBits((float) p) << 32) | (fluidAmount & 0xFFFFFFFFL);
    }

    // 土壤其他效果相关函数

    /**
     * 孔隙水弯液面把颗粒拉在一起的等效应力计算
     * @return 吸力应力 kPa
     */
    public final double 吸力应力函数(final int humidity){
        return humidity / 4d * 基质吸力函数(humidity);
    }
}
