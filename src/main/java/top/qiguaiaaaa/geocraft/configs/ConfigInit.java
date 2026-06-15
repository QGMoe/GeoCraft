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
import top.qiguaiaaaa.geocraft.MixinEarlyInit;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.EffectiveMode;
import top.qiguaiaaaa.geocraft.api.configs.GeoConfig;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.ConfigCollection;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.IConfigIntCollection;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.SizeRequirement;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.list.ConfigDoubleList;
import top.qiguaiaaaa.geocraft.api.configs.item.map.ConfigMap;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigDouble;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigInteger;
import top.qiguaiaaaa.geocraft.api.configs.item.number.ConfigLong;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.exception.ConfigParseError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.BiConsumer;

import static top.qiguaiaaaa.geocraft.configs.ConfigurationLoader.registerConfigCategory;
import static top.qiguaiaaaa.geocraft.configs.ConfigurationLoader.registerConfigItem;

public final class ConfigInit {
    private static boolean hasLoaded = false;

    private static final HashMap<Class<? extends Annotation>, BiConsumer<ConfigItem<?,?>,Annotation>> itemProcessors = new HashMap<>();
    private static final HashMap<Class<? extends Annotation>, BiConsumer<ConfigCategory,Annotation>> categoryProcessors = new HashMap<>();

    static {
        loadItemProcessors();
        loadCategoryProcessors();
    }

    private static void loadItemProcessors(){
        itemProcessors.put(Deprecated.class,(item,a) -> item.setDeprecated(true));
        itemProcessors.put(Config.Comment.class,(item,a)-> item.setComment(String.join("\n",((Config.Comment)a).value())));
        itemProcessors.put(Config.LangKey.class,(item,a) -> item.setTranslationKey(((Config.LangKey)a).value()));
        itemProcessors.put(GeoConfig.Experimental.class,(item,a) -> item.setBeta(true));
        itemProcessors.put(GeoConfig.Since.class,(item,a) -> {
            final GeoConfig.Since since = (GeoConfig.Since) a;
            item.addCommentProperty("自 From","v"+since.value()+" 起 Begin");
        });

        for(final @Nonnull EffectiveMode mode : EffectiveMode.values())
            if(mode.getAnnotation() != null)
                itemProcessors.put(mode.getAnnotation(),(item,a)-> item.setMode(mode));

        itemProcessors.put(Config.RangeInt.class,(item,a)->{
            final Config.RangeInt range = (Config.RangeInt) a;
            if(item instanceof ConfigInteger) ((ConfigInteger)item).setMinValue(range.min()).setMaxValue(range.max());
            else if(item instanceof IConfigIntCollection<?>) ((IConfigIntCollection<?>)item).setMinValue(range.min()).setMaxValue(range.max());});
        itemProcessors.put(Config.RangeDouble.class,(item,a)->{
            final Config.RangeDouble range = (Config.RangeDouble) a;
            final double min = range.min() == Double.MIN_VALUE ? Double.NEGATIVE_INFINITY:range.min();
            final double max = range.max() == Double.MAX_VALUE ? Double.POSITIVE_INFINITY:range.max();
            if(item instanceof ConfigDouble) ((ConfigDouble)item).setMinValue(min).setMaxValue(max);
            else if(item instanceof ConfigDoubleList<?>) ((ConfigDoubleList<?>)item).setMinValue(min).setMaxValue(max);});
        itemProcessors.put(GeoConfig.RangeLong.class,(item,a)->{
            if(item instanceof ConfigLong){
                final GeoConfig.RangeLong range = (GeoConfig.RangeLong) a;
                ((ConfigLong)item).setMinValue(range.min()).setMaxValue(range.max());
            }});

        itemProcessors.put(GeoConfig.Fixed.class,(item, a)->{
            if(item instanceof ConfigCollection<?,?,?>){
                final ConfigCollection<?,?,?> c = (ConfigCollection<?, ?, ?>) item;
                c.requireSize(SizeRequirement.fixed(c.getDefaultValue().size()));
            }else if(item instanceof ConfigMap<?,?>) ((ConfigMap<?,?>)item).setKeyFixed(true);
        });
        itemProcessors.put(GeoConfig.SizeRange.class,(item, a)->{
            if(item instanceof ConfigCollection<?,?,?>){
                final GeoConfig.SizeRange size = (GeoConfig.SizeRange) a;
                ((ConfigCollection<?,?,?>)item).requireSize(SizeRequirement.range(size.min(),size.max()));
            }});

        itemProcessors.put(GeoConfig.KeyComment.class,(item,a)->{
            if(item instanceof ConfigMap<?,?>) ((ConfigMap<?, ?>) item).setKeyComment(String.join("\n",((GeoConfig.KeyComment)a).value()));});
        itemProcessors.put(GeoConfig.ValueComment.class,(item,a)->{
            if(item instanceof ConfigMap<?,?>) ((ConfigMap<?,?>)item).setValueComment(String.join("\n",((GeoConfig.ValueComment)a).value()));});
    }

