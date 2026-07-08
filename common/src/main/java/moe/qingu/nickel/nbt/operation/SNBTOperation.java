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

package moe.qingu.nickel.nbt.operation;

import moe.qingu.nickel.command.exception.NickelRuntimeException;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QGMoe
 */
@FunctionalInterface
public interface SNBTOperation {
    @Nonnull
    NBTBase invoke(final @Nonnull NBTBase[] args) throws NickelRuntimeException;

    static int distance(final @Nonnull Class<? extends NBTBase> arg,final @Nonnull Class<? extends NBTBase> para){
        int dis = 0;
        for (Class<?> c = arg; c != null; c = c.getSuperclass(),dis++){
            if(c == para) return dis;
        }
        return -1;
    }

    final class OperationType{
        public static final Map<Class<? extends NBTBase>,String> TYPES = new HashMap<>();
        private final Class<? extends NBTBase>[] inputs;

        static {
            TYPES.put(NBTBase.class,"BASE");
            TYPES.put(NBTPrimitive.class,"NUMBER");
            TYPES.put(NBTTagEnd.class,"END");
            TYPES.put(NBTTagByte.class,"BYTE");
            TYPES.put(NBTTagShort.class,"SHORT");
            TYPES.put(NBTTagInt.class,"INT");
            TYPES.put(NBTTagLong.class,"LONG");
            TYPES.put(NBTTagFloat.class,"FLOAT");
            TYPES.put(NBTTagDouble.class,"DOUBLE");
            TYPES.put(NBTTagString.class,"STRING");
            TYPES.put(NBTTagList.class,"LIST");
            TYPES.put(NBTTagCompound.class,"COMPOUND");
            TYPES.put(NBTTagByteArray.class,"BYTE[]");
            TYPES.put(NBTTagIntArray.class,"INT[]");
            TYPES.put(NBTTagLongArray.class,"LONG[]");
        }

        public OperationType(final @Nonnull Class<? extends NBTBase>[] inputs) {
            this.inputs = inputs.clone();
        }

        @Nonnull
        public Class<? extends NBTBase>[] getInputTypes() {
            return inputs.clone();
        }

        public int getParameterCount(){
            return inputs.length;
        }

        @Nonnull
        public Class<? extends NBTBase> getInputTypeAt(final int loc){
            return inputs[loc];
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(inputs);
        }

        @Override
        public boolean equals(final @Nonnull Object obj) {
            if(obj instanceof OperationType){
                return Arrays.equals(inputs,((OperationType)obj).inputs);
            }else return false;
        }

        @Override
        public String toString() {
            final String[] types = new String[inputs.length];
            for(int i=0;i<inputs.length;i++) types[i] = TYPES.get(inputs[i]);
            return '('+String.join(",",types)+')';
        }
    }
}
