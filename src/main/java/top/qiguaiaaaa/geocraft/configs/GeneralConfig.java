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
import top.qiguaiaaaa.geocraft.api.configs.item.collection.list.ConfigDoubleList;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.list.ConfigIntegerList;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigInteger;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.ConfigurableList;

@SuppressWarnings("unused")
public final class GeneralConfig {

    public static final ConfigCategory GENERAL = new ConfigCategory(Configuration.CATEGORY_GENERAL);

    @Config.RequiresMcRestart
    @Config.Comment("在区块生成的时候阻止方块下落，这可能可以防止一部分的因流体大量下落造成的卡顿。\n" +
            "Prevent blocks from falling during chunk generation, which may help reduce lag caused by massive fluid updates.")
    @Config.LangKey("geocraft.config.comment.general.prevent_falling_block_while_generation")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean PREVENT_FALLING_BLOCK_FROM_FALLING_WHILE_GENERATION =
            new ConfigBoolean(GENERAL, "preventBlockFromFallingDuringGeneration",true);

    @Config.RequiresMcRestart
    @GeoConfig.Since("0.1")
    @Config.Comment("是否允许客户端读取模组拓展的数据。默认为禁止。在禁止状态下，模组将会对服务器和客户端的网络通信进行修改，以去除无法被原版客户端正确识别的数据，例如泥土的湿度信息。其原理和反矿透原理类似。\n" +
            "如果您遇到兼容性问题，想要禁止mod对网络通信进行修改，可以更改此选项为true。这样子，mod将不再修改网络通信，您可以使用其他更专业的mod以阻止客户端获取这些拓展信息，以提供对原版客户端的兼容性。\n" +
            "请注意,允许客户端阅读拓展数据后,若客户端没有安装此模组,对于土壤相关方块(比如灰化土)或雪的显示可能出现异常.您可以通过其他具有修改网络通信功能的模组来避免此问题,或禁止未安装该模组的客户端连接,或放着不管（。\n" +
            "该配置在客户端无效。\n" +
            "Whether to allow the client to read mod-extended data. Default is disabled. When disabled, the mod will modify network communication between the server and client to remove data that cannot be correctly interpreted by vanilla clients, such as soil moisture information. The principle is similar to anti-X-ray mechanisms.\n" +
            "If you encounter compatibility issues and wish to disable the mod's network modifications, you can change this option to true. In this case, the mod will no longer modify network communication, and you can use other more specialized mods to prevent clients from accessing this extended data, thereby providing compatibility with vanilla clients.\n" +
            "Please note that after enabling client access to extended data, if the client does not have this mod installed, the display of soil-related blocks (such as podzol) or snow may appear abnormal. You can address this by using other mods with network modification capabilities, prohibiting connections from clients without this mod, or simply leaving it as is.\n" +
            "This configuration item does noting to Client Side.")
    public static final ConfigBoolean COMPATIBLE_FOR_VANILLA_CLIENT = new ConfigBoolean(GENERAL, "compatibilityForVanillaClient",true);

    //*********************
    // Block Updater
    //*********************

    public static final ConfigCategory CATEGORY_BLOCK_UPDATER = GENERAL.getChildCategory("block_updater");

    @Config.Comment("开启BlockUpdater以在1游戏刻内提供额外的方块更新额度和更精细的更新管理\n" +
            "Enable BlockUpdater to provide additional block update quotas and more refined update management within a single game tick.")
    @Config.LangKey("geocraft.config.comment.general.enable_block_updater")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean ENABLE_BLOCK_UPDATER =
            new ConfigBoolean(CATEGORY_BLOCK_UPDATER, "enableBlockUpdater",true);

    @Config.RangeInt(min = 1)
    @Config.Comment("天圆地方内置的附加方块更新器在一游戏刻内最多更新的方块数量，多余的更新任务会被忽略。\n" +
            "The max number of blocks to update by Block Updater inside GeoCraft. The excess part will be ignored.")
    @GeoConfig.Since("0.1")
    public static final ConfigInteger BLOCK_UPDATER_MAX_UPDATES_BLOCK =
            new ConfigInteger(CATEGORY_BLOCK_UPDATER, "maxUpdateBlocksPerTick",65536*4);

    @Config.RangeInt(min = 1)
    @Deprecated
    @GeoConfig.Since("0.1")
    @Config.Comment("[!!!自0.2.0-alpha.2版本开始弃用!!!]BlockUpdater清理并丢弃过期的更新任务的时间间隔，单位为游戏刻，以防止在极端卡顿下的任务堆积造成的内存泄漏。\n" +
            "The time interval for BlockUpdater to clean up and discard expired update tasks, measured in game ticks, which prevents memory leaks caused by task accumulation during severe server lag.")
    public static final ConfigInteger BLOCK_UPDATER_CLEAN_PERIOD = new ConfigInteger(CATEGORY_BLOCK_UPDATER, "cleaningPeriod",200);

    @Config.RangeInt(min = -1)
    @GeoConfig.Since("0.1")
    @Config.Comment("BlockUpdater在1游戏刻内的最大耗时，当用时超过该阈值时，将会推迟剩余任务更新到下一游戏刻。设为-1以禁用时间限制。\n" +
            "The maximum processing time allowed for BlockUpdater within a single game tick. When the processing time exceeds this threshold, non-compliant update tasks will be discarded. Set it to -1 to disable this function.")
    public static final ConfigInteger BLOCK_UPDATER_MAX_TIME_USAGE = new ConfigInteger(CATEGORY_BLOCK_UPDATER, "maxTimeUsage",200);

