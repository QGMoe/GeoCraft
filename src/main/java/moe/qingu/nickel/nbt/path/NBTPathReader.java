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

package moe.qingu.nickel.nbt.path;

import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.nbt.SNBTReader;
import moe.qingu.nickel.nbt.matcher.NBTMatcher;
import moe.qingu.nickel.nbt.path.node.*;
import moe.qingu.nickel.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public class NBTPathReader extends SNBTReader {
    public NBTPathReader(@Nonnull final InputReader input) {
        super(input);
    }

    public static NBTPath readPathFromInput(final @Nonnull InputReader input) throws CommandException {
        return new NBTPathReader(input).readPath(new NBTPath());
    }

    @Nonnull
    public final NBTPath readPath(final @Nonnull NBTPath path) throws CommandException {
        if(!input.canRead()) return path.length() != 0?path:input.panic(input.getCursor(), I18nKeys.NBTPath.EOF);
        switch (input.peek()){
            case '{': {
                readPathCompound(path);
                break;
            }
            case '[': {
                readPathArray(path);
                break;
            }
            default:{
                readPathTag(path);
            }
        }
        if(input.canRead()){
            switch (input.peek()){
                case '{':
                case '[': return readPath(path);
                case '.':{
                    input.skip();
                    return readPath(path);
                }
                default: return Character.isWhitespace(input.peek())?path:input.panic(input.getCursor(),I18nKeys.Syntax.NO_SPLIT);
            }
        }else return path;
    }

    public final void readPathCompound(final @Nonnull NBTPath path) throws CommandException {
        expect('{');
        input.unread();
        final int begin = input.getCursor();
        path.append(new NBTPathCompound(NBTMatcher.toMatcher(readCompound())));
        if(path.length()>1) input.panic(begin,I18nKeys.NBTPath.compoundMisplace(path.length()));
    }

    public final void readPathArray(final @Nonnull NBTPath path) throws CommandException {
        expect('[');
        input.skipWhitespaces();
        if(!input.canRead()) input.panic(input.getCursor(),I18nKeys.NBTPath.LIST_OR_ARR_NO_CLOSE);
        switch (input.peek()){
            case '{':{
                final NBTTagCompound compound = readCompound();
                expect(']');
                final NBTPathListCompound list = new NBTPathListCompound(NBTMatcher.toMatcher(compound));
                path.append(list);
                break;
            } case ']':{
                input.skip();
                path.append(new NBTPathAll());
                break;
            } default:{
                final NBTPathIndex index = new NBTPathIndex(input.readInt());
                expect(']');
                path.append(index);
            }
        }
    }

    public final void readPathTag(final @Nonnull NBTPath path) throws CommandException {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        final String key;
        if(!input.canRead()) input.panic(begin,translation(I18nKeys.NBTPath.EXPECT_TAG_NAME));
        if(input.peek() == '"' || input.peek() == '\'') key = StringUtils.strip(input.readString());
        else key = readUnquotedPathString();
        if(begin == input.getCursor() || key.isEmpty()) input.panic(begin,translation(I18nKeys.NBTPath.EMPTY_TAG_NAME));
        if(!input.canRead() || input.peek() != '{') path.append(new NBTPathTag(key,null));
        else path.append(new NBTPathTag(key,NBTMatcher.toMatcher(readCompound())));
    }


    @Nonnull
    public final String readUnquotedPathString() {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        while (input.canRead() && isAllowedUnquotedPathString(input.peek())) input.skip();
        return input.getSubInput(begin,input.getCursor());
    }

    public static boolean isAllowedUnquotedPathString(final int cp) {
        return cp >= '0' && cp <= '9' || cp >= 'A' && cp <= 'Z' || cp >= 'a' && cp <= 'z' || cp == '_' || cp == '-' || cp == '+';
    }
}
