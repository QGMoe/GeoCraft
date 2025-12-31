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

package top.qiguaiaaaa.geocraft.geography.fluidphysics;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import top.qiguaiaaaa.geocraft.api.configs.value.json.ConfigurableJSON;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @since 0.2.0
 * @author QiguaiAAAA
 */
public final class FluidPhysicsInfo {
    public static final Function<Integer,FluidPhysicsInfoJSONWrapper> CREATE_INFO_FUNC = key -> new FluidPhysicsInfoJSONWrapper(new FluidPhysicsInfo());

    @SerializedName("skyLight")
    @Expose
    SkyLight skyLight = new SkyLight();

    @SerializedName("version")
    @Expose(deserialize = false)
    int version = 1;

    public SkyLight getSkyLight() {
        return skyLight;
    }

    public FluidPhysicsInfo setSkyLight(@Nonnull SkyLight skyLight) {
        this.skyLight = skyLight;
        return this;
    }

    public int getVersion() {
        return version;
    }

    /**
     * @since 0.2.0
     * @author QiguaiAAAA
     */
    public static final class SkyLight{
        @SerializedName("checkWhenIceSmelting")
        @Expose
        public boolean checkWhenIceSmelting = false;
        @SerializedName("checkWhenSnowSmelting")
        @Expose
        public boolean checkWhenSnowSmelting = false;
        @SerializedName("checkWhenSnowLayerSmelting")
        @Expose
        public boolean checkWhenSnowLayerSmelting = false;

        public SkyLight checkWhenIceSmelting(boolean checkWhenIceSmelting) {
            this.checkWhenIceSmelting = checkWhenIceSmelting;
            return this;
        }

        public SkyLight checkWhenSnowLayerSmelting(boolean checkWhenSnowLayerSmelting) {
            this.checkWhenSnowLayerSmelting = checkWhenSnowLayerSmelting;
            return this;
        }

        public SkyLight checkWhenSnowSmelting(boolean checkWhenSnowSmelting) {
            this.checkWhenSnowSmelting = checkWhenSnowSmelting;
            return this;
        }
    }

    public static final class FluidPhysicsInfoJSONWrapper extends ConfigurableJSON<FluidPhysicsInfo>{

        public FluidPhysicsInfoJSONWrapper(@Nonnull String jsonStr) {
            super(jsonStr);
        }

        public FluidPhysicsInfoJSONWrapper(@Nonnull FluidPhysicsInfo json) {
            super(json);
        }

        @Override
        protected Class<FluidPhysicsInfo> getObjectClass() {
            return FluidPhysicsInfo.class;
        }

        @Nonnull
        public FluidPhysicsInfo getInfo(){
            return json;
        }

        @Nonnull
        public SkyLight getSkyLight(){
            return json.skyLight;
        }
    }
}
