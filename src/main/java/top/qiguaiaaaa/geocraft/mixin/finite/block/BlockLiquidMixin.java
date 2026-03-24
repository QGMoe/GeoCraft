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

package top.qiguaiaaaa.geocraft.mixin.finite.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;

import static net.minecraft.block.BlockLiquid.LEVEL;

@Mixin(value = BlockLiquid.class)
public abstract class BlockLiquidMixin extends Block {
    public BlockLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "getFlow",at = @At("HEAD"),cancellable = true)
    protected void 天圆地方$getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Vec3d> cir){
        if(state.getBlock() instanceof BlockStaticLiquid){
            cir.setReturnValue(new Vec3d(0,0,0));
            cir.cancel();
        }
    }
    /**
     * @author QiguaiAAAA
     * @reason make Water disappear when mixing
     */
    @Inject(method = "checkForMixing",at = @At("HEAD"),cancellable = true)
    public void 天圆地方$checkForMixing(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        if (material == Material.LAVA) {
            boolean flag = false;
            EnumFacing waterFacing = null;
            for (EnumFacing facing : EnumFacing.values()) {
                if(facing == EnumFacing.DOWN) continue;
                if (worldIn.getBlockState(pos.offset(facing)).getMaterial() == Material.WATER) {
                    flag = true;
                    waterFacing = facing;
                    break;
                }
            }

            if (flag) {
                int meta = state.getValue(LEVEL);
                if (meta == 0) {
                    worldIn.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.OBSIDIAN.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                }else{
                    worldIn.setBlockState(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.COBBLESTONE.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                }
                cir.setReturnValue(true);
                BlockPos waterPos = pos.offset(waterFacing);
                IBlockState waterState = worldIn.getBlockState(waterPos);
                int waterQuanta = FluidUtil.getFluidQuanta(worldIn,waterPos,waterState);
                int newWaterQuanta = waterQuanta-1;
                FluidOperationUtil.setQuanta(worldIn,waterPos,waterState,newWaterQuanta);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    /**
     * @reason 变更RayTrace时检测液体的逻辑，允许选中LEVEL != 0的液体
     */
    @Redirect(method = "canCollideCheck",at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I",ordinal = 0))
    public int 天圆地方$redirectCollideCheck(final @Nonnull Integer instance){
        return 0;
    }

    @Shadow
    protected void triggerMixEffects(World worldIn, BlockPos pos) {
    }
}
