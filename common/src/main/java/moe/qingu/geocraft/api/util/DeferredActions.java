/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.geocraft.api.util;

import net.minecraftforge.fml.common.LoaderState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * @author QGMoe
 */
public final class DeferredActions {
    private static final EnumMap<LoaderState, List<Runnable>> queues = new EnumMap<>(LoaderState.class);
    private static final EnumMap<LoaderState, List<Runnable>> consumed = new EnumMap<>(LoaderState.class);
    private static final LoaderState[] states = LoaderState.values();

    private static void on(final @Nonnull LoaderState state,final @Nonnull Runnable runnable){
        final List<Runnable> list = queues.computeIfAbsent(state,(k->new ArrayList<>()));
        list.add(runnable);
    }

    private static void consume(final @Nonnull LoaderState state,final @Nonnull Runnable runnable){
        runnable.run();
        final List<Runnable> list = consumed.computeIfAbsent(state,(k -> new ArrayList<>()));
        list.add(runnable);
    }

    public static void onPreInit(final @Nonnull Runnable runnable){
        on(LoaderState.PREINITIALIZATION,runnable);
    }

    public static void onInit(final @Nonnull Runnable runnable){
        on(LoaderState.INITIALIZATION,runnable);
    }

    public static void onPostInit(final @Nonnull Runnable runnable){
        on(LoaderState.POSTINITIALIZATION,runnable);
    }

    public static void onInited(final @Nonnull Runnable runnable){
        on(LoaderState.AVAILABLE,runnable);
    }

    public static void onServerAboutToStart(final @Nonnull Runnable runnable){
        on(LoaderState.SERVER_ABOUT_TO_START,runnable);
    }

    public static void onServerStarting(final @Nonnull Runnable runnable){
        on(LoaderState.SERVER_STARTING,runnable);
    }

    public static void onServerStarted(final @Nonnull Runnable runnable){
        on(LoaderState.SERVER_STARTED,runnable);
    }

    public static void run(final @Nonnull LoaderState state){
        if(state.ordinal() >= LoaderState.ERRORED.ordinal()) throw new IllegalArgumentException();
        if(state.ordinal() < LoaderState.SERVER_ABOUT_TO_START.ordinal()) runSingle(state);
        else runMulti(state);
    }

    public static void restore(){
        consumed.forEach((state,queue)->{
            queues.get(state).addAll(queue);
            queue.clear();
        });
    }

    private static void runSingle(final @Nonnull LoaderState state){
        for(int i=0;i<=state.ordinal();i++){
            final LoaderState s = states[i];
            final List<Runnable> queue = queues.get(s);
            if(queue != null){
                queue.forEach(Runnable::run);
                queue.clear();
            }
        }
    }

    private static void runMulti(final @Nonnull LoaderState state){
        runSingle(LoaderState.AVAILABLE);
        for(int i=LoaderState.SERVER_ABOUT_TO_START.ordinal();i<=state.ordinal();i++){
            final LoaderState s = states[i];
            final List<Runnable> queue = queues.get(s);
            if(queue != null){
                queue.forEach(r -> consume(state,r));
                queue.clear();
            }
        }
    }
}
