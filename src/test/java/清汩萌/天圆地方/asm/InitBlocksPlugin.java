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

package 清汩萌.天圆地方.asm;

import git.jbredwards.fluidlogged_api.api.asm.IASMPlugin;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.tree.ClassNode;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.assets.MockBlocks;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QiguaiAAAA
 */
public class InitBlocksPlugin implements IASMPlugin {
    public static final InitBlocksPlugin PLUGIN = new InitBlocksPlugin();

    private static final Map<String, Block> OVERRIDES = new HashMap<>();

    private static boolean isTransformed = false;

    private InitBlocksPlugin(){}

    public static boolean registerOverride(final @Nonnull String fieldName,final @Nonnull Block instance){
        OVERRIDES.put(fieldName,instance);
        return !isTransformed;
    }

    @Override
    public boolean transformClass(@Nonnull final ClassNode classNode, final boolean obfuscated) {
        /*
        重写 Blocks 映射规则
         */
        overrideMethod(classNode,method -> "<clinit>".equals(method.name),"initBlocks", "()V",g -> {});
        return false;
    }

    @SuppressWarnings("unused")
    public static final class Hooks{
        public static void initBlocks(){
            try {
                final @Nonnull Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                for(@Nonnull final Map.Entry<String,Block> entry:OVERRIDES.entrySet()){
                    final @Nonnull Field field = Blocks.class.getDeclaredField(entry.getKey());
                    modifiersField.setInt(field,field.getModifiers() & ~Modifier.FINAL);
                    field.set(null, entry.getValue());
                    天圆地方测试.LOGGER.info("Loading Override Block {} Into Minecraft Blocks",entry.getKey());
                }
                isTransformed = true;
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                Assertions.fail(e);
            }

            Assertions.assertSame(Blocks.AIR, MockBlocks.Bases.AIR);
            天圆地方测试.LOGGER.info("Initialising Minecraft Init Blocks in Test Env!");
        }
    }
}
