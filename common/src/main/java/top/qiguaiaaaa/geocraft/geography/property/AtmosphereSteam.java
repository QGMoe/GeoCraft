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

package top.qiguaiaaaa.geocraft.geography.property;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.AtmosphereLayer;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.property.FluidProperty;
import top.qiguaiaaaa.geocraft.api.property.IAtmosphereProperty;
import top.qiguaiaaaa.geocraft.api.state.FluidState;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.math.Altitude;
import top.qiguaiaaaa.geocraft.geography.state.SteamState;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;

import javax.annotation.Nonnull;

public class AtmosphereSteam extends FluidProperty implements IAtmosphereProperty {
    public static final AtmosphereSteam STEAM = new AtmosphereSteam();
    protected AtmosphereSteam() {
        super(FluidRegistry.WATER);
        setRegistryName(GeoCraft.MODID,"steam");
    }

    @Override
    public boolean haveWindEffect() {
        return false;
    }

    @Override
    public boolean isFlowable() {
        return true;
    }

    @Override
    public void onFlow(@Nonnull AtmosphereLayer from, Chunk fromChunk, Atmosphere to, Chunk toChunk, @Nonnull EnumFacing direction, @Nonnull Vec3d windSpeed) {
        double fromTop = from.getBeginY()+from.getDepth();
        if (to.getUnderlying(BlockPos.ORIGIN).getAltitude().get() > fromTop) return;
        FluidState steam = from.getSteam();
        if(steam == null) return;
        double speed = MathUtil.获得带水平正负方向的速度(windSpeed,direction);
        BlockPos centerPos = new BlockPos(0,from.getBeginY()+from.getDepth()/2,0);
        double fromWP = from.getWaterPressure(),toWP = to.getWaterPressure(centerPos);
        speed = speed+计算水气压差动力(fromWP,toWP);
        if(speed >1e-5){
            int waterTransferAmount = getSteamTransferAmount(steam.getFluidAmount()/4.0,speed);
            if(steam.fill(-waterTransferAmount,true)!=0){
                to.addSteam(waterTransferAmount,centerPos,true);
            }
        }else if(speed <-1e-5){
            Layer layer = to.getLayer(centerPos);
            if(!(layer instanceof AtmosphereLayer)) return;
            FluidState toSteam = ((AtmosphereLayer)layer).getSteam();
            if(toSteam == null) return;
            int transferAmount = getSteamTransferAmount(toSteam.getFluidAmount()
                            *Math.min((fromTop-layer.getBeginY())/ from.getDepth(),1)/4.0,
                    -speed);
            if(toSteam.fill(-transferAmount,true)!=0){
                steam.fill(transferAmount,true);
            }
        }
    }

    @Override
    public void onConvect(@Nonnull AtmosphereLayer lower, @Nonnull AtmosphereLayer upper, double speed) {
        FluidState from = lower.getSteam();
        FluidState to = upper.getSteam();
        if(from == null || to == null) return;
        double fromWP = lower.getWaterPressure(),toWP = upper.getWaterPressure();
        speed = speed+计算水气压差动力(fromWP,toWP);
        if(speed >1e-6){
            double dis = Altitude.to物理高度(upper.getBeginY()+upper.getDepth()/2-(lower.getBeginY()+lower.getDepth()/2));
            int waterTransferAmount = getSteamTransferAmountVertically(from.getFluidAmount()/ AtmosphereUtil.Constants.大气单元底面积,speed, dis);
            if(from.fill(-waterTransferAmount,true)!=0){
                to.fill(waterTransferAmount,true);
            }
        }else if(speed <-1e-6){
            double dis = Altitude.to物理高度(upper.getBeginY()+upper.getDepth()/2-(lower.getBeginY()+lower.getDepth()/2));
            int waterTransferAmount = getSteamTransferAmountVertically(to.getFluidAmount()/ AtmosphereUtil.Constants.大气单元底面积,-speed, dis);
            if(to.fill(-waterTransferAmount,true)!=0){
                from.fill(waterTransferAmount,true);
            }
        }
    }

    @Nonnull
    @Override
    public FluidState getStateInstance() {
        return new SteamState(0);
    }

    public static int getSteamTransferAmount(double totalAmount, double windSpeed){
        return (int) (totalAmount*windSpeed/(windSpeed+17));
    }
    public static int getSteamTransferAmountVertically(double totalAmount, double windSpeed,double distance){
        return (int) (totalAmount*windSpeed/(windSpeed+distance)*216); //时间步长
    }

    public static double 计算水气压差动力(double from,double to){
        return (from-to)/1000;
    }
}
