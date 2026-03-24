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

import com.google.common.collect.Sets;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidRegistry;
import top.qiguaiaaaa.geocraft.compat.GeoCompatInfo;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.util.mixinapi.FluidSettable;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class MixinHandler {

    private static final GeoCompatInfo BRIGO_COMPAT = new GeoCompatInfo("brigo","top.qiguaiaaaa.geocraft.command.BrigoCompat");

    public static final Set<GeoCompatInfo> COMPATS_UNDER_FINITE = Collections.unmodifiableSet(Sets.newHashSet(
            new GeoCompatInfo("ic2",null, "mixins/compat/ic2/mixins.geocraft_finite.json")
                    .enableIf(FluidPhysicsConfig.enableSupportForIC2::getValue),
            new GeoCompatInfo("immersiveengineering",null, "mixins/compat/immersiveengineering/mixins.geocraft_finite.json")
                    .enableIf(FluidPhysicsConfig.enableSupportForIE::getValue),
            new GeoCompatInfo("toughasnails",
                    "top.qiguaiaaaa.geocraft.compat.toughasnails.TANCompat",
                    "mixins/compat/toughasnails/mixins.geocraft_finite.json")
                    .enableIf(FluidPhysicsConfig.enableSupportForTAN::getValue),
            BRIGO_COMPAT
    ));

    public static final Set<GeoCompatInfo>[] FLUID_PHYSICS_TO_COMPATS = new Set[]{Sets.newHashSet(BRIGO_COMPAT),Sets.newHashSet(BRIGO_COMPAT),COMPATS_UNDER_FINITE};

    public static void linkLiquidWithFluid(){
        if(Blocks.FLOWING_WATER instanceof FluidSettable){
            ((FluidSettable) Blocks.FLOWING_WATER).天圆地方$setCorrespondingFluid(FluidRegistry.WATER);
            ((FluidSettable) Blocks.FLOWING_LAVA).天圆地方$setCorrespondingFluid(FluidRegistry.LAVA);
        }
        if(Blocks.WATER instanceof FluidSettable){
            ((FluidSettable) Blocks.WATER).天圆地方$setCorrespondingFluid(FluidRegistry.WATER);
            ((FluidSettable) Blocks.LAVA).天圆地方$setCorrespondingFluid(FluidRegistry.LAVA);
        }
    }

    @Nonnull
    public static List<String> getCompatMixins(){
        final @Nonnull FluidPhysicsMode mode = FluidPhysicsMode.getCurrentMode();
        final List<String> mixins = new ArrayList<>();
        FLUID_PHYSICS_TO_COMPATS[mode.ordinal()].stream()
                .filter(GeoCompatInfo::isValid)
                .forEach(compat -> compat.getMixins.accept(mixins));
        return mixins;
    }
}
