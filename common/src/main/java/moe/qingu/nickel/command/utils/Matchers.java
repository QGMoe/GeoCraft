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

import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * @author QiguaiAAAA
 */
public final class Matchers {
    public static final Claimer ANY = input-> !input.isRemainingEmpty();
    public static final Claimer INTEGER = matchInteger(Integer.MIN_VALUE,Integer.MAX_VALUE);
    public static final Claimer LONG = matchLong(Long.MIN_VALUE,Long.MAX_VALUE);
    public static final Claimer DOUBLE = matchDouble(Double.MIN_VALUE,Double.MAX_VALUE);
    public static final Claimer RESOURCE_LOCATION = matchOnlyFirstToken(Matchers::isResourceLocation);

    private Matchers(){}

    @Nonnull
    public static Claimer matchOnlyFirstToken(@Nonnull final Predicate<String> simpleMatcher){
        return ANY.and(input-> simpleMatcher.test(input.readToken()));
    }

    @Nonnull
    public static Claimer matchInteger(final int min, final int max){
        return matchOnlyFirstToken(arg->{
            try {
                CommandBase.parseInt(arg,min,max);
            } catch (final @Nonnull NumberInvalidException e) {
                return false;
            }
            return true;
        });
    }

    @Nonnull
    public static Claimer matchLong(final long min, final long max){
        return matchOnlyFirstToken(arg->{
            try {
                CommandBase.parseLong(arg,min,max);
            } catch (NumberInvalidException e) {
                return false;
            }
            return true;
        });
    }

    @Nonnull
    public static Claimer matchDouble(final double min, final double max){
        return matchOnlyFirstToken(arg->{
            try {
                CommandBase.parseDouble(arg,min,max);
            } catch (NumberInvalidException e) {
                return false;
            }
            return true;
        });
    }

    public static boolean isResourceLocation(@Nonnull final String arg){
        final String[] split = arg.split(":");
        return split.length==1 || split.length==2 && !split[0].contains("/");
    }
}
