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

package top.qiguaiaaaa.geocraft.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;

/**
 * @author QiguaiAAAA
 */
@SuppressWarnings("unused")
public final class FluidloggedAPICompatTransformer implements IClassTransformer {
    static final String IFluidloggableClass = "git/jbredwards/fluidlogged_api/api/block/IFluidloggable";
    static final String IFluidloggableLayeredHostClass = "top/qiguaiaaaa/geocraft/api/block/IFluidloggableLayeredFluidHost";

    @Override
    @Nullable
    public byte[] transform(@Nullable String name, @Nullable String transformedName, @Nullable byte[] basicClass) {
        if (basicClass == null || transformedName == null) return basicClass;
        try{
            final ClassReader reader = new ClassReader(basicClass);
            final ClassNode root = new ClassNode();
            reader.accept(root,0);
            boolean modified = false;
            if(root.interfaces.contains(IFluidloggableClass)){
                if(!root.interfaces.contains(IFluidloggableLayeredHostClass)){
                    modified = true;
                    //将所有含水方块桥接为载流方块
                    root.interfaces.add(IFluidloggableLayeredHostClass);
                }
            }
            if(!modified) return basicClass;
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            root.accept(writer);
            System.out.println("[GeoCraft] Layered "+transformedName+" Successfully!");
            return writer.toByteArray();
        }catch (Throwable e){
            e.printStackTrace();
            return basicClass;
        }
    }
}
