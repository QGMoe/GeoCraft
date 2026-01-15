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

package top.qiguaiaaaa.geocraft.api.command.builder.parameter.minecraft;

import net.minecraft.entity.Entity;
import top.qiguaiaaaa.geocraft.api.command.builder.parameter.SmartParameterNodeBuilder;
import top.qiguaiaaaa.geocraft.api.command.node.parament.minecraft.EntitySelectorNode;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author QiguaiAAAA
 */
public class EntitySelectorNodeBuilder extends SmartParameterNodeBuilder<List<Entity>,EntitySelectorNode,EntitySelectorNodeBuilder> {
    public EntitySelectorNodeBuilder(@Nonnull String name) {
        super(name);
    }

    protected boolean single = false;
    protected boolean allowPlayerName = true;
    protected boolean allowUUID = true;
    protected Class<? extends Entity> target = Entity.class;

    @Nonnull
    public EntitySelectorNodeBuilder target(final @Nonnull Class<? extends Entity> targetClass){
        target = targetClass;
        return this;
    }

    @Nonnull
    public EntitySelectorNodeBuilder asSingle(){
        single = true;
        return this;
    }

    @Nonnull
    public EntitySelectorNodeBuilder allowPlayerName(final boolean allowed){
        allowPlayerName = allowed;
        return this;
    }

    @Nonnull
    public EntitySelectorNodeBuilder allowUUID(final boolean allowed){
        allowUUID = allowed;
        return this;
    }

    @Nonnull
    @Override
    protected EntitySelectorNode buildInstance() {
        final EntitySelectorNode node = new EntitySelectorNode(name);
        node.setAllowUUID(allowUUID);
        node.setMatchTarget(target);
        node.setRequireSingleEntity(single);
        node.setAllowPlayerName(allowPlayerName);
        return node;
    }
}