    private static void loadCategoryProcessors(){
        categoryProcessors.put(Config.Comment.class,(c,a)->c.setComment(String.join("\n",((Config.Comment)a).value())));
    }

    public static void initConfigs(){
        if(hasLoaded) return;
        initConfigClass(GeneralConfig.class);
        initConfigClass(FluidPhysicsConfig.class);
        initConfigClass(AtmosphereConfig.class);
        initConfigClass(SoilConfig.class);
        hasLoaded = true;
    }

    public static void verifyConfigValidity(){
        FluidPhysicsConfig.FLUID_PHYSICS_INFO.forEach((dim,infoWrapper)->{
            if(infoWrapper.getGravity().relativeGravitySize<0) throw new ConfigParseError("Gravity "+infoWrapper.getGravity().relativeGravitySize+" for dimension "+dim+" in "+FluidPhysicsConfig.FLUID_PHYSICS_INFO.getPath()+" can't be negative!");
            GeoFluidSetting.setGravity(dim,infoWrapper.getGravity().relativeGravitySize);
        });
    }

    private static void initConfigClass(final @Nonnull Class<?> configClass){
        final @Nonnull Field[] fields = configClass.getFields();
        for(final @Nonnull  Field field:fields){
            final int modifiers = field.getModifiers();
            if(!Modifier.isStatic(modifiers)) continue;
            if(!Modifier.isPublic(modifiers)) continue;
            try {
                initField(field);
            } catch (final @Nonnull IllegalAccessException e) {
                MixinEarlyInit.LOGGER.error("Couldn't get field {} in config class {}",field,configClass);
            }
        }
    }

    private static void initField(@Nonnull final Field field) throws IllegalAccessException {
        if(field.isAnnotationPresent(Config.Ignore.class)) return;
        final @Nonnull Object val = field.get(null);
        if(val == null) return;

        final @Nonnull Annotation[] annotations = field.getDeclaredAnnotations();
        if(val instanceof ConfigCategory){
            final @Nonnull ConfigCategory c = (ConfigCategory) val;
            processAnnotations(c,annotations,categoryProcessors);
            registerConfigCategory(c);
        }else if(val instanceof ConfigItem<?,?>){
            final @Nonnull ConfigItem<?,?> item = (ConfigItem<?, ?>) val;
            processAnnotations(item,annotations,itemProcessors);
            registerConfigItem(item);
        }
    }

    private static <T> void processAnnotations(final @Nonnull T item,
                                               final @Nonnull Annotation[] annotations,
                                               final @Nonnull HashMap<Class<? extends Annotation>, BiConsumer<T,Annotation>> processors){
        for(final @Nonnull Annotation a:annotations){
            final @Nullable BiConsumer<T,Annotation> processor = processors.get(a.annotationType());
            if(processor != null) processor.accept(item,a);
        }
    }
}
