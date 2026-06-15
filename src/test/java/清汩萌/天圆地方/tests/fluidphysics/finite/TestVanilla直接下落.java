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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.world.sandbox.MockSimpleSandbox;
import 清汩萌.天圆地方.world.sandbox.SandboxTestCase;
import 清汩萌.造.工具.YamlUtil;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.空间假设;
import 清汩萌.造.空间.空间工具;
import 清汩萌.造.空间.词块网格;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.assets.MockBlocks.VanillaFluids.*;

/**
 * @author QiguaiAAAA
 */
public final class TestVanilla直接下落 extends FiniteModeTest {
    @ParameterizedTest
    @MethodSource("pullDataFor测试直接下落")
    public void 测试直接下落(final @Nonnull 直接下落测试数据 data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(new Object[]{打包网格数据(data.$网格),
                data.beginPosRaw,
                打包网格数据(data.expected)});
    }

    public static class 直接下落测试数据 extends SandboxTestCase{
        final @Nonnull int[] beginPosRaw;
        final @Nonnull 词块网格 expected;

        private 直接下落测试数据(final @Nonnull 格文件 in,final @Nonnull 格文件 ans) {
            super(in);
            this.expected = ans.获取网格();
            final Map<String,Object> ext = $格文件.获取附加数据();
            Assertions.assertNotNull(ext);
            this.beginPosRaw = 空间工具.转换为游戏坐标(YamlUtil.getIntArray(ext,"fall_pos"));
        }
    }

    @Nonnull
    public static Stream<直接下落测试数据> pullDataFor测试直接下落(){
        final ArrayList<直接下落测试数据> cases = new ArrayList<>();
        SandboxTestCase.findInputs("data/fluidphysics/finite/直接下落/",(scan,in) -> {
            final @Nonnull 格文件 $输入 = 格文件.解析(in.getURI());
            final @Nonnull 格文件 $答案 = 格文件.解析(SandboxTestCase.getAnswerByInput(scan,in).getURI());
            cases.add(new 直接下落测试数据($输入,$答案));
        });
        return cases.stream();
    }

    @SuppressWarnings("unused")
    public static void 测试直接下落_Inner(final @Nonnull Object[] $打包输入网格数据,
                                          final @Nonnull int[] beginPosRaw,
                                          final @Nonnull Object[] $打包答案网格数据){
        final BlockPos beginPos = new BlockPos(beginPosRaw[0],beginPosRaw[1],beginPosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(VANILLA_FLUIDS_BUILDER,恢复网格数据($打包输入网格数据),beginPos);
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
        天圆地方测试.LOGGER.info("output:");
        VANILLA_FLUIDS_BUILDER.打印(sandbox.getStructure());
        空间假设.假设构造相同(恢复网格数据($打包答案网格数据).构造(VANILLA_FLUIDS_BUILDER),sandbox.getStructure());
    }
}
