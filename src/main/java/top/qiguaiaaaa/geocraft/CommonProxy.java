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

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.capability.SavingScheduledTicksCapability;
import top.qiguaiaaaa.geocraft.capability.SchedulingTicksCapability;
import top.qiguaiaaaa.geocraft.configs.ConfigInit;
import top.qiguaiaaaa.geocraft.configs.ConfigurationLoader;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.reality.RealityFluidOperationChecker;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla.VanillaFluidOperationChecker;
import top.qiguaiaaaa.geocraft.handler.FluidHandler;
import top.qiguaiaaaa.geocraft.handler.RegistryHandler;
import top.qiguaiaaaa.geocraft.handler.event.MoreRealityEventHandler;
import top.qiguaiaaaa.geocraft.handler.event.SoilEventHandler;
import top.qiguaiaaaa.geocraft.handler.event.VanillaLikeEventHandler;
import top.qiguaiaaaa.geocraft.handler.network.NetworkFakeStateManager;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.MixinUtil;

import java.io.File;

import static top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig.FLUID_PHYSICS_MODE;

public class CommonProxy {
    private static boolean isConfigInitialised = false;
    public static void earlyInit(){
        initConfig();
        File suggestedConfigurationFile = BaseUtil.getSuggestedConfigurationFile();
        if(suggestedConfigurationFile==null){
            return;
        }
        ConfigurationLoader.init(suggestedConfigurationFile);
        ConfigurationLoader.load();
    }
    public void preInit(FMLPreInitializationEvent event) {
        SchedulingTicksCapability.register();
        SavingScheduledTicksCapability.register();

        if(ConfigurationLoader.isInitialised()) return;
        if(!isConfigInitialised) initConfig();
        ConfigurationLoader.init(event.getSuggestedConfigurationFile());
        ConfigurationLoader.load();
    }

    public void init(FMLInitializationEvent event) {
        RegistryHandler.registerEventHandler();
    }

    public void postInit(FMLPostInitializationEvent event) {
        MixinUtil.linkLiquidWithFluid();
        FluidHandler.initRegisteredFluids();
        FluidPhysicsMode.VANILLA_LIKE.setChecker(new VanillaFluidOperationChecker());
        FluidPhysicsMode.VANILLA.setChecker(new VanillaFluidOperationChecker());
        FluidPhysicsMode.MORE_REALITY.setChecker(new RealityFluidOperationChecker());
        if(FLUID_PHYSICS_MODE.getValue() == FluidPhysicsMode.MORE_REALITY){
            MoreRealityEventHandler.onPostInit(event);
        }else if(FLUID_PHYSICS_MODE.getValue() == FluidPhysicsMode.VANILLA_LIKE){
            VanillaLikeEventHandler.onPostInit(event);
        }
        SoilEventHandler.onPostInit(event);
        NetworkFakeStateManager.registerDefaultConfig();
    }

    private static void initConfig(){
        ConfigInit.initConfigs();
        isConfigInitialised = true;
    }
}
