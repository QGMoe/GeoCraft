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

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import top.qiguaiaaaa.geocraft.test.GeoTest;
import top.qiguaiaaaa.geocraft.test.GeoTestItem;
import moe.qingu.nickel.command.builder.CommandBuilder;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.node.parameter.generic.StringNode;

import javax.annotation.Nonnull;

import java.util.stream.Collectors;

import static moe.qingu.nickel.command.Nodes.*;
import static moe.qingu.nickel.text.Texts.*;
import static top.qiguaiaaaa.geocraft.command.GeoArguments.*;

/**
 * @author QGMoe
 */
public final class CommandGeoTest {
    public static final String GEOTEST_COMMAND_NAME = "geotest";
    public static final String GEOTEST_PERMISSION_NODE = "geocraft.command."+GEOTEST_COMMAND_NAME;

    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_PASS = -1;
    private static final int STATUS_FAILED = -2;
    private static final int STATUS_ERROR = -3;

    public static ICommand create(){
        return new CommandBuilder(GEOTEST_COMMAND_NAME)
                .require(GEOTEST_PERMISSION_NODE).allow(DefaultPermissionLevel.OP).register()
                .requirePlayer(true)
                .then(literals()
                        .when("run").then($token("test_id")
                                .translate("geocraft.command.geotest.arg.test_id")
                                .allow(GeoTest.queryAll().stream().map(Object::toString).collect(Collectors.toList()))
                                .then(_pos()
                                        .then($entity("target")
                                                .asSingle()
                                                .asOptional()
                                                .translate("geocraft.command.geotest.arg.target")
                                                .then(execute(CommandGeoTest::doTest))))))
                .build();
    }

    public static void doTest(final @Nonnull ExecuteContext ctx) {
        final BlockPos pos = ctx.getBlockPos(POS);
        final ICommandSender sender = ctx.getSender();
        final GeoTestItem item = GeoTest.query(new ResourceLocation(ctx.get("test_id", StringNode.class)));
        assert item != null;
        try {
            final @Nonnull EnumActionResult res = item.test(ctx.getWorld(),pos,sender);
            switch (res){
                case FAIL:{
                    sender.sendMessage(translation("geocraft.command.geotest.run.failed").arg(item.getId()).color(TextFormatting.RED).done());
                    sender.sendMessage(GeoMessages.在GitHub上向作者报告().done());
                    ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_FAILED);
                    break;
                }
                case PASS:{
                    sender.sendMessage(translation("geocraft.command.geotest.run.pass").arg(item.getId()).color(TextFormatting.GOLD).done());
                    ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_PASS);
                    break;
                }
                case SUCCESS:{
                    sender.sendMessage(translation("geocraft.command.geotest.run.success").arg(item.getId()).color(TextFormatting.GREEN).done());
                    ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_SUCCESS);
                }
                default:break;
            }
        }catch (final @Nonnull Exception e){
            ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,STATUS_ERROR);
            sender.sendMessage(translation("geocraft.command.geotest.run.failed").arg(item.getId())
                    .color(TextFormatting.DARK_RED)
                    .hoverTo(HoverEvent.Action.SHOW_TEXT).content(plain(e.getMessage()).color(TextFormatting.AQUA))
                    .done());
            sender.sendMessage(GeoMessages.在GitHub上向作者报告().done());
        }
    }
}
