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

package 清汩萌.造.测试;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import 清汩萌.造.词块.词块;

/**
 * @author QiguaiAAAA
 */
public final class 测试词块 {
    @Test
    public void 测试词块创建(){
        Assertions.assertDoesNotThrow(()->词块.of("土1"));
        Assertions.assertDoesNotThrow(()->词块.of("[土壤]11"));
        Assertions.assertDoesNotThrow(()->词块.of("孇abc"));
        Assertions.assertDoesNotThrow(()->词块.of("土壤11"));
        Assertions.assertDoesNotThrow(()->词块.of("㵘㵘㵘㵘鈤囸鈤馹1145141919810abcdefg"));
        Assertions.assertThrows(IllegalArgumentException.class,()->词块.of("aabb"));
        Assertions.assertThrows(IllegalArgumentException.class,()->词块.of("[]aabb"));
        Assertions.assertThrows(IllegalArgumentException.class,()->词块.of("???????aabb[][][]agegase"));
    }

    @Test
    public void 测试词块相等(){
        Assertions.assertEquals(词块.of("土壤1"),词块.of("土壤1"));
        Assertions.assertEquals(词块.of("土壤2"),词块.of("[土壤]2"));
        Assertions.assertEquals(词块.of("[土壤]abab"),词块.of("[土壤]abab"));
        Assertions.assertEquals(词块.of("土壤"),词块.of("土壤"));
        Assertions.assertEquals(词块.of("图"),词块.of("图"));

        Assertions.assertNotEquals(词块.of("土壤1"),词块.of("土壤ab"));
        Assertions.assertNotEquals(词块.of("土壤2"),词块.of("[土壤]1"));
        Assertions.assertNotEquals(词块.of("[土壤]abab"),词块.of("[土壤]cdcd"));
        Assertions.assertNotEquals(词块.of("土壤"),词块.of("土层"));
        Assertions.assertNotEquals(词块.of("图"),词块.of("土"));
    }

    @Test
    public void 测试词块hash(){
        Assertions.assertEquals(词块.of("土壤").hashCode(),词块.of("土壤").hashCode());
        Assertions.assertEquals(词块.of("土壤").hashCode(),词块.of("[土壤]").hashCode());
        Assertions.assertEquals(词块.of("土").hashCode(),词块.of("土").hashCode());
        Assertions.assertEquals(词块.of("土壤ab").hashCode(),词块.of("土壤ab").hashCode());
        Assertions.assertEquals(词块.of("土ab").hashCode(),词块.of("土ab").hashCode());
    }
}
