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

package top.qiguaiaaaa.geocraft.mixin.finite.mod.toughasnails;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.compat.toughasnails.TANCompat;
import top.qiguaiaaaa.geocraft.compat.toughasnails.TANPlayerDrinkInWorldEvent;
import toughasnails.api.thirst.WaterType;
import toughasnails.handler.PacketHandler;
import toughasnails.handler.thirst.DrinkHandler;
import toughasnails.network.message.MessageDrinkWaterInWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
@Mixin(value = DrinkHandler.class,remap = false)
public class DrinkHandlerMixin {
    @Shadow
    private static void applyDrink(final @Nonnull EntityPlayer player,
                                   final int thirstRestored,
                                   final float hydrationRestored,
                                   final float poisonChance) {
    }

    @Inject(method = "tryDrinkWaterInWorld",at = @At("HEAD"),cancellable = true)
    private static void 天圆地方$applyEffectsInWorld(final @Nonnull EntityPlayer player,
                                                     final boolean isClient,
                                                     @Nonnull final CallbackInfo ci){
        ci.cancel();
        final @Nullable Pair<BlockPos, WaterType> drinkStat = TANCompat.getRightClickedWater(player);
        if(drinkStat == null) return;
        if(isClient){
            final @Nonnull TANPlayerDrinkInWorldEvent event = new TANPlayerDrinkInWorldEvent(player,drinkStat.getRight(),drinkStat.getLeft());
            if(MinecraftForge.EVENT_BUS.post(event)) return;
            PacketHandler.instance.sendToServer(new MessageDrinkWaterInWorld());
            player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f, 1.0f);
            player.swingArm(EnumHand.MAIN_HAND);
        }else {
            final @Nonnull TANPlayerDrinkInWorldEvent.Server event = new TANPlayerDrinkInWorldEvent.Server((EntityPlayerMP) player,drinkStat.getRight(),drinkStat.getLeft());
            if(MinecraftForge.EVENT_BUS.post(event)) return;
            if(event.hasResult() && event.getResult() == Event.Result.DENY) return;
            applyDrink(player, event.getThirst(), event.getHydration(), event.getPoisonChance());
        }
    }

}
