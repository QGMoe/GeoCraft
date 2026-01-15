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
import net.minecraft.command.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.registries.IForgeRegistry;
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
import top.qiguaiaaaa.geocraft.api.command.Nodes;
import top.qiguaiaaaa.geocraft.api.command.NumberType;
import top.qiguaiaaaa.geocraft.api.command.builder.CommandBuilder;
import top.qiguaiaaaa.geocraft.api.command.builder.INodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft.EntitySelectorNode;
import top.qiguaiaaaa.geocraft.api.command.utils.Matchers;
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
import java.util.*;

import static top.qiguaiaaaa.geocraft.api.command.Nodes.*;
import static top.qiguaiaaaa.geocraft.api.command.builder.execute.CommandRunFunction.notifyCommandListener;

public final class CommandAtmosphere {
    public static final String ATMOSPHERE_COMMAND_NAME = "atmosphere";
    public static final List<String> ALIASES = new ArrayList<>(Collections.singleton("大气"));

    public String getUsage(ICommandSender sender) {
        return "geocraft.command.atmosphere.usage";
    }

    public static ICommand create(){
        return new CommandBuilder(ATMOSPHERE_COMMAND_NAME)
                .requirePermissionLevel(2)
                .smart()
                .literal("set").then(
                        string("property").suggest(
                                (args, context) -> {
                                    List<String> res = getPropertyList();
                                    res.addAll(Arrays.asList("water","temp","ground_temp"));
                                    return res;
                                }).then(number("value", NumberType.DOUBLE).then(
                                        blockPos("pos").asOptional().then(
                                                relay(CommandAtmosphere::processAtmosphereInfo).then(
                                                        execute(CommandAtmosphere::set)
                                                ).after(CommandAtmosphere::afterProcessAtmosphereInfo)
                                        )
                        ))).done()
                .literal("stop").then(
                        execute(((args, context) -> {
                            final World world = context.getWorld();
                            final IAtmosphereSystem system = AtmosphereSystemManager.getAtmosphereSystem(world);
                            if(system == null) throw new CommandException("geocraft.command.atmosphere_system.null");
                            system.setStop(!system.isStopped());
                            notifyCommandListener(context, "geocraft.command.atmosphere.reset.temp",system.isStopped() , 0, 0);
                        }))
                ).done()
                .literal("reset").then(
                        string("property").allow("temp").then(
                                blockPos("pos").asOptional().then(
                                        relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                                execute((args, context) -> {
                                                    final World world = context.getWorld();
                                                    final Atmosphere atmosphere = context.get("atm");
                                                    final BlockPos pos = context.getBlockPos("pos");
                                                    final int x = pos.getX(),z = pos.getZ();
                                                    final String property = context.get("property");

                                                    if("temp".equalsIgnoreCase(property)){
                                                        if(!world.isAreaLoaded(pos,1)){
                                                            throw new CommandException("geocraft.command.chunk_error.unloaded",x,z);
                                                        }
                                                        final Chunk chunk = world.getChunk(pos);
                                                        if(atmosphere instanceof SurfaceAtmosphere){
                                                            ((SurfaceAtmosphere)atmosphere).重置温度(chunk);
                                                        }else throw new CommandException("geocraft.command.atmosphere.unknown");
                                                        notifyCommandListener(context,"geocraft.command.atmosphere.reset.temp",x,z, atmosphere.getAtmosphereTemperature(pos));
                                                        return;
                                                    }
                                                    throw new WrongUsageException("geocraft.command.atmosphere.reset.usage");
                                                })
                                        )
                                )
                        )
                ).done()
                .literal("add").then(string("property").suggest(
                        (args,context)->{
                            List<String> res = getPropertyList();
                            res.addAll(Arrays.asList("steam","water","temp","heat"));
                            return res;
                        }).then(number("value",NumberType.DOUBLE).then(
                                blockPos("pos").asOptional().then(
                                        relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                                execute(CommandAtmosphere::add)
                                        )
                                )
                        )
                )).done()
                .literal("query").then(string("property").suggest(
                        (strings, context) -> Arrays.asList("water","steam","temp","water_pressure","pressure","ground_temp","block_temp","wind","underlying","heat_volume")
                ).then(
                        blockPos("pos").asOptional().then(
                                relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                        execute(CommandAtmosphere::query)
                                )
                        )
                )).done()
                .literal("util").then(
                        string("util_name")
                                .allow("sun","property","block_info","storage")
                                .then(blockPos("pos").asOptional().then(
                                        relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                                execute(CommandAtmosphere::util)
                                        )))
                ).done()
                .literal("track").then(
                        string("property").suggest(
                                (args, context) -> {
                                    List<String> res = getPropertyList();
                                    res.addAll(Arrays.asList("temp","water","steam"));
                                    return res;
                                }).then(integer("time").min(1).then(
                                        string("file_name").suggest(
                                                (strings, context) -> Collections.singletonList("track_"+new Date().getTime()+".csv")
                                        ).then(
                                                blockPos("pos").then(
                                                        relay(CommandAtmosphere::processAtmosphereInfo,CommandAtmosphere::afterProcessAtmosphereInfo).then(
                                                                execute(CommandAtmosphere::track)
                                                        )
                                                )
                                        )
                                )
                        )
                ).done()
                .execute(((args, context) -> {
                    final ICommandSender sender = context.getSender();
                    final World world = sender.getEntityWorld();
                    final BlockPos pos = sender.getPosition();
                    final IAtmosphereAccessor accessor = getAtmosphereAccessor(world,pos.getX(),pos.getY(),pos.getZ());
                    final Atmosphere atmosphere = getAtmosphere(accessor);
                    notifyCommandListener(context, "geocraft.command.atmosphere.query.basic", TextFormatting.AQUA,pos.getX(), Altitude.get物理海拔(pos.getY()),pos.getZ());
                    notifyCommandListener(context, "geocraft.command.atmosphere.query.basic.1",
                            accessor.getPressure(),
                            accessor.getWaterPressure(),
                            accessor.getTemperature()
                    );
                    notifyCommandListener(context, "geocraft.command.atmosphere.query.basic.2",accessor.getWind());
                    for(Layer layer = atmosphere.getBottomLayer(); layer != null; layer = layer.getUpperLayer()){
                        notifyCommandListener(context, "geocraft.command.atmosphere.query.basic.3",
                                layer.getTagName(),layer.getBeginY(),layer.getBeginY()+layer.getDepth());
                        notifyCommandListener(context, "geocraft.command.atmosphere.query.basic.4",layer.getTemperature());
                        final FluidState steam = (layer instanceof AtmosphereLayer)?((AtmosphereLayer)layer).getSteam():null;
                        final FluidState water = layer.getWater();
                        notifyCommandListener(context, "geocraft.command.atmosphere.query.basic.5",
                                steam==null?"NULL":steam,water==null?"NULL":water);
                    }
                })).done()
                .build();
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
    }

    static void afterProcessAtmosphereInfo(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) {
        context.remove("accessor");
        context.remove("atm");
        context.remove("layer");
    }

    static void set(@Nonnull final List<String> args, @Nonnull final ExecuteContext context) throws CommandException{
        double value = context.getDouble("value");
        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX(),y = pos.getY(),z = pos.getZ();
        final IAtmosphereAccessor accessor = context.get("accessor");
        final Atmosphere atmosphere = context.get("atm");
        final Layer layer = context.get("layer");
        final String name = context.get("property");
        notifyCommandListener(context,"geocraft.command.atmosphere.query.layer_inf",layer.getTagName(),
                layer.getBeginY(),layer.getBeginY()+layer.getDepth());
        if("steam".equalsIgnoreCase(name)){
            final FluidState steam = (layer instanceof AtmosphereLayer)?((AtmosphereLayer)layer).getSteam():null;
            if(steam == null){
                throw new CommandException("geocraft.command.atmosphere.property.null");
            }
            steam.setAmount((int)value);
            notifyCommandListener(context,"geocraft.command.atmosphere.set.steam",x,pos.getY(),z,(int) value);
            return;
        }
        if("water".equalsIgnoreCase(name)){
            FluidState water = layer.getWater();
            if(water == null){
                throw new CommandException("geocraft.command.atmosphere.property.null");
            }
            water.setAmount((int)value);
            notifyCommandListener(context,"geocraft.command.atmosphere.set.water",x,pos.getY(),z,(int) value);
            return;
        }
        if("temp".equalsIgnoreCase(name)){
            layer.getTemperature().set((float) value);
            notifyCommandListener(context,"geocraft.command.atmosphere.set.temp",x,pos.getY(),z, (float) value);
            return;
        }
        if("debug".equalsIgnoreCase(name)){
            if(!(atmosphere instanceof SurfaceAtmosphere)){
                throw new CommandException("geocraft.command.atmosphere.unknown");
            }
            SurfaceAtmosphere a = (SurfaceAtmosphere) atmosphere;
            a.setDebug(value>0);
            if(value>0) notifyCommandListener(context,"geocraft.command.atmosphere.set.debug",x,z,a.isDebug());
            return;
        }
        ResourceLocation location = new ResourceLocation(name);
        IGeographyProperty property= getProperty(location);
        GeographyState state = getState(property,layer);
        if(state instanceof FluidState){
            FluidState gas = (FluidState) state;
            gas.setAmount((int) value);
            notifyCommandListener(context,"geocraft.command.atmosphere.set.gas",x,pos.getY(),z,gas.getFluidAmount());
            return;
        }
        if(state instanceof TemperatureState){
            TemperatureState temperature = (TemperatureState) state;
            temperature.set((float) value);
            notifyCommandListener(context,"geocraft.command.atmosphere.set.temp2",x,pos.getY(),z,temperature.get());
            return;
        }
        throw new CommandException("geocraft.command.atmosphere.property.unknown");
    }

    static void add(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException {
        final double value = context.getDouble("value");
        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX(),y = pos.getY(),z = pos.getZ();
        final Layer layer = context.get("layer");
        notifyCommandListener(context,"geocraft.command.atmosphere.query.layer_inf",layer.getTagName(),
                layer.getBeginY(),layer.getBeginY()+layer.getDepth());

        final String name = context.get("property");
        if("steam".equalsIgnoreCase(name)){
            FluidState steam = (layer instanceof AtmosphereLayer)?((AtmosphereLayer)layer).getSteam():null;
            if(steam == null) throw new CommandException("geocraft.command.atmosphere.property.null");
            if(steam.fill((int) value,true) == 0){
                throw new NumberInvalidException("commands.generic.num.tooSmall", value, -steam.getFluidAmount());
            }
            notifyCommandListener(context,"geocraft.command.atmosphere.add.water",x,pos.getY(),z,steam);
            return;
        }
        if("water".equalsIgnoreCase(name)){
            FluidState water = layer.getWater();
            if(water == null) throw new CommandException("geocraft.command.atmosphere.property.null");
            if(water.fill((int) value,true) == 0){
                throw new NumberInvalidException("commands.generic.num.tooSmall", value, -water.getFluidAmount());
            }
            notifyCommandListener(context,"geocraft.command.atmosphere.add.water",x,pos.getY(),z,water);
            return;
        }
        if("temp".equalsIgnoreCase(name)){
            layer.getTemperature().add(value);
            notifyCommandListener(context,"geocraft.command.atmosphere.add.temp",x,pos.getY(),z, layer.getTemperature());
            return;
        }
        if("heat".equalsIgnoreCase(name)){
            layer.putHeat(value,new BlockPos(x,pos.getY(),z));
            notifyCommandListener(context,"geocraft.command.atmosphere.add.temp",x,pos.getY(),z, layer.getTemperature());
            return;
        }
        final ResourceLocation location = new ResourceLocation(name);
        final IGeographyProperty property= getProperty(location);
        final GeographyState state = getState(property,layer);
        if(state instanceof FluidState){
            FluidState gas = (FluidState) state;
            gas.fill((int) value,true);
            notifyCommandListener(context,"geocraft.command.atmosphere.add.gas",x,pos.getY(),z,gas.getFluidAmount());
            return;
        }
        if(state instanceof TemperatureState){
            TemperatureState temperature = (TemperatureState) state;
            temperature.add(value);
            notifyCommandListener(context,"geocraft.command.atmosphere.add.temp2",x,pos.getY(),z,temperature.get());
            return;
        }
        throw new CommandException("geocraft.command.atmosphere.property.unknown");
    }

    static void query(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX(),y=pos.getY(),z = pos.getZ();
        final IAtmosphereAccessor accessor = context.get("accessor");
        final String name = context.get("property");
        final World world = context.getWorld();
        final ICommandSender sender = context.getSender();
        final Layer layer = context.get("layer");
        final Atmosphere atmosphere = context.get("atm");

        if("block_temp".equalsIgnoreCase(name)){
            accessor.setNotAir(world.getBlockState(new BlockPos(x,y,z)).getMaterial() != Material.AIR);
            final double temp = accessor.getTemperature();
            notifyCommandListener(context,"geocraft.command.atmosphere.query.block_temp",x,y,z,temp);
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int)temp);
            return;
        }

        notifyCommandListener(context,"geocraft.command.atmosphere.query.layer_inf",layer.getTagName(),
                layer.getBeginY(),layer.getBeginY()+layer.getDepth());
        if("steam".equalsIgnoreCase(name)){
            FluidState steam = (layer instanceof AtmosphereLayer)?((AtmosphereLayer)layer).getSteam():null;
            if(steam == null) throw new CommandException("geocraft.command.atmosphere.property.null");
            notifyCommandListener(context,"geocraft.command.atmosphere.query.steam",x,y,z,steam);
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT,steam.getFluidAmount());
            return;
        }
        if("water".equalsIgnoreCase(name)){
            FluidState water = layer.getWater();
            if(water == null) throw new CommandException("geocraft.command.atmosphere.property.null");
            notifyCommandListener(context,"geocraft.command.atmosphere.query.water",x,y,z,water);
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT,water.getFluidAmount());
            return;
        }
        if("temp".equalsIgnoreCase(name)){
            notifyCommandListener(context,"geocraft.command.atmosphere.query.temp",x,y,z,layer.getTemperature() );
            return;
        }
        if("ground_temp".equalsIgnoreCase(name)){
            notifyCommandListener(context,"geocraft.command.atmosphere.query.ground_temp",x,z,atmosphere.getUnderlying(pos).getTemperature());
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int) atmosphere.getUnderlying(pos).getTemperature().get());
            return;
        }
        if("wind".equalsIgnoreCase(name)){
            Vec3d wind = accessor.getWind();
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.north",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.NORTH.getDirectionVec())));
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.east",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.EAST.getDirectionVec())));
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.south",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.SOUTH.getDirectionVec())));
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.west",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.WEST.getDirectionVec())));
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.west",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.UP.getDirectionVec())));
            notifyCommandListener(context,"geocraft.command.atmosphere.query.wind.west",x,y,z,wind.dotProduct(new Vec3d(EnumFacing.DOWN.getDirectionVec())));
            return;
        }
        if("underlying".equalsIgnoreCase(name)){
            UnderlyingLayer underlying1 = atmosphere.getUnderlying(pos);
            SurfaceUnderlying underlying;
            if(underlying1 instanceof SurfaceUnderlying){
                underlying = (SurfaceUnderlying) underlying1;
            }else throw new CommandException("geocraft.command.atmosphere.unknown_underlying");
            notifyCommandListener(context,"geocraft.command.atmosphere.query.underlying",x,underlying.getAltitude(),z,underlying.getHeatCapacity(),underlying.平均返照率);
            return;
        }
        if("heat_volume".equalsIgnoreCase(name)){
            notifyCommandListener(context,"geocraft.command.atmosphere.query.heat_volume",x,y,z,layer.getHeatCapacity());
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT,(int) layer.getHeatCapacity());
            return;
        }
        if("water_pressure".equalsIgnoreCase(name)){
            notifyCommandListener(context,"geocraft.command.atmosphere.query.water_pressure",x,y,z,accessor.getWaterPressure()*0.01);
            return;
        }
        if("pressure".equalsIgnoreCase(name)){
            notifyCommandListener(context,"geocraft.command.atmosphere.query.pressure",x,y,z,accessor.getPressure()*0.01);
            return;
        }
        throw new WrongUsageException("geocraft.command.atmosphere.query.usage");
    }

    static void util(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final World world = context.getWorld();
        final WorldInfo info = world.getWorldInfo();
        final String util = context.get("util_name");
        final BlockPos pos = context.get("pos");
        final IAtmosphereAccessor accessor = context.get("accessor");

        if("sun".equalsIgnoreCase(util)){
            notifyCommandListener(context,"geocraft.command.atmosphere.util.sun");
            notifyCommandListener(context,"geocraft.command.atmosphere.util.sun.1",
                    AtmosphereUtil.getSunHeight(info).getDegree());
            notifyCommandListener(context,"geocraft.command.atmosphere.util.sun.2",
                    AtmosphereUtil.getSunEnergyPerChunk(info));
            return;
        }
        if("property".equalsIgnoreCase(util)){
            IForgeRegistry<IGeographyProperty> registry = GeographyProperty.MANAGER.getProperties();
            for(IGeographyProperty property:registry){
                notifyCommandListener(context,"geocraft.command.atmosphere.util.property",property.getRegistryName());
            }
            return;
        }
        if("block_info".equalsIgnoreCase(util)){
            BlockPos downPos = pos.down();
            IBlockState state = world.getBlockState(downPos);
            ConfigurableBlockState cState = new ConfigurableBlockState(state);
            int heatCapacity = GeoBlockSetting.getBlockHeatCapacity(state);
            double reflectivity= GeoBlockSetting.getBlockReflectivity(state);
            notifyCommandListener(context,"geocraft.command.atmosphere.util.block_info",cState,heatCapacity,reflectivity);
            return;
        }
        if("storage".equalsIgnoreCase(util)){
            IAtmosphereSystem system = accessor.getSystem();
            IAtmosphereDataProvider provider = system.getDataProvider();
            Collection<AtmosphereData> data = provider.getLoadedAtmosphereDataCollection();
            notifyCommandListener(context,"geocraft.command.atmosphere.util.storage",data.size());
            return;
        }
        throw new WrongUsageException("geocraft.command.atmosphere.util.usage");
    }

    static void track(@Nonnull final List<String> args,@Nonnull final ExecuteContext context) throws CommandException{
        final int time = context.getInt("time");
        final String fileName = context.get("file_name");
        if(fileName.trim().isEmpty()) throw new WrongUsageException("geocraft.command.atmosphere.track.usage");
        final BlockPos pos = context.getBlockPos("pos");
        final int x = pos.getX(),y = pos.getY(),z = pos.getZ();
        final String name = context.get("property");
        final Atmosphere atmosphere = context.get("atm");
        if("temp".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createInformationTracker(atmosphere, TemperatureTracker::new,fileName, GeoCraft.getLogger(),new BlockPos(x,y,z),time);
            notifyCommandListener(context,"geocraft.command.atmosphere.track.temp",x,y,z,tracker.getId());
            return;
        }
        if("water".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(), GeoCraftProperties.WATER,new BlockPos(x,y,z),time);
            notifyCommandListener(context,"geocraft.command.atmosphere.track.water",x,y,z,tracker.getId());
            return;
        }
        if("steam".equalsIgnoreCase(name)){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(), GeoCraftProperties.STEAM,new BlockPos(x,y,z),time);
            notifyCommandListener(context,"geocraft.command.atmosphere.track.water",x,y,z,tracker.getId());
            return;
        }
        final ResourceLocation location = new ResourceLocation(name);
        final IGeographyProperty property= getProperty(location);
        if(property instanceof FluidProperty){
            InformationLoggingTracker tracker = createFluidTracker(atmosphere,fileName, GeoCraft.getLogger(),(FluidProperty) property,new BlockPos(x,y,z),time);
            notifyCommandListener(context,"geocraft.command.atmosphere.track.gas",x,y,z,tracker.getId(),property.getRegistryName());
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

    public interface InformationLoggingTrackerFactory {
        InformationLoggingTracker getInstance(FileLogger logger,BlockPos pos,int time);
    }
}
