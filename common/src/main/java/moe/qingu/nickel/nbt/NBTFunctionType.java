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

package moe.qingu.nickel.nbt;

import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QGMoe
 */
public final class NBTFunctionType {
    public static final Map<Class<? extends NBTBase>, String> TYPES = new HashMap<>();
    private final Class<? extends NBTBase>[] inputs;

    static {
        TYPES.put(NBTBase.class, "BASE");
        TYPES.put(NBTPrimitive.class, "NUMBER");
        TYPES.put(NBTTagEnd.class, "END");
        TYPES.put(NBTTagByte.class, "BYTE");
        TYPES.put(NBTTagShort.class, "SHORT");
        TYPES.put(NBTTagInt.class, "INT");
        TYPES.put(NBTTagLong.class, "LONG");
        TYPES.put(NBTTagFloat.class, "FLOAT");
        TYPES.put(NBTTagDouble.class, "DOUBLE");
        TYPES.put(NBTTagString.class, "STRING");
        TYPES.put(NBTTagList.class, "LIST");
        TYPES.put(NBTTagCompound.class, "COMPOUND");
        TYPES.put(NBTTagByteArray.class, "BYTE[]");
        TYPES.put(NBTTagIntArray.class, "INT[]");
        TYPES.put(NBTTagLongArray.class, "LONG[]");
    }

    public NBTFunctionType(final @Nonnull Class<? extends NBTBase>[] inputs) {
        this.inputs = inputs.clone();
    }

    @Nullable
    public static <T> T resolve(final @Nonnull Map<NBTFunctionType,T> candidates, final @Nonnull NBTBase[] args){
        T bestOpt = null;
        int bestScore = Integer.MAX_VALUE;
        boolean ambiguous = false;
        outer:
        for(final Map.Entry<NBTFunctionType,T> entry:candidates.entrySet()){
            final NBTFunctionType type = entry.getKey();
            if(type.getParameterCount() != args.length) continue;

            int score = 0;
            for(int i=0;i<args.length;i++){
                if(args[i] == null) return null;
                final int dis = distance(args[i].getClass(),type.getInputTypeAt(i));
                if(dis <0 ) continue outer;
                score += dis;
            }

            if(score < bestScore){
                bestScore = score;
                ambiguous  =false;
                bestOpt = entry.getValue();
            } else if(score == bestScore) ambiguous = true;
        }
        if(ambiguous) return null;
        return bestOpt;
    }

    public static int distance(final @Nonnull Class<? extends NBTBase> arg, final @Nonnull Class<? extends NBTBase> para){
        int dis = 0;
        for (Class<?> c = arg; c != null; c = c.getSuperclass(),dis++){
            if(c == para) return dis;
        }
        return -1;
    }

    @Nonnull
    public Class<? extends NBTBase>[] getInputTypes() {
        return inputs.clone();
    }

    public int getParameterCount() {
        return inputs.length;
    }

    @Nonnull
    public Class<? extends NBTBase> getInputTypeAt(final int loc) {
        return inputs[loc];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(inputs);
    }

    @Override
    public boolean equals(final @Nonnull Object obj) {
        if (obj instanceof NBTFunctionType) {
            return Arrays.equals(inputs, ((NBTFunctionType) obj).inputs);
        } else return false;
    }

    @Override
    public String toString() {
        final String[] types = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) types[i] = TYPES.get(inputs[i]);
        return '(' + String.join(",", types) + ')';
    }
}
