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

package moe.qingu.nickel.command.node;

import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.reader.InputReader;
import moe.qingu.nickel.command.utils.Claimer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 智能节点，可以实现智能分支。
 * @author QiguaiAAAA
 */
public interface ISmartNode extends ICommandNode{
    /**
     * 当前提供的参数是否与该节点匹配，如果匹配则会走当前节点。
     * @implSpec  不应当有副作用。
     * @param input 当前命令的输入。
     * @return 是否匹配。
     */
    boolean claims(@Nonnull final InputReader input);

    /**
     * 设置匹配器，用于自定义节点匹配。
     * @see #claims(InputReader)
     * @param checker 一个匹配器，是一个传入了{@link List<String>}和{@link CommandContext}并返回{@link Boolean}的函数，相当于一个{@link #claims(InputReader)}函数。
     * @throws UnsupportedOperationException 若不支持设置自定义的匹配器则抛出。
     */
    void setClaimer(@Nullable final Claimer checker);
}
