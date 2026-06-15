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

package 清汩萌.造;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.空间.空间构造器;
import 清汩萌.造.空间.网格参数;
import 清汩萌.造.空间.词块网格;
import 清汩萌.造.词块.词块;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author QiguaiAAAA
 */
public final class 格文件 {
    public static final String 扩展名 = "格";
    private static final Function<Object,String> STRIPE = o -> StringUtil.strip(o.toString());
    private static final int[] $关键字 = "略用".codePoints().toArray();
    private static final @Nonnull IntOpenHashSet $关键字集合 = new IntOpenHashSet($关键字,1.0f);

    private int $版本;
    private @Nullable Map<String,Object> $头部信息;
    private @Nullable String $名称;
    private 词块网格 $网格;
    private @Nullable Map<String,Object> $附加数据;

    static {
        $关键字集合.trim();
    }

    private 格文件(){};

    public static boolean 是关键字(final int codePoint){
        return $关键字集合.contains(codePoint);
    }

    @Nonnull
    public static Stream<格文件> 获取目录下所有格文件(final @Nonnull String path){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths(path).scan()){
            return scan.getResourcesWithExtension(格文件.扩展名).stream()
                    .map(in -> 解析(new File(in.getURI())));
        }
    }

    @Nonnull
    public static 格文件 解析(final @Nonnull URI uri){
        return 解析(new File(uri));
    }

    @Nonnull
    public static 格文件 解析(@Nonnull final File file){
        return 解析(file.toPath());
    }

    @Nonnull
    public static 格文件 解析(@Nonnull final Path path){
        final 格文件 res = new 格文件();
        res.$名称 = path.toString();
        try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            res.$头部信息 = 解析头部信息(reader);
            res.$网格 = 解析网格(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        res.初始化元数据();
        return res;
    }

    @Nullable
    public String 获取名称() {
        return $名称;
    }

    @Nonnull
    public 词块网格 获取网格(){
        return $网格;
    }

    @Nullable
    public Map<String, Object> 获取附加数据() {
        return $附加数据;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String,Object> 解析头部信息(final @Nonnull BufferedReader reader) throws IOException {
        final @Nullable String firstLine = reader.readLine();
        if(firstLine == null) return null;
        final boolean hasFrontmatter = "---".equals(StringUtil.strip(firstLine.split("#",2)[0]));

        if(hasFrontmatter){
            final @Nonnull StringBuilder builder = new StringBuilder();
            while (true){
                final String line = reader.readLine();
                if(line == null) throw new RuntimeException("不完整的头部信息");
                if("---".equals(StringUtil.strip(line.split("#",2)[0]))) break;
                builder.append(line).append('\n');
            }
            final @Nonnull Object o = 造.YAML.load(builder.toString());
            if(o instanceof Map) return (Map<String, Object>) o;
            else{
                造.LOGGER.fatal("解析 YAML 失败: {} 不是一个有效的映射表",builder.toString());
                throw new IllegalArgumentException();
            }
        }else return null;
    }

    @Nonnull
    private static 词块网格 解析网格(final @Nonnull BufferedReader reader) throws IOException {
        final @Nonnull 词块网格 $网格 = new 词块网格();
        String line;
        词块网格.一层词块 $当前层 = null;
        while (true){
            line = reader.readLine();
            if(line == null) break;
            line = StringUtil.strip(line.split("#",2)[0]);
            if("---".equals(line)) break;
            if(line.isEmpty()){
                if($当前层 != null){
                    $当前层.完成();
                    $当前层 = null;
                }
            }else if(line.startsWith("略")){
                if($当前层 != null){
                    $当前层.完成();
                    $当前层 = null;
                }
                line = line.replace("略","L");
                final @Nonnull List<词块> parsed = 空间构造器.解析行(StringUtil.removeWhitesInCodePoints(line.codePoints()));
                if(parsed.size() != 1) throw new IllegalArgumentException(line);
                final 词块 $略 = parsed.get(0);
                final int count = $略.获取下标().isEmpty()?1:Integer.parseInt($略.获取下标());
                $网格.略(count);

            }else {
                if($当前层 == null) $当前层 = $网格.层();
                $当前层.行(line);
            }
        }
        if($当前层 != null) $当前层.完成();
        return $网格;
    }

    @SuppressWarnings("unchecked")
    private void 初始化元数据(){
        if($头部信息 != null) {
            $版本 = Optional.ofNullable($头部信息.get("ver"))
                    .map(o -> Integer.parseInt(o.toString()))
                    .filter(ver -> ver > 0)
                    .orElseThrow(IllegalArgumentException::new);
            $名称 = computeIfNonNull(Optional.ofNullable(
                    Optional.ofNullable($头部信息.get("name")).orElse(Optional.ofNullable($头部信息.get("名")).orElse($名称))).map(STRIPE), STRIPE);
            Optional.ofNullable(Optional.ofNullable($头部信息.get("default")).orElse($头部信息.get("默认用")))
                    .ifPresent(o -> $网格.默认用(STRIPE.apply(o)));

            @Nullable final List<Integer> $大小 = (List<Integer>) Optional.ofNullable($头部信息.get("size")).filter(o -> o instanceof List).orElse($头部信息.get("期望"));
            if($大小 != null)
                for(@Nonnull final 网格参数 $参数:网格参数.列表)
                    $网格.期望($大小.get($参数.ordinal()),$参数);
            $附加数据 = (Map<String,Object>) $头部信息.get("ext");
        }else{
            throw new RuntimeException(); //TODO
        }
    }

    private static <T> T computeIfNonNull(final @Nullable Object obj, final @Nonnull Function<Object,T> mapping){
        return obj == null?null:mapping.apply(obj);
    }
}
