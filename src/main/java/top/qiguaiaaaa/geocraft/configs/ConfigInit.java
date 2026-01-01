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
import top.qiguaiaaaa.geocraft.api.configs.GeoConfig;
import top.qiguaiaaaa.geocraft.api.configs.item.ConfigItem;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.IConfigIntCollection;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.list.ConfigDoubleList;
import top.qiguaiaaaa.geocraft.api.configs.item.collection.list.ConfigList;
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

import static top.qiguaiaaaa.geocraft.configs.ConfigurationLoader.registerConfigCategory;
import static top.qiguaiaaaa.geocraft.configs.ConfigurationLoader.registerConfigItem;

public final class ConfigInit {
    private static boolean hasLoaded = false;
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

    private static void initConfigClass(Class<?> configClass){
        Field[] fields = configClass.getFields();
        for(Field field:fields){
            int modifiers = field.getModifiers();
            if(!Modifier.isStatic(modifiers)) continue;
            if(!Modifier.isPublic(modifiers)) continue;
            try {
                initField(field);
            } catch (IllegalAccessException e) {
                MixinEarlyInit.LOGGER.error("Couldn't get field {} in config class {}",field,configClass);
            }
        }
    }

    private static void initField(@Nonnull Field field) throws IllegalAccessException {
        Object val = field.get(null);
        if(val == null) return;
        if(field.isAnnotationPresent(Config.Ignore.class)){
            return;
        }
        final Config.RangeInt rangeInt = getFieldAnnotation(field,Config.RangeInt.class);
        final Config.RangeDouble rangeDouble = getFieldAnnotation(field,Config.RangeDouble.class);
        final GeoConfig.RangeLong rangeLong = getFieldAnnotation(field,GeoConfig.RangeLong.class);
        final GeoConfig.MaxSize maxSize = getFieldAnnotation(field,GeoConfig.MaxSize.class);

        if(rangeInt != null){
            if(val instanceof ConfigInteger){
                ConfigInteger integer = (ConfigInteger) val;
                integer.setMinValue(rangeInt.min())
                        .setMaxValue(rangeInt.max());
            }else if(val instanceof IConfigIntCollection){
                IConfigIntCollection list = (IConfigIntCollection) val;
                list.setMinValue(rangeInt.min())
                        .setMaxValue(rangeInt.max());
            }
        }else if(rangeDouble != null && val instanceof ConfigDouble) {
            ConfigDouble d = (ConfigDouble) val;
            d.setMinValue((rangeDouble.min() == Double.MIN_VALUE)?Double.NEGATIVE_INFINITY:rangeDouble.min())
                    .setMaxValue((rangeDouble.max() == Double.MAX_VALUE)?Double.POSITIVE_INFINITY:rangeDouble.max());
        }else if(rangeDouble != null && val instanceof ConfigDoubleList){
            ((ConfigDoubleList)val).setMinValue((rangeDouble.min() == Double.MIN_VALUE)?Double.NEGATIVE_INFINITY:rangeDouble.min())
                    .setMaxValue((rangeDouble.max() == Double.MAX_VALUE)?Double.POSITIVE_INFINITY:rangeDouble.max());
        }else if(rangeLong != null && val instanceof ConfigLong){
            ((ConfigLong)val).setMaxValue(rangeLong.max())
                    .setMinValue(rangeLong.min());
        }

        if(val instanceof ConfigList<?>){
            if(field.isAnnotationPresent(GeoConfig.SizeFixed.class)){
                ((ConfigList<?>) val).setListSizeFixed(true);
            }
            if(maxSize !=null){
                ((ConfigList<?>) val).setMaxListSize(maxSize.value());
            }
        }

        if(val instanceof ConfigCategory){
            if(field.isAnnotationPresent(Config.Comment.class)){
                Config.Comment comment = field.getAnnotation(Config.Comment.class);
                ConfigCategory category = (ConfigCategory) val;
                category.setComment(String.join("\n",comment.value()));
            }
            registerConfigCategory((ConfigCategory) val);
        }else if(val instanceof ConfigItem<?>){
            ConfigItem<?> item = (ConfigItem<?>) val;
            if(field.isAnnotationPresent(Deprecated.class)) item.setDeprecated(true);
            registerConfigItem(item);
        }
    }

    @Nullable
    private static <T extends Annotation> T getFieldAnnotation(@Nonnull Field field, @Nonnull Class<T> annotation){
        if(field.isAnnotationPresent(annotation)){
            return field.getAnnotation(annotation);
        }
        return null;
    }
}
