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

package top.qiguaiaaaa.geocraft.command;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import org.apache.logging.log4j.Logger;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoCraftProperties;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.UnderlyingLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.atmosphere.tracker.InformationLoggingTracker;
import top.qiguaiaaaa.geocraft.api.command.NumberType;
import top.qiguaiaaaa.geocraft.api.command.builder.CommandBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.INodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.CommandRunFunction;
import top.qiguaiaaaa.geocraft.api.command.builder.execute.RelayExecuteNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.node.ISmartNode;
import top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft.DimensionNode;
import top.qiguaiaaaa.geocraft.api.configs.value.minecraft.ConfigurableBlockState;
import top.qiguaiaaaa.geocraft.api.property.FluidProperty;
import top.qiguaiaaaa.geocraft.api.property.GeographyProperty;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;
import top.qiguaiaaaa.geocraft.api.setting.GeoBlockSetting;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.state.GeographyState;
import top.qiguaiaaaa.geocraft.api.state.TemperatureState;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.io.FileLogger;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.geography.atmosphere.SurfaceAtmosphere;
import top.qiguaiaaaa.geocraft.geography.atmosphere.layer.surface.SurfaceUnderlying;
import top.qiguaiaaaa.geocraft.geography.atmosphere.tracker.FluidTracker;
import top.qiguaiaaaa.geocraft.geography.atmosphere.tracker.TemperatureTracker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static top.qiguaiaaaa.geocraft.api.command.Nodes.*;

public final class CommandAtmosphere {
    public static final String ATMOSPHERE_COMMAND_NAME = "atmosphere";
    public static final String PERMISSION_NODE = "qg.geocraft.command.atmosphere";
    public static final List<String> ALIASES = new ArrayList<>(Collections.singleton("大气"));

    static final Map<String, BiConsumer<AtmosphereCommandContext,Double>> SetConsumer = new HashMap<>();
    static final Map<String, BiConsumer<AtmosphereCommandContext,Double>> AddConsumer = new HashMap<>();
    static final Map<String, Consumer<AtmosphereCommandContext>> QueryConsumer = new HashMap<>();
    static boolean inited = false;

    public static void init(){
        if(inited) return;
        initSet();
        initAdd();
        initQuery();
        inited = true;
    }

