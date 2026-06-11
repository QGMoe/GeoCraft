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

package top.qiguaiaaaa.geocraft.command;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import moe.qingu.nickel.command.builder.parameter.FastParameterNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.StringNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.minecraft.MinecraftVec3NodeBuilder;
import moe.qingu.nickel.command.builder.parameter.num.NumberNodeBuilder;
import moe.qingu.nickel.command.node.parameter.generic.number.NumberNode;
import moe.qingu.nickel.command.node.parameter.minecraft.BlockPosNode;
import moe.qingu.nickel.command.node.parameter.minecraft.DimensionNode;

import javax.annotation.Nonnull;

import static moe.qingu.nickel.command.Nodes.*;

/**
 * @author QiguaiAAAA
 */
public final class GeoArguments {

    public static final String POS = "pos";
    public static final String VALUE = "value";
    public static final String PROPERTY = "property";
    public static final String WORLD = "world";

    public static final String DOIT = "doit";

    private GeoArguments(){}

    @Nonnull
    public static MinecraftVec3NodeBuilder<BlockPos, BlockPosNode> pos(){
        return blockPos(POS)
                .asOptional()
                .translate("geocraft.command.common.arg.pos");
    }

    @Nonnull
    public static NumberNodeBuilder<Double, NumberNode<Double>> value(){
        return doubleArg(VALUE)
                .translate("geocraft.command.common.arg.value");
    }

    @Nonnull
    public static StringNodeBuilder property(){
        return string(PROPERTY)
                .translate("geocraft.command.common.arg.property");
    }

    @Nonnull
    public static FastParameterNodeBuilder.FastSmart<World, DimensionNode> world(){
        return dimension(WORLD)
                .asOptional()
                .translate("geocraft.command.common.arg.world");
    }

    @Nonnull
    public static StringNodeBuilder doit(){
        return string(DOIT)
                .asOptional()
                .defaultAs("")
                .translate("geocraft.command.common.arg.doit")
                .comment("geocraft.command.common.comment.doit")
                .allow("do");
    }
}
