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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * @author QiguaiAAAA
 */
@ThreadSafe
public final class 词块 {
    final int $主体;
    final String $多字主体;
    final @Nonnull String $下标;

    词块(final int $主体, @Nonnull final String $下标){
        this.$主体 = 主体工具.需要合法主体($主体);
        this.$下标 = 下标工具.需要合法下标(Objects.requireNonNull($下标));
        this.$多字主体 = null;
    }

    词块(final @Nonnull String $多字主体, @Nonnull final String $下标){
        this.$主体 = 0;
        this.$多字主体 = 主体工具.需要合法主体(Objects.requireNonNull($多字主体));
        this.$下标 = 下标工具.需要合法下标(Objects.requireNonNull($下标));
    }

    @Nonnull
    public static 词块 of(final @Nonnull String raw){
        final @Nonnull String serialised = Objects.requireNonNull(raw).replace("[","").replace("]",""); //去除主体的[]包裹
        final int splitLoc = 获取下标起始索引(serialised);
        if(splitLoc <= 0) throw new IllegalArgumentException(serialised + " 并没有一个有效的主体");
        final int firstCodePoint = serialised.codePointAt(0);
        final @Nonnull String 下标 = serialised.substring(splitLoc);
        if(Character.charCount(firstCodePoint) == splitLoc){ //主体就一个字符
            return new 词块(firstCodePoint,下标);
        }else return new 词块(serialised.substring(0,splitLoc),下标);
    }

    @Nonnull
    public static 词块 of(final @Nonnull String $主体, @Nonnull final String $下标) {
        return of($主体+$下标);
    }

    @Nonnull
    public String 获取主体() {
        return $多字主体 != null? $多字主体 : new String(new int[]{$主体},0,1);
    }

    public int 获取单字主体() {
        return $主体;
    }

    public String 获取多字主体() {
        return $多字主体;
    }

    @Nonnull
    public String 获取下标() {
        return $下标;
    }

    public boolean 是多字主体(){
        return $多字主体 != null;
    }

    private static int 获取下标起始索引(final @Nonnull String serialised){
        for(int loc =0,len = serialised.length();loc<len;){
            final int cp = Character.codePointAt(serialised,loc);
            if(!主体工具.是主体字符(cp)){
                return loc;
            }
            loc += Character.charCount(cp);
        }
        return serialised.length();
    }

    @Override
    public int hashCode() {
        int hash = 31 + $下标.hashCode();
        if($多字主体 != null) hash = 31 * hash + $多字主体.hashCode();
        else hash = 31 * hash + $主体;

        return hash;
    }

    @Override
    public boolean equals(final @Nonnull Object obj) {
        if(obj instanceof 词块){
            final 词块 B = (词块) obj;
            if(this.$多字主体 == null && B.$多字主体 == null) return this.$主体 == B.$主体 && this.$下标.equals(B.$下标);
            else if(this.$多字主体 != null) return this.$多字主体.equals(B.$多字主体) && this.$下标.equals(B.$下标);
            else return false;
        }else return false;
    }

    @Override
    public String toString() {
        if($多字主体 != null){
            return '['+$多字主体+']' + $下标;
        }else return new String(new int[]{$主体},0,1) + $下标;
    }
}