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

package top.qiguaiaaaa.geocraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.block.IBlockStateLayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.fluid.FluidHostOperation;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidFrom;
import top.qiguaiaaaa.geocraft.api.fluid.IFluidTo;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.util.*;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.api.util.math.FlowChoice;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.geography.soil.BlockSoilType;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.ChunkUtil;
import top.qiguaiaaaa.geocraft.util.WaterUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static top.qiguaiaaaa.geocraft.api.block.BlockProperties.HUMIDITY;
import static top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode.getCurrentMode;
import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.FLUID_PHYSICS_MODE;

public interface IBlockSoil extends IBlockStateLayeredFluidHost {
    @Nonnull IFluidFrom FROM_SOIL = new IFluidFrom() {};
    @Nonnull IFluidTo TO_SOIL = new IFluidTo() {};
    ThreadLocal<List<FlowChoice>> averageModeFlowChoices = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 土壤将自身水掉下去的能力
     * @return 湿度变化
     */
    default int dropWaterDown(final @Nonnull World worldIn,
                              final @Nonnull BlockPos pos,
                              final @Nonnull IBlockState state){
        final @Nonnull BlockPos down = pos.down();
        final @Nonnull IBlockState downState = worldIn.getBlockState(down);
        if(downState.getMaterial() == Material.AIR){
            if(getCurrentMode() == FluidPhysicsMode.MORE_REALITY)
                FiniteFlowingVanilla.WATER_FLOW.placeDynamicBlock(worldIn,pos,7);
            return -1;
        }else if(LayeredFluidHostUtil.isLayeredFluidHost(downState)){
            final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost) downState.getBlock();
            final boolean canFill = host.canFill(worldIn,down,downState,EnumFacing.UP,FluidRegistry.WATER,FROM_SOIL,false);
            if(!canFill) return 0;
            final long filled = host.addAmountInQB(worldIn,down,downState,EnumFacing.UP,FluidRegistry.WATER,
                    QBUtil.QUANTA_VOLUME, FluidHostOperation.DO_WITH_CERTAINTY,null,FROM_SOIL,BlockFlagsModifier.MODIFY_NOTHING);
            return filled>0?-1:0;
        }else if(FiniteFlowingVanilla.WATER_FLOW.canFlowDownTo(downState)){
            if(getCurrentMode() == FluidPhysicsMode.MORE_REALITY) {
                FluidOperationUtil.triggerDestroyBlockEffectByFluid(worldIn,down,downState,FluidRegistry.WATER);
                FiniteFlowingVanilla.WATER_FLOW.placeDynamicBlock(worldIn,pos,7);
            }
            return -1;
        }else if(downState.getBlock() == Blocks.CAULDRON && getCurrentMode() == FluidPhysicsMode.VANILLA){
            if(!BaseUtil.getRandomResult(worldIn.rand,0.3)) return 0;
            if(downState.getValue(BlockCauldron.LEVEL) <3){
                worldIn.setBlockState(pos, downState.cycleProperty(BlockCauldron.LEVEL), BlockFlags.SEND_TO_CLIENTS);
                return -1;
            }
        }
        return 0;
    }

    /**
     * 土壤水向四周流动的能力
     * @param humidity 当前湿度
     */
    default void flowWaterHorizontally(final @Nonnull World worldIn,
                                       final @Nonnull BlockPos pos,
                                       final @Nonnull IBlockState state,
                                       final int humidity){
        if (!worldIn.isAreaLoaded(pos, 1)) return;
        //可流动方向检查
        final @Nonnull List<FlowChoice> averageModeFlowDirections = averageModeFlowChoices.get();
        averageModeFlowDirections.clear();

        for(final @Nonnull EnumFacing facing:EnumFacing.Plane.HORIZONTAL){
            final @Nonnull BlockPos facingPos = pos.offset(facing);
            final @Nonnull IBlockState facingState = worldIn.getBlockState(facingPos);
            if(!canFlowInto(worldIn,facingPos,facingState,facing)) continue;
            if(facingState.getMaterial() == Material.AIR){
                averageModeFlowDirections.add(new FlowChoice(facing));
                continue;
            }
            final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost)facingState.getBlock();
            final int facingHeight = host.getHeight(worldIn,facingPos,facingState,facing,FluidRegistry.WATER);
            final int facingHeightPerLayer = host.getHeightPerLayer(worldIn,facingPos,facingState,facing);
            if(facingHeight+facingHeightPerLayer<=(humidity-1)*getHeightPerLayer(state,null)){
                averageModeFlowDirections.add(new FlowChoice(worldIn,facingPos,facingState,host,facing,FluidRegistry.WATER));
            }
        }

        final int newHumidity = LayeredFluidHostUtil.averageFlow(humidity,getHeightPerLayer(state,null),
                this.getAmountInQBPerLayer(state,null,FluidRegistry.WATER),
                getMaxStableHumidity(state),
                averageModeFlowDirections);

        if(newHumidity != humidity){
            long left = 0;
            for(@Nonnull FlowChoice choice:averageModeFlowDirections){ //向四周流动
                if(choice.getAddedLayers() == 0){
                    left += choice.getAddedAmountInQB();
                    continue;
                }
                BlockPos facingPos = pos.offset(choice.direction);
                if(choice.isAir()){
                    if(FLUID_PHYSICS_MODE.getValue() != FluidPhysicsMode.MORE_REALITY) continue;
                    FiniteFlowingVanilla.WATER_FLOW.placeDynamicBlock(worldIn,facingPos,8-choice.getNewLayers());
                    continue;
                }
                IBlockState facingState = worldIn.getBlockState(facingPos);
                left += choice.apply(worldIn,facingPos,facingState,FluidRegistry.WATER);
            }
            setLayer(worldIn,pos,state,null,FluidRegistry.WATER,newHumidity+QBUtil.toQuanta(left),null,BlockFlagsModifier.MODIFY_NOTHING);
        }

        averageModeFlowDirections.clear();
    }

    /**
     * 土壤吸收上层水的能力
     * @return 湿度变化
     */
    default int drainUpWater(final @Nonnull World worldIn,
                             final @Nonnull BlockPos pos,
                             final @Nonnull IBlockState state){
        final @Nonnull BlockPos upPos = pos.up();
        final @Nonnull IBlockState upState = worldIn.getBlockState(upPos);
        if(upState.getBlock() instanceof ILayeredFluidHost){
            final @Nonnull ILayeredFluidHost block = (ILayeredFluidHost) upState.getBlock();
            if(!block.canDrain(worldIn,upPos,upState,EnumFacing.DOWN,FluidRegistry.WATER,TO_SOIL,false)) return 0;
            final int drained = block.drainLayer(worldIn,upPos,upState,EnumFacing.DOWN,FluidRegistry.WATER,1,
                    FluidHostOperation.SIM_WITH_CERTAINTY,TO_SOIL,BlockFlagsModifier.MODIFY_NOTHING);
            if(drained < 1) return 0;
            return block.drainLayer(worldIn,upPos,upState,EnumFacing.DOWN,FluidRegistry.WATER,1,
                    FluidHostOperation.DO_WITH_CERTAINTY, TO_SOIL,BlockFlagsModifier.MODIFY_NOTHING);
        }
        return 0;
    }

    default int onEvaporate(final @Nonnull World world,
                            final @Nonnull BlockPos pos,
                            final @Nonnull IBlockState state,
                            final @Nonnull Random random){
        final @Nonnull BlockPos up = pos.up();
        if(world.isAirBlock(up)) return 0;
        final int humidity = getLayers(state,null,FluidRegistry.WATER);
        if(humidity ==0) return 0;
        try(@Nullable final IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(world,pos,true)) {
            if (accessor == null) return 0;
            final int light = ChunkUtil.getNeighborsLightFor(world,EnumSkyBlock.SKY,pos);
            accessor.setSkyLight(light);

            if(!accessor.getAtmosphereInfo().canWaterEvaporate()) return 0;
            if(!accessor.canAccessAtmosphere()) return 0;

            double basePossibility = WaterUtil.getWaterEvaporatePossibility(accessor);
            basePossibility /= (8-humidity)*2;
            if(!BaseUtil.getRandomResult(random,basePossibility)) return 0;

            accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_EVAPORATE_LATENT_HEAT_PER_QUANTA);
            accessor.fillFluidToAtmosphere(FluidRegistry.WATER,FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true);
            return -1;
        }
    }

    default void onRandomTick(final @Nonnull World worldIn,
                              final @Nonnull BlockPos pos,
                              final @Nonnull IBlockState state,
                              final @Nonnull Random random){
        if(worldIn.isRemote) return;
        final int humidity = getLayers(state,null,FluidRegistry.WATER);
        int newHumidity = humidity;
        final int rnd = random.nextInt(3);
        switch (rnd){
            case 0: //吸收上面的水
                if(humidity < 4) {
                    newHumidity += drainUpWater(worldIn,pos,state);
                }
                break;
            case 1: //向下掉水
                if(humidity >getMaxStableHumidity(state)){
                    newHumidity += dropWaterDown(worldIn, pos,state);
                }
                break;
            default: //水平平衡
                flowWaterHorizontally(worldIn,pos,state,humidity);
                return;
        }
        if(humidity == newHumidity){
            if(humidity == 0) return;
            newHumidity += onEvaporate(worldIn, pos, state, random);
        }
        if(humidity == newHumidity) return;
        setLayer(worldIn,pos,state,null,FluidRegistry.WATER,newHumidity,null,BlockFlagsModifier.MODIFY_NOTHING);
    }

    /**
     * 土壤在破坏时掉水的能力
     */
    default void dropWaterWhenBroken(final @Nonnull World world,
                                     final @Nonnull BlockPos pos,
                                     final @Nonnull IBlockState state){
        final int humidity = getLayers(state,null,FluidRegistry.WATER);
        if(humidity == 0) return;
        if(getCurrentMode() != FluidPhysicsMode.MORE_REALITY){
            world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5,
                    0, 0, 0, Block.getStateId(Blocks.WATER.getDefaultState()));
            return;
        }
        FiniteFlowingVanilla.WATER_FLOW.placeDynamicBlock(world,pos,8-humidity);
    }

    /**
     * 当玩家右键土壤添加水分的操作
     * @see BlockCauldron#onBlockActivated(World, BlockPos, IBlockState, EntityPlayer, EnumHand, EnumFacing, float, float, float)
     */
    @MultiThread({ThreadType.MINECRAFT_CLIENT,ThreadType.MINECRAFT_SERVER})
    default boolean onPlayerUseBottle(final @Nonnull World worldIn,
                                      final @Nonnull BlockPos pos,
                                      final @Nonnull IBlockState state,
                                      final @Nonnull EntityPlayer playerIn,
                                      final @Nonnull EnumHand hand,
                                      final @Nonnull EnumFacing facing,
                                      final float hitX,
                                      final float hitY,
                                      final float hitZ){
        final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
        if(stack.isEmpty()) return false;
        final int moisture = getLayers(state,facing,FluidRegistry.WATER);
        final @Nonnull Item item = stack.getItem();
        if(moisture >2) return false;
        if (item == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER) {
            if (!playerIn.capabilities.isCreativeMode) {
                final @Nonnull ItemStack bottleStack = new ItemStack(Items.GLASS_BOTTLE);
                playerIn.setHeldItem(hand, bottleStack);

                if (playerIn instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                }
            }

            worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.addLayer(worldIn,pos,state,facing,FluidRegistry.WATER,2,FluidHostOperation.DO_WITH_CERTAINTY);
            return true;
        }
        return false;
    }

    /**
     * 检查土壤水是否能够流进指定方块
     * @param state 目标方块状态
     * @return 能，则true，否，则反之
     */
    default boolean canFlowInto(@Nonnull final World world,
                                @Nonnull final BlockPos pos,
                                @Nonnull final IBlockState state,
                                @Nonnull final EnumFacing fromFacing){
        if(state.getMaterial() == Material.AIR) return true;
        final @Nonnull Block block = state.getBlock();
        if(block instanceof ILayeredFluidHost){
            final @Nonnull ILayeredFluidHost host = (ILayeredFluidHost) block;
            return host.canFill(world,pos,state,fromFacing.getOpposite(),FluidRegistry.WATER,FROM_SOIL,false);
        }
        return false;
    }

    @Nonnull
    BlockSoilType getType(@Nonnull final IBlockState state);

    default int getMaxStableHumidity(@Nonnull final IBlockState state){
        return getType(state).getMaxStableHumidity();
    }

    default double getFlowInPossibility(@Nonnull final IBlockState state){
        return getType(state).getFlowInPossibility();
    }

    default double getRainInPossibility(@Nonnull final IBlockState state){
        return getType(state).getRainInPossibility();
    }

    //******************
    // ILayeredFluidHost
    //******************


    @Nullable
    @Override
    default EnumFacing getDefaultSide(@Nonnull final IBlockState state){
        return null;
    }

    @Override
    default boolean isAcceptedFluid(@Nonnull final IBlockState state,
                                    @Nullable final EnumFacing side,
                                    @Nonnull final Fluid fluid){
        return fluid == FluidRegistry.WATER;
    }

    @Override
    default int getLayers(@Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid){
        if(fluid != FluidRegistry.WATER) return 0;
        return state.getValue(HUMIDITY);
    }

    @Override
    default int getMaxLayers(@Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        if(fluid != FluidRegistry.WATER) return 0;
        return 4;
    }

    @Override
    default int getHeight(@Nonnull final IBlockState state,
                          @Nullable final EnumFacing side,
                          @Nonnull final Fluid fluid) {
        if(fluid != FluidRegistry.WATER) return 0;
        return state.getValue(HUMIDITY)* getHeightPerLayer(state,side);
    }

    @Override
    default int getEmptyHeight(@Nonnull final IBlockState state,
                               @Nullable final EnumFacing side,
                               @Nonnull final Fluid fluid){
        return LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default int getHeightPerLayer(@Nonnull final IBlockState state,
                                  @Nullable final EnumFacing side){
        return LayeredFluidHostUtil.FIFTH_HEIGHT;
    }

    @Override
    default int getMaxHeight(@Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid) {
        return fluid == FluidRegistry.WATER ? LayeredFluidHostUtil.FIFTH_HEIGHT*4:LayeredFluidHostUtil.EMPTY_HEIGHT;
    }

    @Override
    default long getAmountInQBPerLayer(@Nonnull final IBlockState state,
                                       @Nullable final EnumFacing side,
                                       @Nonnull final Fluid fluid){
        return QBUtil.QUANTA_VOLUME;
    }

    @Override
    default boolean setLayer(@Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final IBlockState state,@Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,final int newLayer, @Nullable final NBTTagCompound nbt, final long blockFlagsModifier){
        if(fluid != FluidRegistry.WATER) return false;
        if(newLayer<0 || newLayer>4) return false;
        return world.setBlockState(pos,state.withProperty(HUMIDITY, newLayer), BlockFlagsModifier.modify(BlockFlags.SEND_TO_CLIENTS,blockFlagsModifier));
    }

    @Nullable
    @Override
    default IBlockState getLayerState(@Nonnull final IBlockState state, @Nullable final EnumFacing side,@Nonnull final Fluid fluid, final int layer){
        if(fluid != FluidRegistry.WATER) return null;
        if(layer <0 || layer >4) return null;
        return state.withProperty(HUMIDITY, layer);
    }

    /**
     * 指定流体是否能够流入当前方块
     * @param world 世界
     * @param pos 位置
     * @param state 方块状态
     * @param fluid 需要流入的流体
     * @return 若可以，则返回true
     */
    @Override
    default boolean canFill(@Nonnull final World world,
                            @Nonnull final BlockPos pos,
                            @Nonnull final IBlockState state,
                            @Nullable final EnumFacing side,
                            @Nonnull final Fluid fluid,
                            @Nullable final IFluidFrom from,
                            final boolean isAssumed){
        if(fluid != FluidRegistry.WATER) return false;
        if(isFull(state,side,fluid)) return false;
        if(IFluidFrom.isAtmosphere(from)){
            return BaseUtil.getRandomResult(world.rand,getRainInPossibility(state));
        }else if (IFluidFrom.isSurfaceRunoff(from)){
            return BaseUtil.getRandomResult(world.rand, getFlowInPossibility(state));
        }
        return true;
    }

    @Override
    default boolean canDrain(@Nonnull final World world,
                             @Nonnull final BlockPos pos,
                             @Nonnull final IBlockState state,
                             @Nullable final EnumFacing side,
                             @Nonnull final Fluid fluid,
                             @Nullable final IFluidTo to,
                             final boolean isAssumed) {
        if(fluid != FluidRegistry.WATER) return false;
        return getLayers(state,side,fluid) > getMaxStableHumidity(state);
    }

    @Override
    default boolean isFull(@Nonnull final IBlockState state,
                           @Nullable final EnumFacing side,
                           @Nonnull final Fluid fluid) {
        if(fluid != FluidRegistry.WATER) return true;
        return state.getValue(HUMIDITY) >= 4;
    }
}
