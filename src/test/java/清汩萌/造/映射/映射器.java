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

package 清汩萌.造.映射;

import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import 清汩萌.造.词块.主体工具;
import 清汩萌.造.词块.词块;
import 清汩萌.造.造;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author QiguaiAAAA
 */
public final class 映射器 {
    private final ResourceLocation $名称;
    private final HashBiMap<词块, IBlockState> $标准映射表 = HashBiMap.create();
    private final Int2ObjectOpenHashMap<词块解析器> $单字别名解析表 = new Int2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String,词块解析器> $多字别名解析表 = new Object2ObjectOpenHashMap<>();

    public 映射器(final @Nonnull String $名称) {
        this(new ResourceLocation($名称));
    }

    public 映射器(final @Nonnull ResourceLocation $名称) {
        this.$名称 = $名称;
    }

    @Nonnull
    public 映射器 设定标准映射(@Nonnull final 词块 $词块,@Nonnull final IBlockState state){
        if($标准映射表.containsKey($词块)) throw new IllegalArgumentException($词块 + " 已经被映射到 "+ $标准映射表.get($词块));
        else if($标准映射表.containsValue(state)) throw new IllegalArgumentException(state + " 已经被映射到 "+ $标准映射表.inverse().get(state));
        $标准映射表.put($词块,state);
        return this;
    }

    @Nonnull
    public 映射器 设定解析器(@Nonnull final String $主体,@Nonnull final 词块解析器 $解析器){
        主体工具.需要合法主体($主体);
        final int firstCP = $主体.codePointAt(0);
        if(Character.charCount(firstCP) == $主体.length()){
            设定解析器(firstCP,$解析器);
        }else {
            if($多字别名解析表.containsKey($主体)) throw new IllegalArgumentException($主体 + " 已经拥有了一个多字解析器");
            $多字别名解析表.put($主体,$解析器);
        }
        return this;
    }

    @Nonnull
    public 映射器 设定解析器(final int $主体,@Nonnull final 词块解析器 $解析器){
        主体工具.需要合法主体($主体);
        if($单字别名解析表.containsKey($主体)) throw new IllegalArgumentException($主体 + " 已经拥有了一个单字解析器");
        $单字别名解析表.put($主体,$解析器);
        return this;
    }

    @Nonnull
    public 映射器 导入映射数据(final @Nonnull Class<?> cls){
        final @Nonnull Field[] fields = cls.getDeclaredFields();

        for(@Nonnull final Field field:fields){
            if((field.getModifiers() & Modifier.STATIC) == 0) continue;
            if(field.isAnnotationPresent(映射.忽略.class)) continue;

            final @Nonnull Class<?> type = field.getType();
            if(IBlockState.class.isAssignableFrom(type)){
                if(field.isAnnotationPresent(映射.别名.class)) 导入别名映射(field);
                else 导入标准映射(field);
            }
            else if (词块解析器.class.isAssignableFrom(type)) 导入映射器(field);
        }
        return this;
    }

    private void 导入标准映射(final @Nonnull Field field){
        field.setAccessible(true);
        try {
            final IBlockState state = (IBlockState) field.get(null);
            final String $原始词块 = field.getName();
            Assertions.assertNotNull(state);

            final 词块 $词块 = 词块.of($原始词块);
            造.LOGGER.info("{} 从 {} 中映射了 {} <-> {}",$名称,field.getDeclaringClass().getName(),$词块,state);
            设定标准映射($词块,state);
        } catch (final IllegalAccessException e) {
            Assertions.fail(e);
        }
    }

    private void 导入别名映射(final @Nonnull Field field){
        field.setAccessible(true);
        try {
            final IBlockState state = (IBlockState) field.get(null);
            final String $原始词块 = field.getName();
            Assertions.assertNotNull(state);

            final 词块 $词块 = 词块.of($原始词块);
            final @Nonnull 词块解析器 $解析器 = $词块.是多字主体()? $多字别名解析表.computeIfAbsent($词块.获取多字主体(),k -> new 别名映射解析器()):
                    $单字别名解析表.computeIfAbsent($词块.获取单字主体(),k -> new 别名映射解析器());
            if($解析器 instanceof 别名映射解析器){
                造.LOGGER.info("{} 从 {} 中映射了 {} -> {}",$名称,field.getDeclaringClass().getName(),$词块,state);
                ((别名映射解析器)$解析器).映射别名($词块,state);
            }else throw new IllegalArgumentException($词块.获取主体() + " 已经拥有了一个自定义的词块解析器");
        } catch (final IllegalAccessException e) {
            Assertions.fail(e);
        }
    }

    private void 导入映射器(final @Nonnull Field field){
        field.setAccessible(true);
        try {
            final 词块解析器 $解析器 = (词块解析器) field.get(null);
            if($解析器 == null){
                造.LOGGER.warn("{} 在解析 {} 时遇到了取值为 NULL 的解析器 {}",$名称,field.getDeclaringClass().getName(),field.getName());
            }else if(field.isAnnotationPresent(映射.用于.class)){
                final 映射.用于 $目标 = field.getAnnotation(映射.用于.class);
                final String[] targets = $目标.value();
                for(final String t:targets) 设定解析器(主体工具.需要合法主体(t),$解析器);
            }else{
                造.LOGGER.error("{} 在解析 {} 时遇到了未设定目标的解析器 {}",$名称,field.getDeclaringClass().getName(),field.getName());
            }
        } catch (final IllegalAccessException e) {
            Assertions.fail(e);
        }
    }

    @Nonnull
    public IBlockState 进行映射(final @Nonnull 词块 $词块){
        final @Nullable IBlockState res = $标准映射表.get($词块);
        if(res != null) return res;
        final @Nullable 词块解析器 $解析器 = $词块.是多字主体()?$多字别名解析表.get($词块.获取多字主体()):$单字别名解析表.get($词块.获取单字主体());
        if($解析器 != null) return $解析器.解析($词块);
        throw new IllegalArgumentException($名称+ " 无法解析 " + $词块);
    }

    @Nonnull
    public 词块 进行映射(final @Nonnull IBlockState state){
        final @Nullable 词块 res = $标准映射表.inverse().get(state);
        if(res != null) return res;
        throw new IllegalArgumentException($名称+ " 无法解析 " + state);
    }

    @Nonnull
    public 词块解析器 获取解析器(@Nonnull final 词块 $词块){
        return $词块.是多字主体()?获取解析器($词块.获取多字主体()):获取解析器($词块.获取单字主体());
    }

    @Nonnull
    public 词块解析器 获取解析器(@Nonnull final String $主体){
        主体工具.需要合法主体($主体);
        final int firstCP = $主体.codePointAt(0);
        if(Character.charCount(firstCP) == $主体.length()){
            return 获取解析器(firstCP);
        }else {
            if(!$多字别名解析表.containsKey($主体)) throw new IllegalArgumentException($主体 + " 没有一个多字解析器");
            return $多字别名解析表.get($主体);
        }
    }

    @Nonnull
    public 词块解析器 获取解析器(final int $主体){
        主体工具.需要合法主体($主体);
        if(!$单字别名解析表.containsKey($主体)) throw new IllegalArgumentException(new String(new int[]{$主体},0,1) + " 没有一个单字解析器");
        return $单字别名解析表.get($主体);
    }

    @Nonnull
    public ResourceLocation 获取名称(){
        return $名称;
    }
}
