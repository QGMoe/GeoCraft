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

package moe.qingu.nickel.reader;

import com.ibm.icu.lang.UCharacter;
import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.exception.NickelCommandException;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.command.exception.NickelScanEOFSignal;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.node.IDocumentaryNode;
import moe.qingu.nickel.command.utils.CommandBranch;
import moe.qingu.nickel.text.TextBuilder;
import net.minecraft.command.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.nickel.text.Texts.*;

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

    public int peek(){
        return codepoints[this.cursor];
    }

    public boolean isRemainingEmpty(){
        final int cur = this.cursor;
        try{
            this.skipWhitespaces();
            return !canRead();
        }finally {
            this.cursor = cur;
        }
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
        if(canRead()) this.cursor++;
    }

    public void unread(){
        this.cursor--;
    }

    public boolean skipIf(final int cp){
        if (this.canRead() && this.peek() == cp){
            this.skip();
            return true;
        }else return false;
    }

    public void expect(final int cp) throws CommandException {
        if(this.skipIf(cp)) return;
        if(!this.canRead()) this.panic(cursor, I18nKeys.Syntax.EOF);
        else this.panic(cursor, I18nKeys.Syntax.unexpected(cp,this.peek()));
    }

    public void expectOrEnd(final int cp) throws CommandException, NickelScanEOFSignal{
        if(this.canRead()){
            if(this.peek() != cp) this.panic(cursor, I18nKeys.Syntax.unexpected(cp,this.peek()));
            else skip();
        }else throw NickelScanEOFSignal.INSTANCE;
    }

    public void skipCodepoints(final int cp){
        while (this.canRead() && this.peek() == cp) this.skip();
    }

    public void skipWhitespaces(){
        while (this.canRead() && Character.isWhitespace(this.peek())) this.skip();
    }

    public void skipContents(){
        while (this.canRead() && !Character.isWhitespace(this.peek())) this.skip();
    }

    @Nonnull
    public String readToken() {
        skipWhitespaces();
        final int begin = this.cursor;
        while (this.canRead() && !Character.isWhitespace(this.peek())) this.skip();
        return this.getSubInput(begin,this.cursor);
    }

    @Nonnull
    public String readString() throws CommandException {
        skipWhitespaces();
        if (this.canRead() && (this.peek() == '"' || this.peek() == '\'')){
            final int quote = this.read();
            final StringBuilder builder = new StringBuilder();
            while (true){
                if(!canRead()) return panic(this.getCursor(),I18nKeys.Syntax.strNoClose(quote));
                int cp = this.read();
                if(cp == '\\') cp = readEscape();
                else if(cp == quote) break;
                builder.appendCodePoint(cp);
            }
            return builder.toString();
        }else return this.readToken();
    }

    public void scanString() throws NickelScanEOFSignal, CommandException {
        skipWhitespaces();
        if (this.canRead() && (this.peek() == '"' || this.peek() == '\'')){
            final int quote = this.read();
            while (true){
                if(!canRead()) throw NickelScanEOFSignal.INSTANCE;
                final int cp = this.read();
                if(cp == '\\') scanEscape();
                else if(cp == quote) break;
            }
        }else this.skipContents();
    }

    public boolean readBoolean() throws CommandException {
        skipWhitespaces();
        final int begin = this.cursor;
        final String token = readToken();
        if("true".equals(token) || "1".equals(token)){
            return true;
        }else if("false".equals(token) || "0".equals(token)){
            return false;
        }else return context.input.panic(begin,translation(I18nKeys.Syntax.INVALID_BOOLEAN,token));
    }

    @Nonnull
    public String readRemaining(){
        final @Nonnull String last = this.getSubInput(this.cursor,this.codepoints.length);
        this.cursor = this.codepoints.length;
        return last;
    }

    public int readEscape() throws CommandException{
        if(this.canRead()){
            final int cp = this.read();
            switch (cp){
                case '\\': return '\\';
                case 's': return ' ';
                case 'e': return '\u001B';
                case 'b': return '\b';
                case 'f': return '\f';
                case 'n': return '\n';
                case 't': return '\t';
                case 'r': return '\r';
                case '\'': return '\'';
                case '/': return '/';
                case '"': return '"';
                case '0': return '\0';
                case 'a': return '\u0007';
                case 'v': return '\u000B';
                case 'x': return readInt(2,4);
                case 'u': return readInt(4,4);
                case 'U': return readInt(8,4);
                case 'N': return readUnicodeName();
                default:{
                    unread();
                    return readInt(3,3);
                }
            }
        }else return panic(this.getCursor(),translation(I18nKeys.Syntax.STR_TRUNCATED_ESCAPE));
    }

    public int scanEscape() throws CommandException, NickelScanEOFSignal {
        if(this.canRead()){
            final int cp = this.read();
            switch (cp){
                case '\\': return '\\';
                case 's': return ' ';
                case 'e': return '\u001B';
                case 'b': return '\b';
                case 'f': return '\f';
                case 'n': return '\n';
                case 't': return '\t';
                case 'r': return '\r';
                case '\'': return '\'';
                case '/': return '/';
                case '"': return '"';
                case '0': return '\0';
                case 'a': return '\u0007';
                case 'v': return '\u000B';
                case 'x': return scanInt(2,4);
                case 'u': return scanInt(4,4);
                case 'U': return scanInt(8,4);
                case 'N': return scanUnicodeName();
                default:{
                    unread();
                    return scanInt(3,3);
                }
            }
        }else throw NickelScanEOFSignal.INSTANCE;
    }

    public int readUnicodeName() throws CommandException{
        expect('{');
        final int begin = cursor;
        final StringBuilder builder = new StringBuilder();
        while (canRead() && isValidUnicodeNameCP(peek())) builder.appendCodePoint(Character.toUpperCase(this.read()));
        expect('}');
        final int cp = UCharacter.getCharFromName(builder.toString());
        if(cp == -1) this.panic(begin,translation(I18nKeys.Syntax.UNDEFINED_UNICODE,builder));
        return cp;
    }

    public int scanUnicodeName() throws CommandException, NickelScanEOFSignal {
        expectOrEnd('{');
        final int begin = cursor;
        final StringBuilder builder = new StringBuilder();
        while (canRead() && isValidUnicodeNameCP(peek())) builder.appendCodePoint(Character.toUpperCase(this.read()));
        expectOrEnd('}');
        final int cp = UCharacter.getCharFromName(builder.toString());
        if(cp == -1) this.panic(begin,translation(I18nKeys.Syntax.UNDEFINED_UNICODE,builder));
        return cp;
    }

    public boolean isValidUnicodeNameCP(final int cp){
        return cp >= 'A' && cp <= 'Z' || cp >= 'a' && cp <= 'z' || cp >= '0' && cp <= '9' || cp == ' ' || cp == '-';
    }

    public int readInt(int len,final int pow) throws CommandException{
        final int radix = 1<<pow;
        if(!this.canRead(len)) return panic(this.getCursor(),translation(I18nKeys.Syntax.STR_TRUNCATED_ESCAPE_INT,len,radix));
        int value = 0;
        while (len-->0){
            final int digit = Character.digit(this.read(),radix);
            if(digit == -1) return panic(this.getCursor()-1,I18nKeys.Syntax.intInvalidDigit(codepoints[this.cursor-1],radix));
            value = (value << pow) | digit;
        }
        return value;
    }

    public int scanInt(final int len,final int pow) throws CommandException, NickelScanEOFSignal {
        if(!this.canRead(len)) throw NickelScanEOFSignal.INSTANCE;
        return readInt(len,pow);
    }

    public int readInt() throws CommandException{
        skipWhitespaces();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Integer.parseInt(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.input.panic(begin,I18nKeys.Syntax.invalidInt(raw,e));
        }
    }

    public long readLong() throws CommandException{
        skipWhitespaces();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Long.parseLong(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.input.panic(begin,I18nKeys.Syntax.invalidLong(raw,e));
        }
    }

    public double readDouble() throws CommandException{
        skipWhitespaces();
        final int begin = this.cursor;
        while (this.canRead() && isNumber(this.peek())) this.skip();
        final String raw = getSubInput(begin,this.cursor);
        try {
            return Double.parseDouble(raw);
        }catch (final @Nonnull NumberFormatException e){
            this.cursor = begin;
            return context.input.panic(begin,I18nKeys.Syntax.invalidDouble(raw,e));
        }
    }

    @Nonnull
    public <T> T panic(final @Nonnull TextBuilder<?,?> text) throws NickelRuntimeException, NickelCommandException, NickelSyntaxException {
        final @Nullable ICommandNode curNode = context.getCurrentNode();
        final @Nullable CommandBranch curBranch = context.getCurrentBranch();
        if(curBranch != null){
            if(curNode instanceof IDocumentaryNode) throw new NickelSyntaxException(curBranch, (IDocumentaryNode) curNode).withAppendix(text);
            else throw new NickelCommandException(curBranch).withAppendix(translation("nickel.command.exception.base.node.unknown",text));
        }else throw new NickelRuntimeException(translation("nickel.command.exception.runtime.unknown",text));
    }

    @Nonnull
    public <T> T panic(final int loc,final @Nonnull String translationKey) throws NickelCommandException, NickelSyntaxException, NickelRuntimeException {
        return panic(loc,translation(translationKey));
    }

    @Nonnull
    public <T> T panic(final int loc,final @Nonnull TextBuilder<?,?> text) throws NickelRuntimeException, NickelCommandException, NickelSyntaxException {
        try{
            panic(text);
        }catch (final NickelSyntaxException e){
            throw e.withCursor(this,loc);
        }
        throw new RuntimeException();//Won't and shouldn't arrive here
    }

    /*
     * -------------------
     *  Getter and Setter
     * -------------------
     */

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

    public int getCursor() {
        return cursor;
    }

    @Nonnull
    public String getSubInput(final int begin){
        return getSubInput(begin,this.getLength());
    }

    @Nonnull
    public String getSubInput(final int begin,final int end){
        if(end<begin) throw new IllegalArgumentException("Invalid subinput: "+begin+" "+end);
        final StringBuilder builder = new StringBuilder();
        for(int i=begin;i<end;i++){
            builder.appendCodePoint(codepoints[i]);
        }
        return builder.toString();
    }

    /*
     * -------------------
     *     Static
     * -------------------
     */

    public static boolean isNumber(final int c){
        return c >= '0' && c <='9' || c == '+' || c == '-' || c == '.';
    }
}
