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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import moe.qingu.nickel.command.builder.CommandBuilder;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.node.parameter.generic.StringNode;
import moe.qingu.nickel.command.node.parameter.generic.UUIDNode;
import moe.qingu.nickel.text.TextBuilder;
import moe.qingu.nickel.text.TranslationTextBuilder;
import moe.qingu.nickel.text.hover.HoverEventBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.api.configs.item.base.ConfigBoolean;
import top.qiguaiaaaa.geocraft.api.configs.item.base.ConfigCustom;
import top.qiguaiaaaa.geocraft.api.configs.item.base.ConfigString;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigDouble;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigInteger;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigLong;
import top.qiguaiaaaa.geocraft.command.node.ConfigItemNode;
import top.qiguaiaaaa.geocraft.configs.ConfigurationLoader;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static moe.qingu.nickel.command.Nodes.*;
import static moe.qingu.nickel.text.Texts.*;
import static top.qiguaiaaaa.geocraft.command.GeoArguments.MULTIPLY;
import static top.qiguaiaaaa.geocraft.command.GeoArguments._multiply;

/**
 * @author QGMoe
 */
public final class CommandGeoConfig {
    public static final String GEO_CONFIG_COMMAND_NAME = "geoconfig";
    public static final String GEO_CONFIG_PERMISSION_NODE = "geocraft.command."+GEO_CONFIG_COMMAND_NAME;

    private static final UUID INIT_REQUEST = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
    private static final HashMap<Class<? extends ConfigItem<?,?>>, Processor> applicationProcessors = new HashMap<>();
    private static final Cache<UUID,Boolean> requestCaches = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static final int STATUS_REQUESTING = 2;
    private static final int STATUS_SUCCESS = 1;
    private static final int ERROR_UNSUPPORTED = -1;
    private static final int ERROR_SET_FAILED = -2;
    private static final int ERROR_SAVE_FAILED = -3;
    private static final int ERROR_INVALID_REQUEST = -4;

    static{
        putProcessor(ConfigString.class, ConfigItem::setValue);
        applicationProcessors.put((Class<? extends ConfigItem<?,?>>) (Object) ConfigCustom.class,(config,val)->{ //NND这东西泛型推断太麻烦了，(Object)不能删不然编译过不了
            try {
                ((ConfigCustom)config).setValue(((ConfigCustom<?>)config).getParser().apply(val));
            }catch (final @Nonnull RuntimeException e){
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.custom.error").arg(e.getMessage()));
            }
        });
        putProcessor(ConfigBoolean.class, (config, val) -> {
            if("true".equals(val)) config.setValue(true);
            else if("false".equals(val)) config.setValue(false);
            else throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.boolean.synatx_error").arg(plain(val).italic(true)));
        });
        putProcessor(ConfigInteger.class,(config,val) -> {
            try{
                final int num = Integer.parseInt(val);
                if(num < config.getMinValue()) throw throwTooMin(val,String.valueOf(config.getMinValue()));
                else if(num > config.getMaxValue()) throw throwTooMax(val,String.valueOf(config.getMaxValue()));
                config.setValue(num);
            }catch (final NumberFormatException e){
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.int.synatx_error").arg(plain(val).italic(true)));
            }
        });
        putProcessor(ConfigDouble.class,(config,val) -> {
            try{
                final double num = Double.parseDouble(val);
                if(num < config.getMinValue()) throw throwTooMin(val,String.valueOf(config.getMinValue()));
                else if(num > config.getMaxValue()) throw throwTooMax(val,String.valueOf(config.getMaxValue()));
                config.setValue(num);
            }catch (final NumberFormatException e){
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.double.synatx_error").arg(plain(val).italic(true)));
            }
        });
        putProcessor(ConfigLong.class,(config, val) -> {
            try{
                final long num = Long.parseLong(val);
                if(num < config.getMinValue()) throw throwTooMin(val,String.valueOf(config.getMinValue()));
                else if(num > config.getMaxValue()) throw throwTooMax(val,String.valueOf(config.getMaxValue()));
                config.setValue(num);
            }catch (final NumberFormatException e){
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.long.synatx_error").arg(plain(val).italic(true)));
            }
        });
    }

    @Nonnull
    private static NickelRuntimeException throwTooMin(final @Nonnull String num, final @Nonnull String minVal){
        return new NickelRuntimeException(translation("geocraft.command.geoconfig.set.num.too_min")
                .arg(plain(num).italic(true))
                .arg(plain(minVal).color(TextFormatting.AQUA).bold(true)));
    }

