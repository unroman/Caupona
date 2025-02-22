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

package com.teammoeg.caupona.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.teammoeg.caupona.CPBlocks;
import com.teammoeg.caupona.blocks.dolium.CounterDoliumBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class CounterDoliumRenderer implements BlockEntityRenderer<CounterDoliumBlockEntity> {

	/**
	 * @param rendererDispatcherIn  
	 */
	public CounterDoliumRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
	}


	private static Vector3f clr(int col) {
		return new Vector3f((col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f);
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@Override
	public void render(CounterDoliumBlockEntity blockEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer,
			int combinedLightIn, int combinedOverlayIn) {
		if (!blockEntity.getLevel().hasChunkAt(blockEntity.getBlockPos()))
			return;
		BlockState state = blockEntity.getBlockState();
		if (!CPBlocks.dolium.contains(state.getBlock()))
			return;

		if (blockEntity.tank.isEmpty())
			return;
		FluidStack fs = blockEntity.tank.getFluid();
		matrixStack.pushPose();
		if (fs != null && !fs.isEmpty() && fs.getFluid() != null) {
			float rr = (fs.getAmount() / 1250f) * 0.5f + 0.375f;
			matrixStack.translate(0, rr, 0);
			matrixStack.mulPose(new Quaternion(90, 0, 0, true));

			VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
			IClientFluidTypeExtensions attr=IClientFluidTypeExtensions.of(fs.getFluid());
			TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS)
					.getSprite(attr.getStillTexture(fs));
			int col = attr.getTintColor(fs);
			int iW = sprite.getWidth();
			int iH = sprite.getHeight();
			if (iW > 0 && iH > 0) {
				Vector3f clr;
				float alp = 1f;
				clr = clr(col);
				RenderUtils.drawTexturedColoredRect(builder, matrixStack, .125f, .125f, .75f, .75f, clr.x(), clr.y(),
						clr.z(), alp, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), combinedLightIn,
						combinedOverlayIn);

			}
		}

		matrixStack.popPose();
	}

}