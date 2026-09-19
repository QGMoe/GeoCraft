/*
 * Copyright 2026 QGMoe
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
 * 版权所有 2026 QGMoe
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

package moe.qingu.orbtellus.api.laminarifer.request;

import moe.qingu.orbtellus.api.fluid.unit.FluidUnit;
import moe.qingu.orbtellus.api.laminarifer.Laminarifers;
import moe.qingu.orbtellus.api.laminarifer.flow.source.IFlowSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author QGMoe
 */
@NotThreadSafe
public final class FillLaminariferRequest extends SpecificLaminariferRequest<FillLaminariferRequest>{
    private @Nullable IFlowSource<?> fillSource;

    @Nonnull
    public FillLaminariferRequest source(final @Nullable IFlowSource<?> source){
        this.fillSource = source;
        return this;
    }

    public boolean test(){
        switch (status){
            case STATUS_NONE:{
                if(laminarifer.canFill(world,pos,state,side,fluid,nbt,fillSource)){
                    this.status = STATUS_ALLOWED;
                    return true;
                }
                else{
                    this.status = STATUS_REFUSED;
                    return false;
                }
            }case STATUS_REFUSED: return false;
            case STATUS_ALLOWED: return true;
            default: throw new IllegalStateException();
        }
    }

    public long fill(final boolean doOperate){
        switch (status){
            case STATUS_REQUESTED: throw new IllegalStateException();
            case STATUS_NONE:{
                if(laminarifer.canFill(world,pos,state,side,fluid,nbt,fillSource)) this.status = STATUS_ALLOWED; //继续ALLOWED的分支
                else{
                    this.status = STATUS_REFUSED;
                    return 0L;
                }
            } case STATUS_ALLOWED: {
                if(doOperate) this.status = STATUS_REQUESTED;
                return Laminarifers.addAmountInQB(laminarifer, world, pos, state, fluid, nbt, amount, doOperate, pulse, fillSource, modifier);
            }
            case STATUS_REFUSED:
            default:return 0L;
        }
    }

    public long fillAs(final boolean doOperate, final long granularity){
        return FluidUnit.sample(world.rand, fill(doOperate), granularity);
    }

    @Override
    public void close(){
        this.fillSource = null;
        super.close();
    }
}
