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

package top.qiguaiaaaa.geocraft.api.atmosphere.raypack;

import top.qiguaiaaaa.geocraft.api.util.APIUtil;

import javax.annotation.Nullable;

/**
 * 辐射热量包，用于在层级之间（一般是上下）传递热量
 * @since 0.1
 * @author QiguaiAAAA
 */
public class HeatPack implements StuffPack {
    public static final int MIN_HEAT = 20; //小于该值的热量包被视为空
    protected double heat;
    protected final HeatType type;

    /**
     * 创建一个空的热量包
     * @param type 热量包类型，可以为null
     */
    public HeatPack(@Nullable HeatType type){
        this(type,0);
    }

    /**
     * 创建一个具有指定热量的热量包
     * @param type 热量包类型，可以为null
     * @param raw 热量包内存有的热量
     */
    public HeatPack(@Nullable HeatType type, double raw){
        this.type = type;
        this.heat = Math.max(raw,0);
        if(Double.isInfinite(raw) || Double.isNaN(raw)){
            APIUtil.LOGGER.warn("{} creates a NaN or Infinite Heat Pack! Something must be wrong!",APIUtil.callerInfo(1));
        }
    }

    /**
     * 该热量包是否空了。
     * 若热量包的内容小于{@link #MIN_HEAT}，或值无效，则被视为空的
     * @return 若热量包是空的，则返回true
     */
    public boolean isEmpty(){
        return heat < MIN_HEAT || Double.isInfinite(heat) || Double.isNaN(heat);
    }
    public double getAmount() {
        return heat;
    }

    /**
     * 从该热量包中抽取指定量的热量
     * @param amount 期望抽取量
     * @return 实际抽取量
     */
    public double drawHeat(double amount){
        amount = Math.min(amount,heat);
        heat -= amount;
        return amount;
    }

    /**
     * 获取当前热量包的类型
     * @return 热量包类型
     */
    @Nullable
    public HeatType getType() {
        return type;
    }

    /**
     * 热量包的类型
     * 若类型为null，则层级应当将热量全部吸收
     * @since 0.1
     * @author QiguaiAAAA
     */
    public enum HeatType {
        SHORT_WAVE, //短波辐射
        LONG_WAVE //长波辐射
    }
}
