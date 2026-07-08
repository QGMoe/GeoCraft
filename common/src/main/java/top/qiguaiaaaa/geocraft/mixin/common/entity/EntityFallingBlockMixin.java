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

package top.qiguaiaaaa.geocraft.mixin.common.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import top.qiguaiaaaa.geocraft.api.block.IBlockFalling;

/**
 * @author QiguaiAAAA
 */
@Mixin(EntityFallingBlock.class)
public abstract class EntityFallingBlockMixin extends Entity {
    @Shadow
    private boolean dontSetBlock;
    @Shadow
    private IBlockState fallTile;

    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate",at = @At(value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/entity/item/EntityFallingBlock;tileEntityData:Lnet/minecraft/nbt/NBTTagCompound;",
            ordinal = 0),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onUpdate_FallingEnd(CallbackInfo info,
                                    @Local BlockPos blockpos1,
                                    @Local IBlockState iblockstate){
        if(this.dontSetBlock) return;
        final Block fallingBlock = this.fallTile.getBlock();
        if(fallingBlock instanceof BlockFalling) return;
        blockpos1 = blockpos1 == null? new BlockPos(this):blockpos1;
        iblockstate = iblockstate == null?world.getBlockState(blockpos1):iblockstate;
        if(fallingBlock instanceof IBlockFalling) {
            ((IBlockFalling)fallingBlock).onEndFalling(this.world,blockpos1,this.fallTile,iblockstate);
        }
    }

    @Inject(method = "onUpdate",at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/entity/item/EntityFallingBlock;setDead()V",
            ordinal = 3),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onUpdate_Broken(CallbackInfo info, @Local BlockPos blockpos1){
        if(!this.dontSetBlock) return;
        final Block fallingBlock = this.fallTile.getBlock();
        if(fallingBlock instanceof BlockFalling) return;
        blockpos1 = blockpos1 == null? new BlockPos(this):blockpos1;
        if(fallingBlock instanceof IBlockFalling) {
            ((IBlockFalling)fallingBlock).onBroken(this.world,blockpos1);
        }
    }
}
