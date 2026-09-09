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

package moe.qingu.orbtellus.api.laminarifer.flow;

import moe.qingu.orbtellus.api.laminarifer.ILaminarifer;
import moe.qingu.orbtellus.api.laminarifer.ImpetusPulse;
import moe.qingu.orbtellus.api.laminarifer.LaminariferModelBuffer;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;
import moe.qingu.orbtellus.api.util.APIMathUtil;
import moe.qingu.orbtellus.api.util.math.vec.MBlockPos;
import moe.qingu.orbtellus.api.util.modifier.BlockFlagModifiers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author QGMoe
 */
public class AverageFlow implements AutoCloseable, Iterator<FlowChoice> {
    static final long _多余流体量分配给粗粒度流动选择忍受度_ = 2L; //todo: 改成可配置项
    protected final MBlockPos mPos = new MBlockPos();
    public @Nonnull LaminariferModelBuffer centralModel;

    protected World world;
    protected BlockPos pos;
    protected Fluid fluid;
    protected @Nullable NBTTagCompound fluidTag;
    protected long pulse;
    protected IFlowSource<?> source;
    protected long blockFlagModifier;

    protected long minLayers;
    protected FlowChoice[] choices = new FlowChoice[]{new FlowChoice(),new FlowChoice(),new FlowChoice(),new FlowChoice()};
    protected int choicesCot;
    protected int iteratorIndex;

    public long finalLayers;
    public long extraAmountInQB;

    public AverageFlow(){
        this.centralModel = new LaminariferModelBuffer();
    }

    public AverageFlow(final @Nonnull LaminariferModelBuffer model){
        this.centralModel = model;
    }

    @Nonnull
    public final AverageFlow at(final @Nonnull World world,final @Nonnull BlockPos pos){
        this.world = world;
        this.pos = pos;
        return this;
    }

    @Nonnull
    public final AverageFlow fluid(final @Nonnull Fluid fluid){
        this.fluid = fluid;
        this.fluidTag = null;
        return this;
    }

    @Nonnull
    public final AverageFlow fluid(final @Nonnull Fluid fluid,final @Nullable NBTTagCompound tag){
        this.fluid = fluid;
        this.fluidTag = tag;
        return this;
    }

    @Nonnull
    public final AverageFlow impetus(final float pressure, final float time){
        this.pulse = ImpetusPulse.of(pressure, time);
        return this;
    }

    @Nonnull
    public final AverageFlow source(final @Nullable IFlowSource<?> source){
        this.source = source;
        return this;
    }

    @Nonnull
    public final AverageFlow blockFlagModifier(final long blockFlagModifier){
        this.blockFlagModifier = blockFlagModifier;
        return this;
    }

    @Nonnull
    public final AverageFlow minLayers(final long min){
        this.minLayers = min;
        return this;
    }

    /**
     * 新增一个基于载流方块的流动选择
     * @param side 方向
     * @param stateAtThisSide 该方向对应的方块状态
     * @param laminariferAtThisSide 该方向对应的载流方块
     * @return 新增的流动选择
     */
    @Nonnull
    public final FlowChoice addChoice(final @Nonnull EnumFacing side,
                                       final @Nonnull IBlockState stateAtThisSide,
                                       final @Nonnull ILaminarifer laminariferAtThisSide){
        return choices[choicesCot++].of(world,mPos.setPos(pos).offsetM(side),stateAtThisSide,side,laminariferAtThisSide,fluid,fluidTag);
    }

    /**
     * 新增一个基于载流方块的流动选择
     * @param side 方向
     * @param stateAtThisSide 该方向对应的方块状态
     * @return 新增的流动选择
     */
    @Nonnull
    public final FlowChoice addChoice(final @Nonnull EnumFacing side, final @Nonnull IBlockState stateAtThisSide){
        return choices[choicesCot++].of(world,mPos.setPos(pos).offsetM(side),stateAtThisSide,side,(ILaminarifer) stateAtThisSide.getBlock(),fluid,fluidTag);
    }

