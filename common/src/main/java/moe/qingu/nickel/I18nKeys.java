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

package moe.qingu.nickel;

import moe.qingu.nickel.nbt.operation.SNBTOperation;
import moe.qingu.nickel.nbt.operation.SNBTOperations;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.stream.Collectors;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.translation;
import static moe.qingu.nickel.util.StringUtils.stringOf;

/**
 * @author QGMoe
 */
public final class I18nKeys {
    public static final String INT = "nickel.command.parameter.generic.integer";
    public static final String LONG = "nickel.command.parameter.generic.long";
    public static final String DOUBLE = "nickel.command.parameter.generic.double";
    public static final String UUID = "nickel.command.parameter.generic.uuid";
    public static final String STRING = "nickel.command.parameter.generic.string";
    public static final String TOKEN = "nickel.command.parameter.generic.token";
    public static final String GREED = "nickel.command.parameter.generic.greed";

    public static final class Syntax{
        public static final String NO_SPLIT = "nickel.command.syntax.no_split";
        public static final String STR_NO_CLOSE = "nickel.command.syntax.string.no_pair";
        public static final String UNEXPECTED = "nickel.command.syntax.unexpected";
        public static final String EOF = "nickel.command.syntax.eof";
        public static final String UNDEFINED_UNICODE = "nickel.command.syntax.undefined_unicode";
        public static final String STR_TRUNCATED_ESCAPE = "nickel.command.syntax.string.truncated_escape";
        public static final String STR_TRUNCATED_ESCAPE_INT = "nickel.command.syntax.string.truncated_escape.int";
        public static final String NUM_INVALID = "nickel.command.syntax.num.invalid";
        public static final String INT_INVALID_DIGIT = "nickel.command.syntax.int.invalid_digit";
        public static final String INVALID_BOOLEAN = "commands.generic.boolean.invalid";

        public static @Nonnull TextBuilder<?,?> strNoClose(final int quote){
            return translation(STR_NO_CLOSE,stringOf(quote));
        }

        public static @Nonnull TextBuilder<?,?> invalidInt(final @Nonnull String raw,final @Nonnull NumberFormatException e){
            return translation(NUM_INVALID,translation(INT),raw,e.getMessage());
        }

        public static @Nonnull TextBuilder<?,?> invalidLong(final @Nonnull String raw,final @Nonnull NumberFormatException e){
            return translation(NUM_INVALID,translation(LONG),raw,e.getMessage());
        }

        public static @Nonnull TextBuilder<?,?> invalidDouble(final @Nonnull String raw,final @Nonnull NumberFormatException e){
            return translation(NUM_INVALID,translation(DOUBLE),raw,e.getMessage());
        }

        public static @Nonnull TextBuilder<?,?> intInvalidDigit(final int digit,final int radix){
            return translation(INT_INVALID_DIGIT,stringOf(digit),radix);
        }

        public static @Nonnull TextBuilder<?,?> unexpected(final int need, final int fact){
            return translation(UNEXPECTED,stringOf(need),stringOf(fact));
        }
    }

    public static final class NBT{
        public static final String EOF = "nickel.command.nbt.syntax.eof";
        public static final String EXPECT_KEY = "nickel.command.nbt.syntax.expect_key";
        public static final String EXPECT_VALUE = "nickel.command.nbt.syntax.expect_value";
        public static final String EMPTY_KEY = "nickel.command.nbt.syntax.empty_key";
        public static final String TOO_MIN = "nickel.command.nbt.syntax.too_min";
        public static final String TOO_MAX = "nickel.command.nbt.syntax.too_max";
        public static final String INVALID_NUM_SPLIT = "nickel.command.nbt.syntax.invalid_";
        public static final String INVALID_0 = "nickel.command.nbt.syntax.invalid_pre0";
        public static final String INVALID_NUM = "nickel.command.nbt.syntax.invalid_num";

        public static final String INT_INVALID_TYPE = "nickel.command.nbt.syntax.int.invalid_type";
        public static final String INT_ESCAPE = "nickel.command.nbt.syntax.int.invalid_escape";
        public static final String INT_INVALID_CHAR = "nickel.command.nbt.syntax.int.invalid_char";
        public static final String FLOAT_INVALID_TYPE = "nickel.command.nbt.syntax.float.invalid_type";
        public static final String INFINITY = "nickel.command.nbt.syntax.float.infinity";

