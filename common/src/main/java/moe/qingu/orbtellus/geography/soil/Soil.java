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

import moe.qingu.orbtellus.block.soil.IBlockSoil;

/**
 * 暂时用中文写比较直观
 * @author QGMoe
 */
public abstract class Soil { //unfinished
    protected static final double 毛细吸力系数 = 145.6d; //kPa*微米
    protected static final double 干端吸力上限 = 1e6; //kPa
    protected IBlockSoil block;

    //非常优雅的三个参数
    protected double 孔隙大小; //微米
    protected double 孔隙分布; //无量纲
    protected double 孔隙连通度; //无量纲 0-1

    // 派生量，基于湿度
    public abstract double 导水率函数(final int humidity); //QB/Bd 纤桶每布里丹，这单位太美妙了

    /**
     * 计算基质吸力
     * 基质吸力=中位吸力*e^{孔隙分布*正态分布右尾概率反过来查的玩意*(湿度/4)}
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
            default: return 干端吸力上限;
        }
        return 毛细吸力系数/孔隙大小*Math.exp(孔隙分布*Q);
    }

    public final double 进水阈值函数(final int humidity){ //kPa
        return 基质吸力函数(humidity)*0.5f;//暂定
    }

    // 土壤被推入或被吸走水的交互量，基于派生量和流强
    public abstract double 主动渗入比率函数(final int humidity, final float impetus);
    public abstract double 被动渗出比率函数(final int humidity, final float impetus);

    // 土壤主动吸入或排出水，返回打包的压强（float）和期望抽取量，带正负
    public abstract long 土壤交互参数函数(final int humidity);
}
