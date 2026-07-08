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

package top.qiguaiaaaa.geocraft.api.event.atmosphere;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 当大气被访问的时候调用
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public class AtmosphereAccessEvent extends AtmosphereEvent{
    private final IAtmosphereAccessor accessor;
    public AtmosphereAccessEvent(@Nonnull Atmosphere atmosphere, @Nonnull IAtmosphereAccessor accessor) {
        super(atmosphere);
        this.accessor = accessor;
    }

    @Nonnull
    public IAtmosphereAccessor getAccessor() {
        return accessor;
    }

    /**
     * 当向大气填充流体的时候调用
     * @since 0.2.0
     * @author QiguaiAAAA
     */
    @HasResult
    public static class FluidFill extends AtmosphereAccessEvent{
        private final Fluid fluid;
        private final FluidStack stack;
        private final int amount;
        private final StateOfMatter state;
        private final boolean doFill;
        private final double temp;

        private int filledAmount;

        public FluidFill(@Nonnull Atmosphere atmosphere,
                         @Nonnull IAtmosphereAccessor accessor,
                         @Nonnull Fluid fluid,
                         @Nullable FluidStack stack,
                         final int amount,
                         final double temp,
                         @Nonnull final StateOfMatter state,
                         final boolean doFill) {
            super(atmosphere, accessor);
            this.fluid = fluid;
            this.stack = stack;
            this.amount = amount;
            this.state = state;
            this.temp = temp;
            this.doFill = doFill;
        }

        public Fluid getFluid() {
            return fluid;
        }

        @Nullable
        public FluidStack getStack() {
            return stack;
        }

        public StateOfMatter getState() {
            return state;
        }

        public boolean isDoFill() {
            return doFill;
        }

        public int getAmount() {
            return amount;
        }

        public double getFluidTemperature() {
            return temp;
        }

        public void setFilledAmount(final int filledAmount) {
            this.filledAmount = filledAmount;
        }

        public int getFilledAmount() {
            return filledAmount;
        }
    }

    /**
     * 当从大气抽取流体的时候调用
     * @since 0.2.0
     * @author QiguaiAAAA
     */
    @HasResult
    public static class FluidDrain extends AtmosphereAccessEvent{
        private final Fluid fluid;
        private final int expectedDrainedAmount;
        private final StateOfMatter state;
        private final boolean doDrain;

        private final boolean requireStack;

        private int drainedAmount;
        private FluidStack drainedStack;

        public FluidDrain(@Nonnull Atmosphere atmosphere,
                         @Nonnull IAtmosphereAccessor accessor,
                         @Nonnull Fluid fluid,
                         final int amount,
                         final boolean requireStack,
                         @Nonnull final StateOfMatter state,
                         final boolean doDrain) {
            super(atmosphere, accessor);
            this.fluid = fluid;
            this.expectedDrainedAmount = amount;
            this.state = state;
            this.requireStack = requireStack;
            this.doDrain = doDrain;
        }

        public Fluid getFluid() {
            return fluid;
        }

        public StateOfMatter getState() {
            return state;
        }

        public int getExpectedDrainedAmount() {
            return expectedDrainedAmount;
        }

        public boolean isDoDrain() {
            return doDrain;
        }

        public boolean isRequireStack() {
            return requireStack;
        }

        public void setDrainedAmount(int drainedAmount) {
            this.drainedAmount = drainedAmount;
        }

        public void setDrainedStack(@Nullable FluidStack drainedStack) {
            this.drainedStack = drainedStack;
            if(drainedStack == null) return;
            setDrainedAmount(drainedStack.amount);
        }

        @Nullable
        public FluidStack getDrainedStack() {
            return drainedStack;
        }

        public int getDrainedAmount() {
            return drainedAmount;
        }
    }
}
