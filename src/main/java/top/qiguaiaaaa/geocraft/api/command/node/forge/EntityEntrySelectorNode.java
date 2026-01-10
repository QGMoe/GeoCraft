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

package top.qiguaiaaaa.geocraft.api.command.node.forge;

import net.minecraft.command.CommandException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import top.qiguaiaaaa.geocraft.api.command.context.CommandContext;
import top.qiguaiaaaa.geocraft.api.command.context.ExecuteContext;
import top.qiguaiaaaa.geocraft.api.command.context.SuggestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author QiguaiAAAA
 */
public class EntityEntrySelectorNode extends ForgeRegistryEntryNode<EntityEntry> {
    public static final BiFunction<List<String>, SuggestContext,List<String>> DEFAULT_SUGGESTOR = createSuggestProviderFromRegistry(ForgeRegistries.ENTITIES);

    public EntityEntrySelectorNode(@Nonnull String name) {
        super(name);
        setSuggestProvider(DEFAULT_SUGGESTOR);
    }

    @Nonnull
    @Override
    public Class<EntityEntry> getType() {
        return EntityEntry.class;
    }

    @Nonnull
    @Override
    public String getLocalizedType() {
        return "api.geo.command.parameter.forge.entityEntry";
    }

    @Override
    public <T extends List<String> & Deque<String>> EntityEntry parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) throws CommandException {
        return getEntityEntryByText(context,args.getFirst());
    }

    @Nonnull
    public EntityEntry getEntityEntryByText(@Nonnull final CommandContext context,@Nonnull final String text) throws InvalidEntityEntryException{
        final ResourceLocation loc = new ResourceLocation(text);
        @Nullable final EntityEntry entry  = ForgeRegistries.ENTITIES.getValue(loc);

        if (entry == null) {
            throw new InvalidEntityEntryException(text);
        } else {
            return entry;
        }
    }

    public class InvalidEntityEntryException extends CommandException{

        public InvalidEntityEntryException(@Nullable final String id){
            super("api.geo.command.parameter.entityEntry.invalid",id,EntityEntrySelectorNode.this.getLocalizedParameter());
        }

        public InvalidEntityEntryException(String message, Object... objects) {
            super(message, objects);
        }
    }
}
