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

package top.qiguaiaaaa.geocraft.api.setting;

import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.api.configs.value.map.ConfigurableLinkedHashMap;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 查询天圆地方关于大气相关的配置
 * @author QiguaiAAAA
 */
public final class GeoAtmosphereSetting {
    /**
     * 下垫面重载的时间间隔,单位大气刻
     */
    private static int UNDERLYING_RELOAD_GAP = 60;
    /**
     * 大气刻每过多长游戏刻更新一次
     */
    private static int ATMOSPHERE_TICK = 60;
    /**
     * 大气刻在Minecraft中对应的虚拟模拟秒数
     */
    private static int ATMOSPHERE_TICK_SECONDS_IN_SIMULATION = (int)(ATMOSPHERE_TICK*86400L/24000);
    /**
     * 大气是否需要在遇到更新错误时,打印出详细的日志信息以供分析。
     * 需要注意，详细的信息很多。若错误批量出现，可能日志会爆炸。
     */
    private static boolean ENABLE_DETAILED_LOGGING = false;

    private static Map<Integer, AtmosphereSystemInfo> ATMOSPHERE_SYSTEM_INFO_MAP;

    public static void setUnderlyingReloadGap(int gap){
        if(gap <1) return;
        UNDERLYING_RELOAD_GAP = gap;
    }

    public static void setEnableDetailedLogging(boolean enableDetailedLogging) {
        ENABLE_DETAILED_LOGGING = enableDetailedLogging;
    }

    public static void setAtmosphereSystemInfoMap(@Nonnull ConfigurableLinkedHashMap<Integer,AtmosphereSystemInfo> infoMap){
        if(ATMOSPHERE_SYSTEM_INFO_MAP != null) return;
        ATMOSPHERE_SYSTEM_INFO_MAP = infoMap;
    }

    /**
     * 设置大气刻每过多少游戏刻执行
     * 实验性功能
     * @param atmosphereTick 大气刻对应的游戏刻长度
     */
    public static void setAtmosphereTick(int atmosphereTick) {
        ATMOSPHERE_TICK = atmosphereTick;
        ATMOSPHERE_TICK_SECONDS_IN_SIMULATION = (int)(ATMOSPHERE_TICK*86400L/24000);
    }

    public static int getUnderlyingReloadGap() {
        return UNDERLYING_RELOAD_GAP;
    }

    public static int getAtmosphereTick() {
        return ATMOSPHERE_TICK;
    }

    public static int getSimulationGap() {
        return ATMOSPHERE_TICK_SECONDS_IN_SIMULATION;
    }

    /**
     * 获取指定维度的大气信息
     * @param dim 维度
     * @return 返回对应的大气信息。若无，则返回
     */
    @Nonnull
    public static AtmosphereSystemInfo getAtmosphereSystemInfo(int dim){
        AtmosphereSystemInfo info = ATMOSPHERE_SYSTEM_INFO_MAP.get(dim);
        return info==null?AtmosphereSystemInfo.Final.FINAL:info;
    }

    public static boolean isEnableDetailedLogging() {
        return ENABLE_DETAILED_LOGGING;
    }
}
