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

package 清汩萌.造.测试;

import org.junit.jupiter.api.Test;
import 清汩萌.造.空间.空间假设;
import 清汩萌.造.空间.词块网格;

import static 清汩萌.造.空间.ByteGridBuilder.grid;
import static 清汩萌.造.空间.亮度构造器.*;
/**
 * @author QGMoe
 */
public final class 测试亮度构造器 {
    @Test
    public void 测试基本构造(){
        空间假设.假设构造相同(
                grid().layer()
                        .row(一,二,三,四)
                        .row(五,六,七,八)
                        .row(九,十,B,C).done()
                        .layer()
                        .row(零,零,零,零)
                        .row(D,D,D,D)
                        .row(E,E,E,E)
                        .done().build(),
                $亮度构造器.构造(new 词块网格().层()
                        .行("一二三四")
                        .行("五六七八")
                        .行("九A B C").完成()
                        .层()
                        .行("零零零零")
                        .行("D D D D")
                        .行("E E E E")
                        .完成())
        );
    }
}
