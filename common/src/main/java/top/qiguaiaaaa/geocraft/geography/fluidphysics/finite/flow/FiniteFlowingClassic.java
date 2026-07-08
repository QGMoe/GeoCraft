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

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadOnly;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.OldFlowChoice;
import top.qiguaiaaaa.geocraft.api.util.math.vec.MBlockPos;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidMixinUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import java.util.List;
import java.util.Set;

import static net.minecraft.block.BlockLiquid.LEVEL;

@ThreadOnly(ThreadType.MINECRAFT_SERVER)
@NotThreadSafe
public final class FiniteFlowingClassic {
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) MBlockPos facingPos$最外层$mut = new MBlockPos();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) MBlockPos downPos$mutable = new MBlockPos();
    private static final @ThreadOnly(ThreadType.MINECRAFT_SERVER) MBlockPos facingPos$迭代用$mut = new MBlockPos();
    public final @Nonnull BlockFluidClassic block;
    public final @Nonnull Material material;
    public final @Nonnull Fluid fluid;
    public final int quantaPerBlock;
    public final byte densityDir;

    public FiniteFlowingClassic(final @Nonnull BlockFluidClassic block) {
        this.block = block;
        this.material = block.getDefaultState().getMaterial();
        this.fluid = block.getFluid();
        this.quantaPerBlock = FluidMixinUtil.getQuantaPerBlock(block);
        this.densityDir = (byte) (block.getDensity() > 0 ? -1 : 1);
    }

    public int tickRate(final @Nonnull World world){
        return MiscUtil.modifyTickRateByGravity(world,block.tickRate(world));
    }

    public void flowDown(@Nonnull final World worldIn,
                         @Nonnull final BlockPos currentPos,
                         @Nonnull final BlockPos downPos,
                         @Nonnull final IBlockState thisState,
                         @Nonnull final IBlockState downState,
                         final int liquidQuanta){
        final int belowQuanta = FluidUtil.getFluidQuanta(worldIn,downPos,downState);
        final int totalQuanta = liquidQuanta+belowQuanta;
        if(totalQuanta<=quantaPerBlock){
            worldIn.setBlockToAir(currentPos);
            worldIn.setBlockState(downPos, downState.withProperty(LEVEL, quantaPerBlock - totalQuanta), Constants.BlockFlags.DEFAULT);
        }else{
            final int remain = totalQuanta-quantaPerBlock;
            worldIn.setBlockState(currentPos,thisState.withProperty(LEVEL, quantaPerBlock - remain),Constants.BlockFlags.DEFAULT);
            BlockUpdater.scheduleUpdate(worldIn,currentPos,block,this.tickRate(worldIn));
            worldIn.setBlockState(downPos,downState.withProperty(LEVEL,0), Constants.BlockFlags.DEFAULT);
        }
    }

    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void gatherFlowChoices(@Nonnull final World world,
                                  @Nonnull final BlockPos pos,
                                  @Nonnull final List<OldFlowChoice> averageFlowChoicesCollector,
                                  @Nullable final Set<EnumFacing> slopeFlowableDirectionsCollector,
                                  final int currentQuanta){
        for(final @Nonnull EnumFacing facing:EnumFacing.Plane.HORIZONTAL){
            facingPos$最外层$mut.setPos(pos.getX()+facing.getXOffset(),pos.getY(),pos.getZ()+facing.getZOffset());
            final @Nonnull IBlockState facingState = world.getBlockState(facingPos$最外层$mut);
            if(!this.canDisplaceEvenIsFluid(world,facingPos$最外层$mut)) continue;
            final int facingQuanta = FluidUtil.getFluidQuanta(world,facingPos$最外层$mut,facingState);
            if(facingQuanta<currentQuanta-1){
                averageFlowChoicesCollector.add(new OldFlowChoice(facingQuanta,facing));
            }
            if(slopeFlowableDirectionsCollector!=null && facingQuanta<currentQuanta)
                slopeFlowableDirectionsCollector.add(facing);
        }
    }

    /**
     * Q=1 坡度流动模式的可流动方向寻找算法
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    public void singleSlopeAlgorithm(@Nonnull final World worldIn,
                                     @Nonnull final BlockPos pos,
                                     @Nonnull final Set<EnumFacing> collectorOfPossibleDirections) {
        int difficulty = 1000;

        for (@Nonnull final EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            facingPos$最外层$mut.setPos(pos).offsetM(enumfacing,1);
            final @Nonnull IBlockState state = worldIn.getBlockState(facingPos$最外层$mut);
            if (!block.canDisplace(worldIn, facingPos$最外层$mut) || FluidUtil.isFluid(state)) {
                continue;
            }
            final int slope;
            if (canFlowDownTo(worldIn, downPos$mutable.setPos(facingPos$最外层$mut).offsetM(EnumFacing.UP,densityDir)) == FlowVerticalState.DENY) {
                slope = this.getSingleSlopeDistance(worldIn,
                        facingPos$最外层$mut.getX(),
                        pos.getY(),
                        facingPos$最外层$mut.getZ(), 1, enumfacing.getOpposite());
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

    /**
     * Q=1 坡度流动模式的可流动方向寻找内层递归算法
     */
    @ThreadOnly(ThreadType.MINECRAFT_SERVER)
    int getSingleSlopeDistance(@Nonnull final World worldIn,
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

            if (!block.canDisplace(worldIn, facingPos$迭代用$mut) || FluidUtil.isFluid(state)) {
                continue;
            }
            if (canFlowDownTo(worldIn, downPos$mutable.setPos((Vec3i) facingPos$迭代用$mut).offsetM(EnumFacing.UP,densityDir)) != FlowVerticalState.DENY) {
                return distance;
            }

            if (distance < quantaPerBlock >> 1) {
                final int newDistance = this.getSingleSlopeDistance(worldIn,
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

    /**
     * Q>1 坡度流动模式的可流动方向寻找算法
     * @param worldIn 所在世界
     * @param pos 位置
     * @param accessibleDirections 可流动的方向
     * @param thisQuanta 搜寻者的液体量
     * @param collectorOfPossibleDirections  一个流动方向的集合，意味着最佳的流动方向
     */
    public void multiSlopeAlgorithm(final @Nonnull World worldIn,
                                    final @Nonnull BlockPos pos,
                                    final @Nonnull Set<EnumFacing> accessibleDirections,
                                    final int thisQuanta,
                                    final @Nonnull Set<EnumFacing> collectorOfPossibleDirections) {
        double difficulty = 10000d;

        for (final @Nonnull EnumFacing enumfacing : accessibleDirections) {
            final double slope = this.getMultiSlopeDistance(worldIn,
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
            if (!this.canDisplaceEvenIsFluid(worldIn,facingPos$迭代用$mut) || (isFluid && quantaDiffer <1)) {
                continue;
            }
            if(isAir){
                if (canFlowDownTo(worldIn, downPos$mutable.setPos((Vec3i) facingPos$迭代用$mut).offsetM(EnumFacing.UP,densityDir)) != FlowVerticalState.DENY) {
                    return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,quantaPerBlock+thisQuanta);
                }else{
                    return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta);
                }
            }else if(quantaDiffer >1){ //同样的流体
                return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta-quantaDiffer);
            }else if(!isFluid){ //例如火把
                return FluidUtil.getFlowDifficulty(distance*quantaPerBlock,thisQuanta);
            }

            if (distance < (quantaPerBlock* FluidPhysicsConfig.slopeFindDistanceMultiplierForModFluidWhenQuantaAbove1.getValue())/2) {
                final double slope = this.getMultiSlopeDistance(worldIn,
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
     * 获得对应方块状态的流体量与自身流体量的差值
     * @param state 对应方块状态
     * @param thisQuanta 自身流体量
     * @return 如果不是一个流体，则返回INT整形最大值。如果是一个流体，则返回自身流体量减去对方流体量的结果。
     */
    int getQuantaDiffer(final @Nonnull IBlockState state,final int thisQuanta){
        if(!isEqualFluid(state)) return Integer.MIN_VALUE;
        int quanta = quantaPerBlock-state.getValue(LEVEL);
        if(quanta<0) quanta = 0;
        return thisQuanta - quanta;
    }

    public boolean tryTeleport(final @Nonnull World world,
                               final @Nonnull BlockPos to,
                               final @Nonnull BlockPos from,
                               final @Nonnull IBlockState fromState){
        if(!world.isBlockLoaded(to)) return false;
        final @Nonnull IBlockState toState = world.getBlockState(to);
        if(toState.getMaterial() == Material.AIR){
            int curQuanta = quantaPerBlock - fromState.getValue(BlockFluidClassic.LEVEL);
            final int movQuanta = from.getY()==to.getY()?curQuanta>>1:curQuanta;
            if(movQuanta <= 0) return false;
            curQuanta -= movQuanta;
            if(curQuanta <= 0) world.setBlockToAir(from);
            else world.setBlockState(from,fromState.withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-curQuanta));
            world.setBlockState(to,fromState.withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-movQuanta));
            return curQuanta <= 0;
        }else if(isEqualFluid(toState)){
            int toQuanta = quantaPerBlock-toState.getValue(BlockFluidClassic.LEVEL);
            int myQuanta = quantaPerBlock -fromState.getValue(BlockFluidClassic.LEVEL);
            if(to.getY() == from.getY() && toQuanta>=myQuanta-1) return false;
            final int movQuanta = from.getY()==to.getY()?(myQuanta-toQuanta)>>1:Math.min(quantaPerBlock-toQuanta,myQuanta);
            myQuanta -=movQuanta;
            if(myQuanta <= 0) world.setBlockToAir(from);
            else world.setBlockState(from,fromState.withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-myQuanta));
            toQuanta += movQuanta;
            world.setBlockState(to,toState.withProperty(BlockFluidClassic.LEVEL,quantaPerBlock-toQuanta));
            return myQuanta <=0;
        }
        return false;
    }

    public void flowIntoBlockDirectly(final @Nonnull World world,
                                      final @Nonnull BlockPos pos,
                                      final @Nonnull IBlockState rawState,
                                      final int meta) {
        if (meta < 0) return;
        FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,pos,world.getBlockState(pos),fluid);
        world.setBlockState(pos, rawState.withProperty(LEVEL, meta));
    }

    @Nonnull
    public FlowVerticalState canFlowDownTo(final @Nonnull World worldIn, final @Nonnull BlockPos pos){
        final @Nonnull IBlockState state = worldIn.getBlockState(pos);
        if(FluidUtil.isFluid(state)){
            if(!isEqualFluid(state)){
                return this.block.canDisplace(worldIn,pos)?FlowVerticalState.SWAP:FlowVerticalState.DENY;
            }
            return FluidUtil.isFullFluid(worldIn,pos,state)?FlowVerticalState.DENY:FlowVerticalState.EQUAL_FLUID;
        }else return this.block.canDisplace(worldIn,pos)?FlowVerticalState.DISPLACE:FlowVerticalState.DENY;
    }

    public boolean canDisplaceEvenIsFluid(final @Nonnull World world,final @Nonnull BlockPos pos){
        final @Nonnull IBlockState state = world.getBlockState(pos);
        if(isEqualFluid(state)) return true;
        return this.block.canDisplace(world,pos);
    }

    public boolean isEqualFluid(final @Nonnull IBlockState state){
        return this.block == state.getBlock();
    }

    public enum FlowVerticalState{
        DENY,
        EQUAL_FLUID,
        SWAP,
        DISPLACE
    }
}
