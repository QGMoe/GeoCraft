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
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.block.reality.ILayeredFluidHostFiniteLiquid;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.pressure.RealityPressureTaskBuilder;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.task.pressure.IFluidPressureSearchTaskResult;
import top.qiguaiaaaa.geocraft.handler.ServerStatusMonitor;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.BlockLiquidUpdater;
import top.qiguaiaaaa.geocraft.util.MiscUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.util.mixinapi.FluidSettable;
import top.qiguaiaaaa.geocraft.util.mixinapi.IVanillaFlowChecker;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(value = BlockStaticLiquid.class)
public class BlockStaticLiquidMixin extends BlockLiquid implements IVanillaFlowChecker, FluidSettable, ILayeredFluidHostFiniteLiquid {
    @Unique
    private static final boolean 天圆地方$debug = false;
    @Unique
    private Fluid 天圆地方$thisFluid;
    @Unique
    private final ThreadLocal<Boolean> 天圆地方$curRandomTick = ThreadLocal.withInitial(()->Boolean.FALSE);

    protected BlockStaticLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    @Unique
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        天圆地方$curRandomTick.set(Boolean.TRUE);
        super.randomTick(worldIn, pos, state, random);
        天圆地方$curRandomTick.set(Boolean.FALSE);
    }

    @Inject(method = "neighborChanged",at =@At("HEAD"),cancellable = true)
    private void 天圆地方$beforeNeighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci){
        if(ServerStatusMonitor.isServerCloselyLagging()) ci.cancel();
    }

    @Inject(method = "<init>",at = @At("RETURN"))
    private void 天圆地方$onInit(Material materialIn, CallbackInfo ci) {
        this.setTickRandomly(true);
    }
    @Inject(method = "updateTick",at = @At("TAIL"))
    public void 天圆地方$updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if(worldIn.isRemote) return;
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$thisFluid)) return;
        if(!天圆地方$isValidState(worldIn,pos,state)) return;
        if(!天圆地方$canFlow(worldIn,pos,state,rand)){
            if(FluidPhysicsConfig.PRESSURE_SYSTEM_FOR_REALITY.getValue()){
                final IFluidPressureSearchTaskResult res = FluidPressureSearchManager.getTaskResult(worldIn,pos);

                if(res == null || res.isEmpty()){
                    天圆地方$sendPressureQuery(worldIn,pos,state,rand,false);
                    if(天圆地方$debug) GeoCraft.getLogger().info("{}: no res,send query",pos);
                }else {
                    IBlockState nowState =state;
                    if(天圆地方$debug) GeoCraft.getLogger().info("{}: has res :",pos);
                    while (res.hasNext()) {
                        final BlockPos toPos = res.next();
                        if(!nowState.getMaterial().isLiquid()) break;
                        if(天圆地方$tryMoveInto(worldIn,toPos,pos,nowState)) break;
                        nowState = worldIn.getBlockState(pos);
                        if(天圆地方$debug) GeoCraft.getLogger().info("{} now State: {}",toPos,nowState);
                    }

                    nowState = worldIn.getBlockState(pos);
                    if(nowState!=state && FluidUtil.getFluid(nowState) == 天圆地方$thisFluid){
                        天圆地方$sendPressureQuery(worldIn,pos,nowState,rand,true);
                    }else if(nowState == state){
                        天圆地方$sendPressureQuery(worldIn,pos,state,rand,false);
                    }
                    if(nowState!=state) return;
                }
            }
            final IBlockState newState = EventFactory.afterBlockLiquidStaticUpdate(天圆地方$thisFluid,worldIn,pos,state, 天圆地方$curRandomTick.get());
            if(newState != null){
                worldIn.setBlockState(pos,newState);
                return;
            }
            return;
        }
        updateLiquid(worldIn,pos,state);
    }

    /**
     * 保证流体流动受重力影响，且使用 BlockUpdater
     */
    @Redirect(method = "updateLiquid",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/world/World;scheduleUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void 天圆地方$scheduleLiquidUpdate(@Nonnull final World instance,final BlockPos pos,final Block blockIn,final int delay){
        MiscUtil.scheduleFluidBlockUpdate(instance, pos, blockIn, delay);
    }

    @Override
    @Unique
    public boolean 天圆地方$canFlow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        BlockDynamicLiquid blockdynamicliquid = BlockLiquid.getFlowingBlock(this.material);
        IVanillaFlowChecker checker = (IVanillaFlowChecker) blockdynamicliquid;
        return checker.天圆地方$canFlow(worldIn,pos,state,rand);
    }

    @Unique
    protected boolean 天圆地方$isValidState(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state){
        if(state.getValue(LEVEL) >= 8){
            world.setBlockState(pos,Blocks.AIR.getDefaultState(), Constants.BlockFlags.SEND_TO_CLIENTS|Constants.BlockFlags.NO_OBSERVERS);
            return false;
        }
        return true;
    }

    @Unique
    protected void 天圆地方$sendPressureQuery(World world, BlockPos pos, IBlockState state, Random rand, boolean directly){
        if(FluidPressureSearchManager.isTaskRunning(world,pos)){
            if(天圆地方$debug) GeoCraft.getLogger().info("{}: task running, returned",pos);
            return;
        }
        IBlockState up = world.getBlockState(pos.up());
        if(FluidUtil.getFluid(up)== 天圆地方$thisFluid && up.getValue(LEVEL)==0){
            if(天圆地方$debug) GeoCraft.getLogger().info("{}: up is full water, returned",pos);
            return;
        }
        if(directly || BaseUtil.getRandomResult(rand,FluidPhysicsConfig.POSSIBILITY_FOR_STATIC_VANILLA_LIQUID_TO_CREATE_PRESSURE_TASK.getValue())) {
            if(天圆地方$debug){
                FluidPressureSearchManager.addTask(world,RealityPressureTaskBuilder.createVanillaTask_Debug(天圆地方$thisFluid,state,pos,BaseUtil.getRandomPressureSearchRange()));
                return;
            }
            FluidPressureSearchManager.addTask(world,
                    RealityPressureTaskBuilder.createVanillaTask(天圆地方$thisFluid,state,pos, BaseUtil.getRandomPressureSearchRange())
            );
        }
    }

    @Unique
    protected boolean 天圆地方$tryMoveInto(World world, BlockPos toPos, BlockPos srcPos, IBlockState myState){
        if(!world.isBlockLoaded(toPos)) return false;
        IBlockState toState = world.getBlockState(toPos);
        final int updateFlag = ServerStatusMonitor.getRecommendedBlockFlags();
        if(FluidUtil.getFluid(toState) == 天圆地方$thisFluid){
            int toQuanta = 8-toState.getValue(LEVEL);
            int myQuanta = 8 -myState.getValue(LEVEL);
            if(toPos.getY() == srcPos.getY() && toQuanta>=myQuanta-1) return false;
            int movQuanta = srcPos.getY()==toPos.getY()?(myQuanta-toQuanta)/2:Math.min(8-toQuanta,myQuanta);
            myQuanta -=movQuanta;
            if(myQuanta <= 0){
                world.setBlockState(srcPos, Blocks.AIR.getDefaultState(),updateFlag);
            }else world.setBlockState(srcPos,this.getDefaultState().withProperty(LEVEL,8-myQuanta),updateFlag);
            toQuanta += movQuanta;
            world.setBlockState(toPos,getFlowingBlock(material).getDefaultState().withProperty(LEVEL,8-toQuanta),updateFlag);
            return myQuanta==0;
        }
        if(!BlockLiquidUpdater.isBlocked(toState)) {
            int quanta = 8 - myState.getValue(LEVEL);
            int movQuanta = srcPos.getY() == toPos.getY() ? quanta / 2 : quanta;
            if (movQuanta <= 0) return false;
            quanta -= movQuanta;
            if (quanta <= 0) {
                world.setBlockState(srcPos, Blocks.AIR.getDefaultState(), updateFlag);
            } else world.setBlockState(srcPos, this.getDefaultState().withProperty(LEVEL, 8 - quanta), updateFlag);
            FluidOperationUtil.triggerDestroyBlockEffectByFluid(world,toPos,toState, 天圆地方$thisFluid);
            world.setBlockState(toPos, getFlowingBlock(material).getDefaultState().withProperty(LEVEL, 8 - movQuanta), updateFlag);
            return quanta == 0;
        }
        return false;
    }
    @Shadow
    private void updateLiquid(World worldIn, BlockPos pos, IBlockState state) {}

    @Override
    @Unique
    public void 天圆地方$setCorrespondingFluid(Fluid fluid) {
        if(天圆地方$thisFluid == null){
            天圆地方$thisFluid = fluid;
        }
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
