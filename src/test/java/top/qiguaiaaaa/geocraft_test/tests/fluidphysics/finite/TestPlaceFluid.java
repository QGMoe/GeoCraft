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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.wrappers.PhysicsBlockLiquidWrapper;
import top.qiguaiaaaa.geocraft_test.GeoCraftTest;
import top.qiguaiaaaa.geocraft_test.tests.fluidphysics.FluidPhysicsTest;
import top.qiguaiaaaa.geocraft_test.world.sandbox.MockSimpleSandbox;
import top.qiguaiaaaa.geocraft_test.world.sandbox.SandboxTestCase;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.VanillaFluids.BUILDER;
import static top.qiguaiaaaa.geocraft_test.assets.MockBlocks.VanillaFluids.getFlowingByMaterial;

/**
 * @author QiguaiAAAA
 */
public final class TestPlaceFluid extends FiniteModeTest {
    @ParameterizedTest
    @MethodSource("pullDataForTestPlaceFluid")
    public void testPlaceFluid(final @Nonnull PlaceFluidTestData data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(Pair.of(new Class<?>[]{String[].class,int.class,int.class,int[].class,String.class,boolean.class},
                new Object[]{data.structure,data.zLength,data.xLength,data.placePosRaw,data.fluidToPlace,data.expectedAblePlace}));
    }

    public static final class PlaceFluidTestData extends SandboxTestCase {
        private final String name;
        public final int[] placePosRaw;
        public final String fluidToPlace;
        public final boolean expectedAblePlace;

        public PlaceFluidTestData(final @Nonnull String name,
                                  final @Nonnull String[] structure,
                                      final int zLength,
                                      final int xLength,
                                      @Nonnull int[] placePosRaw,
                                      @Nonnull String fluidToPlace,
                                      final boolean expectedAblePlace) {
            super(structure,zLength,xLength);
            this.name = name;
            this.placePosRaw = placePosRaw;
            this.fluidToPlace = fluidToPlace;
            this.expectedAblePlace = expectedAblePlace;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 第一行三个整数 h r c，表示层数，每层行数和每层列数。
     * 第二行三个整数 x y z，表示在测试结构中第 y+1 层，第 z+1 行，第 x+1 列尝试放置一个流体方块。
     * 第三行一个字符串，表示需要放置的流体方块
     * 然后是结构，共 h 层，每层 r 行 c 列，层与层间间隔一行。
     * @return 坡度流动测试数据
     */
    @Nonnull
    public static Stream<PlaceFluidTestData> pullDataForTestPlaceFluid(){
        final ArrayList<PlaceFluidTestData> cases = new ArrayList<>();
        SandboxTestCase.findInputs("data/fluidphysics/finite/PlaceFluid/",(scan,in,scannerIn) -> {
            final int height = scannerIn.nextInt();
            final int zLength = scannerIn.nextInt();
            final int xLength = scannerIn.nextInt();
            final int[] placePosRaw = new int[]{scannerIn.nextInt(),scannerIn.nextInt(),scannerIn.nextInt()};
            final String placement = scannerIn.next();
            scannerIn.nextLine(); //jump rest of line

            final String[] structure = new String[height];
            final boolean expected;

            SandboxTestCase.buildStructureFromScanner(structure,scannerIn,zLength);

            try (final Scanner scannerOut = SandboxTestCase.getScannerOf(SandboxTestCase.getAnswerByInput(scan,in))){
                final String input = scannerOut.next();
                if("Y".equalsIgnoreCase(input)){
                    expected = true;
                }else if("N".equalsIgnoreCase(input)){
                    expected = false;
                }else{
                    Assertions.fail("Invalid expected result: "+input);
                    expected = false; //won't reach here
                }
            }

            cases.add(new PlaceFluidTestData(in.getPath(),structure,zLength,xLength,placePosRaw,placement,expected));
        });
        return cases.stream();
    }

    @SuppressWarnings("unused")
    public static void testPlaceFluid_Inner(final @Nonnull String[] structure,
                                            final int zLength,
                                            final int xLength,
                                            final int[] placePosRaw,
                                            final @Nonnull String placement,
                                            final boolean expectedAbleToPlace){
        final BlockPos placePos = new BlockPos(placePosRaw[0],placePosRaw[1],placePosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(BUILDER,placePos,zLength,xLength,structure);
        final @Nonnull IBlockState state = BUILDER.getBlockStateByName(placement.trim());
        Assertions.assertFalse(state.getValue(BlockLiquid.LEVEL)>7);
        GeoCraftTest.LOGGER.info("{} block state is {}",placePos,world.getBlockState(placePos));

        final @Nonnull FiniteFlowingVanilla flowing = getFlowingByMaterial(state.getMaterial());
        final @Nonnull PhysicsBlockLiquidWrapper wrapper = new PhysicsBlockLiquidWrapper(flowing,world,placePos);
        final int quanta = 8- state.getValue(BlockLiquid.LEVEL);
        final int amount = quanta* FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
        wrapper.setExpectedQuanta(quanta);
        final int filledAmount = wrapper.fill(new FluidStack(flowing.fluid,amount),true);
        GeoCraftTest.LOGGER.info("Input Amount: {} mB, Filled Amount: {} mB",amount,filledAmount);
        Assertions.assertEquals(expectedAbleToPlace,filledAmount==amount);
        BUILDER.print(sandbox.getStructure());
    }
}
