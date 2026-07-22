/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.geocraft.api.fluidphysics.task;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import moe.qingu.geocraft.api.event.EventFactory;
import moe.qingu.geocraft.api.event.fluidphysics.FluidTaskRegistryEvent;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author QGMoe
 */
public final class FluidTaskRegistry {
    private static final HashMap<ResourceLocation, IFluidTask> ID2Tasks = new HashMap<>();
    private static final Map<ResourceLocation,IFluidTask> UNMODIFIABLE_ID2Tasks = Collections.unmodifiableMap(ID2Tasks);
    private static final IdentityHashMap<IFluidTask,ResourceLocation> Tasks2ID = new IdentityHashMap<>();
    private static final Int2ObjectOpenHashMap<IFluidTask> TaskLookup = new Int2ObjectOpenHashMap<>();
    private static IFluidTask[] ArrTaskLookup = null;
    private static final Object2IntMap<IFluidTask> IDLookup = new Object2IntOpenHashMap<>();
    private static boolean frozen = false;

    static {
        register(EmptyFluidTask.ID,EmptyFluidTask.INSTANCE);
        IDLookup.defaultReturnValue(-1);
        TaskLookup.defaultReturnValue(EmptyFluidTask.INSTANCE);
    }

    private FluidTaskRegistry(){}

    public static void freeze() {
        if(!frozen) EventFactory.EVENT_BUS.post(new FluidTaskRegistryEvent.Register());
        final boolean hasFrozen = frozen;
        frozen = true;
        if(!hasFrozen) EventFactory.EVENT_BUS.post(new FluidTaskRegistryEvent.Freeze());
    }

    public static void register(final @Nonnull ResourceLocation location, final @Nonnull IFluidTask task){
        if(frozen) throw new IllegalStateException("Fluid Task Registry has been frozen!");
        ID2Tasks.put(location, Objects.requireNonNull(task));
        Tasks2ID.put(task,location);
    }

    public static void reloadMapping(final @Nonnull Int2ObjectMap<ResourceLocation> mapping){
        ArrTaskLookup = null;
        TaskLookup.clear();
        IDLookup.clear();
        final HashSet<IFluidTask> lefts = new HashSet<>(Tasks2ID.keySet());
        int maxID = 0;
        for(final @Nonnull Int2ObjectMap.Entry<ResourceLocation> entry:mapping.int2ObjectEntrySet()){
            final IFluidTask task = ID2Tasks.get(entry.getValue());
            if(task == null) continue; //missingMapping
            lefts.remove(task);
            TaskLookup.put(entry.getIntKey(),task);
            IDLookup.put(task,entry.getIntKey());
            maxID = Math.max(maxID,entry.getIntKey());
        }
        int i = 0;
        for (final @Nonnull IFluidTask task : lefts) {
            while (TaskLookup.containsKey(i)) i++;
            TaskLookup.put(i, task);
            IDLookup.put(task, i);
        }
        maxID = Math.max(maxID,i);
        if(maxID<16384){
            ArrTaskLookup = new IFluidTask[maxID+1];
            for(final @Nonnull Int2ObjectMap.Entry<IFluidTask> entry:TaskLookup.int2ObjectEntrySet()) ArrTaskLookup[entry.getIntKey()] = entry.getValue();
            TaskLookup.clear();
        }
    }

    @Nonnull
    public static Int2ObjectMap<ResourceLocation> getMapping(){
        final Int2ObjectOpenHashMap<ResourceLocation> mapping = new Int2ObjectOpenHashMap<>();
        for(final @Nonnull Object2IntMap.Entry<IFluidTask> entry: IDLookup.object2IntEntrySet()) mapping.put(entry.getIntValue(),Tasks2ID.get(entry.getKey()));
        return mapping;
    }

    public static int getID(final @Nonnull IFluidTask task){
        return IDLookup.get(task);
    }

    public static ResourceLocation getName(final @Nonnull IFluidTask task){
        return Tasks2ID.get(task);
    }

    @Nonnull
    public static IFluidTask getTaskByID(final int id){
        if(ArrTaskLookup == null) return TaskLookup.get(id);
        else if(id >= ArrTaskLookup.length || id < 0) return EmptyFluidTask.INSTANCE;
        else{
            final IFluidTask t = ArrTaskLookup[id];
            return t == null?EmptyFluidTask.INSTANCE:t;
        }
    }

    @Nullable
    public static IFluidTask getTaskByName(final @Nonnull ResourceLocation location){
        return ID2Tasks.get(location);
    }

    @Nonnull
    public static Map<ResourceLocation,IFluidTask> getTasks(){
        return UNMODIFIABLE_ID2Tasks;
    }
}
