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

package top.qiguaiaaaa.geocraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemRunner;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereRegionFileCache;
import top.qiguaiaaaa.geocraft.command.CommandAtmosphere;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidUpdateManager;
import top.qiguaiaaaa.geocraft.world.BlockUpdater;
import top.qiguaiaaaa.geocraft.world.gen.GeoCraftPostPopulatingGenerator;

@Mod(modid = GeoCraft.MODID, name = GeoCraft.NAME, version = GeoCraft.VERSION, dependencies = "required:mixinbooter;",acceptableRemoteVersions = "*",useMetadata = true)
public class GeoCraft {
    public static final String MODID = "geocraft";
    public static final String NAME = "Geo Craft";
    public static final String VERSION = "0.2.2";
    @SidedProxy(clientSide = "top.qiguaiaaaa.geocraft.ClientProxy",serverSide = "top.qiguaiaaaa.geocraft.CommonProxy")
    private static CommonProxy proxy;
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
        GameRegistry.registerWorldGenerator(new GeoCraftPostPopulatingGenerator(),100000);
    }
    @EventHandler
    public void init(FMLInitializationEvent event){
        proxy.init(event);
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
        AtmosphereSystemRunner.onPostInit(event);
    }
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new CommandAtmosphere());
//        event.registerServerCommand(new CommandQueryBlockState());
        if(FluidPhysicsConfig.RUN_PRESSURE_SYSTEM_AS_ASYNC.getValue()){
            FluidPressureSearchManager.asyncRun();
        }else{
            FluidPressureSearchManager.syncRun();
        }

    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event){
        AtmosphereSystemRunner.onServerStopping(event);
        if(FluidPhysicsConfig.RUN_PRESSURE_SYSTEM_AS_ASYNC.getValue()){
            FluidPressureSearchManager.asyncStop();
        }else{
            FluidPressureSearchManager.syncStop();
        }
        FluidUpdateManager.onServerStop();
        BlockUpdater.onServerStop();
    }

    @EventHandler
    public void onServerStop(FMLServerStoppedEvent event){
        AtmosphereRegionFileCache.clearRegionFileReferences();
        AtmosphereSystemRunner.onServerStopped(event);
    }
    public static Logger getLogger(){
        return logger;
    }
}
