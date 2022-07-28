/*
 * Copyright (c) 2022 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.caupona.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public class SpicedFoodInfo {
	public MobEffectInstance spice;
	public boolean hasSpice=false;
	public ResourceLocation spiceName;
	public SpicedFoodInfo() {
	}
	public SpicedFoodInfo(CompoundTag nbt) {
		hasSpice=nbt.getBoolean("hasSpice");
		if(nbt.contains("spice"))
			spice=MobEffectInstance.load(nbt.getCompound("spice"));
		if(nbt.contains("spiceName"))
			spiceName=new ResourceLocation(nbt.getString("spiceName"));
	}
	public boolean addSpice(MobEffectInstance spice,ItemStack im) {
		if(this.spice!=null)return false;
		this.spice=new MobEffectInstance(spice);
		hasSpice=true;
		this.spiceName=im.getItem().getRegistryName();
		return true;
	}
	public void clearSpice() {
		spice=null;
		hasSpice=false;
		spiceName=null;
	}
	public boolean canAddSpice() {
		return !hasSpice;
	}
	public void write(CompoundTag nbt) {
		nbt.putBoolean("hasSpice",hasSpice);
		if(spice!=null)
			nbt.put("spice",spice.save(new CompoundTag()));
		if(spiceName!=null)
			nbt.putString("spiceName",spiceName.toString());
	}

	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		write(nbt);
		return nbt;
	}
}
