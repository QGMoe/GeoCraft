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

package top.qiguaiaaaa.geocraft_test;

import net.minecraft.init.Bootstrap;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.GameData;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import top.qiguaiaaaa.geocraft_test.assets.MockBlocks;
import top.qiguaiaaaa.geocraft_test.tests.TestMixinEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * @author QiguaiAAAA
 */
@ExtendWith(GeoCraftTest.SetupGeoTestExtension.class)
public class GeoCraftTest {
    public static final String MODID = "test";
    private static byte stage = Stage.NO_INIT;
    public static final Logger LOGGER = LogManager.getLogger("GeoTest");

    public static int getLoadStage(){
        return stage;
    }

    @SuppressWarnings("unchecked")
    public static void preInit()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(stage != Stage.NO_INIT) return;
        stage = Stage.PRE_INIT;
        LOGGER.info("Pre Initialisation begin");

        // 加载可以进行 ASM 操作的 ClassLoader
        LOGGER.info("Initialising Class Loader");
        final URLClassLoader loader = (URLClassLoader) GeoCraftTest.class.getClassLoader();
        Launch.classLoader = new LaunchClassLoader(loader.getURLs());
        Launch.blackboard = new HashMap<>();
        Thread.currentThread().setContextClassLoader(Launch.classLoader);

        //加载 Transformer, Patch 原版和 Forge 以提供测试环境
        LOGGER.info("Initialising Transformers");
        Launch.classLoader.registerTransformer("top.qiguaiaaaa.geocraft_test.asm.TestEnvTransformer");

        LOGGER.info("Initialising Mixins");
        System.setProperty("mixin.env.remapRefMap", "true"); //开发环境映射
        System.setProperty("mixin.service", "top.qiguaiaaaa.geocraft_test.asm.mixin.MixinServiceTestEnv");
        MixinBootstrap.init();

        GeoCraftTest.LOGGER.info("Current Registered transformers:");
        Launch.classLoader.getTransformers().forEach(transform -> GeoCraftTest.LOGGER.info(transform.getClass().getName()));

        Mixins.addConfiguration("mixin/mixins.geocraft.test.mixin-test.json");

        LOGGER.info("Transforming to LaunchClassLoader");
        final @Nonnull Class<GeoCraftTest> self = (Class<GeoCraftTest>) Launch.classLoader.loadClass("top.qiguaiaaaa.geocraft_test.GeoCraftTest");
        self.getMethod("init").invoke(null);
        stage = Stage.TEST_AVAILABLE;
        LOGGER.info("Test Environment Initialised on JUnit Environment");
    }

    public static void init() {
        stage = Stage.INIT;
        LOGGER.info("Initialisation begin");

        LOGGER.info("Testing Transformers");
        final @Nullable ResourceLocation testObj = GameData.checkPrefix("minecraft:test",false);
        Assertions.assertNull(testObj,"Inject Failed!");

        // TODO: Make Mixin Work
//        LOGGER.info("Testing Mixins");
//        final int numberA = 10;
//        final int numberB = 35;
//        Assertions.assertEquals(numberA-numberB, TestMixinEnvironment.add(numberA,numberB), "Mixin Environment Initialisation Failed");

        stage = Stage.POST_INIT;
        LOGGER.info("Post Initialisation begin");

        LOGGER.info("Patching Sound Event");
        SoundEvent.registerSounds();

        LOGGER.info("Patching Bootstrap");
        if(!Bootstrap.isRegistered()) {
            try {
                final @Nonnull Field field = Bootstrap.class.getDeclaredField("alreadyRegistered");
                field.setAccessible(true);
                field.setBoolean(null,true);
            }catch (final @Nonnull NoSuchFieldException | IllegalAccessException e){
                Assertions.fail(e);
            }
        }

        LOGGER.info("Initialising Minecraft Blocks");
        MockBlocks.initMinecraftBlocks();

        stage = Stage.INITED;
        LOGGER.info("Test Environment Initialised On LaunchClassLoader");
    }

    public static void run(final @Nonnull String testEntryClass, final @Nonnull String testEntryPoint)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        run(testEntryClass,testEntryPoint,Pair.of(new Class<?>[]{},new Object[]{}));
    }

    public static void run(final @Nonnull String testEntryClass, final @Nonnull String testEntryPoint,final @Nonnull Pair<Class<?>[],Object[]> data)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Assertions.assertEquals(Stage.TEST_AVAILABLE,stage);
        final @Nonnull Class<?> entryCls = Launch.classLoader.loadClass(testEntryClass);
        final @Nonnull Method entryPoint = entryCls.getMethod(testEntryPoint,data.getKey());
        entryPoint.invoke(null,data.getValue());
    }

    public static final class SetupGeoTestExtension implements BeforeAllCallback{

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            preInit();
        }
    }

    public void test(final @Nonnull String methodName) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        run(this.getClass().getName(),methodName);
    }

    public void test() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        run(this.getClass().getName(),Thread.currentThread().getStackTrace()[2].getMethodName()+"_Inner");
    }

    public void test(final @Nonnull Pair<Class<?>[],Object[]> data) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        run(this.getClass().getName(),Thread.currentThread().getStackTrace()[2].getMethodName()+"_Inner",data);
    }

    public static final class Stage{
        public static final byte NO_INIT = 0;
        public static final byte PRE_INIT = 1;
        public static final byte INIT = 2;
        public static final byte POST_INIT = 3;
        public static final byte INITED = 4;
        public static final byte TEST_AVAILABLE = 5;
        private Stage(){}
    }
}
