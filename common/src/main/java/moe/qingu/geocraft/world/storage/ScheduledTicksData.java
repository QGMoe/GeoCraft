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

package moe.qingu.geocraft.world.storage;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.util.misc.ExtendedNextTickListEntry;
import moe.qingu.geocraft.world.BlockUpdater;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public class ScheduledTicksData implements INBTSerializable<NBTTagCompound> {
    public static final ResourceLocation ID = new ResourceLocation(GeoCraft.MODID,"scheduled_ticks_data");
    protected Set<ExtendedNextTickListEntry> entrySet = new HashSet<>();

    protected Chunk chunk;

    public ScheduledTicksData setChunk(@Nonnull Chunk chunk) {
        this.chunk = chunk;
        return this;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Set<ExtendedNextTickListEntry> getEntrySet() {
        return entrySet;
    }

    @Override
    @Nonnull
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        final BlockUpdater updater = BlockUpdater.getBlockUpdater(chunk.getWorld());
        if(updater == null) return compound;

        final NBTTagList updateLists = new NBTTagList();
        final long totalTime = chunk.getWorld().getTotalWorldTime();
        compound.setLong("totalWorldTime",totalTime);

        entrySet = updater.queryEntries(chunk,false);

        for(@Nonnull ExtendedNextTickListEntry entry:entrySet){
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("id",Block.REGISTRY.getIDForObject(entry.getBlock()));
            c.setIntArray("pos",new int[]{entry.position.getX(),entry.position.getY(),entry.position.getZ()});
            c.setInteger("time",(int)(entry.scheduledTime-totalTime));
            updateLists.appendTag(c);
        }
        compound.setTag("block_updating_entries",updateLists);
        compound.setInteger("version",1);
        return compound;
    }

    @Override
    public void deserializeNBT(final @Nonnull NBTTagCompound nbt) {
        entrySet.clear();
        final NBTTagList updateLists = nbt.getTagList("block_updating_entries", Constants.NBT.TAG_COMPOUND);
        final long savedTime = nbt.getLong("totalWorldTime");
        for(@Nonnull NBTBase base:updateLists){
            NBTTagCompound compound = (NBTTagCompound) base;
            Block block = Block.REGISTRY.getObjectById(compound.getInteger("id"));
            if(block == Blocks.AIR) continue;
            final int[] posArray = compound.getIntArray("pos");
            BlockPos pos = new BlockPos(posArray[0],posArray[1],posArray[2]);
            final int time = compound.getInteger("time");
            ExtendedNextTickListEntry entry = new ExtendedNextTickListEntry(pos,block,savedTime+time);
            entrySet.add(entry);
        }
        BlockUpdater.scheduleUpdates(chunk.getWorld(),entrySet);
    }
}
