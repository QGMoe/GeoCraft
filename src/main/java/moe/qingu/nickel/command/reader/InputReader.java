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

package moe.qingu.nickel.command.reader;

import moe.qingu.nickel.command.context.CommandContext;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.text.Texts.plain;
import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class InputReader {
    private final @Nonnull String rawStr;
    private final @Nonnull int[] codepoints;
    private int cursor;
    private CommandContext context;

    public InputReader(final @Nonnull String raw) {
        this.rawStr = raw;
        this.codepoints = raw.codePoints().toArray();
    }

    public void setContext(final @Nonnull CommandContext context) {
        this.context = context;
    }

    public void setCursor(final int cursor) {
        this.cursor = cursor;
    }

    @Nonnull
    public CommandContext getContext() {
        return context;
    }

    public int getLength(){
        return codepoints.length;
    }

    @Nonnull
    public String getInput(){
        return rawStr;
    }

    @Nonnull
    public String getSubInput(final int begin){
        return getSubInput(begin,this.getLength());
    }

    @Nonnull
    public String getSubInput(final int begin,final int end){
        final StringBuilder builder = new StringBuilder();
        for(int i=begin;i<end;i++){
            builder.appendCodePoint(codepoints[i]);
        }
        return builder.toString();
    }

    public int getCursor() {
        return cursor;
    }

    public int peek(){
        return codepoints[this.cursor];
    }

    public boolean isRemainingEmpty(){
        final int cur = this.cursor;
        this.skipIfWhitespace();
        final boolean empty = !canRead();
        this.cursor = cur;
        return empty;
    }

    public boolean canRead(){
        return this.cursor < codepoints.length;
    }

    public boolean canRead(final int readLen){
        return this.cursor + readLen - 1 < codepoints.length;
    }

    public int read(){
        return codepoints[this.cursor++];
    }

    public void skip(){
        this.cursor++;
    }

    public void skipIfWhitespace(){
        while (this.canRead() && Character.isWhitespace(this.peek())){
            this.skip();
        }
    }

    public void skipIfContent(){
        while (this.canRead() && !Character.isWhitespace(this.peek())){
            this.skip();
        }
    }

    @Nonnull
    public String readToken() {
        skipIfWhitespace();
        final int begin = this.cursor;
        while (this.canRead() && !Character.isWhitespace(this.peek())) this.skip();
        return this.getSubInput(begin,this.cursor);
    }

    @Nonnull
    public String readString(){
        skipIfWhitespace();
        if (this.canRead() && (this.peek() == '"' || this.peek() == '\'')){
            final int quote = this.read();
            final StringBuilder builder = new StringBuilder();
            while (true){
                if(!canRead()) throw new IndexOutOfBoundsException();
                int cp = this.read();
                if(cp == '\\') cp = readEscape();
                else if(cp == quote) break;
                builder.appendCodePoint(cp);
            }
            return builder.toString();
        }else return this.readToken();
    }

    public boolean readBoolean() throws CommandException {
        final String token = readToken();
        if("true".equals(token) || "1".equals(token)){
            return true;
        }else if("false".equals(token) || "0".equals(token)){
            return false;
        }else return context.panic(translation("commands.generic.boolean.invalid").arg(token));
    }

    @Nonnull
    public String readRemaining(){
        final @Nonnull String last = this.getSubInput(this.cursor,this.codepoints.length);
        this.cursor = this.codepoints.length;
        return last;
    }

    public int readEscape(){
        if(this.canRead()){
            final int cp = this.read();
            switch (cp){
                case '\\': return '\\';
                case 'b': return '\b';
                case 'f': return '\f';
                case 'n': return '\n';
                case 't': return '\t';
                case 'r': return '\r';
                case '\'': return '\'';
                case '"': return '"';
                case '0': return '\0';
                case 'a': return '\u0007';
                case 'v': return '\u000B';
                case 'u': return readInt(4,4);
                case 'U': return readInt(8,4);
                default:return readInt(3,3);
            }
        }else throw new IndexOutOfBoundsException();
    }

    public int readInt(int len,final int pow){
        final int radix = 1<<pow;
        int value = 0;
        if(!this.canRead(len)) throw new IndexOutOfBoundsException();
        while (len-->0){
            final int digit = Character.digit(this.read(),radix);
            if(digit == -1) throw new IllegalArgumentException();
            value = (value << pow) | digit;
        }
        return value;
    }

    public int readInt() throws CommandException{
        skipIfWhitespace();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Integer.parseInt(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.panic(plain(raw));
        }
    }

    public long readLong() throws CommandException{
        skipIfWhitespace();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Long.parseLong(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.panic(plain(raw));
        }
    }

    public double readDouble() throws CommandException{
        skipIfWhitespace();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Double.parseDouble(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.panic(plain(raw));
        }
    }

    public static boolean isNumber(final int c){
        return c >= '0' && c <='9' || c == '+' || c == '-' || c == '.';
    }
}
