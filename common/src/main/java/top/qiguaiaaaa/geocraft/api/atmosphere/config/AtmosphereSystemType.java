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

package top.qiguaiaaaa.geocraft.api.atmosphere.config;

import javax.annotation.Nonnull;

/**
 * 维度使用的大气系统类型，用于判断采用哪个大气系统<br/>
 * 该类只有天圆地方本身的大气系统类型，通过判断玩家是否指定{@link #THIRD_PARTY_ATMOSPHERE_SYSTEM}以判断是否使用第三方模组大气系统。<br/>
 * 若类型为{@link #NO_ATMOSPHERE_SYSTEM}，则不应添加大气系统<br/>
 * 具体大气系统的配置信息需要查看{@link AtmosphereSystemInfo}
 */
public enum AtmosphereSystemType {
    SURFACE_ATMOSPHERE_SYSTEM("surface"),
    VANILLA_ATMOSPHERE_SYSTEM("vanilla"),
    CLOSE_ATMOSPHERE_SYSTEM("close"),
    THIRD_PARTY_ATMOSPHERE_SYSTEM("third_party"),
    NO_ATMOSPHERE_SYSTEM("none");
    public final String configName;
    AtmosphereSystemType(@Nonnull String configName){
        this.configName = configName;
    }

    private boolean isStringMatched(String s){
        return configName.equalsIgnoreCase(s);
    }

    /**
     * 将对应字符串反序列化为对应大气系统类型
     * @param s 字符串
     * @return 反序列化后的大气系统类型
     */
    @Nonnull
    public static AtmosphereSystemType getInstanceByString(@Nonnull String s){
        if(s.trim().isEmpty()) return NO_ATMOSPHERE_SYSTEM;
        for(AtmosphereSystemType type:values()){
            if(type.isStringMatched(s.trim())) return type;
        }
        return THIRD_PARTY_ATMOSPHERE_SYSTEM;
    }

    @Override
    public String toString() {
        return configName;
    }
}
