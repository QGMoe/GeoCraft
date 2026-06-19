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

package 清汩萌.造.空间;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import 清汩萌.造.工具.StringUtil;
import 清汩萌.造.映射.映射器;
import 清汩萌.造.管理.映射局;
import 清汩萌.造.管理.空间构造局;
import 清汩萌.造.词块.词块;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author QiguaiAAAA
 */
public final class 词块网格 {
    private @Nullable 空间构造器 $构造器;
    private @Nullable String $默认构造器名;
    private @Nullable Set<String> $默认映射器名集合;
    private final List<一层词块> $层列表 = new ArrayList<>();
    private 词块 $默认填充方块;
    private int $层数 = 0;
    private int $行数 = 0;
    private int $列数 = 0;

    @Nonnull
    public static 词块网格 从原始网格数据恢复(@Nonnull final List<List<List<String>>> raw){
        final @Nonnull 词块网格 $网格 = new 词块网格();
        $网格.$层列表.addAll(raw.stream()
                .map($原始单层 ->{
                    final 一层词块 $单层 = $网格.new 一层词块($原始单层);
                    $网格.$行数 = Math.max($单层.$行列表.size(),$网格.$行数);
                    $网格.$列数 = Math.max($单层.$最大列数, $网格.$列数);
                    return $单层;})
                .peek($单层 -> $单层.$最大列数 = $网格.$列数)
                .collect(Collectors.toList()));
        $网格.$层数 = $网格.$层列表.size();
        return $网格;
    }

    @Nonnull
    public 词块网格 基于(final @Nonnull 空间构造器 $构造器){
        this.$构造器 = $构造器;
        return this;
    }

    @Nonnull
    public 词块网格 基于(final @Nonnull String $构造器名){
        this.$默认构造器名 = $构造器名;
        return this;
    }

    @Nonnull
    public 词块网格 基于(final @Nonnull Set<String> $映射器名称集合){
        this.$默认映射器名集合 = $映射器名称集合;
        return this;
    }

    @Nonnull
    public 词块网格 默认用(final @Nonnull String $默认方块) {
        this.$默认填充方块 = 词块.of($默认方块);
        return this;
    }

    @Nonnull
    public 词块网格 默认用(final @Nonnull 词块 $词块) {
        this.$默认填充方块 = $词块;
        return this;
    }

    @Nonnull
    public 词块网格 默认用(final @Nonnull IBlockState state) {
        this.$默认填充方块 = $构造器.进行映射(state);
        return this;
    }

    @Nonnull
    public 一层词块 层() {
        return new 一层词块();
    }

    @Nonnull
    public 词块网格 期望(final int $数量,final @Nonnull 网格参数 $参数){
        switch ($参数){
            case 层:{
                this.$层数 = Math.max($层数,$数量);
                break;
            }case 行:{
                this.$行数 = Math.max($行数,$数量);
                break;
            }case 列:{
                this.$列数 = Math.max($列数,$数量);
                break;
            }
        }
        return this;
    }

    @Nonnull
    public 词块网格 略() {
        略(1);
        return this;
    }

    @Nonnull
    public 词块网格 略(final int $略去层数) {
        if ($略去层数 < 1) throw new IllegalArgumentException();
        for (int i = 0; i < $略去层数; i++) $层列表.add(new 一层词块());
        $层数 = Math.max($层数,$层列表.size());
        return this;
    }

    @Nonnull
    public IBlockState[][][] 构造() {
        加载默认构造器();
        return 构造(Objects.requireNonNull(this.$构造器));
    }

    @Nonnull
    public IBlockState[][][] 构造(@Nonnull final 空间构造器 $构造器) {
        return $构造器.构造(处理填充().stream().map($单层 -> $单层.$行列表).collect(Collectors.toList()));
    }

    private void 加载默认构造器(){
        if(this.$构造器 != null) return;
        if(this.$默认构造器名 != null){
            final ArrayList<映射器> $映射表 = new ArrayList<>(空间构造局.需要(new ResourceLocation(this.$默认构造器名)).获取映射表());
            if(this.$默认映射器名集合 != null) this.$默认映射器名集合.stream()
                    .map(ResourceLocation::new)
                    .map(映射局::需要)
                    .forEach($映射表::add);
            this.$构造器 = new 空间构造器();
            $映射表.forEach(this.$构造器::添加映射);
        }else if(this.$默认映射器名集合 != null){
            this.$构造器 = new 空间构造器();
            this.$默认映射器名集合.stream()
                    .map(ResourceLocation::new)
                    .map(映射局::需要)
                    .forEach(this.$构造器::添加映射);
        }
    }

