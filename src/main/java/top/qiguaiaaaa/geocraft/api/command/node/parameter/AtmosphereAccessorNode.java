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

package top.qiguaiaaaa.geocraft.api.command.node.parameter;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.qiguaiaaaa.geocraft.api.atmosphere.AtmosphereSystemManager;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.context.SuggestContext;
import moe.qingu.nickel.command.node.parameter.minecraft.MinecraftVec3Node;
import moe.qingu.nickel.command.node.parameter.minecraft.Vec3dNode;
import moe.qingu.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class AtmosphereAccessorNode extends MinecraftVec3Node<IAtmosphereAccessor> {
    public static final DefaultParser<IAtmosphereAccessor> DEFAULT_PARSER = (parameterNode, context) -> {
        final BlockPos pos = context.getSender().getPosition();
        final boolean notAir;
        final World world = context.getWorld();
        if(!world.isBlockLoaded(pos)) throw new CommandException("geocraft.command.chunk_error.unloaded",pos.getX(),pos.getZ());
        final IBlockState state = world.getBlockState(pos);
        notAir = !state.getBlock().isAir(state,world,pos);
        IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(context.getWorld(),pos,notAir);
        if(accessor == null) throw new CommandException("geocraft.command.atmosphere.nonexistent",pos);
        return accessor;
    };

    public static final BiFunction<List<String>,SuggestContext,List<String>> DEFAULT_SUGGESTOR = (args,context) -> {
        final List<String> suggests = args.size()<=3?Lists.newArrayList("~"):Lists.newArrayList("default","false","true");
        final BlockPos pos = context.getTargetPos()==null?context.getPosition():context.getTargetPos();
        switch (args.size()){
            case 1:
                suggests.add(String.valueOf(pos.getX()));
                break;
            case 2:
                suggests.add(String.valueOf(pos.getY()));
                break;
            case 3:
                suggests.add(String.valueOf(pos.getZ()));
                break;
            case 4:break;
            default:return null;
        }
        return suggests;
    };

    public AtmosphereAccessorNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
        setMatcher(Vec3dNode.DEFAULT_MATCHER);
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        return ValidChecker.MATCH_FOUR_PARAMETER.check(this,args,context);
    }

    @Override
    public <T extends List<String> & Deque<String>> IAtmosphereAccessor parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException{
        final BlockPos pos = CommandBase.parseBlockPos(context.getSender(),args.toArray(new String[0]), 0,doCenterBlock);
        final boolean notAir;
        if("default".equals(args.get(3))){
            final World world = context.getWorld();
            if(!world.isBlockLoaded(pos)) throw new CommandException("geocraft.command.chunk_error.unloaded",pos.getX(),pos.getZ());
            final IBlockState state = world.getBlockState(pos);
            notAir = !state.getBlock().isAir(state,world,pos);
        }else notAir = CommandBase.parseBoolean(args.get(3));

        final IAtmosphereAccessor accessor = AtmosphereSystemManager.getAtmosphereAccessor(context.getWorld(),pos,notAir);

        if(accessor == null) throw new CommandException("geocraft.command.atmosphere.nonexistent",pos);

        return accessor;
    }

    @Override
    public int getParametersLength() {
        return 4;
    }

    @Nonnull
    @Override
    public Class<IAtmosphereAccessor> getType() {
        return IAtmosphereAccessor.class;
    }

    @Nonnull
    @Override
    public Class<IAtmosphereAccessor> getTypeClass() {
        return getType();
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.geocraft.atmosphere_accessor";
    }
}
