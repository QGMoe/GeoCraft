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

package top.qiguaiaaaa.geocraft.api.atmosphere.accessor;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.atmosphere.raypack.HeatPack;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.event.atmosphere.AtmosphereAccessEvent;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 一个简单的平滑大气属性的{@link IAtmosphereAccessor}实现，大部分数据均会通过多点采样+平均进行平滑过渡，防止在区块交界处出现属性值突变
 * 获取数据（例如温度、气压）、放置热量时，会将请求平均分配到当前方块对应大气以及八个角落（差1个方块）的大气，以实现平滑过渡。
 */
public class AverageAtmosphereAccessor extends AbstractAtmosphereAccessor{
    protected static final int[] CURRENT = {0,0};
    protected static final int[][] DIRS8 = {
            {2,0},{-2, 0},{0, 2},{ 0,-2},
            {2,2},{-2,-2},{2,-2},{-2, 2}};

    protected Map<int[],AtmosphereData> datas = new HashMap<>();

    protected BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

    public AverageAtmosphereAccessor(@Nonnull IAtmosphereSystem system,@Nonnull AtmosphereData data,@Nonnull BlockPos pos, boolean notAir) {
        super(system, data, pos, notAir);
        loadDatas();
    }

    @Override
    public boolean canAccessAtmosphere() {
        return skyLight>0;
    }

    @Override
    public boolean refresh() {
        datas.clear();
        loadDatas();
        return true;
    }

    @Override
    public double getTemperature() {
        if(skyLight<0 || notAir || skyLight >=15){
            return getAtmosphereValue((ints, atmosphere) ->
                    (double) atmosphere.getTemperature(mutableBlockPos.setPos(pos.getX() + ints[0], pos.getY(), pos.getZ() + ints[1]), notAir));
        }
        double airTemp= getAtmosphereValue((ints, atmosphere) ->
                (double) atmosphere.getTemperature(mutableBlockPos.setPos(pos.getX() + ints[0], pos.getY(), pos.getZ() + ints[1]), false)),
                blockTemp = getAtmosphereValue((ints, atmosphere) ->
                (double) atmosphere.getTemperature(mutableBlockPos.setPos(pos.getX() + ints[0], pos.getY(), pos.getZ() + ints[1]), true));
        return (skyLight/15.0)*(airTemp-blockTemp)+blockTemp;
    }

    @Override
    public double getPressure() {
        return getAtmosphereValue((ints, atmosphere) ->
                atmosphere.getPressure(mutableBlockPos.setPos(pos.getX() + ints[0], pos.getY(), pos.getZ() + ints[1])));
    }

    @Override
    public double getWaterPressure() {
        checkAtmosphereDataLoaded();
        return getAtmosphereValue((dir, atmosphere) ->
                atmosphere.getWaterPressure(mutableBlockPos.setPos(pos.getX() + dir[0], pos.getY(), pos.getZ() + dir[1])));
    }

    @Nonnull
    @Override
    public Vec3d getWind() {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Vec3d wind = data.getAtmosphere().getWind(pos);
        if(skyLight>=0 && skyLight<15){
            wind.scale(skyLight/15d);
        }
        return wind;
    }

    @Override
    public int fillFluidToAtmosphere(@Nonnull final Fluid fluid,final int amount,final @Nonnull StateOfMatter state,final double temp,final boolean doFill) {
        if(amount <= 0) return 0;
        assert data.getAtmosphere() != null;
        int filled = EventFactory.onFillFluidToAtmosphere(data.getAtmosphere(),this,fluid,temp,amount,null,state,doFill);
        if(filled >=0) return filled;
        if(fluid == FluidRegistry.WATER){
            return fillWaterToAtmosphere(amount,state,temp,doFill);
        }
        return 0;
    }

    @Override
    public int fillFluidToAtmosphere(@Nonnull final Fluid fluid, @Nonnull final FluidStack stack, @Nonnull final StateOfMatter state,final double temp,final boolean doFill) {
        if(stack.amount <= 0) return 0;
        assert data.getAtmosphere() != null;
        int filled = EventFactory.onFillFluidToAtmosphere(data.getAtmosphere(),this,fluid,temp,stack.amount,stack,state,doFill);
        if(filled >=0) return filled;
        if(fluid == FluidRegistry.WATER){
            return fillWaterToAtmosphere(stack.amount,state,temp,doFill);
        }
        return 0;
    }

