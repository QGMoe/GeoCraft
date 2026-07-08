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

package moe.qingu.nickel.command.node.parameter.minecraft;

import com.google.common.collect.Lists;
import moe.qingu.nickel.command.suggestor.TokenizeSuggestor;
import moe.qingu.nickel.command.suggestor.Suggestor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.BlockPos;
import moe.qingu.nickel.command.context.CommandContext;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public class BlockPosNode extends MinecraftVec3Node<BlockPos> {
    public static final DefaultParser<BlockPos> DEFAULT_PARSER = (node, context) -> context.getSender().getPosition();

    public static final Suggestor<BlockPos> DEFAULT_SUGGESTOR = TokenizeSuggestor.of(3,(args, context) -> {
        final List<String> suggests = Lists.newArrayList("~");
        final BlockPos pos = context.getTargetPos()==null?context.getPosition():context.getTargetPos();
        switch (args.length){
            case 1:
                suggests.add(String.valueOf(pos.getX()));
                break;
            case 2:
                suggests.add(String.valueOf(pos.getY()));
                break;
            case 3:
                suggests.add(String.valueOf(pos.getZ()));
                break;
            default: return Collections.emptyList();
        }
        return suggests;
    });

    public BlockPosNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
        setClaimer(Vec3dNode.DEFAULT_CLAIMER);
    }

    @Override
    public BlockPos parse(@Nonnull final String[] args, @Nonnull final CommandContext context) throws NumberInvalidException {
        return CommandBase.parseBlockPos(context.getSender(),args, 0,doCenterBlock);
    }

    @Override
    public int getTokenCount() {
        return 3;
    }

    @Nonnull
    @Override
    public Class<BlockPos> getTypeClass() {
        return BlockPos.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.block_pos";
    }

    @Override
    public String serialise(@Nonnull final BlockPos pos) {
        return String.format("%s %s %s", pos.getX(),pos.getY(),pos.getZ());
    }
}
