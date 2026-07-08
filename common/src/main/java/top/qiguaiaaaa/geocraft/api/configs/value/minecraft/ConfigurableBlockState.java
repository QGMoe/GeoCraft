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

package top.qiguaiaaaa.geocraft.api.configs.value.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import top.qiguaiaaaa.geocraft.api.util.exception.ConfigParseError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个用于配置指定方块的方块属性包装类
 * @author QiguaiAAAA
 */
public class ConfigurableBlockState {
    private static final Pattern BLOCK_STATE_PATTERN = Pattern.compile("^([^\\[]+)\\[(.*)]$"); //命名空间:方块ID[方块状态或数据值]
    public final String location;
    public final int meta;
    public final Map<String,ConfigurableBlockProperty> properties;

    /**
     * @see #ConfigurableBlockState(String, int) 
     */
    public ConfigurableBlockState(@Nonnull ResourceLocation location, int meta) {
        this(location.toString(),meta);
    }

    /**
     * 表示指定数据值的方块<br/>
     * 若数据值为-1，则表示选中该方块的全部数据值状态
     * @param registryName 方块的注册名
     * @param meta 方块的数据值，应大于等于-1
     * @throws IllegalArgumentException 当数据值小于-1时抛出
     */
    public ConfigurableBlockState(@Nonnull String registryName, int meta) {
        if(meta <-1) throw new IllegalArgumentException("Meta couldn't be lower than -1! Block: "+registryName);
        this.location = registryName.toLowerCase().trim();
        this.meta = meta;
        this.properties = Collections.emptyMap();
    }

    /**
     * @see #ConfigurableBlockState(String, ConfigurableBlockProperty...) 
     */
    public ConfigurableBlockState(@Nonnull ResourceLocation location,@Nullable ConfigurableBlockProperty... properties){
        this(location.toString(),properties);
    }

    /**
     * 表示具有指定属性的指定方块的状态
     * @param registryName 方块注册名，例如minecraft:grass
     * @param properties 方块所具有的状态，若为空则表示选中该方块的全部状态。需要注意，若填写，则必须要包含该方块所具有的全部属性，否则无法匹配成功。
     */
    public ConfigurableBlockState(@Nonnull String registryName,@Nullable ConfigurableBlockProperty... properties){
        this.location = registryName.toLowerCase().trim();
        if (properties == null || properties.length == 0) {
            this.properties = Collections.emptyMap();
            this.meta = -1;
            return;
        }
        Map<String, ConfigurableBlockProperty> map = new TreeMap<>();

        for(ConfigurableBlockProperty property:properties){
            if(property == null) continue;
            map.put(property.name, property);
        }
        if(map.isEmpty()){
            this.meta = -1;
            this.properties = Collections.emptyMap();
        }else{
            this.meta = -2;
            this.properties = Collections.unmodifiableMap(map);
        }
    }

    /**
     * 从指定方块状态创建指定的方块状态配置包装
     * @param state 方块状态
     */
    public ConfigurableBlockState(@Nonnull IBlockState state){
        Block block = state.getBlock();
        ResourceLocation resourceLocation = block.getRegistryName();
        Map<String, ConfigurableBlockProperty> map = new TreeMap<>();
        if(resourceLocation == null) throw new NullPointerException();
        location = resourceLocation.toString();
        BlockStateContainer container = block.getBlockState();
        for(IProperty<?> property:container.getProperties()){
            Comparable<?> value = state.getValue(property);
            map.put(property.getName(),new ConfigurableBlockProperty(property,value));
        }
        if(map.isEmpty()){
            this.meta = -1;
            this.properties = Collections.emptyMap();
        }else{
            this.meta = -2;
            this.properties = Collections.unmodifiableMap(map);
        }
    }

