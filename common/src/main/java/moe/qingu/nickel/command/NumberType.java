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

package moe.qingu.nickel.command;

import moe.qingu.nickel.command.builder.parameter.num.ComparableNumberNodeBuilder;
import moe.qingu.nickel.command.builder.parameter.num.NumberNodeBuilder;
import moe.qingu.nickel.command.node.parameter.generic.number.BigDecimalNode;
import moe.qingu.nickel.command.node.parameter.generic.number.BigIntegerNode;
import moe.qingu.nickel.command.node.parameter.generic.number.DoubleNode;
import moe.qingu.nickel.command.node.parameter.generic.number.IntegerNode;
import moe.qingu.nickel.command.node.parameter.generic.number.LongNode;
import moe.qingu.nickel.command.node.parameter.generic.number.NumberNode;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * @author QiguaiAAAA
 */
public final class NumberType<N extends Number> {

    public static final NumberType<Double> DOUBLE = new NumberType<>(s -> new ComparableNumberNodeBuilder<>(s, DoubleNode::new));
    public static final NumberType<Long> LONG = new NumberType<>(s -> new ComparableNumberNodeBuilder<>(s, LongNode::new));
    public static final NumberType<Integer> INTEGER = new NumberType<>(s -> new ComparableNumberNodeBuilder<>(s, IntegerNode::new));
    public static final NumberType<BigInteger> BIG_INTEGER = new NumberType<>(s -> new ComparableNumberNodeBuilder<>(s, BigIntegerNode::new));
    public static final NumberType<BigDecimal> BIG_DECIMAL = new NumberType<>(s -> new ComparableNumberNodeBuilder<>(s, BigDecimalNode::new));

    private final Function<String, NumberNodeBuilder<N, NumberNode<N>>> factory;

    public NumberType(@Nonnull Function<String, NumberNodeBuilder<N,NumberNode<N>>> factory) {
        this.factory = factory;
    }

    @Nonnull
    public NumberNodeBuilder<N,NumberNode<N>> create(@Nonnull String name){
        return factory.apply(name);
    }
}
