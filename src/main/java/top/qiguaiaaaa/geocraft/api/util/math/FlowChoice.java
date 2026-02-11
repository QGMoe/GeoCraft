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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public class FlowChoice {
    public final EnumFacing direction;
    public final int heightPerLayer, emptyHeight,currentLayers,maxLayers;
    public final long QBPerLayer;
    public final ILayeredFluidHost host;

    protected long addedAmountInQB;
    protected int addedLayers;

    /**
     * 创建一个基于载流方块的流动选择
     * @param world 世界
     * @param pos 目标位置
     * @param state 目标方块状态
     * @param host 目标载流方块
     * @param direction 方向
     * @param fluid 流体
     */
    public FlowChoice(@Nonnull final World world,
                      @Nonnull final BlockPos pos,
                      @Nonnull final IBlockState state ,
                      @Nonnull final ILayeredFluidHost host,
                      @Nonnull final EnumFacing direction,
                      @Nonnull final Fluid fluid) {
        this.direction = direction;
        final @Nonnull EnumFacing side = this.direction.getOpposite();
        this.heightPerLayer = host.getHeightPerLayer(world, pos,state,side,true);
        this.emptyHeight = host.getEmptyHeight(world,pos,state,side,fluid);
        this.currentLayers = host.getLayers(world, pos, state,side, fluid);
        this.maxLayers = host.getMaxLayers(world, pos, state,side, fluid);
        this.QBPerLayer = host.getAmountInQBPerLayer(world, pos, state,side, fluid);
        this.host = host;
    }

    /**
     * 创建一个常见(quantaPerBlock = 8)的流体流入空气或相同流体时的流动选择
     * @param direction 方向
     * @param currentLayers 当前层数
     */
    public FlowChoice(@Nonnull EnumFacing direction,int currentLayers){
        this.direction = direction;
        this.heightPerLayer = LayeredFluidHostUtil.EIGHTH_HEIGHT;
        this.emptyHeight = 0;
        this.currentLayers = currentLayers;
        this.maxLayers = 8;
        this.QBPerLayer = QBUtil.QUANTA_VOLUME;
        this.host = null;
    }

    /**
     * 创建一个流体流入空气或相同流体时的流动选择
     * @param direction 方向
     * @param currentLayers 当前层数
     * @param maxLayers 最大层数
     * @param QBPerLayer 每层流体量,单位QMU
     */
    public FlowChoice(@Nonnull EnumFacing direction,int currentLayers,int maxLayers,long QBPerLayer){
        this.direction = direction;
        this.heightPerLayer = LayeredFluidHostUtil.ONE_BLOCK_HEIGHT /maxLayers;
        this.emptyHeight = 0;
        this.currentLayers = currentLayers;
        this.maxLayers = maxLayers;
        this.QBPerLayer = QBPerLayer;
        this.host = null;
    }

    /**
     * 创建一个流体流入空气时的流动选择
     * @param direction 方向
     * @param maxLayers 最大层数
     * @param QBPerLayer 每层流体量，单位QMU
     */
    public FlowChoice(@Nonnull EnumFacing direction,int maxLayers,long QBPerLayer){
        this(direction,0,maxLayers, QBPerLayer);
    }

    /**
     * 创建一个常见的向空气的流动选择
     * @param direction 方向
     */
    public FlowChoice(@Nonnull EnumFacing direction){
        this(direction,0);
    }

    /**
     * 将该流动选择应用到具体世界中
     * @param world 所在世界
     * @param pos 应用的位置
     * @param state 对应的方块状态
     * @param fluid 流体
     * @return 剩余未应用的流体量，单位为QB
     */
    public long apply(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return Math.max(addedAmountInQB-Math.max(host.addAmountInQB(world,
                pos,
                state,
                this.direction.getOpposite(),
                fluid,
                addedAmountInQB,
                FluidHostOperation.DO_WITH_CERTAINTY,
                null,
                null),0),0);
    }

    public void addAmountInQB(long amount){
        addedAmountInQB += amount;
        addedLayers = (int) (addedAmountInQB / QBPerLayer);
    }

    public boolean isFull(){
        return currentLayers + addedLayers >= maxLayers;
    }

    public boolean isAir(){
        return host == null;
    }

    public int getAddedLayers() {
        return addedLayers;
    }

    public long getAddedAmountInQB() {
        return addedAmountInQB;
    }

    public int getNewLayers(){
        return addedLayers+currentLayers;
    }

    public int getHeight(){
        return emptyHeight+heightPerLayer*(currentLayers+addedLayers);
    }

    public int getNextLayerHeight(){
        return emptyHeight+heightPerLayer*(currentLayers+addedLayers+1);
    }
}
