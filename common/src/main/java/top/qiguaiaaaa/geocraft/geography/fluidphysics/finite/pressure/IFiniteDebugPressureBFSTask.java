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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.pressure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import top.qiguaiaaaa.geocraft.GeoCraft;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public interface IFiniteDebugPressureBFSTask extends IFinitePressureBFSTask {

    default void isEqualState_Debug(@Nonnull IBlockState curState) {
        GeoCraft.getLogger().info("{} stopped because source state changed to {}",this,curState);
    }

    default void cancel_Debug() {
        GeoCraft.getLogger().info("{} is cancelled",this);
    }

    default void putBlockPosToResults_Debug(@Nonnull BlockPos pos) {
        GeoCraft.getLogger().info("{} found a possible result {}",this,pos);
    }

    default void queued_Debug(@Nonnull BlockPos pos) {
        GeoCraft.getLogger().info("{} queued pos {}",this,pos);
    }

    default void markVisited_Debug(@Nonnull BlockPos pos) {
        GeoCraft.getLogger().info("{} mark {} as visited",this,pos);
    }

    default void finish_Debug() {
        GeoCraft.getLogger().info("{} finished:",this);
        for(BlockPos pos:getResultCollection()){
            GeoCraft.getLogger().info("{}",pos);
        }
    }

    default void canSearchInto_Debug(@Nonnull WorldServer world,@Nonnull BlockPos pos,@Nonnull int[] dir) {
        GeoCraft.getLogger().info("{} checked {} via dir ({},{},{}) and is sure it can be searched into.",
                this,pos,dir[0],dir[1],dir[2]);
    }

    default void search_Debug(@Nonnull WorldServer world) {
        GeoCraft.getLogger().info("{} is being running",this);
    }

    @Override
    default boolean isFinished() {
        if(IFinitePressureBFSTask.super.isFinished()){
            GeoCraft.getLogger().info("{} is finished with visited poses {} , queued poses {}, res poses {}, search times {}, max search times {}",
                    this,getVisitedSize(),getQueueSize(),getResultCollection().size(),getSearchTimes(),getMaxSearchTimes());
            return true;
        }
        return false;
    }

    interface IFiniteVanillaDebugPressureBFSTask extends IFiniteVanillaPressureBFSTask, IFiniteDebugPressureBFSTask {
        @Override
        default boolean isEqualState(@Nonnull IBlockState curState) {
            if(IFiniteVanillaPressureBFSTask.super.isEqualState(curState)){
                return true;
            }
            isEqualState_Debug(curState);
            return false;
        }

        @Override
        default boolean canSearchInto(@Nonnull WorldServer world, @Nonnull BlockPos pos, int[] dir) {
            if(IFiniteVanillaPressureBFSTask.super.canSearchInto(world, pos, dir)){
                canSearchInto_Debug(world,pos,dir);
                return true;
            }
            return false;
        }
    }

    interface IFiniteClassicDebugPressureBFSTask extends IFiniteClassicPressureBFSTask, IFiniteDebugPressureBFSTask {
        @Override
        default boolean isEqualState(@Nonnull IBlockState curState) {
            if(IFiniteClassicPressureBFSTask.super.isEqualState(curState)){
                return true;
            }
            isEqualState_Debug(curState);
            return false;
        }

        @Override
        default boolean canSearchInto(@Nonnull WorldServer world, @Nonnull BlockPos pos, int[] dir) {
            if(IFiniteClassicPressureBFSTask.super.canSearchInto(world, pos, dir)){
                canSearchInto_Debug(world,pos,dir);
                return true;
            }
            return false;
        }
    }
}
