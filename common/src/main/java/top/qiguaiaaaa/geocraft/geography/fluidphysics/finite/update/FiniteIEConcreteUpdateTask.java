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

package top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.update;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingClassic;

import javax.annotation.Nonnull;
import java.util.Random;

import static net.minecraftforge.fluids.BlockFluidBase.LEVEL;

/**
 * @author QiguaiAAAA
 */
public final class FiniteIEConcreteUpdateTask extends FiniteFluidClassicUpdateTask {
    public static final @Nonnull FiniteFlowingClassic IE_CONCRETE_FLOWING_UPDATER = new FiniteFlowingClassic(IEContent.blockFluidConcrete);

    public FiniteIEConcreteUpdateTask(@Nonnull final BlockPos pos) {
        super(pos, IE_CONCRETE_FLOWING_UPDATER);
    }

    @Override
    public void onUpdate(@Nonnull final World world, @Nonnull final IBlockState curState, @Nonnull final Random rand) {
        this.state = curState;
        final int meta = state.getValue(LEVEL);
        final int quanta = IE_CONCRETE_FLOWING_UPDATER.quantaPerBlock-meta;
        final int timer = state.getValue(IEProperties.INT_16);

        if(timer >= Math.min(14, quanta)) {
            world.setBlockState(pos, getConcretingBlock(meta));
            for(@Nonnull final EntityLivingBase living : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))))
                living.addPotionEffect(new PotionEffect(IEPotions.concreteFeet, Integer.MAX_VALUE));
            return;
        } else {
            state = state.withProperty(IEProperties.INT_16, Math.min(15, timer+1));
            world.setBlockState(pos, state);
        }

        super.onUpdate(world,curState,rand);
    }

    /**
     * 混凝土固化，参考自沉浸工程
     * @param meta 当前数据值
     * @return 固化后的方块状态
     */
    private IBlockState getConcretingBlock(final int meta){
        if(meta >= 14)
            return IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.CONCRETE_SHEET.getMeta());
        else if(meta >= 10)
            return IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.CONCRETE_QUARTER.getMeta());
        else if(meta >= 6)
            return IEContent.blockStoneDecorationSlabs.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE.getMeta());
        else if(meta >= 2)
            return IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.CONCRETE_THREEQUARTER.getMeta());
        else
            return IEContent.blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE.getMeta());
    }
}
