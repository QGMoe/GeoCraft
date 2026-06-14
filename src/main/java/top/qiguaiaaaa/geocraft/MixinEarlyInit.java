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

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.configs.GeneralConfig;
import top.qiguaiaaaa.geocraft.configs.SoilConfig;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.Name(GeoCraft.MODID)
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public class MixinEarlyInit implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final boolean isClient = FMLLaunchHandler.side() == Side.CLIENT;
    public static final Logger LOGGER = LogManager.getLogger(GeoCraft.MODID + " Init");

    @Override
    public List<String> getMixinConfigs() {
        CommonProxy.earlyInit();
        List<String> mixinList =  Lists.newArrayList("mixins.geocraft.json");
        FluidPhysicsMode mode = FluidPhysicsConfig.FLUID_PHYSICS_MODE.getValue();
        switch (mode){
            case VANILLA:{
                if(FluidPhysicsConfig.ENABLE_MIXIN_FOR_WATER_EVAPORATE.getValue())
                    mixinList.add("mixins/fluid_physics/vanilla/mixins.geocraft_vanilla.json");
                break;
            }
            case VANILLA_LIKE:{
                mixinList.add("mixins.geocraft_vanilla_like.json");
                break;
            }
            case MORE_REALITY:
            default:{
                mixinList.add("mixins/fluid_physics/finite/mixins.geocraft_finite.json");
                if(FluidPhysicsConfig.ENABLE_INVALID_LIQUID_STATE_REPORT.getValue()){
                    mixinList.add("mixins/fluid_physics/finite/mixins.geocraft_finite_debug.json");
                }
                if(FluidPhysicsConfig.BOAT_ADJUSTMENT.getValue()){
                    mixinList.add("mixins/fluid_physics/finite/mixins.orbtellus_finite_boat.json");
                }
                break;
            }
        }
        if(!isClient && GeneralConfig.COMPATIBLE_FOR_VANILLA_CLIENT.getValue()){
            mixinList.add("mixins/ground_water/mixins.geocraft_client_fake.json");
        }

        if(GeneralConfig.PREVENT_FALLING_BLOCK_FROM_FALLING_WHILE_GENERATION.getValue()){
            mixinList.add("mixins/common/mixins.geocraft_falling_block.json");
        }

        if(FluidPhysicsConfig.PAUSE_PRESSURE_SYSTEM_WHILE_CHUNK_SAVING.getValue()){
            if(FluidPhysicsConfig.WRAP_MODIFIED_CHUNK_SAVING_METHOD.getValue()){
                mixinList.add("mixins/pressure/async/mixins.geocraft_pressure_async_8.json");
            }else mixinList.add("mixins/pressure/async/mixins.geocraft_pressure_async.json");
        }
        mixinList.add("mixins/ground_water/mixins.geocraft_ground_water.json");
        if(SoilConfig.ENABLE_SOIL_SYSTEM.getValue()){
            mixinList.add("mixins/soil/mixins.geocraft_soil.json");
        }else mixinList.add("mixins/soil/mixins.geocraft_disabled_soil.json");
        mixinList.add("mixins.geocraft_atmosphere.json");
        LOGGER.info("天圆地方(GeoCraft)'s Fluid Physics is using {} mode",mode);
        FluidPhysicsMode.setCurrentMode(mode);

        return mixinList;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
        //return new String[]{"top.qiguaiaaaa.geocraft.asm.FluidloggedAPICompatTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}