    private static void initSet(){
        SetConsumer.put("steam",(context,value)->{
            final FluidState steam = (context.layer instanceof AtmosphereLayer)?((AtmosphereLayer)context.layer).getSteam():null;
            if(steam == null){
                context.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            steam.setAmount(value.intValue());
            context.execute.notifyCommandListener("geocraft.command.atmosphere.set.steam", context.x, context.y, context.z, value.intValue());
        });
        SetConsumer.put("water",(context,value)->{
            final FluidState water = context.layer.getWater();
            if(water == null){
                context.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            water.setAmount(value.intValue());
            context.execute.notifyCommandListener("geocraft.command.atmosphere.set.water", context.x,context.y,context.z,value.intValue());
        });
        SetConsumer.put("temp",(context,value)->{
            context.layer.getTemperature().set(value.floatValue());
            context.execute.notifyCommandListener("geocraft.command.atmosphere.set.temp",context.x,context.y,context.z, value.floatValue());
        });
        SetConsumer.put("debug",(context,value)->{
            if(!(context.atmosphere instanceof SurfaceAtmosphere)){
                context.exception = new CommandException("geocraft.command.atmosphere.unknown");
                return;
            }
            final SurfaceAtmosphere a = (SurfaceAtmosphere) context.atmosphere;
            a.setDebug(value>0);
            if(value>0) context.execute.notifyCommandListener("geocraft.command.atmosphere.set.debug",context.x,context.z,a.isDebug());
        });
    }

    private static void initAdd(){
        AddConsumer.put("steam",(context,value)->{
            final FluidState steam = (context.layer instanceof AtmosphereLayer)?((AtmosphereLayer)context.layer).getSteam():null;
            if(steam == null){
                context.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            if(steam.fill(value.intValue(),true) == 0){
                context.exception = new NumberInvalidException("commands.generic.num.tooSmall", value, -steam.getFluidAmount());
                return;
            }
            context.execute.notifyCommandListener("geocraft.command.atmosphere.add.water",context.x,context.y,context.z,steam);
        });
        AddConsumer.put("water",(context,value)->{
            final FluidState water = context.layer.getWater();
            if(water == null){
                context.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            if(water.fill(value.intValue(),true) == 0){
                context.exception = new NumberInvalidException("commands.generic.num.tooSmall", value, -water.getFluidAmount());
                return;
            }
            context.execute.notifyCommandListener("geocraft.command.atmosphere.add.water",context.x,context.y,context.z,water);
        });
        AddConsumer.put("temp",(context,value)->{
            context.layer.getTemperature().add(value);
            context.execute.notifyCommandListener("geocraft.command.atmosphere.add.temp",context.x,context.y,context.z, context.layer.getTemperature());
        });
        AddConsumer.put("heat",(context,value)->{
            context.layer.putHeat(value,context.pos);
            context.execute.notifyCommandListener("geocraft.command.atmosphere.add.temp",context.x,context.y,context.z, context.layer.getTemperature());
        });
    }

    private static void initQuery(){
        QueryConsumer.put("block_temp",context->{
            context.accessor.setNotAir(context.execute.getWorld().getBlockState(context.pos).getMaterial() != Material.AIR);
            final double temp = context.accessor.getTemperature();
            context.execute.notifyCommandListener("geocraft.command.atmosphere.query.block_temp",context.x,context.y,context.z,temp);
            context.execute.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int)temp);
        });
        QueryConsumer.put("steam",ctx->{
            final FluidState steam = (ctx.layer instanceof AtmosphereLayer)?((AtmosphereLayer)ctx.layer).getSteam():null;
            if(steam == null){
                ctx.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.steam",ctx.x,ctx.y,ctx.z,steam);
            ctx.execute.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,steam.getFluidAmount());
        });
        QueryConsumer.put("water",ctx->{
            final FluidState water = ctx.layer.getWater();
            if(water == null){
                ctx.exception = new CommandException("geocraft.command.atmosphere.property.null");
                return;
            }
            ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.water",ctx.x,ctx.y,ctx.z,water);
            ctx.execute.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,water.getFluidAmount());
        });
        QueryConsumer.put("temp",ctx-> ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.temp",ctx.x,ctx.y,ctx.z,ctx.layer.getTemperature()));
        QueryConsumer.put("ground_temp",ctx->{
            final TemperatureState state = ctx.atmosphere.getUnderlying(ctx.pos).getTemperature();
            ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.ground_temp", ctx.x, ctx.z, state);
            ctx.execute.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int) state.get());
        });
        QueryConsumer.put("wind",ctx->{
            final Vec3d wind = ctx.accessor.getWind();
            for(EnumFacing facing:EnumFacing.VALUES){
                ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.wind."+facing.getName(), ctx.x,ctx.y, ctx.z, wind.dotProduct(new Vec3d(facing.getDirectionVec())));
            }
        });
        QueryConsumer.put("underlying",ctx->{
            final UnderlyingLayer underlying1 = ctx.atmosphere.getUnderlying(ctx.pos);
            final SurfaceUnderlying underlying;
            if(underlying1 instanceof SurfaceUnderlying){
                underlying = (SurfaceUnderlying) underlying1;
            }else{
                ctx.exception = new CommandException("geocraft.command.atmosphere.unknown_underlying");
                return;
            }
            ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.underlying",
                    ctx.x, underlying.getAltitude(), ctx.z,
                    underlying.getHeatCapacity(),
                    underlying.平均返照率);
        });
        QueryConsumer.put("heat_volume",ctx->{
            ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.heat_volume",ctx.x,ctx.y,ctx.z,ctx.layer.getHeatCapacity());
            ctx.execute.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int) ctx.layer.getHeatCapacity());
        });
        QueryConsumer.put("water_pressure",ctx-> ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.water_pressure",ctx.x,ctx.y,ctx.z,ctx.accessor.getWaterPressure()*0.01));
        QueryConsumer.put("pressure",ctx-> ctx.execute.notifyCommandListener("geocraft.command.atmosphere.query.pressure",ctx.x,ctx.y,ctx.z,ctx.accessor.getPressure()*0.01));
    }

    @Nonnull
    public static ICommand create(@Nonnull final MinecraftServer server){
        init();
        return new CommandBuilder(ATMOSPHERE_COMMAND_NAME)
                .require(2)
                .requirePlayer(true)
                .require(PERMISSION_NODE).allow(DefaultPermissionLevel.OP).register()
                .addAlias(server.isSinglePlayer() && FMLCommonHandler.instance().getSide() == Side.CLIENT? "zh_cn".equals(
                        Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode()
                )?ALIASES:Collections.emptyList():Collections.emptyList())
                .smart()
                .append(buildSetCommand()).done()
                .append(buildStopCommand()).done()
                .append(buildResetCommand()).done()
                .append(buildAddCommand()).done()
                .append(buildQueryCommand()).done()
                .append(buildUtilCommand()).done()
                .append(buildTrackCommand()).done()
                .execute(buildDefaultExecutor()).done()
                .usage("geocraft.command.atmosphere.usage")
                .build();
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildSetCommand(){
        return literal("set")
                .require(PERMISSION_NODE+".set").allow(DefaultPermissionLevel.OP).register()
                .then(string("property").suggest(
                        (args,context) -> {
                            List<String> res = getPropertyList();
                            res.addAll(SetConsumer.keySet());
                            return res;
                }).then(number("value", NumberType.DOUBLE).then(
                        blockPos("pos").asOptional().then(
                                process(CommandAtmosphere::set)
                        )
                )
        ));
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildStopCommand(){
        return literal("stop")
                .require(PERMISSION_NODE+".stop").allow(DefaultPermissionLevel.OP).register()
                .then(dimension("world").asOptional().then(
                        execute(((args, context) -> {
                            final World world = context.get("world", DimensionNode.class);
                            final IAtmosphereSystem system = AtmosphereSystemManager.getAtmosphereSystem(world);
                            if(system == null) throw new CommandException("geocraft.command.atmosphere_system.null");
                            system.setStop(!system.isStopped());
                            if(system.isStopped()) context.notifyCommandListener("geocraft.command.atmosphere.stop.stopped",world.provider.getDimension());
                            else context.notifyCommandListener("geocraft.command.atmosphere.stop.running",world.provider.getDimension());
                        }))
                )
        );
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildResetCommand(){
        return literal("reset")
                .require(PERMISSION_NODE+".reset").allow(DefaultPermissionLevel.OP).register()
                .then(string("property").allow("temp").then(
                        blockPos("pos").asOptional().then(
                                process((args, context) -> {
                                    final World world = context.getWorld();
                                    final Atmosphere atmosphere = context.get("atm");
                                    final BlockPos pos = context.getBlockPos("pos");
                                    final int x = pos.getX();
                                    final int z = pos.getZ();
                                    final String property = context.get("property");

                                    if("temp".equalsIgnoreCase(property)){
                                        if(!world.isAreaLoaded(pos,1)){
                                            throw new CommandException("geocraft.command.chunk_error.unloaded",x,z);
                                        }
                                        final Chunk chunk = world.getChunk(pos);
                                        if(atmosphere instanceof SurfaceAtmosphere){
                                            ((SurfaceAtmosphere)atmosphere).重置温度(chunk);
                                        }else throw new CommandException("geocraft.command.atmosphere.unknown");
                                        context.notifyCommandListener("geocraft.command.atmosphere.reset.temp",x,z, atmosphere.getAtmosphereTemperature(pos));
                                        return;
                                    }
                                    throw new WrongUsageException("geocraft.command.atmosphere.reset.usage");
                                })
                        )
                )
        );
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildAddCommand(){
        return literal("add")
                .require(PERMISSION_NODE+".add").allow(DefaultPermissionLevel.OP).register()
                .then(string("property").suggest(
                (args,context)->{
                    List<String> res = getPropertyList();
                    res.addAll(AddConsumer.keySet());
                    return res;
                }).then(number("value",NumberType.DOUBLE).then(
                        blockPos("pos").asOptional().then(
                                relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                        execute(CommandAtmosphere::add)
                                )
                        )
                )
        ));
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildQueryCommand(){
        return literal("query")
                .require(PERMISSION_NODE+".query").allow(DefaultPermissionLevel.OP).register()
                .then(string("property").suggest(
                        new ArrayList<>(QueryConsumer.keySet())
                ).then(blockPos("pos").asOptional().then(
                        process(CommandAtmosphere::query)
                )
        ));
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildUtilCommand(){
        return literal("util")
                .require(PERMISSION_NODE+".util").allow(DefaultPermissionLevel.OP).register()
                .smart()
                .literal("block_info")
                .then(blockState("blockToQuery")
                        .asOptional()
                        .defaultAs(((node, context) -> context.getWorld().getBlockState(context.getSender().getPosition().down())))
                        .then(execute((args, context) ->{
                            final IBlockState state = context.get("blockToQuery");
                            final ConfigurableBlockState cState = new ConfigurableBlockState(state);
                            int heatCapacity = GeoBlockSetting.getBlockHeatCapacity(state);
                            double reflectivity= GeoBlockSetting.getBlockReflectivity(state);
                            context.notifyCommandListener("geocraft.command.atmosphere.util.block_info",cState,heatCapacity,reflectivity);
                        }))
                ).done()
                .append(string("util_name")
                        .allow("sun","property","storage")
                        .then(process(CommandAtmosphere::util))
                ).done()
                .done();
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildTrackCommand(){
        return literal("track")
                .requirePlayer(true)
                .require(4)
                .require(PERMISSION_NODE+".track").allow(DefaultPermissionLevel.OP).register()
                .then(string("property")
                .suggest((args, context) -> {
                    List<String> res = getPropertyList();
                    res.addAll(Arrays.asList("temp","water","steam"));
                    return res;
                }).then(integer("time").min(1).then(
                        string("file_name")
                                .suggest((strings, context) -> Collections.singletonList("track_"+new Date().getTime()+".csv"))
                                .then(blockPos("pos")
                                        .then(process(CommandAtmosphere::track))
                                )
                        )
                )
        );
    }

    @Nonnull
    public static CommandRunFunction buildDefaultExecutor(){
        return (args, context) -> {
            final ICommandSender sender = context.getSender();
            final World world = sender.getEntityWorld();
            final BlockPos pos = sender.getPosition();
            final IAtmosphereAccessor accessor = getAtmosphereAccessor(world,pos.getX(),pos.getY(),pos.getZ());
            final Atmosphere atmosphere = getAtmosphere(accessor);
            context.notifyCommandListener("geocraft.command.atmosphere.query.basic", TextFormatting.AQUA,pos.getX(), Altitude.get物理海拔(pos.getY()),pos.getZ());
            context.notifyCommandListener("geocraft.command.atmosphere.query.basic.1",
                    accessor.getPressure(),
                    accessor.getWaterPressure(),
                    accessor.getTemperature());
            context.notifyCommandListener("geocraft.command.atmosphere.query.basic.2",accessor.getWind());
            for(Layer layer = atmosphere.getBottomLayer(); layer != null; layer = layer.getUpperLayer()){
                context.notifyCommandListener("geocraft.command.atmosphere.query.basic.3",
                        layer.getTagName(),layer.getBeginY(),layer.getBeginY()+layer.getDepth());
                context.notifyCommandListener("geocraft.command.atmosphere.query.basic.4",layer.getTemperature());
                final FluidState steam = (layer instanceof AtmosphereLayer)?((AtmosphereLayer)layer).getSteam():null;
                final FluidState water = layer.getWater();
                context.notifyCommandListener("geocraft.command.atmosphere.query.basic.5",
                        steam==null?"NULL":steam,water==null?"NULL":water);
            }
        };
    }

    @Nonnull
    public static RelayExecuteNodeBuilder process(@Nonnull final CommandRunFunction func){
        return relay(CommandAtmosphere::processAtmosphereInfo).then(
                execute(func)
        ).after(CommandAtmosphere::afterProcessAtmosphereInfo);
    }

    static void processAtmosphereInfo(@Nonnull final List<String> args, @Nonnull final ExecuteContext context) throws CommandException {
        final ICommandSender sender = context.getSender();
        final World world = sender.getEntityWorld();
        final BlockPos pos = context.getBlockPos("pos");
        final IAtmosphereAccessor accessor = getAtmosphereAccessor(world,pos.getX(),pos.getY(),pos.getZ());
        final Atmosphere atmosphere = getAtmosphere(accessor);
        final Layer layer = getAtmosphereLayer(atmosphere,pos.getY());
        context.put("accessor",accessor);
        context.put("atm",atmosphere);
        context.put("layer",layer);
        context.notifyCommandListener("geocraft.command.atmosphere.query.layer_inf",layer.getTagName(),
                layer.getBeginY(),layer.getBeginY()+layer.getDepth());
    }

    static void afterProcessAtmosphereInfo(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) {
        context.remove("accessor");
        context.remove("atm");
        context.remove("layer");
    }

    static void set(@Nonnull final List<String> args, @Nonnull final ExecuteContext context) throws CommandException{
        final double value = context.getDouble("value");
        final String name = context.get("property");
        final BiConsumer<AtmosphereCommandContext,Double> consumer = SetConsumer.get(name.toLowerCase(Locale.ROOT));
        if(consumer != null){
            final AtmosphereCommandContext atmosphereCommandContext = new AtmosphereCommandContext(context);
            consumer.accept(atmosphereCommandContext,value);
            if(atmosphereCommandContext.exception != null) throw atmosphereCommandContext.exception;
            return;
        }

        final BlockPos pos = context.getBlockPos("pos");
        final Layer layer = context.get("layer");
        final GeographyState state = getState(getProperty(new ResourceLocation(name)),layer);
        if(state instanceof FluidState){
            final FluidState gas = (FluidState) state;
            gas.setAmount((int) value);
            context.notifyCommandListener("geocraft.command.atmosphere.set.gas",pos.getX(),pos.getY(),pos.getZ(),gas.getFluidAmount());
            return;
        }
        if(state instanceof TemperatureState){
            final TemperatureState temperature = (TemperatureState) state;
            temperature.set((float) value);
            context.notifyCommandListener("geocraft.command.atmosphere.set.temp2",pos.getX(),pos.getY(),pos.getZ(),temperature.get());
            return;
        }
        throw new CommandException("geocraft.command.atmosphere.property.unknown");
    }

    static void add(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException {
        final double value = context.getDouble("value");
        final String name = context.get("property");
        final BiConsumer<AtmosphereCommandContext,Double> consumer = AddConsumer.get(name.toLowerCase(Locale.ROOT));
        if(consumer != null){
            final AtmosphereCommandContext atmosphereCommandContext = new AtmosphereCommandContext(context);
            consumer.accept(atmosphereCommandContext,value);
            if(atmosphereCommandContext.exception != null) throw atmosphereCommandContext.exception;
            return;
        }

        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX();
        final int z = pos.getZ();
        final Layer layer = context.get("layer");
        final GeographyState state = getState(getProperty(new ResourceLocation(name)),layer);
        if(state instanceof FluidState){
            FluidState gas = (FluidState) state;
            gas.fill((int) value,true);
            context.notifyCommandListener("geocraft.command.atmosphere.add.gas",x,pos.getY(),z,gas.getFluidAmount());
            return;
        }
        if(state instanceof TemperatureState){
            TemperatureState temperature = (TemperatureState) state;
            temperature.add(value);
            context.notifyCommandListener("geocraft.command.atmosphere.add.temp2",x,pos.getY(),z,temperature.get());
            return;
        }
        throw new CommandException("geocraft.command.atmosphere.property.unknown");
    }

    static void query(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final String name = context.get("property");
        final Consumer<AtmosphereCommandContext> consumer = QueryConsumer.get(name.toLowerCase(Locale.ROOT));
        if(consumer != null){
            final AtmosphereCommandContext atmosphereCommandContext = new AtmosphereCommandContext(context);
            consumer.accept(atmosphereCommandContext);
            if(atmosphereCommandContext.exception != null) throw atmosphereCommandContext.exception;
            return;
        }
        throw new WrongUsageException("geocraft.command.atmosphere.query.usage");
    }

    static void util(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final World world = context.getWorld();
        final WorldInfo info = world.getWorldInfo();
        final String util = context.get("util_name");
        final IAtmosphereAccessor accessor = context.get("accessor");

        if("sun".equalsIgnoreCase(util)){
            context.notifyCommandListener("geocraft.command.atmosphere.util.sun");
            context.notifyCommandListener("geocraft.command.atmosphere.util.sun.1",
                    AtmosphereUtil.getSunHeight(info).getDegree());
            context.notifyCommandListener("geocraft.command.atmosphere.util.sun.2",
                    AtmosphereUtil.getSunEnergyPerChunk(info));
            return;
        }
        if("property".equalsIgnoreCase(util)){
            IForgeRegistry<IGeographyProperty> registry = GeographyProperty.MANAGER.getProperties();
            for(IGeographyProperty property:registry){
                context.notifyCommandListener("geocraft.command.atmosphere.util.property",property.getRegistryName());
            }
            return;
        }
        if("storage".equalsIgnoreCase(util)){
            IAtmosphereSystem system = accessor.getSystem();
            IAtmosphereDataProvider provider = system.getDataProvider();
            Collection<AtmosphereData> data = provider.getLoadedAtmosphereDataCollection();
            context.notifyCommandListener("geocraft.command.atmosphere.util.storage",data.size());
            return;
        }
        throw new WrongUsageException("geocraft.command.atmosphere.util.usage");
    }

    static void track(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final int time = context.getInt("time");
        final String fileName = context.get("file_name");
        if(fileName.trim().isEmpty()) throw new WrongUsageException("geocraft.command.atmosphere.track.usage");
        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final String name = context.get("property");
        final Atmosphere atmosphere = context.get("atm");
        if("temp".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createInformationTracker(atmosphere, TemperatureTracker::new,fileName, GeoCraft.getLogger(),new BlockPos(x,y,z),time);
            context.notifyCommandListener("geocraft.command.atmosphere.track.temp",x,y,z,tracker.getId());
            return;
        }
        if("water".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(), GeoCraftProperties.WATER,new BlockPos(x,y,z),time);
            context.notifyCommandListener("geocraft.command.atmosphere.track.water",x,y,z,tracker.getId());
            return;
        }
        if("steam".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(), GeoCraftProperties.STEAM,new BlockPos(x,y,z),time);
            context.notifyCommandListener("geocraft.command.atmosphere.track.water",x,y,z,tracker.getId());
            return;
        }
        final ResourceLocation location = new ResourceLocation(name);
        final IGeographyProperty property= getProperty(location);
        if(property instanceof FluidProperty){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(),(FluidProperty) property,new BlockPos(x,y,z),time);
            context.notifyCommandListener("geocraft.command.atmosphere.track.gas",x,y,z,tracker.getId(),property.getRegistryName());
        }
        throw new CommandException("geocraft.command.atmosphere.property.unknown");
    }

    private static IAtmosphereAccessor getAtmosphereAccessor(World world, int x, int y, int z) throws CommandException {
        IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(world,new BlockPos(x,y,z),false);
        if(accessor == null){
            throw new CommandException("geocraft.command.atmosphere.nonexistent.there");
        }
        return accessor;
    }
    private static Atmosphere getAtmosphere(IAtmosphereAccessor accessor) throws CommandException {
        Atmosphere atmosphere = accessor.getAtmosphereHere();
        if(atmosphere ==null) throw new CommandException("geocraft.command.atmosphere.nonexistent.there");
        return atmosphere;
    }
    private static Layer getAtmosphereLayer(Atmosphere atmosphere, int height) throws CommandException {
        Layer layer = atmosphere.getLayer(new BlockPos(0,height,0));
        if(layer == null){
            throw new CommandException("geocraft.command.atmosphere.layer.null");
        }
        return layer;
    }
    private static List<String> getPropertyList(){
        Set<ResourceLocation> locations = GeographyProperty.MANAGER.getProperties().getKeys();
        List<String> strings = new ArrayList<>();
        for(ResourceLocation location:locations){
            strings.add(location.toString());
        }
        return strings;
    }
    private static IGeographyProperty getProperty(ResourceLocation location) throws CommandException {
        IGeographyProperty property= GeographyProperty.MANAGER.getProperties().getValue(location);
        if(property == null){
            throw new CommandException("geocraft.command.atmosphere.property.not_found",location);
        }
        return property;
    }
    private static GeographyState getState(IGeographyProperty property, Layer layer) throws CommandException {
        GeographyState state = layer.getState(property);
        if (state == null) {
            throw new CommandException("geocraft.command.atmosphere.property.null2", property.getRegistryName());
        }
        return state;
    }

    public static InformationLoggingTracker createInformationTracker(Atmosphere atmosphere, InformationLoggingTrackerFactory factory, String fileName, Logger logger, BlockPos pos, int time) throws CommandException {
        InformationLoggingTracker tracker;
        try {
            tracker = factory.getInstance(new FileLogger(fileName,logger),pos,time);
            atmosphere.addTracker(tracker);
        } catch (IOException e) {
            GeoCraft.getLogger().error(e);
            throw new CommandException("geocraft.command.io_error",e.getMessage());
        }
        return tracker;
    }
    public static InformationLoggingTracker createFluidTracker(Atmosphere atmosphere, String fileName, Logger logger, FluidProperty property, BlockPos pos, int time) throws CommandException {
        InformationLoggingTracker tracker;
        try {
            tracker = new FluidTracker(new FileLogger(fileName,logger),property,pos,time);
            atmosphere.addTracker(tracker);
        } catch (IOException e) {
            GeoCraft.getLogger().error(e);
            throw new CommandException("geocraft.command.io_error",e.getMessage());
        }
        return tracker;
    }

    @FunctionalInterface
    public interface InformationLoggingTrackerFactory {
        InformationLoggingTracker getInstance(FileLogger logger,BlockPos pos,int time);
    }

    static class AtmosphereCommandContext{
        public final IAtmosphereAccessor accessor;
        public final Atmosphere atmosphere;
        public final Layer layer;
        public final BlockPos pos;
        public final int x;
        public final int y;
        public final int z;
        public final ExecuteContext execute;
        public CommandException exception;

        AtmosphereCommandContext(final @Nonnull ExecuteContext execute) {
            this.accessor = execute.get("accessor");
            this.atmosphere = execute.get("atm");
            this.layer = execute.get("layer");
            this.pos = execute.getBlockPos("pos");
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.execute = execute;
        }
    }
}
