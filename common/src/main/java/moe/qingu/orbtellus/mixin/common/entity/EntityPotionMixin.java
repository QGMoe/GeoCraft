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

package moe.qingu.orbtellus.mixin.common.entity;

import moe.qingu.orbtellus.api.atmosphere.AtmosphereFlowSource;
import moe.qingu.orbtellus.api.fluid.unit.MillibucketUnit;
import moe.qingu.orbtellus.api.laminarifer.request.FillLaminariferRequest;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.fluidphysics.FluidPhysicsMode;
import moe.qingu.orbtellus.api.fluidphysics.FluidPhysicsSystem;
import moe.qingu.orbtellus.api.fluid.unit.QBUnit;
import moe.qingu.orbtellus.api.util.math.vec.MBlockPos;
import moe.qingu.orbtellus.geography.fluidphysics.finite.flow.FiniteFlowings;
import moe.qingu.orbtellus.util.math.MathUtil;
import moe.qingu.orbtellus.util.wrappers.FiniteBlockLiquidWrapper;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
@Mixin(EntityPotion.class)
public abstract class EntityPotionMixin extends EntityThrowable {
    @Unique
    private static final FillLaminariferRequest 天圆地方$fillRequest = new FillLaminariferRequest();

    public EntityPotionMixin(final @Nonnull World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onImpact(Lnet/minecraft/util/math/RayTraceResult;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/EntityPotion;extinguishFires(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V",
                    ordinal = 0))
    private void 天圆地方$applyWaterImpact(final @Nonnull RayTraceResult res, final @Nonnull CallbackInfo ci){
        final BlockPos hitPos = res.getBlockPos();
        final BlockPos curPos = hitPos.offset(res.sideHit);
        long left = QBUnit.VOLUMES_1_TO_16.getLong(3);
        left -= 天圆地方$applyOnLayeredFluidHost(res.sideHit,curPos,天圆地方$randomizeAmount(left));
        left -= 天圆地方$applyOnLayeredFluidHostSide(res.sideHit,hitPos,天圆地方$randomizeAmount(left),4);
        final byte[] directions = MathUtil.randomizeByteArray(new byte[]{0,1,2,3},world.rand);
        final MBlockPos mutable = new MBlockPos();
        for(int i=0;i<4;i++){
            final EnumFacing side = EnumFacing.HORIZONTALS[directions[i]];
            final BlockPos pos = mutable.setPos(curPos).offsetM(side);
            left -= 天圆地方$applyOnLayeredFluidHostSide(side.getOpposite(),pos,天圆地方$randomizeAmount(left),3);
            if(left <= 0L) break;
        }
        if(left >0){
            final byte[] dir2 = MathUtil.randomizeByteArray(new byte[]{0,1,2,3,4,5,6,7},world.rand);
            for(int i=0;i<8;i++){
                final EnumFacing side = MathUtil.OUTER_DIR_FACINGS[dir2[i]];
                final BlockPos pos = mutable.setPos(
                        curPos.getX()+MathUtil.OUTER_DIRECTIONS[dir2[i]][0],
                        curPos.getY(),
                        curPos.getZ()+MathUtil.OUTER_DIRECTIONS[dir2[i]][1]);
                left -= 天圆地方$applyOnLayeredFluidHostSide(side.getOpposite(),pos,天圆地方$randomizeAmount(left),2);
                if(left <= 0L) break;
            }
        }
        final int quantaLeft = QBUnit.toQuantaAsInt(left);
        if(quantaLeft >0 && FluidPhysicsSystem.isFluidToBePhysical(FluidRegistry.WATER) && FluidPhysicsMode.getCurrentMode() == FluidPhysicsMode.FINITE){
            final FiniteBlockLiquidWrapper wrapper = new FiniteBlockLiquidWrapper(FiniteFlowings.WATER_FLOW,world,curPos);
            wrapper.fill(new FluidStack(FluidRegistry.WATER,quantaLeft* MillibucketUnit.QUANTA_VOLUME_INT),true);
        }
    }

    @Unique
    private long 天圆地方$randomizeAmount(final long qb){
        return qb > QBUnit.QUANTA_VOLUME*2? QBUnit.QUANTA_VOLUME*(world.rand.nextInt(2)+1):qb;
    }

    @Unique
    private long 天圆地方$applyOnLayeredFluidHostSide(final @Nonnull EnumFacing side, final @Nonnull BlockPos pos, final long left ,final int iter){
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if(block instanceof ILaminarifer){
            return 天圆地方$applyOnLayeredFluidHost(side,pos,state,left);
        }else if(iter > 0 && block.isPassable(world,pos)){
            return 天圆地方$applyOnLayeredFluidHostSide(EnumFacing.UP,pos.down(),left,iter - 1);
        }
        return 0L;
    }

    @Unique
    private long 天圆地方$applyOnLayeredFluidHost(final @Nonnull EnumFacing side, final @Nonnull BlockPos pos, final long left){
        final IBlockState state = world.getBlockState(pos);
        return 天圆地方$applyOnLayeredFluidHost(side,pos,state,left);
    }

    @Unique
    private long 天圆地方$applyOnLayeredFluidHost(final @Nonnull EnumFacing side, final @Nonnull BlockPos pos,final @Nonnull IBlockState state, final long left){
        final Block block = state.getBlock();
        if(block instanceof ILaminarifer){
            try (final FillLaminariferRequest request = 天圆地方$fillRequest.open()){
                return request.to(world,pos,state)
                        .target((ILaminarifer) block)
                        .specific(FluidRegistry.WATER)
                        .amount(left)
                        .side(side)
                        .source(AtmosphereFlowSource.SOURCE)
                        .fill(true);
            }
        }
        return 0L;
    }
}
