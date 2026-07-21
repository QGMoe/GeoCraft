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

package moe.qingu.geocraft.api.event;

import moe.qingu.geocraft.api.event.fluidphysics.FluidUpdaterManagerEvent;
import moe.qingu.geocraft.api.fluidphysics.updater.scheduler.FluidTaskScheduler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import moe.qingu.geocraft.api.atmosphere.Atmosphere;
import moe.qingu.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.geocraft.api.atmosphere.system.IAtmosphereSystem;
import moe.qingu.geocraft.api.event.atmosphere.AtmosphereAccessEvent;
import moe.qingu.geocraft.api.event.atmosphere.AtmosphereGenerateEvent;
import moe.qingu.geocraft.api.event.atmosphere.AtmosphereSystemEvent;
import moe.qingu.geocraft.api.event.atmosphere.AtmosphereUpdateEvent;
import moe.qingu.geocraft.api.event.block.StaticLiquidUpdateEvent;
import moe.qingu.geocraft.api.event.player.ExtendedUseHoeEvent;
import moe.qingu.geocraft.api.event.player.FillGlassBottleEvent;
import moe.qingu.geocraft.api.event.player.FillGlassBottleEvent.FillGlassBottleOnAreaEffectCloudEvent;
import moe.qingu.geocraft.api.event.player.FillGlassBottleEvent.FillGlassBottleOnFluidEvent;
import moe.qingu.geocraft.api.event.player.UseSpadeEvent;
import moe.qingu.geocraft.api.fluid.StateOfMatter;
import moe.qingu.geocraft.api.setting.GeoAtmosphereSetting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static moe.qingu.geocraft.api.util.AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;

