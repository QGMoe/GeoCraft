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

package 清汩萌.天圆地方.tests.fluidphysics.finite;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.wrappers.FiniteBlockLiquidWrapper;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.world.sandbox.MockSimpleSandbox;
import 清汩萌.天圆地方.world.sandbox.SandboxTestCase;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.工具.YamlUtil;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.空间工具;
import 清汩萌.造.词块.词块;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.assets.MockBlocks.VanillaFluids.*;

/**
 * @author QiguaiAAAA
 */
public final class TestPlaceFluid extends FiniteModeTest {
    @ParameterizedTest
    @MethodSource("pullDataForTestPlaceFluid")
    public void testPlaceFluid(final @Nonnull PlaceFluidTestData data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(new Object[]{打包网格数据(data.$网格),
                data.placePosRaw,
                data.fluidToPlace.toString(),
                data.expectedAblePlace});
    }

    public static final class PlaceFluidTestData extends SandboxTestCase {
        public final @Nonnull int[] placePosRaw;
        public final @Nonnull 词块 fluidToPlace;
        public final boolean expectedAblePlace;

        public PlaceFluidTestData(final @Nonnull 格文件 $格文件) {
            super($格文件);
            final Map<String,Object> ext = $格文件.获取附加数据();
            Assertions.assertNotNull(ext);
            final Map<String,Object> place = YamlUtil.getMap(ext,"place");
            this.placePosRaw = 空间工具.转换为游戏坐标(YamlUtil.getIntArray(place,"at"));
            this.fluidToPlace = 词块.of(YamlUtil.getString(place,"fluid"));
            this.expectedAblePlace = YamlUtil.getBool(ext,"expected");
        }
    }

    @Nonnull
    public static Stream<PlaceFluidTestData> pullDataForTestPlaceFluid(){
        final ArrayList<PlaceFluidTestData> cases = new ArrayList<>();
        SandboxTestCase.findInputs("data/fluidphysics/finite/PlaceFluid/",
                (scan,in) -> cases.add(new PlaceFluidTestData(格文件.解析(in.getURI()))));
        return cases.stream();
    }

    @SuppressWarnings("unused")
    public static void testPlaceFluid_Inner(final @Nonnull Object[] $打包网格数据,
                                            final @Nonnull int[] placePosRaw,
                                            final @Nonnull String placementRaw,
                                            final boolean expectedAbleToPlace){
        final BlockPos placePos = new BlockPos(placePosRaw[0],placePosRaw[1],placePosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(VANILLA_FLUIDS_BUILDER,恢复网格数据($打包网格数据),placePos);
        final @Nonnull IBlockState state = VANILLA_FLUIDS_BUILDER.进行映射(词块.of(StringUtil.removeWhites(placementRaw)));
        Assertions.assertFalse(state.getValue(BlockLiquid.LEVEL)>7);
        天圆地方测试.LOGGER.info("{} block state is {}",placePos,world.getBlockState(placePos));

        final @Nonnull FiniteFlowingVanilla flowing = getFlowingByMaterial(state.getMaterial());
        final @Nonnull FiniteBlockLiquidWrapper wrapper = new FiniteBlockLiquidWrapper(flowing,world,placePos);
        final int quanta = 8- state.getValue(BlockLiquid.LEVEL);
        final int amount = quanta* FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME;
        final int filledAmount = wrapper.fill(new FluidStack(flowing.fluid,amount),true);
        天圆地方测试.LOGGER.info("Input Amount: {} mB, Filled Amount: {} mB",amount,filledAmount);
        Assertions.assertEquals(expectedAbleToPlace,filledAmount==amount);
        VANILLA_FLUIDS_BUILDER.打印(sandbox.getStructure());
    }
}
