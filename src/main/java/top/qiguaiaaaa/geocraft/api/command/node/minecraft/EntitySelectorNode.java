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

package top.qiguaiaaaa.geocraft.api.command.node.minecraft;

import com.google.common.reflect.TypeToken;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;
import top.qiguaiaaaa.geocraft.api.command.node.generic.SmartParameterNode;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class EntitySelectorNode extends SmartParameterNode<List<Entity>> {

    public static final DefaultParser<List<Entity>> DEFAULT_PARSER = (node, context) -> Collections.singletonList(context.getSenderAsPlayer());
    public static final BiFunction<List<String>, SuggestContext,List<String>> DEFAULT_SUGGESTOR = ((args, context) ->
            Arrays.asList("@s","@a","@p","@r","@e", context.getSender().getName()));

    protected static final TypeToken<List<Entity>> Token = new TypeToken<List<Entity>>(List.class) {};
    protected boolean allowPlayerName = true;
    protected boolean allowUUID = true;
    protected boolean requireSingleEntity = false;
    protected Class<? extends Entity> matchTarget = Entity.class;

    public EntitySelectorNode(@Nonnull String name) {
        super(name);
        setDefaultParser(DEFAULT_PARSER);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    public void setAllowPlayerName(final boolean allowPlayerName) {
        this.allowPlayerName = allowPlayerName;
    }

    public void setAllowUUID(final boolean allowUUID) {
        this.allowUUID = allowUUID;
    }

    public void setRequireSingleEntity(final boolean requireSingleEntity) {
        this.requireSingleEntity = requireSingleEntity;
    }

    public void setMatchTarget(@Nonnull final Class<? extends Entity> matchTarget) {
        this.matchTarget = matchTarget;
    }

    public boolean isPlayerNameAllowed() {
        return allowPlayerName;
    }

    public boolean isUUIDAllowed() {
        return allowUUID;
    }

    public boolean isRequireSingleEntity() {
        return requireSingleEntity;
    }

    public Class<? extends Entity> getMatchTarget() {
        return matchTarget;
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Type getType() {
        return Token.getType();
    }

    @Nonnull
    @Override
    public String getLocalizedType() {
        return "api.geo.command.parameter.minecraft.entitySelector";
    }

    @Override
    public <T extends List<String> & Deque<String>> List<Entity> parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        if(isRequireSingleEntity()) return Collections.singletonList(EntitySelector.matchOneEntity(context.getSender(), args.getFirst(), matchTarget));
        return EntitySelector.matchEntities(context.getSender(),args.getFirst(),matchTarget);
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, InvalidBlockStateException, NumberInvalidException {
        if(!MATCH_ONE_PARAMETER.check(this,args,context)) return false;
        if(isPlayerNameAllowed()) return true;

        final String arg = args.get(0);
        if(arg.startsWith("@")) return true;
        if(!isUUIDAllowed()) return false;

        try {
            UUID.fromString(args.get(0));
        }catch (IllegalArgumentException e){
            return false;
        }
        return true;
    }
}
