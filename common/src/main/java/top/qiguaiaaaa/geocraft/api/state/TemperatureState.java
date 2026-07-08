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

package top.qiguaiaaaa.geocraft.api.state;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagFloat;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.APIUtil;

import javax.annotation.Nonnull;

import static top.qiguaiaaaa.geocraft.api.util.APIUtil.LOGGER;

/**
 * 温度状态
 * @author QiguaiAAAA
 */
public abstract class TemperatureState implements INumberState<Float> {
    protected float temperature; //单位开尔文
    public TemperatureState(float temp){
        this.temperature = temp;
    }

    /**
     * 获取温度值
     * @return 温度，单位为开尔文 K
     */
    public float get() {
        return temperature;
    }

    /**
     * 获取摄氏度单位下的温度
     * @return 温度，单位摄氏度
     */
    public final float getCelsius(){
        return temperature-TemperatureProperty.ICE_POINT;
    }

    public void set(float temperature) {
        if(temperature < TemperatureProperty.MIN){
            LOGGER.warn("{} wants to set temp to very small.", APIUtil.callerInfo(1));
            temperature = 3;
        }
        this.temperature = temperature;
    }
    public void set(@Nonnull TemperatureState temp){
        set(temp.temperature);
    }

    public void add(double temp){
        this.temperature += temp;
    }

    public void addHeat(double Q, double heatCapacity){
        if(Double.isInfinite(Q) || Double.isNaN(Q)){
            LOGGER.warn("{} wants to add a temperature state from {} K by {} FE",APIUtil.callerInfo(1),temperature,Q);
            return;
        }
        double tempChange = Q/heatCapacity;
        double res = temperature+tempChange;
        if(res< TemperatureProperty.MIN){
            LOGGER.warn("{} wants to add a temperature state to {} K from {} K by {} FE, min temperature is {}"
                    ,APIUtil.callerInfo(1),res,temperature,Q,TemperatureProperty.MIN);
            return;
        }else if(Double.isInfinite(res)){
            LOGGER.warn("{} wants to add a temperature state to {} from {} K by {} FE"
                    ,APIUtil.callerInfo(1),res,temperature,Q);
            return;
        }
        this.add(tempChange);

    }

    @Nonnull
    @Override
    public abstract TemperatureProperty getProperty() ;

    @Override
    public NBTTagFloat serializeNBT() {
        return new NBTTagFloat(temperature);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        if(nbt instanceof NBTPrimitive){
            this.temperature = ((NBTPrimitive) nbt).getFloat();
        }
    }

    @Override
    public String toString() {
        return Float.toString(temperature);
    }

    //***************
    // INumberState
    //***************

    @Override
    public int getInt() {
        return (int) temperature;
    }

    @Override
    public double getDouble() {
        return getFloat();
    }

    @Override
    public float getFloat() {
        return temperature;
    }
}