    /**
     * 该方块状态配置是否与指定方块状态匹配。当该方块状态包含在该配置集合中，则匹配成功。<br/>
     * 例如，配置minecraft:grass[*]会匹配方块状态minecraft:grass[snow=true]，也会匹配minecraft:grass[snow=false]<br/>
     * 配置minecraft:grass[1]会匹配数据值为1的minecraft:grass，但不会匹配数据值为0的minecraft:grass<br/>
     * 配置minecraft:dirt[variant=dirt,snow=*]会匹配minecraft:dirt[variant=dirt,snow=true]和minecraft:dirt[variant=dirt,snow=false]，但不会匹配minecraft:dirt[variant=podzol,snow=false]以及其他的<br/>
     * 配置minecraft:dirt[variant=podzol,snow=false]仅会匹配方块状态minecraft:dirt[variant=podzol,snow=false]<br/>
     * 另外，若属性种类不对，也无法匹配。例如：
     * 配置minecraft:dirt[variant=dirt]无法匹配minecraft:dirt[variant=dirt,snow=false]<br/>
     * 配置minecraft:dirt[variant=dirt,facing=north]无法匹配minecraft:dirt[variant=dirt]
     * @param state 方块状态
     * @return 是否匹配成功
     */
    public boolean match(@Nonnull IBlockState state) {
        Block block = state.getBlock();
        ResourceLocation stateLoc = block.getRegistryName();
        if(stateLoc == null) return false;
        if(!stateLoc.toString().equalsIgnoreCase(location)) return false;
        if(meta == -1) return true;
        if(meta >-1) return block.getMetaFromState(state) == meta;

        BlockStateContainer container = block.getBlockState();
        for(IProperty<?> property:container.getProperties()){
            String name = property.getName();
            ConfigurableBlockProperty p = properties.get(name);
            if(p == null) return false;
            if(p.value.isEmpty()) return false;
            String val = state.getValue(property).toString();
            if(p.value.equalsIgnoreCase("*")) continue;
            if(!p.value.equalsIgnoreCase(val)) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurableBlockState)) return false;
        ConfigurableBlockState that = (ConfigurableBlockState) o;
        return Objects.equals(this.location, that.location)
                && this.meta == that.meta
                && Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, meta, properties);
    }

    /**
     * 将指定字符串反序列化为{@link ConfigurableBlockState}对象
     * @param content 字符串
     * @return 一个Configurable对象，若无法转换则返回null
     */
    @Nullable
    public static ConfigurableBlockState getInstanceByString(@Nullable String content) {
        if (content == null || content.trim().isEmpty()) return null;
        try {
            content = content.trim();

            // 匹配 "命名空间:方块id[xxx]"
            Matcher matcher = BLOCK_STATE_PATTERN.matcher(content);

            if (!matcher.matches()) return null;

            String registryName = matcher.group(1); // 命名空间+ID
            String inside = matcher.group(2);       // 方括号里的内容（可能为 null）

            if (inside == null || inside.isEmpty()) {
                // 没有方括号，默认匹配全部
                return new ConfigurableBlockState(registryName, -1);
            }

            if (inside.equals("*")) {
                return new ConfigurableBlockState(registryName, -1);
            }
            // 数据值模式
            if (inside.matches("-?\\d+")) {
                return new ConfigurableBlockState(registryName, Integer.parseInt(inside));
            }

            // property 模式
            String[] props = inside.split(",");
            ConfigurableBlockProperty[] properties = new ConfigurableBlockProperty[props.length];
            for (int i = 0; i < props.length; i++) {
                String[] kv = props[i].split("=", 2);
                if (kv.length == 2) {
                    properties[i] = new ConfigurableBlockProperty(kv[0].trim(), kv[1].trim());
                } else {
                    throw new ConfigParseError("property "+props[i]+" of "+registryName+" is not valid");
                }
            }
            return new ConfigurableBlockState(registryName, properties);

        } catch (Throwable t) {
            throw new ConfigParseError("Parsing blockstate config error:",t);
        }
    }

    @Nullable
    public static ConfigurableBlockState getFixedInstanceByString(@Nonnull String content){
        final ConfigurableBlockState instance = getInstanceByString(content);
        if(instance == null) return null;
        for(ConfigurableBlockProperty property:instance.properties.values()){
            if(property == null) continue;
            if(property.value.equals("*")) throw new ConfigParseError(content+" can't refer to a specific block state!");
        }
        if(instance.meta == -1) throw new ConfigParseError(content+" can't refer to a specific block state!");
        return instance;
    }

    @Nullable
    public Block getBlock(){
        return Block.REGISTRY.getObject(new ResourceLocation(location));
    }

    @Nullable
    public IBlockState getState(){
        Block block = getBlock();
        if(block == null) return null;
        if(meta == -1) return block.getDefaultState();
        if(meta >=0) return block.getStateFromMeta(meta);
        for(IBlockState state :block.getBlockState().getValidStates()){
            if(this.match(state)) return state;
        }
        return null;
    }

    /**
     * 将该实例序列化为字符串
     * @return 序列化后的字符串
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(location);
        if(meta == -1){
            builder.append("[*]");
            return builder.toString();
        }
        builder.append('[');
        if(meta >-1){
            builder.append(meta).append(']');
            return builder.toString();
        }
        boolean notFirst = false;
        for(ConfigurableBlockProperty property:properties.values()){
            if(notFirst) builder.append(',');
            builder.append(property.toString());
            notFirst = true;
        }
        builder.append(']');
        return builder.toString();
    }
}
