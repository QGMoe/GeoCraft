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

package moe.qingu.geocraft.configs;

import moe.qingu.geocraft.api.util.annotation.EarlyLoaded;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Property;
import moe.qingu.geocraft.api.configs.ConfigCategory;
import moe.qingu.geocraft.api.configs.GeoConfig;
import moe.qingu.geocraft.api.configs.item.base.ConfigBoolean;
import moe.qingu.geocraft.api.configs.item.collection.set.ConfigIntegerSet;
import moe.qingu.geocraft.api.configs.item.collection.set.ConfigSet;
import moe.qingu.geocraft.api.configs.item.map.ConfigMap;
import moe.qingu.geocraft.api.configs.value.collection.ConfigurableSet;
import moe.qingu.geocraft.api.configs.value.map.entry.ConfigEntry;
import moe.qingu.geocraft.api.configs.value.minecraft.ConfigurableBiome;
import moe.qingu.geocraft.api.configs.value.minecraft.ConfigurableBlockState;
import moe.qingu.geocraft.api.soil.SoilSystem;
import moe.qingu.geocraft.geography.soil.BlockSoilType;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
@EarlyLoaded
@SuppressWarnings("unused")
public final class SoilConfig {
    @Config.Comment({"土壤系统相关配置","Configs for Soil System"})
    public static final ConfigCategory CATEGORY_SOIL = new ConfigCategory("soil");

    @Config.Comment({"启用土壤系统，注意禁用土壤系统为新功能，可能会破坏存档，请谨慎使用!",
            "Enable Soil System. Attention: Disabling Soil System is in experiment and may break your world!"})
    @GeoConfig.Support(since = "0.2.6")
    @Config.LangKey("geocraft.config.comment.soil.enable_soil")
    @GeoConfig.Experimental
    @Config.RequiresMcRestart
    private static final ConfigBoolean ENABLE_SOIL_SYSTEM = new ConfigBoolean(CATEGORY_SOIL,"enableSoilSystem",true){
        @Override
        protected void load(@Nonnull Property property) {
            super.load(property);
            SoilSystem.setStatus(this.value);
        }
    };

    @Config.RequiresMcRestart
    @Config.Ignore
    @GeoConfig.Support(since = "0.1")
    public static final ConfigBoolean ALLOW_CLIENT_TO_READ_HUMIDITY_DATA =
            new ConfigBoolean(CATEGORY_SOIL, "allowClientToReadHumidityData",false)
                    .setComment("是否允许客户端读取土壤的湿度数据。默认为允许。在禁止状态下，模组将会对服务器和客户端的网络通信进行修改，以去除土壤的湿度信息。其原理和反矿透原理类似。\n" +
                            "如果您想要允许客户端读取土壤的湿度数据，可以更改此选项为true。这样子，mod将不再修改网络通信，您可以使用其他更专业的mod以阻止客户端阅读土壤湿度数据。\n" +
                            "请注意,部分信息在关闭此项后仍然无法被客户端读取，除非你关闭"+GeneralConfig.COMPATIBLE_FOR_VANILLA_CLIENT.getPath()+"。若关闭前面这个选项，则模组自身无法阻止客户端阅读土壤的湿度数据，即使本配置项为禁用状态。\n" +
                            "该配置项在客户端无效。\n" +
                            "Whether to allow the client to read soil humidity data. Default is enabled. When disabled, the mod will modify network communication between the server and client to remove soil moisture information. The principle is similar to anti-X-ray mechanisms.\n" +
                            "If you wish to allow clients to read soil moisture data, you can change this option to true. In this case, the mod will no longer modify network communication, and you can use other more specialized mods to prevent clients from reading soil moisture data.\n" +
                            "Please note that certain information remains unreadable by the client even after disabling this option, unless you disable " + GeneralConfig.COMPATIBLE_FOR_VANILLA_CLIENT.getPath() + ". If the aforementioned option is disabled, the mod itself cannot prevent clients from reading soil moisture data, even if this configuration is set to disabled.\n" +
                            "This configuration option has no effect on the client side.");

    //*********************
    // Generation
    //*********************

    @Config.Comment("土壤相关的世界生成机制控制\n" + "Control for soil-related world generation features")
    public static final ConfigCategory CATEGORY_SOIL_GENERATION = CATEGORY_SOIL.getChildCategory("generation");

