/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.api.laminarifer.flow;

import moe.qingu.orbtellus.api.fluid.unit.FluidUnit;
import moe.qingu.orbtellus.api.laminarifer.AHUnit;
import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * 一个流动选择，用于平均流动 {@link AverageFlow}
 * @author QGMoe
 */
public class FlowChoice {
    public EnumFacing direction;
    public @Nullable ILaminarifer laminarifer;
    public @Nullable IBlockState state;

    /// 载流方块模型
    public @Nonnull LaminariferModelBuffer model = new LaminariferModelBuffer();

    public long addedLayers;
    public long extraAmountInQB;

    /**
     * 变成一个基于载流方块的流动选择
     * @param world 世界
     * @param pos 目标位置
     * @param state 目标方块状态
     * @param laminarifer 目标载流方块
     * @param direction 方向
     * @param fluid 流体
     */
    @Nonnull
    public final FlowChoice of(@Nonnull final World world,
                         @Nonnull final BlockPos pos,
                         @Nonnull final IBlockState state ,
                         @Nonnull final EnumFacing direction,
                         @Nonnull final ILaminarifer laminarifer,
                         @Nonnull final Fluid fluid,
                         @Nullable final NBTTagCompound nbt) {
        this.direction = direction;
        this.laminarifer = laminarifer;

        laminarifer.describeModel(world,pos,state,fluid,nbt,model);

        this.state = state;
        this.extraAmountInQB = this.addedLayers = 0L;
        return this;
    }

    /**
     * 变成一个常见最大层数为 8 的流体流入空气或相同流体时的流动选择
     * @param direction 方向
     * @param currentLayers 当前层数
     */
    @Nonnull
    public final FlowChoice of(@Nonnull final EnumFacing direction,final long currentLayers) {
        this.direction = direction;
        this.laminarifer = null;
        this.state = null;

        this.model.maxLayers = 8L;
        this.model.currentLayers = currentLayers;
        this.model.heightPerLayer = AHUnit.EIGHTH_FLUID;
        this.model.emptyHeight = 0L;
        this.model.amountInQBPerLayer = QBUnit.QUANTA_VOLUME;

        this.extraAmountInQB = this.addedLayers = 0L;
        return this;
    }

    /**
     * 变成一个常见最大层数为 8 的流体流入空气的流动选择
     * @param direction 方向
     */
    @Nonnull
    public final FlowChoice of(@Nonnull final EnumFacing direction){
        return of(direction,0L);
    }

    public final boolean sampleExtraAmount(final @Nonnull Random rand){
        final long sampled;
        this.addedLayers += (sampled = FluidUnit.sample(rand, extraAmountInQB, model.amountInQBPerLayer));
        this.extraAmountInQB = 0L;
        return sampled > 0L;
    }

    /**
     * 将该流动选择应用到具体世界中
     * @param world 所在世界
     * @param pos 应用的位置
     * @param fluid 流体
     * @return 剩余未应用的流体量，单位为QB
     */
    public final long apply(@Nonnull final World world,
                      @Nonnull final BlockPos pos,
                      @Nonnull final Fluid fluid,
                      @Nullable final NBTTagCompound tag,
                      final long pulse,
                      @Nullable final IFlowSource<?> source,
                      final long blockFlagModifier){
        sampleExtraAmount(world.rand);
        assert laminarifer != null && state != null;
        return (addedLayers - laminarifer.addLayer(world, pos, state, fluid, tag, addedLayers, true, pulse, source, blockFlagModifier)) * model.amountInQBPerLayer;
    }

    public final boolean isAir(){
        return laminarifer == null;
    }

    public final boolean canFill(@Nonnull final World world,
                                 @Nonnull final BlockPos pos,
                                 @Nonnull final Fluid fluid,
                                 @Nullable final NBTTagCompound nbt,
                                 @Nullable final IFlowSource<?> source) {
        if(laminarifer != null){
            assert state != null;
            return laminarifer.canFill(world,pos,state,direction.getOpposite(),fluid,nbt,source);
        }else return true;
    }

    public final long getAddedAmountInQB() {
        return addedLayers*model.amountInQBPerLayer;
    }

    public final long getNewLayers(){
        return addedLayers+model.currentLayers;
    }

    public final long getNewHeight(){
        return model.getHeight()+model.heightPerLayer*addedLayers;
    }
}
