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

package com.teammoeg.caupona.compat.jei.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teammoeg.caupona.CPItems;
import com.teammoeg.caupona.CPMain;
import com.teammoeg.caupona.data.recipes.StewBaseCondition;
import com.teammoeg.caupona.data.recipes.StewCookingRecipe;
import com.teammoeg.caupona.util.Utils;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class StewCookingCategory extends IConditionalCategory<StewCookingRecipe> {
	public static RecipeType<StewCookingRecipe> TYPE=RecipeType.create(CPMain.MODID, "stew_cooking",StewCookingRecipe.class);
	private IDrawable ICON;
	private IGuiHelper helper;

	public StewCookingCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		this.ICON = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CPItems.anyWater.get()));
		this.helper = guiHelper;
	}

	public Component getTitle() {
		return Utils.translate("gui.jei.category." + CPMain.MODID + ".stew_cooking.title");
	}
	@Override
	public void drawCustom(StewCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
			double mouseY) {
		IDrawable density;
		if(recipe.getDensity()<0.4)
			density=DENSITY[0];
		else if(recipe.getDensity()<0.6)
			density=DENSITY[1];
		else if(recipe.getDensity()<0.8)
			density=DENSITY[2];
		else if(recipe.getDensity()<1.2)
			density=DENSITY[3];
		else
			density=DENSITY[4];
		density.draw(stack,25,15);
	}
	@Override
	public void draw(StewCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
			double mouseY) {
		
		ResourceLocation imagePath=new ResourceLocation(recipe.getId().getNamespace(),"textures/gui/recipes/" + recipe.getId().getPath() + ".png");
		if(Minecraft.getInstance().getResourceManager().getResource(imagePath).isPresent()) {
			stack.pushPose();
			stack.scale(0.5f, 0.5f, 0);
			helper.createDrawable(imagePath, 0, 0, 200, 210).draw(stack);
			stack.popPose();
		}else {
			super.draw(recipe, recipeSlotsView, stack, mouseX, mouseY);
		}
	}

	@Override
	public IDrawable getIcon() {
		return ICON;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, StewCookingRecipe recipe, IFocusGroup focuses) {
		if (recipe.getBase() != null && recipe.getBase().size() > 0) {
			List<FluidStack> fss = new ArrayList<>();
			for (Fluid f : ForgeRegistries.FLUIDS) {
				for (StewBaseCondition base : recipe.getBase())
					if (base.test(f))
						fss.add(new FluidStack(f, 250));
			}
			builder.addSlot(RecipeIngredientRole.INPUT, 30, 13).addIngredients(ForgeTypes.FLUID_STACK, fss)
					.setFluidRenderer(250, false, 16, 16);
		} else
			builder.addSlot(RecipeIngredientRole.INPUT, 30, 13).addIngredient(VanillaTypes.ITEM_STACK,
					new ItemStack(CPItems.any.get()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 18)
				.addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(recipe.output, 250))
				.setFluidRenderer(250, false, 16, 16);
	}

	@Override
	public List<Component> getTooltipStrings(StewCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX,
			double mouseY) {
		if (inRange(mouseX, mouseY, 21, 6, 34, 30)) {
			return Arrays.asList(Utils.translate("recipe.caupona.density", recipe.getDensity()));
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public RecipeType<StewCookingRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public IDrawable getHeadings() {
		return POT_HEADING;
	}



}
