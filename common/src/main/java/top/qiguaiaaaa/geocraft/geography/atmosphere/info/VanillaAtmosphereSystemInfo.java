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

package top.qiguaiaaaa.geocraft.geography.atmosphere.info;

import com.google.gson.JsonObject;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemType;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.CommonAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.JsonUtil;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public class VanillaAtmosphereSystemInfo extends CommonAtmosphereSystemInfo {
    public static final String CLOUD_EXPONENT = "cloud_exponent",
    CLOUD_EXPONENT_THUNDERING = "thundering",
    CLOUD_EXPONENT_RAIN = "raining",
    WATER_DRAIN_MULTIPLIER = "maxWaterDrainedMultiplier";
    public static VanillaAtmosphereSystemInfo create(){
        return new VanillaAtmosphereSystemInfo(new JsonObject());
    }

    public static VanillaAtmosphereSystemInfo create(@Nonnull JsonObject object){
        return new VanillaAtmosphereSystemInfo(object);
    }

    VanillaAtmosphereSystemInfo(@Nonnull JsonObject object) {
        super(object,AtmosphereSystemType.VANILLA_ATMOSPHERE_SYSTEM);
    }

    @Override
    public VanillaAtmosphereSystemInfo waterFreeze(boolean enable) {
        super.waterFreeze(enable);
        return this;
    }

    @Override
    public VanillaAtmosphereSystemInfo waterEvaporate(boolean enable) {
        super.waterEvaporate(enable);
        return this;
    }

    @Override
    public VanillaAtmosphereSystemInfo setRainSmoothingConstant(int constant) {
        super.setRainSmoothingConstant(constant);
        return this;
    }

    @Override
    public VanillaAtmosphereSystemInfo setVaporExchangeRate(double rate) {
        super.setVaporExchangeRate(rate);
        return this;
    }

    public double getThunderingCloudExponent(){
        return JsonUtil.readDouble(JsonUtil.readObject(json,CLOUD_EXPONENT),CLOUD_EXPONENT_THUNDERING,60,0,100);
    }

    public double getRainingCloudExponent(){
        return JsonUtil.readDouble(JsonUtil.readObject(json,CLOUD_EXPONENT),CLOUD_EXPONENT_RAIN,30,0,100);
    }

    public int getMaxWaterDrainedMultiplier(){
        return JsonUtil.readInt(json,WATER_DRAIN_MULTIPLIER,50000,0,Integer.MAX_VALUE);
    }

    @Nonnull
    @Override
    public AtmosphereSystemType getType() {
        return AtmosphereSystemType.VANILLA_ATMOSPHERE_SYSTEM;
    }

    public VanillaAtmosphereSystemInfo setThunderingCloudExponent(double exponent){
        JsonUtil.setDouble(JsonUtil.readObject(json,CLOUD_EXPONENT),CLOUD_EXPONENT_THUNDERING, BaseUtil.checkAndReturn(exponent,0,100));
        return this;
    }

    public VanillaAtmosphereSystemInfo setRainingCloudExponent(double exponent){
        JsonUtil.setDouble(JsonUtil.readObject(json,CLOUD_EXPONENT),CLOUD_EXPONENT_RAIN,BaseUtil.checkAndReturn(exponent,0,100));
        return this;
    }

    public VanillaAtmosphereSystemInfo setMaxWaterDrainedMultiplier(int multiplier){
        JsonUtil.setInt(json,WATER_DRAIN_MULTIPLIER,BaseUtil.checkAndReturn(multiplier,0,Integer.MAX_VALUE));
        return this;
    }
}
