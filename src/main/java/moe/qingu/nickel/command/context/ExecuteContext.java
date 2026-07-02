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

package moe.qingu.nickel.command.context;

import moe.qingu.nickel.command.node.ICommandNode;
import moe.qingu.nickel.command.reader.InputReader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import moe.qingu.nickel.command.node.parameter.ParameterNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

/**
 * @author QiguaiAAAA
 */
public final class ExecuteContext extends CommandContext{
    private static final Function<String,String> OnNoContextFound = k->{
        throw new IllegalArgumentException("Context "+k+" doesn't exist!");
    };

    private final Map<String,Object> contexts = new HashMap<>();

    public ExecuteContext(@Nonnull final InputReader context,
                          @Nonnull final ICommand command,
                          @Nonnull final MinecraftServer server,
                          @Nonnull final ICommandSender sender) {
        super(context,command, server, sender);
    }

    public void enter(final @Nonnull ICommandNode node) throws CommandException {
        this.nodeContext.context.push(node);
        try(@Nonnull final ContextStack<?> ignored = this.nodeContext){
            final int begin = input.getCursor();
            node.execute(input,this);
            input.setCursor(begin);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(@Nonnull final String key){
        return (T) contexts.remove(key);
    }

    public void put(@Nonnull final String key, @Nonnull final Object content){
        contexts.put(key,content);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@Nonnull final String key){
        return (T) contexts.computeIfAbsent(key, OnNoContextFound);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@Nonnull final String key, @Nonnull final Class<? extends ParameterNode<T>> paraType){
        return (T) contexts.computeIfAbsent(key, OnNoContextFound);
    }

    public float getFloat(@Nonnull String key){
        return (float) contexts.get(key);
    }

    public double getDouble(@Nonnull String key){
        return (double) contexts.get(key);
    }

    public int getInt(@Nonnull String key){
        return (int) contexts.get(key);
    }

    @Nonnull
    public Entity getEntity(@Nonnull final String key) throws CommandException {
        final Object context = contexts.get(key);
        if(context == null) throw new CommandException("nickel.command.context.get.non_entity");
        if(context instanceof Collection<?>){
            return getEntityInCollection((Collection<?>) context);
        }else if(context instanceof Entity){
            return (Entity) context;
        }else if(context instanceof Optional<?>){
            final Optional<?> optional = (Optional<?>) context;
            if(!optional.isPresent()) throw new CommandException("nickel.command.context.get.non_entity");
            throwIfInvalidEntityClass(optional.get());
            return (Entity) optional.get();
        }else throw new CommandException("nickel.command.context.get.unknown_argument.entity",context.getClass());
    }

    @Nonnull
    private Entity getEntityInCollection(@Nonnull final Collection<?> collection) throws CommandException {
        if(collection.isEmpty()) throw new CommandException("nickel.command.context.get.non_entity");
        if(collection instanceof List<?>){
            final List<?> list = (List<?>) collection;
            throwIfInvalidEntityClass(list.get(0));
            return (Entity) list.get(0);
        }else if(collection instanceof Queue<?>){
            final Queue<?> queue = (Queue<?>) collection;
            final Object ele = queue.peek();
            if(ele == null) throw new CommandException("nickel.command.context.get.non_entity");
            throwIfInvalidEntityClass(ele);
            return (Entity) ele;
        }
        final Object ele = collection.iterator().next();
        throwIfInvalidEntityClass(ele);
        return (Entity) ele;
    }

    @Nonnull
    public List<Entity> getEntities(@Nonnull final String key) throws CommandException{
        final Object context = contexts.get(key);
        if(context == null) throw new CommandException("nickel.command.context.get.non_entity");
        if(context instanceof Collection<?>){
            return getEntitiesInCollection((Collection<?>) context);
        }else if(context instanceof Entity){
            return Collections.singletonList((Entity) context);
        }else if(context instanceof Optional<?>){
            final Optional<?> optional = (Optional<?>) context;
            if(!optional.isPresent()) throw new CommandException("nickel.command.context.get.non_entity");
            throwIfInvalidEntitiesClass(optional.get());
            return Collections.singletonList((Entity) optional.get());
        }else throw new CommandException("nickel.command.context.get.unknown_argument.entities", context.getClass());
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private List<Entity> getEntitiesInCollection(@Nonnull final Collection<?> collection) throws CommandException{
        if(collection.isEmpty()) throw new CommandException("nickel.command.context.get.non_entity");
        if(collection instanceof List<?>){
            final List<?> list = (List<?>) collection;
            throwIfInvalidEntitiesClass(list.get(0));
            return (List<Entity>) list;
        }else if(collection instanceof Queue<?>){
            final Queue<?> queue = (Queue<?>) collection;
            throwIfInvalidEntitiesClass(queue.peek());
            return new ArrayList<>((Queue<? extends Entity>)queue);
        }
        throwIfInvalidEntitiesClass(collection.iterator().next());
        return new ArrayList<>((Collection<? extends Entity>) collection);
    }

    private void throwIfInvalidEntityClass(@Nullable final Object ele) throws CommandException {
        if(!(ele instanceof Entity)) throw new CommandException("nickel.command.context.get.unknown_argument.entity",ele==null?"NULL":ele.getClass());
    }

    private void throwIfInvalidEntitiesClass(@Nullable final Object ele) throws CommandException {
        if(!(ele instanceof Entity)) throw new CommandException("nickel.command.context.get.unknown_argument.entities",ele==null?"NULL":ele.getClass());
    }

    public BlockPos getBlockPos(@Nonnull String key){
        return (BlockPos) contexts.get(key);
    }

    @Nonnull
    public Map<String, Object> getContexts() {
        return contexts;
    }

    @Nonnull
    public EntityPlayerMP getSenderAsPlayer() throws PlayerNotFoundException {
        if (sender instanceof EntityPlayerMP) {
            return (EntityPlayerMP)sender;
        } else {
            throw new PlayerNotFoundException("commands.generic.player.unspecified");
        }
    }

    public void notifyCommandListener(@Nonnull final String translationKey,final Object... translationArgs){
        CommandBase.notifyCommandListener(sender,command,translationKey,translationArgs);
    }

    public void notifyCommandListener(final int flags,@Nonnull final String translationKey,final Object... translationArgs){
        CommandBase.notifyCommandListener(sender,command,flags,translationKey,translationArgs);
    }
}
