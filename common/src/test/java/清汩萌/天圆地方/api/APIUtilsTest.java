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

package 清汩萌.天圆地方.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.qiguaiaaaa.geocraft.api.util.APIMathUtil;
import top.qiguaiaaaa.geocraft.api.util.APIUtil;
import 清汩萌.天圆地方.天圆地方测试;

/**
 * @author QiguaiAAAA
 */
public class APIUtilsTest {


    /**
     * @author QiguaiAAAA,ChatGPT
     * @see APIMathUtil
     */
    public static final class TestAPIMathUtil{

        /**
         * ChatGPT Generated
         */
        @Test
        public void clampTest(){

            // 正常区间
            Assertions.assertEquals(5, APIMathUtil.clamp(5,1,10));

            // 小于最小值
            Assertions.assertEquals(1, APIMathUtil.clamp(0,1,10));

            // 大于最大值
            Assertions.assertEquals(10, APIMathUtil.clamp(20,1,10));

            // 等于边界
            Assertions.assertEquals(1, APIMathUtil.clamp(1,1,10));
            Assertions.assertEquals(10, APIMathUtil.clamp(10,1,10));

            // 负数区间
            Assertions.assertEquals(-5, APIMathUtil.clamp(-5,-10,10));
            Assertions.assertEquals(-10, APIMathUtil.clamp(-20,-10,10));

            // long 极值
            Assertions.assertEquals(Long.MAX_VALUE,
                    APIMathUtil.clamp(Long.MAX_VALUE,Long.MIN_VALUE,Long.MAX_VALUE));

            Assertions.assertEquals(Long.MIN_VALUE,
                    APIMathUtil.clamp(Long.MIN_VALUE,Long.MIN_VALUE,Long.MAX_VALUE));

            天圆地方测试.LOGGER.info("clampTest passed");
        }
    }

    /**
     * @author QiguaiAAAA, ChatGPT
     * @see APIUtil
     */
    public static final class TestAPIUtil{

        /**
         * ChatGPT Generated
         */
        @Test
        public void callerInfoTest(){

            // 获取调用者信息
            final String info = APIUtil.callerInfo(0);

            天圆地方测试.LOGGER.info("callerInfo result={}", info);

            // 不应该是 fallback
            Assertions.assertNotEquals("?.?(?:?)", info);

            // 应包含当前测试方法名
            Assertions.assertTrue(info.contains("callerInfoTest"));

            // 应包含当前类名
            Assertions.assertTrue(info.contains("TestAPIUtil"));

            // 极大 who 应触发 fallback
            final String fallback = APIUtil.callerInfo(999);

            天圆地方测试.LOGGER.info("callerInfo fallback={}", fallback);

            Assertions.assertEquals("?.?(?:?)", fallback);
        }
    }
}
