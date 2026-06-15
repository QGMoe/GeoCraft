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

package 清汩萌.天圆地方.assets;

import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import top.qiguaiaaaa.geocraft.api.block.BlockProperties;
import top.qiguaiaaaa.geocraft.block.BlockSnowFinite;
import top.qiguaiaaaa.geocraft.block.soil.BlockSoilDirt;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowingVanilla;
import top.qiguaiaaaa.geocraft.geography.fluidphysics.finite.flow.FiniteFlowings;
import 清汩萌.天圆地方.天圆地方测试;
import 清汩萌.天圆地方.asm.InitBlocksPlugin;
import 清汩萌.天圆地方.block.MockBlockFluidHostCommon;
import 清汩萌.天圆地方.block.MockBlockLiquid;
import 清汩萌.造.映射.映射;
import 清汩萌.造.映射.映射器;
import 清汩萌.造.空间.空间构造器;

import javax.annotation.Nonnull;

/**
 * @author QiguaiAAAA
 */
public final class MockBlocks {

    public static final 空间构造器 BUILDER;

    public static final class LayeredFluidHosts{
        public static MockBlockFluidHostCommon FLUID_HOST_COMMON = new MockBlockFluidHostCommon();
    }

    @SuppressWarnings("unused")
    public static final class Bases{
        public static final 映射器 BASIC_MAPPING;

        public static BlockAir AIR = new BlockAir(){{ /// @see net.minecraft.init.Blocks#AIR
            this.setRegistryName(天圆地方测试.MODID, "air");
            registerToMinecraft("AIR",0,this);
        }};
        public static BlockStone STONE = new BlockStone(){{
            this.setSoundType(SoundType.STONE).setHardness(1.5F).setResistance(10.0F).setRegistryName(天圆地方测试.MODID, "stone");
            registerToMinecraft("STONE",1,this);
        }};
        public static BlockSoilDirt DIRT = new BlockSoilDirt(){{
            registerToMinecraft("DIRT",3,this.setHardness(0.5F).setRegistryName(天圆地方测试.MODID,"dirt"));
        }};
        public static Block COBBLESTONE = new Block(Material.ROCK){{
            registerToMinecraft("COBBLESTONE",4,this.setSoundType(SoundType.STONE).setHardness(2.0F)
                    .setResistance(10.0F).setRegistryName("cobblestone"));
        }}; /// @see Blocks#COBBLESTONE

        public static IBlockState 〇 = AIR.getDefaultState();

