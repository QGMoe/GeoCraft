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

package top.qiguaiaaaa.geocraft.handler;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.configs.GeneralConfig;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;

import javax.annotation.Nonnull;

import java.util.Random;

import static top.qiguaiaaaa.geocraft.configs.GeneralConfig.*;

/**
 * @author QiguaiAAAA
 */
@Mod.EventBusSubscriber(modid = GeoCraft.MODID)
public final class ServerStatusMonitor {
    private static final Random random = new Random((int)0d+00+0721);
    static final TickDurationStatic ticks32Static = new TickDurationStatic((byte) 5,PERFORMANCE_SAMPLING_TICK_PERCENTILE.get(0)),
    tick256Static = new TickDurationStatic((byte) 8,PERFORMANCE_SAMPLING_TICK_PERCENTILE.get(1)),
    tick1024Static = new TickDurationStatic((byte) 10,PERFORMANCE_SAMPLING_TICK_PERCENTILE.get(2));
    static long tickBeginTime = System.currentTimeMillis();
    static boolean lagging = false,alarming = false,enableLagging = true,enableAlarming = true;

    private ServerStatusMonitor(){}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerTickStart(@Nonnull TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            return;
        }
        tickBeginTime = System.currentTimeMillis();
        lagging = false;
        alarming = false;
        enableLagging = ENABLE_PERFORMANCE_DELAY_DETECT.getValue();
        enableAlarming = ENABLE_PERFORMANCE_WARNING.getValue();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTickEnd(@Nonnull TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            return;
        }
        long duration = getCurrentTickTime();
        ticks32Static.putTick(duration);
        tick256Static.putTick(duration);
        tick1024Static.putTick(duration);
    }

    public static long getCurrentTickTime(){
        return System.currentTimeMillis() - tickBeginTime;
    }

    public static boolean isServerLagging(){
        if(!enableLagging) return false;
        if(lagging) return true;
        if(getCurrentTickTime()<PROTECT_TIME.getValue()) return false;
        if(tick1024Static.getPercentTickTime() > TICK_DELAY_THRESHOLD.get(3)){
//            GeoCraft.getLogger().info("Because {} tick in 1024 reached {} ms, mark lagging. 1024: {} 256: {} 32: {} Cur: {}",
//                    tick1024Static.percent,TICK_DELAY_THRESHOLD.get(3),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return lagging = true;
        }
        if(tick256Static.getPercentTickTime() > TICK_DELAY_THRESHOLD.get(2)){
//            GeoCraft.getLogger().info("Because {} tick in 256 reached {} ms, mark lagging. 1024: {} 256: {} 32: {} Cur: {}",
//                    tick256Static.percent,TICK_DELAY_THRESHOLD.get(2),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return lagging = true;
        }
        if(ticks32Static.getPercentTickTime() > TICK_DELAY_THRESHOLD.get(1)){
//            GeoCraft.getLogger().info("Because {} tick in 32 reached {} ms, mark lagging. 1024: {} 256: {} 32: {} Cur: {}",
//                    ticks32Static.percent,TICK_DELAY_THRESHOLD.get(1),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return lagging = true;
        }
        if(GeneralConfig.ENABLE_SINGLE_TICK_DELAY_DETECT.getValue() && getCurrentTickTime() > TICK_DELAY_THRESHOLD.get(0)){
            return lagging = true;
        }
        return lagging;
    }

    public static boolean isServerCloselyLagging(){
        if(!enableAlarming) return false;
        if(alarming) return true;
        if(getCurrentTickTime()<PROTECT_TIME.getValue()) return false;
        if(tick1024Static.getPercentTickTime() > TICK_DELAY_WARNING_THRESHOLDS.get(3)){
//            GeoCraft.getLogger().info("Because {} tick in 1024 reached {} ms, alarming. 1024: {} 256: {} 32: {} Cur: {}",
//                    tick1024Static.percent,TICK_DELAY_WARNING_THRESHOLDS.get(3),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return alarming = true;
        }
        if(tick256Static.getPercentTickTime() > TICK_DELAY_WARNING_THRESHOLDS.get(2)){
//            GeoCraft.getLogger().info("Because {} tick in 256 reached {} ms, alarming. 1024: {} 256: {} 32: {} Cur: {}",
//                    tick256Static.percent,TICK_DELAY_WARNING_THRESHOLDS.get(2),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return alarming = true;
        }
        if(ticks32Static.getPercentTickTime() > TICK_DELAY_WARNING_THRESHOLDS.get(1)){
//            GeoCraft.getLogger().info("Because {} tick in 256 reached {} ms, alarming. 1024: {} 256: {} 32: {} Cur: {}",
//                    ticks32Static.percent,TICK_DELAY_WARNING_THRESHOLDS.get(1),tick1024Static,tick256Static,ticks32Static,getCurrentTickTime());
            return alarming = true;
        }
        if(GeneralConfig.ENABLE_SINGLE_TICK_DELAY_DETECT.getValue() && getCurrentTickTime() > GeneralConfig.TICK_DELAY_WARNING_THRESHOLDS.get(0)){
            return alarming = true;
        }
        return alarming;
    }

    public static int getRecommendedBlockFlags(){
        if(ServerStatusMonitor.isServerLagging()){
            return Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS;
        }
        if(ServerStatusMonitor.isServerCloselyLagging()){
            return Constants.BlockFlags.SEND_TO_CLIENTS | Constants.BlockFlags.NO_OBSERVERS;
        }
        return Constants.BlockFlags.DEFAULT;
    }

    private static class TickDurationStatic{
        private final long[] tickDurations;
        private final int period;
        private final double percent;
        private int cur = 0;
        private double averageTickTime;
        private long percentTickTime;

        public TickDurationStatic(byte periodLevel,double percent){
            this.period = 1<<periodLevel;
            tickDurations = new long[period];
            this.percent = percent;
        }

        public void putTick(long duration){
            tickDurations[cur++] = duration;
            cur &= (period-1);
            averageTickTime = MathUtil.getAverage(tickDurations);
            percentTickTime = MathUtil.getPercent(tickDurations,percent);
        }

        public double getAverageTickTime() {
            return averageTickTime;
        }

        public long getPercentTickTime() {
            return percentTickTime;
        }

        @Override
        public String toString() {
            return "[Tick Static "+period+" : average "+averageTickTime+" ms , percent "+percent+" "+percentTickTime+" ms]";
        }
    }
}
