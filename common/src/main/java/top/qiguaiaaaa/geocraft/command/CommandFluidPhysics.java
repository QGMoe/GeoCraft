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

package top.qiguaiaaaa.geocraft.command;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import moe.qingu.nickel.command.builder.execute.CommandExecutor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import top.qiguaiaaaa.geocraft.api.atmosphere.accessor.IAtmosphereAccessor;
import moe.qingu.nickel.command.builder.CommandBuilder;
import moe.qingu.nickel.command.builder.INodeBuilder;
import moe.qingu.nickel.command.builder.execute.RelayExecuteNodeBuilder;
import moe.qingu.nickel.command.context.ExecuteContext;
import moe.qingu.nickel.command.node.ISmartNode;
import moe.qingu.nickel.command.node.parameter.generic.StringNode;
import top.qiguaiaaaa.geocraft.api.configs.value.geo.FluidPhysicsMode;
import top.qiguaiaaaa.geocraft.api.fluid.StateOfMatter;
import top.qiguaiaaaa.geocraft.api.property.TemperatureProperty;
import top.qiguaiaaaa.geocraft.api.util.AtmosphereUtil;
import top.qiguaiaaaa.geocraft.api.util.FluidUtil;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.FluidPhysicsCoreFinite;
import top.qiguaiaaaa.geocraft.util.WaterUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static moe.qingu.nickel.command.Nodes.*;
import static moe.qingu.nickel.text.Texts.*;
import static net.minecraft.block.BlockLiquid.LEVEL;
import static top.qiguaiaaaa.geocraft.command.CommandAtmosphere.AtmosphereCommandContext.ACCESSOR;
import static top.qiguaiaaaa.geocraft.command.CommandFluidPhysics.FluidPhysicsCommandExecutor.CHECK_ATMOSPHERE_ACCESSIBILITY;
import static top.qiguaiaaaa.geocraft.command.CommandFluidPhysics.FluidPhysicsCommandExecutor.GET_LIGHTED_ATMOSPHERE_ACCESSOR;
import static top.qiguaiaaaa.geocraft.command.GeoArguments.*;

/**
 * @author QiguaiAAAA
 */
public final class CommandFluidPhysics {
    public static final String FLUID_PHYSICS_COMMAND_NAME = "fluidphysics";
    public static final String FLUIDPHYSICS_PERMISSION_NODE = "geocraft.command.fluidphysics";

    private static ModeHandler HANDLER = ModeHandler.getHandler(FluidPhysicsMode.getCurrentMode());

    @Nonnull
    public static ICommand create(){
        HANDLER = ModeHandler.getHandler(FluidPhysicsMode.getCurrentMode());
        return new CommandBuilder(FLUID_PHYSICS_COMMAND_NAME)
                .require(2)
                .require(FLUIDPHYSICS_PERMISSION_NODE).allow(DefaultPermissionLevel.OP).register()
                .smart()
                .append(buildQueryCommand()).done()
                .append(buildUtilCommand()).done()
                .done()
                .usage("geocraft.command.fluidphysics.usage")
                .build();

    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildQueryCommand(){
        return literal("query")
                .then(literals()
                        .when("mode").then(execute(ctx -> {
                            ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.query.mode").arg(FluidPhysicsMode.getCurrentMode()).done());
                            ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,FluidPhysicsMode.getCurrentMode().ordinal());
                        })));
    }

    @Nonnull
    public static INodeBuilder<? extends ISmartNode> buildUtilCommand(){
        return literal("operation")
                .then(literals()
                        .when("evaporate").then(_pos()
                                .then(_doit()
                                        .then(process(HANDLER::evaporate,true)))));
    }

    @Nonnull
    static RelayExecuteNodeBuilder process(@Nonnull final FluidPhysicsCommandExecutor executor,final boolean checkAccessibility){
        return relay(checkAccessibility?GET_LIGHTED_ATMOSPHERE_ACCESSOR.then(CHECK_ATMOSPHERE_ACCESSIBILITY):GET_LIGHTED_ATMOSPHERE_ACCESSOR)
                .keepArguments(false)
                .then(execute(executor))
                .after(ctx -> {
                    final @Nullable IAtmosphereAccessor accessor = ctx.remove(ACCESSOR);
                    if(accessor != null) accessor.close();
                });
    }

