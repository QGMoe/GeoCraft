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

package top.qiguaiaaaa.geocraft.api.event.player;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 当玩家装瓶的时候触发<br/>
 * 通过Mixin实现
 * @author QiguaiAAAA
 */
@Cancelable
@Event.HasResult
public class FillGlassBottleEvent extends PlayerEvent {
    private final ItemStack current;
    private final World world;
    private ItemStack result;

    public FillGlassBottleEvent(@Nonnull EntityPlayer player, @Nonnull ItemStack current,@Nonnull World world) {
        super(player);
        this.current = current;
        this.world = world;
    }

    /**
     * 获取装之前的瓶子
     * @return 装之前的瓶子
     */
    @Nonnull
    public ItemStack getEmptyGlassBottle(){
        return current;
    }
    @Nonnull
    public World getWorld(){
        return world;
    }

    /**
     * 获取设置的填充满后的瓶子<br/>
     * 当Result被设置为{@link Result#ALLOW}时，该方法不应返回null，否则会抛出{@link NullPointerException}
     * @return 设置的填满后的瓶子
     */
    @Nullable
    public ItemStack getFilledGlassBottle(){
        return result;
    }

    /**
     * 设置填充满后的瓶子
     * @param glassBottle 填满后的瓶子
     */
    public void setFilledGlassBottle(@Nonnull ItemStack glassBottle){
        this.result = glassBottle;
    }

    /**
     * 当玩家在效果云装瓶的时候发布
     */
    @Cancelable
    @HasResult
    public static class FillGlassBottleOnAreaEffectCloudEvent extends FillGlassBottleEvent{
        private final List<EntityAreaEffectCloud> entityAreaEffectClouds;

        public FillGlassBottleOnAreaEffectCloudEvent(@Nonnull EntityPlayer player, @Nonnull ItemStack current,@Nonnull World world, @Nonnull List<EntityAreaEffectCloud> entityList) {
            super(player, current, world);
            entityAreaEffectClouds = entityList;
        }
        @Nonnull
        public List<EntityAreaEffectCloud> getEntityListClicked(){
            return entityAreaEffectClouds;
        }
    }

    /**
     * 当玩家在装流体的时候发布
     */
    @Cancelable
    @HasResult
    public static class FillGlassBottleOnFluidEvent extends FillGlassBottleEvent{
        @Nullable
        private final RayTraceResult rayTraceResult;

        public FillGlassBottleOnFluidEvent(@Nonnull EntityPlayer player, @Nonnull ItemStack current, World world, @Nullable RayTraceResult rayTraceResult) {
            super(player, current, world);
            this.rayTraceResult = rayTraceResult;
        }
        @Nullable
        public RayTraceResult getRayTraceResult(){
            return rayTraceResult;
        }
    }
}
