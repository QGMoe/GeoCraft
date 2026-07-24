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

package 清汩萌.造.测试;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import 清汩萌.造.格文件;
import 清汩萌.造.空间.网格参数;
import 清汩萌.造.空间.词块网格;
import 清汩萌.造.造;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author QGMoe
 */
public final class 测试格文件 {

    @ParameterizedTest
    @MethodSource("准备尺寸测试数据")
    @SuppressWarnings("unchecked")
    public void 尺寸测试(final @Nonnull File $原始格文件){
        final 格文件 $格文件 = 格文件.解析($原始格文件);
        final 词块网格 $网格 = $格文件.获取网格();
        final Map<String,Object> $扩展数据 = $格文件.获取附加数据();
        Assertions.assertNotNull($网格);
        Assertions.assertNotNull($扩展数据);

        造.LOGGER.info("解析的网格：\n{}",$网格);
        final List<Integer> expected = (List<Integer>) $扩展数据.get("expected");
        for(@Nonnull final 网格参数 $参数:网格参数.列表){
            Assertions.assertEquals(expected.get($参数.ordinal()),$网格.获取参数($参数),"参数："+$参数);
        }
    }

    @ParameterizedTest
    @MethodSource("准备非法格式测试数据")
    public void 非法格式测试(final @Nonnull File $原始格文件){
        Assertions.assertThrows(Exception.class,() -> 格文件.解析($原始格文件));
    }

    @Nonnull
    public static Stream<File> 准备尺寸测试数据(){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths("data/造/格文件/尺寸测试/").scan()){
            return scan.getResourcesWithExtension(格文件.扩展名).stream()
                    .map(in -> new File(in.getURI()));
        }
    }

    @Nonnull
    public static Stream<File> 准备非法格式测试数据(){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths("data/造/格文件/非法格式测试/").scan()){
            return scan.getResourcesWithExtension(格文件.扩展名).stream()
                    .map(in -> new File(in.getURI()));
        }
    }
}
