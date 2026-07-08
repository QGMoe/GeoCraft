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

package 清汩萌.天圆地方.world.sandbox;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import 清汩萌.天圆地方.util.IOBiConsumer;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.词块网格;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author QiguaiAAAA
 */
public class SandboxTestCase {
    public static final String INPUT_FILE_EXT = ".in.格";
    public static final String ANSWER_FILE_EXT = ".ans.格";
    public static final String COMMON_ANSWER_FILE_EXT = "ans";

    public final String name;
    public final @Nonnull 格文件 $格文件;
    public final @Nonnull 词块网格 $网格;

    public SandboxTestCase(final @Nonnull 格文件 $格文件) {
        this.$格文件 = $格文件;
        this.name = $格文件.获取名称();
        this.$网格 = $格文件.获取网格();
        final Map<String,Object> ext = $格文件.获取附加数据();
        Assertions.assertNotNull(ext);
        process(this);
    }

    public static <T> Stream<T> findInputs(final @Nonnull String dataDir,
                                                                   final @Nonnull Function<格文件,T> supplier){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths(dataDir).scan()){
            final ArrayList<T> cases = new ArrayList<>();
            findInputs(scan,(ignored,in)-> cases.add(supplier.apply(格文件.解析(in.getURI()))));
            return cases.stream();
        }
    }

    public static void findInputs(final @Nonnull String dataDir, final @Nonnull IOBiConsumer<ScanResult, Resource> forEachInput){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths(dataDir).scan()){
            findInputs(scan,forEachInput);
        }
    }

    public static void findInputs(final @Nonnull ScanResult scan, final @Nonnull IOBiConsumer<ScanResult, Resource> forEachInput){
        scan.getResourcesWithExtension(格文件.扩展名)
                .filter(r -> r.getPath().endsWith(INPUT_FILE_EXT))
                .forEach(in -> {
                    try {
                        forEachInput.accept(scan,in);
                    } catch (final @Nonnull Exception e) {
                        Assertions.fail("error occurred when processing "+in.getPath(),e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static <C extends SandboxTestCase> C process(final @Nonnull C c){
        final Map<String,Object> ext = c.$格文件.获取附加数据();
        Assertions.assertNotNull(ext);
        final Field[] fields = c.getClass().getDeclaredFields();
        for(final @Nonnull Field field:fields){
            final int modifiers = field.getModifiers();
            if(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;
            if(!field.isAnnotationPresent(TestArg.class)) continue;
            final @Nonnull TestArg arg = field.getAnnotation(TestArg.class);
            field.setAccessible(true);
            final String name = arg.value().isEmpty()?field.getName():arg.value();
            final @Nullable Optional<Map<String,Object>> parent = arg.in().isEmpty()?Optional.of(ext):
                    Optional.of(ext.get(arg.in())).filter(o -> o instanceof Map).map(o -> (Map<String, Object>) o);
            try {
                arg.type().parse(c,parent,name,field);
            } catch (final @Nonnull IllegalAccessException e) {
                Assertions.fail(e);
            }
        }
        return c;
    }

    @Nonnull
    public static Resource getAnswerByInput(final @Nonnull ScanResult scan,final @Nonnull Resource in){
        final String outPath = in.getPath().replaceAll("\\.in\\.格$", ".ans.格");
        return scan.getResourcesWithPath(outPath).get(0);
    }

    @Nonnull
    public static Resource getCommonAnswerByInput(final @Nonnull ScanResult scan,final @Nonnull Resource in){
        final String outPath = in.getPath().replaceAll("\\.in\\.格$", "."+COMMON_ANSWER_FILE_EXT);
        return scan.getResourcesWithPath(outPath).get(0);
    }

    @Override
    public String toString() {
        return name;
    }
}
