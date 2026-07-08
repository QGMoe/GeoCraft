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

package top.qiguaiaaaa.geocraft.mixin.finite.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
@Mixin(value = EntityBoat.class)
public abstract class EntityBoatMixin extends Entity {
    public EntityBoatMixin(final @Nonnull World worldIn) {
        super(worldIn);
    }

    /**
     * @reason 修改船的下沉判定，让船更难下沉
     */
    @ModifyConstant(method = "getUnderwaterStatus",constant = @Constant(doubleValue = 0.001d),allow = 1)
    private double 天圆地方$modifyUnderwater(final double original){
        return FluidPhysicsConfig.BOAT_SINKING_THRESHOLD.getValue();
    }

    /**
     * @reason 让船的浮力计算更符合视觉情况
     */
    @Redirect(method = "checkInWater",at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(D)I",ordinal = 1))
    private int 天圆地方$modifyMaxY(final double original){
        return MathHelper.ceil(this.getEntityBoundingBox().maxY);
    }
}
