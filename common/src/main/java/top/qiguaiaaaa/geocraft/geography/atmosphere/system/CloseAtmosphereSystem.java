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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.api.atmosphere.gen.IAtmosphereDataProvider;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.geography.atmosphere.CloseAtmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.DirectAtmosphereAccessor;
import top.qiguaiaaaa.geocraft.geography.atmosphere.info.CloseAtmosphereSystemInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CloseAtmosphereSystem extends QGAtmosphereSystem {
    public static final float HELL_TEMP = 400;
    protected final float temp;
    protected final double maxWindSpeed,pressure;

    public CloseAtmosphereSystem(@Nonnull WorldServer world, @Nonnull AtmosphereInfo worldInfo, @Nonnull CloseAtmosphereSystemInfo systemInfo, @Nonnull IAtmosphereDataProvider dataProvider){
        super(world,worldInfo,systemInfo,dataProvider);
        temp = systemInfo.getFinalTemperature();
        maxWindSpeed = systemInfo.getMaxWindSpeed();
        pressure = systemInfo.getPressure();
    }

    @Override
    public IAtmosphereAccessor getAccessor(@Nonnull AtmosphereData data, @Nonnull BlockPos pos, boolean notAir) {
        return new DirectAtmosphereAccessor(this,data,pos,notAir){
            @Override
            public boolean canAccessAtmosphere() {
                return true;
            }
        };
    }

    @Nullable
    @Override
    protected Atmosphere generateAtmosphere(@Nullable Chunk chunk, @Nonnull AtmosphereData data) {
        if(data.isEmpty() && chunk == null) return null;
        CloseAtmosphere atmosphere = new CloseAtmosphere(temp);
        atmosphere.setLocation(data.pos.x,data.pos.z);
        atmosphere.setMaxWindSpeed(maxWindSpeed);
        atmosphere.setPressure(pressure);
        if(!data.isEmpty()){
            atmosphere.deserializeNBT(data.getSaveCompound());
        }
        atmosphere.onLoad(chunk,worldInfo);
        return atmosphere;
    }
}
