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

package top.qiguaiaaaa.geocraft.mixin.finite.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.qiguaiaaaa.geocraft.api.block.ILayeredFluidHost;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author QiguaiAAAA
 */
@Mixin(value = BlockLiquid.class)
public abstract class BlockLiquidMixin extends Block{
    @Shadow @Final public static PropertyInteger LEVEL;

    public BlockLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "shouldSideBeRendered",at =@At("HEAD"),cancellable = true)
    public void 天圆地方$shouldSideBeRendered(@Nonnull final IBlockState thisState,
                                          @Nonnull final IBlockAccess access,
                                          @Nonnull final BlockPos pos,
                                          @Nonnull final EnumFacing side,
                                          @Nonnull final CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        final IBlockState toState;
        final Fluid fluid = FluidUtil.getFluid(toState = access.getBlockState(pos.offset(side)));
        if(fluid == null  //对方不是流体
                && toState.getMaterial() == this.material //又有流体的材质，例如 BOP 的水中珊瑚
        ){
            cir.setReturnValue(false); //不要渲染，保持原版的逻辑
            return;
        }

        final Fluid thisFluid = FluidUtil.getFluid(this);

        if (fluid == thisFluid) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(side == EnumFacing.UP || super.shouldSideBeRendered(thisState, access, pos, side));
    }

    @Inject(method = "shouldRenderSides",at =@At("HEAD"),cancellable = true)
    public void 天圆地方$shouldRenderSides(final @Nonnull IBlockAccess blockAccess,
                                       final @Nonnull BlockPos pos,
                                       final @Nonnull CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                final IBlockState state = blockAccess.getBlockState(pos.add(i, 0, j));

                final Fluid fluid = FluidUtil.getFluid(state);
                final Fluid thisFluid = FluidUtil.getFluid(this);

                if (fluid != thisFluid && !state.isFullBlock()) { //不同流体相邻的面应当渲染
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "randomDisplayTick",at =@At("HEAD"),cancellable = true)
    public void 天圆地方$randomDisplayTick(@Nonnull final IBlockState stateIn,
                                       @Nonnull final World worldIn,
                                       @Nonnull final BlockPos pos,
                                       @Nonnull final Random rand,
                                       @Nonnull final CallbackInfo ci) {
        ci.cancel();

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        BlockPos up = pos.up(),down = pos.down();

        if (this.material == Material.WATER) {
            if(stateIn.getBlock() instanceof BlockDynamicLiquid){
                if (rand.nextInt(64) == 0) {
                    worldIn.playSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
                }
            } else if (rand.nextInt(10) == 0) {
                worldIn.spawnParticle(EnumParticleTypes.SUSPENDED, x + rand.nextFloat(), y + rand.nextFloat(), z +rand.nextFloat(), 0, 0, 0);
            }
        }

        if (this.material == Material.LAVA && worldIn.getBlockState(up).getMaterial() == Material.AIR && !worldIn.getBlockState(up).isOpaqueCube()) {
            if (rand.nextInt(100) == 0) {
                double quanta = 8- stateIn.getValue(LEVEL);
                if(quanta<=0) quanta = 0.0;
                double rndX = x + rand.nextFloat();
                double rndY = y + stateIn.getBoundingBox(worldIn, pos).maxY*(quanta/8);
                double rndZ = z + rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.LAVA, rndX, rndY, rndZ, 0, 0, 0);
                worldIn.playSound(rndX, rndY, rndZ, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }

            if (rand.nextInt(200) == 0) {
                worldIn.playSound(x, y, z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }
        IBlockState downState = worldIn.getBlockState(down);
        if(!downState.isTopSolid()) return;
        final int rndNext =  downState.getBlock() instanceof ILayeredFluidHost ?10:100;
        if (rand.nextInt(rndNext) == 0) {
            Material material = worldIn.getBlockState(pos.down(2)).getMaterial();

            if (!material.blocksMovement() && !material.isLiquid()) {
                double rndX = x + rand.nextFloat();
                double rndY = y - 1.05;
                double rndZ = z + rand.nextFloat();

                if (this.material == Material.WATER) {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, rndX, rndY, rndZ, 0, 0, 0);
                } else {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, rndX, rndY, rndZ, 0, 0, 0);
                }
            }
        }
    }
}
