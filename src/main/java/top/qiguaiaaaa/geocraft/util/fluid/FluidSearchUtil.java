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

package top.qiguaiaaaa.geocraft.util.fluid;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.GeoCraft;
import top.qiguaiaaaa.geocraft.api.setting.GeoFluidSetting;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.api.util.LayeredFluidHostUtil;
import top.qiguaiaaaa.geocraft.api.util.math.PlaceChoice;
import top.qiguaiaaaa.geocraft.configs.FluidPhysicsConfig;
import top.qiguaiaaaa.geocraft.mixin.common.block.BlockFluidBaseAccessor;
import top.qiguaiaaaa.geocraft.util.math.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class FluidSearchUtil {
    private static final ThreadLocal<Queue<FluidSourceSearchNode>> SearchSourceQueue = ThreadLocal.withInitial(LinkedList::new);
    private static final ThreadLocal<Queue<FluidSearchNode>> SearchQueue = ThreadLocal.withInitial(LinkedList::new);
    private static final ThreadLocal<Set<BlockPos>> SearchVisitedSet = ThreadLocal.withInitial(HashSet::new);
    private static final Set<BlockPos> EMPTY_BLOCKPOS_SET = Collections.emptySet();
    public static final int[][] DIRS4 = {
            { 1, 0}, { -1, 0}, {0, 1}, {0, -1}};
    public static final int[][] DIRS6 = {
            {1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}
    };

    @Nonnull
    public static EnumFacing getFacingFromDIRS6(@Nonnull int[] dir){
        for(EnumFacing facing:EnumFacing.values()){
            Vec3i vec = facing.getDirectionVec();
            if(vec.getX() == dir[0] && vec.getY() == dir[1] && vec.getZ() == dir[2]) return facing;
        }
        throw new IllegalArgumentException(Arrays.toString(dir));
    }

    /**
     * 广度优先搜索搜寻原版流体源
     * @param world 世界
     * @param startPos 起始位置
     * @param material 流体类型
     * @param ignoreSameY 是否不考虑同层液体
     * @param ignoreLevel 是否忽略流体等级进行搜索
     * @param maxIterations 最大迭代次数
     * @param sameLevelIterationLimit 最大同等级搜索次数
     * @return 一个可能的流体源
     */
    public static Optional<BlockPos> findSource(@Nonnull World world,
                                                @Nonnull BlockPos startPos,
                                                @Nonnull Material material,
                                                boolean ignoreSameY,
                                                boolean ignoreLevel,
                                                int maxIterations,
                                                int sameLevelIterationLimit){
        return findSourceIterate(world,startPos,material,ignoreSameY,ignoreLevel,maxIterations,sameLevelIterationLimit);
    }

    /**
     * 广度优先搜索流体源（支持MOD）
     * 流体方块需要是
     * @param world 世界
     * @param startPos 起始位置
     * @param fluid 流体类型
     * @param ignoreSameY 是否不考虑同层流体
     * @param ignoreLevel 是否忽略流体等级进行搜索
     * @param maxIterations 最大迭代次数
     * @param sameQuantaIterationLimit 最大同量搜索次数
     * @return 一个可能的流体源
     */
    public static Optional<BlockPos> findSource(@Nonnull World world,
                                                @Nonnull BlockPos startPos,
                                                @Nonnull Fluid fluid,
                                                boolean ignoreSameY,
                                                boolean ignoreLevel,
                                                int maxIterations,
                                                int sameQuantaIterationLimit){
        if(fluid == FluidRegistry.WATER){
            findSource(world,startPos,Material.WATER,ignoreSameY,ignoreLevel,maxIterations,sameQuantaIterationLimit);
        }else if(fluid == FluidRegistry.LAVA){
            findSource(world,startPos,Material.LAVA,ignoreSameY,ignoreLevel,maxIterations,sameQuantaIterationLimit);
        }
        try{
            return findSourceIterate(world,startPos,fluid,ignoreSameY,ignoreLevel,
                    (fluid.getDensity()>0)?-1:1,
                    ((BlockFluidBaseAccessor)(fluid.getBlock())).getQuantaPerBlock(),
                    maxIterations,sameQuantaIterationLimit);
        }catch (Throwable e){
            GeoCraft.getLogger().error(e);
        }
        return Optional.empty();
    }

    /**
     * 广度优先搜索的实现 for 原版流体
     * @param world 世界
     * @param startPos 起始位置
     * @param material 流体类型
     * @param ignoreSameY 是否不考虑同层液体
     * @param ignoreLevel 是否忽略流体等级进行搜索
     * @param maxIterations 最大迭代次数
     * @param sameLevelIterationLimit 最大同等级搜索次数
     * @return 一个可能的流体源
     */
    private static Optional<BlockPos> findSourceIterate(@Nonnull World world,
                                                    @Nonnull BlockPos startPos,
                                                    @Nonnull Material material,
                                                    boolean ignoreSameY,
                                                    boolean ignoreLevel,
                                                    int maxIterations,
                                                    int sameLevelIterationLimit) {

        final Queue<FluidSourceSearchNode> queue = SearchSourceQueue.get();
        final Set<BlockPos> visited = SearchVisitedSet.get();
        queue.clear();
        visited.clear();

        queue.add(new FluidSourceSearchNode(startPos, EnumFacing.UP, 0, 0));
        visited.add(startPos);

        while (!queue.isEmpty()) {
            final FluidSourceSearchNode current = queue.poll();

            if (current.iteration > maxIterations) continue;
            final IBlockState state = world.getBlockState(current.pos);
            if (state.getMaterial() != material || state.getBlock() instanceof IFluidBlock)
                continue;
            if(!(state.getBlock() instanceof BlockLiquid)) continue; // 不是流体
            // 检查是否为源方块
            int level = FluidUtil.getFluidQuanta(world, current.pos, state);
            if (!(ignoreSameY && ( current.pos.getY() == startPos.getY()) ) && level == 8) return Optional.of(current.pos);

            if(!(ignoreSameY  && current.pos.getY()>startPos.getY())){
                // 向上搜索
                BlockPos upPos = current.pos.up();
                if (!visited.contains(upPos)) {
                    visited.add(upPos);
                    if(!world.isBlockLoaded(upPos)) continue;
                    queue.add(new FluidSourceSearchNode(
                            upPos,
                            EnumFacing.UP,
                            current.iteration + 1,
                            0
                    ));
                }
            }


            // 水平方向搜索
            EnumFacing opposite = current.direction.getOpposite();
            boolean falling = state.getValue(BlockLiquid.LEVEL) >= 8;

            for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
                if (dir == opposite) continue;

                BlockPos nextPos = current.pos.offset(dir);
                if (visited.contains(nextPos)) continue;
                if(!world.isBlockLoaded(nextPos)) continue;

                IBlockState nextState = world.getBlockState(nextPos);
                if (nextState.getMaterial() != material || nextState.getBlock() instanceof IFluidBlock) continue;
                if(!(nextState.getBlock() instanceof BlockLiquid)) continue; // 不是流体

                int nextLevel = FluidUtil.getFluidQuanta(world, nextPos, nextState);
                boolean nextFalling = nextState.getValue(BlockLiquid.LEVEL) >= 8;

                if (nextLevel >= level || (falling && !nextFalling) || ignoreLevel) {
                    int newSameLevelIter = (nextLevel == level) ?
                            current.sameLevelIteration + 1 : 0;
                    if(ignoreLevel) newSameLevelIter = 0;
                    if (newSameLevelIter > sameLevelIterationLimit) continue;

                    visited.add(nextPos);
                    queue.add(new FluidSourceSearchNode(
                            nextPos,
                            dir,
                            current.iteration + 1,
                            newSameLevelIter
                    ));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 广度优先搜索实现的 for Mod流体
     * @param world 世界
     * @param startPos 起始位置
     * @param ignoreSameY 是否不考虑同层流体
     * @param ignoreLevel 是否忽略流体等级进行搜索
     * @param maxIterations 最大迭代次数
     * @param sameQuantaIterationLimit 最大同量搜索次数
     * @param fluid 流体类型
     * @param densityDir 流体流动方向。向上为1，向下为-1。
     * @param quantaPerBlock 每个流体方块最大容量
     * @return 一个可能的流体源
     */
    private static Optional<BlockPos> findSourceIterate(@Nonnull World world,
                                                        @Nonnull BlockPos startPos,
                                                        @Nonnull Fluid fluid,
                                                        boolean ignoreSameY,
                                                        boolean ignoreLevel,
                                                        int densityDir,
                                                        int quantaPerBlock,
                                                        int maxIterations,
                                                        int sameQuantaIterationLimit) {

        final Queue<FluidSourceSearchNode> queue = SearchSourceQueue.get();
        final Set<BlockPos> visited = SearchVisitedSet.get();
        queue.clear();
        visited.clear();

        queue.add(new FluidSourceSearchNode(startPos, EnumFacing.UP, 0, 0));
        visited.add(startPos);

        while (!queue.isEmpty()) {
            FluidSourceSearchNode current = queue.poll();
            if (current.iteration > maxIterations) continue;
            IBlockState state = world.getBlockState(current.pos);
            Fluid currentFluid = FluidUtil.getFluid(state);
            if (currentFluid != fluid) continue;
            // 检查是否为源方块
            int quanta = FluidUtil.getFluidQuanta(world, current.pos, state);
            if (!(ignoreSameY && ( current.pos.getY() == startPos.getY()) ) && quanta == quantaPerBlock) return Optional.of(current.pos);

            if(!(ignoreSameY && current.pos.getY()>startPos.getY())){
                BlockPos upPos = current.pos.down(densityDir);
                if (!visited.contains(upPos)) {
                    visited.add(upPos);
                    queue.add(new FluidSourceSearchNode(
                            upPos,
                            EnumFacing.UP,
                            current.iteration + 1,
                            0
                    ));
                }
            }
            // 向上搜索


            // 水平方向搜索
            EnumFacing opposite = current.direction.getOpposite();
            boolean falling = (world.getBlockState(current.pos.down(densityDir)).getBlock() == state.getBlock());

            for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
                if (dir == opposite) continue;

                BlockPos nextPos = current.pos.offset(dir);
                if (visited.contains(nextPos)) continue;
                if (!world.isBlockLoaded(nextPos)) continue;

                IBlockState nextState = world.getBlockState(nextPos);
                if (FluidUtil.getFluid(nextState) != fluid) continue;

                int nextQuanta = FluidUtil.getFluidQuanta(world, nextPos, nextState);
                boolean nextFalling = (world.getBlockState(nextPos.down(densityDir)).getBlock() == nextState.getBlock());

                if (nextQuanta >= quanta || (falling && !nextFalling) || ignoreLevel) {
                    int newSameQuantaIter = (nextQuanta == quanta) ?
                            current.sameLevelIteration + 1 : 0;
                    if(ignoreLevel) newSameQuantaIter = 0;
                    if (newSameQuantaIter > sameQuantaIterationLimit) continue;

                    visited.add(nextPos);
                    queue.add(new FluidSourceSearchNode(
                            nextPos,
                            dir,
                            current.iteration + 1,
                            newSameQuantaIter
                    ));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 寻找放置指定流体的可选位置集合
     * 返回一个PlaceChoice集合
     * @param world 世界
     * @param origin 开始搜寻位置
     * @param fluid 流体
     * @param maxOptions 最大可选项
     * @param ignoreBeginPos 是否忽略开始搜索的位置
     * @param dir 指定垂直搜寻方向
     * @return 一个PlaceChoice集合，按照进<远排列
     */
    @Nonnull
    public static Set<PlaceChoice> findPlaceableLocations(final @Nonnull World world,
                                                          final @Nonnull BlockPos origin,
                                                          final @Nonnull Fluid fluid,
                                                          final int maxOptions,
                                                          final boolean ignoreBeginPos,
                                                          final @Nullable EnumFacing dir) {
        final @Nonnull Set<PlaceChoice> res = Sets.newLinkedHashSet();
        final @Nonnull Set<BlockPos> visited = SearchVisitedSet.get();
        final @Nonnull Object2LongMap<BlockPos> dis = new Object2LongOpenHashMap<>();
        final @Nonnull PriorityQueue<Pair<BlockPos,Long>> queue = new PriorityQueue<>(Comparator.comparing(Pair::getRight));
        visited.clear();
        dis.defaultReturnValue(Long.MAX_VALUE);
        dis.put(origin,0L);
        queue.add(Pair.of(origin,0L));

        final double gravity = dir == EnumFacing.UP?-GeoFluidSetting.getGravity(world):GeoFluidSetting.getGravity(world);
        final long valueInfo = getPlaceableSearchNodeValues(gravity == 0?0:1d/gravity);
        final short v_down = (short) (valueInfo>>(Short.SIZE<<1));
        final short v_0 = (short) ((valueInfo>>Short.SIZE) & 0xffffL);
        final short v_up = (short) (valueInfo & 0xffffL);

        while (!queue.isEmpty()){
            final @Nonnull BlockPos cur = queue.poll().getLeft();
            if(visited.contains(cur)) continue;
            visited.add(cur);
            if(MathUtil.manhattanDistance(cur,origin)>16 || !world.isBlockLoaded(cur)) continue;
            if(cur != origin || !ignoreBeginPos){
                if (FluidUtil.isFluidPlaceable(world,cur,fluid)) {
                    res.add(new PlaceChoice(FluidUtil.getFluidQuanta(world,cur,world.getBlockState(cur)),cur));
                    if (res.size() >= maxOptions) break;
                }else if(FluidUtil.getFluid(world.getBlockState(cur)) != fluid) {
                    continue;
                }
            }
            final long costHorizontal =  dis.getLong(cur) + v_0;
            for(final @Nonnull EnumFacing facing:EnumFacing.HORIZONTALS) queueIfBetterCost(cur.offset(facing),costHorizontal,queue,dis);
            if(cur.getY()>0) queueIfBetterCost(cur.down(),dis.getLong(cur) + v_down,queue,dis);
            if(cur.getY()<world.getHeight()) queueIfBetterCost(cur.up(),dis.getLong(cur) + v_up,queue,dis);
        }
        return res;
    }

    private static void queueIfBetterCost(final @Nonnull BlockPos pos,
                                          final long newCost,
                                          final @Nonnull PriorityQueue<Pair<BlockPos,Long>> queue,
                                          final @Nonnull Object2LongMap<BlockPos> dis){
        if(dis.getLong(pos) > newCost){
            dis.put(pos,newCost);
            queue.add(Pair.of(pos,newCost));
        }
    }

    /**
     * 基于相对重力大小计算寻找可放置流体位置时，向下/向上/水平方向的移动代价
     * @param gravity 相对重力大小
     * @return 一个 long，第1-16位无作用，17-32位为向下代价，33-48为水平代价，49-64为向上代价
     */
    public static long getPlaceableSearchNodeValues(final double gravity){
        final double a = FluidPhysicsConfig.PLACE_ALGORITHM_MAX_COST_FACTOR.getValue();
        final double m = FluidPhysicsConfig.PLACE_ALGORITHM_COST_SMOOTHNESS.getValue();
        final double d = FluidPhysicsConfig.PLACE_ALGORITHM_COST_MIDPOINT.getValue();
        final short v_down = (short) MathUtil.tanh(-gravity,a,m,d);
        final short v_0 = (short) MathUtil.tanh(0,a,m,d);
        final short v_up = (short) MathUtil.tanh(gravity,a,m,d);
        return (long) v_down <<(Short.SIZE<<1) | v_0<<Short.SIZE | v_up;
    }

    /**
     * 寻找放置指定流体的可选位置集合
     * BFS广度优先搜索，优先水平，其次往上或下
     * 返回一个PlaceChoice集合，按照水平<垂值，进<远排列
     * @param world 世界
     * @param origin 开始搜寻位置
     * @param fluid 流体
     * @param maxOptions 最大可选项
     * @param ignoreBeginPos 是否忽略开始搜索的位置
     * @param dir 指定垂直搜寻方向，若为null则默认会按照下->上优先级搜寻（负密度流体反之）
     * @return 一个PlaceChoice集合，按照水平<垂值，进<远排列
     */
    @Deprecated
    @Nonnull
    public static Set<PlaceChoice> findPlaceableLocationsPermeable(@Nonnull World world,
                                                          @Nonnull BlockPos origin,
                                                          @Nonnull Fluid fluid,
                                                          int maxOptions,
                                                          boolean ignoreBeginPos,
                                                          boolean allowAir,
                                                          @Nullable EnumFacing dir) {
        Set<PlaceChoice> res = new LinkedHashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        if (maxOptions <= 0) return res;

        if (!world.isBlockLoaded(origin)) return res;

        if(!ignoreBeginPos){
            if (FluidUtil.isFluidPlaceablePermeable(world,origin,fluid,allowAir))
                res.add(new PlaceChoice(LayeredFluidHostUtil.getFluidLayers(world,origin,world.getBlockState(origin),fluid,allowAir),origin));
            else if(
                    LayeredFluidHostUtil.isFluidAccepted(world,origin,world.getBlockState(origin),fluid,allowAir)) return res;
            if (res.size() >= maxOptions) return res;
        }

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && res.size() < maxOptions) {
            BlockPos current = queue.poll();

            for (int[] d : DIRS4) {
                BlockPos p = current.add(d[0], 0, d[1]);
                if (visited.contains(p)) continue;
                visited.add(p);

                if (!world.isBlockLoaded(p)) continue;

                if (FluidUtil.isFluidPlaceablePermeable(world,p,fluid,allowAir)) {
                    res.add(new PlaceChoice(LayeredFluidHostUtil.getFluidLayers(world,origin,world.getBlockState(p),fluid,allowAir),p));
                    if (res.size() >= maxOptions) break;
                    queue.add(p);
                }else if((LayeredFluidHostUtil.isFluidAccepted(world,p,world.getBlockState(p),fluid,allowAir))) {
                    queue.add(p);
                }
            }
        }
        if (res.size() >= maxOptions) return res;
        if(dir == null && fluid.getDensity() >=0){
            if(origin.getY() > 0) res.addAll(findPlaceableLocationsPermeable(
                    world,origin.down(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.DOWN));
            if (res.size() >= maxOptions) return res;
            if(origin.getY() < world.getHeight()-1) res.addAll(findPlaceableLocationsPermeable(
                    world,origin.up(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.UP));
        }else if(dir == null){
            if(origin.getY() < world.getHeight()-1) res.addAll(findPlaceableLocationsPermeable(
                    world,origin.up(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.UP));
            if (res.size() >= maxOptions) return res;
            if(origin.getY() > 0) res.addAll(findPlaceableLocationsPermeable(
                    world,origin.down(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.DOWN));
        }else if(dir == EnumFacing.DOWN && origin.getY() > 0){
            res.addAll(findPlaceableLocationsPermeable(world,origin.down(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.DOWN));
        }else if(dir == EnumFacing.UP && origin.getY() < world.getHeight()-1){
            res.addAll(findPlaceableLocationsPermeable(world,origin.up(),fluid,maxOptions-res.size(),false,allowAir,EnumFacing.UP));
        }

        return res;
    }

    /**
     * 寻找流体方块（广度优先）
     * @param world 所在世界
     * @param startPos 起始位置位置
     * @param searchInFlat 仅在水平面上搜索
     * @param upwardPriority 优先往上搜索
     * @param maxIterations 最大迭代次数
     * @return 一个流体方块，不一定是流体源
     */
    public static Optional<BlockPos> findFluid(World world,BlockPos startPos,boolean searchInFlat,boolean upwardPriority,int maxIterations){
        return findFluidIterate(world,startPos,null,null,searchInFlat,upwardPriority,maxIterations);
    }

    /**
     * 寻找流体方块（广度优先）
     * @param world 所在世界
     * @param startPos 起始位置位置
     * @param fluid 指定流体，若为null则不指定
     * @param ignoreBlocks 忽略的方块列表，null即为不指定忽略的方块
     * @param searchInFlat 仅在水平面上搜索
     * @param upwardPriority 优先往上搜索
     * @param maxIterations 最大迭代次数
     * @return 一个流体方块，不一定是流体源
     */
    public static Optional<BlockPos> findFluid(World world,BlockPos startPos,Fluid fluid,Set<BlockPos> ignoreBlocks,boolean searchInFlat,boolean upwardPriority,int maxIterations){
        return findFluidIterate(world,startPos,fluid,ignoreBlocks,searchInFlat,upwardPriority,maxIterations);
    }

    private static Optional<BlockPos> findFluidIterate(World world, BlockPos startPos, Fluid fluid, Set<BlockPos> ignoreBlocks, boolean searchInFlat, boolean upwardPriority, int maxIterations){
        final Queue<FluidSearchNode> queue = SearchQueue.get();
        final Set<BlockPos> visited = SearchVisitedSet.get();
        queue.clear();
        visited.clear();
        if(ignoreBlocks == null) ignoreBlocks = EMPTY_BLOCKPOS_SET;

        queue.add(new FluidSearchNode(startPos, EnumFacing.UP, 0));
        visited.add(startPos);

        while (!queue.isEmpty()) {
            FluidSearchNode current = queue.poll();
            if (current.iteration > maxIterations) continue;
            IBlockState state = world.getBlockState(current.pos);
            if(FluidUtil.isFluid(state) && (fluid == null || FluidUtil.getFluid(state) == fluid) && !ignoreBlocks.contains(current.pos)){
                if(!upwardPriority){
                    return Optional.of(current.pos);
                }
                queue.clear(); //下面一定不会再搜到了
                visited.clear();
                BlockPos upPos = current.pos.up();
                IBlockState upState = world.getBlockState(upPos);
                if(!FluidUtil.isFluid(upState)) return Optional.of(current.pos);
                visited.add(upPos); //不可能已经搜索过
                queue.add(new FluidSearchNode(
                        upPos,
                        EnumFacing.UP,
                        current.iteration // 不论怎样都已经找到了，不需要再加迭代次数
                ));
                continue;
            }

            // 水平方向搜索
            EnumFacing opposite = current.direction.getOpposite();

            for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
                if (dir == opposite) continue;
                BlockPos nextPos = current.pos.offset(dir);
                if (visited.contains(nextPos)) continue;
                visited.add(nextPos);
                IBlockState nextState = world.getBlockState(nextPos);
                if(!ignoreBlocks.contains(nextPos) && FluidUtil.isFluid(nextState) && (fluid == null || FluidUtil.getFluid(nextState) == fluid)) return Optional.of(nextPos);
                else if(nextState.getBlock().isPassable(world,nextPos) || nextState.getBlock().isReplaceable(world,nextPos) || FluidUtil.isFluid(nextState)) { //需要方块不能够阻挡
                    queue.add(new FluidSearchNode(
                            nextPos,
                            dir,
                            current.iteration + 1
                    ));
                }
            }
            //向上搜索
            if(!searchInFlat && !upwardPriority){
                BlockPos upPos = current.pos.up();
                visited.add(upPos);
                queue.add(new FluidSearchNode(
                        upPos,
                        EnumFacing.UP,
                        current.iteration + 1
                ));
            }
        }

        return Optional.empty();
    }

    private static class FluidSearchNode{
        final BlockPos pos;
        final EnumFacing direction;
        final int iteration;
        public FluidSearchNode(BlockPos pos, EnumFacing direction, int iteration) {
            this.pos = pos;
            this.direction = direction;
            this.iteration = iteration;
        }
    }

    private static class FluidSourceSearchNode extends FluidSearchNode{
        final int sameLevelIteration;

        public FluidSourceSearchNode(BlockPos pos, EnumFacing direction, int iteration, int sameLevelIteration) {
            super(pos,direction,iteration);
            this.sameLevelIteration = sameLevelIteration;
        }
    }
}
