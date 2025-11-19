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

package top.qiguaiaaaa.geocraft.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 载流方块，全称分层流体承载方块，英文名 Layered Fluid Host Block <br/>
 * 注意这和含水方块有本质区别，含水方块是载流方块的子集。例如，泥土不是含水方块，但应该为载流方块。<br/>
 * @author QiguaiAAAA
 */
public interface ILayeredFluidHost {

    /**
     * 当前方块是否允许存储对应流体
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 流体
     * @return 若可以,则返回true
     */
    boolean isAcceptedFluid(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nonnull Fluid fluid);

    /**
     * 查询指定位置载流方块的指定流体的层数。<br/>
     * 请注意，有可能当前位置的方块不是该方块，因此请注意判断当前位置的方块状态。
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 流体，若为null则表示查询当前方块所有流体的合层数
     * @return 对应流体的层数
     */
    int getLayers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid);

    /**
     * 查询指定流体的量，以QB为单位
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 需要查询的流体
     * @return 对应流体的量，以QB为单位
     */
    default long getAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return getLayers(world, pos, state, fluid)* getAmountInQBPerLayer(world, pos, state,fluid);
    }

    /**
     * 查询指定位置方块的指定流体的最大层数。
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 流体，若为null则表示查询当前方块所有流体的合层数最大值
     * @return 对应流体的层数最大值
     */
    default int getMaxLayers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable Fluid fluid){
        return (LayeredFluidHostUtil.DEFAULT_MAX_HEIGHT -getEmptyHeight(world,pos,state,fluid))/ getHeightPerLayer(world,pos,state);
    }

    /**
     * 查询指定流体最大含量，单位QB
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 需要查询的流体，若为null则表示查询当前方块所有流体的最大值
     * @return 对应流体的含量最大值，单位QB
     */
    default long getMaxAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid){
        return getMaxLayers(world, pos, state, fluid)* getAmountInQBPerLayer(world, pos, state,fluid);
    }

    /**
     * 返回液面高度。
     * @param world 所在世界
     * @param pos 所在位置
     * @param state 方块状态
     * @return 液面高度，数字越大越高。
     */
    default int getHeight(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nullable Fluid fluid){
        return getEmptyHeight(world,pos,state,fluid)+ getLayers(world,pos,state,fluid)* getHeightPerLayer(world,pos,state);
    }

    /**
     * 返回若没有指定液体的情况下的基准高度。含水方块或一般的流体方块固定为0。
     * @param world 所在世界
     * @param pos 查询位置
     * @param state 方块状态
     * @param fluid 流体，若为null则意思是没有含有任何流体时的基准高度
     * @return 基准高度，单位和液面高度一样
     */
    int getEmptyHeight(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nullable Fluid fluid);

    /**
     * 返回指定位置方块状态下指定流体能够有的最高液面高度
     * @param state 方块状态
     * @param fluid 流体，若为null则意思是含有任意流体时所能达到的最高液面高度
     * @return 最高液面高度
     */
    default int getMaxHeight(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nullable Fluid fluid){
        return getEmptyHeight(world,pos,state,fluid) + getMaxLayers(world,pos,state,fluid)* getHeightPerLayer(world,pos,state);
    }

    /**
     * 对于指定位置的方块状态来说,每层液体的高度，必须是{@link #getMaxHeight}的因数
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @return 每层的高度
     */
    int getHeightPerLayer(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state);

    /**
     * 获取每层的以QB为单位的液体量
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 查询的流体
     * @return QB为单位的液体量
     */
    long getAmountInQBPerLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid);

    /**
     * @see #addLayer(World, BlockPos, IBlockState, Fluid, int, int)
     */
    default void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer){
        addLayer(world, pos, state, fluid, layer,null,0,0);
    }

    /**
     * 添加或移除指定层(layer)指定的流体<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}<br/>
     * 一般请使用{@link #addLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}，因为该方法会直接添加而不返回具体添加了多少。
     * @param world 世界
     * @param pos 位置
     * @param state 状态，必须是当前方块的状态
     * @param fluid 需要添加或移除的流体
     * @param layer 层数,若为负值则为提取(remove or drain).建议使用{@link #drainLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}
     * @param disabledBlockFlags 禁止的方块更新 flags
     */
    default void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags){
        addLayer(world, pos, state, fluid, layer,null, disabledBlockFlags,0);
    }

    /**
     * 添加或移除指定层(layer)指定的流体<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}<br/>
     * 一般请使用{@link #addLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}，因为该方法会直接添加而不返回具体添加了多少。<br/>
     * 和 {@link #setLayer(World, BlockPos, IBlockState, Fluid, int, int, int)}不同的是，addLayer要求指定位置的方块状态一定是state。
     * @param world 世界
     * @param pos 位置
     * @param state 状态，必须是当前方块的状态
     * @param fluid 需要添加或移除的流体
     * @param layer 层数,若为负值则为提取(remove or drain).建议使用{@link #drainLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 需要的方块更新 flags
     */
    default void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags,final int enabledBlockFlags){
        addLayer(world, pos, state, fluid, layer,null, disabledBlockFlags, enabledBlockFlags);
    }

    /**
     * 添加或移除指定层(layer)指定的流体<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}<br/>
     * 一般请使用{@link #addLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}，因为该方法会直接添加而不返回具体添加了多少。<br/>
     * 和 {@link #setLayer(World, BlockPos, IBlockState, Fluid, int, int, int)}不同的是，addLayer要求指定位置的方块状态一定是state。
     * @param world 世界
     * @param pos 位置
     * @param state 状态，必须是当前方块的状态
     * @param fluid 需要添加或移除的流体
     * @param layer 层数,若为负值则为提取(remove or drain).建议使用{@link #drainLayer(World, BlockPos, IBlockState, Fluid, int, boolean)}
     * @param nbt NBT标签
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 需要的方块更新 flags
     */
    default void addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer, @Nullable NBTTagCompound nbt, final int disabledBlockFlags, final int enabledBlockFlags){
        if(layer == 0) return;
        final int curLayer = getLayers(world, pos, state, fluid),maxLayer = getMaxLayers(world, pos, state, fluid);
        layer = MathHelper.clamp(layer,-curLayer,maxLayer-curLayer);
        if(layer == 0) return;
        setLayer(world, pos, state, fluid, curLayer+layer,nbt, disabledBlockFlags,enabledBlockFlags);
    }

    /**
     * 添加指定层液体。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param doAdd 是否真的添加
     * @return 实际添加的层数
     */
    default int addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer, boolean doAdd){
        return addLayer(world, pos, state, fluid, layer,null, 0,0,doAdd);
    }

    /**
     * 添加指定层液体。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param doAdd 是否真的添加

     * @return 实际添加的层数
     */
    default int addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags, boolean doAdd){
        return addLayer(world, pos, state, fluid, layer,null, disabledBlockFlags,0, doAdd);
    }

    /**
     * 添加指定层液体。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 启用的方块更新 flags
     * @param doAdd 是否真的添加

     * @return 实际添加的层数
     */
    default int addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags,final int enabledBlockFlags, boolean doAdd){
        return addLayer(world, pos, state, fluid, layer,null, disabledBlockFlags, enabledBlockFlags, doAdd);
    }

    /**
     * 添加指定层液体。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param layer 层数
     * @param nbt NBT标签
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 启用的方块更新 flags
     * @param doAdd 是否真的添加

     * @return 实际添加的层数
     */
    default int addLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,@Nullable NBTTagCompound nbt,final int disabledBlockFlags,final int enabledBlockFlags, boolean doAdd){
        if(layer == 0) return 0;
        int curLayer = getLayers(world,pos,state,fluid);
        int layerInFact = MathHelper.clamp(layer,-curLayer, getMaxLayers(world,pos,state,fluid)-curLayer);
        if(layerInFact == 0) return 0;
        if(doAdd) addLayer(world, pos, state, fluid,layerInFact,nbt,disabledBlockFlags,enabledBlockFlags);
        return layerInFact;
    }

    /**
     * 添加指定量液体，以QB为单位。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param amount 液体量
     * @param doAdd 是否真的添加
     * @return 实际添加的层数
     */
    default long addAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, boolean doAdd){
        return addAmountInQB(world, pos, state, fluid, amount,null,0,0, doAdd);
    }

    /**
     * 添加指定量液体，以QB为单位。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param amount 液体量
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param doAdd 是否真的添加
     * @return 实际添加的层数
     */
    default long addAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, final int disabledBlockFlags, boolean doAdd){
        return addAmountInQB(world, pos, state, fluid, amount,null, disabledBlockFlags,0, doAdd);
    }

    /**
     * 添加指定量液体，以QB为单位。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param amount 液体量
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 允许的方块更新 flags
     * @param doAdd 是否真的添加
     * @return 实际添加的层数
     */
    default long addAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, final int disabledBlockFlags,final int enabledBlockFlags, boolean doAdd){
        return addAmountInQB(world, pos, state, fluid, amount,null, disabledBlockFlags, enabledBlockFlags, doAdd);
    }

    /**
     * 添加指定量液体，以QB为单位。<br/>
     * 在添加前一般需要检测{@link #canFill(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要添加的流体
     * @param amount 液体量
     * @param nbt NBT
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 允许的方块更新 flags
     * @param doAdd 是否真的添加
     * @return 实际添加的层数
     */
    default long addAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount,@Nullable NBTTagCompound nbt, final int disabledBlockFlags,final int enabledBlockFlags, boolean doAdd){
        if(amount == 0L) return 0L;
        if(amount < 0L) return -drainAmountInQB(world, pos, state, fluid, -amount, doAdd);
        final long amountPerLayer = getAmountInQBPerLayer(world, pos, state,fluid);
        amount -= (amount%amountPerLayer);
        if(amount == 0L) return 0L;
        long curAmount = getAmountInQB(world, pos, state, fluid);
        long amountInFact = Math.min(amount, getMaxAmountInQB(world, pos, state, fluid)-curAmount);
        if(amountInFact == 0) return 0;
        return addLayer(world,pos,state,fluid,(int)(amount/amountPerLayer),nbt,disabledBlockFlags,enabledBlockFlags,doAdd)*amountPerLayer;
    }

    /**
     * 吸取指定层流体<br/>
     * 在吸取前一般需要先检查{@link #canDrain(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要吸取的流体
     * @param layer 层数
     * @param doDrain 是否真的吸取
     * @return 实际吸取的层数
     */
    default int drainLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer, boolean doDrain){
        return drainLayer(world, pos, state, fluid, layer,0,0, doDrain);
    }

    /**
     * 吸取指定层流体<br/>
     * 在吸取前一般需要先检查{@link #canDrain(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要吸取的流体
     * @param layer 层数
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param doDrain 是否真的吸取
     * @return 实际吸取的层数
     */
    default int drainLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags, boolean doDrain){
        return drainLayer(world, pos, state, fluid, layer, disabledBlockFlags,0, doDrain);
    }

    /**
     * 吸取指定层流体<br/>
     * 在吸取前一般需要先检查{@link #canDrain(World, BlockPos, IBlockState, Fluid, EnumFacing, IBlockState)}
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 要吸取的流体
     * @param layer 层数
     * @param disabledBlockFlags 禁止的方块更新 flags
     * @param enabledBlockFlags 允许的方块更新 flags
     * @param doDrain 是否真的吸取
     * @return 实际吸取的层数
     */
    default int drainLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int layer,final int disabledBlockFlags,final int enabledBlockFlags, boolean doDrain){
        if(layer == 0) return 0;
        return -addLayer(world, pos, state, fluid, -layer,disabledBlockFlags,enabledBlockFlags, doDrain);
    }

    default long drainAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, boolean doDrain){
        return drainAmountInQB(world, pos, state, fluid, amount, 0,0, doDrain);
    }

    default long drainAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, final int disabledBlockFlags, boolean doDrain){
        return drainAmountInQB(world, pos, state, fluid, amount, disabledBlockFlags,0 ,doDrain);
    }

    default long drainAmountInQB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, long amount, final int disabledBlockFlags,final int enabledBlockFlags, boolean doDrain){
        if(amount == 0L) return 0L;
        if(amount < 0L) return -addAmountInQB(world, pos, state, fluid, -amount, doDrain);
        final long amountPerLayer = getAmountInQBPerLayer(world, pos, state,fluid);
        amount += (amountPerLayer-(amount%amountPerLayer));
        long curAmount = getAmountInQB(world, pos, state, fluid);
        long amountInFact = Math.min(amount,curAmount);
        if(amountInFact == 0) return 0;
        return drainLayer(world,pos,state,fluid,(int) (amount/amountPerLayer),disabledBlockFlags,enabledBlockFlags,doDrain)*amountPerLayer;
    }

    default FluidStack drainAmountInMB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull FluidStack stack, boolean doDrain){
        return new FluidStack(stack,QBUtil.toMB(drainAmountInQB(world,pos,state, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),doDrain)));
    }

    default FluidStack drainAmountInMB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull FluidStack stack, final int disabledBlockFlags, boolean doDrain){
        return new FluidStack(stack,QBUtil.toMB(drainAmountInQB(world,pos,state, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),disabledBlockFlags,doDrain)));
    }

    default FluidStack drainAmountInMB(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull FluidStack stack, final int disabledBlockFlags,final int enabledBlockFlags, boolean doDrain){
        return new FluidStack(stack,QBUtil.toMB(drainAmountInQB(world,pos,state, stack.getFluid(),QBUtil.toQBFromMB(stack.amount),disabledBlockFlags,enabledBlockFlags,doDrain)));
    }

    /**
     * 将指定流体流体层数设置到指定层数
     * @param world 世界
     * @param pos 当前位置
     * @param state 方框状态
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @return 是否成功
     */
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer){
        return setLayer(world, pos, state, fluid, newLayer,null,0,0);
    }

    /**
     * 将指定流体流体层数设置到指定层数
     * @param world 世界
     * @param pos 当前位置
     * @param state 方框状态
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @param disabledBlockFlags 禁止的BlockFlags
     * @return 是否成功
     */
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer,final int disabledBlockFlags){
        return setLayer(world, pos, state, fluid, newLayer,null, disabledBlockFlags,0);
    }

    /**
     * 在世界某处放置具有指定流体层数的方块
     * @param world 世界
     * @param pos 当前位置
     * @param state 方块状态，该方块状态是指示性的，不代表指定位置的确是本方块
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @param disabledBlockFlags 禁止的BlockFlags
     * @return 是否成功
     */
    default boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer,final int disabledBlockFlags,final int enabledBlockFlags){
        return setLayer(world, pos, state, fluid, newLayer,null, disabledBlockFlags, enabledBlockFlags);
    }

    /**
     * 在世界某处放置具有指定流体层数的方块。需要注意，提供的方块状态是指示性的。若提供的方块状态已经存储了一部分流体，并且当前方块支持多流体存储，并且设置的是另一个流体，那么放置时一般会放置同时含有存储流体+设置流体的方块。
     * @param world 世界
     * @param pos 当前位置
     * @param state 方块状态，该方块状态是指示性的，不代表指定位置的确是本方块
     * @param fluid 指定流体
     * @param newLayer 新的层数
     * @param disabledBlockFlags 禁止的BlockFlags
     * @return 是否成功
     */
    boolean setLayer(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, int newLayer,@Nullable NBTTagCompound nbt,final int disabledBlockFlags,final int enabledBlockFlags);

    /**
     * 指定流体以指定条件是否能够流入当前方块
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 需要流入的流体
     * @param side 流入的面
     * @param source 来源。若来源为{@link Blocks#AIR}则表示为大气。
     * @return 若可以，则返回true
     */
    default boolean canFill(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Fluid fluid, @Nonnull EnumFacing side, @Nullable IBlockState source){
        return !isFull(world,pos,state, fluid);
    }

    /**
     * 以指定条件是否能够吸取当前方块的流体
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 需要吸取的流体
     * @param side 吸取的面
     * @param source 来源。
     * @return 若可以，返回true。
     */
    default boolean canDrain(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nonnull Fluid fluid,@Nonnull EnumFacing side,@Nullable IBlockState source){
        return getLayers(world,pos,state, fluid) != 0;
    }

    /**
     * 指定流体含量是否已满
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 流体
     * @return 若当前方块不支持含有该流体或当前流体已满，则返回true，否则为false
     */
    default boolean isFull(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nullable Fluid fluid){
        return getLayers(world,pos,state,fluid) >= getMaxLayers(world,pos,state,fluid);
    }

    /**
     * 指定液体是否是空的
     * @param world 世界
     * @param pos 位置
     * @param state 状态
     * @param fluid 液体
     * @return 若液体的 {@link #getMaxLayers(World, BlockPos, IBlockState, Fluid)}为0或{@link #getLayers(World, BlockPos, IBlockState, Fluid)}不为0则为false，否则为true
     */
    default boolean isEmpty(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nullable Fluid fluid){
        if(isFull(world,pos,state,fluid)) return false;
        return getLayers(world,pos,state,fluid) <= 0;
    }
}
