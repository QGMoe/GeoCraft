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
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.property.FluidProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 流体状态
 * @since 0.1
 * @author QiguaiAAAA
 */
public abstract class FluidState implements INumberState<Integer>, IFluidTank {
    protected final Fluid fluid;
    protected int amount;
    public FluidState(@Nonnull Fluid fluid, int amount){
        this.fluid = fluid;
        this.amount = amount;
    }

    public void setAmount(int fluidAmount) {
        if(fluidAmount<0) fluidAmount = 0;
        this.amount = fluidAmount;
    }

    /**
     * @see #fill(FluidStack, boolean) 
     * @since 0.2.0
     */
    public int fill(int amount,final boolean doFill){
        if(this.amount + amount <0) return 0;
        if(!doFill) return amount;
        this.amount += amount;
        return amount;
    }

    @Override
    public int getFluidAmount() {
        return amount;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void load(@Nonnull Layer layer) {
        this.amount = 0;
    }

    @Override
    public boolean isLoaded() {
        return amount>=0;
    }

    @Nonnull
    @Override
    public abstract FluidProperty getProperty() ;

    /**
     * 获取气体对应的Forge流体
     * @return Forge流体
     */
    @Nonnull
    public Fluid getFluidType() {
        return fluid;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return new FluidStack(fluid,amount);
    }

    @Nonnull
    @Override
    public NBTBase serializeNBT() {
        return new NBTTagInt(amount);
    }

    @Override
    public void deserializeNBT(@Nonnull NBTBase nbt) {
        if(nbt instanceof NBTPrimitive){
            this.amount = ((NBTPrimitive) nbt).getInt();
        }
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(@Nonnull FluidStack resource, boolean doFill) {
        if(resource.getFluid() != fluid) return 0;
        if(this.amount + resource.amount <0) return 0;
        if(doFill) fill(resource.amount,true);
        return resource.amount;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        int drainedInFact = (this.amount- maxDrain <0)?this.amount: maxDrain;
        if(doDrain) fill(-drainedInFact,true);
        return new FluidStack(fluid,drainedInFact);
    }
    @Nonnull
    @Override
    public String toString() {
        return amount+"";
    }

    //***************
    // INumberState
    //***************


    @Override
    public int getInt() {
        return amount;
    }

    @Override
    public double getDouble() {
        return amount;
    }
}
