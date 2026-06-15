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

package 清汩萌.天圆地方.world.sandbox;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
public class MockHashSandbox extends MockSandboxAbstract{
    protected final boolean isTransparentByDefault;
    protected final IBlockState defaultState;
    protected @Nonnull Long2ObjectMap<Int2ObjectMap<IBlockState>> states = new Long2ObjectOpenHashMap<>();
    protected @Nonnull Long2IntMap highestGroundPoses = new Long2IntOpenHashMap();

    public MockHashSandbox(@Nonnull final IBlockState defaultState) {
        this.defaultState = defaultState;
        isTransparentByDefault = !defaultState.getMaterial().isOpaque();
        states.defaultReturnValue(null);
        highestGroundPoses.defaultReturnValue(Integer.MIN_VALUE);
    }

    @Override
    public boolean isOutOfRange(@Nonnull final BlockPos pos) {
        return false;
    }

    @Override
    public boolean canSeeSky(@Nonnull final BlockPos pos) {
        if(isTransparentByDefault) return pos.getY() > highestGroundPoses.get((long) pos.getX() <<Integer.SIZE | pos.getZ());
        return false;
    }

    @Override
    public IBlockState setBlockState(@Nonnull final BlockPos pos, @Nonnull final IBlockState state) {
        final long posL = (long) pos.getX() << Integer.SIZE | pos.getZ();
        final @Nullable Int2ObjectMap<IBlockState> verticalStates = states.get(posL);
        final @Nonnull IBlockState old;
        if(state == defaultState){
            if(verticalStates == null) return defaultState;
            else old = verticalStates.remove(pos.getY());
        }else {
            if(verticalStates == null){
                final Int2ObjectMap<IBlockState> newVerticalStates = new Int2ObjectOpenHashMap<>();
                newVerticalStates.defaultReturnValue(defaultState);
                newVerticalStates.put(pos.getY(),state);
                states.put(posL,newVerticalStates);
                updateHeightMap(posL,pos.getY(),newVerticalStates,defaultState,state);
                return defaultState;
            }else{
                old = verticalStates.put(pos.getY(),state);
            }
        }
        if(verticalStates.isEmpty()) states.remove(posL);
        updateHeightMap(posL,pos.getY(),verticalStates,old,state);
        return old;
    }

    @Override
    @Nonnull
    public IBlockState getBlockState(@Nonnull final BlockPos pos) {
        final long posL = (long) pos.getX() << Integer.SIZE | pos.getZ();
        final @Nullable Int2ObjectMap<IBlockState> verticalStates = states.get(posL);
        return verticalStates == null? defaultState : verticalStates.get(pos.getY());
    }

    protected void updateHeightMap(final long posL,
                                   final int y,
                                   final @Nonnull Int2ObjectMap<IBlockState> verticalStates,
                                   final @Nonnull IBlockState oldState,
                                   final @Nonnull IBlockState newState){
        if(!isTransparentByDefault) return;
        final boolean transparentOld = !oldState.getMaterial().isOpaque();
        final boolean transparentNew = !newState.getMaterial().isOpaque();
        if(transparentOld == transparentNew) return;
        else if(transparentOld){
            if(y > highestGroundPoses.get(posL)){
                highestGroundPoses.put(posL,y);
            }
            return;
        }else if(y == highestGroundPoses.get(posL)){
            int max = Integer.MIN_VALUE;
            for(final int posY: verticalStates.keySet()){
                max = Math.max(max,posY);
            }
            highestGroundPoses.put(posL,max);
            return;
        }
        Assertions.assertTrue(y < highestGroundPoses.get(posL));
    }
}
