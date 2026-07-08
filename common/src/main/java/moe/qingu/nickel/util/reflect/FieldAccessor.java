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

package moe.qingu.nickel.util.reflect;

import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author QGMoe
 */
public abstract class FieldAccessor {
    private static final Constructor<? extends FieldAccessor> CONSTRUCTOR;
    protected final Field field;

    static {
        if(SystemUtils.IS_JAVA_1_8){
            try {
                CONSTRUCTOR = ReflectFieldAccessor.class.getConstructor(Field.class);
            } catch (final NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                CONSTRUCTOR = Class.forName("moe.qingu.nickel.util.reflect.HandlerFieldAccessor")
                        .asSubclass(FieldAccessor.class)
                        .getConstructor(Field.class);
            } catch (final NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public FieldAccessor(@Nonnull final Field field) {
        this.field = field;
    }

    public static @Nonnull FieldAccessor of(final Field field) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return CONSTRUCTOR.newInstance(field);
    }

    @Nonnull
    public final Field getField() {
        return field;
    }

    public abstract Object get(final Object obj) throws IllegalAccessException;

    public abstract byte getByte(final Object obj) throws IllegalAccessException;

    public abstract short getShort(final Object obj) throws IllegalAccessException;

    public abstract int getInt(final Object obj) throws IllegalAccessException;

    public abstract long getLong(final Object obj) throws IllegalAccessException;

    public abstract float getFloat(final Object obj) throws IllegalAccessException;

    public abstract double getDouble(final Object obj) throws IllegalAccessException;

    public abstract char getChar(final Object obj) throws IllegalAccessException;

    public abstract boolean getBoolean(final Object obj) throws IllegalAccessException;

    public abstract void set(final Object obj,final Object replacement) throws IllegalAccessException;

    public abstract void setByte(final Object obj,final byte replacement) throws IllegalAccessException;

    public abstract void setShort(final Object obj,final short replacement) throws IllegalAccessException;

    public abstract void setInt(final Object obj,final int replacement) throws IllegalAccessException;

    public abstract void setLong(final Object obj,final long replacement) throws IllegalAccessException;

    public abstract void setFloat(final Object obj,final float replacement) throws IllegalAccessException;

    public abstract void setDouble(final Object obj,final double replacement) throws IllegalAccessException;

    public abstract void setChar(final Object obj,final char replacement) throws IllegalAccessException;

    public abstract void setBoolean(final Object obj,final boolean replacement) throws IllegalAccessException;
}
