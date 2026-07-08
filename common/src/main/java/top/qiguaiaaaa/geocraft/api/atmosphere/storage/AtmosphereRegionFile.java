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

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.storage.RegionFile;
import top.qiguaiaaaa.geocraft.api.util.APIUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * 大气数据文件，类似{@link RegionFile}但格式些许不同<br/>
 * 一个文件最多保存{@link #TOTAL_ATMOSPHERES}个大气
 */
public class AtmosphereRegionFile {
    public static final short STORAGE_ATMOSPHERES_COUNT_LOG = 7;
    private static final int TOTAL_ATMOSPHERES = 1<<(STORAGE_ATMOSPHERES_COUNT_LOG*2),
            ATMOSPHERES_PER_ROW = 1<<STORAGE_ATMOSPHERES_COUNT_LOG,
            HEAD_SECTORS = 1<<((STORAGE_ATMOSPHERES_COUNT_LOG-5)*2+1),//计算开头存储索引+时间戳需要的扇区数
            TIME_SECTORS_BEGIN = 1<<((STORAGE_ATMOSPHERES_COUNT_LOG-5)*2);
    private static final byte[] EMPTY_SECTOR = new byte[4096];
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets = new int[TOTAL_ATMOSPHERES];
    private final int[] atmosphereTimestamps = new int[TOTAL_ATMOSPHERES];
    private List<Boolean> sectorFree;
    private int fileSizeChangeDelta;
    private long lastModifiedTime;

    public AtmosphereRegionFile(@Nonnull File fileNameIn) {
        this.fileName = fileNameIn;
        this.fileSizeChangeDelta = 0;

        try {
            if (fileNameIn.exists()) {
                this.lastModifiedTime = fileNameIn.lastModified();
            }

            this.dataFile = new RandomAccessFile(fileNameIn, "rw");
            if (this.dataFile.length() < 4096L) {
                int i = HEAD_SECTORS;
                while ((i--)>0){
                    this.dataFile.write(EMPTY_SECTOR);
                }
                this.fileSizeChangeDelta += HEAD_SECTORS*4096;
            }

            if ((this.dataFile.length() & 4095L) != 0L) { //检查文件大小是否是4096的倍数
                for (long i = 0; i < (this.dataFile.length() & 4095L); i++) {
                    this.dataFile.write(0);
                }
            }

            int totalSectors = (int)(this.dataFile.length() / 4096L);
            this.sectorFree = Lists.newArrayListWithCapacity(totalSectors);

            for (int i = 0; i < totalSectors; ++i) {
                this.sectorFree.add(Boolean.TRUE);
            }
            for(int i=0;i<HEAD_SECTORS;i++){
                this.sectorFree.set(i,Boolean.FALSE); //设置开头的已被占用
            }
            this.dataFile.seek(0L);

            for (int i = 0; i < TOTAL_ATMOSPHERES; i++) {
                int offset = this.dataFile.readInt();
                this.offsets[i] = offset;

                int length = offset & 255;
                if (offset != 0 && (offset >> 8) + length <= this.sectorFree.size()) { //一个已储存的区块
                    for (int sector_loc = 0; sector_loc < length; sector_loc++) {
                        this.sectorFree.set((offset >> 8) + sector_loc, Boolean.FALSE);
                    }
                } else if (length > 0)
                    APIUtil.LOGGER.warn("Invalid atmosphere at chunk: ({}, {}) Offset: {} Length: {} runs off end file. {}"
                            , i % ATMOSPHERES_PER_ROW, i / ATMOSPHERES_PER_ROW, offset >> 8, length, fileNameIn);
            }

            for (int i = 0; i < TOTAL_ATMOSPHERES; i++) {
                int time = this.dataFile.readInt();
                this.atmosphereTimestamps[i] = time;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean atmosphereExists(int x, int z) {
        return isAtmosphereSaved(x, z);
    }

    @Nullable
    public synchronized DataInputStream getAtmosphereDataInputStream(int x, int z) {
        if (this.outOfBounds(x, z))
            return null;
        try {
            int i = this.getOffset(x, z);

            if (i == 0) {
                return null;
            }

            int begin = i >> 8;
            int sectors = i & 0b11111111;

            if (begin + sectors > this.sectorFree.size()) {
                return null;
            }
            this.dataFile.seek(begin * 4096L);
            int dataBytes = this.dataFile.readInt();

            if (dataBytes > 4096 * sectors) {
                APIUtil.LOGGER.warn("Invalid atmosphere: ({}, {}) Offset: {} Invalid Size: {}>{} {}", x, z, begin, dataBytes, sectors * 4096, fileName);
                return null;
            } else if (dataBytes <= 0) {
                APIUtil.LOGGER.warn("Invalid atmosphere: ({}, {}) Offset: {} Invalid Size: {} {}", x, z, begin, dataBytes, fileName);
                return null;
            }

            byte compressType = this.dataFile.readByte();

            if (compressType == 1) {
                byte[] contentBytes = new byte[dataBytes - 1];
                this.dataFile.read(contentBytes);
                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(contentBytes))));
            } else if (compressType == 2) {
                byte[] contentBytes = new byte[dataBytes - 1];
                this.dataFile.read(contentBytes);
                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(contentBytes))));
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public DataOutputStream getAtmosphereDataOutputStream(int x, int z) {
        return this.outOfBounds(x, z) ? null : new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new AtmosphereBuffer(x, z))));
    }

    protected synchronized void write(int x, int z,@Nonnull byte[] data, int length) {
        try {
            int i = this.getOffset(x, z);
            int begin = i >> 8;
            int sectors = i & 255;
            int usedSectors = (length + 5) / 4096 + 1;

            if (begin != 0 && sectors == usedSectors) {
                this.write(begin, data, length); // 不用扩容或压缩
            } else {
                for (int sector_loc = 0; sector_loc < sectors; sector_loc++) {
                    this.sectorFree.set(begin + sector_loc, Boolean.TRUE); //释放原有扇区
                }

                int newBegin = this.sectorFree.indexOf(Boolean.TRUE);
                int maxAvailableSectors = 0;

                if (newBegin != -1) { //寻找文件内是否有可用空间
                    for (int loc = newBegin; loc < this.sectorFree.size(); loc++) {
                        if (maxAvailableSectors != 0) {
                            if (this.sectorFree.get(loc)) {
                                maxAvailableSectors++;
                            } else {
                                maxAvailableSectors = 0;
                            }
                        } else if (this.sectorFree.get(loc)) {
                            newBegin = loc;
                            maxAvailableSectors = 1;
                        }

                        if (maxAvailableSectors >= usedSectors) {
                            break;
                        }
                    }
                }

                if (maxAvailableSectors >= usedSectors) {
                    begin = newBegin;
                    this.setOffset(x, z, begin << 8 | (Math.min(usedSectors, 255)));

                    for (int sector_loc = 0; sector_loc < usedSectors; sector_loc++) {
                        this.sectorFree.set(begin + sector_loc, Boolean.FALSE); //标记占用
                    }

                    this.write(begin, data, length);
                } else { //NO! 没有!
                    this.dataFile.seek(this.dataFile.length());
                    begin = this.sectorFree.size();

                    for (int sector_loc = 0; sector_loc < usedSectors; sector_loc++) {
                        this.dataFile.write(EMPTY_SECTOR);
                        this.sectorFree.add(Boolean.FALSE);
                    }

                    this.fileSizeChangeDelta += 4096 * usedSectors;
                    this.write(begin, data, length);
                    this.setOffset(x, z, begin << 8 | (Math.min(usedSectors, 255)));
                }
            }

            this.setAtmosphereTimestamp(x, z, (int)(MinecraftServer.getCurrentTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void write(int sectorNumber,@Nonnull byte[] data, int length) throws IOException {
        this.dataFile.seek(sectorNumber * 4096L);
        this.dataFile.writeInt(length + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(data, 0, length);
    }

    private boolean outOfBounds(int x, int z) {
        return x < 0 || x >= ATMOSPHERES_PER_ROW || z < 0 || z >= ATMOSPHERES_PER_ROW;
    }

    private int getOffset(int x, int z) {
        return this.offsets[x + z * ATMOSPHERES_PER_ROW];
    }

    public boolean isAtmosphereSaved(int x, int z) {
        return this.getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        this.offsets[x + z * ATMOSPHERES_PER_ROW] = offset;
        this.dataFile.seek((x + (long) z * ATMOSPHERES_PER_ROW) * 4);
        this.dataFile.writeInt(offset);
    }

    private void setAtmosphereTimestamp(int x, int z, int timestamp) throws IOException {
        this.atmosphereTimestamps[x + z * ATMOSPHERES_PER_ROW] = timestamp;
        this.dataFile.seek(4096*TIME_SECTORS_BEGIN + (x + (long) z * ATMOSPHERES_PER_ROW) * 4);
        this.dataFile.writeInt(timestamp);
    }

    public void close() throws IOException {
        if (this.dataFile != null) {
            this.dataFile.close();
        }
    }

    protected class AtmosphereBuffer extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;

        public AtmosphereBuffer(int x, int z) {
            super(8096);
            this.chunkX = x;
            this.chunkZ = z;
        }

        public void close() {
            AtmosphereRegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}
