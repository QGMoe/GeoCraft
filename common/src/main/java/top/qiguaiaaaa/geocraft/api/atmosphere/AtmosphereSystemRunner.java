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

package top.qiguaiaaaa.geocraft.api.atmosphere;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import top.qiguaiaaaa.geocraft.api.GeoCraftAPI;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereGenerateEvent;

import static top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager.*;

/**
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class AtmosphereSystemRunner {
    static {
        if(Loader.isModLoaded(GeoCraftAPI.MODID)){
            MinecraftForge.EVENT_BUS.register(AtmosphereSystemRunner.class);
            EventFactory.EVENT_BUS.register(AtmosphereSystemRunner.class);
        }
    }

    private AtmosphereSystemRunner(){}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event){
        if(event.phase == TickEvent.Phase.START) return;
        IAtmosphereSystem system = getAtmosphereSystem(event.world);
        if(system != null){
            system.updateTick();
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        WorldServer server = getValidWorld(event.getWorld());
        if(server == null) return;
        IAtmosphereSystem system = getAtmosphereSystem(server);
        if(system == null) system = EventFactory.onAtmosphereSystemCreate(server);
        if(system == null) return;
        atmosphereSystems.put(server,system);
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event){
        IAtmosphereSystem system = getAtmosphereSystem(event.getWorld());
        if(system == null) return;
        system.onWorldSave();
    }

    public static void onServerStopping(FMLServerStoppingEvent event){
        for(IAtmosphereSystem system:atmosphereSystems.values()){
            if(system == null) continue;
            system.onServerStopping(event);
        }
    }

    public static void onServerStopped(FMLServerStoppedEvent event){
        for(IAtmosphereSystem system:atmosphereSystems.values()){
            if(system == null) continue;
            system.onServerStopped(event);
        }
        atmosphereSystems.clear();
    }

    public static void onPostInit(FMLPostInitializationEvent event){
    }

    @SubscribeEvent
    public static void onPreAtmosphereGenerate(AtmosphereGenerateEvent.Pre event){
        IAtmosphereSystem system = getAtmosphereSystem(event.getWorld());
        if(system == null) return;
        system.onChunkGenerated(event.getChunk());
    }
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event){
        IAtmosphereSystem system = getAtmosphereSystem(event.getWorld());
        if(system == null)return;
        if(!event.getChunk().isTerrainPopulated()) return;
        system.onChunkLoaded(event.getChunk());
    }

    @SubscribeEvent
    public void onChunkUnLoad(ChunkEvent.Unload event){
        IAtmosphereSystem system = getAtmosphereSystem(event.getWorld());
        if(system == null) return;
        system.onChunkUnloaded(event.getChunk());
    }
}