        public static final String LIST_MISMATCH = "nickel.command.nbt.syntax.list.mismatch";
        public static final String ARR_MISMATCH = "nickel.command.nbt.syntax.arr.mismatch";

        public static final String OPT_UNDEFINED = "nickel.command.nbt.operation.undefined";
        public static final String OPT_FAILED = "nickel.command.nbt.operation.exception";

        public static final String R_CT_LONG_ARR = "nickel.command.nbt.runtime.critical.long_arr";
        public static final String R_CT_LIST = "nickel.command.nbt.runtime.critical.list";


        public static @Nonnull TextBuilder<?,?> tooMin(final long num,final long min){
            return translation(TOO_MIN,num,min);
        }

        public static @Nonnull TextBuilder<?,?> tooMax(final long num,final long max){
            return translation(TOO_MAX,num,max);
        }

        public static @Nonnull TextBuilder<?,?> invalidNum(final @Nonnull String raw,final @Nonnull NumberFormatException e){
            return translation(INVALID_NUM,raw,e.getMessage());
        }

        public static @Nonnull TextBuilder<?,?> intEscape(final int cursor,final @Nonnull String str){
            return translation(INT_ESCAPE,str.substring(cursor));
        }

        public static @Nonnull TextBuilder<?,?> intInvalidChar(final int radix,final char c){
            return translation(INT_INVALID_CHAR,radix,c);
        }

        public static @Nonnull TextBuilder<?,?> optFailed(final @Nonnull SNBTOperation operation){
            return translation(OPT_FAILED, SNBTOperations.signatureOf(operation));
        }

        public static @Nonnull TextBuilder<?,?> optUndefined(final @Nonnull String name,final @Nonnull List<NBTBase> args){
            return translation(OPT_FAILED, translation(OPT_UNDEFINED)
                    .arg(plain(name+'('+ args.stream()
                            .map(NBTBase::toString)
                            .collect(Collectors.joining(",")) +')')
                            .color(TextFormatting.GOLD)
                            .underlined(true))
                    .arg(plain(SNBTOperations.signatureOf(name,args))
                            .color(TextFormatting.AQUA).underlined(true)));
        }

    }

    public static final class NBTPath{
        public static final String NODE_COMPOUND = "nickel.command.nbt.path.node.compound";
        public static final String NODE_ALL =  "nickel.command.nbt.path.node.all";
        public static final String NODE_INDEX = "nickel.command.nbt.path.node.index";
        public static final String NODE_LIST_COMPOUND = "nickel.command.nbt.path.node.list_compound";
        public static final String NODE_TAG = "nickel.command.nbt.path.node.tag";
        public static final String NODE_TAG_COMPOUND = "nickel.command.nbt.path.node.tag_compound";
        public static final String NODE_METHOD = "nickel.command.nbt.path.node.method";

        public static final String METHOD_UNDEFINED = "nickel.command.nbt.path.method.undefined";

        public static final String EOF = "nickel.command.nbt.path.syntax.eof";
        public static final String COMPOUND_MISPLACE = "nickel.command.nbt.path.syntax.compound_incorrected_place";
        public static final String LIST_OR_ARR_NO_CLOSE = "nickel.command.nbt.path.syntax.array_not_close";
        public static final String EXPECT_TAG_NAME = "nickel.command.nbt.path.syntax.expect_name";
        public static final String EMPTY_TAG_NAME = "nickel.command.nbt.path.syntax.empty_name";
        public static final String EMPTY_METHOD_NAME = "nickel.command.nbt.path.syntax.empty_method";

