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

import dev.xhyrom.brigo.accessor.CommandHandlerExtras;
import dev.xhyrom.brigo.command.CommandSource;
import dev.xhyrom.brigo.shadow.brigadier.Command;
import dev.xhyrom.brigo.shadow.brigadier.CommandDispatcher;
import dev.xhyrom.brigo.shadow.brigadier.arguments.*;
import dev.xhyrom.brigo.shadow.brigadier.builder.ArgumentBuilder;
import dev.xhyrom.brigo.shadow.brigadier.builder.LiteralArgumentBuilder;
import dev.xhyrom.brigo.shadow.brigadier.builder.RequiredArgumentBuilder;
import dev.xhyrom.brigo.shadow.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.ICommandManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.test.GeoTest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.xhyrom.brigo.shadow.brigadier.arguments.DoubleArgumentType.doubleArg;
import static dev.xhyrom.brigo.shadow.brigadier.arguments.IntegerArgumentType.integer;
import static dev.xhyrom.brigo.shadow.brigadier.arguments.StringArgumentType.greedyString;
import static top.qiguaiaaaa.geocraft.command.CommandAtmosphere.ATMOSPHERE_COMMAND_NAME;
import static top.qiguaiaaaa.geocraft.command.CommandAtmosphere.getPropertyList;
import static top.qiguaiaaaa.geocraft.command.CommandFluidPhysics.FLUID_PHYSICS_COMMAND_NAME;
import static top.qiguaiaaaa.geocraft.command.GeoArguments.*;

/**
 * @author QiguaiAAAA
 */
@SuppressWarnings("unused")
public final class BrigoCompat {
    private static final Command<CommandSource> DO_NOTHING = ctx -> 0;
    private static CommandDispatcher<CommandSource> dispatcher;

    private BrigoCompat(){}

