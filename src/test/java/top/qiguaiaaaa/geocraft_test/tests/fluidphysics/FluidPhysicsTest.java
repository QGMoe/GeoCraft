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

package top.qiguaiaaaa.geocraft_test.tests.fluidphysics;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import top.qiguaiaaaa.geocraft.configs.GeneralConfig;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.world.MockSimpleWorld;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSandboxEnvBuilder;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSimpleSandbox;
import top.qiguaiaaaa.geocraft_test.world.storage.MockWorldInfo;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.Bases.〇;
import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.VanillaFluids.曜;

/**
 * @author QiguaiAAAA
 */
public class FluidPhysicsTest extends GeoCraftTest {
    protected static MockSimpleWorld world;

    @BeforeEach
    public void beforeFluidPhysicsTest() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test();
    }

    @SuppressWarnings("unused")
    public static void beforeFluidPhysicsTest_Inner(){
        GeneralConfig.ENABLE_BLOCK_UPDATER.setValue(false);
        world = MockSimpleWorld.create(MockWorldInfo.create(b-> b.withGameType(GameType.CREATIVE)), false);
        world.setAirBlock(〇);
    }

    @AfterEach
    public void afterFluidPhysicsTest() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test();
    }

    @SuppressWarnings("unused")
    public static void afterFluidPhysicsTest_Inner(){
        world = null;
        GeneralConfig.ENABLE_BLOCK_UPDATER.setValue(true);
    }

    @Nonnull
    public static MockSimpleSandbox initWorldSandbox(final @Nonnull MockSandboxEnvBuilder<?> builder,
                                            final @Nonnull BlockPos beginPos,
                                            final int zLength,
                                            final int xLength,
                                            final @Nonnull String[] structure){
        final @Nonnull MockSimpleSandbox sandbox = new MockSimpleSandbox(builder.generateFromCharacters(zLength,xLength,structure));
        sandbox.setOuterBlock(曜);
        world.setSandbox(sandbox);
        GeoCraftTest.LOGGER.info("begin pos {}",beginPos);
        builder.print(sandbox.getStructure());
        return sandbox;
    }
}
