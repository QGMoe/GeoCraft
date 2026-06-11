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

package moe.qingu.nickel.command.utils;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author QiguaiAAAA
 */
public class SplitCommandBranch extends CommandBranch{

    protected final Set<CommandBranch> branches;
    protected final Map<CommandBranch,ITextComponent> splitLocations = new HashMap<>();

    public SplitCommandBranch(final @Nonnull Collection<CommandBranch> subBranches){
        branches = new HashSet<>(Objects.requireNonNull(subBranches));
        for(@Nonnull final CommandBranch branch:branches){
            splitLocations.put(branch,branch.getDocuments().get(0));
        }
    }

    public void setEndDocument(@Nonnull final ITextComponent document){
        super.appendDocument(document);
    }

    @Override
    public void appendDocument(@Nonnull final ITextComponent document) {
        super.appendDocument(document);
        for(final @Nonnull CommandBranch branch:branches){
            branch.appendDocument(document.createCopy());
        }
    }

    @Override
    public void finish(@Nonnull final ICommand command) {
        super.finish(command);
        List<ITextComponent> subNodes = null;
        int lengthMax = 0;
        for(final @Nonnull CommandBranch branch:branches){
            final List<ITextComponent> subDocument = branch.getDocuments();
            final int splitIndex = getSplitIndexOf(branch);
            final int splitSize = subDocument.size()-splitIndex;// 相当于从分支点开始（包含）到末尾的长度
            lengthMax = Math.max(splitSize,lengthMax);
            if(subNodes == null){
                subNodes = subDocument.subList(splitIndex+1,subDocument.size());
                continue;
            }
            if(subNodes.isEmpty()) break;  //这意味着一定与其他节点不存在共有节点
            for(int i=0;i<subNodes.size();i++){
                if(i+1<splitSize && subNodes.get(i).equals(subDocument.get(splitIndex+i+1))) continue; //相同，可以保留
                subNodes = subNodes.subList(0,i); //不相同，或者超过该分支最大长度，截取相同部分
            }
        }
        if(subNodes == null) subNodes = Collections.emptyList();
        for(final ITextComponent component:subNodes){
            this.document.appendText(" ").appendSibling(component.createCopy());
        }
        if(subNodes.size()+1 < lengthMax) document.appendText(" ..."); //存在省略部分
        for (final @Nonnull CommandBranch branch : branches) {
            branch.finish(command);
        }
    }

    @Override
    public void print(@Nonnull final ICommandSender sender){
        super.print(sender);
        for(final @Nonnull CommandBranch branch:branches){
            branch.print(sender);
        }
    }

    protected int getSplitIndexOf(@Nonnull final CommandBranch branch){
        if(!splitLocations.containsKey(branch)) throw new IllegalArgumentException();
        return branch.getDocuments().indexOf(splitLocations.get(branch));
    }
}
