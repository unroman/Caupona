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

package com.teammoeg.caupona.datagen;

import java.util.concurrent.CompletableFuture;

import com.teammoeg.caupona.CPMain;
import com.teammoeg.caupona.util.Utils;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CPMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CPDataGenerator {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		ExistingFileHelper exHelper = event.getExistingFileHelper();
		
		CompletableFuture<HolderLookup.Provider> completablefuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
		gen.addProvider(event.includeClient(),new CPItemModelProvider(gen, CPMain.MODID, exHelper));
		gen.addProvider(event.includeServer(),new CPRecipeProvider(gen));
		gen.addProvider(event.includeServer(),new CPItemTagGenerator(gen, CPMain.MODID, exHelper,event.getLookupProvider()));
		gen.addProvider(event.includeServer(),new CPBlockTagGenerator(gen, CPMain.MODID, exHelper,event.getLookupProvider()));
		gen.addProvider(event.includeServer(),new CPFluidTagGenerator(gen, CPMain.MODID, exHelper,event.getLookupProvider()));
		gen.addProvider(event.includeServer(),new CPLootGenerator(gen));
		gen.addProvider(event.includeClient()||event.includeServer(),new CPStatesProvider(gen, CPMain.MODID, exHelper));
		gen.addProvider(event.includeServer(),new CPBookGenerator(gen.getPackOutput(), exHelper));
		gen.addProvider(event.includeServer()||event.includeClient(),new PackMetadataGenerator(gen.getPackOutput()).add(PackMetadataSection.TYPE,new PackMetadataSection(Utils.string("Caupona Resources"),6)));
		gen.addProvider(event.includeServer(),new CPRegistryGenerator(gen.getPackOutput(),completablefuture));
		gen.addProvider(event.includeClient(),new FluidAnimationGenerator(gen.getPackOutput(),exHelper));
		gen.addProvider(event.includeClient()||event.includeServer(), new RegistryJavaGenerator(gen.getPackOutput(),exHelper));
	}
}