    @Nonnull
    static IBlockState getWaterState(@Nonnull final World world,@Nonnull final BlockPos pos) throws CommandException{
        final @Nonnull IBlockState state = world.getBlockState(pos);
        if(state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.FLOWING_WATER)
            throw new CommandException("geocraft.command.fluidphysics.not_water",pos.getX(),pos.getY(),pos.getZ(),state);
        return state;
    }

    @Nonnull
    static IAtmosphereAccessor getLightedAtmosphereAccessor(final @Nonnull World world,final @Nonnull BlockPos pos) throws CommandException {
        final @Nullable IAtmosphereAccessor accessor = AtmosphereUtil.getLightedAtmosphereAccessor(world,pos,true);
        if(accessor == null){
            throw new CommandException("geocraft.command.atmosphere.nonexistent",pos);
        }
        return accessor;
    }

    @FunctionalInterface
    interface FluidPhysicsCommandExecutor extends CommandExecutor {
        CommandExecutor GET_LIGHTED_ATMOSPHERE_ACCESSOR = ctx -> ctx.put(ACCESSOR,getLightedAtmosphereAccessor(ctx.getWorld(),ctx.getBlockPos(POS)));
        CommandExecutor CHECK_ATMOSPHERE_ACCESSIBILITY = ctx -> {
            final IAtmosphereAccessor accessor = ctx.get(ACCESSOR);
            if(!accessor.canAccessAtmosphere()) throw new CommandException("geocraft.command.fluidphysics.inaccessibility_to_atmosphere",
                    accessor.getPos().getX(),
                    accessor.getPos().getY(),
                    accessor.getPos().getZ());
        };

        void run(@Nonnull final ExecuteContext ctx,@Nonnull final IAtmosphereAccessor accessor) throws CommandException;

        @Override
        default void run(@Nonnull ExecuteContext context) throws CommandException {
            this.run(context,context.get(ACCESSOR));
        }
    }

    enum ModeHandler{
        FINITE(FluidPhysicsMode.MORE_REALITY){
            @Override
            public void evaporate(@Nonnull final ExecuteContext ctx,@Nonnull final IAtmosphereAccessor accessor) throws CommandException {
                super.evaporate(ctx,accessor);
                final @Nonnull BlockPos pos = ctx.getBlockPos(POS);
                final @Nonnull World worldIn = ctx.getWorld();
                final boolean doEvaporate = !ctx.get(DOIT, StringNode.class).isEmpty();
                final @Nonnull IBlockState state = getWaterState(worldIn,pos);
                final Object2DoubleArrayMap<String> reasons = new Object2DoubleArrayMap<>();
                final double possibility = this.gatherEvaporationStatus(state,reasons,accessor);

                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.title").arg(pos.getX(),pos.getY(),pos.getZ())
                        .color(TextFormatting.YELLOW).bold(true).done());
                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.possibility.pre")
                        .then(plain(possibility * 100d +" %").color(TextFormatting.AQUA))
                        .done());
                reasons.forEach((reason,delta)-> ctx.getSender().sendMessage(translation(reason)
                        .then(plain(" : "))
                        .italic(true)
                        .then(plain((delta>=0?"+":"-")+String.format("%.4f %%",delta*100d))
                                .color(delta>0?TextFormatting.GREEN:delta<0?TextFormatting.RED:TextFormatting.GRAY)
                                .underlined(true))
                        .done()));
                if(doEvaporate) this.evaporateFor(state,accessor,ctx);
            }