    @GeoConfig.Support(since = "0.1")
    @Config.Comment("开启土壤相关的世界生成机制。\n" +
            "若禁用，自然生成的土壤和其他透水方块将默认不会有湿度，该配置块内的其他功能同样不会生效。\n" +
            "Enable soil-related world generation features.\n" +
            "If disabled, naturally generated soil and other permeable blocks will have no moisture by default, and other functions within this configuration block will also be disabled.")
    public static final ConfigBoolean ENABLE_GENERATION = new ConfigBoolean(CATEGORY_SOIL_GENERATION, "enable",true);

    @GeoConfig.Support(since = "0.1")
    @Config.Comment("需要禁用土壤相关世界生成机制的维度。\n" + "Dimensions for which soil-related world generation features should be disabled.")
    public static final ConfigIntegerSet GENERATION_DIMENSION_BLACK_LIST =
            new ConfigIntegerSet(CATEGORY_SOIL_GENERATION, "dimensionBlackList", new ConfigurableSet<>(1,-1));

    @GeoConfig.Support(since = "0.1")
    @Config.Comment("需要禁用土壤相关世界生成机制的生物群系。\n" +
            "Biomes for which soil-related world generation features should be disabled.")
    public static final ConfigSet<ConfigurableBiome,?> GENERATION_BIOME_BLACK_LIST =
            ConfigSet.create(CATEGORY_SOIL_GENERATION, "biomeBlockList",new ConfigurableSet<>(
                    new ConfigurableBiome("minecraft:hell"),
                    new ConfigurableBiome("minecraft:void"),
                    new ConfigurableBiome("minecraft:sky")
            ),ConfigurableBiome::new);

    @GeoConfig.Support(since = "0.1")
    @Config.Comment("开启水下落的预保护机制，通过在区块生成的时候检测竖直方向上可能被水流入的地方，并自动生成方块阻止水流动，以一定程度上避免诸如海洋生物群系在一生成就漏海的情况。但导致阻止水下落的方块可能在奇怪的地方出现，强迫症不建议开启。\n" +
            "Enable the pre-protection mechanism for water flow by detecting areas vertically susceptible to water inflow during chunk generation and automatically generating blocks to prevent water movement. " +
            "This helps mitigate issues such as water leakage in ocean biomes immediately after generation.")
    public static final ConfigBoolean ENABLE_PRE_PROTECTION_OF_WATER_FALLING =
            new ConfigBoolean(CATEGORY_SOIL_GENERATION, "enablePreProtectionFromWaterFalling",false);

    @Config.RequiresMcRestart
    @GeoConfig.Support(since = "0.1")
    @Config.Comment("每个生物群系用于阻止水下落而生成的方块。默认为石头。\n" +
            "The block generated per biome to prevent water from falling. Defaults to stone.")
    public static final ConfigMap<ConfigurableBiome, ConfigurableBlockState> WATER_PROTECTION_BLOCK =
            new ConfigMap<>(CATEGORY_SOIL_GENERATION, "preProtectionOfWaterFallingBlocks" ,
                    ConfigurableBiome::new,ConfigurableBlockState::getFixedInstanceByString,
                    new ConfigEntry<>(new ConfigurableBiome("minecraft","sky"), new ConfigurableBlockState("minecraft:end_stone",0)),
                    new ConfigEntry<>(new ConfigurableBiome("minecraft","hell"),new ConfigurableBlockState("minecraft:netherrack",0)),
                    new ConfigEntry<>(new ConfigurableBiome("minecraft:void"),new ConfigurableBlockState("minecraft:air",0))
            ).setKeyClass(ConfigurableBiome.class).setValueClass(ConfigurableBlockState.class);

    //*********************
    // Water
    //*********************

    public static final ConfigCategory CATEGORY_SOIL_WATER = CATEGORY_SOIL.getChildCategory("water");

