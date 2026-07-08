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

package top.qiguaiaaaa.geocraft.compat.toughasnails;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.annotation.MultiThread;
import top.qiguaiaaaa.geocraft.api.util.annotation.ThreadType;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.thirst.WaterType;
import toughasnails.fluids.blocks.BlockPurifiedWaterFluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;

/**
 * @author QiguaiAAAA
 */
public final class TANCompat {

    private static HashSet<Block> blocksCanDrink;

    public static void init(final @Nonnull LoaderState state){
        if(state != LoaderState.POSTINITIALIZATION) return;
        blocksCanDrink = new HashSet<>();
        blocksCanDrink.add(Blocks.WATER);
        blocksCanDrink.add(Blocks.FLOWING_WATER);
        blocksCanDrink.add(TANBlocks.purified_water);
        MinecraftForge.EVENT_BUS.register(TANCompat.class);
    }

    public static void registerDrinkableBlock(@Nonnull final Block block){
        blocksCanDrink.add(Objects.requireNonNull(block));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerDrinkInWorld_Server(@Nonnull final TANPlayerDrinkInWorldEvent.Server event){
        if(event.getDrinkBlockPos() == null) return;
        final @Nonnull World world = event.getEntityPlayer().world;
        final @Nonnull BlockPos pos = event.getDrinkBlockPos();
        switch (event.getWaterType()){
            case PURIFIED:{
                final @Nonnull IBlockState state = world.getBlockState(pos);
                final int quanta = Math.max(8-state.getValue(BlockFluidBase.LEVEL),1);
                final boolean drinkByQuanta = FluidPhysicsConfig.drinkPurifiedWaterByQuanta.getValue();
                final int thirst = drinkByQuanta?3:(quanta+5)>>1;
                final @Nonnull IBlockState newState = drinkByQuanta && quanta >= 2? state.withProperty(BlockFluidBase.LEVEL,9-quanta): Blocks.AIR.getDefaultState();
                world.setBlockState(pos,newState, Constants.BlockFlags.DEFAULT);
                event.setThirst(thirst);
                event.setResult(Event.Result.ALLOW);
                return;
            }case NORMAL:{
                final @Nonnull IBlockState state = world.getBlockState(pos);
                final @Nonnull Block block = state.getBlock();
                if(block instanceof BlockLiquid || block instanceof BlockFluidBase){
                    final int quanta = FluidUtil.getFluidQuanta(world,pos,state);
                    FluidOperationUtil.setQuanta(world,pos,state,quanta-1);
                }
            } default:
                event.setResult(Event.Result.ALLOW);
        }
    }

    @MultiThread({ThreadType.MINECRAFT_CLIENT,ThreadType.MINECRAFT_SERVER})
    public static Pair<BlockPos, WaterType> getRightClickedWater(final @Nonnull EntityPlayer player) {
        if (canDrinkFromAtmosphere(player)) {
            return Pair.of(player.getPosition(),WaterType.RAIN);
        } else if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST_WORLD)) {
            final @Nullable RayTraceResult result = MathUtil.rayTrace(player,true);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                final @Nonnull BlockPos pos = result.getBlockPos();
                final @Nonnull Block block = player.world.getBlockState(pos).getBlock();
                if (block instanceof BlockPurifiedWaterFluid) {
                    return Pair.of(pos.toImmutable(), WaterType.PURIFIED);
                } else if (blocksCanDrink.contains(block)) {
                    return Pair.of(pos.toImmutable(), WaterType.NORMAL);
                }
            }
        }
        return null;
    }

    @MultiThread({ThreadType.MINECRAFT_CLIENT,ThreadType.MINECRAFT_SERVER})
    private static boolean canDrinkFromAtmosphere(final @Nonnull EntityPlayer player){
        if(!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST_RAIN)) return false;
        if(player.rotationPitch >= -75.0f) return false;
        final @Nonnull BlockPos pos = player.getPosition();
//        if(!player.world.canSeeSky(pos)) return false;
        return player.world.isRainingAt(pos);
//        try(@Nullable final IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(player.world,player.getPosition(),false)) {
//            if(accessor == null) return false;
//            if(!accessor.canAccessAtmosphere()) return false;
//            final @Nullable Atmosphere atmosphere = accessor.getAtmosphereHere();
//            if(atmosphere == null) return false;
//            return atmosphere.getWeather(pos).isRainy();
//        }
    }
}