            final double gatherEvaporationStatus(@Nonnull final IBlockState state, @Nonnull final Object2DoubleArrayMap<String> reasons,@Nonnull final IAtmosphereAccessor accessor){
                final @Nonnull World world = accessor.getWorld();
                final @Nonnull BlockPos pos = accessor.getPos();
                if(!world.isAirBlock(pos.up())){
                    reasons.put("geocraft.command.fluidphysics.evapration.reasons.upon_non_air",0);
                    return 0;
                }else if(state.getValue(LEVEL) >= 8){
                    reasons.put("geocraft.command.fluidphysics.evapration.reasons.invalid_water",1);
                    return 1;
                }
                final double raw = WaterUtil.getWaterEvaporatePossibility(accessor);
                reasons.put("geocraft.command.fluidphysics.evapration.reasons.rawPossibility",raw);
                if(raw >= 0.9999d){
                    reasons.put("geocraft.command.fluidphysics.evapration.reasons.saturated",1d-raw);
                    return 1;
                }else if(useRawEvaporationPossibility(world,pos,state)){
                    return raw;
                }

                final double possibility = adjustEvaporationPossibilityByNeighborsAir(world,pos,raw);
                reasons.put("geocraft.command.fluidphysics.evapration.reasons.exposure",possibility-raw);
                return possibility;
            }

            final boolean useRawEvaporationPossibility(@Nonnull final World world,@Nonnull final BlockPos pos,@Nonnull final IBlockState state){
                return state.getValue(LEVEL) <5 || !world.isAreaLoaded(pos,1) || pos.getY() <= 0 || FluidUtil.getFluid(world.getBlockState(pos.down())) == FluidRegistry.WATER;
            }

            final double adjustEvaporationPossibilityByNeighborsAir(@Nonnull final World world,@Nonnull final BlockPos pos,final double possibility){
                byte neighborsAir = 0;
                for(final @Nonnull EnumFacing facing:EnumFacing.HORIZONTALS){
                    final @Nonnull BlockPos facingPos = pos.offset(facing);
                    if(world.isAirBlock(facingPos)) neighborsAir++;
                }
                if(neighborsAir <= 1) return possibility;
                return Math.min(possibility*(1<<(neighborsAir-1)),1);
            }