        public static IBlockState 石 = STONE.getDefaultState();
        public static IBlockState 崗 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.GRANITE); //繁体岗，表示未打磨的花岗岩
        public static IBlockState 岗 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.GRANITE_SMOOTH); //简体，表示已打磨，看起来简单了
        public static IBlockState 閃 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.DIORITE); //繁体闪，表示未打磨的闪长岩
        public static IBlockState 闪 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.DIORITE_SMOOTH); //简体，表示已打磨
        public static IBlockState 峖 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.ANDESITE); //带有山字旁，表示未打磨的安山岩
        public static IBlockState 安 = STONE.getDefaultState().withProperty(BlockStone.VARIANT,BlockStone.EnumType.ANDESITE_SMOOTH); //没有山字旁，简单了，表示已打磨的安山岩

        /*
        Dirt 泥土
         */
        public static IBlockState 土0 = DIRT.getDefaultState().withProperty(BlockProperties.HUMIDITY,0); //后面的数字表示含水量
        public static IBlockState 土1 = 土0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 土2 = 土0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 土3 = 土0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 土4 = 土0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 土 = 土0;

        public static IBlockState 𪢶0 = 土.withProperty(BlockDirt.SNOWY,true); //𪢶，取冬上部+土字组合，表示冬天的土，也就是覆盖雪的土
        public static IBlockState 𪢶1 = 𪢶0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 𪢶2 = 𪢶0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 𪢶3 = 𪢶0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 𪢶4 = 𪢶0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 𪢶 = 𪢶0;

        /*
        Coarse Dirt 砂土（沙土）
         */
        public static IBlockState 砂0 = 土.withProperty(BlockDirt.VARIANT,BlockDirt.DirtType.COARSE_DIRT);
        public static IBlockState 砂1 = 砂0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 砂2 = 砂0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 砂3 = 砂0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 砂4 = 砂0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 砂 = 砂0;

        public static IBlockState 粆0 = 砂0.withProperty(BlockDirt.SNOWY,true); //粆，本意蔗糖。在这里会意，表示具有和米一样的颜色的砂土，也就是表面覆盖白雪的砂土。读砂
        public static IBlockState 粆1 = 粆0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 粆2 = 粆0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 粆3 = 粆0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 粆4 = 粆0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 粆 = 粆0;

        /*
        Podzol 灰化土
         */
        public static IBlockState 䒚0 = 土.withProperty(BlockDirt.VARIANT,BlockDirt.DirtType.PODZOL); //灰化土，在寒冷地带砂土（也称沙土，这里简化为少）表层覆盖有有机物，由于气温低有机物难以分解，在表层形成灰色的有机层，即为草字头，该字读少。
        public static IBlockState 䒚1 = 䒚0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 䒚2 = 䒚0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 䒚3 = 䒚0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 䒚4 = 䒚0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 䒚 = 䒚0;

        public static IBlockState 莎0 = 䒚.withProperty(BlockDirt.SNOWY,true); //莎，相比䒚多了三点水，表示有水，也就是上面覆雪。实在找不到带有冫的字，而且这也很方便打出来，莎草纸嘛，只不过这里赋予了新的意思
        public static IBlockState 莎1 = 莎0.withProperty(BlockProperties.HUMIDITY,1);
        public static IBlockState 莎2 = 莎0.withProperty(BlockProperties.HUMIDITY,2);
        public static IBlockState 莎3 = 莎0.withProperty(BlockProperties.HUMIDITY,3);
        public static IBlockState 莎4 = 莎0.withProperty(BlockProperties.HUMIDITY,4);
        @映射.别名
        public static IBlockState 莎 = 莎0;

        public static IBlockState 圆 = COBBLESTONE.getDefaultState();

        static {
            BASIC_MAPPING = new 映射器("Basic Mapping").导入映射数据(Bases.class);
        }
    }

    @SuppressWarnings("unused")
    public static final class VanillaFluids {
        public static final 映射器 VANILLA_FLUIDS_MAPPING;
        public static final 空间构造器 VANILLA_FLUIDS_BUILDER;

        private static final Pair<MockBlockLiquid.MockBlockDynamicLiquid, MockBlockLiquid.MockBlockStaticLiquid> WATERS =
                MockBlockLiquid.create(b -> b.withID("water").withMaterial(Material.WATER).withMapColor(MapColor.WATER));

        private static final Pair<MockBlockLiquid.MockBlockDynamicLiquid, MockBlockLiquid.MockBlockStaticLiquid> LAVAS =
                MockBlockLiquid.create(b -> b.withID("lava").withMaterial(Material.LAVA));

        public static final BlockDynamicLiquid DYNAMIC_WATER = WATERS.getKey();
        public static final BlockStaticLiquid STATIC_WATER = WATERS.getRight();
        public static final BlockDynamicLiquid DYNAMIC_LAVA = LAVAS.getKey();
        public static final BlockStaticLiquid STATIC_LAVA = LAVAS.getRight();

        public static final BlockObsidian OBSIDIAN = new BlockObsidian(){{
            registerToMinecraft("OBSIDIAN",49, this.setSoundType(SoundType.STONE)
                    .setHardness(50.0F).setResistance(2000.0F).setRegistryName(天圆地方测试.MODID,"obsidian")); /// @see Blocks#OBSIDIAN
        }};

        public static IBlockState 曜 = OBSIDIAN.getDefaultState();

        public static IBlockState 丶 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,7); // 主 （读音）
        public static IBlockState 冫 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,6); // 冰
        public static IBlockState 氵 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,5); // 水
        public static IBlockState 灬 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,4); // 火
        public static IBlockState 水 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,3);
        public static IBlockState 沝 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,2); // 子
        public static IBlockState 淼 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,1); // 汪淼的淼
        public static IBlockState 㵘 = STATIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,0); // 打 水水水水

        public static IBlockState 丶v = 丶.withProperty(BlockLiquid.LEVEL,15); // v 表示垂直流动，在 FINITE 模式下目前不应当出现，这是原版的一个奇怪的设计，包括模组也没有
        public static IBlockState 冫v = 丶.withProperty(BlockLiquid.LEVEL,14);
        public static IBlockState 氵v = 丶.withProperty(BlockLiquid.LEVEL,13);
        public static IBlockState 灬v = 丶.withProperty(BlockLiquid.LEVEL,12);
        public static IBlockState 水v = 丶.withProperty(BlockLiquid.LEVEL,11);
        public static IBlockState 沝v = 丶.withProperty(BlockLiquid.LEVEL,10);
        public static IBlockState 淼v = 丶.withProperty(BlockLiquid.LEVEL,9);
        public static IBlockState 㵘v =丶.withProperty(BlockLiquid.LEVEL,8);

        public static IBlockState 涸 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,7); //几乎干涸
        public static IBlockState 浅 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,6); //浅水
        public static IBlockState 涓 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,5); //涓涓细流
        public static IBlockState 盈 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,4); //充盈的水流
        public static IBlockState 涨 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,3); //涨起来的水流
        public static IBlockState 洪 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,2); //洪水
        public static IBlockState 滔 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,1); //滔滔不绝
        public static IBlockState 溢 = DYNAMIC_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,0); //快溢出来了

        public static IBlockState 涸v = 涸.withProperty(BlockLiquid.LEVEL,15);
        public static IBlockState 浅v = 涸.withProperty(BlockLiquid.LEVEL,14);
        public static IBlockState 涓v = 涸.withProperty(BlockLiquid.LEVEL,13);
        public static IBlockState 盈v = 涸.withProperty(BlockLiquid.LEVEL,12);
        public static IBlockState 涨v = 涸.withProperty(BlockLiquid.LEVEL,11);
        public static IBlockState 洪v = 涸.withProperty(BlockLiquid.LEVEL,10);
        public static IBlockState 滔v = 涸.withProperty(BlockLiquid.LEVEL,9);
        public static IBlockState 溢v = 涸.withProperty(BlockLiquid.LEVEL,8);

        public static IBlockState 火 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,7);
        public static IBlockState 炎 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,6);
        public static IBlockState 焱 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,5);
        public static IBlockState 燚 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,4);
        public static IBlockState 日 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,3);
        public static IBlockState 昍 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,2);
        public static IBlockState 晿 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,1);
        public static IBlockState 𣊭 = STATIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,0);

        public static IBlockState 火v = 火.withProperty(BlockLiquid.LEVEL,15);
        public static IBlockState 炎v = 火.withProperty(BlockLiquid.LEVEL,14);
        public static IBlockState 焱v = 火.withProperty(BlockLiquid.LEVEL,13);
        public static IBlockState 燚v = 火.withProperty(BlockLiquid.LEVEL,12);
        public static IBlockState 日v = 火.withProperty(BlockLiquid.LEVEL,11);
        public static IBlockState 昍v = 火.withProperty(BlockLiquid.LEVEL,10);
        public static IBlockState 晿v = 火.withProperty(BlockLiquid.LEVEL,9);
        public static IBlockState 𣊭v = 火.withProperty(BlockLiquid.LEVEL,8);

        public static IBlockState 曦 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,7);
        public static IBlockState 旭 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,6);
        public static IBlockState 晁 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,5);
        public static IBlockState 昌 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,4);
        public static IBlockState 晅 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,3);
        public static IBlockState 暑 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,2);
        public static IBlockState 炽 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,1);
        public static IBlockState 灼 = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,0);

        public static IBlockState 曦v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,15);
        public static IBlockState 旭v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,14);
        public static IBlockState 晁v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,13);
        public static IBlockState 昌v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,12);
        public static IBlockState 晅v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,11);
        public static IBlockState 暑v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,10);
        public static IBlockState 炽v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,9);
        public static IBlockState 灼v = DYNAMIC_LAVA.getDefaultState().withProperty(BlockLiquid.LEVEL,8);

        static {
            registerToMinecraft("FLOWING_WATER",8,DYNAMIC_WATER).setHardness(100.0F).setLightOpacity(3);
            registerToMinecraft("WATER",9,STATIC_WATER).setHardness(100.0F).setLightOpacity(3);
            registerToMinecraft("FLOWING_LAVA",10,DYNAMIC_LAVA).setHardness(100.0F).setLightLevel(1.0F).setLightOpacity(3);
            registerToMinecraft("LAVA",11,STATIC_LAVA).setHardness(100.0F).setLightLevel(1.0F).setLightOpacity(3);
            VANILLA_FLUIDS_MAPPING = new 映射器("Vanilla Fluids Mapping").导入映射数据(VanillaFluids.class);
            VANILLA_FLUIDS_BUILDER = new 空间构造器().添加映射(Bases.BASIC_MAPPING).添加映射(VANILLA_FLUIDS_MAPPING);
        }

        @Nonnull
        public static FiniteFlowingVanilla getFlowingByMaterial(final @Nonnull Material material){
            return material.isLiquid()?material == Material.WATER? FiniteFlowings.WATER_FLOW : FiniteFlowings.LAVA_FLOW :Assertions.fail("Unknown Liquid Type!");
        }
    }

    @SuppressWarnings("unused")
    public static final class GeoSnows{
        public static final BlockSnowFinite SNOW = new BlockSnowFinite(){{
            this.setSoundType(SoundType.SNOW).setLightOpacity(0).setRegistryName(天圆地方测试.MODID,"snow_layer").setHardness(0.1F);
            registerToMinecraft("SNOW_LAYER",78,this);
        }};

        public static IBlockState 霁 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,1);
        public static IBlockState 霰 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,2);
        public static IBlockState 霙 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,3);
        public static IBlockState 霏 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,4);
        public static IBlockState 雱 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,5);
        public static IBlockState 霈 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,6);
        public static IBlockState 雹 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,7);
        public static IBlockState 霃 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,8);

        public static IBlockState 凉 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,1).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 冷 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,2).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 寒 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,3).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 冽 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,4).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 凝 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,5).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 凘 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,6).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 凌 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,7).withProperty(BlockProperties.MIXTURE,true);
        public static IBlockState 冻 = SNOW.getDefaultState().withProperty(BlockSnow.LAYERS,8).withProperty(BlockProperties.MIXTURE,true);
    }

    static {
        BUILDER = new 空间构造器().添加映射(Bases.BASIC_MAPPING)
                .添加映射(VanillaFluids.VANILLA_FLUIDS_MAPPING)
                .添加映射(new 映射器("GeoSnows Mapping").导入映射数据(GeoSnows.class));
    }

    public static void initMinecraftBlocks(){
        /*
        Load Minecraft Blocks Mapping, by forcing its static init function to run
         */
        Blocks.AIR.getDefaultState().getBlock();
    }

    public static <T extends Block> T registerToMinecraft(final @Nonnull String fieldNameInBlocks,final int id,final T instance){
        Assertions.assertNotNull(instance.getRegistryName());
        Block.REGISTRY.register(id,instance.getRegistryName(),instance);
        if(!InitBlocksPlugin.registerOverride(fieldNameInBlocks,instance)){
            Assertions.fail("Block {} isn't registered to Minecraft because Blocks have been loaded");
        }
        return instance;
    }
}
