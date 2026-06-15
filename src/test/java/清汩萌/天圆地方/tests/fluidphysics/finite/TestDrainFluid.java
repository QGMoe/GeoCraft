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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.api.util.QBUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.util.wrappers.FiniteBlockLiquidWrapper;
import 清汩萌.天圆地方.world.sandbox.MockSimpleSandbox;
import 清汩萌.天圆地方.world.sandbox.SandboxTestCase;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.工具.YamlUtil;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.空间工具;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.assets.MockBlocks.BUILDER;
import static 清汩萌.天圆地方.assets.MockBlocks.VanillaFluids.getFlowingByMaterial;

/**
 * @author QGMoe
 */
public final class TestDrainFluid extends FiniteModeTest{

    @ParameterizedTest
    @MethodSource("pullDataForTestDrainFluid")
    public void testDrainFluid(final @Nonnull DrainFluidTestCase c) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(new Object[]{打包网格数据(c.$网格),
                c.drainPosRaw,
                c.expectedDrainedQB});
    }

    public static final class DrainFluidTestCase extends SandboxTestCase {
        final int[] drainPosRaw;
        final long expectedDrainedQB;

        DrainFluidTestCase(final @Nonnull 格文件 $格文件) {
            super($格文件);
            final Map<String,Object> ext = $格文件.获取附加数据();
            Assertions.assertNotNull(ext);
            this.drainPosRaw = 空间工具.转换为游戏坐标(YamlUtil.getIntArray(ext,"drain_at"));
            final Map<String,Object> expected = YamlUtil.getMap(ext,"expected");
            this.expectedDrainedQB = Optional.ofNullable(expected.get("unit"))
                    .map(o -> StringUtil.removeWhites(o.toString()))
                    .map(unit -> Optional.ofNullable(expected.get("drained"))
                            .map(o -> Long.parseLong(o.toString()))
                            .map(value -> value * ("QB".equalsIgnoreCase(unit) ? 1L :
                                    "MB".equalsIgnoreCase(unit) ? QBUtil.MB_VOLUME :
                                            Assertions.<Long>fail("Unknown unit " + unit)))
                            .orElseGet(() -> Assertions.fail("drained not found")))
                    .orElseGet(() -> Assertions.fail("unit not found"));
        }
    }

    @Nonnull
    public static Stream<DrainFluidTestCase> pullDataForTestDrainFluid(){
        return 格文件.获取目录下所有格文件("data/fluidphysics/finite/DrainFluid").map(DrainFluidTestCase::new);
    }


    @SuppressWarnings("unused")
    public static void testDrainFluid_Inner(final @Nonnull Object[] $打包网格数据,
                                            final @Nonnull int[] $drainPosRaw,
                                            final long expectedDrainedQB){
        final BlockPos drainPos = new BlockPos($drainPosRaw[0],$drainPosRaw[1],$drainPosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(BUILDER,恢复网格数据($打包网格数据),drainPos);
        final @Nonnull IBlockState state = world.getBlockState(drainPos);
        天圆地方测试.LOGGER.info("{} block state is {}",drainPos,state);

        final @Nonnull FiniteFlowingVanilla flowing = getFlowingByMaterial(state.getMaterial());
        final @Nonnull FiniteBlockLiquidWrapper wrapper = new FiniteBlockLiquidWrapper(flowing,world,drainPos);
        final @Nullable FluidStack stack = wrapper.drain(Fluid.BUCKET_VOLUME,true);
        final long drained = QBUtil.toQBFromMB(stack == null?0:stack.amount);
        BUILDER.打印(sandbox.getStructure());
        Assertions.assertEquals(expectedDrainedQB,drained);
    }
}
