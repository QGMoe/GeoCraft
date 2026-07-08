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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.Event;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.UnderlyingLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereAccessEvent;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 一个最简单的{@link IAtmosphereAccessor}实现，所有数据均没有经过平滑处理
 */
public class DirectAtmosphereAccessor extends AbstractAtmosphereAccessor {

    public DirectAtmosphereAccessor(@Nonnull IAtmosphereSystem system, @Nonnull AtmosphereData data, @Nonnull BlockPos pos, boolean notAir) {
        super(system, data, pos, notAir);
    }

    @Override
    public boolean canAccessAtmosphere() {
        return skyLight>0;
    }

    @Override
    public double getTemperature() {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        return data.getAtmosphere().getTemperature(pos,notAir);
    }

    @Override
    public double getPressure() {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        return data.getAtmosphere().getPressure(pos);
    }

    @Override
    public double getWaterPressure() {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        return data.getAtmosphere().getWaterPressure(pos);
    }

    @Nonnull
    @Override
    public Vec3d getWind() {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        return data.getAtmosphere().getWind(pos);
    }

    @Override
    public int fillFluidToAtmosphere(@Nonnull final Fluid fluid,final int amount,final @Nonnull StateOfMatter state,final double temp,final boolean doFill) {
        assert data.getAtmosphere() != null;
        int filled = EventFactory.onFillFluidToAtmosphere(data.getAtmosphere(),this,fluid,temp,amount,null,state,doFill);
        if(filled >=0) return filled;
        if(fluid == FluidRegistry.WATER){
            return fillWaterToAtmosphere(amount,state,temp,doFill);
        }
        return 0;
    }

    @Override
    public int fillFluidToAtmosphere(@Nonnull final Fluid fluid, @Nonnull final FluidStack stack, @Nonnull final StateOfMatter state,final double temp,final boolean doFill) {
        assert data.getAtmosphere() != null;
        int filled = EventFactory.onFillFluidToAtmosphere(data.getAtmosphere(),this,fluid,temp,stack.amount,stack,state,doFill);
        if(filled >=0) return filled;
        if(fluid == FluidRegistry.WATER){
            return fillWaterToAtmosphere(stack.amount,state,temp,doFill);
        }
        return 0;
    }

    @Override
    public int drainFluidFromAtmosphere(@Nonnull final Fluid fluid, @Nonnull final StateOfMatter state,final int maxDrainedAmount,final boolean doDrain) {
        if(maxDrainedAmount <= 0) return 0;
        assert data.getAtmosphere() != null;
        final AtmosphereAccessEvent.FluidDrain event = EventFactory.onDrainedFluidToAtmosphere(data.getAtmosphere(), this,fluid,maxDrainedAmount,false,state,doDrain);
        if(event != null && event.hasResult()){
            switch (event.getResult()){
                case DENY:return 0;
                case ALLOW:{
                    if(event.getDrainedStack() != null) return event.getDrainedStack().amount;
                    return Math.max(event.getDrainedAmount(), 0);
                }
                case DEFAULT:break;
            }
        }
        if(fluid == FluidRegistry.WATER){
            return drainWaterFromAtmosphere(maxDrainedAmount,state,doDrain);
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drainFluidStackFromAtmosphere(@Nonnull final Fluid fluid, @Nonnull final StateOfMatter state, final int maxDrainedAmount, final boolean doDrain) {
        if(maxDrainedAmount <= 0) return null;
        assert data.getAtmosphere() != null;
        final AtmosphereAccessEvent.FluidDrain event = EventFactory.onDrainedFluidToAtmosphere(data.getAtmosphere(), this,fluid,maxDrainedAmount,true,state,doDrain);
        if(event != null && event.hasResult()){
            switch (event.getResult()){
                case DENY:return new FluidStack(fluid,0);
                case ALLOW:{
                    if(event.getDrainedStack() != null) return event.getDrainedStack();
                    if(event.getDrainedAmount()<=0) return null;
                    return new FluidStack(fluid,event.getDrainedAmount());
                }
                case DEFAULT:break;
            }
        }
        if(fluid == FluidRegistry.WATER){
            return new FluidStack(FluidRegistry.WATER,drainWaterFromAtmosphere(maxDrainedAmount,state,doDrain));
        }
        return null;
    }

    protected int fillWaterToAtmosphere(final int amount,@Nonnull final StateOfMatter state, final double temp,boolean doFill){
        assert data.getAtmosphere() != null;
        switch (state){
            case GAS: return data.getAtmosphere().addSteam(amount,pos,doFill);
            case LIQUID:return data.getAtmosphere().addWater(amount,pos,doFill);
        }
        return 0;
    }

    protected int drainWaterFromAtmosphere(final int amount,@Nonnull final StateOfMatter state,final boolean doDrain){
        assert data.getAtmosphere() != null;
        switch (state){
            case GAS: return -data.getAtmosphere().addSteam(-amount,pos,doDrain);
            case LIQUID:return data.getAtmosphere().drainWater(amount,pos,doDrain);
        }
        return 0;
    }

    @Override
    public void putHeatToAtmosphere(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        data.getAtmosphere().putHeat(amount,pos);
    }

    @Override
    public void putHeatToUnderlying(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        data.getAtmosphere().getUnderlying(pos).putHeat(amount,pos);
    }

    @Override
    public void putHeatToCurrentLayer(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        layer.putHeat(amount,pos);
    }

    @Override
    public double drainHeatFromAtmosphere(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return 0;
        if(layer instanceof AtmosphereLayer){
            return layer.drainHeat(amount,pos);
        }
        data.getAtmosphere().putHeat(-amount,pos);
        return amount;
    }

    @Override
    public double drainHeatFromUnderlying(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        UnderlyingLayer underlying = data.getAtmosphere().getUnderlying(pos);
        return underlying.drainHeat(amount,pos);
    }

    @Override
    public double drainHeatFromCurrentLayer(double amount) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return 0;
        return layer.drainHeat(amount,pos);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable EnumFacing direction) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,direction);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3d directionVec) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,directionVec);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3i directionVec) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,directionVec);
    }
}