            final void evaporateFor(@Nonnull final IBlockState state, @Nonnull final IAtmosphereAccessor accessor, @Nonnull final ExecuteContext ctx){
                final @Nonnull IBlockState newState = FluidPhysicsCoreFinite.evaporateWater(state,accessor.getWorld().rand,accessor);
                accessor.getWorld().setBlockState(accessor.getPos(),newState);
                final int quanta = newState == Blocks.AIR.getDefaultState()?Math.max(8-state.getValue(LEVEL),0):(newState.getValue(LEVEL)-state.getValue(LEVEL));
                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.evaporated")
                        .arg(quanta*FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME,quanta)
                        .color(quanta <= 0?TextFormatting.GRAY:TextFormatting.GREEN)
                        .done());
                ctx.getSender().setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS,quanta==0?0:1);
            }
        },
        CLASSIC(FluidPhysicsMode.VANILLA_LIKE){
            @Override
            public void evaporate(@Nonnull final ExecuteContext ctx,@Nonnull final IAtmosphereAccessor accessor) throws CommandException {
                super.evaporate(ctx,accessor);
                final @Nonnull BlockPos pos = ctx.getBlockPos(POS);
                final @Nonnull World worldIn = ctx.getWorld();
                final boolean doEvaporate = !ctx.get(DOIT, StringNode.class).isEmpty();
                final @Nonnull IBlockState state = getWaterState(worldIn,pos);
                final int amount = this.getEstimatedEvaporateAmount(state,accessor);

                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.title")
                        .arg(pos.getX(),pos.getY(),pos.getZ())
                        .color(TextFormatting.YELLOW)
                        .bold(true)
                        .done());
                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.amount")
                        .then(plain(amount +" mB").color(TextFormatting.AQUA))
                        .done());
                ctx.getSender().setCommandStat(CommandResultStats.Type.QUERY_RESULT,amount);
                if(doEvaporate) this.evaporateFor(state,accessor,ctx);
            }

            final int getEstimatedEvaporateAmount(@Nonnull final IBlockState state, @Nonnull final IAtmosphereAccessor accessor){
                final int meta = state.getValue(LEVEL);
                if(accessor.getTemperature()> TemperatureProperty.BOILED_POINT){
                    if(meta == 0){
                        return accessor.fillFluidToAtmosphere(FluidRegistry.WATER, Fluid.BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),false);
                    }
                    return 0;
                }
                final int amount = (int) MathHelper.clamp(WaterUtil.getWaterEvaporateAmount(accessor),0,Fluid.BUCKET_VOLUME);
                if(amount == 0) return 0;
                return accessor.fillFluidToAtmosphere(FluidRegistry.WATER,amount, StateOfMatter.GAS,accessor.getTemperature(true),false);
            }

            final int getRealEvaporateAmount(@Nonnull final IBlockState state, @Nonnull final IAtmosphereAccessor accessor){
                final World world = accessor.getWorld();
                final BlockPos pos = accessor.getPos();
                final int meta = state.getValue(LEVEL);
                if(accessor.getTemperature()> TemperatureProperty.BOILED_POINT){
                    FluidRegistry.WATER.vaporize(null,world,pos,null);
                    world.setBlockToAir(pos);
                    if(meta == 0) return accessor.fillFluidToAtmosphere(FluidRegistry.WATER,Fluid.BUCKET_VOLUME, StateOfMatter.GAS,accessor.getTemperature(true),true);
                    return 0;
                }

                int amount = (int) MathHelper.clamp(WaterUtil.getWaterEvaporateAmount(accessor),0,Fluid.BUCKET_VOLUME);
                if(amount > 0 && (amount = accessor.fillFluidToAtmosphere(FluidRegistry.WATER,amount, StateOfMatter.GAS,accessor.getTemperature(true),true)) > 0){
                    accessor.drainHeatFromUnderlying(AtmosphereUtil.Constants.WATER_EVAPORATE_LATENT_HEAT_PER_QUANTA*(double)amount/FluidUtil.ONE_IN_EIGHT_OF_BUCKET_VOLUME);
                    if(meta == 0 && amount >= Fluid.BUCKET_VOLUME){
                        world.setBlockToAir(pos);
                    }
                }
                return amount;
            }

            final void evaporateFor(@Nonnull final IBlockState state,@Nonnull final IAtmosphereAccessor accessor,@Nonnull final ExecuteContext ctx){
                final int amount = getRealEvaporateAmount(state,accessor);
                ctx.getSender().sendMessage(translation("geocraft.command.fluidphysics.evapration.vanilla.evaporated").arg(amount)
                        .color(amount <= 0?TextFormatting.GRAY:TextFormatting.GREEN).done());
            }
        },
        VANILLA(FluidPhysicsMode.VANILLA){
            @Override
            public void evaporate(@Nonnull final ExecuteContext ctx, @Nonnull final IAtmosphereAccessor accessor) throws CommandException {
                CLASSIC.evaporate(ctx, accessor);
            }
        };

        private final FluidPhysicsMode mode;

        ModeHandler(final @Nonnull FluidPhysicsMode mode) {
            this.mode = mode;
        }

        @Nonnull
        public FluidPhysicsMode getMode() {
            return mode;
        }

        public void evaporate(@Nonnull final ExecuteContext ctx,@Nonnull final IAtmosphereAccessor accessor) throws CommandException{
            final @Nonnull BlockPos pos = ctx.getBlockPos(POS);
            if(!accessor.getAtmosphereInfo().canWaterEvaporate(pos)){
                throw new CommandException("geocraft.command.fluidphysics.unable_to_evaporate",pos.getX(),pos.getY(),pos.getZ());
            }
        }

        public static ModeHandler getHandler(@Nonnull final FluidPhysicsMode mode){
            switch (mode){
                case MORE_REALITY:return FINITE;
                case VANILLA_LIKE:return CLASSIC;
                case VANILLA:return VANILLA;
                default:throw new IllegalArgumentException();
            }
        }
    }
}