    /**
     * 新增一个空气的流动选择
     * @param side 方向
     * @return 新增的流动选择
     */
    @Nonnull
    public final FlowChoice addAirChoice(final @Nonnull EnumFacing side){
        return choices[choicesCot++].of(side);
    }

    public final boolean isLastChoiceAvailable(){
        final FlowChoice choice = choices[choicesCot-1];
        try {
            if(choice.model.isFull()) return false;
            if(!choice.canFill(world,mPos.setPos(pos).offsetM(choice.direction), fluid, fluidTag, source)) return false;
            choice.addedLayers = 1L;
            return choice.getNewHeight() <= centralModel.getHeight() - centralModel.heightPerLayer;
        }finally {
            choice.addedLayers = 0L;
        }
    }

    @Nonnull
    public final FlowChoice removeLastChoice(){
        return choices[--choicesCot];
    }

    // ------- AutoClosable 重置状态 -------

    @Override
    public final void close(){
        this.world = null;
        this.pos = null;
        this.fluid = null;
        this.fluidTag = null;
        this.pulse = 0L;
        this.source = null;
        this.blockFlagModifier = BlockFlagModifiers.KEEP;
        this.choicesCot = 0;
        this.iteratorIndex = 0;

        this.minLayers = 0L;
        this.finalLayers = 0L;
        this.extraAmountInQB = 0L;
    }

    // 迭代器 Iterator

    @Override
    public final boolean hasNext() {
        return iteratorIndex < choicesCot;
    }

    @Override
    @Nonnull
    public final FlowChoice next() {
        if(!hasNext()) throw new NoSuchElementException();
        return choices[iteratorIndex++];
    }

    public final long applyCurrentChoice(){
        final FlowChoice choice = choices[iteratorIndex-1];
        return choice.apply(world,mPos.setPos(pos).offsetM(choice.direction),fluid,fluidTag,pulse,source,blockFlagModifier);
    }

    // 核心求解算法

    public final boolean resolve() {
        if (minLayers >= centralModel.currentLayers || choicesCot <= 0) { // 没有任何需要计算的东西
            finalLayers = centralModel.currentLayers;
            return false;
        }

        // 先进行最基础的扫描，用于之后分流判断
        final long $最大搬运次数 = 计算最大搬运次数();

        // 根据规模大小分流，小规模用朴素算法更快
        final long $消耗流体量;
        if ($最大搬运次数 <= 32L) $消耗流体量 = 逐层分配算法();
        else $消耗流体量 = 双二分分配算法();

        final long $中心剩余流体量 = centralModel.getAmountInQB() - $消耗流体量;
        this.finalLayers = $中心剩余流体量 / centralModel.amountInQBPerLayer;
        long $多余流体量 = $中心剩余流体量 % centralModel.amountInQBPerLayer;

        // 尝试将多余流体量概率性地分配出去
        if ($多余流体量 > 0L) $多余流体量 = 分配多余流体量($多余流体量);
        extraAmountInQB = $多余流体量; //分配不出去的多余量记在中心
        return this.finalLayers < centralModel.currentLayers;
    }

    protected final long 计算最大搬运次数() {
        long $假设一层一层搬抬高邻居需要的次数上限 = 0L;
        long $邻居最小每层流体量 = Long.MAX_VALUE;
        for (int i=0;i<choicesCot;i++) {
            final FlowChoice $流动选择 = choices[i];
            final LaminariferModelBuffer $邻居模型 = $流动选择.model;
            long $该邻居一层一层抬高最大次数上限 = Math.min($邻居模型.maxLayers - $邻居模型.currentLayers, // 容量限制
                    Math.floorDiv(centralModel.getHeight() - $邻居模型.getHeight(), $邻居模型.heightPerLayer)); //不能超过中心初始高度限制
            $该邻居一层一层抬高最大次数上限 = Math.max($该邻居一层一层抬高最大次数上限, 0L);
            $假设一层一层搬抬高邻居需要的次数上限 += $该邻居一层一层抬高最大次数上限;
            $邻居最小每层流体量 = Math.min($邻居最小每层流体量, $邻居模型.amountInQBPerLayer);
        }

        final long $中心最大可消费层数 = centralModel.currentLayers - minLayers;
        return Math.min($假设一层一层搬抬高邻居需要的次数上限,
                $中心最大可消费层数 * centralModel.amountInQBPerLayer / $邻居最小每层流体量); //将中心全部分配到每层流体量最小的流动选择这种最坏情况下要抬多少次
    }

