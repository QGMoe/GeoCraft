/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.geography.atmosphere.system;

import moe.qingu.orbtellus.api.atmosphere.AtmosphereFlowSource;
import moe.qingu.orbtellus.api.fluid.unit.MillibucketUnit;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.laminarifer.request.FillLaminariferRequest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidRegistry;
import moe.qingu.orbtellus.OrbTellusCraft;
import moe.qingu.orbtellus.api.OTCFluids;
import moe.qingu.orbtellus.api.atmosphere.Atmosphere;
import moe.qingu.orbtellus.api.atmosphere.AtmosphereInfo;
import moe.qingu.orbtellus.api.atmosphere.accessor.AverageAtmosphereAccessor;
import moe.qingu.orbtellus.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.orbtellus.api.atmosphere.gen.IAtmosphereDataProvider;
import moe.qingu.orbtellus.api.atmosphere.storage.AtmosphereData;
import moe.qingu.orbtellus.api.atmosphere.system.BaseAtmosphereSystem;
import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.event.EventFactory;
import moe.qingu.orbtellus.api.property.TemperatureProperty;
import moe.qingu.orbtellus.api.setting.GeoAtmosphereSetting;
import moe.qingu.orbtellus.geography.atmosphere.QiguaiAtmosphere;
import moe.qingu.orbtellus.api.atmosphere.config.CommonAtmosphereSystemInfo;
import moe.qingu.orbtellus.util.BaseUtil;
import moe.qingu.orbtellus.util.WaterUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

import static moe.qingu.orbtellus.api.util.AtmosphereUtil.Constants.WATER_MELT_LATENT_HEAT_PER_QUANTA;

public abstract class QGAtmosphereSystem extends BaseAtmosphereSystem{
    private static final FillLaminariferRequest fillRequest = new FillLaminariferRequest();
    protected final WorldServer world;
    public QGAtmosphereSystem(WorldServer server, AtmosphereInfo info, CommonAtmosphereSystemInfo systemInfo, IAtmosphereDataProvider provider) {
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
                OrbTellusCraft.getLogger().error("AtmosphereSystem {} meet an error while updating atmosphere at ChunkPos({},{}) which started at BlockPos({},{}).",world.provider.getDimension()
                        ,data.pos.x,data.pos.z,data.pos.getXStart(),data.pos.getZStart());
                if(GeoAtmosphereSetting.isEnableDetailedLogging()){
                    OrbTellusCraft.getLogger().error("Atmosphere detailed:{}",atmosphere);
                    OrbTellusCraft.getLogger().error("Error:",e);
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

        if(doRain && state.getBlock() instanceof ILaminarifer){
            try (final FillLaminariferRequest request = fillRequest.open()){
                long filled = 0L;
                if(request.to(world,pos,state)
                        .target((ILaminarifer) state.getBlock())
                        .source(AtmosphereFlowSource.SOURCE)
                        .side(EnumFacing.UP)
                        .specific(accessor.getTemperature(false) <= TemperatureProperty.ICE_POINT? OTCFluids.SNOW : FluidRegistry.WATER)
                        .test()){
                    final int drained = atmosphere.drainWater(MillibucketUnit.QUANTA_VOLUME_INT,pos,false); //mB
                    if(drained>= MillibucketUnit.QUANTA_VOLUME_INT){
                        final int actualDrained;
                        filled = request.amount(MillibucketUnit.toQB(actualDrained = atmosphere.drainWater(MillibucketUnit.QUANTA_VOLUME_INT,pos,true)))
                                .fill(true);
                        atmosphere.addWater(Math.max(0, actualDrained - QBUnit.sampleMillibucketAsInt(world.rand, actualDrained)), pos, true);
                    }
                }

                if(filled>0){
                    if(request.getFluid() == OTCFluids.SNOW)
                        accessor.putHeatToAtmosphere(WATER_MELT_LATENT_HEAT_PER_QUANTA* (filled / MillibucketUnit.QUANTA_VOLUME_DOUBLE));
                    return;
                }
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
