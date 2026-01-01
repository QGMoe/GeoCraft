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

package top.qiguaiaaaa.geocraft.mixin.vanilla_like;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidUpdateManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla_like.mixin.IVanillaLikeFluidBlock;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla_like.update.VanillaLikeBlockDynamicLiquidUpdateTask;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidSearchUtil;
import top.qiguaiaaaa.geocraft.util.mixinapi.FluidSettable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.*;

@Mixin(value = BlockDynamicLiquid.class)
public class BlockDynamicLiquidMixin extends BlockLiquid implements FluidSettable, IVanillaLikeFluidBlock {
    @Shadow
    int adjacentSourceBlocks;
    @Unique
    private Fluid 天圆地方$thisFluid;

    protected BlockDynamicLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    /**
     * @author QiguaiAAAA
     */
    @Inject(method = "updateTick",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        ci.cancel();
        if(worldIn.isRemote) return;
        if(!GeoFluidSetting.hasGravity(worldIn)){
            //变成静态方块
            worldIn.setBlockState(pos, getStaticBlock(this.material).getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), Constants.BlockFlags.SEND_TO_CLIENTS);
            return;
        }
        FluidUpdateManager.addTask(worldIn,new VanillaLikeBlockDynamicLiquidUpdateTask(天圆地方$thisFluid,pos,(BlockDynamicLiquid) (Block)this));
    }

    @Inject(method = "onBlockAdded",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull CallbackInfo ci) {
        ci.cancel();
        if (!this.checkForMixing(worldIn, pos, state)) {
            MiscUtil.scheduleFluidBlockUpdate(worldIn,pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public void neighborChanged(@Nonnull final IBlockState state,
                                @Nonnull final World worldIn,
                                @Nonnull final BlockPos pos,
                                @Nonnull final Block blockIn,
                                @Nonnull final BlockPos fromPos) {
        if(!FluidPhysicsConfig.ALLOW_DYNAMIC_LIQUID_NEIGHBOR_UPDATE.getValue()){
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
            return;
        }
        MiscUtil.scheduleFluidBlockUpdate(worldIn,pos, this, this.tickRate(worldIn));
    }

    @Override
    @Unique
    public void 天圆地方$onFlowingTask(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand){
        if (!world.isAreaLoaded(pos, this.getSlopeFindDistance(world))) return;
        int liquidMeta = state.getValue(LEVEL);
        int spreadLevel = 天圆地方$getSpreadLevel(world);

        int updateRate = MiscUtil.modifyTickRateByGravity(world,this.tickRate(world));

        if(updateRate <= 0){//无重力
            placeStaticBlock(world,pos,state);
            return;
        }

        final BlockPos downPos = pos.down();
        IBlockState stateBelow = world.getBlockState(downPos);

        //是否能够往下流
        Optional<BlockPos> sourcePosOption = Optional.empty();
        if(liquidMeta == 0) sourcePosOption = Optional.of(pos);
        boolean canMoveSourceDown = this.天圆地方$canMoveInto(world, downPos, stateBelow);
        if(canMoveSourceDown){
            if (!sourcePosOption.isPresent())
                sourcePosOption = FluidSearchUtil.findSource(world,pos,material,false,false,
                        findSourceMaxIterationsWhenVerticalFlowing.getValue(),
                        findSourceMaxSameLevelIterationsWhenVerticalFlowing.getValue());
            if(sourcePosOption.isPresent()){
                FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),downPos);
                if(sourcePosOption.get() == pos) return;
            }
        }else if(liquidMeta == 1){
            sourcePosOption = FluidSearchUtil.findSource(world,pos,material,true,false,
                    findSourceMaxIterationsWhenHorizontalFlowing.getValue(),
                    findSourceMaxSameLevelIterationsWhenHorizontalFlowing.getValue());
            if(sourcePosOption.isPresent()){
                FluidOperationUtil.moveFluidSource(world,sourcePosOption.get(),pos);
                BlockUpdater.scheduleUpdate(world,pos,BlockLiquid.getStaticBlock(material),updateRate);
                return;
            }
        }

        boolean noSourceFound = canMoveSourceDown && !sourcePosOption.isPresent();

        if(noSourceFound){
            spreadLevel+=8;
        }

        if (liquidMeta > 0) {
            //水平方向处理
            int 相邻方块最高等级 = -100;
            this.adjacentSourceBlocks = 0;

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
                相邻方块最高等级 = this.checkAdjacentBlock(world, pos.offset(enumfacing), 相邻方块最高等级);

            int newLiquidMeta = 相邻方块最高等级 + spreadLevel;

            if (newLiquidMeta >= 8 || 相邻方块最高等级 < 0) newLiquidMeta = -1;
            //垂直方向处理
            int upBlockMeta = this.getDepth(world.getBlockState(pos.up()));

            if (upBlockMeta >= 0) {
                if (upBlockMeta >= 8) newLiquidMeta = upBlockMeta;
                else newLiquidMeta = upBlockMeta + 8;
            }
            //无限水
            if(enableInfiniteWater.getValue()){
                if (this.adjacentSourceBlocks >= 2 && ForgeEventFactory.canCreateFluidSource(world, pos, state, material == Material.WATER)) {

                    if (stateBelow.getMaterial().isSolid()) {
                        newLiquidMeta = 0;
                    } else if (stateBelow.getMaterial() == material && stateBelow.getValue(LEVEL) == 0) {
                        newLiquidMeta = 0;
                    }
                }
            }
            boolean isQuantaDecreasing = newLiquidMeta < 8 && newLiquidMeta > liquidMeta;
            //岩浆处理
            if (!noSourceFound && (material == Material.LAVA) && isQuantaDecreasing && rand.nextInt(4) != 0){
                updateRate *= 4;
            }

            //更新纹理（流动还是静止）
            if (newLiquidMeta == liquidMeta) {
                this.placeStaticBlock(world, pos, state);
            } else {
                liquidMeta = newLiquidMeta;
                if (newLiquidMeta < 0) world.setBlockToAir(pos);
                else {
                    state = state.withProperty(LEVEL, newLiquidMeta);
                    world.setBlockState(pos, state, 2);
                    BlockUpdater.scheduleUpdate(world,pos, this, updateRate);
                    world.notifyNeighborsOfStateChange(pos, this, false);
                }
            }
        } else {
            this.placeStaticBlock(world, pos, state);
        }
        if(liquidMeta <0) return;
        stateBelow = world.getBlockState(downPos);
        if (canFlowInto(world, downPos, stateBelow)) {
            if (material == Material.LAVA && stateBelow.getMaterial() == Material.WATER) {
                world.setBlockState(downPos, ForgeEventFactory.fireFluidPlaceBlockEvent(world, downPos, pos, Blocks.STONE.getDefaultState()));
                FluidOperationUtil.triggerFluidMixEffects(world, downPos);
                return;
            }
            if (liquidMeta >= 8) this.tryFlowInto(world, downPos, stateBelow, liquidMeta);
            else this.tryFlowInto(world, downPos, stateBelow, liquidMeta + 8);
        } else if (FluidUtil.isFullFluid(world,downPos,stateBelow) || this.isBlocked(world, downPos, stateBelow)){//横向流动
            Set<EnumFacing> directions = this.getPossibleFlowDirections(world, pos);
            int nextLiquidState = liquidMeta + spreadLevel;

            if (liquidMeta >= 8) nextLiquidState = 1;
            if (nextLiquidState >= 8) return;

            for (EnumFacing facing : directions)
                this.tryFlowInto(world, pos.offset(facing), world.getBlockState(pos.offset(facing)), nextLiquidState);
        }
    }

    @Unique
    private boolean 天圆地方$canMoveInto(World worldIn, BlockPos pos, IBlockState state){
        if(state.getBlock() instanceof IFluidBlock) return false;
        final Material material = state.getMaterial();
        if(material.isLiquid()){
            if(!(state.getBlock() instanceof BlockLiquid)) return false;
            if(material != this.material) return false;
            return state.getValue(LEVEL) != 0;
        }
        return !this.isBlocked(worldIn,pos,state);
    }

    @Unique
    private int 天圆地方$getSpreadLevel(World world){
        if (material == Material.LAVA && !world.provider.doesWaterVaporize()) {
            return 2;
        }
        return 1;
    }

    @Shadow
    private void tryFlowInto(World worldIn, BlockPos pos, IBlockState state, int level) {}
    @Shadow
    private boolean canFlowInto(World worldIn, BlockPos pos, IBlockState state) {return false;}
    @Shadow
    private int getSlopeFindDistance(World worldIn) {
        return 0;
    }
    @Shadow
    protected int checkAdjacentBlock(World worldIn, BlockPos pos, int currentMinLevel){return 0;}
    @Shadow
    private void placeStaticBlock(World worldIn, BlockPos pos, IBlockState currentState){}
    @Shadow
    private Set<EnumFacing> getPossibleFlowDirections(World worldIn, BlockPos pos) {return Collections.emptySet();}
    @Shadow
    private boolean isBlocked(World worldIn, BlockPos pos, IBlockState state){return false;}

    @Override
    public void 天圆地方$setCorrespondingFluid(Fluid fluid) {
        if(天圆地方$thisFluid != null)return;
        天圆地方$thisFluid = fluid;
    }

}
