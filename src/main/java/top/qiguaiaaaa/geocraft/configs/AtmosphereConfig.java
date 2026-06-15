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

package top.qiguaiaaaa.geocraft.configs;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.GeoConfig;
import top.qiguaiaaaa.geocraft.api.configs.item.base.ConfigBoolean;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigInteger;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.api.configs.value.map.entry.BlockIntegerEntry;
import top.qiguaiaaaa.geocraft.api.configs.value.map.entry.ConfigEntry;
import top.qiguaiaaaa.geocraft.api.configs.value.minecraft.ConfigurableBlockProperty;
import top.qiguaiaaaa.geocraft.api.configs.value.minecraft.ConfigurableBlockState;
import top.qiguaiaaaa.geocraft.api.setting.GeoAtmosphereSetting;
import top.qiguaiaaaa.geocraft.api.setting.GeoBlockSetting;
import top.qiguaiaaaa.geocraft.api.configs.item.map.ConfigMap;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.CloseAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.SurfaceAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.VanillaAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.geography.atmosphere.system.CloseAtmosphereSystem;

import javax.annotation.Nonnull;
import java.util.Map;

@SuppressWarnings("unused")
public final class AtmosphereConfig {

    @GeoConfig.Since("0.1")
    public static final ConfigCategory CATEGORY_ATMOSPHERE = new ConfigCategory("atmosphere");

    @GeoConfig.Since("0.1")
    @Config.Comment("开启详细的大气日志记录")
    @Config.LangKey("geocraft.config.comment.atmosphere.enable_detailed_logging")
    @Config.RequiresMcRestart
    public static final ConfigBoolean ENABLE_DETAIL_LOGGING = new ConfigBoolean(CATEGORY_ATMOSPHERE,"enableDetailLogging", false){
        @Override
        public void load(@Nonnull Configuration config) {
            super.load(config);
            GeoAtmosphereSetting.setEnableDetailedLogging(value);
        }
    };

    @Config.RangeInt(min = 1)
    @Config.Comment("[自 0.2.0-beta.1 开始弃用]大气加载的最大距离，单位为区块。\n" +
            "The max loaded distance for Atmosphere, measured in chunks.")
    @GeoConfig.Since("0.1")
    @Deprecated
    public static final ConfigInteger ATMOSPHERE_MAX_LOAD_DISTANCE = new ConfigInteger(CATEGORY_ATMOSPHERE,"maxLoadDistance",100);

