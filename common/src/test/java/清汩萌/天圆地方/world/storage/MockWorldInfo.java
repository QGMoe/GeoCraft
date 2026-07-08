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

package 清汩萌.天圆地方.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.storage.WorldInfo;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.Consumer;

/**
 * @author QiguaiAAAA
 */
public class MockWorldInfo extends WorldInfo {

    protected MockWorldInfo(){}

    public MockWorldInfo(@Nonnull final MockWorldInfoBuilder builder){
        super(builder.build());
    }

    @Nonnull
    public static MockWorldInfo create(final @Nonnull Consumer<MockWorldInfoBuilder> builderConsumer){
        final MockWorldInfoBuilder builder = new MockWorldInfoBuilder();
        builderConsumer.accept(builder);
        return new MockWorldInfo(builder);
    }

    public static final class MockWorldInfoBuilder{
        private static final Random random = new Random(System.nanoTime());
        private final NBTTagCompound compound = new NBTTagCompound();

        private MockWorldInfoBuilder(){
            final NBTTagCompound version = new NBTTagCompound();
            version.setString("Name","1.12.2");
            version.setInteger("Id",0);
            version.setString("Snapshot","?");
            compound.setTag("Version",version);

            this.withSpawnPos(0,0,0)
                    .withSeed(random.nextLong())
                    .withTotalTime(0);
        }

        @Nonnull
        public MockWorldInfoBuilder withSeed(final long seed){
            compound.setLong("RandomSeed",seed);
            return this;
        }

        @Nonnull
        public MockWorldInfoBuilder withGameType(final @Nonnull GameType type){
            Assertions.assertNotNull(type);
            compound.setInteger("GameType",type.getID());
            return this;
        }

        @Nonnull
        public MockWorldInfoBuilder withSpawnPos(final int x,final int y,final int z){
            compound.setInteger("SpawnX",x);
            compound.setInteger("SpawnY",y);
            compound.setInteger("SpawnZ",z);
            return this;
        }

        @Nonnull
        public MockWorldInfoBuilder withTotalTime(final long time){
            compound.setLong("Time",time);
            return this;
        }

        @Nonnull
        private NBTTagCompound build(){
            return compound;
        }
    }
}
