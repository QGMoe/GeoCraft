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

package top.qiguaiaaaa.geocraft_test.tests.fluidphysics.finite;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.tests.fluidphysics.FluidPhysicsTest;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSimpleSandbox;
import top.qiguaiaaaa.geocraft_test.world.sandbox.SandboxTestCase;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.VanillaFluids.*;

/**
 * @author QiguaiAAAA
 */
public final class TestVanilla直接下落 extends FluidPhysicsTest {
    @ParameterizedTest
    @MethodSource("pullDataFor测试直接下落")
    public void 测试直接下落(final @Nonnull 直接下落测试数据 data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(Pair.of(new Class<?>[]{int.class,int[].class,String[].class,String[].class},
                new Object[]{data.zLength,data.beginPosRaw,data.structure,data.expected}));
    }

    public static class 直接下落测试数据 extends SandboxTestCase{
        final @Nonnull int[] beginPosRaw;
        final @Nonnull String[] expected;

        private 直接下落测试数据(final int size,final @Nonnull int[] beginPosRaw, final @Nonnull String[] structure,final @Nonnull String[] expected) {
            super(structure,size);
            this.beginPosRaw = beginPosRaw;
            this.expected = expected;
        }
    }

    @Nonnull
    public static Stream<直接下落测试数据> pullDataFor测试直接下落(){
        final ArrayList<直接下落测试数据> cases = new ArrayList<>();
        SandboxTestCase.findInputs("data/fluidphysics/finite/直接下落/",(scan,in,scannerIn) -> {
            final int height = scannerIn.nextInt();
            final int size = scannerIn.nextInt();
            final int[] beginPosRaw = new int[]{scannerIn.nextInt(),scannerIn.nextInt(),scannerIn.nextInt()};
            final String[] structure = new String[height];
            final String[] expected = new String[height];

            scannerIn.nextLine(); //jump rest of line
            SandboxTestCase.buildStructureFromScanner(structure,scannerIn,size);

            try (final @Nonnull Scanner scannerOut = SandboxTestCase.getScannerOf(SandboxTestCase.getAnswerByInput(scan,in))){
                SandboxTestCase.buildStructureFromScanner(expected,scannerOut,size);
            }

            cases.add(new 直接下落测试数据(size,beginPosRaw,structure,expected));
        });
        return cases.stream();
    }

    @SuppressWarnings("unused")
    public static void 测试直接下落_Inner(final int size,
                                          final int[] beginPosRaw,
                                          final @Nonnull String[] structure,
                                          final @Nonnull String[] expected){
        final BlockPos beginPos = new BlockPos(beginPosRaw[0],beginPosRaw[1],beginPosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(BUILDER,beginPos,size,size,structure);
        final @Nonnull IBlockState beginState = world.getBlockState(beginPos);
        final @Nonnull FiniteFlowingVanilla flowing = getFlowingByMaterial(beginState.getMaterial());
        Assertions.assertNotNull(flowing);
        flowing.flowDown(
                world,
                beginPos,
                world.getBlockState(beginPos.down()),
                8-world.getBlockState(beginPos).getValue(BlockLiquid.LEVEL),
                5
        );
        GeoCraftTest.LOGGER.info("output:");
        BUILDER.print(sandbox.getStructure());
        BUILDER.assertEqualStructure(sandbox.getStructure(),expected,size);
    }
}
