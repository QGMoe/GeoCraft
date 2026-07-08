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

import com.google.common.collect.Maps;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import 清汩萌.天圆地方.util.网格工具;
import 清汩萌.天圆地方.world.sandbox.MockSimpleSandbox;
import 清汩萌.天圆地方.world.sandbox.SandboxTestCase;
import 清汩萌.天圆地方.world.sandbox.TestArg;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.格文件;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static 清汩萌.天圆地方.assets.MockBlocks.Liquids.*;

/**
 * @author QiguaiAAAA
 */
public final class TestVanillaSlopeAlgorithm extends FiniteModeTest {
    public static Map<String,EnumFacing> FACING_ALIASES = Maps.newHashMap();

    @SuppressWarnings("unused")
    public static void initFacingAliases(){
        final @Nonnull Function<EnumFacing,Consumer<String>> putAllTo = facing -> alias -> FACING_ALIASES.put(alias,facing);
        Arrays.asList("前","北","HEAD","AHEAD").forEach(putAllTo.apply(EnumFacing.NORTH));
        Arrays.asList("后","南","BACK","BACKWARD").forEach(putAllTo.apply(EnumFacing.SOUTH));
        Arrays.asList("左","西","LEFT").forEach(putAllTo.apply(EnumFacing.WEST));
        Arrays.asList("右","东","RIGHT").forEach(putAllTo.apply(EnumFacing.EAST));
        Arrays.asList("上","UPSTAIRS").forEach(putAllTo.apply(EnumFacing.UP));
        Arrays.asList("下","DOWNSTAIRS").forEach(putAllTo.apply(EnumFacing.DOWN));
    }

    @BeforeAll
    public static void initBeforeSlopeTest() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        run(TestVanillaSlopeAlgorithm.class.getName(),"initFacingAliases");
    }

    @ParameterizedTest
    @MethodSource("pullDataForTestSlopeAlgorithm")
    public void testSingleSlopeAlgorithm(final @Nonnull SlopeAlgorithmTestData data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        test(new Object[]{网格工具.打包网格数据(data.$网格),data.beginPosRaw,data.expectedDirections});
    }

    public static class SlopeAlgorithmTestData extends SandboxTestCase{
        @TestArg(value = "begin_at",type = TestArg.Type.BLOCK_POS) int[] beginPosRaw;
        public final String[] expectedDirections;

        public SlopeAlgorithmTestData(final @Nonnull 格文件 $格文件,
                                      final @Nonnull String[] expectedDirections) {
            super($格文件);
            this.expectedDirections = expectedDirections;
        }
    }

    @Nonnull
    public static Stream<SlopeAlgorithmTestData> pullDataForTestSlopeAlgorithm(){
        final ArrayList<SlopeAlgorithmTestData> cases = new ArrayList<>();
        SandboxTestCase.findInputs("data/fluidphysics/finite/VanillaSlopeFlow/",(scan,in) -> {
            final @Nonnull 格文件 $输入 = 格文件.解析(in.getURI());
            final ArrayList<String> expected = new ArrayList<>();
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(SandboxTestCase.getCommonAnswerByInput(scan, in).open(), StandardCharsets.UTF_8))){
                String line;
                while ((line = reader.readLine()) != null) expected.add(StringUtil.strip(line));
            }
            cases.add(new SlopeAlgorithmTestData($输入,expected.toArray(new String[0])));
        });
        return cases.stream();
    }

    @Nonnull
    public static Set<EnumFacing> toExpectedFacings(final @Nonnull String[] expectedDirections){
        final @Nonnull Set<EnumFacing> directions = EnumSet.noneOf(EnumFacing.class);
        final @Nonnull BiConsumer<String,EnumFacing> add = (name,facing) ->{
            Assertions.assertFalse(directions.contains(facing),"Facing "+ facing+" ("+name+") "+" is duplicated!");
            directions.add(facing);
        };
        for(final @Nonnull String d : expectedDirections){
            if(d.isEmpty()) continue;
            final @Nonnull String D = d.toUpperCase(Locale.ROOT);
            try {
                add.accept(D,EnumFacing.valueOf(D));
            }catch (final @Nonnull Exception ignore){
                if(FACING_ALIASES.containsKey(D)){
                    add.accept(D,FACING_ALIASES.get(D));
                }else if(FACING_ALIASES.containsKey(new StringBuilder().appendCodePoint(D.codePointAt(0)).toString())){ //单汉字串
                    D.codePoints()
                            .filter(code -> !Character.isWhitespace(code))
                            .mapToObj(code -> new StringBuilder().appendCodePoint(code).toString())
                            .peek(c -> Assertions.assertTrue(FACING_ALIASES.containsKey(c),c +" isn't a valid direction!"))
                            .map(c -> Pair.of(c,FACING_ALIASES.get(c)))
                            .forEach(pair -> add.accept(pair.getLeft(),pair.getRight()));
                }else{
                    Assertions.fail("Unknown Direction "+D);
                }
            }
        }
        return directions;
    }

    @SuppressWarnings("unused")
    public static void testSingleSlopeAlgorithm_Inner(final @Nonnull Object[] raw,
                                                      final int[] beginPosRaw,
                                                      final @Nonnull String[] expectedDirections){
        final BlockPos beginPos = new BlockPos(beginPosRaw[0],beginPosRaw[1],beginPosRaw[2]);
        final @Nonnull MockSimpleSandbox sandbox = initWorldSandbox(raw,beginPos);
        final @Nonnull IBlockState beginState = world.getBlockState(beginPos);
        Assertions.assertEquals(7,beginState.getValue(BlockLiquid.LEVEL));
        final @Nonnull FiniteFlowingVanilla flowing = getFlowingByMaterial(beginState.getMaterial());
        final Set<EnumFacing> directions = EnumSet.noneOf(EnumFacing.class);
        final Set<EnumFacing> expected = toExpectedFacings(expectedDirections);

        flowing.singleSlopeAlgorithm(world,beginPos,directions);

        LOGGER.info("expected : {}",expected.stream().map(EnumFacing::toString).collect(Collectors.joining(",")));
        LOGGER.info("in fact: {}",directions.stream().map(EnumFacing::toString).collect(Collectors.joining(",")));
        Assertions.assertIterableEquals(expected,directions);
    }
}
