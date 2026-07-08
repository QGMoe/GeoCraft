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

package top.qiguaiaaaa.geocraft.mixin.finite.block;

import net.minecraft.block.Block;
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
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.block.finite.IBlockLiquidFinite;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.block.finite.ILayeredFluidHostFiniteLiquid;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.pressure.FinitePressureTasks;
import top.qiguaiaaaa.geocraft.handler.ServerStatusMonitor;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@Mixin(value = BlockStaticLiquid.class)
public class BlockStaticLiquidMixin extends BlockLiquid implements IBlockLiquidFinite, ILayeredFluidHostFiniteLiquid {
//    @Unique
//    private static final boolean 天圆地方$debug = false;
    @Unique
    private FiniteFlowingVanilla 天圆地方$FINITE$flowingHandler;
    @Unique
    private final ThreadLocal<Boolean> 天圆地方$curRandomTick = ThreadLocal.withInitial(()->Boolean.FALSE);

    protected BlockStaticLiquidMixin(final @Nonnull Material materialIn) {
        super(materialIn);
    }

    @Override
    @Unique
    public void randomTick(@Nonnull final World worldIn,
                           @Nonnull final BlockPos pos,
                           @Nonnull final IBlockState state,
                           @Nonnull final Random random) {
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
        if(!GeoFluidSetting.isFluidToBePhysical(天圆地方$FINITE$flowingHandler.fluid)) return;
        if(!天圆地方$isValidState(worldIn,pos,state)) return;
        if(!天圆地方$FINITE$getFlowingHandler().canFlow(worldIn, pos, state)){
            // *******
            // Pressure Flow
            // *******
            final @Nullable IBlockState nowState = 天圆地方$FINITE$flowingHandler.tryPressureFlow(worldIn,pos,state,ServerStatusMonitor.getRecommendedBlockFlags());
            if(nowState != null){
                if(nowState!=state && 天圆地方$FINITE$getFlowingHandler().isEqualFluid(nowState)){
                    天圆地方$sendPressureQuery(worldIn,pos,nowState,rand,true);
                }else if(nowState == state){
                    天圆地方$sendPressureQuery(worldIn,pos,state,rand,false);
                }
                if(nowState!=state) return;
            }
            // ********
            // Evaporation & Freezing
            // ********
            final @Nullable IBlockState newState = EventFactory.afterBlockLiquidStaticUpdate(天圆地方$FINITE$flowingHandler.fluid,worldIn,pos,state, 天圆地方$curRandomTick.get());
            if(newState != null){
                worldIn.setBlockState(pos,newState);
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

    @Unique
    protected boolean 天圆地方$isValidState(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state){
        if(state.getValue(LEVEL) >= 8){
            world.setBlockState(pos,Blocks.AIR.getDefaultState(), Constants.BlockFlags.SEND_TO_CLIENTS|Constants.BlockFlags.NO_OBSERVERS);
            return false;
        }
        return true;
    }

    @Unique
    protected void 天圆地方$sendPressureQuery(final @Nonnull World world,
                                              final @Nonnull BlockPos pos,
                                              final @Nonnull IBlockState state,
                                              final @Nonnull Random rand,
                                              final boolean directly){
        if(FluidPressureSearchManager.isTaskRunning(world,pos)){
//            if(天圆地方$debug) GeoCraft.getLogger().info("{}: task running, returned",pos);
            return;
        }
        final @Nonnull IBlockState up = world.getBlockState(pos.up());
        if(天圆地方$FINITE$getFlowingHandler().isEqualFluid(up) && up.getValue(LEVEL)==0){
//            if(天圆地方$debug) GeoCraft.getLogger().info("{}: up is full water, returned",pos);
            return;
        }
        if(directly || BaseUtil.getRandomResult(rand,FluidPhysicsConfig.POSSIBILITY_FOR_STATIC_VANILLA_LIQUID_TO_CREATE_PRESSURE_TASK.getValue())) {
//            if(天圆地方$debug){
//                FluidPressureSearchManager.addTask(world,RealityPressureTaskBuilder.createVanillaTask_Debug(天圆地方$FINITE$flowingHandler.fluid,state,pos,BaseUtil.getRandomPressureSearchRange()));
//                return;
//            }
            FluidPressureSearchManager.addTask(world,
                    FinitePressureTasks.createVanillaTask(天圆地方$FINITE$flowingHandler.fluid,state,pos, BaseUtil.getRandomPressureSearchRange())
            );
        }
    }

    @Shadow
    private void updateLiquid(World worldIn, BlockPos pos, IBlockState state) {}

    //*********
    // 透水方块
    //*********

    @Nonnull
    @Override
    @Unique
    public Fluid 天圆地方$getFluid() {
        return 天圆地方$FINITE$flowingHandler.fluid;
    }

    // ********************
    // IBlockLiquidFinite
    // ********************

    @Nonnull
    @Override
    public FiniteFlowingVanilla 天圆地方$FINITE$getFlowingHandler() {
        return 天圆地方$FINITE$flowingHandler;
    }

    @Override
    public void 天圆地方$FINITE$init() {
        this.天圆地方$FINITE$flowingHandler = FiniteFlowingVanilla.getFlowingByMaterial(this.material);
    }
}
