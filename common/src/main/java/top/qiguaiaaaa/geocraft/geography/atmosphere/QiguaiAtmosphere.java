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

package top.qiguaiaaaa.geocraft.geography.atmosphere;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.BaseAtmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.tracker.IAtmosphereTracker;
import top.qiguaiaaaa.geocraft.api.state.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class QiguaiAtmosphere extends BaseAtmosphere {
    protected boolean debug = false;
    protected int x,z;

    public abstract void updateTick(@Nullable Chunk chunk);

    protected void updateListeners(){
        for(IAtmosphereTracker listener: trackers) listener.postAtmosphereTick(this);
    }

    public void setLocation(int x,int z){
        this.x = x;
        this.z = z;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public int getChunkX() {
        return x;
    }

    @Override
    public int getChunkZ() {
        return z;
    }

    @Override
    public int addSteam(int amount, @Nonnull BlockPos pos,final boolean doAdd){
        if(amount == 0) return 0;
        return getAtmosphereLayer(pos).addSteam(pos,amount,doAdd);
    }

    @Override
    public int addWater(int amount, @Nonnull BlockPos pos,final boolean doAdd) {
        if(amount == 0) return 0;
        return getAtmosphereLayer(pos).addWater(pos,amount,doAdd);
    }

    @Override
    public int drainWater(int amount, @Nonnull BlockPos pos, boolean doDrain) {
        if(amount<0) return 0;
        int realAmount = 0;
        for(Layer layer = getAtmosphereLayer(pos); layer != null; layer=layer.getUpperLayer()){
            if(!(layer instanceof AtmosphereLayer)) continue;
            FluidState state = layer.getWater();
            if(state == null) continue;
            int realAmountLayer = Math.min(amount,state.getFluidAmount());
            amount -= realAmountLayer;
            realAmount += realAmountLayer;
            if(doDrain) state.fill(-realAmountLayer, true);
            if(amount <=0) break;
        }
        return realAmount;
    }

    @Override
    public void putHeat(double Q, BlockPos pos){
        getAtmosphereLayer(pos).putHeat(Q,pos);
    }

    @Override
    public Layer getLayer(@Nonnull BlockPos pos) {
        Layer res = null;
        for (Layer layer : layers) {
            if (pos.getY() < layer.getBeginY()) break;
            res = layer;
        }
        return res;
    }

    public AtmosphereLayer getAtmosphereLayer(BlockPos pos){
        Layer res = getLayer(pos);
        while (!(res instanceof AtmosphereLayer)){
            if(res == null) return getBottomAtmosphereLayer(pos);
            res = res.getUpperLayer();
        }
        return (AtmosphereLayer) res;
    }

    //************
    // Override
    //************

    @Nonnull
    @Override
    public Vec3d getWind(@Nonnull BlockPos pos){
        AtmosphereLayer layer = getAtmosphereLayer(pos);
        if(layer == null) return Vec3d.ZERO;
        return layer.getWind(pos);
    }

    @Override
    public double getPressure(@Nonnull BlockPos pos) {
        AtmosphereLayer layer = getAtmosphereLayer(pos);
        if(layer == null) return 0;
        return layer.getPressure(pos);
    }

    @Override
    public double getWaterPressure(@Nonnull BlockPos pos) {
        AtmosphereLayer layer = getAtmosphereLayer(pos);
        if(layer == null) return 0;
        return layer.getWaterPressure(pos);
    }
}
