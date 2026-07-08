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

package top.qiguaiaaaa.geocraft.world.storage;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import top.qiguaiaaaa.geocraft.util.misc.ExtendedNextTickListEntry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author QiguaiAAAA
 */
@Deprecated
public class GeoCraftWorldSavedData extends WorldSavedData {
    public static final String DATA_NAME = "GeoCraftData";

    protected Set<ExtendedNextTickListEntry> entrySet = new HashSet<>();
    protected World world;

    public GeoCraftWorldSavedData(){
        super(DATA_NAME);
    }

    public GeoCraftWorldSavedData(String name) {
        super(name);
    }

    public Set<ExtendedNextTickListEntry> getEntrySet() {
        return entrySet;
    }

    public void setWorld(@Nonnull World world) {
        this.world = world;
    }

    public void setEntrySet(@Nonnull Set<ExtendedNextTickListEntry> entrySet) {
        this.entrySet = entrySet;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
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
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        final NBTTagList updateLists = new NBTTagList();
        final long totalTime = world.getTotalWorldTime();
        compound.setLong("totalWorldTime",totalTime);
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
    public boolean isDirty() {
        return true;
    }
}