    public static CommandDispatcher<CommandSource> getDispatcher(final @Nonnull ICommandManager manager){
        if(manager instanceof CommandHandlerExtras){
            return ((CommandHandlerExtras)manager).brigo$dispatcher();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static void init(final @Nonnull LoaderState state){
        if(state != LoaderState.SERVER_STARTING) return;
        final @Nonnull ICommandManager manager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        dispatcher = getDispatcher(manager);
        if(dispatcher == null) return;
        if(manager.getCommands().containsKey(ATMOSPHERE_COMMAND_NAME)){
            final @Nonnull LiteralCommandNode<CommandSource> root = registerAtmosphere();
            CommandAtmosphere.ALIASES.forEach(alias -> register(literal(alias)
                    .redirect(root)));
        }
        if(manager.getCommands().containsKey(FLUID_PHYSICS_COMMAND_NAME)) registerFluidPhysics();
        if(manager.getCommands().containsKey(CommandGeoTest.GEOTEST_COMMAND_NAME)) registerGeoTest();
        if(manager.getCommands().containsKey(CommandGeoConfig.GEO_CONFIG_COMMAND_NAME)) registerGeoConfig();
        GeoCraft.getLogger().info("GeoCraft detected Brigo, Brigadier compat loaded.");
    }

    @Nonnull
    public static LiteralCommandNode<CommandSource> registerAtmosphere(){
        return register(literal(ATMOSPHERE_COMMAND_NAME)
                .then(literal("set", Stream.concat(getPropertyList().stream(),CommandAtmosphere.SetConsumer.keySet().stream()).collect(Collectors.toList()),
                        builder -> builder.then(pos(argument(VALUE, doubleArg()),Function.identity())
                                .executes(DO_NOTHING)))
                        .then(compatArgs(PROPERTY,VALUE,"x","y","z")))
                .then(literal("add", Stream.concat(getPropertyList().stream(),CommandAtmosphere.AddConsumer.keySet().stream()).collect(Collectors.toList()),
                        builder -> builder.then(pos(argument(VALUE, doubleArg()),Function.identity())
                                .executes(DO_NOTHING)))
                        .then(compatArgs(PROPERTY,VALUE,"x","y","z")))
                .then(literal("query",new ArrayList<>(CommandAtmosphere.QueryConsumer.keySet()),
                        builder -> pos(builder,posThen ->
                                posThen.then(argument("scale",DoubleArgumentType.doubleArg()))
                                        .executes(DO_NOTHING))
                                .executes(DO_NOTHING)))
                .then(literal("reset")
                        .then(pos(literal("temp"),Function.identity())
                                .executes(DO_NOTHING)))
                .then(literal("util")
                        .then(literal("block_info")
                                .then(argument("[block state to query] [prop] [scale]",greedyString()))
                                .executes(DO_NOTHING))
                        .then(literal("sun"))
                        .then(literal("property"))
                        .then(literal("storage")))
                .then(literal("track", Stream.concat(getPropertyList().stream(), Stream.of("temp","water","steam")).collect(Collectors.toList()),
                        builder -> builder.then(argument("duration",integer(1))
                                .then(pos(argument("file name",StringArgumentType.string()),Function.identity())
                                        .executes(DO_NOTHING))))
                        .then(compatArgs(PROPERTY,"duration","file name","x","y","z")))
                .then(literal("stop")
                        .then(argument(WORLD, integer()))
                        .executes(DO_NOTHING)));
    }

    public static void registerFluidPhysics(){
        register(literal(FLUID_PHYSICS_COMMAND_NAME)
                .then(literal("query").then(literal("mode")))
                .then(literal("operation")
                        .then(pos(literal("evaporate"),posThen ->
                                posThen.then(literal("do"))
                                        .executes(DO_NOTHING))
                                .executes(DO_NOTHING))));
    }

    public static void registerGeoTest(){
        register(literal(CommandGeoTest.GEOTEST_COMMAND_NAME)
                .then(literal("run",GeoTest.queryAll().stream().map(Object::toString).collect(Collectors.toList()), builder ->
                        pos(builder,posThen ->
                                posThen.then(argument("target",StringArgumentType.greedyString()))
                                        .executes(DO_NOTHING))
                                .executes(DO_NOTHING))));
    }

    public static void registerGeoConfig(){
        register(literal(CommandGeoConfig.GEO_CONFIG_COMMAND_NAME)
                .then(argument("config_item_path",StringArgumentType.word())
                        .then(literal("query")
                                .then(argument("scale",DoubleArgumentType.doubleArg()))
                                .executes(DO_NOTHING))
                        .then(literal("set")
                                .then(argument("value",StringArgumentType.string())))
                        .then(literal("reset")
                                .then(argument("request_id",StringArgumentType.word()))
                                .executes(DO_NOTHING))));
    }

    @Nonnull
    public static LiteralCommandNode<CommandSource> register(@Nonnull final LiteralArgumentBuilder<CommandSource> builder){
        final @Nonnull String literal = builder.getLiteral();
        dispatcher.getRoot().getChildren().removeIf(node -> node instanceof LiteralCommandNode<?> && literal.equals(((LiteralCommandNode<CommandSource>) node).getLiteral()));
        return dispatcher.register(builder);
    }

    @Nonnull
    public static RequiredArgumentBuilder<CommandSource,String> compatArgs(@Nonnull final String... args){
        return argument(String.join(" ",args), greedyString());
    }

    @Nonnull
    public static LiteralArgumentBuilder<CommandSource> literal(final @Nonnull String literal){
        return LiteralArgumentBuilder.literal(Objects.requireNonNull(literal));
    }

    @Nonnull
    public static LiteralArgumentBuilder<CommandSource> literal(final @Nonnull String literal,
                                                                final @Nonnull Collection<String> args,
                                                                final @Nonnull Function<LiteralArgumentBuilder<CommandSource>,LiteralArgumentBuilder<CommandSource>> consumer){
        return literals(literal(literal),args,consumer);
    }

    @Nonnull
    public static <T extends ArgumentBuilder<CommandSource,T>> T literals(final @Nonnull T root,
                                                                          final @Nonnull Collection<String> args,
                                                                          final @Nonnull Function<LiteralArgumentBuilder<CommandSource>,LiteralArgumentBuilder<CommandSource>> consumer){
        for (final @Nonnull String arg:args){
            root.then(consumer.apply(literal(Objects.requireNonNull(arg))));
        }
        return root;
    }

    @Nonnull
    public static <T> RequiredArgumentBuilder<CommandSource,T> argument(final @Nonnull String name, final @Nonnull ArgumentType<T> type){
        return RequiredArgumentBuilder.argument(name,type);
    }

    @Nonnull
    public static <T extends ArgumentBuilder<CommandSource,T>> T pos(final @Nonnull T builder,
                                                                     final @Nonnull Function<ArgumentBuilder<CommandSource,? extends ArgumentBuilder<CommandSource,?>>, ArgumentBuilder<CommandSource,? extends ArgumentBuilder<CommandSource,?>>> then){
        return builder.then(literal("~")
                        .then(literal("~")
                                .then(then.apply(literal("~")))
                                .then(then.apply(argument("z",DoubleArgumentType.doubleArg()))))
                        .then(argument("y",DoubleArgumentType.doubleArg())
                                .then(then.apply(literal("~")))
                                .then(then.apply(argument("z",DoubleArgumentType.doubleArg())))))
                .then(argument("x",DoubleArgumentType.doubleArg())
                        .then(literal("~")
                                .then(then.apply(literal("~")))
                                .then(then.apply(argument("z",DoubleArgumentType.doubleArg()))))
                        .then(argument("y",DoubleArgumentType.doubleArg())
                                .then(then.apply(literal("~")))
                                .then(then.apply(argument("z",DoubleArgumentType.doubleArg())))));
    }
}
