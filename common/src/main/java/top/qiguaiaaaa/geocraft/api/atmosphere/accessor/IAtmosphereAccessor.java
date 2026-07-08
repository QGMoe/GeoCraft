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

package top.qiguaiaaaa.geocraft.api.atmosphere.accessor;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气和外部进行交互的接口
 * 推荐通过大气系统{@link IAtmosphereSystem}或{@link AtmosphereSystemManager}获取自己的{@link IAtmosphereAccessor}对象以操作大气，例如获取温度，释放热量等<br/>
 * 非底层或非常规操作则应当直接通过{@link Atmosphere}操作，否则应当使用 Accessor 访问。
 * @since 0.1
 * @author QiguaiAAA
 */
public interface IAtmosphereAccessor extends AutoCloseable {
    /**
     * 获取当前Accessor所在世界
     * @since 0.1
     * @return 世界,为一个 {@link World} 实例
     */
    @Nonnull
    default World getWorld(){
        return getSystem().getAtmosphereInfo().getWorld();
    }

    /**
     * 获取当前大气系统的大气数据提供者
     * @since 0.1
     * @return 一个 {@link IAtmosphereDataProvider}实例
     */
    @Nonnull
    default IAtmosphereDataProvider getDataProvider(){
        return getSystem().getDataProvider();
    }

    /**
     * 获取当前大气信息
     * @since 0.1
     * @return 一个 {@link AtmosphereInfo}实例
     */
    @Nonnull
    default AtmosphereInfo getAtmosphereInfo(){return getSystem().getAtmosphereInfo();}

    /**
     * 获取当前的大气系统
     * @since 0.1
     * @return 一个 {@link IAtmosphereSystem}实例
     */
    @Nonnull
    IAtmosphereSystem getSystem();

    /**
     * 该 Accessor 的位置
     * @since 0.1
     * @return 位置。为一个 {@link BlockPos}
     */
    @Nonnull
    BlockPos getPos();

    /**
     * 获取该Accessor目前位置的大气
     * @since 0.1
     * @return 大气,若大气未加载则为null
     */
    @Nullable
    Atmosphere getAtmosphereHere();

    /**
     * 获取该Accessor目前位置的大气数据
     * @since 0.1
     * @return 大气数据,若大气未加载则为null
     */
    @Nullable
    AtmosphereData getAtmosphereDataHere();

    /**
     * 获取该大气接口指向的大气是否处于加载状态
     * @since 0.1
     * @return 大气是否在加载
     */
    boolean isAtmosphereLoaded();

    /**
     * 当前位置是否可以访问大气。例如，若在地底的山洞，则不应操作到大气。
     * @since 0.2.0
     * @return 若可以访问，则返回 true
     */
    boolean canAccessAtmosphere();

    /**
     * 刷新该Accessor的状态
     * @since 0.1
     * @return 状态是否有更新
     */
    boolean refresh();

    /**
     * 设置当前位置的天光亮度,设置为负数以忽略天光亮度
     * @param light {@link EnumSkyBlock#SKY}天光亮度的值
     * @since 0.1
     */
    void setSkyLight(int light);

    /**
     * 设置当前方块是否不是空气
     * @param notAir 若不为空气，则请设为 true
     * @since 0.1
     */
    void setNotAir(boolean notAir);

    /**
     * 获取当前 Accessor 存储的天光亮度，注意这可能不是实际的天光亮度
     * @since 0.2.0
     * @return 一个天光亮度，介于 0 到 15.若不考虑光照条件,返回负数.
     */
    int getSkyLight();

    /**
     * 获取当前位置的温度，单位为开尔文
     * @since 0.1
     * @return 温度
     */
    double getTemperature();

    /**
     * 获取当前位置的温度，并指定是方块温度还是大气温度，单位为开尔文
     * @param notAir 是否需要获取方块温度
     * @since 0.1
     * @return 温度
     */
    double getTemperature(boolean notAir);

