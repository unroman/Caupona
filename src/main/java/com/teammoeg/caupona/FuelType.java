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
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.caupona;

import com.teammoeg.caupona.CPTags.Items;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum FuelType {
	WOODS(2,Items.FUEL_WOODS), CHARCOAL(1,Items.FUEL_CHARCOALS), FOSSIL(3,Items.FUEL_FOSSIL), GEOTHERMAL(0,Items.FUEL_LAVA),
	OTHER(0, Items.FUEL_OTHERS);

	private final int modelId;
	private final TagKey<Item> it;

	private FuelType(int modelId,TagKey<Item> tag ){
		this.modelId = modelId;
		it = tag;
	}

	public static FuelType getType(ItemStack is) {
		for (FuelType ft : FuelType.values()) {
			if (is.is(ft.it))
				return ft;
		}
		return FuelType.OTHER;
	}

	public int getModelId() {
		return modelId;
	}
}
