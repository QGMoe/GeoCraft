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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public abstract class FluidStackState extends FluidState{
    protected FluidStack stack;
    public FluidStackState(@Nonnull FluidStack stack) {
        super(stack.getFluid(), stack.amount);
        this.stack = stack;
    }

    @Override
    public void setAmount(int fluidAmount) {
        super.setAmount(fluidAmount);
        this.stack.amount = amount;
    }

    @Override
    public int getFluidAmount() {
        return stack.amount;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return stack;
    }

    @Nonnull
    @Override
    public NBTBase serializeNBT() {
        return stack.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(@Nonnull NBTBase nbt) {
        if(nbt instanceof NBTTagCompound){
            FluidStack newStack = FluidStack.loadFluidStackFromNBT((NBTTagCompound) nbt);
            if(newStack != null) stack = newStack;
        }
    }

    @Override
    public int fill(int amount, boolean doFill) {
        amount = super.fill(amount,doFill);
        if(doFill) stack.amount += amount;
        return amount;
    }
}
