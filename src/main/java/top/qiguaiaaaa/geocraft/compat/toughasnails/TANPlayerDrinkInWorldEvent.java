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

package top.qiguaiaaaa.geocraft.compat.toughasnails;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import toughasnails.api.thirst.WaterType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
@Cancelable
public class TANPlayerDrinkInWorldEvent extends PlayerEvent {
    private final @Nonnull WaterType type;
    private final @Nullable BlockPos pos;

    public TANPlayerDrinkInWorldEvent(final @Nonnull EntityPlayer player,
                                      final @Nonnull WaterType type,
                                      final @Nullable BlockPos pos) {
        super(player);
        this.type = type;
        this.pos = pos;
    }

    @Nonnull
    public WaterType getWaterType() {
        return type;
    }

    @Nullable
    public BlockPos getDrinkBlockPos() {
        return pos;
    }

    @HasResult
    public static class Server extends TANPlayerDrinkInWorldEvent {

        private int thirst;
        private float hydration;
        private float poisonChance;

        public Server(@Nonnull final EntityPlayerMP player,
                      @Nonnull final WaterType type,
                      @Nullable final BlockPos pos) {
            super(player, type, pos);
            this.thirst = type.getThirst();
            this.hydration = type.getHydration();
            this.poisonChance = type.getPoisonChance();
        }

        public void setHydration(final float hydration) {
            this.hydration = hydration;
        }

        public void setPoisonChance(final float poisonChance) {
            this.poisonChance = poisonChance;
        }

        public void setThirst(final int thirst) {
            this.thirst = thirst;
        }

        public int getThirst() {
            return thirst;
        }

        public float getHydration() {
            return hydration;
        }

        public float getPoisonChance() {
            return poisonChance;
        }
    }
}
