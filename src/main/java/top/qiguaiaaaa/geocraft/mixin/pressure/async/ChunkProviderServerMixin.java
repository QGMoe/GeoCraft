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

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.FluidPressureSearchManager;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * 当压强异步运行的时候，使得在区块卸载时压强系统停止运行，以避免可能的崩溃问题，因为{@link MinecraftServer}是线程不安全的
 * @author QiguaiAAAA
 */
@Mixin(value = ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin implements IChunkProvider {
    @Shadow @Final public Long2ObjectMap<Chunk> loadedChunks;

    @Shadow protected abstract void saveChunkData(Chunk chunkIn);

    @Shadow protected abstract void saveChunkExtraData(Chunk chunkIn);

    @Shadow @Final public WorldServer world;

    @Shadow @Final public IChunkLoader chunkLoader;

    @Inject(method = "tick",
            at =@At(value = "INVOKE_ASSIGN",target = "Ljava/util/Set;iterator()Ljava/util/Iterator;",ordinal = 0),
    locals = LocalCapture.CAPTURE_FAILEXCEPTION,cancellable = true)
    private void 天圆地方$tickLock(@Nonnull final CallbackInfoReturnable<Boolean> ci, @Nonnull @Local final Iterator<Long> iterator){
        if(!iterator.hasNext()) return; //不会卸载区块
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
                ci.setReturnValue(false);
                return;
            }

            for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
                final Long chunkId = iterator.next();
                final Chunk chunk = loadedChunks.get(chunkId);

                if (chunk != null && chunk.unloadQueued) {
                    chunk.onUnload();
                    ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.x, chunk.z), chunk);
                    this.saveChunkData(chunk);
                    this.saveChunkExtraData(chunk);
                    this.loadedChunks.remove(chunkId);
                    i++;
                }
            }
        }finally {
            if(locked) FluidPressureSearchManager.releaseWorldReadLock();
        }

        if(!iterator.hasNext()) { //之后的卸载区块不会执行了，先返回吧
            return;
        }
        ci.setReturnValue(false);

        if (this.loadedChunks.isEmpty()) DimensionManager.unloadWorld(this.world.provider.getDimension());

        this.chunkLoader.chunkTick();
    }
}
