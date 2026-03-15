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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;
import top.qiguaiaaaa.geocraft.api.util.math.vec.BlockPosI;
import top.qiguaiaaaa.geocraft.block.finite.ILayeredFluidHostFiniteLiquid;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.VanillaFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * 参考{@link BlockDynamicLiquid}和{@link BlockLiquid}实现
 * @author QiguaiAAAA
 */
public final class FiniteFlowingVanilla extends VanillaFlowingVanilla {
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> slopeFlowableDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) List<FlowChoice> averageFlowChoices = new ArrayList<>();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) Set<EnumFacing> bestFlowDirections = EnumSet.noneOf(EnumFacing.class);
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) BlockPosI.Mutable facingPos$最外层$mut = new BlockPosI.Mutable();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) BlockPosI.Mutable downPos$mutable = new BlockPosI.Mutable();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) BlockPosI.Mutable facingPos$迭代用$mut = new BlockPosI.Mutable();
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    private static final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public FiniteFlowingVanilla(@Nonnull final BlockDynamicLiquid dynamic,
                                @Nonnull final BlockStaticLiquid _static,
                                @Nonnull final Fluid fluid) {
        super(dynamic,_static ,fluid);
    }

    @Nonnull
    public static FiniteFlowingVanilla getFlowingByMaterial(@Nonnull final Material material){
        return material == Material.WATER? FiniteFlowings.WATER_FLOW : FiniteFlowings.LAVA_FLOW;
    }

    /**
     * 检查方块四周可流动的选择
     * @param worldIn 所在世界
     * @param pos 方块位置
     * @param liquidQuanta 液体量
     * @param averageModeFlowDirections 平均流动模式下的选择列表
     * @param slopeModeFlowDirections Q>1 坡度流动模式下的选择集合
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void gatherFlowChoices(final @Nonnull World worldIn,
                                  final @Nonnull BlockPos pos,
                                  final @Nonnull IBlockState state,
                                  final int liquidQuanta,
                                  final @Nonnull List<FlowChoice> averageModeFlowDirections,
                                  final @Nullable Set<EnumFacing> slopeModeFlowDirections){
        for(final @Nonnull EnumFacing facing:EnumFacing.Plane.HORIZONTAL){
            final IBlockState facingState = worldIn.getBlockState(mutablePos.setPos(pos.getX()+facing.getXOffset(),pos.getY(),pos.getZ()+facing.getZOffset()));
            if(slopeModeFlowDirections==null?
                    !canFlowIntoRegardedPermeable(worldIn,mutablePos,facingState,state,facing.getOpposite()): //不启用坡度流动，不用管坡度流动的情况
                    !canFlowInto2(worldIn,mutablePos,facingState,state,facing.getOpposite())) //启用坡度流动，包含坡度流动的情况
                continue;

            final Block block = facingState.getBlock();
            final ILayeredFluidHost permeableBlock = (block instanceof ILayeredFluidHost)?(ILayeredFluidHost) block:null;

            int facingHeight,facingQuanta,facingHeightPerLayer = ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA;
            if(permeableBlock != null){
                facingHeight = permeableBlock.getHeight(worldIn,mutablePos,facingState,fluid);
                facingQuanta = permeableBlock.getLayers(worldIn,mutablePos,facingState,fluid);
            }else {
                int facingMeta = getDepth(facingState);
                if(facingMeta <0 || facingMeta>7) facingMeta = 8;
                facingQuanta = 8-facingMeta;
                facingHeight = facingQuanta* ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA;
            }

            if(facingHeight+facingHeightPerLayer<=(liquidQuanta-1)* ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA){
                averageModeFlowDirections.add(permeableBlock == null?
                        new FlowChoice(facing,facingQuanta):
                        new FlowChoice(worldIn,mutablePos,facingState,permeableBlock,facing,fluid));
            }

            if(!canFlowInto2RegardlessPermeable(facingState)) continue;
            if(slopeModeFlowDirections != null && facingHeight<liquidQuanta* ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA) slopeModeFlowDirections.add(facing);
        }
    }

    public boolean canFlowIntoRegardedPermeable(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nonnull IBlockState fromState,@Nonnull EnumFacing from){
        if(canFlowInto(state)) return true;
        if(state.getBlock() instanceof ILayeredFluidHost){
            final ILayeredFluidHost block = (ILayeredFluidHost) state.getBlock();
            return block.canFill(world, pos, state, fluid,from,fromState);
        }
        return false;
    }

    /**
     * Q>1 坡度流动模式下检查是否能够流入指定方块
     * @param state 检测位置方块状态
     * @return 如果可以，则返回true
     */
    public boolean canFlowInto2(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,@Nonnull IBlockState fromState,@Nonnull EnumFacing from){
        if(canFlowIntoRegardedPermeable(world,pos,state,fromState,from)) return true;
        return isEqualFluid(state);
    }

    /**
     * Q>1 坡度流动模式下检查是否能够流入指定方块
     * @param state 检测位置方块状态
     * @return 如果可以，则返回true
     */
    public boolean canFlowInto2RegardlessPermeable(@Nonnull IBlockState state){
        if(canFlowInto(state)) return true;
        return isEqualFluid(state);
    }

    public boolean canFlowIntoWhenSnowLayer(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,int quanta){
        if(state.getBlock() != Blocks.SNOW_LAYER) return true;
        if(((ILayeredFluidHost)Blocks.SNOW_LAYER).isFull(world,pos,state,FluidRegistry.WATER)) return false;
        return ((ILayeredFluidHost)Blocks.SNOW_LAYER).getHeight(world,pos,state, FluidRegistry.WATER)<(quanta-1)* ILayeredFluidHostFiniteLiquid.HEIGHT_PER_QUANTA;
    }

    public boolean canFlow(@Nonnull final World worldIn,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state){
        if (!worldIn.isAreaLoaded(pos, this.getSingleSlopeFindDistance(worldIn))){
            return false;
        }

        final int liquidMeta = state.getValue(LEVEL);
        if(liquidMeta >= 8){
            return false;
        }
        final int liquidQuanta = 8-liquidMeta;

        final @Nonnull IBlockState stateBelow = worldIn.getBlockState(downPos$mutable.setPos(pos).offsetM(EnumFacing.DOWN,1));
        if(canFlowDownTo(stateBelow)) return true;

        //坡度流动模式
        if(liquidMeta == 7){
            if(!FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaIs1.getValue() && isEqualFluid(stateBelow)) return false;
            slopeFlowableDirections.clear();
            singleSlopeAlgorithm(worldIn, pos, slopeFlowableDirections);
            return !slopeFlowableDirections.isEmpty();
        }
        //可流动方向检查
        averageFlowChoices.clear();
        final @Nullable Set<EnumFacing> slopeModeFlowDirections = FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()?
                slopeFlowableDirections :null;//非Q=1坡度模式可用方向
        gatherFlowChoices(worldIn,pos,state,liquidQuanta, averageFlowChoices,slopeModeFlowDirections);

        if(!averageFlowChoices.isEmpty()){ //平均流动模式
            return true;
        }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()){ //Q>1坡度模式
            if(!worldIn.isAreaLoaded(pos, getMultiSlopeFindDistance(worldIn))) return false;
            bestFlowDirections.clear();
            multiSlopeAlgorithm(worldIn,pos,slopeModeFlowDirections,liquidQuanta, bestFlowDirections);
            return !bestFlowDirections.isEmpty();
        }
        return false;
    }

    // ----------------------------------------
    // |                                      |
    // |      VERTICALLY FLOW                 |
    // |                                      |
    // ----------------------------------------

    /**
     * 在下方有相同流体的情况下，流下去
     * @param world 所在世界
     * @param currentPos 当前位置
     * @param downState 下方方块状态
     * @param liquidQuanta 当前流体量
     * @param tickRate 更新间隔
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void flowDown(@Nonnull final World world,
                         @Nonnull final BlockPos currentPos,
                         @Nonnull final IBlockState downState,
                         final int liquidQuanta,
                         final int tickRate){
        downPos$mutable.setPos(currentPos).offsetM(EnumFacing.DOWN,1);
        final int belowQuanta = Math.max(8-downState.getValue(LEVEL),1);
        final int totalQuanta = liquidQuanta+belowQuanta;
        if(totalQuanta<=8){
            world.setBlockToAir(currentPos);
            this.placeDynamicBlock(world,downPos$mutable,8-totalQuanta);
        }else{
            final int remain = totalQuanta-8;
            this.placeDynamicBlock(world,currentPos,8-remain);
            BlockUpdater.scheduleUpdate(world,currentPos,dynamic,tickRate);
            this.placeDynamicBlock(world,downPos$mutable,0);
        }
    }

    /**
     * 液体是否可以往下流动，会检查载流方块
     * TODO: 与下面的重载合并
     * @param world 世界
     * @param downPos 下方位置
     * @param curQuanta 当前量
     * @param fromState 当前状态
     * @param downState 下方方块状态
     * @return 如果可以往下流动，则返回true
     */
    public boolean canFlowDownTo(@Nonnull final World world,
                                 @Nonnull final BlockPos downPos,
                                 @Nonnull final IBlockState downState,
                                 final int curQuanta,
                                 @Nonnull final IBlockState fromState){
        if(canFlowDownTo(downState)) return true;
        if(downState.getBlock() instanceof ILayeredFluidHost){
            final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost) downState.getBlock();
            if(!host.canFill(world,downPos,downState, fluid,EnumFacing.UP,fromState)) return false;
            return host.addAmountInQB(world,downPos,downState,fluid, QBUtil.toQBFromQuanta(curQuanta),false)>0;
        }
        return false;
    }

    /**
     * 液体是否可以往下流动
     * @param stateDown 下方方块状态
     * @return 如果可以往下流动，则返回true
     */
    public boolean canFlowDownTo(final @Nonnull IBlockState stateDown){
        final @Nonnull Block block = stateDown.getBlock();
        final @Nonnull Material material = stateDown.getMaterial();
        if(block instanceof BlockLiquid){
            if(material == this.material) return stateDown.getValue(LEVEL) > 0; //同种液体，检查下方是否还有空间
            else return material == Material.WATER; //不同液体，对方还是水，在原版情况下应当是岩浆接触到水了
        }else if(block instanceof IFluidBlock){
            return false;
//            return ((IFluidBlock)block).getFluid().getDensity() < this.fluid.getDensity(); //下方液体密度小于当前液体
        }else if(block == Blocks.SNOW_LAYER){ //对雪的特判，TODO :与载流方块的逻辑合并
            return stateDown.getValue(BlockSnow.LAYERS) < 8;
        }else return !VanillaFlowingVanilla.isBlocked(stateDown);
    }

    // ----------------------------------------
    // |                                      |
    // |      SINGLE SLOPE FLOW               |
    // |                                      |
    // ----------------------------------------

    /**
     * Q=1 坡度流动模式的可流动方向寻找算法
     */
    public void singleSlopeAlgorithm(@Nonnull final World worldIn,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final Set<EnumFacing> collectorOfPossibleDirections){
        int difficulty = 1000;

        for (@Nonnull final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            facingPos$最外层$mut.setPos(pos).offsetM(enumfacing,1);
            final @Nonnull IBlockState state = worldIn.getBlockState(facingPos$最外层$mut);

            if (VanillaFlowingVanilla.isBlocked(state) || state.getBlock() == Blocks.SNOW_LAYER || FluidUtil.isFluid(state)) {
                continue;
            }

            final int slope;
            final @Nonnull IBlockState stateDown = worldIn.getBlockState(facingPos$最外层$mut.offsetM(EnumFacing.DOWN,1));
            /// 从这里开始 {@link #facingPos$最外层$mut} 表示为朝向方块的下方
            if (!canFlowDownTo(stateDown)) {
                slope = getSingleSlopeDistance(worldIn,
                        facingPos$最外层$mut.getX(),
                        pos.getY(),
                        facingPos$最外层$mut.getZ(),
                        1,
                        enumfacing.getOpposite());
            } else{
                slope = 0;
            }

            if (slope < difficulty)
                collectorOfPossibleDirections.clear();
            if (slope <= difficulty) {
                collectorOfPossibleDirections.add(enumfacing);
                difficulty = slope;
            }
        }
        if(difficulty == 1000) collectorOfPossibleDirections.clear();
    }

    public int getSingleSlopeDistance(@Nonnull final World worldIn,
                                      final int x,
                                      final int y,
                                      final int z,
                                      final int distance,
                                      @Nonnull final EnumFacing from) {
        int difficulty = 1000;

        for (final @Nonnull EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (enumfacing == from) continue;
            facingPos$迭代用$mut.setPos(x+enumfacing.getXOffset(),y,z+enumfacing.getZOffset());
            final @Nonnull IBlockState state = worldIn.getBlockState(facingPos$迭代用$mut);

            if (VanillaFlowingVanilla.isBlocked(state) || state.getBlock() == Blocks.SNOW_LAYER || FluidUtil.isFluid(state)) {
                continue;
            }

            final @Nonnull IBlockState stateBelow = worldIn.getBlockState(facingPos$迭代用$mut.offsetM(EnumFacing.DOWN,1));
            /// 从这里开始 {@link #facingPos$迭代用$mut} 表示为朝向方块的下方
            if (canFlowDownTo(stateBelow)) {
                return distance;
            }

            if (distance < getSingleSlopeFindDistance(worldIn)) {
                final int newDistance = getSingleSlopeDistance(worldIn,
                        facingPos$迭代用$mut.getX(),
                        y,
                        facingPos$迭代用$mut.getZ(),
                        distance + 1,
                        enumfacing.getOpposite());
                if (newDistance < difficulty) difficulty = newDistance;
            }
        }

        return difficulty;
    }

    public int getSingleSlopeFindDistance(@Nonnull final World worldIn) {
        return material == Material.LAVA && !worldIn.provider.doesWaterVaporize() ? 2 : 4;
    }

    // ----------------------------------------
    // |                                      |
    // |      MULTI SLOPE FLOW                |
    // |                                      |
    // ----------------------------------------

    /**
     * 多层坡度流动模式的可流动方向寻找算法
     * @param worldIn 所在世界
     * @param pos 位置
     * @param accessibleDirections 可流动的方向
     * @param thisQuanta 搜寻者的液体量
     */
    public void multiSlopeAlgorithm(final @Nonnull World worldIn,
                                    final @Nonnull BlockPos pos,
                                    final @Nonnull Set<EnumFacing> accessibleDirections,
                                    final int thisQuanta,
                                    final @Nonnull Set<EnumFacing> collectorOfPossibleDirections) {
        double difficulty = 10000d;

        for (final @Nonnull EnumFacing enumfacing : accessibleDirections) {
            final double slope = getMultiSlopeDistance(worldIn,
                    pos.getX()+enumfacing.getXOffset(),
                    pos.getY(),
                    pos.getZ()+enumfacing.getZOffset(),
                    1,
                    thisQuanta,
                    enumfacing.getOpposite());

            if (slope < difficulty)
                collectorOfPossibleDirections.clear();
            if (slope <= difficulty) {
                collectorOfPossibleDirections.add(enumfacing);
                difficulty = slope;
            }
        }
        if(difficulty == 10000d) collectorOfPossibleDirections.clear();
    }

    /***
     * 多层坡度流动模式的可流动方向寻找内层递归算法
     * @param worldIn 所在世界
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param distance 当前距离原点的距离
     * @param thisQuanta 搜寻者的液体量
     * @param from 来源方向
     * @return 难易度，即坡度的余切值
     */
    double getMultiSlopeDistance(final @Nonnull World worldIn,
                                 final int x,
                                 final int y,
                                 final int z,
                                 final int distance,
                                 final int thisQuanta,
                                 final @Nonnull EnumFacing from) {
        double difficulty = 10000d;

        for (@Nonnull final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (enumfacing == from) continue;
            facingPos$迭代用$mut.setPos(x+enumfacing.getXOffset(),y,z+enumfacing.getZOffset());
            final @Nonnull IBlockState state = worldIn.getBlockState(facingPos$迭代用$mut);
            final int quantaDiffer = getQuantaDiffer(state,thisQuanta);
            final boolean isFluid = FluidUtil.isFluid(state);
            final boolean isAir = state.getMaterial() == Material.AIR;
            if (VanillaFlowingVanilla.isBlocked(state) || !canFlowIntoWhenSnowLayer(worldIn,facingPos$迭代用$mut,state,thisQuanta) || (isFluid && quantaDiffer <1)) {
                continue;
            }
            if(isAir){
                final @Nonnull IBlockState stateBelow = worldIn.getBlockState(downPos$mutable.setPos((Vec3i) facingPos$迭代用$mut).offsetM(EnumFacing.DOWN,1));
                if (canFlowDownTo(stateBelow)) {
                    return FluidUtil.getFlowDifficulty(distance*8,8+thisQuanta);
                }else{
                    return FluidUtil.getFlowDifficulty(distance*8,thisQuanta);
                }
            }else if(quantaDiffer >1){ //同样的流体
                return FluidUtil.getFlowDifficulty(distance*8,thisQuanta-quantaDiffer);
            }else if(!isFluid){ //例如火把
                return FluidUtil.getFlowDifficulty(distance*8,thisQuanta);
            }

            if (distance < getMultiSlopeFindDistance(worldIn)) {
                final double slope = getMultiSlopeDistance(worldIn,
                        facingPos$迭代用$mut.getX(),
                        y,
                        facingPos$迭代用$mut.getZ(),
                        distance + 1,
                        thisQuanta,
                        enumfacing.getOpposite());
                if (slope < difficulty) difficulty = slope;
            }
        }

        return difficulty;
    }

    /**
     * 多层坡度流动模式的搜寻距离
     * @param worldIn 所在世界
     */
    public int getMultiSlopeFindDistance(final @Nonnull World worldIn) {
        if(!FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()) return 0;
        int ans = FluidPhysicsConfig.slopeFindDistanceForWaterWhenQuantaAbove1.getValue();
        if(this.material == Material.LAVA && !worldIn.provider.doesWaterVaporize()){
            ans = FluidPhysicsConfig.slopeFindDistanceForLavaWhenQuantaAbove1.getValue();
        }
        return ans;
    }

    /**
     * 获得对应方块状态的流体量与自身流体量的差值
     * @param state 对应方块状态
     * @param thisQuanta 自身流体量
     * @return 如果不是一个流体，则返回INT整形最大值。如果是一个流体，则返回自身流体量减去对方流体量的结果。
     */
    int getQuantaDiffer(final @Nonnull IBlockState state,final int thisQuanta){
        if(!isEqualFluid(state)) return Integer.MIN_VALUE;
        int quanta = 8- getDepth(state);
        if(quanta<0) quanta = 0;
        return thisQuanta - quanta;
    }

    // ----------------------------------------
    // |                                      |
    // |      PRESSURE FLOW                   |
    // |                                      |
    // ----------------------------------------

    @Nullable
    public IBlockState tryPressureFlow(final @Nonnull World worldIn,
                                       final @Nonnull BlockPos pos,
                                       final @Nonnull IBlockState state,
                                       final int flags){
        if(!FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue()) return null;
        if(worldIn.isRemote) return null;
        final @Nullable IFluidPressureSearchTaskResult res = FluidPressureSearchManager.getTaskResult(worldIn, pos);
        if(res == null || res.isEmpty()){
            return null;
        }
        IBlockState nowState =state;
        while (res.hasNext()){
            final BlockPos toPos = res.next();
            assert toPos != null;
            if(!isEqualFluid(nowState)) break;
            if(tryTeleport(worldIn,toPos,pos,nowState,flags)){
                nowState = Blocks.AIR.getDefaultState();
                break;
            }
            nowState = worldIn.getBlockState(pos);
        }
        return nowState;
    }

    public boolean tryTeleport(final @Nonnull World world,
                               final @Nonnull BlockPos to,
                               final @Nonnull BlockPos from,
                               final @Nonnull IBlockState fromState,
                               final int flag){
        if(!world.isBlockLoaded(to)) return false;
        final @Nonnull IBlockState toState = world.getBlockState(to);
        if(isEqualFluid(toState)){
            int toQuanta = Math.max(8- toState.getValue(LEVEL),1);
            int myQuanta = Math.max(8 -fromState.getValue(LEVEL),1);
            if(to.getY() == from.getY() && toQuanta>=myQuanta-1) return false;
            final int movQuanta = from.getY()==to.getY()?(myQuanta-toQuanta)>>1:Math.min(8-toQuanta,myQuanta);
            myQuanta -=movQuanta;
            if(myQuanta <= 0){
                world.setBlockState(from, Blocks.AIR.getDefaultState(),flag);
            }else world.setBlockState(from,fromState.withProperty(LEVEL,8-myQuanta),flag);
            toQuanta += movQuanta;
            world.setBlockState(to,dynamic.getDefaultState().withProperty(LEVEL,8-toQuanta),flag);
            return myQuanta<=0;
        }else if(!VanillaFlowingVanilla.isBlocked(toState)){
            int quanta = 8 -fromState.getValue(LEVEL);
            final int movQuanta = from.getY()==to.getY()?quanta>>1:quanta;
            if(movQuanta <= 0)return false;
            quanta -=movQuanta;
            if(quanta <= 0){
                world.setBlockState(from, Blocks.AIR.getDefaultState(),flag);
            }else world.setBlockState(from,fromState.withProperty(LEVEL,8-quanta),flag);
            FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,to,toState,fluid);
            world.setBlockState(to,dynamic.getDefaultState().withProperty(LEVEL,8-movQuanta),flag);
            return quanta <= 0;
        }
        return false;
    }

    /**
     * 以指定的液体等级(meta)流进指定位置
     * @param world 所在世界
     * @param pos 流进的指定位置
     * @param state 流进位置的方块状态
     * @param level 期望流进的液体等级
     */
    public void flowInto(@Nonnull final World world,@Nonnull final BlockPos pos,@Nonnull final IBlockState state,final int level){
        FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,pos,state,fluid);
        world.setBlockState(pos, dynamic.getDefaultState().withProperty(LEVEL, level), Constants.BlockFlags.DEFAULT);
    }

    /**
     * 是否是相同的原版液体
     * @param state 需要检测的方块状态
     */
    public boolean isEqualFluid(@Nonnull final IBlockState state){
        final Block block = state.getBlock();
        return block == dynamic || block == _static;
    }
}
