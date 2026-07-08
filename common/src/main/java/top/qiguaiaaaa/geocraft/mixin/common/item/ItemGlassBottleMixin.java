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

package top.qiguaiaaaa.geocraft.mixin.common.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.qiguaiaaaa.geocraft.api.event.EventFactory;

import java.util.List;

@Mixin(value = ItemGlassBottle.class)
public class ItemGlassBottleMixin extends Item {
    /**
     * @reason 插入事件
     */
    @Inject(method = "onItemRightClick",
            at = @At(value = "INVOKE",target = "Ljava/util/List;isEmpty()Z",ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    public void onItemRightClick_AreaEffectCloudEvent(World worldIn, EntityPlayer playerIn, EnumHand handIn,
                                                      CallbackInfoReturnable<ActionResult<ItemStack>> cir,
                                                      @Local List<EntityAreaEffectCloud> list,
                                                      @Local ItemStack itemstack) {
        if (!list.isEmpty()) {
            ActionResult<ItemStack> ret = EventFactory.onGlassBottleUseOnAreaEffectCloud(playerIn,itemstack,worldIn,list); // 事件插入
            if(ret != null){
                cir.setReturnValue(ret);
                cir.cancel();
            }
        }
    }

    /**
     * @reason 插入事件
     */
    @Inject(method = "onItemRightClick",
    at = @At(value = "INVOKE_ASSIGN",target =
            "Lnet/minecraft/item/ItemGlassBottle;rayTrace(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Z)Lnet/minecraft/util/math/RayTraceResult;",
    ordinal = 0),
    locals = LocalCapture.CAPTURE_FAILEXCEPTION,
    cancellable = true)
    public void onItemRightClick_UseOnFluid(World worldIn, EntityPlayer playerIn, EnumHand handIn,
                                            CallbackInfoReturnable<ActionResult<ItemStack>> cir,
                                            @Local ItemStack itemstack,
                                            @Local RayTraceResult raytraceresult) {
        ActionResult<ItemStack> ret = EventFactory.onGlassBottleUseOnFluid(playerIn,itemstack,worldIn,raytraceresult);
        if(ret != null){
            cir.setReturnValue(ret);
            cir.cancel();
        }
    }
}
