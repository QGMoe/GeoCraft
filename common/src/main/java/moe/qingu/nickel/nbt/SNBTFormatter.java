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

import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.nbt.*;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.plain;

/**
 * @author QGMoe
 */
public final class SNBTFormatter {
    public static final TextFormatting COLOR_KEY = TextFormatting.AQUA;
    public static final TextFormatting COLOR_NUMBER = TextFormatting.GOLD;
    public static final TextFormatting COLOR_STRING = TextFormatting.GREEN;
    public static final TextFormatting COLOR_SUFFIX = TextFormatting.RED;

    @Nonnull
    public static TextBuilder<?,?> formatKey(final String str){
        return format(str,COLOR_KEY);
    }

    @Nonnull
    public static TextBuilder<?,?> format(final String str,final TextFormatting contentColor){
        final String escape = NBTUtils.escapeIfQuoted(str);
        if(escape == null) return plain(str).color(contentColor);
        else return plain("\"").color(TextFormatting.WHITE)
                .then(plain(escape).color(contentColor))
                .then(plain("\"").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagCompound compound){
        final TextBuilder<?,?> root = plain("{").color(TextFormatting.WHITE);
        boolean first = true;
        for (final @Nonnull String key:compound.getKeySet()){
            if (first) first = false;
            else root.then(plain(",").color(TextFormatting.WHITE));
            root.then(format(key,COLOR_KEY))
                    .then(plain(":").color(TextFormatting.WHITE))
                    .then(format(compound.getTag(key)));
        }
        return root.then(plain("}").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagByte b){
        return plain(String.valueOf(b.getByte())).color(COLOR_NUMBER)
                .then(plain("b").color(COLOR_SUFFIX));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagShort s){
        return plain(String.valueOf(s.getShort())).color(COLOR_NUMBER)
                .then(plain("s").color(COLOR_SUFFIX));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagInt i){
        return plain(String.valueOf(i.getInt())).color(COLOR_NUMBER);
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagLong l){
        return plain(String.valueOf(l.getLong())).color(COLOR_NUMBER)
                .then(plain("L").color(COLOR_SUFFIX));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagFloat f){
        return plain(String.valueOf(f.getFloat())).color(COLOR_NUMBER)
                .then(plain("f").color(COLOR_SUFFIX));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagDouble d){
        return plain(String.valueOf(d.getDouble())).color(COLOR_NUMBER)
                .then(plain("d").color(COLOR_SUFFIX));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagString s){
        return format(s.getString(),COLOR_STRING);
    }

    @Nonnull
    public static TextBuilder<?,?> getArrayPrefix(final @Nonnull String type){
        return plain("[").color(TextFormatting.WHITE)
                .then(plain(type).color(COLOR_SUFFIX))
                .then(plain(";").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagByteArray arr){
        final TextBuilder<?,?> root = getArrayPrefix("B");
        boolean first = true;
        for(final byte b:arr.getByteArray()){
            if(first) first =false;
            else root.then(plain(",").color(TextFormatting.WHITE));
            root.then(plain(String.valueOf(b)).color(COLOR_NUMBER));
        }
        return root.then(plain("]").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagIntArray arr){
        final TextBuilder<?,?> root = getArrayPrefix("I");
        boolean first = true;
        for(final int i:arr.getIntArray()){
            if(first) first =false;
            else root.then(plain(",").color(TextFormatting.WHITE));
            root.then(plain(String.valueOf(i)).color(COLOR_NUMBER));
        }
        return root.then(plain("]").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagLongArray arr){
        final TextBuilder<?,?> root = getArrayPrefix("L");
        boolean first = true;
        for(final long l:NBTUtils.streamOf(arr).toArray()){
            if(first) first =false;
            else root.then(plain(",").color(TextFormatting.WHITE));
            root.then(plain(String.valueOf(l)).color(COLOR_NUMBER));
        }
        return root.then(plain("]").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTTagList list){
        final TextBuilder<?,?> root = plain("[").color(TextFormatting.WHITE);
        boolean first = true;
        for(final NBTBase base:list){
            if(first) first =false;
            else root.then(plain(",").color(TextFormatting.WHITE));
            root.then(format(base));
        }
        return root.then(plain("]").color(TextFormatting.WHITE));
    }

    @Nonnull
    public static TextBuilder<?,?> format(final NBTBase base){
        if(base instanceof NBTTagCompound) return format((NBTTagCompound) base);
        else if(base instanceof NBTTagList) return format((NBTTagList) base);
        else if(base instanceof NBTTagByte) return format((NBTTagByte) base);
        else if(base instanceof NBTTagShort) return format((NBTTagShort) base);
        else if(base instanceof NBTTagInt) return format((NBTTagInt) base);
        else if(base instanceof NBTTagLong) return format((NBTTagLong) base);
        else if(base instanceof NBTTagFloat) return format((NBTTagFloat) base);
        else if(base instanceof NBTTagDouble) return format((NBTTagDouble) base);
        else if(base instanceof NBTTagByteArray) return format((NBTTagByteArray) base);
        else if(base instanceof NBTTagIntArray) return format((NBTTagIntArray) base);
        else if(base instanceof NBTTagLongArray) return format((NBTTagLongArray) base);
        else if(base instanceof NBTTagString) return format((NBTTagString) base);
        else if(base instanceof NBTTagEnd) return plain("");
        else throw new IllegalArgumentException();
    }
}
