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

import net.minecraft.util.math.BlockPos;
import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereData;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.util.exception.AtmosphereNotLoadedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * {@link IAtmosphereAccessor}抽象层，没有数据获取功能
 */
public abstract class AbstractAtmosphereAccessor implements IAtmosphereAccessor{
    protected final IAtmosphereSystem system;
    protected final AtmosphereData data;
    protected final BlockPos pos;
    protected boolean notAir;
    protected int skyLight = -1;

    public AbstractAtmosphereAccessor(@Nonnull IAtmosphereSystem system, @Nonnull AtmosphereData data,@Nonnull BlockPos pos, boolean notAir) {
        this.system = system;
        this.data = data;
        this.pos = pos.toImmutable();
        this.notAir = notAir;
    }

    @Nonnull
    @Override
    public IAtmosphereSystem getSystem() {
        return system;
    }

    @Nonnull
    @Override
    public BlockPos getPos() {
        return pos;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nullable
    @Override
    public Atmosphere getAtmosphereHere() {
        if(data.isUnloaded()) return null;
        return data.getAtmosphere();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Nullable
    @Override
    public AtmosphereData getAtmosphereDataHere() {
        return data.isUnloaded() ?null:data;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isAtmosphereLoaded() {
        return isAtmosphereDataLoaded() && data.getAtmosphere().isLoaded();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean refresh() {
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public void setSkyLight(int light) {
        this.skyLight = light;
    }

    @Override
    public int getSkyLight() {
        return skyLight;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public void setNotAir(boolean notAir) {
        this.notAir = notAir;
    }

    @Override
    public double getTemperature(boolean notAir) {
        if(this.notAir == notAir) return getTemperature();
        boolean old = this.notAir;
        setNotAir(notAir);
        double temp = getTemperature();
        setNotAir(old);
        return temp;
    }

    @Override
    public void close() {}

    /**
     * 检查大气数据是否已经被加载，该方法不会检查大气是否已经初始化
     * @return 若大气数据已经加载，则返回true
     */
    protected boolean isAtmosphereDataLoaded(){
        return !data.isUnloaded() && data.getAtmosphere() != null;
    }

    /**
     * 检查大气数据是否已经已经加载，该方法会调用{@link #isAtmosphereDataLoaded()}
     * 需要注意若检查失败，会抛出{@link AtmosphereNotLoadedException}
     * @throws AtmosphereNotLoadedException 若大气数据未加载，则抛出该错误。
     */
    protected void checkAtmosphereDataLoaded(){
        if(!isAtmosphereDataLoaded()) throw new AtmosphereNotLoadedException(getWorld().provider.getDimension(),pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorld(),pos);
    }
}
