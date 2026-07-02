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

package moe.qingu.nickel.command.builder.parameter.minecraft;

import moe.qingu.nickel.command.builder.parameter.ParameterNodeBuilder;
import moe.qingu.nickel.command.node.parameter.minecraft.BlockPosNode;
import moe.qingu.nickel.command.node.parameter.minecraft.MinecraftVec3Node;
import moe.qingu.nickel.command.node.parameter.minecraft.Vec3dNode;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public abstract class MinecraftVec3NodeBuilder<T,N extends MinecraftVec3Node<T>> extends ParameterNodeBuilder<T,N, MinecraftVec3NodeBuilder<T,N>> {
    protected boolean doCenter = false;

    public MinecraftVec3NodeBuilder(@Nonnull final String name) {
        super(name);
    }

    public MinecraftVec3NodeBuilder(@Nonnull final String parentName,@Nonnull final String childName){
        super(parentName,childName);
    }

    @Nonnull
    public MinecraftVec3NodeBuilder<T,N> center(final boolean doCenterBlock){
        this.doCenter = doCenterBlock;
        return this;
    }

    @Nonnull
    @Override
    public N build() {
        final N node = super.build();
        node.setDoCenterBlock(doCenter);
        return node;
    }

    public static class BlockPos extends MinecraftVec3NodeBuilder<net.minecraft.util.math.BlockPos, BlockPosNode>{

        public BlockPos(@Nonnull final String name) {
            super(name);
        }

        public BlockPos(@Nonnull final String parentName,@Nonnull final String childName){
            super(parentName, childName);
        }

        @Nonnull
        @Override
        protected BlockPosNode buildInstance() {
            return new BlockPosNode(name);
        }
    }

    public static class Vec3d extends MinecraftVec3NodeBuilder<net.minecraft.util.math.Vec3d, Vec3dNode>{

        public Vec3d(@Nonnull final String name) {
            super(name);
        }

        public Vec3d(@Nonnull final String parentName,@Nonnull final String childName){
            super(parentName,childName);
        }

        @Nonnull
        @Override
        protected Vec3dNode buildInstance() {
            return new Vec3dNode(name);
        }
    }

}
