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

package 清汩萌.天圆地方.world.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import org.junit.jupiter.api.Assumptions;

import javax.annotation.Nonnull;

/**
 * @author QGMoe
 */
public class SimpleLightGrid implements ILightGrid{
    protected final @Nonnull EnumSkyBlock type;
    protected final @Nonnull byte[][][] structure;
    protected byte outLight;

    public SimpleLightGrid(@Nonnull final EnumSkyBlock type, @Nonnull byte[][][] structure) {
        this.type = type;
        this.structure = structure;
    }

    public void setOutLight(final byte outLight) {
        Assumptions.assumeTrue(outLight >=0 && outLight <= 15);
        this.outLight = outLight;
    }

    protected boolean isOutOfRange(final int x,final int y,final int z){
        return !(y>=0 && y < structure.length && z>=0 && z < structure[y].length && x>=0 && x < structure[y][z].length);
    }

    @Nonnull
    @Override
    public EnumSkyBlock getType() {
        return type;
    }

    @Override
    public byte getLight(@Nonnull final BlockPos pos) {
        return isOutOfRange(pos)?this.outLight:this.structure[pos.getY()][pos.getZ()][pos.getX()];
    }

    @Override
    public boolean isOutOfRange(@Nonnull final BlockPos pos) {
        return isOutOfRange(pos.getX(),pos.getY(),pos.getZ());
    }
}