    @GeoConfig.Since("0.1")
    @Config.Comment("配置每个维度使用的大气系统。注意，切换大气系统后原大气系统的数据可能丢失，建议提前备份。\n" +
            "Configure the atmosphere system for each dimension. ATTENTION: Changing of atmosphere system may cause data loss on old atmosphere system, and a backup is recommended.\n")
    @GeoConfig.KeyComment("维度ID Dimension ID")
    @GeoConfig.ValueComment("大气系统信息，为一个JSON对象。Atmosphere system information, which is a JSON object.\n" +
            "id - 当前维度采用的大气系统ID。The atmosphere system ID used by the current dimension.\n" +
            "   可选值 Available values:\n" +
            "   surface - 主世界大气系统，类似现实的地球大气系统 An Earth-like atmosphere system.\n" +
            "   vanilla - 原版大气系统，基于Minecraft原版的生物群系 An atmosphere system based on biomes.\n" +
            "   close - 地狱大气系统 Atmosphere system designed for Hall.\n" +
            "   第三方大气系统ID - 更改此值为第三方大气系统ID以使用第三方模组提供的大气系统. Change this value to a third-party atmosphere system ID to use atmosphere systems provided by third-party mods.\n" +
            "   none - 无大气系统. No atmosphere system.\n" +
            "不同大气系统有不同的配置项，请参阅相关文档获取详细信息。\n" +
            "Different atmosphere systems have different configuration items. Please refer to the relevant documentation for detailed information.")
    @Config.RequiresWorldRestart
    public static final ConfigMap<Integer, AtmosphereSystemInfo> ATMOSPHERE_SYSTEM_TYPES =
            new ConfigMap<Integer, AtmosphereSystemInfo>(CATEGORY_ATMOSPHERE,"customAtmosphereSystem",
                    Integer::parseInt,AtmosphereSystemInfo::new,
                    new ConfigEntry<>(0, SurfaceAtmosphereSystemInfo.create()
                            .waterEvaporate(true)
                            .waterFreeze(true)
                            .setRainSmoothingConstant(256)
                            .setVaporExchangeRate(1e-6)
                            .setMaxLoadDistance(100)),
                    new ConfigEntry<>(-1, CloseAtmosphereSystemInfo.create()
                            .setFinalTemperature(CloseAtmosphereSystem.HELL_TEMP)
                            .setPressure(2e5)
                            .setMaxWindSpeed(4)
                            .waterEvaporate(true)
                            .waterFreeze(false)
                            .setVaporExchangeRate(1e-5)
                            .setRainSmoothingConstant(Integer.MAX_VALUE)
                            .setMaxLoadDistance(100)),
                    new ConfigEntry<>(1, VanillaAtmosphereSystemInfo.create()
                            .setMaxWaterDrainedMultiplier(50000)
                            .setRainingCloudExponent(20)
                            .setThunderingCloudExponent(40)
                            .waterEvaporate(false)
                            .waterFreeze(false)
                            .setVaporExchangeRate(0)
                            .setRainSmoothingConstant(Integer.MAX_VALUE)
                            .setMaxLoadDistance(100))){
                @Override
                public void load(@Nonnull Configuration config) {
                    super.load(config);
                    GeoAtmosphereSetting.setAtmosphereSystemInfoMap(value);
                }
    }.setKeyClass(Integer.class).setValueClass(AtmosphereSystemInfo.class);