    @Nonnull
    private static NickelRuntimeException throwTooMax(final @Nonnull String num, final @Nonnull String maxVal){
        return new NickelRuntimeException(translation("geocraft.command.geoconfig.set.num.too_max")
                .arg(plain(num).italic(true))
                .arg(plain(maxVal).color(TextFormatting.AQUA).bold(true)));
    }

    private static <T extends ConfigItem<?,T>> void putProcessor(final @Nonnull Class<T> cls,final @Nonnull ConfigProcessor<T> processor){
        applicationProcessors.put(cls,processor);
    }

    @Nonnull
    public static ICommand create(){
        return new CommandBuilder(GEO_CONFIG_COMMAND_NAME)
                .require(GEO_CONFIG_PERMISSION_NODE).allow(DefaultPermissionLevel.OP).register()
                .require(2)
                .then(ConfigItemNode.configItem("config_entry")
                        .translate("geocraft.command.geoconfig.arg.config_entry")
                        .smart()
                        .literal("set")
                        .require(4)
                        .requirePlayer(true)
                        .require(GEO_CONFIG_PERMISSION_NODE+".set").allow(DefaultPermissionLevel.OP).register()
                        .then($string("config_value")
                                .translate("geocraft.command.geoconfig.arg.config_value")
                                .then(execute(CommandGeoConfig::applyChanges))
                        ).done()
                        .literal("reset")
                        .require(4)
                        .requirePlayer(true)
                        .require(GEO_CONFIG_PERMISSION_NODE+".reset").allow(DefaultPermissionLevel.OP).register()
                        .then($uuid("request_id")
                                .asOptional()
                                .suggest(Collections.emptyList())
                                .defaultAs(INIT_REQUEST)
                                .translate("geocraft.command.geoconfig.arg.request_id")
                                .then(execute(CommandGeoConfig::resetToDefault)))
                        .done()
                        .literal("query").then(_multiply()
                                .then(execute(CommandGeoConfig::showInfo))).done()
                        .execute(CommandGeoConfig::showInfo)
                        .done())
                .build();
    }

    @Nonnull
    private static HoverEventBuilder.ShowText showComment(final @Nonnull ConfigItem<?,?> item){
        return Hovers.text((
                (item.getTranslationKey() != null)? translation(item.getTranslationKey()):
                        item.hasComment()?plain(item.getComment()):
                                translation("geocraft.command.geoconfig.info.no_comment")
        ).italic(true));
    }

    @Nonnull
    private static TranslationTextBuilder modeTextOf(final @Nonnull ConfigItem<?,?> item){
        return translation(item.getEffectiveMode().getTranslationKey())
                .color(item.getEffectiveMode().getColor())
                .bold(true);
    }

    @Nonnull
    private static TextBuilder<?,?> valueTextOf(final @Nonnull ConfigItem<?,?> item){
        final String value = item.getValue().toString();
        return value.codePoints().count()<300?plain(value).color(TextFormatting.AQUA):
                translation("geocraft.command.geoconfig.info.cur_value.out_of_range").color(TextFormatting.GOLD);
    }

    @Nonnull
    private static TextBuilder<?,?> defaultValueTextOf(final @Nonnull ConfigItem<?,?> item){
        final String value = item.getDefaultValue().toString();
        return value.codePoints().count()<500?plain(value).color(TextFormatting.AQUA):
                translation("geocraft.command.geoconfig.info.cur_value.out_of_range").color(TextFormatting.GOLD);
    }

    @Nonnull
    private static Object statusTextOf(final @Nonnull ConfigItem<?,?> item){
        return item.isDeprecated()?translation("geocraft.command.geoconfig.info.title.deprecation").color(TextFormatting.RED):
                item.isBeta()?translation("geocraft.command.geoconfig.info.title.experimental").color(TextFormatting.GOLD):
                        "";
    }

    private static int toIntValueOf(final @Nonnull ConfigItem<?,?> item,final double multiply) throws IllegalStateException{
        if(item instanceof ConfigInteger) return (int) (((ConfigInteger) item).getValue()*multiply);
        else if(item instanceof ConfigLong) return (int) (((ConfigLong)item).getValue()*multiply);
        else if(item instanceof ConfigDouble) return (int) (((ConfigDouble)item).getValue()*multiply);
        else if(item instanceof ConfigBoolean) return (int) ((((ConfigBoolean)item).getValue()?1:0)*multiply);
        else throw new IllegalStateException();//仅作为 flag
    }

