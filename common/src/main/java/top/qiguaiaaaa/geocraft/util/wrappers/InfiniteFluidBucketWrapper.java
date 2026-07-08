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

package top.qiguaiaaaa.geocraft.util.wrappers;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class InfiniteFluidBucketWrapper implements IFluidHandler {
    public static final InfiniteFluidBucketWrapper INFINITE_WATER_BUCKET_WRAPPER = new InfiniteFluidBucketWrapper(FluidRegistry.WATER);
    public static final InfiniteFluidBucketWrapper INFINITE_LAVA_BUCKET_WRAPPER = new InfiniteFluidBucketWrapper(FluidRegistry.LAVA);
    protected final IFluidTankProperties tankProperties;
    protected final FluidStack fluidStack;
    protected final Fluid fluid;
    public InfiniteFluidBucketWrapper(Fluid fluid){
        this.fluid = fluid;
        fluidStack = new FluidStack(fluid,Integer.MAX_VALUE);
        tankProperties = new FluidTankProperties(fluidStack,Integer.MAX_VALUE,false,true);
    }
    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] {tankProperties};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if(resource.getFluid() != fluid) return null;
        return new FluidStack(fluid,resource.amount);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return new FluidStack(fluid,maxDrain);
    }
}
