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

package moe.qingu.nickel.command.node.parameter.generic;

import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.util.text.TextComponentTranslation;
import moe.qingu.nickel.command.context.CommandContext;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.exception.NickelSyntaxException;
import moe.qingu.nickel.command.node.parameter.SmartParameterNode;
import moe.qingu.nickel.command.utils.ValidChecker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author QiguaiAAAA
 */
public class StringNode extends SmartParameterNode<String> {
    public static final ValidChecker CHECK_WHITELIST = (self, args, context) -> {
        final StringNode node = (StringNode) self;
        if(node.whitelist !=null && !node.whitelist.isEmpty()){
            if(node.whitelist.contains(args.get(0))) return true;
            throw new NickelSyntaxException(node.currentBranch, node,
                    new TextComponentTranslation("nickel.command.parameter.string.invalid.white",args.get(0),String.join(" ", node.whitelist)));
        }
        return true;
    };
    public static final ValidChecker CHECK_BLACKLIST = (self, args, context) -> {
        final StringNode node = (StringNode) self;
        if(node.blacklist != null && !node.blacklist.isEmpty() && node.blacklist.contains(args.get(0))){
            throw new NickelSyntaxException(node.currentBranch, node,
                    new TextComponentTranslation("nickel.command.parameter.string.invalid.black",args.get(0),String.join(" ",node.blacklist)));
        }
        return true;
    };
    public static final ValidChecker CHECK_PATTERN = (self, args, context) -> {
        final StringNode node = (StringNode) self;
        if(node.pattern != null && !node.pattern.matcher(args.get(0)).matches()) {
            throw new NickelSyntaxException(node.currentBranch, node,
                    new TextComponentTranslation("nickel.command.parameter.string.nonMatch", args.get(0), node.pattern.toString()));
        }
        return true;
    };
    protected Set<String> whitelist = null;
    protected Set<String> blacklist = null;
    protected Pattern pattern = null;
    protected ValidChecker checker = ValidChecker.MATCH_ONE_PARAMETER;
    public StringNode(@Nonnull String name) {
        super(name);
    }

    public void addAllowValue(@Nonnull final String content){
        if(whitelist == null) whitelist = new HashSet<>();
        whitelist.add(content);
    }

    public void addDisallowedValue(@Nonnull final String content){
        if(blacklist == null) blacklist = new HashSet<>();
        blacklist.add(content);
    }

    public void setPattern(@Nullable final Pattern pattern){
        this.pattern = pattern;
    }

    @Nonnull
    public StringNode refresh(){
        checker = ValidChecker.MATCH_ONE_PARAMETER;
        if(whitelist != null && !whitelist.isEmpty()) checker = checker.and(CHECK_WHITELIST);
        if(blacklist != null && !blacklist.isEmpty()) checker = checker.and(CHECK_BLACKLIST);
        if(pattern != null) checker = checker.and(CHECK_PATTERN);
        return this;
    }

    @Override
    public int getParametersLength() {
        return 1;
    }

    @Nonnull
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Nonnull
    @Override
    public Class<String> getTypeClass() {
        return getType();
    }

    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "nickel.command.parameter.generic.string";
    }

    @Override
    public boolean checkValid(@Nonnull List<String> args, @Nonnull CommandContext context) throws SyntaxErrorException, NumberInvalidException {
        return checker.check(this,args,context);
    }

    @Override
    public <T extends List<String> & Deque<String>> String parseParameter(@Nonnull T args, @Nonnull ExecuteContext context) {
        return args.getFirst();
    }
}
