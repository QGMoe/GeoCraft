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

package top.qiguaiaaaa.geocraft_test.world.sandbox;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.state.IBlockState;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * 一个 builder，可以配置中文字符环境，并将传入的中文字符序列转换为 IBlockState[y][z][x]
 * @author QiguaiAAAA
 */
public class MockSandboxEnvBuilder<S extends MockSandboxEnvBuilder<S>> {

    protected final BiMap<String, IBlockState> characterToState = HashBiMap.create();

    protected MockSandboxEnvBuilder(){}

    @Nonnull
    public static MockSandboxEnvBuilder.Impl create(){
        return new Impl();
    }

    /**
     * 构造一个结构层字符串。
     * <p>
     * 注意：
     * 第一个参数仅用于避免 IntelliJ 的参数提示 `rows:` 影响代码排版，
     * 实际结构内容从第二个参数开始。
     * <p>
     * 示例：
     * layer(
     *     "",
     *     "石石石",
     *     "石〇石",
     *     "石石石"
     * )
     */
    @Nonnull
    public static String layer(@Nonnull final String... rows) {
        return String.join("\n", Arrays.copyOfRange(rows, 1, rows.length));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S withStateData(final @Nonnull Class<?> stateDataCls){
        final @Nonnull Field[] fields = stateDataCls.getFields();

        for(@Nonnull final Field field:fields){
            if((field.getModifiers() & Modifier.STATIC) == 0) continue;
            if(IBlockState.class.isAssignableFrom(field.getDeclaringClass())){
                field.setAccessible(true);
                try {
                    final IBlockState state = (IBlockState) field.get(null);
                    final String name = field.getName();
                    Assertions.assertNotNull(state);
                    Assertions.assertEquals(name.length(), Character.charCount(name.codePointAt(0))); //保证只有一个中文字符
                    characterToState.put(name,state);
                } catch (final IllegalAccessException e) {
                    Assertions.fail(e);
                }
            }
        }

        return (S) this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public S withStateData(final @Nonnull MockSandboxEnvBuilder<?> anotherBuilder){
        this.characterToState.putAll(anotherBuilder.characterToState);
        return (S) this;
    }

    /**
     * 从汉字组成的字符串描述生成一个基本的沙盒结构
     * @param size 正方形边长
     * @param chars 由汉字描述的结构，从低到高，其边长必须是 size 的大小
     * @return 转换后的 IBlockState，索引为 Y,Z,X
     */
    @Nonnull
    public IBlockState[][][] generateFromCharacters(final int size,final @Nonnull String[] chars){
        final IBlockState[][][] sandbox = new IBlockState[chars.length][][];
        for(int y=0;y<chars.length;y++){
            sandbox[y] = new IBlockState[size][];
            int i = 0;
            for(int z=0;z<size;z++){
                sandbox[y][z] = new IBlockState[size];
                for(int x=0;x<size;x++){
                    final int code = chars[y].codePointAt(i);
                    final int charCount = Character.charCount(code);
                    final String character = chars[y].substring(i,i+charCount);
                    final IBlockState state = getBlockStateByName(character);
                    sandbox[y][z][x] = state;
                    i += charCount;
                }
                if(z == size-1) break;
                Assertions.assertTrue(z<size-1);
                Assertions.assertEquals('\n', chars[y].charAt(i));
                i++;
            }
        }
        return sandbox;
    }

    @Nonnull
    public IBlockState getBlockStateByName(final @Nonnull String name){
        return characterToState.computeIfAbsent(name, k -> Assertions.fail(k + " isn't a block!"));
    }

    public static final class Impl extends MockSandboxEnvBuilder<Impl> {
        private Impl(){}
    }
}