    @Override
    public int drainFluidFromAtmosphere(@Nonnull final Fluid fluid, @Nonnull final StateOfMatter state,final int maxDrainedAmount,final boolean doDrain) {
        if(maxDrainedAmount <= 0) return 0;
        assert data.getAtmosphere() != null;
        final AtmosphereAccessEvent.FluidDrain event = EventFactory.onDrainedFluidToAtmosphere(data.getAtmosphere(), this,fluid,maxDrainedAmount,false,state,doDrain);
        if(event != null && event.hasResult()){
            switch (event.getResult()){
                case DENY:return 0;
                case ALLOW:{
                    if(event.getDrainedStack() != null) return event.getDrainedStack().amount;
                    return Math.max(event.getDrainedAmount(), 0);
                }
                case DEFAULT:break;
            }
        }
        if(fluid == FluidRegistry.WATER){
            return drainWaterFromAtmosphere(maxDrainedAmount,state,doDrain);
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drainFluidStackFromAtmosphere(@Nonnull final Fluid fluid, @Nonnull final StateOfMatter state,final int maxDrainedAmount,final boolean doDrain) {
        if(maxDrainedAmount <= 0) return null;
        assert data.getAtmosphere() != null;
        final AtmosphereAccessEvent.FluidDrain event = EventFactory.onDrainedFluidToAtmosphere(data.getAtmosphere(), this,fluid,maxDrainedAmount,true,state,doDrain);
        if(event != null && event.hasResult()){
            switch (event.getResult()){
                case DENY:return new FluidStack(fluid,0);
                case ALLOW:{
                    if(event.getDrainedStack() != null) return event.getDrainedStack();
                    if(event.getDrainedAmount()<=0) return null;
                    return new FluidStack(fluid,event.getDrainedAmount());
                }
                case DEFAULT:break;
            }
        }
        if(fluid == FluidRegistry.WATER){
            return new FluidStack(FluidRegistry.WATER,drainWaterFromAtmosphere(maxDrainedAmount,state,doDrain));
        }
        return null;
    }

    protected int fillWaterToAtmosphere(final int amount,@Nonnull final StateOfMatter state,final double temp,final boolean doFill){
        assert data.getAtmosphere() != null;
        switch (state){
            case GAS: return data.getAtmosphere().addSteam(amount,pos,doFill);
            case LIQUID:return data.getAtmosphere().addWater(amount,pos,doFill);
        }
        return 0;
    }

    protected int drainWaterFromAtmosphere(final int amount,@Nonnull final StateOfMatter state,final boolean doDrain){
        assert data.getAtmosphere() != null;
        switch (state){
            case GAS: return -data.getAtmosphere().addSteam(-amount,pos,doDrain);
            case LIQUID:return data.getAtmosphere().drainWater(amount,pos,doDrain);
        }
        return 0;
    }

    @Override
    public void putHeatToAtmosphere(double amount) {
        if(amount <0) return;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final double average;

        if(skyLight>=0 && skyLight<15){
            average = amount/datas.size()*(skyLight/15d);
        }else average = amount/datas.size();

        forAtmospheresDo((dir, atmosphere) -> {
            atmosphere.putHeat(average,mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]));
            return null;
        });
    }

