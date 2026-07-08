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
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.nbt.operation.SNBTOperation;
import moe.qingu.nickel.nbt.operation.SNBTOperations;
import moe.qingu.nickel.util.StringUtils;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public strictfp class SNBTReader {

    protected final InputReader input;

    public SNBTReader(final @Nonnull InputReader input) {
        this.input = input;
    }

    @Nonnull
    public static NBTTagCompound readNBTFromInput(final @Nonnull InputReader input) throws CommandException {
        return new SNBTReader(input).readCompound();
    }

    @Nonnull
    public static NBTTagCompound readSingleNBTFromInput(final @Nonnull InputReader input) throws CommandException {
        final NBTTagCompound compound =  new SNBTReader(input).readCompound();
        if(input.canRead() && !Character.isWhitespace(input.peek())) return input.panic(input.getCursor(),I18nKeys.Syntax.NO_SPLIT);
        return compound;
    }

    @Nonnull
    public final NBTTagCompound readCompound() throws CommandException {
        expect('{');
        final NBTTagCompound compound = new NBTTagCompound();
        while (input.canRead() && input.peek() != '}'){
            final String key = this.readKey();
            input.skipWhitespaces();
            expect(':');
            compound.setTag(key,this.readValue('i'));
            if(this.shouldExit('}')) break;
        }
        expect('}');
        return compound;
    }

    @Nonnull
    public final NBTBase readListOrArray() throws CommandException {
        expect('[');
        input.skipWhitespaces();
        final int begin = input.getCursor();
        if(!input.canRead()) return input.panic(begin,translation(I18nKeys.NBT.EXPECT_VALUE));

        switch (input.peek()){
            case 'B':
            case 'I':
            case 'L':{
                if(!input.canRead(2)) break;
                final int cp;
                try{
                    cp = input.read();
                    if(input.read() != ';') break;
                }finally {
                    input.setCursor(begin);
                }
                return readArray((char) Character.toLowerCase(cp));
            }
        }

        return readList();
    }

    @Nonnull
    private NBTTagList readList() throws CommandException {
        int type = -1;
        final @Nonnull NBTTagList list = new NBTTagList();
        while (input.canRead() && input.peek() != ']'){
            input.skipWhitespaces();
            final int begin = input.getCursor();
            final NBTBase value = this.readValue('i');
            if(type == -1) type = value.getId();
            else if(value.getId() != type) return input.panic(begin, translation(I18nKeys.NBT.LIST_MISMATCH)
                    .arg(NBTBase.NBT_TYPES[type],NBTBase.NBT_TYPES[value.getId()]));
            list.appendTag(value);
            if(shouldExit(']')) break;
        }
        expect(']');
        return list;
    }


    @Nonnull
    @SuppressWarnings("unchecked")
    private NBTBase readArray(final char cp) throws CommandException {
        final int type = getArrayContentTypeByChar(cp);
        final @Nonnull List<Number> array = new ArrayList<>();
        while (input.canRead() && input.peek() != ']'){
            input.skipWhitespaces();
            final int begin = input.getCursor();
            final NBTBase value = this.readValue(cp);
            if(value.getId() != type && value.getId() > type) //对于整型类型，id越大，范围越大，小范围装不了大范围
                return input.panic(begin, translation(I18nKeys.NBT.ARR_MISMATCH).arg(NBTBase.NBT_TYPES[type],NBTBase.NBT_TYPES[value.getId()]));
            final NBTPrimitive primitive = (NBTPrimitive) value;
            if(cp == 'b') array.add(primitive.getByte());
            else if(cp == 'l') array.add(primitive.getLong());
            else array.add(primitive.getInt());
            if(shouldExit(']')) break;
        }
        expect(']');
        switch (cp){
            case 'b': return new NBTTagByteArray((List<Byte>) (List<?>) array);
            case 'l': return new NBTTagLongArray((List<Long>) (List<?>) array);
            default:return new NBTTagIntArray((List<Integer>) (List<?>) array);
        }
    }

    private int getArrayContentTypeByChar(final char c){
        switch (c){
            case 'B': return Constants.NBT.TAG_BYTE;
            case 'L': return Constants.NBT.TAG_LONG;
            default:return Constants.NBT.TAG_INT;
        }
    }

    @Nonnull
    public final String readKey() throws CommandException {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        final String key;
        if(!input.canRead()) return input.panic(begin,translation(I18nKeys.NBT.EXPECT_KEY));
        if(input.peek() == '"' || input.peek() == '\'') key = StringUtils.strip(input.readString());
        else key = readUnquotedString();
        if(begin == input.getCursor() || key.isEmpty()) return input.panic(begin,translation(I18nKeys.NBT.EMPTY_KEY));
        return key;
    }

    @Nonnull
    public String readUnquotedString(){
        input.skipWhitespaces();
        final int begin = input.getCursor();
        while (input.canRead() && isAllowedUnquoted(input.peek())) input.skip();
        return input.getSubInput(begin,input.getCursor());
    }

    @Nonnull
    public NBTBase readValue(final char defaultNumType) throws CommandException {
        input.skipWhitespaces();
        if(!input.canRead()) return input.panic(input.getCursor(),translation(I18nKeys.NBT.EXPECT_VALUE));
        switch (input.peek()){
            case '{': return readCompound();
            case '[': return readListOrArray();
            case '\'':
            case '"': return new NBTTagString(input.readString());
            default: return readTypedValue(defaultNumType);
        }
    }

    @Nonnull
    public NBTBase readTypedValue(final char defaultNumType) throws CommandException {
        input.skipWhitespaces();
        final int begin = input.getCursor();
        final String raw = readUnquotedString();
        switch (raw) {
            case "":
                return input.panic(begin, translation(I18nKeys.NBT.EXPECT_VALUE));
            case "true":
                return new NBTTagByte((byte) 1);
            case "false":
                return new NBTTagByte((byte) 0);
        }
        if(input.canRead() && input.peek() == '(') return readFunction(begin,raw);
        if(InputReader.isNumber(raw.charAt(0))){
            try{
                if(raw.startsWith("0x")) return parseInt(begin+2,raw.substring(2),16,false,false,defaultNumType);
                else if(raw.startsWith("+0x") || raw.startsWith("-0x"))
                    return parseInt(begin+3,(raw.charAt(0) + raw.substring(3)),16,false,true,defaultNumType);
                if(raw.length() > 2 && raw.startsWith("0b")) return parseInt(begin+2,raw.substring(2),2,false,false,defaultNumType); //等于2时是Byte 0
                else if(raw.length() > 3 && (raw.startsWith("+0b") || raw.startsWith("-0b")))
                    return parseInt(begin+3,raw.charAt(0) + raw.substring(3),2,false,true,defaultNumType);
                try {
                    return parseInt(begin,raw,10,true,true,defaultNumType);
                }catch (final CommandException | NumberFormatException e){
                    return parseFloat(begin,raw);
                }
            }catch (final NumberFormatException e){
                return input.panic(begin,I18nKeys.NBT.invalidNum(raw,e));
            }
        }else return new NBTTagString(raw);
    }

    @Nonnull
    public NBTBase readFunction(final int begin,final @Nonnull String name) throws CommandException {
        expect('(');
        final List<NBTBase> args = new ArrayList<>();
        boolean beforeAnyArgs = true;
        while (input.canRead() && input.peek() != ')'){
            input.skipWhitespaces();
            if(beforeAnyArgs && input.peek() == ')') break; // func()
            beforeAnyArgs = false;
            args.add(this.readValue('i'));
            if(shouldExit(')')) break;
        }
        expect(')');
        final NBTBase[] argsArr = args.toArray(new NBTBase[0]);
        final @Nullable SNBTOperation func = SNBTOperations.resolve(name,argsArr);
        if(func == null) return input.panic(begin,I18nKeys.NBT.optUndefined(name,args));
        try{
            return func.invoke(argsArr);
        }catch (final NickelRuntimeException e){
            return input.panic(begin,I18nKeys.NBT.optFailed(func).hoverTo(HoverEvent.Action.SHOW_TEXT).content(e.getInformation()));
        }catch (final Exception e){
            return input.panic(begin,I18nKeys.NBT.optFailed(func).hoverTo(HoverEvent.Action.SHOW_TEXT).content(e.getLocalizedMessage()));
        }
    }

    @Nonnull
    public NBTBase parseInt(final int begin,
                            final @Nonnull String raw,
                            final int radix,
                            final boolean checkZero,
                            final boolean canSign,
                            final char defaultType) throws CommandException{
        boolean inDigit = false;
        boolean zero = false;
        boolean hasMark = false;
        boolean unsigned = false;
        int cursor = 0;
        loop: for(;cursor<raw.length();cursor++){
            final char c = raw.charAt(cursor);
            final int digit = Character.digit(c,radix);
            if(canSign && cursor==0 && (c == '+' || c == '-')) continue ;
            else if(digit != -1){
                if(checkZero && zero) return input.panic(begin + cursor,I18nKeys.NBT.INVALID_0); //已经有前导0
                if(digit == 0 && !inDigit) zero = true; //第一个0可以接受
                inDigit = true;
                continue ;
            } else if(inDigit){
                switch (c){
                    case 'u':
                    case 'U': unsigned = true;
                    case 's':
                    case 'S': hasMark = unsigned || raw.length() > cursor+1; //signed的和Short符号一样，如后面还有则这必然是short
                    case 'b':
                    case 'B':
                    case 'l':
                    case 'L': break loop;
                    case '_':{
                        cursor = checkValid_(begin,raw,radix,cursor)-1;//continue会+1
                        continue;
                    }
                }
            }
            return input.panic(begin+cursor,I18nKeys.NBT.intInvalidChar(radix,c));
        }
        final char type;
        if(hasMark){
            if(raw.length() == cursor+1) type = defaultType;
            else if(raw.length() == cursor+2) type = getIntType(begin+cursor+1,raw.charAt(cursor+1));
            else return input.panic(begin+cursor+2,I18nKeys.NBT.intEscape(cursor+2,raw)); //最后最多两个字母了
        }else if(raw.length() > cursor+1)
            return input.panic(begin+cursor+1,I18nKeys.NBT.intEscape(cursor+1,raw));  //没符号标记，数字后必须最多只剩下一个字母
        else if(raw.length() == cursor +1) type = getIntType(begin+cursor,raw.charAt(cursor));
        else type = defaultType;
        final String val = raw.substring(0,cursor).replace("_","");
        switch (type){
            case 'b': return new NBTTagByte((byte) (unsigned?validateRange(begin,Long.parseUnsignedLong(val,radix),0,255):Byte.parseByte(val,radix)));
            case 's': return new NBTTagShort((short) (unsigned?validateRange(begin,Long.parseUnsignedLong(val,radix),0,65535):Short.parseShort(val,radix)));
            case 'i': return new NBTTagInt(unsigned?Integer.parseUnsignedInt(val,radix):Integer.parseInt(val,radix));
            case 'l': return new NBTTagLong(unsigned?Long.parseUnsignedLong(val,radix):Long.parseLong(val,radix));
            default:return input.panic(begin,translation(I18nKeys.NBT.INT_INVALID_TYPE,type));
        }
    }

    @Nonnull
    public NBTBase parseFloat(final int begin,final @Nonnull String raw) throws CommandException{
        final int last = raw.charAt(raw.length()-1);
        final char type = last >= '0' && last <= '9' || last == '.' ? 'd': getFloatType(begin,raw);
        for(int i=0;i<raw.length();i++) if(raw.charAt(i) == '_') i = checkValid_(begin,raw,10,i);
        for(int i=raw.length()-1;i>=0;i--) if(raw.charAt(i) == '0') i = checkPreZeroInFloat(begin,raw,10,i);
        final String val = raw.replace("_","");
        switch (type){
            case 'f':{
                final float res = Float.parseFloat(val);
                if(Float.isInfinite(res) || Float.isNaN(res)) return input.panic(begin,I18nKeys.NBT.INFINITY);
                return new NBTTagFloat(res);
            }
            case 'd':{
                final double res = Double.parseDouble(val); //那些.1f、.1d、1.f、1.d、1ef之类的写法Java原生支持
                if(Double.isInfinite(res) || Double.isNaN(res)) return input.panic(begin,I18nKeys.NBT.INFINITY);
                return new NBTTagDouble(res);
            }
            default:return input.panic(begin,translation(I18nKeys.NBT.FLOAT_INVALID_TYPE,type));
        }
    }

    /**
     * 检查非法的前导0
     * @param raw 字符串
     * @param radix 进制
     * @param loc 一个0所在的位置
     * @return 跳过这个0向前的第一个非0位置的后一位
     */
    public int checkPreZeroInFloat(final int begin,final @Nonnull String raw, final int radix, final int loc) throws CommandException{
        int i=loc-1;
        while (i>=0 && raw.charAt(i) == '0') i--;
        if(i == loc -1 && (loc >= raw.length()-1 || Character.digit(raw.charAt(loc+1),radix) == -1)) return loc; //单个0也在最后，是没问题的，否则0后面接了数字
        if(i < 0 || raw.charAt(i) != '.' && Character.digit(raw.charAt(i),radix) == -1)
            return input.panic(begin+i+1,I18nKeys.NBT.INVALID_0);//这一串0最前面不是数字或小数点，是非法的前导0
        else return i+1;
    }

    public int checkValid_(final int begin,final @Nonnull String raw,final int radix,final int loc) throws CommandException{
        int i=loc-1;
        int j=loc+1;
        while (i>=0 && raw.charAt(i) == '_') i--;
        while (j < raw.length() && raw.charAt(j) == '_') j++;
        if(i >= 0 && Character.digit(raw.charAt(i),radix) != -1 && j < raw.length() && Character.digit(raw.charAt(j),radix) != -1) return j;
        else return input.panic(begin+loc,I18nKeys.NBT.INVALID_NUM_SPLIT);
    }

    private char getIntType(final int begin,final char cp) throws CommandException{
        switch (cp){
            case 'b':
            case 'B': return 'b';
            case 's':
            case 'S': return 's';
            case 'l':
            case 'L': return  'l';
            default:return input.panic(begin,translation(I18nKeys.NBT.INT_INVALID_TYPE,cp));
        }
    }

    private char getFloatType(final int begin,final @Nonnull String raw) throws CommandException{
        final char c = raw.charAt(raw.length()-1);
        switch (c){
            case 'f':
            case 'F': return 'f';
            case 'd':
            case 'D': return 'd';
            default:return input.panic(begin+raw.length()-1,translation(I18nKeys.NBT.FLOAT_INVALID_TYPE,c));
        }
    }

    private long validateRange(final int begin,final long num,final long min,final long max) throws CommandException{
        if(num<min) return input.panic(begin,I18nKeys.NBT.tooMin(num,min));
        if(num>max) return input.panic(begin,I18nKeys.NBT.tooMax(num,max));
        return num;
    }

    public boolean shouldExit(final int end){
        input.skipWhitespaces();
        if(input.canRead() && input.peek() == ','){
            input.skip();
            input.skipWhitespaces();
            return input.canRead() && input.peek() == end;
        }return true;
    }

    public void expect(final int cp) throws CommandException {
        if(input.skipIf(cp)) return;
        if(!input.canRead()) input.panic(input.getCursor(), I18nKeys.NBT.EOF);
        else input.panic(input.getCursor(),I18nKeys.NBT.unexpected(cp,input.peek()));
    }

    public static boolean isAllowedUnquoted(final int cp) {
        return cp >= '0' && cp <= '9' || cp >= 'A' && cp <= 'Z' || cp >= 'a' && cp <= 'z' || cp == '_' || cp == '-' || cp == '.' || cp == '+';
    }

}
