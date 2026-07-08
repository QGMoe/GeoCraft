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

package 清汩萌.天圆地方.world.sandbox;

import org.junit.jupiter.api.Assertions;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.工具.YamlUtil;
import 清汩萌.造.空间.空间工具;
import 清汩萌.造.词块.词块;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author QGMoe
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TestArg {
    String value() default "";
    String in() default "";
    Type type() default Type.GENERAL;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBool{
        boolean value();
    }

    enum Type{
        GENERAL{
            @Override
            public final void parse(@Nonnull final Object obj,
                                    @Nonnull final Optional<Map<String,Object>> ext,
                                    @Nonnull final String name,
                                    @Nonnull final Field field) throws IllegalAccessException {
                final @Nonnull Class<?> type = field.getType();
                if(type == String.class){
                    field.set(obj, Optional.ofNullable(Type.getFromOptionalExt(ext, name))
                            .map(Object::toString)
                            .orElseGet(Type.returnNullIfNullable(field,name)));
                }else if(type == int.class){
                    field.setInt(obj, YamlUtil.getInt(ext.orElse(Collections.emptyMap()),name));
                }else if(type == long.class){
                    field.setLong(obj,Optional.ofNullable(Type.getFromOptionalExt(ext,name))
                            .map(Object::toString)
                            .map(Long::parseLong)
                            .orElseThrow(Type.throwIfNull(name)));
                }else if(type == boolean.class){
                    if(field.isAnnotationPresent(DefaultBool.class)){
                        final boolean defaultV = field.getAnnotation(DefaultBool.class).value();
                        field.setBoolean(obj, Optional.ofNullable(Type.getFromOptionalExt(ext,name))
                                .map(o ->{
                                    Assertions.assertTrue(o instanceof Boolean,"arg "+name+" isn't boolean!");
                                    return (Boolean)o;
                                })
                                .orElse(defaultV));
                    }else field.setBoolean(obj, YamlUtil.getBool(ext.orElse(Collections.emptyMap()),name));
                }else if(type == 词块.class){
                    field.set(obj,Optional.ofNullable(Type.getFromOptionalExt(ext, name))
                            .map(Object::toString).map(StringUtil::strip).map(词块::of)
                            .orElseGet(Type.returnNullIfNullable(field,name)));
                }else if(type == Collection.class){
                    field.set(obj,Optional.ofNullable(Type.getFromOptionalExt(ext,name))
                            .filter(o -> o instanceof Collection<?>)
                            .orElseGet(Type.returnNullIfNullable(field,name)));
                }else super.parse(obj, ext, name, field);
            }
        },
        BLOCK_POS{
            @Override
            public void parse(@Nonnull final Object obj,
                              @Nonnull final Optional<Map<String,Object>> ext,
                              @Nonnull final String name,
                              @Nonnull final Field field) throws IllegalAccessException {
                final @Nonnull Class<?> type = field.getType();
                if(type == int[].class){
                    field.set(obj,parseToBlockPos(Type.getFromOptionalExt(ext,name)).orElseGet(Type.returnNullIfNullable(field,name)));
                }else super.parse(obj, ext, name, field);
            }
        };

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public void parse(@Nonnull final Object obj,
                          @Nonnull final Optional<Map<String,Object>> ext,
                          @Nonnull final String name,
                          @Nonnull final Field field) throws IllegalAccessException {
            throw new IllegalArgumentException(name + "used an unsupported type "+field.getType().getName());
        }

        @Nonnull
        private static <T> Supplier<T> returnNullIfNullable(@Nonnull final Field field, @Nonnull final String name){
            return ()-> field.isAnnotationPresent(Nullable.class)?null:Assertions.fail(name +" doesn't exist!");
        }

        @Nonnull
        private static Supplier<IllegalArgumentException> throwIfNull(@Nonnull final String name){
            return () -> new IllegalArgumentException("arg "+name+" doesn't exist!");
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @Nullable
        private static Object getFromOptionalExt(@Nonnull final Optional<Map<String,Object>> ext, @Nonnull final String name){
            return ext.map(e -> e.get(name)).orElse(null);
        }


        @Nonnull
        public static Optional<int[]> parseToBlockPos(final @Nullable Object obj){
            return Optional.ofNullable(obj)
                    .filter(o ->{
                        Assertions.assertTrue(o instanceof Collection<?>);
                        Assertions.assertEquals(3,((Collection<?>) o).size());
                        return true;
                    }).map(o -> ((Collection<?>) o).stream()
                            .map(Object::toString)
                            .mapToInt(Integer::parseInt)
                            .toArray()
                    ).map(空间工具::转换为游戏坐标);
        }
    }
}
