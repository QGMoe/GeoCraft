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

package top.qiguaiaaaa.geocraft.api.util.math;

import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * 海拔
 * @author QiguaiAAAA
 */
public class Altitude {
    protected double val; //游戏海拔，单位为格

    /**
     * @param val 游戏海拔
     */
    public Altitude(double val){
        this(val,false);
    }

    /**
     * @param val 海拔
     * @param isPhysical 传入的是否是物理海拔
     */
    public Altitude(double val,boolean isPhysical){
        if(isPhysical) val = get游戏海拔(val);
        this.val = val;
    }

    /**
     * 获得游戏海拔
     * @return 游戏海拔，单位为格
     */
    public double get(){
        return val;
    }

    /**
     * 获得物理海拔，物理海拔=(游戏海拔-64)*24
     * @return 物理海拔，单位为米
     */
    public double get物理海拔(){
        return get物理海拔(val);
    }
    public void set(double newAltitude){
        this.val = newAltitude;
    }
    public void set物理海拔(double newAltitude){
        this.val = get游戏海拔(newAltitude);
    }
    public void set(@Nonnull Altitude altitude){
        this.val = altitude.val;
    }
    public boolean between(double a,double b){
        return val >= a && val <=b;
    }
    public static double get物理海拔(double 游戏海拔){
        return (游戏海拔-63)*64;
    }
    public static double get游戏海拔(double 物理海拔){
        return (物理海拔/64)+63;
    }
    public static double to游戏高度(double 物理高度){return 物理高度/64;}
    public static double to物理高度(double 游戏高度){return 游戏高度*64;}
    @Nonnull
    @Override
    public String toString() {
        return get物理海拔()+" m [ "+val+" ]";
    }

    /**
     * 获取某个区块的海拔高度中位数
     * @param chunk 区块
     * @return 海拔高度中位数
     */
    @Nonnull
    public static Altitude getMiddleHeight(@Nonnull Chunk chunk){
        int[] heights = chunk.getHeightMap().clone();
        Arrays.sort(heights); //获取海拔中位数
        return new Altitude(heights[127]);
    }
}
