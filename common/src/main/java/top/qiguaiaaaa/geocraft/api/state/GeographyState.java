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

package top.qiguaiaaaa.geocraft.api.state;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.INBTSerializable;
import top.qiguaiaaaa.geocraft.api.atmosphere.layer.Layer;
import top.qiguaiaaaa.geocraft.api.property.IGeographyProperty;

import javax.annotation.Nonnull;

/**
 * 地理状态
 * @author QiguaiAAAA
 */
public interface GeographyState extends INBTSerializable<NBTBase> {
    /**
     * 初始化状态
     * @param layer 层级
     */
    default void load(@Nonnull Layer layer){}

    /**
     * 该状态是否需要被保存
     * @return 若为true,则大气状态会被保存到大气nbt当中
     */
    default boolean canSerialize(){return true;}

    /**
     * 该状态是否需要从NBT中加载
     * @return 若为true,则大气状态会被提供相应的nbt数据
     */
    default boolean canDeserialize(){return true;}

    /**
     * 该状态是否已经初始化完成
     * @return 若完成,则返回true
     */
    boolean isLoaded();

    /**
     * 获得该状态对应的属性
     * @return 大气属性
     */
    @Nonnull
    IGeographyProperty getProperty();
    @Deprecated
    @Nonnull
    String getNBTTagKey();
}
