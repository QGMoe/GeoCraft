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

package top.qiguaiaaaa.geocraft.api.event.atmosphere;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气生成事件，当大气生成的时候会调用
 */
public class AtmosphereGenerateEvent extends AtmosphereEvent{
    private final WorldServer world;
    private final Chunk chunk;
    public AtmosphereGenerateEvent(@Nonnull WorldServer world,@Nonnull Chunk chunk,@Nullable Atmosphere atmosphere) {
        super(atmosphere);
        this.world = world;
        this.chunk= chunk;
    }
    @Nonnull
    public WorldServer getWorld() {
        return world;
    }
    @Nonnull
    public Chunk getChunk() {
        return chunk;
    }

    @Nullable
    @Override
    public Atmosphere getAtmosphere() {
        return super.getAtmosphere();
    }

    /**
     * 在大气生成之前，这时候{@link #getAtmosphere()}是null
     */
    public static class Pre extends AtmosphereGenerateEvent{

        public Pre(@Nonnull WorldServer world,@Nonnull Chunk chunk) {
            super(world, chunk, null);
        }

    }
}
