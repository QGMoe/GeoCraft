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

package top.qiguaiaaaa.geocraft.handler;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;
import top.qiguaiaaaa.geocraft.api.soil.SoilSystem;
import top.qiguaiaaaa.geocraft.block.BlockSnowExtended;
import top.qiguaiaaaa.geocraft.block.BlockSnowFinite;
import top.qiguaiaaaa.geocraft.block.soil.*;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.property.*;
import top.qiguaiaaaa.geocraft.handler.event.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RegistryHandler {
    private static final Map<String, Block> OverrideVanillaBlockRegistry = new HashMap<>();
    private static final Int2ObjectOpenHashMap<IBlockState> missingStatesMap = new Int2ObjectOpenHashMap<>();

    public static void registerVanillaBlockOverride(@Nonnull final Block newBlock){
        OverrideVanillaBlockRegistry.put(Objects.requireNonNull(newBlock.getRegistryName()).getPath(),newBlock);
    }

    public static void registerVanillaBlockOverride(@Nonnull final String blockId,@Nonnull final Block newBlock){
        OverrideVanillaBlockRegistry.put(blockId,newBlock);
    }

    @Nullable
    public static Block queryOverrideVanillaBlock(@Nonnull final String blockId){
        return OverrideVanillaBlockRegistry.get(blockId);
    }

    static {
        registerVanillaBlockOverrides();
    }

    /**
     * @see Block#registerBlocks()
     */
    private static void registerVanillaBlockOverrides(){
        registerVanillaBlockOverride("snow_layer",(FluidPhysicsMode.getCurrentMode() == FluidPhysicsMode.MORE_REALITY?new BlockSnowFinite():new BlockSnowExtended())
                .setHardness(0.1F).setTranslationKey("snow").setLightOpacity(0));
        final Block grass;
        final Block dirt;
        final Block sand;
        final Block gravel;
        final Block grass_path;
        final Block clay;
        if(SoilSystem.getStatus()){
            grass = new BlockSoilGrass();
            dirt = new BlockSoilDirt();
            sand = new BlockSoilSand();
            gravel = new BlockSoilGravel();
            grass_path = new BlockSoilGrassPath();
            clay = new BlockSoilClay();
            registerVanillaBlockOverride("farmland",(FluidPhysicsMode.getCurrentMode() == FluidPhysicsMode.MORE_REALITY?new BlockSoilFarmland.MoreReality():new BlockSoilFarmland())
                    .setHardness(0.6F).setTranslationKey("farmland"));
        }else {
            grass = new BlockSoilExtends.Grass();
            dirt = new BlockSoilExtends.Dirt();
            sand = new BlockSoilExtends.Sand();
            gravel = new BlockSoilExtends.Gravel();
            grass_path = new BlockSoilExtends.GrassPath();
            clay = new BlockSoilExtends.Clay();
        }
        registerVanillaBlockOverride("grass",grass.setHardness(0.6F).setTranslationKey("grass"));
        registerVanillaBlockOverride("dirt",dirt.setHardness(0.5F).setTranslationKey("dirt"));
        registerVanillaBlockOverride("sand",sand.setHardness(0.5F).setTranslationKey("sand"));
        registerVanillaBlockOverride("gravel",gravel.setHardness(0.6F).setTranslationKey("gravel"));
        registerVanillaBlockOverride("grass_path",grass_path.setHardness(0.65F).setTranslationKey("grassPath"));
        registerVanillaBlockOverride("clay",clay.setHardness(0.6F).setTranslationKey("clay"));
    }

    @SuppressWarnings("deprecation")
    public static void mapMissingStates(){
        if(SoilSystem.getStatus()) return;
        GeoCraft.getLogger().info("GeoCraft is mapping missing states for disabling soil system");
        for(final @Nonnull Block block: Arrays.asList(Blocks.GRASS,Blocks.DIRT,Blocks.SAND,Blocks.GRAVEL,Blocks.GRASS_PATH,Blocks.CLAY)){
            final Object2IntOpenHashMap<IBlockState> extendedStateToMetaMap = new Object2IntOpenHashMap<>();
            final int id = Block.getIdFromBlock(block);
            for(int meta = 0;meta<16;meta++){
                final IBlockState state = block.getStateFromMeta(meta);
                if(extendedStateToMetaMap.containsKey(state)) continue;
                extendedStateToMetaMap.put(state,meta);
            }
            for(final Object2IntMap.Entry<IBlockState> entry: extendedStateToMetaMap.object2IntEntrySet()){
                missingStatesMap.put(id<<4|entry.getIntValue(),entry.getKey());
            }
        }
    }

    @Nullable
    public static IBlockState mapToMissingState(final int id){
        return missingStatesMap.get(id);
    }

    public static void registerGeographyProperties(final @Nonnull RegistryEvent.Register<IGeographyProperty> event){
        IForgeRegistry<IGeographyProperty> registry =event.getRegistry();
        registry.register(DefaultTemperature.TEMPERATURE);
        registry.register(DeepTemperature.DEEP_TEMPERATURE);
        registry.register(AtmosphereWater.WATER);
        registry.register(AtmosphereSteam.STEAM);
        registry.register(CarbonDioxide.CARBON_DIOXIDE);
        registry.register(AltitudeProperty.ALTITUDE);
        registry.register(HeatCapacity.HEAT_CAPACITY);
        registry.register(ReflectivityProperty.REFLECTIVITY);
        registry.register(FinalTemperature.FINAL_TEMPERATURE);

        GeoCraftProperties.FINAL_TEMPERATURE = FinalTemperature.FINAL_TEMPERATURE;
        GeoCraftProperties.TEMPERATURE = DefaultTemperature.TEMPERATURE;
        GeoCraftProperties.DEEP_TEMPERATURE = DeepTemperature.DEEP_TEMPERATURE;
        GeoCraftProperties.WATER = AtmosphereWater.WATER;
        GeoCraftProperties.CARBON_DIOXIDE = CarbonDioxide.CARBON_DIOXIDE;
        GeoCraftProperties.STEAM = AtmosphereSteam.STEAM;
        GeoCraftProperties.ALTITUDE = AltitudeProperty.ALTITUDE;
        GeoCraftProperties.HEAT_CAPACITY = HeatCapacity.HEAT_CAPACITY;
        GeoCraftProperties.REFLECTIVITY = ReflectivityProperty.REFLECTIVITY;
    }

    public static void registerEventHandler(){
        EventFactory.EVENT_BUS.register(AtmosphereEventHandler.class);
        if(SoilSystem.getStatus()) MinecraftForge.EVENT_BUS.register(SoilEventHandler.class);
        final @Nonnull FluidPhysicsMode mode = FluidPhysicsConfig.FLUID_PHYSICS_MODE.getValue();
        switch (mode){
            case MORE_REALITY:{
                registerMoreRealityEventHandler();
                break;
            }
            case VANILLA_LIKE:{
                registerVanillaLikeEventHandler();
                break;
            }
            case VANILLA:
            default:{
                registerVanillaEventHandler();
                break;
            }
        }
    }

    private static void registerMoreRealityEventHandler(){
        FiniteEventHandler finiteEventHandler = new FiniteEventHandler();
        MinecraftForge.EVENT_BUS.register(finiteEventHandler);
        EventFactory.EVENT_BUS.register(finiteEventHandler);
    }
    private static void registerVanillaLikeEventHandler(){
        ClassicEventHandler handler = new ClassicEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        EventFactory.EVENT_BUS.register(handler);
    }
    private static void registerVanillaEventHandler(){
        VanillaEventHandler handler = new VanillaEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        EventFactory.EVENT_BUS.register(handler);
    }
}
