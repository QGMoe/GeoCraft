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

package 清汩萌.造.词块;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public final class 下标工具 {
    private static final int[] $允许的下标字符 = "0123456789abcdefghijklmnopqrstuvwxyz".codePoints().toArray();
    private static final @Nonnull IntOpenHashSet $允许的下标字符集合 = new IntOpenHashSet($允许的下标字符,1.0f);

    static {
        $允许的下标字符集合.trim();
    }

    private 下标工具(){}

    public static boolean 是合法下标(final @Nonnull String str){
        for(int i = 0, len = str.length(); i < len; ) {
            final int code = str.codePointAt(i);
            i += Character.charCount(code);
            if(!是下标字符(code)) return false;
        }
        return true;
    }

    public static boolean 是下标字符(final int codePoint){
        return $允许的下标字符集合.contains(codePoint);
    }

    /**
     * 校验输入的字符串是否是合法的下标。如果是合法的，则返回，否则抛出错误。
     * @param str 需要检测的字符串
     * @throws IllegalArgumentException 当输入的字符串并不是一个合法的下标时抛出。
     * @see #是合法下标(String) 
     * @return 如果合法，则返回原来的字符串
     */
    @Nonnull
    public static String 需要合法下标(final @Nonnull String str){
        if(是合法下标(str)) return str;
        else throw new IllegalArgumentException(str + " 并不是一个合法的下标");
    }
}
