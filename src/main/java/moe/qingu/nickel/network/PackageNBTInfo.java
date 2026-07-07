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

package moe.qingu.nickel.network;

import io.netty.buffer.ByteBuf;
import moe.qingu.nickel.nbt.NBTUtils;
import moe.qingu.nickel.nbt.SNBTFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;
import java.util.concurrent.ForkJoinPool;

import static moe.qingu.nickel.text.Texts.plain;

/**
 * @author QGMoe
 */
public final class PackageNBTInfo implements IMessage {
    private NBTBase nbt;

    public PackageNBTInfo(){}

    public PackageNBTInfo(final @Nonnull NBTBase nbt){
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(final @Nonnull ByteBuf buf) {
        if(buf.readBoolean()){
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            if(compound == null) return;
            nbt = compound.getTag("d");
        }
    }

    @Override
    public void toBytes(final @Nonnull ByteBuf buf) {
        buf.writeBoolean(nbt != null);
        if(nbt == null) return;
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("d",nbt);
        ByteBufUtils.writeTag(buf,compound);
    }

    public static final class Handler implements IMessageHandler<PackageNBTInfo,IMessage> {

        @Override
        public IMessage onMessage(final @Nonnull PackageNBTInfo info,final @Nonnull MessageContext ctx) {
            if(info.nbt != null){
                ForkJoinPool.commonPool().execute(()->{
                    final ITextComponent component;
                    if(NBTUtils.sizeOf(info.nbt) > 200) component = plain(info.nbt.toString()).done();
                    else component = SNBTFormatter.format(info.nbt).done();
                    Minecraft.getMinecraft().addScheduledTask(()-> Minecraft.getMinecraft().ingameGUI.getChatGUI()
                            .printChatMessage(component));
                });
            }
            return null;
        }
    }
}
