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

package moe.qingu.geocraft.world.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import moe.qingu.geocraft.api.fluidphysics.task.FluidTaskRegistry;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.StartupQuery;
import moe.qingu.geocraft.GeoCraft;
import moe.qingu.geocraft.api.fluidphysics.FluidPhysicsMode;
import moe.qingu.geocraft.api.soil.SoilSystem;
import moe.qingu.geocraft.util.MiscUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author QGMoe
 */
public final class GeoDataFile {
    public static final String FILE_NAME = "GeoLevelData.dat";
    private static final long CURRENT_VER = 1L;
    private static final String KEY_VERSION = "version";
    private static final String KEY_FLUID_PHYSICS_MODE = "fluidPhysicsMode";
    private static final String KEY_SOIL_SYSTEM_STATUS = "soilSystemStatus";
    private static final String KEY_FLUID_TASKS_MAPPING = "fluidTasksMapping";
    public static GeoDataFile CURRENT;

    private static final ArrayList<Supplier<Boolean>> validators = new ArrayList<>(
            Arrays.asList(()->{
                final ActionResult<FluidPhysicsMode> modeInSave = CURRENT.getFluidPhysicsMode();
                switch (modeInSave.getType()){
                    case SUCCESS:{
                        if(modeInSave.getResult() != FluidPhysicsMode.getCurrentMode())
                            return StartupQuery.confirm(MiscUtil.translate("geocraft.storage.mismatch.fluidphysics",FluidPhysicsMode.getCurrentMode(),modeInSave.getResult()));
                        return true;
                    }case FAIL:{
                        return StartupQuery.confirm(MiscUtil.translate("geocraft.storage.mismatch.fluidphysics.unknown",FluidPhysicsMode.getCurrentMode()));
                    } default:return true;
                }},()->{
                final ActionResult<Boolean> soilStatusInSave = CURRENT.getSoilSystemStatus();
                switch (soilStatusInSave.getType()){
                    case SUCCESS:{
                        if(soilStatusInSave.getResult() != SoilSystem.getStatus())
                            return StartupQuery.confirm(MiscUtil.translate("geocraft.storage.mismatch.soil",SoilSystem.getStatus(),soilStatusInSave.getResult()));
                        return true;
                    }case FAIL:{
                        return StartupQuery.confirm(MiscUtil.translate("geocraft.storage.mismatch.soil.unknown",SoilSystem.getStatus()));
                    } default:return true;
                }})
    );

    public final File file;
    private boolean trash = false;
    private NBTTagCompound data;

    public GeoDataFile(final @Nonnull File dir){
        this.file = new File(dir,FILE_NAME);
        if(!this.file.exists()){
            this.data = new NBTTagCompound();
            return;
        }
        try (final @Nonnull FileInputStream s = new FileInputStream(file)) {
            this.data = CompressedStreamTools.readCompressed(s);
        } catch (@Nonnull final IOException e) {
            GeoCraft.getLogger().error("Couldn't read data file:",e);
            this.data = buildErrorData();
        }
    }

    public static void init(final @Nonnull File dir){
        CURRENT = new GeoDataFile(dir);
    }

    public static boolean validateEqualization(){
        for(final Supplier<Boolean> validator:validators){
            if(!validator.get()){
                StartupQuery.abort();
                return false;
            }
        }
        return true;
    }

    public static void captureCurrentState(){
        CURRENT.setFluidPhysicsMode(FluidPhysicsMode.getCurrentMode());
        CURRENT.setSoilSystemStatus(SoilSystem.getStatus());
        CURRENT.setFluidTasksMapping(FluidTaskRegistry.getMapping());
        CURRENT.save();
    }

    @Nonnull
    public ActionResult<FluidPhysicsMode> getFluidPhysicsMode(){
        final int val = this.data.getInteger(KEY_FLUID_PHYSICS_MODE);
        if(val >0 && val <= 3){
            return ActionResult.newResult(EnumActionResult.SUCCESS, FluidPhysicsMode.values()[val-1]);
        }else if(val == 0) return ActionResult.newResult(EnumActionResult.PASS,FluidPhysicsMode.FINITE);//这里的模式没用
        else return ActionResult.newResult(EnumActionResult.FAIL,FluidPhysicsMode.FINITE);//同样没用
    }

    @Nonnull
    public ActionResult<Boolean> getSoilSystemStatus(){
        final byte res = this.data.getByte(KEY_SOIL_SYSTEM_STATUS);
        switch (res){
            case 0:return ActionResult.newResult(EnumActionResult.PASS,Boolean.TRUE);
            case 1:return ActionResult.newResult(EnumActionResult.SUCCESS,Boolean.FALSE);
            case 2:return ActionResult.newResult(EnumActionResult.SUCCESS,Boolean.TRUE);
            default:return ActionResult.newResult(EnumActionResult.FAIL,Boolean.TRUE);
        }
    }

    @Nonnull
    public Int2ObjectOpenHashMap<ResourceLocation> getFluidTasksMapping(){
        final Int2ObjectOpenHashMap<ResourceLocation> mapping = new Int2ObjectOpenHashMap<>();
        final NBTTagList arr = this.data.getTagList(KEY_FLUID_TASKS_MAPPING, Constants.NBT.TAG_COMPOUND);
        for (int i=0;i<arr.tagCount();i++){
            final NBTTagCompound compound = arr.getCompoundTagAt(i);
            if(compound.hasKey("K") && compound.hasKey("V")) mapping.put(compound.getInteger("V"),new ResourceLocation(compound.getString("K")));
        }
        return mapping;
    }

    public void setFluidTasksMapping(final @Nonnull Int2ObjectMap<ResourceLocation> mapping){
        final NBTTagList list = new NBTTagList();
        for(final @Nonnull Int2ObjectMap.Entry<ResourceLocation> entry:mapping.int2ObjectEntrySet()){
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setString("K",entry.getValue().toString());
            compound.setInteger("V",entry.getIntKey());
            list.appendTag(compound);
        }
        this.data.setTag(KEY_FLUID_TASKS_MAPPING,list);
    }

    public void setFluidPhysicsMode(final @Nonnull FluidPhysicsMode mode){
        this.data.setInteger(KEY_FLUID_PHYSICS_MODE,mode.ordinal()+1);
    }

    public void setSoilSystemStatus(final boolean status){
        this.data.setByte(KEY_SOIL_SYSTEM_STATUS,(byte) (status?2:1));
    }

    public void setTrash(final boolean trash) {
        this.trash = trash;
    }

    public boolean isTrash() {
        return trash;
    }

    public void save(){
        this.data.setLong(KEY_VERSION,CURRENT_VER);
        if(isTrash()) return;
        try (final @Nonnull FileOutputStream s = new FileOutputStream(file)) {
            CompressedStreamTools.writeCompressed(this.data, s);
        } catch (final @Nonnull IOException e) {
            GeoCraft.getLogger().error("Couldn't save data file:",e);
        }
    }

    @Nonnull
    private static NBTTagCompound buildErrorData(){
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setLong(KEY_VERSION,CURRENT_VER);
        compound.setInteger(KEY_FLUID_PHYSICS_MODE,-1);
        compound.setByte(KEY_SOIL_SYSTEM_STATUS,(byte) -1);
        return compound;
    }
}