    /**
     * 将多余流体量尝试分配出去，用于概率层
     * @param $多余流体量 多余的流体量
     * @return 剩余的多余流体量
     */
    protected final long 分配多余流体量(final long $多余流体量){
        FlowChoice $最佳选择 = null;
        long $最佳选择分配后最低高度 = Long.MAX_VALUE;
        //选择可以分配的邻居中，假设分配一层后高度最低的
        for (int i = 0; i < choicesCot; i++) {
            final FlowChoice $流动选择 = choices[i];
            final LaminariferModelBuffer model = $流动选择.model;
            if ($流动选择.getNewLayers() >= model.maxLayers) continue;
            if ($流动选择.getNewHeight() + model.heightPerLayer > finalLayers * centralModel.heightPerLayer + centralModel.emptyHeight)
                continue; //保证分配出去的概率层高度也不超过中心最终高度
            if (model.amountInQBPerLayer <= $多余流体量) continue; //每层流体量过小，概率会大于 1。由于一些复杂的数学因素，单次分配后可能出现这种非最佳分配。
            if (model.amountInQBPerLayer > _多余流体量分配给粗粒度流动选择忍受度_ * centralModel.amountInQBPerLayer) continue; //避免每层流体量过大，导致流体流动过程中系统总流体量震动方差变大
            final long $模拟分配后高度 = $流动选择.getNewHeight() + model.heightPerLayer;
            if ($模拟分配后高度 < $最佳选择分配后最低高度) {
                $最佳选择分配后最低高度 = $模拟分配后高度;
                $最佳选择 = $流动选择;
            }
        }
        if ($最佳选择 != null) {
            $最佳选择.extraAmountInQB = $多余流体量;
            return 0L;
        }else return $多余流体量;
    }

    /**
     * 朴素算法，一层一层分配，在小规模下不用二分非常快
     * @return 中心消耗的流体量
     */
    protected final long 逐层分配算法() {
        final long $中心总流体量 = centralModel.getAmountInQB();
        final long $中心最低流体量 = minLayers * centralModel.amountInQBPerLayer;

        long $中心消耗流体量 = 0L;
        int $遍历随机偏移 = 0;
        while (true) {
            FlowChoice $最低邻居 = null;
            long $最低邻居高度 = Long.MAX_VALUE;
            // 遍历当前邻居，寻找最低且可以新添一层的
            for (int i = 0; i < choicesCot; i++) {
                final FlowChoice choice = choices[(i+$遍历随机偏移)%choicesCot];
                final LaminariferModelBuffer model = choice.model;
                if (choice.getNewLayers() >= model.maxLayers) continue;
                final long $搬运后的中心剩余流体量;
                if (($搬运后的中心剩余流体量 = $中心总流体量 - $中心消耗流体量 - model.amountInQBPerLayer) < $中心最低流体量) continue;
                final long $搬运后的中心高度 = ($搬运后的中心剩余流体量 / centralModel.amountInQBPerLayer) * centralModel.heightPerLayer + centralModel.emptyHeight;
                final long $目前邻居高度 = choice.getNewHeight();
                if ($目前邻居高度 + model.heightPerLayer > $搬运后的中心高度) continue;
                if ($目前邻居高度 < $最低邻居高度) {
                    $最低邻居 = choice;
                    $最低邻居高度 = $目前邻居高度;
                }
            }
            if ($最低邻居 == null) break;
            $最低邻居.addedLayers += 1L;
            $中心消耗流体量 += $最低邻居.model.amountInQBPerLayer;
            $遍历随机偏移++;
        }
        return $中心消耗流体量;
    }

