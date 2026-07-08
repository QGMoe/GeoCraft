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

package top.qiguaiaaaa.geocraft.mixin.pressure.async;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * 当压强异步运行的时候，使得在区块卸载时压强系统停止运行，以避免可能的崩溃问题，因为{@link MinecraftServer}是线程不安全的.
 * 该类基于拓展的 Mixin 功能实现
 * @see ChunkProviderServerMixin
 * @author QiguaiAAAA
 */
@Mixin(value = ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin8 {

    @Shadow @Final public WorldServer world;

    @Shadow @Final private Set<Long> droppedChunks;

    @Shadow @Final public Long2ObjectMap<Chunk> loadedChunks;

    @Shadow @Final public IChunkLoader chunkLoader;

    @SuppressWarnings("unused")
    @WrapMethod(method = "tick")
    private boolean 天圆地方$tick(@Nonnull final Operation<Boolean> original){

        if(this.world.disableLevelSaving) return false;

        if(this.droppedChunks.isEmpty()){
            if (this.loadedChunks.isEmpty()) net.minecraftforge.common.DimensionManager.unloadWorld(this.world.provider.getDimension());
            this.chunkLoader.chunkTick();
            return false;
        }

        boolean locked = false;
        try {
            try {
                final long begin = System.nanoTime();

                locked = FluidPressureSearchManager.tryLockWorldRead(FluidPhysicsConfig.PAUSE_TIME_FOR_PRESSURE_PRE_CHUNK_SAVING.getValue());
                if(locked){
                    GeoCraft.getLogger().debug("Server successfully locked World Read Lock.");
                }else {
                    GeoCraft.getLogger().warn("Is there any wrong with Pressure System? Server thread wait time exceeded {} ms.",FluidPhysicsConfig.PAUSE_TIME_FOR_PRESSURE_PRE_CHUNK_SAVING.getValue());
                }

                final long end = System.nanoTime();
                GeoCraft.getLogger().debug("Server Thread spent {} ms in trying acquiring lock before chunk saving.",(end-begin)/1000000d);
            }catch (InterruptedException ignored){
            }

            if(!locked && FluidPhysicsConfig.DO_NOT_DROP_CHUNKS_WHEN_FAILING_PAUSING_PRESSURE_SYSTEM.getValue()){
                GeoCraft.getLogger().warn("Failed to pause PressureSystem, dropping chunks is cancelled for stability.");
                return false;
            }

            return original.call();
        }finally {
            if(locked) FluidPressureSearchManager.releaseWorldReadLock();
        }
    }
}
