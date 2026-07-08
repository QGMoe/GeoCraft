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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * @author QGMoe
 */
@SuppressWarnings("unused")
public final class ReflectFieldAccessor extends FieldAccessor{
    public ReflectFieldAccessor(@Nonnull final Field field) {
        super(field);
        this.field.setAccessible(true);
    }

    @Override
    public Object get(final Object obj) throws IllegalAccessException {
        return field.get(obj);
    }

    @Override
    public byte getByte(final Object obj) throws IllegalAccessException {
        return field.getByte(obj);
    }

    @Override
    public short getShort(final Object obj) throws IllegalAccessException {
        return field.getShort(obj);
    }

    @Override
    public int getInt(final Object obj) throws IllegalAccessException {
        return field.getInt(obj);
    }

    @Override
    public long getLong(final Object obj) throws IllegalAccessException {
        return field.getLong(obj);
    }

    @Override
    public float getFloat(final Object obj) throws IllegalAccessException {
        return field.getFloat(obj);
    }

    @Override
    public double getDouble(final Object obj) throws IllegalAccessException {
        return field.getDouble(obj);
    }

    @Override
    public char getChar(final Object obj) throws IllegalAccessException {
        return field.getChar(obj);
    }

    @Override
    public boolean getBoolean(final Object obj) throws IllegalAccessException {
        return field.getBoolean(obj);
    }

    @Override
    public void set(final Object obj,final Object replacement) throws IllegalAccessException {
        field.set(obj,replacement);
    }

    @Override
    public void setByte(final Object obj, byte replacement) throws IllegalAccessException {
        field.setByte(obj,replacement);
    }

    @Override
    public void setShort(final Object obj, short replacement) throws IllegalAccessException {
        field.setShort(obj,replacement);
    }

    @Override
    public void setInt(final Object obj, int replacement) throws IllegalAccessException {
        field.setInt(obj,replacement);
    }

    @Override
    public void setLong(final Object obj, long replacement) throws IllegalAccessException {
        field.setLong(obj,replacement);
    }

    @Override
    public void setFloat(final Object obj, float replacement) throws IllegalAccessException {
        field.setFloat(obj,replacement);
    }

    @Override
    public void setDouble(final Object obj, double replacement) throws IllegalAccessException {
        field.setDouble(obj,replacement);
    }

    @Override
    public void setChar(final Object obj, char replacement) throws IllegalAccessException {
        field.setChar(obj,replacement);
    }

    @Override
    public void setBoolean(final Object obj, boolean replacement) throws IllegalAccessException {
        field.setBoolean(obj,replacement);
    }
}