    /**
     * 先二分中心的最终最低高度，来计算预算，从而找到分配量接近最优的分配局面
     * 然后尝试两个方案：一个是预算恰大于等于需求（这不一定存在），一个是需求恰大于等于预算的点（可以证明这一定存在）
     * 对于前者，直接分配满，毫无疑问
     * 对于后者，进行进一步二分高度，批量分配，并在无法批量分配的时候退回朴素算法
     * @return 中心消耗的流体量
     */
    protected final long 双二分分配算法() {
        final long $需求恰大于等于预算的层数 = 计算需求恰大于等于预算的终态中心层数();
        final long $预算恰大于等于需求候选的中心高度 = centralModel.emptyHeight + ($需求恰大于等于预算的层数 - 1L) * centralModel.heightPerLayer;
        final long $预算恰大于等于需求候选的实际消耗流体量 = $需求恰大于等于预算的层数 - 1L < minLayers ? Long.MIN_VALUE : 计算在指定高度限制下邻居的总需求流体量($预算恰大于等于需求候选的中心高度);

        final long $需求恰大于等于预算候选的实际消耗流体量 = 均匀填充($需求恰大于等于预算的层数); //先算需求恰大于等于预算的情况，因为这个需要有复杂的分配，而预算大于需求不需要
        // 取分配量最优的
        if ($需求恰大于等于预算候选的实际消耗流体量 >= $预算恰大于等于需求候选的实际消耗流体量) return $需求恰大于等于预算候选的实际消耗流体量;
        for (int i = 0; i < choicesCot; i++) {
            final FlowChoice $流动选择 = choices[i];
            $流动选择.addedLayers = 计算该邻居在一定限制下还能增长的层数($流动选择, $预算恰大于等于需求候选的中心高度);
        }
        return $预算恰大于等于需求候选的实际消耗流体量;
    }

    /**
     * 二分中心的最终最低高度，寻找需求恰大于等于预算的终态最低中心高度
     * @return 最终二分得到的终态最低中心高度
     */
    protected final long 计算需求恰大于等于预算的终态中心层数(){
        //确定最大可能的流体需求量
        final long $总需求流体量 = 计算在指定高度限制下邻居的总需求流体量(centralModel.getMaxHeight());

        long $二分区间左侧 = Math.max(
                minLayers,
                centralModel.currentLayers - $总需求流体量 / centralModel.amountInQBPerLayer //按最大可能需求算中心层数的最低值
        );
        long $二分区间右侧 = centralModel.currentLayers;
        if (需求是否大于等于预算($二分区间左侧)) $二分区间右侧 = $二分区间左侧; //尝试一下最左边，如果最左边也大于就直接这样了
        while ($二分区间左侧 < $二分区间右侧) {
            final long $中点 = $二分区间左侧 + ($二分区间右侧 - $二分区间左侧) / 2;
            if (需求是否大于等于预算($中点)) $二分区间右侧 = $中点;
            else $二分区间左侧 = $中点 + 1L;
        }
        return $二分区间右侧;
    }

    /**
     * 假设终态中心层数为指定层数，邻居将自己的高度提高到最多终态中心层数对应的高度所需要的需求流体量是否大于等于中心消费的流体量也就是预算
     * @param $假设中心层数 假设地终态中心层数
     * @return 如果满足条件，则返回 true
     */
    protected final boolean 需求是否大于等于预算(final long $假设中心层数) {
        final long $预算 = (centralModel.currentLayers - $假设中心层数) * centralModel.amountInQBPerLayer;
        final long $终态中心高度 = centralModel.emptyHeight + $假设中心层数 * centralModel.heightPerLayer;
        long $邻居总需求 = 0L;
        for (int i=0;i<choicesCot;i++) {
            final FlowChoice $流动选择 = choices[i];
            $邻居总需求 += 计算该邻居在一定限制下还能增长的层数($流动选择, $终态中心高度) * $流动选择.model.amountInQBPerLayer;
            if ($邻居总需求 >= $预算) return true;
        }
        return $邻居总需求 >= $预算;
    }