    @Nonnull
    private List<一层词块> 处理填充(){
        final List<一层词块> $层列表复制 = $层列表.stream()
                .map(一层词块::new)
                .peek(一层词块::填充默认)
                .collect(Collectors.toList());
        while ($层列表复制.size()<$层数) $层列表复制.add(new 一层词块().填充默认());
        return $层列表复制;
    }

    @Override
    public String toString() {
        return 处理填充().stream().map(Object::toString).collect(Collectors.joining("\n\n"));
    }

    public int 获取层数() {
        return $层数;
    }

    public int 获取行数() {
        return $行数;
    }

    public int 获取列数() {
        return $列数;
    }

    public int[] 获取参数(){
        return new int[]{$层数,$行数,$列数};
    }

    @Nullable
    public 空间构造器 获取当前构造器(){
        if(this.$构造器 == null) 加载默认构造器();
        return this.$构造器;
    }

    @Nullable
    public String 获取默认构造器名称(){
        return this.$默认构造器名;
    }

    @Nullable
    public Set<String> 获取默认映射器名称集合(){
        return this.$默认映射器名集合;
    }

    public int 获取参数(final @Nonnull 网格参数 $参数){
        switch ($参数){
            case 层:return $层数;
            case 行:return $行数;
            case 列:return $列数;
            default:throw new IllegalArgumentException();
        }
    }

    @Nullable
    public 词块 获取默认填充方块(){
        return this.$默认填充方块;
    }

    @Nonnull
    public List<List<List<String>>> 获取原始网格数据(){
        return $层列表.stream()
                .map(一层词块::获取原始数据)
                .collect(Collectors.toList());
    }

    public final class 一层词块{
        private final List<List<词块>> $行列表;
        private int $最大列数 = 0;

        public 一层词块(){
            this.$行列表 = new ArrayList<>();
        }

        public 一层词块(final @Nonnull 一层词块 B){
            this.$行列表 = B.$行列表.stream().map(ArrayList::new).collect(Collectors.toList());
            this.$最大列数 = B.$最大列数;
        }

        public 一层词块(final @Nonnull List<List<String>> raw){
            this.$行列表 = raw.stream()
                    .map($原始单行 -> $原始单行.stream()
                            .map(词块::of)
                            .collect(Collectors.toList()))
                    .peek($单行 -> this.$最大列数 = Math.max(this.$最大列数,$单行.size()))
                    .collect(Collectors.toList());
        }

        @Nonnull
        public 一层词块 行(final @Nonnull String line) {
            return 行(line.codePoints());
        }

        @Nonnull
        public 一层词块 行(final @Nonnull IntStream line) {
            final @Nonnull List<词块> dat = 空间构造器.解析行(StringUtil.removeWhitesInCodePoints(line));
            this.$行列表.add(dat);
            $最大列数 = Math.max($最大列数, dat.size());
            return this;
        }

        @Nonnull
        public 词块网格 完成() {
            $层列表.add(this);
            $层数 = Math.max($层数,$层列表.size());
            $行数 = Math.max($行数, $行列表.size());
            $列数 = Math.max($列数, $最大列数);
            return 词块网格.this;
        }

        @Nonnull
        public List<List<String>> 获取原始数据(){
            return $行列表.stream()
                    .map($单行 -> $单行.stream()
                            .map(Objects::toString)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return new 一层词块(this)
                    .填充默认()
                    .$行列表.stream().map($单行 -> $单行.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining()))
                    .collect(Collectors.joining("\n"));
        }

        @Nonnull
        private 一层词块 填充默认(){
            $最大列数 = Math.max($列数,$最大列数);
            while ($行列表.size() < $行数) $行列表.add(new ArrayList<>());
            if($默认填充方块 == null) return this;
            for (final @Nonnull List<词块> $单行 : $行列表)
                while ($单行.size() < $列数) $单行.add($默认填充方块);

            return this;
        }
    }
}
