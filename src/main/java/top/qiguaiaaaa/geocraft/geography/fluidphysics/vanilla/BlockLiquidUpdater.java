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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import top.qiguaiaaaa.geocraft.util.fluid.FluidOperationUtil;

import javax.annotation.Nonnull;

import static net.minecraft.block.BlockLiquid.LEVEL;

/**
 * 参考{@link BlockDynamicLiquid}和{@link BlockLiquid}实现
 * @author QiguaiAAAA
 */
public class BlockLiquidUpdater {
    protected final BlockDynamicLiquid liquid;
    protected final Material material;
    protected final Fluid fluid;

    public BlockLiquidUpdater(@Nonnull BlockDynamicLiquid liquid,@Nonnull Material material,@Nonnull Fluid fluid) {
        this.liquid = liquid;
        this.material = material;
        this.fluid = fluid;
    }

    /**
     * 对应方块状态是否阻挡{@link BlockLiquid}的流动
     * @param state 需要检测的方块状态
     * @return 若阻挡，则返回true
     */
    public static boolean isBlocked(final @Nonnull IBlockState state) {
        final Block block = state.getBlock();
        final Material material = state.getMaterial();

        if (!(block instanceof BlockDoor) && block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS) { //来自原版
            return material == Material.PORTAL || material == Material.STRUCTURE_VOID || material.blocksMovement();
        }
        if(material.isLiquid() && !(block instanceof BlockLiquid)) return false; // Fix #3 ，BOP 之类的模组可能会加入 Material 为流体但不是 BlockLiquid 的方块，要排除
        return true;
    }

    public BlockDynamicLiquid getBlock() {
        return liquid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public Material getMaterial() {
        return material;
    }

    public int getDepth(IBlockState state){
        if(state.getBlock() instanceof IFluidBlock) return -1;
        return state.getMaterial() == material ? state.getValue(LEVEL) : -1;
    }

    public void placeStaticBlock(World world, BlockPos pos,IBlockState curState){
        world.setBlockState(pos,
                BlockLiquid.getStaticBlock(material)
                        .getDefaultState().withProperty(LEVEL, curState.getValue(LEVEL)), Constants.BlockFlags.SEND_TO_CLIENTS);
    }

    /**
     * 指定液体是否可以流进指定方块位置
     * @param state 该方块位置的方块状态
     * @return 若能,则返回true,否则返回false
     */
    public boolean canFlowInto(final @Nonnull IBlockState state){
        final Material material = state.getMaterial();
        return material != this.material //不是同种流体
                && material != Material.LAVA //目标不是岩浆
                && !isBlocked(state);
    }

    /**
     * 尝试以指定的液体等级(meta)流进指定位置
     * @param world 所在世界
     * @param pos 流进的指定位置
     * @param state 流进位置的方块状态
     * @param level 期望流进的液体等级
     */
    public void tryFlowInto(@Nonnull World world,@Nonnull BlockPos pos,@Nonnull IBlockState state,int level){
        if (!canFlowInto(state)) return;
        if (state.getMaterial() != Material.AIR) {
            if (material == Material.LAVA) {
                FluidOperationUtil.triggerFluidMixEffects(world, pos);
            } else {
                if (state.getBlock() != Blocks.SNOW_LAYER)
                    state.getBlock().dropBlockAsItem(world, pos, state, 0);
            }
        }

        world.setBlockState(pos, liquid.getDefaultState().withProperty(LEVEL, level), Constants.BlockFlags.DEFAULT);
    }
}