    public static void showInfo(@Nonnull final ExecuteContext ctx) {
        final ConfigItem<?,?> item = ctx.get("config_entry",ConfigItemNode.class);
        final double multiply = ctx.getContexts().containsKey(MULTIPLY)?ctx.getDouble(MULTIPLY):1d;
        ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.info.title")
                .arg(statusTextOf(item))
                .arg(item.getPath())
                .color(TextFormatting.AQUA)
                .bold(true)
                .strikethrough(item.isDeprecated())
                .hoverTo(showComment(item))
                .done());
        ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.info.type")
                .arg(translation(item.getTypeTranslationKey())).done());
        ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.info.mode")
                .arg(modeTextOf(item))
                .done());
        ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.info.cur_value")
                .arg(valueTextOf(item))
                .hoverTo(HoverEvent.Action.SHOW_TEXT)
                .content(translation("geocraft.command.geoconfig.info.default")
                        .arg(defaultValueTextOf(item)))
                .done());
        try{
            final int queryRes =toIntValueOf(item,multiply);
            ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,queryRes);
        }catch (final IllegalStateException ignored){}
    }

    public static void applyChanges(@Nonnull final ExecuteContext ctx) throws CommandException {
        final ConfigItem<?,?> item = ctx.get("config_entry",ConfigItemNode.class);
        final String val = ctx.get("config_value", StringNode.class);
        for(Class<?> cls = item.getClass();cls != Object.class;cls = cls.getSuperclass()){
            final Processor processor = applicationProcessors.get(cls);
            if(processor != null){
                processor.process(item,val);
                try {
                    ConfigurationLoader.save();
                }catch (final @Nonnull RuntimeException e){
                    GeoCraft.getLogger().error("Save Config Item Failed:",e);
                    ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,ERROR_SAVE_FAILED);
                    throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.save_fail")
                            .arg(valueTextOf(item))
                            .arg(e.getMessage())
                            .color(TextFormatting.RED));
                }
                ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.set.success")
                        .arg(valueTextOf(item))
                        .arg(modeTextOf(item))
                        .color(TextFormatting.GREEN)
                        .done());
                ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_SUCCESS);
                return;
            }
        }
        ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,ERROR_UNSUPPORTED);
        throw new NickelRuntimeException(translation("geocraft.command.geoconfig.set.unsupported"));
    }

    public static void resetToDefault(@Nonnull final ExecuteContext ctx) throws CommandException {
        final ConfigItem item = ctx.get("config_entry",ConfigItemNode.class);
        final UUID request = ctx.get("request_id", UUIDNode.class);
        if(request == INIT_REQUEST){
            final UUID uuid = UUID.randomUUID();
            requestCaches.put(uuid,Boolean.TRUE);
            ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.reset.confirm.title")
                    .arg(statusTextOf(item))
                    .arg(item.getPath())
                    .color(TextFormatting.GOLD).done());
            ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.reset.confirm.default").arg(defaultValueTextOf(item)).done());
            ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.reset.confirm.question")
                    .arg(plain("[")
                            .then(translation("geocraft.command.geoconfig.reset.confirm.button"))
                            .then("]")
                            .clickTo(ClickEvent.Action.SUGGEST_COMMAND)
                            .then("/geoconfig "+item.getPath()+" reset "+uuid)
                            .color(TextFormatting.AQUA))
                    .done());
            ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_REQUESTING);
        }else {
            final Boolean status = requestCaches.getIfPresent(request);
            if(status == null){
                ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,ERROR_INVALID_REQUEST);
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.reset.invalid_request").arg(request));
            }
            requestCaches.invalidate(request);
            try{
                item.setValue(item.getDefaultValue());
            }catch (final RuntimeException e){
                GeoCraft.getLogger().error("Reset Config Item Failed:",e);
                ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,ERROR_SET_FAILED);
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.reset.failed_set").arg(e.getMessage()));
            }
            try {
                ConfigurationLoader.save();
            }catch (final @Nonnull RuntimeException e){
                GeoCraft.getLogger().error("Save Reset Config Item Failed:",e);
                ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,ERROR_SAVE_FAILED);
                throw new NickelRuntimeException(translation("geocraft.command.geoconfig.reset.save_fail")
                        .arg(e.getMessage())
                        .color(TextFormatting.RED));
            }
            ctx.getSender().sendMessage(translation("geocraft.command.geoconfig.reset.success")
                    .arg(modeTextOf(item))
                    .color(TextFormatting.GREEN)
                    .done());
            ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_SUCCESS);
        }
    }

    @FunctionalInterface
    private interface Processor{
        void process(final @Nonnull Object item,final @Nonnull String val) throws CommandException;
    }

    @FunctionalInterface
    private interface ConfigProcessor<P extends ConfigItem<?,P>> extends Processor{
        void process(final @Nonnull P item,final @Nonnull String val) throws CommandException;

        @Override
        @SuppressWarnings("unchecked")
        default void process(@Nonnull final Object item, @Nonnull final String val) throws CommandException{
            this.process((P)item,val);
        }
    }
}