    @GeoConfig.Support(since = "0.1")
    @GeoConfig.Fixed
    @Config.RequiresMcRestart
    @Config.Comment("各种土壤的最大持水量\nMax water-holding capacity for each soil type.")
    @GeoConfig.ValueComment("土壤的最大持水量。含水量超过该值时，土壤中的水分将有流动的趋势。这还会使得部分种类的土壤受重力影响而下落。\n" +
            "Max water-holding capacity. When moisture exceeds this value, water in the soil tends to flow. This also causes certain soil types to fall under gravity.")
    public static final ConfigMap<BlockSoilType,Integer> STABLE_HUMIDITY =
            new ConfigMap<>(CATEGORY_SOIL_WATER, "stableHumidityValues",BlockSoilType::getInstanceByString,Integer::parseInt,
                    new ConfigEntry<>(BlockSoilType.DIRT,2),
                    new ConfigEntry<>(BlockSoilType.COARSE_DIRT,1),
                    new ConfigEntry<>(BlockSoilType.PODZOL,1),
                    new ConfigEntry<>(BlockSoilType.GRASS,3),
                    new ConfigEntry<>(BlockSoilType.GRASS_PATH,3),
                    new ConfigEntry<>(BlockSoilType.SAND,1),
                    new ConfigEntry<>(BlockSoilType.GRAVEL,0),
                    new ConfigEntry<>(BlockSoilType.FARMLAND,1),
                    new ConfigEntry<>(BlockSoilType.CLAY,4)
            ).setKeyClass(BlockSoilType.class).setValueClass(Integer.class);

    @GeoConfig.Support(since = "0.1")
    @GeoConfig.Fixed
    @Config.RequiresMcRestart
    @Config.Comment("地表径流在一次下渗尝试中下渗到指定土壤类型的概率\nProbability of surface runoff infiltrating into the specified soil type per infiltration attempt.")
    @GeoConfig.ValueComment("一个概率,取值范围[0,1]。\nA probability value in the range [0, 1].")
    public static final ConfigMap<BlockSoilType,Double> FLOW_IN_POSSIBILITY =
            new ConfigMap<>(CATEGORY_SOIL_WATER, "possibilityForCurrentsToInfiltrate",BlockSoilType::getInstanceByString,Double::parseDouble,
                    new ConfigEntry<>(BlockSoilType.DIRT,0.3),
                    new ConfigEntry<>(BlockSoilType.COARSE_DIRT,0.4),
                    new ConfigEntry<>(BlockSoilType.PODZOL,0.4),
                    new ConfigEntry<>(BlockSoilType.GRASS,0.5),
                    new ConfigEntry<>(BlockSoilType.GRASS_PATH,0.4),
                    new ConfigEntry<>(BlockSoilType.SAND,0.7),
                    new ConfigEntry<>(BlockSoilType.GRAVEL,0.9),
                    new ConfigEntry<>(BlockSoilType.FARMLAND,0.5),
                    new ConfigEntry<>(BlockSoilType.CLAY,0.1)
            ).setKeyClass(BlockSoilType.class).setValueClass(Double.class);

    @GeoConfig.Support(since = "0.1")
    @Config.RequiresMcRestart
    @GeoConfig.Fixed
    @Config.Comment("大气降雨在一次下渗尝试中下渗到指定土壤类型的概率\nProbability of rainfall infiltrating into the specified soil type per infiltration attempt.")
    @GeoConfig.ValueComment("一个概率,取值范围[0,1]。\nA probability value in the range [0, 1].")
    public static final ConfigMap<BlockSoilType,Double> RAIN_IN_POSSIBILITY =
            new ConfigMap<>(CATEGORY_SOIL_WATER,
                    "possibilityForRainToInfiltrate",BlockSoilType::getInstanceByString,Double::parseDouble,
                    new ConfigEntry<>(BlockSoilType.DIRT,1d),
                    new ConfigEntry<>(BlockSoilType.COARSE_DIRT,1d),
                    new ConfigEntry<>(BlockSoilType.PODZOL,1d),
                    new ConfigEntry<>(BlockSoilType.GRASS,1d),
                    new ConfigEntry<>(BlockSoilType.GRASS_PATH,1d),
                    new ConfigEntry<>(BlockSoilType.SAND,1d),
                    new ConfigEntry<>(BlockSoilType.GRAVEL,1d),
                    new ConfigEntry<>(BlockSoilType.FARMLAND,1d),
                    new ConfigEntry<>(BlockSoilType.CLAY,0.5)
            ).setKeyClass(BlockSoilType.class).setValueClass(Double.class);
}