    @Deprecated
    @GeoConfig.Since("0.1")
    @Config.Comment("[!!!自0.2.0-alpha.2版本开始弃用!!!]当BlockUpdater达到更新的时间限制时，仅允许动态流体方块更新。因为这些流体方块的更新方法的作用一般是向流体更新器提交更新任务，不会有太大开销。\n" +
            "When the BlockUpdater reaches its time limit for updates, only dynamic fluid block updates are permitted. This is because the primary function of these fluid block update methods is typically to submit update tasks to the FluidUpdateManager.")
    public static final ConfigBoolean ALLOW_DYNAMIC_FLUID_UPDATE =
            new ConfigBoolean(CATEGORY_BLOCK_UPDATER, "allowDynamicFluidToUpdate",true);

    @Config.Ignore
    @Config.Comment("按距离最近玩家距离从进到远更新方块。\n" +
            "Update blocks in order of proximity to the nearest player.")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean SORT_UPDATE_TASKS_BY_DISTANCE_TO_PLAYERS =
            new ConfigBoolean(CATEGORY_BLOCK_UPDATER, "sortTasksByDistanceToPlayers",false);

    //*********************
    // Performance
    //*********************

    @Config.Comment({
            "模组性能调整。注意这里的“警告”是对游戏内的逻辑来说，不会真的打印日志。",
            "Mod Performance Tuning"})
    public static final ConfigCategory CATEGORY_PERFORMANCE = GENERAL.getChildCategory("performance");

    @Config.Comment("开启延迟警告。")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean ENABLE_PERFORMANCE_WARNING =
            new ConfigBoolean(CATEGORY_PERFORMANCE, "enableWarning",true);

    @Config.Comment("开启延迟检测。")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean ENABLE_PERFORMANCE_DELAY_DETECT =
            new ConfigBoolean(CATEGORY_PERFORMANCE, "enableDelayDetect",true);

    @Config.Comment("开启基于单游戏刻的延迟检测。默认关闭，开启意味着更加激进的优化。\n" +
            "Enable single-tick based lag detection. Default is disabled; enabling this means more aggressive optimization.")
    @GeoConfig.Since("0.1")
    public static final ConfigBoolean ENABLE_SINGLE_TICK_DELAY_DETECT =
            new ConfigBoolean(CATEGORY_PERFORMANCE, "enableSingleTickDelayDetect",false);

    @Config.RangeInt(min = 0)
    @Config.Comment("在一游戏刻内，从游戏刻开始保证不会触发延迟警报的最大时长。越大意味着在大规模方块更新时的卡顿加剧，但可以减少一些奇怪的问题，例如水浮在空中不掉下来。\n" +
            "Within a single game tick, the maximum guaranteed duration from the start of the tick that will not trigger a lag alert. A higher value means increased stuttering during large-scale block updates, but can resolve certain peculiar issues, such as water hovering in mid-air without falling.")
    @GeoConfig.Since("0.1")
    public static final ConfigInteger PROTECT_TIME = new ConfigInteger(CATEGORY_PERFORMANCE, "protectTime",30);

    @Config.RangeDouble(min = 0,max = 1)
    @Config.Comment("监测游戏刻时长时采样的第k百分位数，从第一个元素到第三个元素分别表示32刻、256刻和1024刻内用于判断服务器是否延迟的游戏刻时长的第k百分位数。\n" +
            "The k-th percentile of sampled game tick durations, where the first to third elements respectively represent the k-th percentile of game tick duration within 32, 256, and 1024 ticks used to determine server lag.")
    @GeoConfig.Fixed
    @GeoConfig.Since("0.1")
    public static final ConfigDoubleList<?> PERFORMANCE_SAMPLING_TICK_PERCENTILE =
            ConfigDoubleList.create(CATEGORY_PERFORMANCE, "performanceSamplingTickPercentile",new ConfigurableList<>(0.5,0.5,0.5));

    @Config.RangeInt(min = 1)
    @GeoConfig.Fixed
    @GeoConfig.Since("0.1")
    public static final ConfigIntegerList<?> TICK_DELAY_WARNING_THRESHOLDS =
            ConfigIntegerList.create(CATEGORY_PERFORMANCE,"tickDelayWarningThresholds",new ConfigurableList<>(300,200,150,100))
                    .setComment("单游戏刻、32、256、1024游戏刻内统计的第"+PERFORMANCE_SAMPLING_TICK_PERCENTILE.getPath()+"位百分位数时长超过该阈值时，将会触发服务端延迟警报，模组将会尝试减少一些次要运算以优化性能。\n" +
                            "When the " + PERFORMANCE_SAMPLING_TICK_PERCENTILE.getPath() + "th percentile duration recorded within a single game tick, 32, 256, or 1024 game ticks exceeds this threshold, a server delay alert will be triggered, and the mod will attempt to reduce non-essential operations to optimize performance.");

    @Config.RangeInt(min = 1)
    @GeoConfig.Fixed
    @GeoConfig.Since("0.1")
    public static final ConfigIntegerList<?> TICK_DELAY_THRESHOLD =
            ConfigIntegerList.create(CATEGORY_PERFORMANCE, "tickDelayThresholds",new ConfigurableList<>(500,300,200,150))
                    .setComment("单游戏刻、32、256、1024游戏刻内统计的第"+PERFORMANCE_SAMPLING_TICK_PERCENTILE.getPath()+"位百分位数时长超过该阈值时，将会被认为是服务器延迟，模组将会尝试进一步减少一些次要运算以优化性能。\n" +
                            "When the " + PERFORMANCE_SAMPLING_TICK_PERCENTILE.getPath() + "th percentile duration recorded within a single game tick, 32, 256, or 1024 game ticks exceeds this threshold, it will be identified as server lag, and the mod will attempt to further reduce non-essential operations to optimize performance.");
}
