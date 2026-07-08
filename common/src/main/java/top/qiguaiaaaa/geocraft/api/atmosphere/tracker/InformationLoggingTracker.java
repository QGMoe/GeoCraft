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

package top.qiguaiaaaa.geocraft.api.atmosphere.tracker;

import top.qiguaiaaaa.geocraft.api.atmosphere.Atmosphere;
import top.qiguaiaaaa.geocraft.api.util.APIUtil;
import top.qiguaiaaaa.geocraft.api.util.io.FileLogger;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * 跟踪大气信息的监听器的抽象实现，该监听器会将大气信息记录到指定文件中
 */
public abstract class InformationLoggingTracker implements IAtmosphereTracker {
    protected final long id;
    protected final FileLogger logger;
    protected final int time;
    protected int nowTime = 0;

    /**
     * 创建一个大气信息追踪器
     * @param logger 一个文件Logger，用于将信息记录到文件中
     * @param time 追踪持续时间，单位大气刻
     */
    public InformationLoggingTracker(@Nonnull FileLogger logger, int time) {
        this.logger =logger;
        this.time = time;
        this.id = new Date().getTime();
    }

    public long getId() {
        return id;
    }

    public int getContinuousTime() {
        return time;
    }

    public int getProcessedTime() {
        return nowTime;
    }

    /**
     * 检查是否已经到结束记录的时间
     * @param atmosphere 记录的大气实例
     * @return 若已经结束，则返回true。该方法会自动关闭文件数据流。
     */
    protected boolean checkLoggingTime(@Nonnull Atmosphere atmosphere){
        if(nowTime >= time){
            APIUtil.LOGGER.info("track atmosphere task id={} completed",id);
            atmosphere.removeTracker(this);
            logger.close();
            return true;
        }
        return false;
    }

    @Override
    public void onAtmosphereUnload(@Nonnull Atmosphere atmosphere) {
        logger.close();
    }
}
