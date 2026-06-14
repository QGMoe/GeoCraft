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

package top.qiguaiaaaa.geocraft.mixin.soil.block;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.qiguaiaaaa.geocraft.api.block.BlockProperties;

import java.util.Map;

@Mixin(BlockModelShapes.class)
public class BlockModelShapesMixin {
    @Final
    @Shadow
    private BlockStateMapper blockStateMapper;

    @Inject(method = "registerBlockWithStateMapper",at = @At("HEAD"),cancellable = true)
    public void registerBlockWithStateMapper(Block assoc, IStateMapper stateMapper, CallbackInfo ci) {
        if(assoc == Blocks.DIRT){
            ci.cancel();
            stateMapper = new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    Map<IProperty<?>, Comparable<? >> map = Maps.newLinkedHashMap(state.getProperties());
                    String s = BlockDirt.VARIANT.getName((BlockDirt.DirtType)map.remove(BlockDirt.VARIANT));

                    if (BlockDirt.DirtType.PODZOL != state.getValue(BlockDirt.VARIANT)) {
                        map.remove(BlockDirt.SNOWY);
                    }
                    map.remove(BlockProperties.HUMIDITY);

                    return new ModelResourceLocation(s, this.getPropertyString(map));
                }
            };
            this.blockStateMapper.registerBlockStateMapper(assoc, stateMapper);
        }else if(assoc == Blocks.SAND){
            ci.cancel();
            stateMapper = new StateMap.Builder().withName(BlockSand.VARIANT).ignore(BlockProperties.HUMIDITY).build();
            this.blockStateMapper.registerBlockStateMapper(assoc, stateMapper);
        }

    }
    @Inject(method = "registerAllBlocks",at = @At("TAIL"))
    private void registerAllBlocks(CallbackInfo ci){
        this.blockStateMapper.registerBlockStateMapper(Blocks.GRASS, (new StateMap.Builder())
                .ignore(BlockProperties.HUMIDITY)
                .build());
        this.blockStateMapper.registerBlockStateMapper(Blocks.GRAVEL,(new StateMap.Builder())
                .ignore(BlockProperties.HUMIDITY)
                .build());
        this.blockStateMapper.registerBlockStateMapper(Blocks.GRASS_PATH,(new StateMap.Builder())
                .ignore(BlockProperties.HUMIDITY)
                .build());
        this.blockStateMapper.registerBlockStateMapper(Blocks.CLAY,(new StateMap.Builder()
                .ignore(BlockProperties.HUMIDITY)
                .build()));
    }
}
