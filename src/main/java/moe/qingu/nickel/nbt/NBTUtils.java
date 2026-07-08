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

package moe.qingu.nickel.nbt;

import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.node.parameter.generic.StringNode;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.LongStream;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTUtils {
    private static final Field longArrField;
    private static final Field listTypeField;
    private NBTUtils(){}

    static {
        longArrField = getLongArrField();
        listTypeField = getListTypeField();
        longArrField.setAccessible(true);
        listTypeField.setAccessible(true);
    }

    private static @Nonnull Field getLongArrField(){
        final Field[] fields = NBTTagLongArray.class.getDeclaredFields();
        for(final Field field:fields)
            if(!Modifier.isStatic(field.getModifiers()) && field.getType() == long[].class)
                return field;
        throw new RuntimeException("Couldn't get long[] field in NBTTagLongArray!");
    }

    private static @Nonnull Field getListTypeField(){
        final Field[] fields = NBTTagList.class.getDeclaredFields();
        for(final Field field:fields)
            if(!Modifier.isStatic(field.getModifiers()) && field.getType() == byte.class)
                return field;
        throw new RuntimeException("Couldn't get byte field in NBTTagList!");
    }

    public static int empty(final @Nonnull NBTTagList list) throws NickelRuntimeException {
        final int size = list.tagCount();
        if(list.isEmpty()) return size;
        while (!list.isEmpty()) list.removeTag(0);
        try{
            listTypeField.setByte(list,(byte) 0);
        } catch (final @Nonnull IllegalAccessException e) {
            throw new NickelRuntimeException(translation(I18nKeys.NBT.R_CT_LIST,list));
        }
        return size;
    }

    @Nonnull
    public static long[] getLongArray(final @Nonnull NBTTagLongArray longs) throws NickelRuntimeException {
        try {
            return (long[]) longArrField.get(longs);
        } catch (final @Nonnull IllegalAccessException e) {
            throw new NickelRuntimeException(translation(I18nKeys.NBT.R_CT_LONG_ARR,longs));
        }
    }

    @Nonnull
    public static LongStream streamOf(final @Nonnull NBTTagLongArray longs){
        final String arr = longs.toString();
        return Arrays.stream(arr.substring(3,arr.length()-1).split(","))
                .map(e -> e.substring(0,e.length()-1))
                .mapToLong(Long::parseLong);
    }

    @Nullable
    public static String escapeIfQuoted(final @Nonnull String str){

        for(int i=0;i<str.length();){
            final int cp = str.codePointAt(i);
            if(!SNBTReader.isAllowedUnquoted(cp)) return StringNode.escape(str).toString();
            i += Character.charCount(cp);
        }
        return null;
    }

    @Nonnull
    public static String escape(final @Nonnull String str){
        for(int i=0;i<str.length();){
            final int cp = str.codePointAt(i);
            if(!SNBTReader.isAllowedUnquoted(cp)) return String.format("\"%s\"", StringNode.escape(str));
            i += Character.charCount(cp);
        }
        return str;
    }

    public static int sizeOf(final @Nonnull NBTTagCompound compound){
        int size = 0;
        for(final String k:compound.getKeySet()) size += sizeOf(compound.getTag(k));
        return size;
    }

    public static int sizeOf(final @Nonnull NBTTagList list){
        int size = 0;
        for(final NBTBase nbt: list) size += sizeOf(nbt);
        return size;
    }

    public static int sizeOf(final @Nonnull NBTTagByteArray arr){
        return arr.getByteArray().length;
    }

    public static int sizeOf(final @Nonnull NBTTagIntArray arr){
        return arr.getIntArray().length;
    }

    public static int sizeOf(final @Nonnull NBTTagLongArray arr){
        return (int) streamOf(arr).count();
    }

    public static int sizeOf(final @Nonnull NBTBase base){
        if(base instanceof NBTTagCompound){
            return sizeOf((NBTTagCompound) base);
        }else if(base instanceof NBTTagList){
            return sizeOf((NBTTagList) base);
        }else if(base instanceof NBTTagByteArray){
            return sizeOf((NBTTagByteArray) base);
        }else if(base instanceof NBTTagIntArray){
            return sizeOf((NBTTagIntArray) base);
        }else if(base instanceof NBTTagLongArray){
            return sizeOf((NBTTagLongArray) base);
        }else return 1;
    }
}
