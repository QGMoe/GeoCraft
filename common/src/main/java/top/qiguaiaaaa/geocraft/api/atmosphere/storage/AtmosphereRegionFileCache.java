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

package top.qiguaiaaaa.geocraft.api.atmosphere.storage;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static top.qiguaiaaaa.geocraft.api.atmosphere.storage.AtmosphereRegionFile.STORAGE_ATMOSPHERES_COUNT_LOG;

/**
 * 管理目前游戏已加载到内存中的大气数据文件
 * 请使用该类加载大气数据文件
 * @since 0.1
 * @author QiguaiAAAA
 */
public final class AtmosphereRegionFileCache {
    public static final String WORKING_DIR = "atmosphere",FILE_SUFFIX = ".atmdat";
    public static final int ATMOSPHERE_SAVE_LOC_BYTE = (1<< STORAGE_ATMOSPHERES_COUNT_LOG)-1;
    private static final Map<File, AtmosphereRegionFile> ATMOSPHERE_REGIONS_BY_FILE = Maps.newHashMap();

    private AtmosphereRegionFileCache(){}

    /**
     * 创建或加载指定区块的大气数据文件<br/>
     * 大气数据文件位于DIMx/atmosphere/r.a.b.atmdat，其中a和b跟区块所处区域有关
     * @param worldDir 维度文件夹。应为类似DIMx的文件夹。主世界请用DIM0
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 加载完成的大气数据文件
     */
    @Nonnull
    public static synchronized AtmosphereRegionFile createOrLoadAtmosphereRegionFile(@Nonnull File worldDir, int chunkX, int chunkZ) {
        File dir = new File(worldDir, WORKING_DIR);
        File file = new File(dir, "r." + (chunkX >> STORAGE_ATMOSPHERES_COUNT_LOG) + "." + (chunkZ >> STORAGE_ATMOSPHERES_COUNT_LOG) + FILE_SUFFIX);
        AtmosphereRegionFile regionFile = ATMOSPHERE_REGIONS_BY_FILE.get(file);

        if (regionFile == null) {
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (ATMOSPHERE_REGIONS_BY_FILE.size() >= 256) {
                clearRegionFileReferences();
            }

            regionFile = new AtmosphereRegionFile(file);
            ATMOSPHERE_REGIONS_BY_FILE.put(file, regionFile);
        }
        return regionFile;
    }

    /**
     * 若指定区块的大气数据文件存在，则加载
     * @param worldDir 维度文件夹。应为类似DIMx的文件夹。主世界请用DIM0
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 加载完成的大气数据文件
     */
    @Nullable
    public static synchronized AtmosphereRegionFile getAtmosphereRegionFileIfExists(@Nonnull File worldDir, int chunkX, int chunkZ) {
        File dir = new File(worldDir, WORKING_DIR);
        File file = new File(dir, "r." + (chunkX >> STORAGE_ATMOSPHERES_COUNT_LOG) + "." + (chunkZ >> STORAGE_ATMOSPHERES_COUNT_LOG) + FILE_SUFFIX);
        AtmosphereRegionFile regionFile = ATMOSPHERE_REGIONS_BY_FILE.get(file);

        if (regionFile != null) {
            return regionFile;
        } else if (dir.exists() && file.exists()) {
            if (ATMOSPHERE_REGIONS_BY_FILE.size() >= 256) {
                clearRegionFileReferences();
            }

            regionFile = new AtmosphereRegionFile(file);
            ATMOSPHERE_REGIONS_BY_FILE.put(file, regionFile);
            return regionFile;
        } else {
            return null;
        }
    }

    /**
     * 清理目前的大气数据文件引用
     */
    public static synchronized void clearRegionFileReferences() {
        for (AtmosphereRegionFile file : ATMOSPHERE_REGIONS_BY_FILE.values()) {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ATMOSPHERE_REGIONS_BY_FILE.clear();
    }

    /**
     * 获取指定区块的大气数据文件的输入流
     * @param worldDir 维度文件夹。应为类似DIMx的文件夹。主世界请用DIM0
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 对应文件的输入流，可能为null
     */
    @Nullable
    public static DataInputStream getAtmosphereInputStream(@Nonnull File worldDir, int chunkX, int chunkZ) {
        AtmosphereRegionFile file = createOrLoadAtmosphereRegionFile(worldDir, chunkX, chunkZ);
        return file.getAtmosphereDataInputStream(chunkX & ATMOSPHERE_SAVE_LOC_BYTE, chunkZ & ATMOSPHERE_SAVE_LOC_BYTE);
    }

    /**
     * 获取指定区块的大气数据文件的输出流
     * @param worldDir 维度文件夹。应为类似DIMx的文件夹。主世界请用DIM0
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 对应文件的输出流，可能为null
     */
    @Nullable
    public static DataOutputStream getAtmosphereOutputStream(@Nonnull File worldDir, int chunkX, int chunkZ) {
        AtmosphereRegionFile file = createOrLoadAtmosphereRegionFile(worldDir, chunkX, chunkZ);
        return file.getAtmosphereDataOutputStream(chunkX & ATMOSPHERE_SAVE_LOC_BYTE, chunkZ & ATMOSPHERE_SAVE_LOC_BYTE);
    }

    /**
     * 检查指定区块的大气是否存在
     * @param worldDir 维度文件夹。应为类似DIMx的文件夹。主世界请用DIM0
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 若大气存在，则返回true
     */
    public static boolean atmosphereExists(@Nonnull File worldDir, int chunkX, int chunkZ) {
        AtmosphereRegionFile regionFile = getAtmosphereRegionFileIfExists(worldDir, chunkX, chunkZ);
        return regionFile != null && regionFile.isAtmosphereSaved(chunkX & ATMOSPHERE_SAVE_LOC_BYTE, chunkZ & ATMOSPHERE_SAVE_LOC_BYTE);
    }
}
