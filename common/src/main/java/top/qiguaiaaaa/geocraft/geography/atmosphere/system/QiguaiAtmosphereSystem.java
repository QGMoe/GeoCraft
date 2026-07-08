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

package top.qiguaiaaaa.geocraft.geography.atmosphere.system;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.GeoFluids;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.AverageAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.BaseAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.setting.GeoAtmosphereSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.geography.atmosphere.QiguaiAtmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.CommonAtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.util.BaseUtil;
import top.qiguaiaaaa.geocraft.util.WaterUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

import static top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;

public abstract class QiguaiAtmosphereSystem extends BaseAtmosphereSystem {
    protected final WorldServer world;
    public QiguaiAtmosphereSystem(WorldServer server, AtmosphereInfo info, CommonAtmosphereSystemInfo systemInfo, IAtmosphereDataProvider provider) {
        super(info, provider);
        this.world = server;
        worldInfo.setSystem(this);
        worldInfo.waterFreeze(systemInfo.canWaterFreeze());
        worldInfo.waterEvaporate(systemInfo.canWaterEvaporate());
        worldInfo.setRainSmoothingConstant(systemInfo.getRainSmoothingConstant());
        worldInfo.setVaporExchangeRate(systemInfo.getVaporExchangeRate());
        provider.setMaxLoadDistance(systemInfo.getMaxLoadDistance());
    }

    @Override
    public void updateTick() {
        if(stopped) return;
        updateAtmospheres();
        Iterator<Chunk> persistentChunkIterator = world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator());
        while (persistentChunkIterator.hasNext()){
            Chunk chunk = persistentChunkIterator.next();
            weatherTick(chunk);
        }

        dataProvider.tick();
    }

    @Override
    public IAtmosphereAccessor getAccessor(@Nonnull BlockPos pos, boolean notAir) {
        AtmosphereData data = dataProvider.getLoadedAtmosphereData(pos.getX()>>4,pos.getZ()>>4);
        if(data == null) return null;
        if(data.getAtmosphere() == null) return null;
        if(!data.getAtmosphere().isLoaded()) return null;
        return getAccessor(data,pos,notAir);
    }

    public IAtmosphereAccessor getAccessor(@Nonnull AtmosphereData data,@Nonnull BlockPos pos, boolean notAir){
        return new AverageAtmosphereAccessor(this,data,pos,notAir);
    }

    protected void updateAtmospheres(){
        Collection<AtmosphereData> dataList = dataProvider.getLoadedAtmosphereDataCollection();
        for (AtmosphereData data:dataList) {
            if(world.getWorldTime()%60 != Math.abs(data.pos.x+data.pos.z)%60) continue;
            QiguaiAtmosphere atmosphere = (QiguaiAtmosphere) data.getAtmosphere();
            if(atmosphere == null){
                if(data.isEmpty() && !data.isUnloadQueued()){
                    dataProvider.queueUnloadAtmosphereData(data.pos.x,data.pos.z);
                    continue;
                }
                data.setAtmosphere(generateAtmosphere(data.getChunk(),data));
                continue;
            }

            try{
                if(data.getChunk() == null){
                    if(!atmosphere.isLoaded()) continue;
                    atmosphere.updateTick(null);
                }else{
                    if(!atmosphere.isLoaded()){
                        if(!data.isEmpty()) atmosphere.deserializeNBT(data.getSaveCompound());
                        atmosphere.onLoad(data.getChunk(), worldInfo);
                    }
                    if(atmosphere.isLoaded()){
                        atmosphere.updateTick(data.getChunk());
                        if(atmosphere.tickTime()%4 == 3){
                            data.saveAtmosphere();
                        }
                    }
                }
            }catch (Throwable e){
                GeoCraft.getLogger().error("AtmosphereSystem {} meet an error while updating atmosphere at ChunkPos({},{}) which started at BlockPos({},{}).",world.provider.getDimension()
                        ,data.pos.x,data.pos.z,data.pos.getXStart(),data.pos.getZStart());
                if(GeoAtmosphereSetting.isEnableDetailedLogging()){
                    GeoCraft.getLogger().error("Atmosphere detailed:{}",atmosphere);
                    GeoCraft.getLogger().error("Error:",e);
                }
            }

        }
    }

    /**
     * 处理下雨等事件
     */
    protected void weatherTick(final @Nonnull Chunk chunk){
        final @Nullable AtmosphereData data = getChunkLoadedAtmosphereData(chunk);
        if(data == null) return;
        final Atmosphere atmosphere = data.getAtmosphere();

        int x = chunk.x<<4;
        int z = chunk.z<<4;
        int rand = world.rand.nextInt();
        BlockPos randPos = world.getPrecipitationHeight(new BlockPos(x + (rand & 15), 0, z + (rand >> 8 & 15)));

        if (!world.isAreaLoaded(randPos, 1)) return;

        BlockPos pos = randPos.down();

        IAtmosphereAccessor accessor = getAccessor(data,pos,true);

        IBlockState state = world.getBlockState(pos);

        assert atmosphere != null;
        boolean isRaining = atmosphere.getWeather(pos).isRainy();
        double rainPossibility = isRaining? WaterUtil.getRainPossibility(accessor):0;
        boolean doRain = BaseUtil.getRandomResult(world.rand,rainPossibility);

        if(doRain && state.getBlock() instanceof ILayeredFluidHost){
            long filled = 0;
            ILayeredFluidHost block = (ILayeredFluidHost) state.getBlock();
            Fluid fluidToFill = FluidRegistry.WATER;
            if(accessor.getTemperature(false)<= TemperatureProperty.ICE_POINT) fluidToFill = GeoFluids.SNOW;
            if(block.canFill(world,pos,state, fluidToFill, EnumFacing.UP,Blocks.AIR.getDefaultState())){
                final int drained = atmosphere.drainWater(FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,pos,false); //mB
                if(drained>=FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME){
                    atmosphere.drainWater(FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,pos,true);
                    filled = block.addAmountInQB(world,pos,state,fluidToFill, QBUtil.toQBFromMB(drained),true);
                }
            }
            if(filled>0){
                if(fluidToFill == GeoFluids.SNOW){
                    accessor.putHeatToAtmosphere(WATER_MELT_LATENT_HEAT_PER_QUANTA);
                }
                return;
            }
        }

        isRaining = atmosphere.getWeather(randPos).isRainy();

        double freezePossibility = worldInfo.canWaterFreeze()?WaterUtil.getFreezePossibility(accessor):0;

        if (worldInfo.canWaterFreeze() && BaseUtil.getRandomResult(world.rand,freezePossibility) && worldInfo.canWaterFreeze(pos,true)) {
            world.setBlockState(pos, Blocks.ICE.getDefaultState());
            accessor.putHeatToUnderlying(WATER_MELT_LATENT_HEAT_PER_QUANTA*8);
            return;
        }

        if(!isRaining || !doRain) return;

        IBlockState newState = EventFactory.onAtmosphereRainAndSnow(chunk,getAccessor(data,randPos,false),atmosphere,randPos,rainPossibility);
        if(newState != null){
            world.setBlockState(randPos,newState);
            return;
        }

        state.getBlock().fillWithRain(world, pos);
    }
}
