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

package top.qiguaiaaaa.geocraft_test.world.sandbox;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import top.qiguaiaaaa.geocraft_test.util.IOTriConsumer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author QiguaiAAAA
 */
public class SandboxTestCase {
    public static final String INPUT_FILE_EXT = "in";
    public static final String ANSWER_FILE_EXT = "ans";

    public final String[] structure;
    public final int xLength;
    public final int zLength;

    public SandboxTestCase(final @Nonnull String[] structure,final int size) {
        this.structure = structure;
        this.xLength = size;
        this.zLength = size;
    }


    public SandboxTestCase(final @Nonnull String[] structure,final int zLength,final int xLength) {
        this.structure = structure;
        this.xLength = xLength;
        this.zLength = zLength;
    }

    public static void findInputs(final @Nonnull String dataDir, final @Nonnull IOTriConsumer<ScanResult, Resource, Scanner> forEachInput){
        try (final @Nonnull ScanResult scan = new ClassGraph().acceptPaths(dataDir).scan()){
            findInputs(scan,forEachInput);
        }
    }

    public static void findInputs(final @Nonnull ScanResult scan, final @Nonnull IOTriConsumer<ScanResult, Resource, Scanner> forEachInput){
        scan.getResourcesWithExtension(SandboxTestCase.INPUT_FILE_EXT).forEach(in ->{
            try (final Scanner scannerIn = getScannerOf(in)){
                forEachInput.accept(scan,in,scannerIn);
            } catch (final IOException e) {
                Assertions.fail("IOException when reading input test file "+in.getPath(),e);
            }
        });
    }

    @Nonnull
    public static Resource getAnswerByInput(final @Nonnull ScanResult scan,final @Nonnull Resource in){
        final String outPath = in.getPath().replaceAll("\\."+INPUT_FILE_EXT+"$", "."+ANSWER_FILE_EXT);
        return scan.getResourcesWithPath(outPath).get(0);
    }

    @Nonnull
    public static Scanner getScannerOf(final @Nonnull Resource res) throws IOException{
        return new Scanner(res.open(),StandardCharsets.UTF_8.name());
    }

    public static void buildStructureFromScanner(final @Nonnull String[] structure,final @Nonnull Scanner scanner,final int zLength){
        for (int y = 0;y<structure.length;y++){
            final StringBuilder builder = new StringBuilder();
            for(int z=0;z<zLength;z++){
                Assertions.assertTrue(scanner.hasNextLine());
                builder.append(scanner.nextLine().split("#",2)[0].codePoints()
                                .filter(code -> !Character.isWhitespace(code))
                                .collect(StringBuilder::new,
                                        StringBuilder::appendCodePoint,
                                        StringBuilder::append))
                        .append('\n');
            }
            structure[y] = builder.toString();
            if(y == structure.length-1) break;
            scanner.nextLine(); //jump empty line
        }
    }
}
