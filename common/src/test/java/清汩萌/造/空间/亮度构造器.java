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

package 清汩萌.造.空间;

import it.unimi.dsi.fastutil.bytes.Byte2CharArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import 清汩萌.造.词块.词块;
import 清汩萌.造.造;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author QGMoe
 */
public final class 亮度构造器 {
    public static final 亮度构造器 $亮度构造器 = new 亮度构造器();
    public static final byte 零 = 0;
    public static final byte 一 = 1;
    public static final byte 二 = 2;
    public static final byte 三 = 3;
    public static final byte 四 = 4;
    public static final byte 五 = 5;
    public static final byte 六 = 6;
    public static final byte 七 = 7;
    public static final byte 八 = 8;
    public static final byte 九 = 9;
    public static final byte 十 = 10;
    public static final byte A = 10;
    public static final byte B = 11;
    public static final byte C = 12;
    public static final byte D = 13;
    public static final byte E = 14;
    public static final byte F = 15;

    private static final Byte2CharArrayMap $亮度到字映射表 = new Byte2CharArrayMap(
            ArrayUtils.toPrimitive(IntStream.range(0,16).mapToObj(i -> (byte) i).toArray(Byte[]::new)),
            "零一二三四五六七八九ABCDEF".toCharArray()
    );
    private static final Object2ByteOpenHashMap<词块> $映射表 = new Object2ByteOpenHashMap<>(
            "零一二三四五六七八九ABCDEF".codePoints().mapToObj(cp -> new StringBuilder().appendCodePoint(cp).toString()).map(词块::of).toArray(词块[]::new),
            ArrayUtils.toPrimitive(IntStream.range(0,16).mapToObj(i -> (byte) i).toArray(Byte[]::new))
    );

    static {
        $亮度到字映射表.defaultReturnValue('〇');
        $映射表.defaultReturnValue((byte) -1);
        $映射表.addTo(词块.of("十"),(byte)10);
    }

    private 亮度构造器(){}

    public byte 进行映射(final @Nonnull 词块 $词块){
        final byte res = $映射表.getByte($词块);
        if(res < 0) throw new IllegalArgumentException($词块 + " 无可用映射");
        return res;
    }

    public char 进行映射(final byte $亮度){
        if($亮度 < 0 || $亮度>15) throw new IllegalArgumentException($亮度 + " 无可用映射");
        return $亮度到字映射表.get($亮度);
    }

    @Nonnull
    public byte[][][] 构造(final @Nonnull 词块网格 $网格){
        return 构造($网格.处理填充().stream().map($单层 -> $单层.$行列表).collect(Collectors.toList()));
    }

    @Nonnull
    public byte[][][] 构造(final @Nonnull List<List<List<词块>>> $原始词块数据){
        return $原始词块数据.stream()
                .map($单层 -> $单层.stream()
                        .map($单行 -> {
                            final byte[] arr = new byte[$单行.size()];
                            for(int i=0;i<$单行.size();i++) arr[i] = 进行映射($单行.get(i));
                            return arr;
                        })
                        .toArray(byte[][]::new)
                ).toArray(byte[][][]::new);
    }

    @Nonnull
    public int[][][] 序列化(final @Nonnull byte[][][] $网格){
        final int[][][] $汉字网格 = new int[$网格.length][][];
        for(int y=0;y<$网格.length;y++){
            $汉字网格[y] = new int[$网格[y].length][];
            for(int z =0;z<$网格[y].length;z++){
                $汉字网格[y][z] = new int[$网格[y][z].length];
                for(int x=0;x<$网格[y][z].length;x++)
                    $汉字网格[y][z][x] = 进行映射($网格[y][z][x]);
            }
        }
        return $汉字网格;
    }

    public void 打印(final @Nonnull byte[][][] $网格){
        打印($网格, 造.LOGGER);
    }

    public void 打印(final @Nonnull byte[][][] $网格, final @Nonnull Logger logger){
        logger.info("亮度构造:\n{}",Arrays.stream(序列化($网格))
                .map($层 -> Arrays.stream($层)
                        .map($行 -> Arrays.stream($行)
                                .mapToObj(cp -> new StringBuilder().appendCodePoint(cp).toString())
                                .collect(Collectors.joining()))
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n")));
    }
}
