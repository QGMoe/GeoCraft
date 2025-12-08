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

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemType;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.DefaultAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.StorageAtmosphereDataLoader;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereSystemEvent;
import top.qiguaiaaaa.geocraft.configs.ConfigurationLoader;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.CloseAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.SurfaceAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.VanillaAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.system.CloseAtmosphereSystem;
import top.qiguaiaaaa.geocraft.geography.atmosphere.system.SurfaceAtmosphereSystem;
import top.qiguaiaaaa.geocraft.geography.atmosphere.system.VanillaAtmosphereSystem;

import java.io.File;

public final class AtmosphereEventHandler {
    @SubscribeEvent
    public static void createAtmosphereSystem(AtmosphereSystemEvent.Create event){
        WorldServer server= event.getWorld();
        WorldProvider provider = server.provider;
        int dimension = provider.getDimension();
        AtmosphereSystemType type = event.getType();
        if(type == AtmosphereSystemType.NO_ATMOSPHERE_SYSTEM || type == AtmosphereSystemType.THIRD_PARTY_ATMOSPHERE_SYSTEM) return;
        AtmosphereInfo info = new AtmosphereInfo(server);
        IAtmosphereSystem system = null;


        String saveFolder = provider.getSaveFolder();
        if(saveFolder == null) saveFolder = "DIM"+provider.getDimension();

        IAtmosphereDataProvider dataProvider = null;

        if(type == AtmosphereSystemType.SURFACE_ATMOSPHERE_SYSTEM){
            StorageAtmosphereDataLoader loader = new StorageAtmosphereDataLoader(new File(server.getSaveHandler().getWorldDirectory(),saveFolder));
            system = new SurfaceAtmosphereSystem(server,info,
                    SurfaceAtmosphereSystemInfo.create(event.getSystemInfo().getDetails()),
                    dataProvider = new DefaultAtmosphereDataProvider(server,loader));
        }else if(type == AtmosphereSystemType.CLOSE_ATMOSPHERE_SYSTEM){
            StorageAtmosphereDataLoader loader = new StorageAtmosphereDataLoader(new File(server.getSaveHandler().getWorldDirectory(),saveFolder));
            system = new CloseAtmosphereSystem(server,info,
                    CloseAtmosphereSystemInfo.create(event.getSystemInfo().getDetails()),
                    dataProvider = new DefaultAtmosphereDataProvider(server,loader));
        }else if(type == AtmosphereSystemType.VANILLA_ATMOSPHERE_SYSTEM){
            StorageAtmosphereDataLoader loader = new StorageAtmosphereDataLoader(new File(server.getSaveHandler().getWorldDirectory(),saveFolder));
            system = new VanillaAtmosphereSystem(server,info,
                    VanillaAtmosphereSystemInfo.create(event.getSystemInfo().getDetails()),
                    dataProvider = new DefaultAtmosphereDataProvider(server,loader));
        }

        ConfigurationLoader.save();

        GeoCraft.getLogger().info("Dimension DIM{} is using atmosphere system type {}",dimension,type);

        event.setSystem(system);
    }
}