        public static final String SET_INDEX_MISMATCH = "nickel.command.nbt.path.set.index.mismatch";
        public static final String SET_INDEX_LIST_OUT = "nickel.command.nbt.path.set.index.out_of_index.list";
        public static final String SET_INDEX_ARR_OUT = "nickel.command.nbt.path.set.index.out_of_index.arr";
        public static final String SET_INDEX_NO_TYPE = "nickel.command.nbt.path.set.index.not_type";
        public static final String SET_INDEX_NO_BYTE = "nickel.command.nbt.path.set.index.not_byte";
        public static final String SET_INDEX_NO_INT = "nickel.command.nbt.path.set.index.not_int";
        public static final String SET_INDEX_NO_LONG = "nickel.command.nbt.path.set.index.not_long";
        public static final String SET_TAG_MISMATCH = "nickel.command.nbt.path.set.tag.mismatch";
        public static final String SET_TAG_MISVALUE = "nickel.command.nbt.path.set.tag.misvalue";
        public static final String SET_ALL_MISMATCH = "nickel.command.nbt.path.set.all.mismatch";
        public static final String SET_ALL_NO_BYTE = "nickel.command.nbt.path.set.all.not_byte";
        public static final String SET_ALL_NO_INT = "nickel.command.nbt.path.set.all.not_int";
        public static final String SET_ALL_NO_LONG = "nickel.command.nbt.path.set.all.not_long";
        public static final String SET_LIST_COM_MISMATCH = "nickel.command.nbt.path.set.list_com.mismatch";
        public static final String SET_LIST_COM_NO_COMPOUND = "nickel.command.nbt.path.set.list_com.not_compound";
        public static final String SET_LIST_COM_NO_LIST_COM = "nickel.command.nbt.path.set.list_com.not_list_com";
        public static final String SET_EMPTY = "nickel.command.nbt.path.set.empty";
        public static final String SET_UNSUPPORTED = "nickel.command.nbt.path.set.unsupported";
        public static final String SET_NOT_FOUND = "nickel.command.nbt.path.set.not_found";
        public static final String SET_FOUND_MULTI = "nickel.command.nbt.path.set.multi_found";

        public static final String INSERT_INDEX_MISMATCH = "nickel.command.nbt.path.insert.index.mismatch";
        public static final String INSERT_INDEX_OUT = "nickel.command.nbt.path.insert.index.out_of_index";
        public static final String INSERT_INDEX_NO_TYPE = "nickel.command.nbt.path.insert.index.not_type";
        public static final String INSERT_EMPTY = "nickel.command.nbt.path.insert.empty";
        public static final String INSERT_UNSUPPORTED = "nickel.command.nbt.path.insert.unsupported";
        public static final String INSERT_NOT_FOUND = "nickel.command.nbt.path.insert.not_found";
        public static final String INSERT_FOUND_MULTI = "nickel.command.nbt.path.insert.multi_found";

        public static final String REMOVE_INDEX_MISMATCH = "nickel.command.nbt.path.remove.index.mismatch";
        public static final String REMOVE_TAG_MISMATCH = "nickel.command.nbt.path.remove.tag.mismatch";
        public static final String REMOVE_TAG_MISVALUE = "nickel.command.nbt.path.remove.tag.misvalue";
        public static final String REMOVE_ALL_MISMATCH = "nickel.command.nbt.path.remove.all.mismatch";
        public static final String REMOVE_LIST_COM_MISMATCH = "nickel.command.nbt.path.remove.list_com.mismatch";
        public static final String REMOVE_EMPTY = "nickel.command.nbt.path.remove.empty";
        public static final String REMOVE_UNSUPPORTED = "nickel.command.nbt.path.remove.unsupported";
        public static final String REMOVE_NOT_FOUND = "nickel.command.nbt.path.remove.not_found";
        public static final String REMOVE_MULTI_FOUND = "nickel.command.nbt.path.remove.multi_found";

        public static @Nonnull TextBuilder<?,?> compoundMisplace(final int fact){
            return translation(COMPOUND_MISPLACE,translation(NODE_COMPOUND),fact);
        }

        public static @Nonnull TextBuilder<?,?> methodUndefined(final @Nonnull String name,final @Nonnull List<NBTBase> args){
            return translation(METHOD_UNDEFINED)
                    .arg(plain(name+'('+ args.stream()
                            .map(NBTBase::toString)
                            .collect(Collectors.joining(",")) +')')
                            .color(TextFormatting.GOLD)
                            .underlined(true))
                    .arg(plain(SNBTOperations.signatureOf(name,args))
                            .color(TextFormatting.AQUA).underlined(true));
        }
    }

    private I18nKeys(){}
}
