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

package top.qiguaiaaaa.geocraft.api.configs.item.collection.list;

import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import top.qiguaiaaaa.geocraft.api.configs.ConfigCategory;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.ConfigurableList;
import top.qiguaiaaaa.geocraft.api.configs.value.collection.IConfigurableList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * @author QiguaiAAAA
 */
public class ConfigIntegerWeightDistribution extends ConfigIntegerList<ConfigIntegerWeightDistribution> {
    protected int sum = 0;
    protected int begin = 0;

    public ConfigIntegerWeightDistribution(final @Nonnull ConfigCategory category,
                                           final @Nonnull String configKey,
                                           final @Nonnull ConfigurableList<Integer> defaultValue) {
        super(category, configKey, defaultValue);
    }

    public void reloadWeights(){
        sum = 0;
        for(int i:value){
            sum += i;
        }
    }

    @Override
    public void setValue(@Nonnull final IConfigurableList<Integer> newValue) {
        super.setValue(newValue);
        reloadWeights();
    }

    @Nonnull
    public ConfigIntegerWeightDistribution setBegin(final int begin) {
        this.begin = begin;
        return this;
    }

    public int getBegin() {
        return begin;
    }

    @Override
    public void load(@Nonnull final Configuration config) {
        super.load(config);
        reloadWeights();
    }

    @Nonnull
    @Override
    protected List<Pair<String, String>> getCommentProperties() {
        final List<Pair<String,String>> list = super.getCommentProperties();
        list.add(Pair.of("分布起始值 Distribution Begin",String.valueOf(begin)));
        return list;
    }


    @Nonnull
    @Override
    public String getTypeTranslationKey() {
        return "geocraft.config.type.collection.list.int.distribution";
    }

    public int getRandomResult(final @Nonnull Random rnd){
        int rndRes = rnd.nextInt(sum);
        int accumulation = 0;
        for(int cur = begin,i =0;i<value.size();i++,cur++){
            accumulation += value.get(i);
            if(rndRes < accumulation) return cur;
        }
        return begin;
    }
}
