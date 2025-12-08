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
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.geography.atmosphere.system.CloseAtmosphereSystem;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.JsonUtil;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public class CloseAtmosphereSystemInfo extends CommonAtmosphereSystemInfo {
    public static final String FINAL_TEMPERATURE = "final_temperature",
    MAX_WIND_SPEED = "max_wind_speed",
    PRESSURE = "pressure";
    public static CloseAtmosphereSystemInfo create(){
        return new CloseAtmosphereSystemInfo(new JsonObject());
    }

    public static CloseAtmosphereSystemInfo create(@Nonnull JsonObject object){
        return new CloseAtmosphereSystemInfo(object);
    }

    CloseAtmosphereSystemInfo(@Nonnull JsonObject object) {
        super(object,AtmosphereSystemType.CLOSE_ATMOSPHERE_SYSTEM);
    }

    @Override
    public CloseAtmosphereSystemInfo waterFreeze(boolean enable) {
        super.waterFreeze(enable);
        return this;
    }

    @Override
    public CloseAtmosphereSystemInfo waterEvaporate(boolean enable) {
        super.waterEvaporate(enable);
        return this;
    }

    @Override
    public CloseAtmosphereSystemInfo setRainSmoothingConstant(int constant) {
        super.setRainSmoothingConstant(constant);
        return this;
    }

    @Override
    public CloseAtmosphereSystemInfo setVaporExchangeRate(double rate) {
        super.setVaporExchangeRate(rate);
        return this;
    }

    @Nonnull
    @Override
    public AtmosphereSystemType getType() {
        return AtmosphereSystemType.CLOSE_ATMOSPHERE_SYSTEM;
    }

    public float getFinalTemperature(){
        return JsonUtil.readFloat(json,FINAL_TEMPERATURE,CloseAtmosphereSystem.HELL_TEMP,TemperatureProperty.MIN,Float.POSITIVE_INFINITY);
    }

    public double getMaxWindSpeed(){
        return JsonUtil.readDouble(json,MAX_WIND_SPEED,4,0,Double.POSITIVE_INFINITY);
    }

    public double getPressure(){
        return JsonUtil.readDouble(json,PRESSURE,2e5,0,Double.POSITIVE_INFINITY);
    }

    public CloseAtmosphereSystemInfo setMaxWindSpeed(double windSpeed){
        JsonUtil.setDouble(json,MAX_WIND_SPEED,BaseUtil.checkAndReturn(windSpeed,0,Double.POSITIVE_INFINITY));
        return this;
    }

    public CloseAtmosphereSystemInfo setPressure(double pressure){
        JsonUtil.setDouble(json,PRESSURE,BaseUtil.checkAndReturn(pressure,0,Double.POSITIVE_INFINITY));
        return this;
    }

    public CloseAtmosphereSystemInfo setFinalTemperature(float temp){
        JsonUtil.setFloat(json,FINAL_TEMPERATURE,BaseUtil.checkAndReturn(temp, TemperatureProperty.MIN,Float.POSITIVE_INFINITY));
        return this;
    }
}
