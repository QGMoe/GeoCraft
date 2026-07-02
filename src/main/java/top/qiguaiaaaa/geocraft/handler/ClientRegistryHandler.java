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

package top.qiguaiaaaa.geocraft.handler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import top.qiguaiaaaa.geocraft.GeoCraft;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
@SuppressWarnings("unused")
public final class ClientRegistryHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerItemModel(final @Nonnull ModelRegistryEvent event){
        // 不知道为什么模组注册额外数据值会影响原版的注册，所以这里先手动注册原版的
        registerVanillaModel(Blocks.CLAY,0,"clay");
        registerVanillaModel(Blocks.GRASS,0,"grass");
        registerVanillaModel(Blocks.GRASS_PATH,0,"grass_path");
        registerVanillaModel(Blocks.GRAVEL,0,"gravel");
        for(final BlockDirt.DirtType type:BlockDirt.DirtType.values())
            registerVanillaModel(Blocks.DIRT,type.getMetadata(),type.getName());
        for(final BlockSand.EnumType type:BlockSand.EnumType.values())
            registerVanillaModel(Blocks.SAND,type.getMetadata(),type.getName());

        for(int i=1;i<=4;i++){
            registerModel(Blocks.CLAY,i,"clay_h"+i);
            registerModel(Blocks.GRASS,i,"grass_h"+i);
            registerModel(Blocks.GRASS_PATH,i,"grass_path_h"+i);
            registerModel(Blocks.GRAVEL,i,"gravel_h"+i);
            for(final BlockDirt.DirtType type : BlockDirt.DirtType.values())
                registerModel(Blocks.DIRT,type.getMetadata()+i*3,type.getName()+"_h"+i);
            for (final BlockSand.EnumType type: BlockSand.EnumType.values())
                registerModel(Blocks.SAND,type.getMetadata()+(i<<1),type.getName()+"_h"+i);
        }
    }

    private static void registerModel(final @Nonnull Block block, final int meta,final @Nonnull String name){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(GeoCraft.MODID+":"+name,"inventory"));
    }

    private static void registerVanillaModel(final @Nonnull Block block, final int meta,final @Nonnull String name){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(name,"inventory"));
    }
}