    /**
     * 需求大于等于预算情况，使用二分寻找目标高度进行批量填充，并在二分失效的时候退回单层分配的朴素算法
     * @param $终态中心层数 第一个二分算出来的需求恰大于预算时的中心剩余的最低层数
     * @return 总分配量
     */
    protected final long 均匀填充(final long $终态中心层数) {
        final long $高度限制 = centralModel.emptyHeight + $终态中心层数 * centralModel.heightPerLayer;
        final long $预算 = (centralModel.currentLayers - $终态中心层数) * centralModel.amountInQBPerLayer;
        if ($预算 <= 0L) return 0L;
        //如果预算大于等于需求，那么肯定能全部填充满。考虑到这个方法的调用限制，只有可能是预算等于需求
        {
            final long $邻居总需求 = 计算在指定高度限制下邻居的总需求流体量($高度限制);
            if ($邻居总需求 <= $预算) {
                long $中心消耗流体量 = 0L;
                for (int i=0;i<choicesCot;i++) {
                    final FlowChoice $流动选择 = choices[i];
                    $流动选择.addedLayers = 计算该邻居在一定限制下还能增长的层数($流动选择, $高度限制);
                    $中心消耗流体量 += $流动选择.getAddedAmountInQB();
                }
                return $中心消耗流体量;
            }
        }
        long $未分配流体量 = $预算;
        int $遍历随机偏移 = 0;
        /// 二分最佳高度，然后填充。如果二分完之后发现无法进一步分配，退回朴素的单层搬运算法，可以证明单层搬运最多不会超过 {@link choicesCot} * ({@link choicesCot} -1) / 2 次
        while (true) {
            int bitmap = 0; //表示是否可以参与分配的 bitmap，最多只用低四位
            FlowChoice $最低邻居 = null;
            long $最低邻居高度 = Long.MAX_VALUE;
            // 寻找目前所有还可以至少分配一层的邻居，并顺便得出最低的流动选择，用于算法降级
            boolean $仍可继续分配 = false;
            for (int i = 0; i < choicesCot; i++) {
                final int $邻居编号 = (i + $遍历随机偏移) % choicesCot;
                final FlowChoice $流动选择 = choices[$邻居编号];
                if ($流动选择.getNewLayers() >= $流动选择.model.maxLayers) continue;
                if (计算该邻居在一定限制下还能增长的层数($流动选择, $高度限制) <= $流动选择.addedLayers) continue; //已经加满了
                if ($流动选择.model.amountInQBPerLayer > $未分配流体量) continue; //剩余预算无法再给这个邻居分配新的一层了
                bitmap |= (1<<$邻居编号);
                $仍可继续分配 = true;
                final long $目前邻居高度 = $流动选择.getNewHeight();
                if ($目前邻居高度 < $最低邻居高度) {
                    $最低邻居 = $流动选择;
                    $最低邻居高度 = $目前邻居高度;
                }
            }
            if (!$仍可继续分配) break;
            assert $最低邻居 != null;
            // 二分当前未分配流体量可以给每个可分配邻居后能增加到的目标高度，然后尝试快速批量分配
            long $二分目标水位区间左侧 = $最低邻居高度;
            long $二分目标水位区间右侧 = Math.min($高度限制,
                    $最低邻居高度 + ($未分配流体量 / $最低邻居.model.amountInQBPerLayer + 1L) * $最低邻居.model.heightPerLayer); //极端情况，全部分配给最低的邻居
            // 如果最低的邻居每层流体密度很大，高度会压得很低，这时候如果其他选择有密度更大的，那也在范围内。如果有密度更小的，那就不是最佳分配选择。所以可以直接基于最低的邻居计算上限高度
            final long $当前可分配邻居总分配量 = 计算可分配邻居总分配量(bitmap);
            //找到最后一个将所有可分配邻居的高度尽可能抬高到不高于指定高度后进一步分配的流体量小于等于未分配流体量的高度
            while ($二分目标水位区间左侧 < $二分目标水位区间右侧) {
                final long $区间中点 = $二分目标水位区间左侧 + ($二分目标水位区间右侧 - $二分目标水位区间左侧 + 1L) / 2L;
                if (将所有可分配邻居的高度尽可能抬高到不高于指定高度后总分配的流体量(bitmap, $高度限制, $区间中点) - $当前可分配邻居总分配量 <= $未分配流体量) $二分目标水位区间左侧 = $区间中点;
                else $二分目标水位区间右侧 = $区间中点 - 1;
            }
            boolean $成功分配 = false;
            for (int i = 0; i < choicesCot; i++) {
                if ((bitmap & (1 << i)) == 0) continue;
                final FlowChoice $流动选择 = choices[i];
                final LaminariferModelBuffer model = $流动选择.model;
                final long $分配后层数 = APIMathUtil.clamp(
                        Math.floorDiv($二分目标水位区间左侧 - model.emptyHeight, model.heightPerLayer) - model.currentLayers,
                        $流动选择.addedLayers,
                        计算该邻居在一定限制下还能增长的层数($流动选择, $高度限制));
                if ($分配后层数 > $流动选择.addedLayers) {
                    $未分配流体量 -= ($分配后层数 - $流动选择.addedLayers) * model.amountInQBPerLayer;
                    $流动选择.addedLayers = $分配后层数;
                    $成功分配 = true;
                }
            }
            //没有任何一个邻居能够被分配，说明目前的剩余量不够可分配邻居都往上涨至少一层，需要回到逐层搬运的朴素算法
            if (!$成功分配) {
                $最低邻居.addedLayers += 1L;
                $未分配流体量 -= $最低邻居.model.amountInQBPerLayer;
                $遍历随机偏移++;
            }
        }
        return $预算-$未分配流体量;
    }

