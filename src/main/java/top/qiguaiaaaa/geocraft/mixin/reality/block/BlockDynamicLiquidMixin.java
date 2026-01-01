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

package top.qiguaiaaaa.geocraft.mixin.reality.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;
import top.qiguaiaaaa.geocraft.block.reality.ILayeredFluidHostFiniteLiquid;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidUpdateManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.RealityBlockLiquidUpdater;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.update.RealityBlockDynamicLiquidUpdateTask;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.mixinapi.FluidSettable;
import top.qiguaiaaaa.geocraft.util.mixinapi.IVanillaFlowChecker;

import javax.annotation.Nonnull;
import java.util.*;

@Mixin(value = BlockDynamicLiquid.class)
public class BlockDynamicLiquidMixin extends BlockLiquid implements FluidSettable, IVanillaFlowChecker, ILayeredFluidHostFiniteLiquid {
    @Unique
    private static final ThreadLocal<List<FlowChoice>> 天圆地方$averageFlowChoices = ThreadLocal.withInitial(ArrayList::new);
    @Unique
    private Fluid 天圆地方$thisFluid;

    @Unique
    private RealityBlockLiquidUpdater 天圆地方$updater;

    protected BlockDynamicLiquidMixin(Material materialIn) {
        super(materialIn);
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

    @Inject(method = "updateTick",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        ci.cancel();
        if (!worldIn.isBlockLoaded(pos)){
            return;
        }
        if(!GeoFluidSetting.hasGravity(worldIn)){
            //变成静态方块
            worldIn.setBlockState(pos, getStaticBlock(this.material).getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), Constants.BlockFlags.SEND_TO_CLIENTS);
            return;
        }
        FluidUpdateManager.addTask(worldIn,new RealityBlockDynamicLiquidUpdateTask(天圆地方$thisFluid,pos, 天圆地方$updater));
    }

    @Inject(method = "onBlockAdded",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull CallbackInfo ci) {
        ci.cancel();
        if (!this.checkForMixing(worldIn, pos, state)) {
            MiscUtil.scheduleFluidBlockUpdate(worldIn,pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    @Unique
    public boolean 天圆地方$canFlow(World worldIn, BlockPos pos, IBlockState state, Random rand){
        if (!worldIn.isAreaLoaded(pos, 天圆地方$updater.getSlopeFindDistance(worldIn))){
            return false;
        }

        int liquidMeta = state.getValue(LEVEL);
        if(liquidMeta >= 8){
            return false;
        }
        int liquidQuanta = 8-liquidMeta;

        IBlockState stateBelow = worldIn.getBlockState(pos.down());
        boolean canMoveDown = 天圆地方$updater.canMoveDownTo(stateBelow);
        if(canMoveDown) return true;

        //坡度流动模式
        if(liquidMeta == 7){
            if(!FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaIs1.getValue() && FluidUtil.getFluid(stateBelow) == 天圆地方$thisFluid) return false;
            Set<EnumFacing> directions = 天圆地方$updater.getPossibleFlowDirections(worldIn, pos);
            return !directions.isEmpty();
        }
        //可流动方向检查
        final List<FlowChoice> averageModeFlowDirections = 天圆地方$averageFlowChoices.get();//平均流动模式可用方向
        averageModeFlowDirections.clear();
        Set<EnumFacing> slopeModeFlowDirections = FluidPhysicsConfig.slopeModeForVanillaWhenOnLiquidsAndQuantaAbove1.getValue()?
                EnumSet.noneOf(EnumFacing.class):null;//非Q=1坡度模式可用方向
        天圆地方$updater.checkNeighborsToFindFlowChoices(worldIn,pos,state,liquidQuanta,averageModeFlowDirections,slopeModeFlowDirections);

        if(!averageModeFlowDirections.isEmpty()){ //平均流动模式
            averageModeFlowDirections.clear();
            return true;
        }else if(slopeModeFlowDirections != null && !slopeModeFlowDirections.isEmpty()){ //Q>1坡度模式
            if(!worldIn.isAreaLoaded(pos, 天圆地方$updater.getSlopeFindDistance2(worldIn))) return false;
            slopeModeFlowDirections = 天圆地方$updater.getPossibleFlowDirections(worldIn,pos,slopeModeFlowDirections,liquidQuanta);
            return !slopeModeFlowDirections.isEmpty();
        }
        return false;
    }

    @Inject(method = "getPossibleFlowDirections",at = @At("HEAD"),cancellable = true)
    private void 天圆地方$getPossibleFlowDirections(World worldIn, BlockPos pos, CallbackInfoReturnable<Set<EnumFacing>> cir) {
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        cir.cancel();
        cir.setReturnValue(天圆地方$updater.getPossibleFlowDirections(worldIn,pos));
    }

    @Inject(method = "getSlopeDistance",at = @At("HEAD"),cancellable = true)
    private void 天圆地方$getSlopeDistance(World worldIn, BlockPos pos, int distance, EnumFacing from, CallbackInfoReturnable<Integer> cir) {
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        cir.cancel();
        cir.setReturnValue(天圆地方$updater.getSlopeDistance(worldIn,pos,distance,from));
    }

    //********
    // FluidSettable
    //********

    @Override
    @Unique
    public void 天圆地方$setCorrespondingFluid(Fluid fluid) {
        if(天圆地方$thisFluid != null)return;
        this.天圆地方$thisFluid = fluid;
        this.天圆地方$updater = new RealityBlockLiquidUpdater((BlockDynamicLiquid) (Block)this,material,fluid);
    }

    //*********
    // 透水方块
    //*********

    @Nonnull
    @Override
    @Unique
    public Fluid 天圆地方$getFluid() {
        return 天圆地方$thisFluid;
    }
}
