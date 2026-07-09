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

package moe.qingu.nickel.nbt.path.node;

import moe.qingu.nickel.I18nKeys;
import moe.qingu.nickel.command.exception.NickelRuntimeException;
import moe.qingu.nickel.nbt.path.method.NBTPathArgsProcessor;
import moe.qingu.nickel.nbt.path.method.NBTPathMethod;
import moe.qingu.nickel.nbt.path.method.NBTPathMethods;
import moe.qingu.nickel.reader.InputReader;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author QGMoe
 */
public class NBTPathMethodNode implements NBTPathNode{

    private final Function<NBTBase,Collection<NBTBase>> handle;
    private final NBTBase[] args;
    private final Object obj;

    public NBTPathMethodNode(final int begin,
                             final @Nonnull InputReader input,
                             final @Nonnull String name,
                             final @Nonnull NBTBase[] args)
            throws NoSuchMethodException, CommandException {
        this.args = args;
        final NBTPathMethod method = NBTPathMethods.resolveMethod(name,args);
        if(method != null){
            handle = tag ->{
                final NBTBase[] actualArgs = new NBTBase[args.length+1];
                actualArgs[0] = tag;
                System.arraycopy(args, 0, actualArgs, 1, args.length);
                return method.invoke(actualArgs);
            };
            this.obj = method;
            return;
        }
        final NBTPathArgsProcessor processor = NBTPathMethods.resolveProcessor(name,args);
        if(processor != null){
            try {
                handle = processor.process(args);
            } catch (final NickelRuntimeException e){
                input.panic(begin,I18nKeys.NBTPath.methodProcessFailed(processor).hoverTo(HoverEvent.Action.SHOW_TEXT).content(e.getInformation()));
                throw new RuntimeException("Impossible",e);
            }catch (final Exception e){
                input.panic(begin,I18nKeys.NBTPath.methodProcessFailed(processor).hoverTo(HoverEvent.Action.SHOW_TEXT).content(e.getLocalizedMessage()));
                throw new RuntimeException("Impossible",e);
            }
            this.obj = processor;
            return;
        }
        throw new NoSuchMethodException();
    }

    @Nonnull
    @Override
    public Collection<NBTBase> resolve(@Nonnull final NBTBase base) {
        return handle.apply(base);
    }

    @Nonnull
    @Override
    public String getLocalName() {
        return I18nKeys.NBTPath.NODE_METHOD;
    }

    @Override
    public int hashCode() {
        return obj.hashCode() ^ Arrays.hashCode(args);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NBTPathMethodNode && ((NBTPathMethodNode) obj).obj == this.obj && Arrays.equals(this.args, ((NBTPathMethodNode) obj).args);
    }

    @Nonnull
    @Override
    public String toString() {
        return NBTPathMethods.signatureOf(obj);
    }
}