    @GeoConfig.Since("0.1")
    @Config.Comment("方块每1立方分米的热容，默认为2000，单位为FE/(dm^3·K),可以用 比热容*密度/1000 计算(国际标准单位)")
    @Config.RequiresMcRestart
    public static final ConfigMap<ConfigurableBlockState,Integer> SPECIFIC_HEAT_CAPACITIES =
            new ConfigMap<ConfigurableBlockState,Integer>(CATEGORY_ATMOSPHERE,"specificHeatCapacities",ConfigurableBlockState::getInstanceByString, Integer::parseInt,
                    //水
                    new BlockIntegerEntry("minecraft:water",4200),
                    new BlockIntegerEntry("minecraft:flowing_water",4200),
                    //石头
                    new BlockIntegerEntry("minecraft:stone",2600,new ConfigurableBlockProperty("variant","stone")),
                    new BlockIntegerEntry("minecraft:stone",2650,new ConfigurableBlockProperty("variant","granite")),
                    new BlockIntegerEntry("minecraft:stone",2650,new ConfigurableBlockProperty("variant","smooth_granite")),
                    new BlockIntegerEntry("minecraft:stone",2800,new ConfigurableBlockProperty("variant","diorite")),
                    new BlockIntegerEntry("minecraft:stone",2800,new ConfigurableBlockProperty("variant","smooth_diorite")),
                    new BlockIntegerEntry("minecraft:stone",2750,new ConfigurableBlockProperty("variant","andesite")),
                    new BlockIntegerEntry("minecraft:stone",2750,new ConfigurableBlockProperty("variant","smooth_andesite")),
                    new BlockIntegerEntry("minecraft:cobblestone",2600),
                    new BlockIntegerEntry("minecraft:mossy_cobblestone",2600),
                    new BlockIntegerEntry("minecraft:gold_ore",2600),
                    new BlockIntegerEntry("minecraft:iron_ore",2600),
                    new BlockIntegerEntry("minecraft:coal_ore",2600),
                    new BlockIntegerEntry("minecraft:lapis_ore",2600),
                    new BlockIntegerEntry("minecraft:diamond_ore",2600),
                    new BlockIntegerEntry("minecraft:stonebrick",2600),
                    new BlockIntegerEntry("minecraft:clay",2160),
                    new BlockIntegerEntry("minecraft:bedrock",3600),
                    //沙子、沙砾等颗粒较大物质
                    new BlockIntegerEntry("minecraft:gravel",1440),
                    new BlockIntegerEntry("minecraft:sand",1280),
                    new BlockIntegerEntry("minecraft:sandstone",2200),
                    new BlockIntegerEntry("minecraft:red_sandstone",2200),
                    //木板
                    new BlockIntegerEntry("minecraft:planks",900),
                    new BlockIntegerEntry("minecraft:log",900),
                    new BlockIntegerEntry("minecraft:log2",900),
                    //特殊建筑材料
                    new BlockIntegerEntry("minecraft:glass",2000),
                    new BlockIntegerEntry("minecraft:stained_glass",2000),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",2000),
                    new BlockIntegerEntry("minecraft:hardened_clay",2000),
                    new BlockIntegerEntry("minecraft:sea_lantern",2000),
                    new BlockIntegerEntry("minecraft:prismarine",1760),
                    new BlockIntegerEntry("minecraft:prismarine",1920,new ConfigurableBlockProperty("variant","dark_prismarine")),
                    new BlockIntegerEntry("minecraft:glowstone",2240),
                    new BlockIntegerEntry("minecraft:concrete",2300),
                    new BlockIntegerEntry("minecraft:concrete_powder",1700),
                    //土壤类
                    new BlockIntegerEntry("minecraft:dirt",1000,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","0")),
                    new BlockIntegerEntry("minecraft:dirt",1680,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","1")),
                    new BlockIntegerEntry("minecraft:dirt",2480,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","2")),
                    new BlockIntegerEntry("minecraft:dirt",3400,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","3")),
                    new BlockIntegerEntry("minecraft:dirt",4750,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","4")),
                    new BlockIntegerEntry("minecraft:dirt",1280,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","0")),
                    new BlockIntegerEntry("minecraft:dirt",1710,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","1")),
                    new BlockIntegerEntry("minecraft:dirt",2140,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","2")),
                    new BlockIntegerEntry("minecraft:dirt",2570,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","3")),
                    new BlockIntegerEntry("minecraft:dirt",3000,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","4")),
                    new BlockIntegerEntry("minecraft:dirt",1040,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","0")),
                    new BlockIntegerEntry("minecraft:dirt",1770,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","1")),
                    new BlockIntegerEntry("minecraft:dirt",2500,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","2")),
                    new BlockIntegerEntry("minecraft:dirt",3230,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","3")),
                    new BlockIntegerEntry("minecraft:dirt",3960,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","4")),
                    new BlockIntegerEntry("minecraft:myselium",1440),
                    new BlockIntegerEntry("minecraft:grass",1100,new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","0")),
                    new BlockIntegerEntry("minecraft:grass",1780,new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","1")),
                    new BlockIntegerEntry("minecraft:grass",2580,new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","2")),
                    new BlockIntegerEntry("minecraft:grass",3500,new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","3")),
                    new BlockIntegerEntry("minecraft:grass",4850,new ConfigurableBlockProperty("snowy","*"),new ConfigurableBlockProperty("humidity","4")),
                    //杂项
                    new BlockIntegerEntry("minecraft:sponge",390,new ConfigurableBlockProperty("wet","false")),
                    new BlockIntegerEntry("minecraft:sponge",4200,new ConfigurableBlockProperty("wet","true")),
                    new BlockIntegerEntry("minecraft:wool",390),
                    new BlockIntegerEntry("minecraft:pumpkin",2100),
                    new BlockIntegerEntry("minecraft:lit_pumpkin",2100),
                    new BlockIntegerEntry("minecraft:hay_block",260),
                    new BlockIntegerEntry("minecraft:melon_block",2660),
                    new BlockIntegerEntry("minecraft:cactus",600),
                    new BlockIntegerEntry("minecraft:slime",2375),
                    new BlockIntegerEntry("minecraft:redstone_lamp",2080),
                    new BlockIntegerEntry("minecraft:lit_redstone_lamp",2080),
                    new BlockIntegerEntry("minecraft:tnt",1260),
                    new BlockIntegerEntry("minecraft:leaves",720),
                    //冰雪
                    new BlockIntegerEntry("minecraft:ice",1932),
                    new BlockIntegerEntry("minecraft:snow",630),
                    new BlockIntegerEntry("minecraft:packed_ice",1932),
                    new BlockIntegerEntry("minecraft:snow_layer",78,new ConfigurableBlockProperty("layers","1")),
                    new BlockIntegerEntry("minecraft:snow_layer",158,new ConfigurableBlockProperty("layers","2")),
                    new BlockIntegerEntry("minecraft:snow_layer",236,new ConfigurableBlockProperty("layers","3")),
                    new BlockIntegerEntry("minecraft:snow_layer",315,new ConfigurableBlockProperty("layers","4")),
                    new BlockIntegerEntry("minecraft:snow_layer",394,new ConfigurableBlockProperty("layers","5")),
                    new BlockIntegerEntry("minecraft:snow_layer",473,new ConfigurableBlockProperty("layers","6")),
                    new BlockIntegerEntry("minecraft:snow_layer",551,new ConfigurableBlockProperty("layers","7")),
                    new BlockIntegerEntry("minecraft:snow_layer",630,new ConfigurableBlockProperty("layers","8")),
                    //单质、金属、压缩（存储）方块
                    new BlockIntegerEntry("minecraft:lapis_block",2240),
                    new BlockIntegerEntry("minecraft:gold_block",2489),
                    new BlockIntegerEntry("minecraft:iron_block",3533),
                    new BlockIntegerEntry("minecraft:brick_block",1600),
                    new BlockIntegerEntry("minecraft:obsidian",3000),
                    new BlockIntegerEntry("minecraft:diamond_block",1806),
                    new BlockIntegerEntry("minecraft:emerald_block",1160),
                    new BlockIntegerEntry("minecraft:quartz_block",1160),
                    new BlockIntegerEntry("minecraft:coal_block",1300),
                    new BlockIntegerEntry("minecraft:bone_block",1400),
                    new BlockIntegerEntry("minecraft:redstone_block",2080),
                    //末地项目
                    new BlockIntegerEntry("minecraft:end_stone",2800),
                    new BlockIntegerEntry("minecraft:end_bricks",2800),
                    new BlockIntegerEntry("minecraft:purpur_block",1440),
                    new BlockIntegerEntry("minecraft:purpur_pillar",1440),
                    new BlockIntegerEntry("minecraft:white_shulker_box",600),
                    new BlockIntegerEntry("minecraft:orange_shulker_box",600),
                    new BlockIntegerEntry("minecraft:magenta_shulker_box",600),
                    new BlockIntegerEntry("minecraft:light_blue_shulker_box",600),
                    new BlockIntegerEntry("minecraft:yellow_shulker_box",600),
                    new BlockIntegerEntry("minecraft:lime_shulker_box",600),
                    new BlockIntegerEntry("minecraft:pink_shulker_box",600),
                    new BlockIntegerEntry("minecraft:gray_shulker_box",600),
                    new BlockIntegerEntry("minecraft:silver_shulker_box",600),
                    new BlockIntegerEntry("minecraft:cyan_shulker_box",600),
                    new BlockIntegerEntry("minecraft:purple_shulker_box",600),
                    new BlockIntegerEntry("minecraft:blue_shulker_box",600),
                    new BlockIntegerEntry("minecraft:brown_shulker_box",600),
                    new BlockIntegerEntry("minecraft:green_shulker_box",600),
                    new BlockIntegerEntry("minecraft:red_shulker_box",600),
                    new BlockIntegerEntry("minecraft:black_shulker_box",600),
                    //地狱项目
                    new BlockIntegerEntry("minecraft:nether_wart_block",1700),
                    new BlockIntegerEntry("minecraft:magma",3000),
                    new BlockIntegerEntry("minecraft:netherrack",2200),
                    new BlockIntegerEntry("minecraft:quartz_ore",2200),
                    new BlockIntegerEntry("minecraft:red_nether_brick",2400),
                    new BlockIntegerEntry("minecraft:nether_brick",2400),
                    new BlockIntegerEntry("minecraft:soul_sand",960),
                    //其他
                    new BlockIntegerEntry("minecraft:air",200)){
                @Override
                public void load(@Nonnull final Configuration config) {
                    super.load(config);
                    for(@Nonnull final Map.Entry<ConfigurableBlockState,Integer> entry: getValue().entrySet()){
                        GeoBlockSetting.setBlockHeatCapacity(entry.getKey(),entry.getValue());
                    }
                }
            };

    @GeoConfig.Since("0.1")
    @Config.Comment("下垫面方块的反射率，默认为12%，单位为%")
    @Config.RequiresMcRestart
    public static final ConfigMap<ConfigurableBlockState,Integer> UNDERLYING_REFLECTIVITY =
            new ConfigMap<ConfigurableBlockState,Integer>(CATEGORY_ATMOSPHERE,"underlyingReflectivity", ConfigurableBlockState::getInstanceByString,Integer::parseInt,
                    //水
                    new BlockIntegerEntry("minecraft:water",5),
                    new BlockIntegerEntry("minecraft:flowing_water",3),
                    //石头
                    new BlockIntegerEntry("minecraft:stone",8,new ConfigurableBlockProperty("variant","stone")),
                    new BlockIntegerEntry("minecraft:stone",15,new ConfigurableBlockProperty("variant","granite")),
                    new BlockIntegerEntry("minecraft:stone",25,new ConfigurableBlockProperty("variant","smooth_granite")),
                    new BlockIntegerEntry("minecraft:stone",22,new ConfigurableBlockProperty("variant","diorite")),
                    new BlockIntegerEntry("minecraft:stone",30,new ConfigurableBlockProperty("variant","smooth_diorite")),
                    new BlockIntegerEntry("minecraft:stone",12,new ConfigurableBlockProperty("variant","andesite")),
                    new BlockIntegerEntry("minecraft:stone",20,new ConfigurableBlockProperty("variant","smooth_andesite")),
                    new BlockIntegerEntry("minecraft:cobblestone",5),
                    new BlockIntegerEntry("minecraft:mossy_cobblestone",4),
                    new BlockIntegerEntry("minecraft:gold_ore",11),
                    new BlockIntegerEntry("minecraft:iron_ore",11),
                    new BlockIntegerEntry("minecraft:coal_ore",11),
                    new BlockIntegerEntry("minecraft:lapis_ore",11),
                    new BlockIntegerEntry("minecraft:diamond_ore",11),
                    new BlockIntegerEntry("minecraft:stonebrick",13),
                    new BlockIntegerEntry("minecraft:stonebrick",8,new ConfigurableBlockProperty("variant","cracked_stonebrick")),
                    new BlockIntegerEntry("minecraft:stonebrick",10,new ConfigurableBlockProperty("variant","chiseled_stonebrick")),
                    new BlockIntegerEntry("minecraft:clay",17),
                    new BlockIntegerEntry("minecraft:bedrock",0),
                    //沙子、沙砾等颗粒较大物质
                    new BlockIntegerEntry("minecraft:gravel",12),
                    new BlockIntegerEntry("minecraft:sand",20,new ConfigurableBlockProperty("variant","sand"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:sand",23,new ConfigurableBlockProperty("variant","red_sand"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:sandstone",30),
                    new BlockIntegerEntry("minecraft:red_sandstone",32,new ConfigurableBlockProperty("type","red_sandstone")),
                    new BlockIntegerEntry("minecraft:red_sandstone",28,new ConfigurableBlockProperty("type","chiseled_red_sandstone")),
                    new BlockIntegerEntry("minecraft:red_sandstone",34,new ConfigurableBlockProperty("type","smooth_red_sandstone")),
                    //木板
                    new BlockIntegerEntry("minecraft:planks",8),
                    new BlockIntegerEntry("minecraft:log",6),
                    new BlockIntegerEntry("minecraft:log2",6),
                    //特殊建筑材料
                    new BlockIntegerEntry("minecraft:glass",8),
                    new BlockIntegerEntry("minecraft:brick_block",20),
                    new BlockIntegerEntry("minecraft:stained_glas",17),
                    new BlockIntegerEntry("minecraft:stained_glas",8,new ConfigurableBlockProperty("color","white")),
                    new BlockIntegerEntry("minecraft:stained_glas",10,new ConfigurableBlockProperty("color","pink")),
                    new BlockIntegerEntry("minecraft:stained_glas",10,new ConfigurableBlockProperty("color","lightBlue")),
                    new BlockIntegerEntry("minecraft:stained_glas",10,new ConfigurableBlockProperty("color","yellow")),
                    new BlockIntegerEntry("minecraft:stained_glas",10,new ConfigurableBlockProperty("color","lime")),
                    new BlockIntegerEntry("minecraft:stained_glas",13,new ConfigurableBlockProperty("color","magenta")),
                    new BlockIntegerEntry("minecraft:stained_glas",13,new ConfigurableBlockProperty("color","orange")),
                    new BlockIntegerEntry("minecraft:stained_glas",20,new ConfigurableBlockProperty("color","black")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",20),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",65,new ConfigurableBlockProperty("color","white")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",50,new ConfigurableBlockProperty("color","pink")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",45,new ConfigurableBlockProperty("color","lightBlue")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",45,new ConfigurableBlockProperty("color","yellow")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",45,new ConfigurableBlockProperty("color","lime")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",30,new ConfigurableBlockProperty("color","magenta")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",30,new ConfigurableBlockProperty("color","orange")),
                    new BlockIntegerEntry("minecraft:stained_hardened_clay",10,new ConfigurableBlockProperty("color","black")),
                    new BlockIntegerEntry("minecraft:hardened_clay",20),
                    new BlockIntegerEntry("minecraft:sea_lantern",10),
                    new BlockIntegerEntry("minecraft:prismarine",15),
                    new BlockIntegerEntry("minecraft:prismarine",18,new ConfigurableBlockProperty("variant","prismarine_bricks")),
                    new BlockIntegerEntry("minecraft:prismarine",5,new ConfigurableBlockProperty("variant","dark_prismarine")),
                    new BlockIntegerEntry("minecraft:glowstone",10),
                    new BlockIntegerEntry("minecraft:concrete",15),
                    new BlockIntegerEntry("minecraft:concrete",50,new ConfigurableBlockProperty("color","white")),
                    new BlockIntegerEntry("minecraft:concrete",35,new ConfigurableBlockProperty("color","pink")),
                    new BlockIntegerEntry("minecraft:concrete",35,new ConfigurableBlockProperty("color","lightBlue")),
                    new BlockIntegerEntry("minecraft:concrete",35,new ConfigurableBlockProperty("color","yellow")),
                    new BlockIntegerEntry("minecraft:concrete",35,new ConfigurableBlockProperty("color","lime")),
                    new BlockIntegerEntry("minecraft:concrete",30,new ConfigurableBlockProperty("color","magenta")),
                    new BlockIntegerEntry("minecraft:concrete",25,new ConfigurableBlockProperty("color","orange")),
                    new BlockIntegerEntry("minecraft:concrete",8,new ConfigurableBlockProperty("color","black")),
                    new BlockIntegerEntry("minecraft:concrete_powder",10),
                    new BlockIntegerEntry("minecraft:concrete_powder",40,new ConfigurableBlockProperty("color","white")),
                    new BlockIntegerEntry("minecraft:concrete_powder",30,new ConfigurableBlockProperty("color","pink")),
                    new BlockIntegerEntry("minecraft:concrete_powder",25,new ConfigurableBlockProperty("color","lightBlue")),
                    new BlockIntegerEntry("minecraft:concrete_powder",25,new ConfigurableBlockProperty("color","yellow")),
                    new BlockIntegerEntry("minecraft:concrete_powder",25,new ConfigurableBlockProperty("color","lime")),
                    new BlockIntegerEntry("minecraft:concrete_powder",20,new ConfigurableBlockProperty("color","magenta")),
                    new BlockIntegerEntry("minecraft:concrete_powder",15,new ConfigurableBlockProperty("color","orange")),
                    new BlockIntegerEntry("minecraft:concrete_powder",2,new ConfigurableBlockProperty("color","black")),
                    //土壤类
                    new BlockIntegerEntry("minecraft:dirt",10,new ConfigurableBlockProperty("variant","dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:dirt",11,new ConfigurableBlockProperty("variant","coarse_dirt"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:dirt",8,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","false"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:dirt",60,new ConfigurableBlockProperty("variant","podzol"),new ConfigurableBlockProperty("snowy","true"),new ConfigurableBlockProperty("humidity","*")),
                    new BlockIntegerEntry("minecraft:myselium",5),
                    new BlockIntegerEntry("minecraft:grass",7), //注意之后添加水分后要改
                    //杂项
                    new BlockIntegerEntry("minecraft:sponge",20,new ConfigurableBlockProperty("wet","false")),
                    new BlockIntegerEntry("minecraft:sponge",5,new ConfigurableBlockProperty("wet","true")),
                    new BlockIntegerEntry("minecraft:wool",20),
                    new BlockIntegerEntry("minecraft:wool",55,new ConfigurableBlockProperty("color","white")),
                    new BlockIntegerEntry("minecraft:wool",40,new ConfigurableBlockProperty("color","pink")),
                    new BlockIntegerEntry("minecraft:wool",40,new ConfigurableBlockProperty("color","lightBlue")),
                    new BlockIntegerEntry("minecraft:wool",40,new ConfigurableBlockProperty("color","yellow")),
                    new BlockIntegerEntry("minecraft:wool",40,new ConfigurableBlockProperty("color","lime")),
                    new BlockIntegerEntry("minecraft:wool",30,new ConfigurableBlockProperty("color","magenta")),
                    new BlockIntegerEntry("minecraft:wool",25,new ConfigurableBlockProperty("color","orange")),
                    new BlockIntegerEntry("minecraft:wool",7,new ConfigurableBlockProperty("color","gray")),
                    new BlockIntegerEntry("minecraft:wool",1,new ConfigurableBlockProperty("color","black")),
                    new BlockIntegerEntry("minecraft:pumpkin",20),
                    new BlockIntegerEntry("minecraft:lit_pumpkin",10),
                    new BlockIntegerEntry("minecraft:hay_block",5),
                    new BlockIntegerEntry("minecraft:melon_block",20),
                    new BlockIntegerEntry("minecraft:slime",20),
                    new BlockIntegerEntry("minecraft:redstone_lamp",10),
                    new BlockIntegerEntry("minecraft:lit_redstone_lamp",5),
                    new BlockIntegerEntry("minecraft:leaves",5),
                    //冰雪
                    new BlockIntegerEntry("minecraft:ice",50),
                    new BlockIntegerEntry("minecraft:snow",80),
                    new BlockIntegerEntry("minecraft:packed_ice",25),
                    new BlockIntegerEntry("minecraft:snow_layer",80),
                    //单质、金属、压缩（存储）方块
                    new BlockIntegerEntry("minecraft:lapis_block",8),
                    new BlockIntegerEntry("minecraft:gold_block",50),
                    new BlockIntegerEntry("minecraft:iron_block",60),
                    new BlockIntegerEntry("minecraft:obsidian",3),
                    new BlockIntegerEntry("minecraft:diamond_block",21),
                    new BlockIntegerEntry("minecraft:emerald_block",15),
                    new BlockIntegerEntry("minecraft:quartz_block",70,new ConfigurableBlockProperty("variant","default")),
                    new BlockIntegerEntry("minecraft:quartz_block",45),
                    new BlockIntegerEntry("minecraft:coal_block",0),
                    new BlockIntegerEntry("minecraft:bone_block",70),
                    new BlockIntegerEntry("minecraft:redstone_block",10),
                    //末地项目
                    new BlockIntegerEntry("minecraft:end_stone",30),
                    new BlockIntegerEntry("minecraft:end_bricks",40),
                    new BlockIntegerEntry("minecraft:purpur_block",20),
                    new BlockIntegerEntry("minecraft:purpur_pillar",15),
                    new BlockIntegerEntry("minecraft:white_shulker_box",40),
                    new BlockIntegerEntry("minecraft:orange_shulker_box",35),
                    new BlockIntegerEntry("minecraft:magenta_shulker_box",30),
                    new BlockIntegerEntry("minecraft:light_blue_shulker_box",35),
                    new BlockIntegerEntry("minecraft:yellow_shulker_box",35),
                    new BlockIntegerEntry("minecraft:lime_shulker_box",35),
                    new BlockIntegerEntry("minecraft:pink_shulker_box",35),
                    new BlockIntegerEntry("minecraft:gray_shulker_box",20),
                    new BlockIntegerEntry("minecraft:silver_shulker_box",35),
                    new BlockIntegerEntry("minecraft:cyan_shulker_box",30),
                    new BlockIntegerEntry("minecraft:purple_shulker_box",35),
                    new BlockIntegerEntry("minecraft:blue_shulker_box",25),
                    new BlockIntegerEntry("minecraft:brown_shulker_box",20),
                    new BlockIntegerEntry("minecraft:green_shulker_box",25),
                    new BlockIntegerEntry("minecraft:red_shulker_box",20),
                    new BlockIntegerEntry("minecraft:black_shulker_box",5),
                    //地狱项目
                    new BlockIntegerEntry("minecraft:nether_wart_block",5),
                    new BlockIntegerEntry("minecraft:netherrack",10),
                    new BlockIntegerEntry("minecraft:quartz_ore",11),
                    new BlockIntegerEntry("minecraft:nether_brick",15),
                    new BlockIntegerEntry("minecraft:soul_sand",5),
                    //其他
                    new BlockIntegerEntry("minecraft:air",1)
            ){
                @Override
                public void load(@Nonnull Configuration config) {
                    super.load(config);
                    for(Map.Entry<ConfigurableBlockState,Integer> entry: getValue().entrySet()){
                        GeoBlockSetting.setBlockReflectivity(entry.getKey(),entry.getValue());
                    }
                }
            };

    @GeoConfig.Since("0.1")
    @Config.Comment("是否允许炼药锅接无限量的水，即在接水时不会消耗大气水")
    public static final ConfigBoolean ALLOW_CAULDRON_GET_INFINITE_WATER = new ConfigBoolean(CATEGORY_ATMOSPHERE,"allowCauldronGetInfiniteWater", false);

    @Config.RangeInt(min = 2)
    @GeoConfig.Since("0.1")
    @Config.Comment("大气重新计算下垫面性质的间隔时间，单位为大气刻")
    @Config.RequiresMcRestart
    public static final ConfigInteger ATMOSPHERE_UNDERLYING_RECALCULATE_GAP = new ConfigInteger(CATEGORY_ATMOSPHERE, "atmosphereUnderlyingRecalculateGap",400){
        @Override
        public void load(@Nonnull final Configuration config) {
            super.load(config);
            GeoAtmosphereSetting.setUnderlyingReloadGap(value);
        }

        @Override
        public void setValue(@Nonnull final Integer newValue) {
            super.setValue(newValue);
            GeoAtmosphereSetting.setUnderlyingReloadGap(value);
        }
    };
}
