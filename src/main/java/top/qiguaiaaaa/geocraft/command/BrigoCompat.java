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
import dev.xhyrom.brigo.shadow.brigadier.CommandDispatcher;
import dev.xhyrom.brigo.shadow.brigadier.arguments.ArgumentType;
import dev.xhyrom.brigo.shadow.brigadier.arguments.DoubleArgumentType;
import dev.xhyrom.brigo.shadow.brigadier.arguments.IntegerArgumentType;
import dev.xhyrom.brigo.shadow.brigadier.arguments.StringArgumentType;
import dev.xhyrom.brigo.shadow.brigadier.builder.ArgumentBuilder;
import dev.xhyrom.brigo.shadow.brigadier.builder.LiteralArgumentBuilder;
import dev.xhyrom.brigo.shadow.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandHandler;
import top.qiguaiaaaa.geocraft.GeoCraft;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static top.qiguaiaaaa.geocraft.command.CommandAtmosphere.*;
import static top.qiguaiaaaa.geocraft.command.GeoArguments.*;

/**
 * @author QiguaiAAAA
 */
public final class BrigoCompat {
    private BrigoCompat(){}

    public static CommandDispatcher<CommandSource> getDispatcher(final @Nonnull CommandHandler handler){
        if(handler instanceof CommandHandlerExtras){
            return ((CommandHandlerExtras)handler).brigo$dispatcher();
        }
        return null;
    }

    public static void registerAtmosphere(final @Nonnull CommandHandler handler){
        final CommandDispatcher<CommandSource> dispatcher = getDispatcher(handler);
        if(dispatcher == null) return;
        dispatcher.register(literal(ATMOSPHERE_COMMAND_NAME)
                .then(literal("set", Stream.concat(getPropertyList().stream(),SetConsumer.keySet().stream()).collect(Collectors.toList()),
                        builder -> builder.then(argument(VALUE, DoubleArgumentType.doubleArg())
                                .then(blockPos())))
                        .then(compatArgs(PROPERTY,VALUE,"x","y","z")))
                .then(literal("add", Stream.concat(getPropertyList().stream(),AddConsumer.keySet().stream()).collect(Collectors.toList()),
                        builder -> builder.then(argument(VALUE,DoubleArgumentType.doubleArg())
                                .then(blockPos())))
                        .then(compatArgs(PROPERTY,VALUE,"x","y","z")))
                .then(literal("query",Stream.concat(getPropertyList().stream(),QueryConsumer.keySet().stream()).collect(Collectors.toList()),
                        builder -> builder.then(blockPos())))
                .then(literal("reset")
                        .then(literal("temp")
                                .then(blockPos())))
                .then(literal("util")
                        .then(literal("block_info")
                                .then(argument("block state to query",StringArgumentType.greedyString())))
                        .then(literal("sun"))
                        .then(literal("property"))
                        .then(literal("storage")))
                .then(literal("track", Stream.concat(getPropertyList().stream(), Stream.of("temp","water","steam")).collect(Collectors.toList()),
                        builder -> builder.then(argument("duration",IntegerArgumentType.integer(1))
                                .then(argument("file name",StringArgumentType.word())
                                        .then(blockPos()))))
                        .then(compatArgs(PROPERTY,"duration","file name","x","y","z")))
                .then(literal("stop")
                        .then(argument(WORLD,IntegerArgumentType.integer())))
        );
        GeoCraft.getLogger().info("GeoCraft detected Brigo, Brigadier compat loaded.");
    }

    public static RequiredArgumentBuilder<CommandSource,String> blockPos(){
        return argument("x y z",StringArgumentType.greedyString());
    }

    public static RequiredArgumentBuilder<CommandSource,String> compatArgs(@Nonnull final String... args){
        return argument(String.join(" ",args),StringArgumentType.greedyString());
    }

    public static LiteralArgumentBuilder<CommandSource> literal(final @Nonnull String literal){
        return LiteralArgumentBuilder.literal(literal);
    }

    public static LiteralArgumentBuilder<CommandSource> literal(final @Nonnull String literal,
                                                                final @Nonnull Collection<String> args,
                                                                final @Nonnull Function<LiteralArgumentBuilder<CommandSource>,LiteralArgumentBuilder<CommandSource>> consumer){
        return literals(LiteralArgumentBuilder.literal(literal),args,consumer);
    }

    public static <T extends ArgumentBuilder<CommandSource,T>> T literals(final @Nonnull T root,
                                                                          final @Nonnull Collection<String> args,
                                                                          final @Nonnull Function<LiteralArgumentBuilder<CommandSource>,LiteralArgumentBuilder<CommandSource>> consumer){
        for (final String arg:args){
            root.then(consumer.apply(literal(arg)));
        }
        return root;
    }

    public static <T> RequiredArgumentBuilder<CommandSource,T> argument(final @Nonnull String name, final @Nonnull ArgumentType<T> type){
        return RequiredArgumentBuilder.argument(name,type);
    }
}
