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

package top.qiguaiaaaa.geocraft.api.util;


import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.qiguaiaaaa.geocraft.api.GeoCraftAPI;

import javax.annotation.Nonnull;

/**
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class APIUtil {
    private APIUtil(){}
    public static final Logger LOGGER = LogManager.getLogger(GeoCraftAPI.PROVIDERS);

    /**
     * 获取当前调用者信息
     * @param who 具体哪个调用者，即往上溯源多少层。例如1就是返回上一层
     * @return 返回一个四个元素的Object数组,包含调用者的className，方法名称，文件名，行数
     */
    @Nonnull
    public static String callerInfo(int who){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (who + 2 >= stackTrace.length)
            return "?.?(?:?)";
        StackTraceElement element = stackTrace[who+2];
        return element.getClassName()+'.'+element.getMethodName()+'('+element.getFileName()+':'+element.getLineNumber()+')';
    }

    public static class Constants{
        public static final String FLUIDLOGGED_API = "fluidlogged_api";
    }

    public static class ModChecker{
        public static final boolean FLUIDLOGGED_API = Loader.isModLoaded(APIUtil.Constants.FLUIDLOGGED_API);
    }
}
