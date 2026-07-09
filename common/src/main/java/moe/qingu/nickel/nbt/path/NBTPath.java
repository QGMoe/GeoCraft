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
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.path.node.*;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static moe.qingu.nickel.text.Texts.translation;

/**
 * @author QGMoe
 */
public final class NBTPath {
    private final List<NBTPathNode> nodes = new ArrayList<>();

    public @Nonnull NBTPath append(final @Nonnull NBTPathNode node){
        this.nodes.add(node);
        return this;
    }

    public int length(){
        return this.nodes.size();
    }

    @Nonnull
    public NBTPath subPath(final int subLen){
        if(subLen > length()) throw new IllegalArgumentException();
        final NBTPath path = new NBTPath();
        for(int i=0;i<subLen;i++){
            path.append(nodes.get(i));
        }
        return path;
    }

    @Nonnull
    public List<NBTBase> resolve(final @Nonnull NBTTagCompound compound){
        Stream<NBTBase> c = Stream.of(compound);
        for(final NBTPathNode node:nodes) c = c
                .flatMap(e -> node.resolve(e).stream());
        return c.collect(Collectors.toList());
    }

    public int set(final @Nonnull NBTTagCompound compound,final @Nonnull NBTBase tag,final boolean allowMulti) throws NickelRuntimeException {
        if(this.nodes.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_EMPTY));
        final List<NBTBase> nbt = resolveParents(compound);
        if(nbt.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_NOT_FOUND));
        else if(!allowMulti && nbt.size() >1) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_FOUND_MULTI));
        final NBTPathNode last = nodes.get(nodes.size()-1);
        int res = 0;
        if(last instanceof NBTPathModifiableNode)
            for(final NBTBase n:nbt)
                try {
                    res += ((NBTPathModifiableNode) last).set(n,tag);
                }catch (final NickelRuntimeException e){
                    if(!allowMulti) throw e;
                }
        else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.SET_UNSUPPORTED,translation(last.getLocalName()).color(TextFormatting.GOLD)));
        return res;
    }

    public int insert(final @Nonnull NBTTagCompound compound,final @Nonnull NBTBase tag,final boolean allowMulti) throws NickelRuntimeException{
        if(this.nodes.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_EMPTY));
        final List<NBTBase> nbt = resolveParents(compound);
        if(nbt.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_NOT_FOUND));
        else if(!allowMulti && nbt.size() >1) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_FOUND_MULTI));
        final NBTPathNode last = nodes.get(nodes.size()-1);
        int res = 0;
        if(last instanceof NBTPathIndex)
            for(final NBTBase n:nbt)
                try {
                    res += ((NBTPathIndex) last).insert(n,tag);
                }catch (final NickelRuntimeException e){
                    if(!allowMulti) throw e;
                }
        else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.INSERT_UNSUPPORTED,translation(last.getLocalName()).color(TextFormatting.GOLD)));
        return res;
    }

    public int remove(final @Nonnull NBTTagCompound compound,final boolean allowMulti) throws NickelRuntimeException {
        if(this.nodes.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_EMPTY));
        final List<NBTBase> nbt = resolveParents(compound);
        if(nbt.isEmpty()) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_NOT_FOUND));
        else if(!allowMulti && nbt.size() >1) throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_MULTI_FOUND));
        final NBTPathNode last = nodes.get(nodes.size()-1);
        int res = 0;
        if(last instanceof NBTPathModifiableNode)
            for(final NBTBase n:nbt)
                try {
                    res += ((NBTPathModifiableNode) last).remove(n);
                }catch (final NickelRuntimeException e){
                    if(!allowMulti) throw e;
                }
        else throw new NickelRuntimeException(translation(I18nKeys.NBTPath.REMOVE_UNSUPPORTED,translation(last.getLocalName()).color(TextFormatting.GOLD)));
        return res;
    }

    public @Nonnull List<NBTBase> init(final @Nonnull NBTTagCompound compound) throws NickelRuntimeException {
        Stream<NBTBase> c = Stream.of(compound);
        for(int i=0;i<nodes.size();i++){
            final int cur = i;
            c = c.peek(e -> init(e,cur)).flatMap(e -> nodes.get(cur).resolve(e).stream());
        }
        return c.collect(Collectors.toList());
    }


    public @Nonnull List<NBTBase> resolveParents(final @Nonnull NBTTagCompound compound){
        Stream<NBTBase> c = Stream.of(compound);
        for(int i=0;i<length()-1;i++){
            final int cur = i;
            c = c.flatMap(e -> nodes.get(cur).resolve(e).stream());
        }
        return c.collect(Collectors.toList());
    }

    private void init(final @Nonnull NBTBase base,final int cur) {
        if (this.nodes.size() - cur > 0) {
            final NBTPathNode node = this.nodes.get(cur);
            if (node instanceof NBTPathInitableNode) {
                final NBTPathInitableNode init = (NBTPathInitableNode) node;
                final NBTPathNode next = this.nodes.size()-cur > 1? this.nodes.get(cur + 1):null;
                if (next == null || next instanceof NBTPathProvidableNode) init.init(base, (NBTPathProvidableNode) next);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(final NBTPathNode node:nodes){
            if(first) first = false;
            else builder.append('.');
            builder.append(node.toString());
        }
        return builder.toString();
    }
}