    /**
     * 获取当前位置的气压，单位为 Pa
     * @since 0.1
     * @return 气压
     */
    double getPressure();

    /**
     * 获取当前位置的水汽压，单位为 Pa
     * @since 0.1
     * @return 水汽压
     */
    double getWaterPressure();

    /**
     * 获取当前位置的风向量
     * @since 0.1
     * @return 风向量
     */
    @Nonnull
    Vec3d getWind();

    /**
     * 将指定量和状态的液体填充到大气中
     * @since 0.2.0
     * @param amount 量，单位mB
     * @param state 物质状态
     * @param temp 液体温度，单位开尔文
     * @param doFill 是否真的要填充
     * @return 实际填充的量
     */
    int fillFluidToAtmosphere(@Nonnull Fluid fluid, int amount,@Nonnull final StateOfMatter state,double temp,final boolean doFill);

    /**
     * @see #fillFluidToAtmosphere(Fluid, int, StateOfMatter, double, boolean)
     * @since 0.2.0
     */
    int fillFluidToAtmosphere(@Nonnull Fluid fluid, @Nonnull final FluidStack stack,@Nonnull final StateOfMatter state,double temp,final boolean doFill);

    /**
     * 从大气中抽取指定最大量和状态的流体
     * @since 0.2.0
     * @param fluid 流体
     * @param state 物质状态
     * @param maxDrainedAmount 最大抽取量
     * @param doDrain 是否真的要抽取
     * @return 实际抽取的量
     */
    int drainFluidFromAtmosphere(@Nonnull Fluid fluid,@Nonnull final StateOfMatter state,final int maxDrainedAmount,boolean doDrain);

    /**
     * @see #drainFluidFromAtmosphere(Fluid, StateOfMatter, int, boolean)
     * @since 0.2.0
     */
    @Nullable
    FluidStack drainFluidStackFromAtmosphere(@Nonnull Fluid fluid,@Nonnull final StateOfMatter state,final int maxDrainedAmount,boolean doDrain);

    /**
     * 将指定的热量释放到大气中
     * @since 0.1
     * @param amount 释放的热量
     */
    void putHeatToAtmosphere(double amount);

    /**
     * 将指定的热量释放到下垫面中
     * @since 0.1
     * @param amount 释放的热量
     */
    void putHeatToUnderlying(double amount);

    /**
     * 将指定的热量释放到当前层级中，无论是大气还是下垫面或是其他层级
     * @since 0.1
     * @param amount 释放的热量
     */
    void putHeatToCurrentLayer(double amount);

    /**
     * 从大气中吸取热量
     * @since 0.1
     * @param amount 吸取量
     * @return 实际吸取量
     */
    double drainHeatFromAtmosphere(double amount);

    /**
     * 从下垫面吸收热量
     * @since 0.1
     * @param amount 吸取量
     * @return 实际吸取量
     */
    double drainHeatFromUnderlying(double amount);

    /**
     * 从当前层级吸取热量，无论是什么类型的层级
     * @param amount 吸取量
     * @return 实际吸取量
     */
    double drainHeatFromCurrentLayer(double amount);

    /**
     * 从该层往指定方向发送{@link HeatPack}，当前层级不会吸收该热量
     * @since 0.1
     * @param pack 热量包
     * @param direction 发射方向
     */
    void sendHeat(@Nonnull HeatPack pack,@Nullable EnumFacing direction);

    /**
     * @see #sendHeat(HeatPack, EnumFacing)
     * @since 0.1
     */
    void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3d directionVec);

    /**
     * @see #sendHeat(HeatPack, EnumFacing)
     * @since 0.1
     */
    void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3i directionVec);

    /**
     * 标记当前逻辑已经使用完成.注意同样的 accessor 复用,并非 close 之后就不再使用.
     * @since 0.2.0
     */
    @Override
    void close();
}