/**
 * 天圆地方的事件管理器
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class EventFactory {
    public static final EventBus EVENT_BUS = new EventBus();

    public static ActionResult<ItemStack> onGlassBottleUseOnAreaEffectCloud(@Nonnull EntityPlayer player, @Nonnull ItemStack itemStack, World world, @Nonnull List<EntityAreaEffectCloud> entityList){
        FillGlassBottleEvent event = new FillGlassBottleOnAreaEffectCloudEvent(player,itemStack,world,entityList);
        return processOnGlassBottleUseEvent(itemStack,player,event);
    }
    public static ActionResult<ItemStack> onGlassBottleUseOnFluid(@Nonnull EntityPlayer player, @Nonnull ItemStack itemStack, World world, @Nullable RayTraceResult rayTraceResult){
        FillGlassBottleEvent event = new FillGlassBottleOnFluidEvent(player,itemStack,world,rayTraceResult);
        return processOnGlassBottleUseEvent(itemStack,player,event);
    }

    /* -----------------------
       Atmosphere Events
       ----------------------- */

    public static IBlockState onAtmosphereRainAndSnow(@Nonnull Chunk chunk, @Nonnull IAtmosphereAccessor accessor, @Nonnull Atmosphere atmosphere, @Nonnull BlockPos randPos, double rainPossibility){
        AtmosphereUpdateEvent.RainAndSnow event = new AtmosphereUpdateEvent.RainAndSnow(chunk,atmosphere,accessor,randPos,rainPossibility);
        EVENT_BUS.post(event);
        if(event.getResult() == Result.ALLOW){
            if(event.isSnowy()){
                accessor.putHeatToAtmosphere(WATER_MELT_LATENT_HEAT_PER_QUANTA);
            }
            return event.getState();
        }
        return null;
    }
    public static void postAtmosphereUpdate(@Nullable Chunk chunk, @Nonnull Atmosphere atmosphere, int x,int z){
        AtmosphereUpdateEvent.Post event = new AtmosphereUpdateEvent.Post(chunk,atmosphere,x,z);
        EVENT_BUS.post(event);
    }
    public static void preAtmosphereGenerate(@Nonnull WorldServer world,@Nonnull Chunk chunk){
        AtmosphereGenerateEvent.Pre event = new AtmosphereGenerateEvent.Pre(world,chunk);
        EVENT_BUS.post(event);
    }
    public static IBlockState afterBlockLiquidStaticUpdate(@Nonnull Fluid fluid, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state,boolean randomTick){
        StaticLiquidUpdateEvent.After event = new StaticLiquidUpdateEvent.After(fluid,world,pos,state,randomTick);
        EVENT_BUS.post(event);
        if(event.getResult() == Result.ALLOW){
            return event.getNewState();
        }
        return null;
    }

    public static IAtmosphereSystem onAtmosphereSystemCreate(@Nonnull WorldServer server){
        AtmosphereSystemEvent.Create event = new AtmosphereSystemEvent.Create(server, GeoAtmosphereSetting.getAtmosphereSystemInfo(server.provider.getDimension()));
        EVENT_BUS.post(event);
        if(event.isCanceled()) return null;
        return event.getSystem();
    }

    /**
     * @since 0.2.0
     * @return 填充的量。若为 -1 则表示事件没有结果
     */
    public static int onFillFluidToAtmosphere(@Nonnull Atmosphere atmosphere,@Nonnull IAtmosphereAccessor accessor,@Nonnull final Fluid fluid,final double temp ,int amount, @Nullable final FluidStack stack, @Nonnull final StateOfMatter state,final boolean doFill){
        AtmosphereAccessEvent.FluidFill event = new AtmosphereAccessEvent.FluidFill(atmosphere, accessor, fluid, stack, amount,temp, state, doFill);
        if(EVENT_BUS.post(event)) return -1;
        if(event.hasResult()){
            switch (event.getResult()){
                case ALLOW:return event.getFilledAmount();
                case DENY:return 0;
                case DEFAULT:
                default:return -1;
            }
        }else return -1;
    }

    /**
     * @since 0.2.0
     * @return 吸取的事件.
     */
    @Nullable
    public static AtmosphereAccessEvent.FluidDrain onDrainedFluidToAtmosphere(@Nonnull Atmosphere atmosphere, @Nonnull IAtmosphereAccessor accessor, @Nonnull final Fluid fluid, int amount,final boolean requireStack, @Nonnull final StateOfMatter state, final boolean doDrain){
        AtmosphereAccessEvent.FluidDrain event = new AtmosphereAccessEvent.FluidDrain(atmosphere,accessor,fluid,amount,requireStack,state,doDrain);
        if(EVENT_BUS.post(event)) return null;
        return event;
    }

    /* -----------------------
       FluidPhysics Events
       ----------------------- */

    @Nullable
    public static Supplier<FluidTaskScheduler> onFluidUpdaterManagerCreate(@Nonnull final World world){
        FluidUpdaterManagerEvent.Create event = new FluidUpdaterManagerEvent.Create(world);
        EVENT_BUS.post(event);
        return event.hasResult() && event.getResult() == Result.ALLOW?event.getCandidate():null;
    }

    public static int onHoeUse(final @Nonnull ItemStack stack,final @Nonnull EntityPlayer player,final @Nonnull World worldIn,
                               final @Nonnull BlockPos pos,final @Nonnull EnumHand hand,final @Nonnull EnumFacing facing,
                               final float x,final float y,final float z) {
        ExtendedUseHoeEvent event = new ExtendedUseHoeEvent(player, stack, worldIn, pos,hand,facing,x,y,z);
        if (MinecraftForge.EVENT_BUS.post(event)) return -1;
        if (event.getResult() == Result.ALLOW) {
            stack.damageItem(1, player);
            return 1;
        }
        return 0;
    }

    public static int onSpadeUse(final @Nonnull ItemStack stack,final @Nonnull EntityPlayer player,final @Nonnull World worldIn,
                                 final  @Nonnull BlockPos pos,final @Nonnull EnumHand hand,final @Nonnull EnumFacing facing,
                                 final float x,final float y,final float z) {
        final @Nonnull UseSpadeEvent event = new UseSpadeEvent(player,stack,worldIn,pos,hand,facing,x,y,z);
        if (MinecraftForge.EVENT_BUS.post(event)) return -1;
        if (event.getResult() == Result.ALLOW) {
            stack.damageItem(1, player);
            return 1;
        }
        return 0;
    }

    private static ActionResult<ItemStack> processOnGlassBottleUseEvent(ItemStack itemStack,EntityPlayer player,FillGlassBottleEvent event){
        if(EVENT_BUS.post(event)) return new ActionResult<>(EnumActionResult.PASS,itemStack);
        if(event.getResult() == Result.ALLOW){
            itemStack.shrink(1);

            final ItemStack filled = event.getFilledGlassBottle();

            if (itemStack.isEmpty()) {
                return new ActionResult<>(EnumActionResult.SUCCESS,filled == null?new ItemStack(Items.AIR,1):filled);
            } else {
                if (filled != null && !player.inventory.addItemStackToInventory(filled)) {
                    player.dropItem(filled, false);
                }

                return new ActionResult<>(EnumActionResult.SUCCESS,itemStack);
            }
        }
        return null;
    }

    @Nullable
    public static CapabilityDispatcher gatherCapabilities(@Nonnull Atmosphere atmosphere) {
        return gatherCapabilities(new AttachCapabilitiesEvent<>(Atmosphere.class, atmosphere), null);
    }

    @Nullable
    private static CapabilityDispatcher gatherCapabilities(AttachCapabilitiesEvent<?> event, @Nullable ICapabilityProvider parent) {
        EVENT_BUS.post(event);
        return event.getCapabilities().size() > 0 || parent != null ? new CapabilityDispatcher(event.getCapabilities(), parent) : null;
    }
}
