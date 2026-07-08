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
import moe.qingu.nickel.command.utils.Claimer;
import moe.qingu.nickel.command.suggestor.Suggestor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.utils.Matchers;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public class Vec3dNode extends MinecraftVec3Node<Vec3d> {
    public static final DefaultParser<Vec3d> DEFAULT_PARSER = (node, context) -> context.getSender().getPositionVector();
    /**
     *  若第一个参数为数字，则说明为坐标。不会检查参数长度是否满足条件，因为若检查则会导致歧义。
     */
    public static final Claimer DEFAULT_CLAIMER = Matchers.matchOnlyFirstToken(arg->{
        try {
            CommandBase.parseDouble(0d,arg,false);
        }catch (final NumberInvalidException e) {
            return false;
        }
        return true;
    });

    public static final Suggestor<Vec3d> DEFAULT_SUGGESTOR = TokenizeSuggestor.of(3,
            (args, context) -> {
                final List<String> suggests = Lists.newArrayList("~");
                if(context.getTargetPos() == null){
                    final Vec3d pos = context.getSender().getPositionVector();
                    switch (args.length){
                        case 1:
                            suggests.add(String.valueOf(pos.x));
                            break;
                        case 2:
                            suggests.add(String.valueOf(pos.y));
                            break;
                        case 3:
                            suggests.add(String.valueOf(pos.z));
                            break;
                        default:return Collections.emptyList();
                    }
                }else {
                    final Vec3i pos = context.getTargetPos();
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
                        default:return Collections.emptyList();
                    }
                }
                return suggests;
            });

    public Vec3dNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setClaimer(DEFAULT_CLAIMER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Override
    public int getTokenCount() {
        return 3;
    }

    @Nonnull
    @Override
    public Class<Vec3d> getTypeClass() {
        return Vec3d.class;
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.minecraft.vector3d";
    }

    @Override
    @Nonnull
    public Vec3d parse(@Nonnull final String[] args, @Nonnull final CommandContext context) throws CommandException {
        final Vec3d pos = context.getSender().getPositionVector();
        final CommandBase.CoordinateArg[] coors = new CommandBase.CoordinateArg[3];
        for (int i=0;i<3;i++){
            final double base;
            switch (i){
                case 0:
                    base = pos.x;
                    break;
                case 1:
                    base = pos.y;
                    break;
                default:
                    base = pos.z;
            }
            coors[i] = CommandBase.parseCoordinate(base,args[i],doCenterBlock);
        }
        return new Vec3d(coors[0].getResult(),coors[1].getResult(),coors[2].getResult());
    }
}
