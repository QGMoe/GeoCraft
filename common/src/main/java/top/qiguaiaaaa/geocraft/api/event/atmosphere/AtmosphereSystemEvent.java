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

package top.qiguaiaaaa.geocraft.api.event.atmosphere;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import top.qiguaiaaaa.geocraft.api.atmosphere.system.IAtmosphereSystem;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemInfo;
import top.qiguaiaaaa.geocraft.api.atmosphere.config.AtmosphereSystemType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 大气系统有关事件
 */
public class AtmosphereSystemEvent extends Event {
    private final WorldServer world;

    public AtmosphereSystemEvent(@Nonnull WorldServer world) {
        this.world = world;
    }
    @Nonnull
    public WorldServer getWorld() {
        return world;
    }

    /**
     * 当一个新的世界加载的时候，会发布该事件以获取对应的大气系统
     */
    @Cancelable
    public static class Create extends AtmosphereSystemEvent{
        private final AtmosphereSystemInfo systemInfo;
        private IAtmosphereSystem systemToBeUsed;

        public Create(@Nonnull WorldServer world,@Nonnull AtmosphereSystemInfo info) {
            super(world);
            systemInfo = info;
        }

        /**
         * 获取目前被设置的大气系统
         * @return 将被使用的大气系统
         */
        @Nullable
        public IAtmosphereSystem getSystem() {
            return systemToBeUsed;
        }

        @Nonnull
        public AtmosphereSystemType getType(){
            return systemInfo.getType();
        }

        @Nonnull
        public AtmosphereSystemInfo getSystemInfo() {
            return systemInfo;
        }

        /**
         * 设置被使用的大气系统
         * @param systemToBeUsed 将被使用的大气系统
         */
        public void setSystem(@Nullable IAtmosphereSystem systemToBeUsed) {
            this.systemToBeUsed = systemToBeUsed;
        }
    }
}
