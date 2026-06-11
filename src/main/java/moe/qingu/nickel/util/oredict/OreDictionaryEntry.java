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

package moe.qingu.nickel.util.oredict;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author QiguaiAAAA
 */
public final class OreDictionaryEntry implements Iterable<ItemStack>{
    private static final Map<String,OreDictionaryEntry> EntryMap = new ConcurrentHashMap<>();
    private final @Nonnull String name;
    private final @Nonnull NonNullList<ItemStack> content;
    private final int id;

    OreDictionaryEntry(final @Nonnull String name){
        this.name = name;
        this.id = OreDictionary.getOreID(name); // Also register it if non existed
        this.content = OreDictionary.getOres(name);
    }

    public static OreDictionaryEntry get(@Nonnull final String name){
        if(!OreDictionary.doesOreNameExist(name)){
            EntryMap.remove(name); //Forge may rebuild ore dictionary
            return null;
        }
        final OreDictionaryEntry entry = EntryMap.get(name);
        if(entry == null || entry.isInvalid()){
            final OreDictionaryEntry newEntry = new OreDictionaryEntry(name);
            EntryMap.put(name,newEntry);
            return newEntry;
        }else return entry;
    }

    @Nonnull
    public NonNullList<ItemStack> getContent() {
        return content;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /**
     * @see OreDictionary#registerOre(String, Item)
     */
    public void register(@Nonnull final Item item){
        OreDictionary.registerOre(this.name,item);
    }

    /**
     * @see OreDictionary#registerOre(String, Block)
     */
    public void register(@Nonnull final Block block){
        OreDictionary.registerOre(this.name,block);
    }

    /**
     * @see OreDictionary#registerOre(String, ItemStack)
     */
    public void register(@Nonnull final ItemStack stack){
        OreDictionary.registerOre(this.name,stack);
    }

    public boolean isInvalid(){
        if(!OreDictionary.doesOreNameExist(name)) return false;
        final NonNullList<ItemStack> ores = OreDictionary.getOres(name);
        return ores == this.content;
    }

    @Nonnull
    @Override
    public Iterator<ItemStack> iterator() {
        return content.iterator();
    }
}