    /**
     * 用于分配的目标高度二分，模拟将所有还可以分配的邻居在不考虑预算的情况下抬升到目标高度的需求
     * @param bitmap 用于判断哪些邻居可以分配的 bitmap
     * @param $高度限制 中心产生的高度限制
     * @param $指定高度 二分的高度中点
     * @return 流体量需求
     */
    protected final long 将所有可分配邻居的高度尽可能抬高到不高于指定高度后总分配的流体量(final int bitmap, final long $高度限制, final long $指定高度) {
        long $累计流体量 = 0L;
        for (int i = 0; i < choicesCot; i++) {
            if ((bitmap & (1 << i)) == 0) continue;
            final FlowChoice $流动选择 = choices[i];
            final LaminariferModelBuffer model = $流动选择.model;
            final long $抬高后累计已抬高层数 = APIMathUtil.clamp(
                    Math.floorDiv($指定高度 - model.emptyHeight, model.heightPerLayer) - model.currentLayers,
                    $流动选择.addedLayers,
                    计算该邻居在一定限制下还能增长的层数($流动选择, $高度限制));
            $累计流体量 += $抬高后累计已抬高层数 * model.amountInQBPerLayer;
        }
        return $累计流体量;
    }

    protected final long 计算在指定高度限制下邻居的总需求流体量(final long $高度限制) {
        long $总需求流体量 = 0L;
        for (int i=0;i<choicesCot;i++) {
            final FlowChoice $流动选择 = choices[i];
            $总需求流体量 += 计算该邻居在一定限制下还能增长的层数($流动选择, $高度限制) * $流动选择.model.amountInQBPerLayer;
        }
        return $总需求流体量;
    }

    protected final long 计算可分配邻居总分配量(final int bitmap) {
        long $总分配量 = 0L;
        for (int i=0;i<choicesCot;i++) {
            if((bitmap & (1 << i)) == 0) continue;
            final FlowChoice $流动选择 = choices[i];
            $总分配量 += $流动选择.getAddedAmountInQB();
        }
        return $总分配量;
    }

    protected static long 计算该邻居在一定限制下还能增长的层数(final @Nonnull FlowChoice $流动选择, final long $高度限制) {
        final LaminariferModelBuffer model = $流动选择.model;
        return APIMathUtil.clamp(
                Math.floorDiv($高度限制 - model.emptyHeight, model.heightPerLayer) - model.currentLayers, //按高度限制计算层数
                0L,
                model.maxLayers - model.currentLayers); //按最大层数限制计算层数
    }

    // Getter

    public final World getWorld() {
        return world;
    }

    public final BlockPos getPosition() {
        return pos;
    }
}