    @Override
    public void putHeatToUnderlying(double amount) {
        if(amount <0) return;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final double average = amount/datas.size();
        forAtmospheresDo((dir, atmosphere) -> {
            atmosphere.getUnderlying(pos).putHeat(average,mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]));
            return null;
        });
    }

    @Override
    public void putHeatToCurrentLayer(double amount) {
        if(amount <0) return;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final double average = amount/datas.size();
        forAtmospheresDo((dir, atmosphere) -> {
            Layer layer = atmosphere.getLayer(mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]));
            if(layer == null) return null;
            layer.putHeat(average,mutableBlockPos);
            return null;
        });
    }

    /**
     * {@inheritDoc}
     * @param amount {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public double drainHeatFromAtmosphere(double amount) {
        if(amount <0) return 0;
        if(skyLight == 0) return amount;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final Set<Pair<int[],Layer>> layerToDraw = new HashSet<>();
        forAtmospheresDo((dir,atmosphere) -> {
            mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]);
            Layer layer = atmosphere.getLayer(mutableBlockPos);
            if(layer == null) return null;
            if(layer instanceof AtmosphereLayer){
                layerToDraw.add(Pair.of(dir,layer));
            }else{
                if(pos.getY()>atmosphere.getTopLayer().getTopY()) return null;
                if(pos.getY()<0) return null;
                layer = atmosphere.getBottomAtmosphereLayer(pos);
                if(layer == null) return null;
                layerToDraw.add(Pair.of(dir,layer));
            }
            return null;
        });
        if(layerToDraw.isEmpty()) return 0;
        double average = amount/layerToDraw.size(),res = 0;
        double factor = 1;
        if(skyLight>0 && skyLight<15) factor = skyLight/15d;
        for(Pair<int[],Layer> pair:layerToDraw){
            mutableBlockPos.setPos(pos.getX()+pair.getKey()[0],pos.getY(),pos.getZ()+pair.getKey()[1]);
            res += pair.getValue().drainHeat(average*factor,mutableBlockPos)/factor;
        }
        return res;
    }

    @Override
    public double drainHeatFromUnderlying(double amount) {
        if(amount <0) return 0;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final double average = amount/datas.size();
        return drawAtmosphereProperty((dir,atmosphere)->{
            mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]);
            return atmosphere.getUnderlying(pos).drainHeat(average,mutableBlockPos);
        });
    }

    @Override
    public double drainHeatFromCurrentLayer(double amount) {
        if(amount <0) return 0;
        checkAtmosphereDataLoaded();
        clearInvalidData();
        final Set<Pair<int[],Layer>> layerToDraw = new HashSet<>();
        forAtmospheresDo((dir,atmosphere) -> {
            mutableBlockPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]);
            Layer layer = atmosphere.getLayer(mutableBlockPos);
            if(layer == null) return null;
            layerToDraw.add(Pair.of(dir,layer));
            return null;
        });
        if(layerToDraw.isEmpty()) return 0;
        double average = amount/layerToDraw.size(),res = 0;
        for(Pair<int[],Layer> pair:layerToDraw){
            mutableBlockPos.setPos(pos.getX()+pair.getKey()[0],pos.getY(),pos.getZ()+pair.getKey()[1]);
            res += pair.getValue().drainHeat(average,mutableBlockPos);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     * @param pack {@inheritDoc}
     * @param direction {@inheritDoc}
     */
    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable EnumFacing direction) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        if(!(layer instanceof AtmosphereLayer)) layer = data.getAtmosphere().getBottomAtmosphereLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,direction);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3d directionVec) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        if(!(layer instanceof AtmosphereLayer)) layer = data.getAtmosphere().getBottomAtmosphereLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,directionVec);
    }

    @Override
    public void sendHeat(@Nonnull HeatPack pack,@Nullable Vec3i directionVec) {
        checkAtmosphereDataLoaded();
        assert data.getAtmosphere() != null;
        Layer layer = data.getAtmosphere().getLayer(pos);
        if(layer == null) return;
        if(!(layer instanceof AtmosphereLayer)) layer = data.getAtmosphere().getBottomAtmosphereLayer(pos);
        if(layer == null) return;
        layer.sendHeat(pack,directionVec);
    }

    /**
     * 通过多点采样获取指定大气的状态值
     * @param method 对每个采样点执行的方法，应返回采样的值
     * @return 平均后的最终状态值
     */
    protected double getAtmosphereValue(@Nonnull BiFunction<int[],Atmosphere,Double> method){
        checkAtmosphereDataLoaded();
        double res = 0;
        int cot = 0;
        for(Map.Entry<int[],AtmosphereData> entry:datas.entrySet()){
            if(isInvalidData(entry.getValue())) continue;
            res += method.apply(entry.getKey(),entry.getValue().getAtmosphere());
            cot ++;
        }
        if(cot == 0) return method.apply(CURRENT,data.getAtmosphere());
        return res/cot;
    }

    /**
     * 通过多点采样提取大气的某个成分
     * @param method 对每个采样点执行的方法，应返回提取的量
     * @return 累计的提取量
     */
    protected double drawAtmosphereProperty(@Nonnull BiFunction<int[],Atmosphere,Double> method){
        checkAtmosphereDataLoaded();
        double res = 0;
        int cot = 0;
        for(Map.Entry<int[],AtmosphereData> entry:datas.entrySet()){
            if(isInvalidData(entry.getValue())) continue;
            res += method.apply(entry.getKey(),entry.getValue().getAtmosphere());
            cot ++;
        }
        if(cot == 0) return 0;
        return res;
    }

    /**
     * 用之前记得{@link #clearInvalidData()}
     * 对每个采样点大气执行某个方法
     * @param method 对每个采样点的大气执行的方法，无返回值
     */
    protected void forAtmospheresDo(@Nonnull BiFunction<int[],Atmosphere,Void> method){
        checkAtmosphereDataLoaded();
        for(Map.Entry<int[],AtmosphereData> entry:datas.entrySet()){
            method.apply(entry.getKey(),entry.getValue().getAtmosphere());
        }
    }

    /**
     * 预处理采样点
     */
    protected void loadDatas(){
        BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos(pos);
        datas.put(CURRENT,data);
        for(int[] dir:DIRS8){
            currentPos.setPos(pos.getX()+dir[0],pos.getY(),pos.getZ()+dir[1]);
            AtmosphereData curDat = system.getDataProvider().getLoadedAtmosphereData(currentPos.getX()>>4,currentPos.getZ()>>4);
            if(curDat == null) continue;
            if(curDat.isUnloaded()) continue;
            if(curDat.getAtmosphere() == null) continue;
            datas.put(dir,curDat);
        }
    }

    /**
     * 检查指定大气数据是否不可用
     * @param data 要检查的大气数据
     * @return 若不可用，比如大气数据已被卸载，则返回false，否则返回true
     */
    protected boolean isInvalidData(@Nullable AtmosphereData data){
        return data == null||data.isUnloaded()||data.getAtmosphere() == null;
    }

    /**
     * 从采样点列表中清除不可用的大气数据
     * 若清除后没有采样点，则会调用{@link #refresh()}来刷新采样点
     */
    protected void clearInvalidData(){
        checkAtmosphereDataLoaded();
        Set<int[]> toClear = new HashSet<>();
        for(Map.Entry<int[],AtmosphereData> entry:datas.entrySet()){
            if(isInvalidData(entry.getValue())) toClear.add(entry.getKey());
        }
        for(int[] key:toClear){
            datas.remove(key);
        }
        if(datas.get(CURRENT) == null) refresh();
    }
}
