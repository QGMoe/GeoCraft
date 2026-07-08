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

package top.qiguaiaaaa.geocraft.handler.event;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;
import top.qiguaiaaaa.geocraft.capability.SavingScheduledTicksCapability;
import top.qiguaiaaaa.geocraft.capability.SchedulingTicksCapability;
import top.qiguaiaaaa.geocraft.handler.RegistryHandler;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.world.storage.GeoCraftWorldSavedData;
import top.qiguaiaaaa.geocraft.world.storage.ScheduledTicksData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
public final class CommonEventHandler {

    @SubscribeEvent
    public static void onRegisterBlocks(final @Nonnull RegistryEvent.Register<Block> event){
        RegistryHandler.mapMissingStates();
    }

    @SubscribeEvent
    public static void onRegisterAtmosphereProperty(final @Nonnull RegistryEvent.Register<IGeographyProperty> event){
        RegistryHandler.registerGeographyProperties(event);
    }

    @SubscribeEvent
    @Deprecated
    public static void onWorldLoad(final @Nonnull WorldEvent.Load event){
        final @Nonnull World world = event.getWorld();
        if(world.isRemote) return;
        final @Nullable GeoCraftWorldSavedData data = getSavedData(world);
        if(data == null) return;
        BlockUpdater.scheduleUpdates(world,data.getEntrySet());
        data.setEntrySet(BlockUpdater.getEntries(world));
        data.setWorld(world);
    }

    @SubscribeEvent
    public static void onWorldAttachCapabilities(final @Nonnull AttachCapabilitiesEvent<World> event){
        if(event.getObject().isRemote) return;
        event.addCapability(BlockUpdater.ID, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == SchedulingTicksCapability.BLOCK_UPDATER;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if(hasCapability(capability,facing)){
                    final BlockUpdater updater = new BlockUpdater();
                    updater.setWorld(event.getObject());
                    return SchedulingTicksCapability.BLOCK_UPDATER.cast(updater);
                }
                return null;
            }
        });
    }

    @SubscribeEvent
    public static void onChunkAttachCapabilities(AttachCapabilitiesEvent<Chunk> event){
        if(event.getObject().getWorld() == null //??? 为什么在逻辑客户端这会是null
                || event.getObject().getWorld().isRemote) return;
        event.addCapability(ScheduledTicksData.ID, new ICapabilitySerializable<NBTTagCompound>() {
            private final ScheduledTicksData data = new ScheduledTicksData().setChunk(event.getObject());
            @Override
            public NBTTagCompound serializeNBT() {
                return data.serializeNBT();
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt) {
                data.deserializeNBT(nbt);
            }

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == SavingScheduledTicksCapability.SCHEDULED_TICKS_DATA;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if(hasCapability(capability,facing)){
                    return SavingScheduledTicksCapability.SCHEDULED_TICKS_DATA.cast(data);
                }else return null;
            }
        });
    }

    @Nullable
    @Deprecated
    static GeoCraftWorldSavedData getSavedData(@Nonnull World world){
        return (GeoCraftWorldSavedData) world.getPerWorldStorage().getOrLoadData(GeoCraftWorldSavedData.class,GeoCraftWorldSavedData.DATA_NAME);
    }

}
