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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.teammoeg.caupona.blocks.BaseColumnBlock;
import com.teammoeg.caupona.blocks.CPHorizontalBlock;
import com.teammoeg.caupona.blocks.ColumnCapitalBlock;
import com.teammoeg.caupona.blocks.dolium.CounterDoliumBlock;
import com.teammoeg.caupona.blocks.foods.BowlBlock;
import com.teammoeg.caupona.blocks.foods.DishBlock;
import com.teammoeg.caupona.blocks.fumarole.FumaroleBoulderBlock;
import com.teammoeg.caupona.blocks.fumarole.FumaroleVentBlock;
import com.teammoeg.caupona.blocks.fumarole.PumiceBloomBlock;
import com.teammoeg.caupona.blocks.hypocaust.CaliductBlock;
import com.teammoeg.caupona.blocks.hypocaust.FireboxBlock;
import com.teammoeg.caupona.blocks.hypocaust.WolfStatueBlock;
import com.teammoeg.caupona.blocks.others.CPStandingSignBlock;
import com.teammoeg.caupona.blocks.others.CPWallSignBlock;
import com.teammoeg.caupona.blocks.pan.GravyBoatBlock;
import com.teammoeg.caupona.blocks.pan.PanBlock;
import com.teammoeg.caupona.blocks.plants.BushLogBlock;
import com.teammoeg.caupona.blocks.plants.CPStripPillerBlock;
import com.teammoeg.caupona.blocks.plants.FruitBlock;
import com.teammoeg.caupona.blocks.plants.FruitsLeavesBlock;
import com.teammoeg.caupona.blocks.pot.StewPot;
import com.teammoeg.caupona.blocks.stove.ChimneyPotBlock;
import com.teammoeg.caupona.blocks.stove.KitchenStove;
import com.teammoeg.caupona.blocks.stove.KitchenStoveBlockEntity;
import com.teammoeg.caupona.items.CPBlockItem;
import com.teammoeg.caupona.items.DishItem;
import com.teammoeg.caupona.util.MaterialType;
import com.teammoeg.caupona.worldgen.FigTreeGrower;
import com.teammoeg.caupona.worldgen.WalnutTreeGrower;
import com.teammoeg.caupona.worldgen.WolfberryTreeGrower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CPBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CPMain.MODID);
	// static string data
	public static final String[] woods = new String[] { "walnut" };
	public static final MaterialType[] all_materials = new MaterialType[] {
			new MaterialType("mud").makeCounter(1),
			new MaterialType("stone_brick").makeCounter(2).makeHypocaust(),
			new MaterialType("stone").setBase(()->Blocks.STONE.defaultBlockState()).makePillar().makeRoad(),
			new MaterialType("sandstone").setBase(()->Blocks.SANDSTONE.defaultBlockState()).makeRoad(),
			new MaterialType("brick").makeCounter(2).makeHypocaust(),
			new MaterialType("mixed_bricks").makeDecoration(),
			new MaterialType("opus_incertum").makeCounter(2).makeDecoration().makeHypocaust(),
			new MaterialType("opus_latericium").makeCounter(2).makeDecoration().makeHypocaust(),
			new MaterialType("opus_reticulatum").makeDecoration(),
			new MaterialType("felsic_tuff_bricks").makeDecoration(),
			new MaterialType("felsic_tuff").makeDecoration().makePillar().makeRoad(),
			new MaterialType("quartz").makePillar(),
			new MaterialType("calcite").makePillar() };

	// Dynamic block types
	public static final List<Block> signs = new ArrayList<>();
	public static final Map<String, RegistryObject<Block>> stoneBlocks = new HashMap<>();
	public static final List<Block> chimney = new ArrayList<>();
	public static final List<Block> dolium = new ArrayList<>();
	public static final List<Block> dishes = new ArrayList<>();
	public static final List<Block> caliduct = new ArrayList<>();
	public static final List<Block> firebox = new ArrayList<>();
	public static final List<RegistryObject<KitchenStove>> stoves = new ArrayList<>();
	// useful blocks
	public static RegistryObject<StewPot> stew_pot = baseblock("stew_pot",
					() -> new StewPot(Block.Properties.of(Material.STONE).sound(SoundType.STONE)
							.requiresCorrectToolForDrops().strength(3.5f, 10).noOcclusion(),
							CPBlockEntityTypes.STEW_POT));

	public static RegistryObject<BowlBlock> bowl = BLOCKS.register("bowl",
			() -> new BowlBlock(Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD).instabreak()
					.noOcclusion().isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid)
					.isViewBlocking(CPBlocks::isntSolid), CPBlockEntityTypes.BOWL));

	static RegistryObject<KitchenStove> stove(String name, Properties props,
			RegistryObject<BlockEntityType<KitchenStoveBlockEntity>> tile) {
		RegistryObject<KitchenStove> bl = BLOCKS.register(name, () -> new KitchenStove(getStoveProps(), tile));
		CPItems.ITEMS.register(name, () -> new CPBlockItem(bl.get(), CPItems.createProps()));
		return bl;
	}

	static <T extends Block> RegistryObject<T> baseblock(String name, Supplier<T> bl) {
		RegistryObject<T> blx = BLOCKS.register(name, bl);
		CPItems.ITEMS.register(name, () -> new CPBlockItem(blx.get(), CPItems.createProps()));
		return blx;
	}

	static <T extends Block> RegistryObject<T> baseblock(String name, Supplier<T> bl, Function<T, Item> toitem) {
		RegistryObject<T> blx = BLOCKS.register(name, bl);
		CPItems.ITEMS.register(name, () -> toitem.apply(blx.get()));
		return blx;
	}

	static RegistryObject<Block> block(String name, Properties props) {
		RegistryObject<Block> blx = BLOCKS.register(name, () -> new Block(props));
		CPItems.ITEMS.register(name, () -> new CPBlockItem(blx.get(), CPItems.createProps()));
		return blx;
	}

	public static RegistryObject<Block> WALNUT_LOG;
	public static RegistryObject<Block> WALNUT_LEAVE;
	public static RegistryObject<Block> WALNUT_PLANKS;
	public static RegistryObject<Block> WALNUT_SAPLINGS;
	public static RegistryObject<Block> FIG_LOG;
	public static RegistryObject<Block> FIG_LEAVE;
	public static RegistryObject<Block> FIG_SAPLINGS;
	public static RegistryObject<Block> WOLFBERRY_LOG;
	public static RegistryObject<Block> WOLFBERRY_LEAVE;
	public static RegistryObject<Block> WOLFBERRY_SAPLINGS;
	public static final RegistryObject<FumaroleBoulderBlock> FUMAROLE_BOULDER = baseblock("fumarole_boulder",
			() -> new FumaroleBoulderBlock(getStoneProps().isViewBlocking(CPBlocks::isntSolid).noOcclusion()
					.isSuffocating(CPBlocks::isntSolid)));
	public static final RegistryObject<FumaroleVentBlock> FUMAROLE_VENT = baseblock("fumarole_vent",
			() -> new FumaroleVentBlock(getStoneProps().strength(4.5f, 10).isViewBlocking(CPBlocks::isntSolid)
					.noOcclusion().isSuffocating(CPBlocks::isntSolid)));
	public static final RegistryObject<Block> PUMICE = block("pumice", getStoneProps());
	public static final RegistryObject<PumiceBloomBlock> PUMICE_BLOOM = baseblock("pumice_bloom",
			() -> new PumiceBloomBlock(getStoneProps().noOcclusion()));
	public static final RegistryObject<GravyBoatBlock> GRAVY_BOAT = BLOCKS.register("gravy_boat",
			() -> new GravyBoatBlock(Block.Properties.of(Material.DECORATION).sound(SoundType.GLASS).instabreak()
					.noOcclusion().isSuffocating(CPBlocks::isntSolid).isViewBlocking(CPBlocks::isntSolid)));

	public static final WoodType WALNUT = WoodType.register(WoodType.create("caupona:walnut"));
	public static final RegistryObject<WolfStatueBlock> WOLF = baseblock("wolf_statue",
			() -> new WolfStatueBlock(Block.Properties.of(Material.METAL).sound(SoundType.COPPER)
					.requiresCorrectToolForDrops().strength(3.5f, 10).noOcclusion()));
	public static final RegistryObject<PanBlock> STONE_PAN = baseblock("stone_griddle", () -> new PanBlock(
			Block.Properties.of(Material.DECORATION).sound(SoundType.STONE).strength(3.5f, 10).noOcclusion()));
	public static final RegistryObject<PanBlock> COPPER_PAN = baseblock("copper_frying_pan", () -> new PanBlock(
			Block.Properties.of(Material.DECORATION).sound(SoundType.COPPER).strength(3.5f, 10).noOcclusion()));
	public static final RegistryObject<PanBlock> IRON_PAN = baseblock("iron_frying_pan", () -> new PanBlock(
			Block.Properties.of(Material.DECORATION).sound(SoundType.METAL).strength(3.5f, 10).noOcclusion()));
	public static final RegistryObject<DishBlock> DISH = BLOCKS.register("dish",
			() -> new DishBlock(Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD).instabreak()
					.noOcclusion().isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid)
					.isViewBlocking(CPBlocks::isntSolid)));

	static {
		for (MaterialType type : all_materials) {
			String name = type.getName();
			if (type.isDecorationMaterial()) {
				RegistryObject<Block> base = block(name, getStoneProps(),TabType.DECORATION);
				stoneBlocks.put(name, base);
				type.setBase(()->base.get().defaultBlockState());
				baseblock(name + "_slab", () -> new SlabBlock(getStoneProps()));
				baseblock(name + "_stairs", () -> new StairBlock(type.getBase(), getStoneProps()));
				baseblock(name + "_wall", () -> new WallBlock(getStoneProps()));
			}
			if (type.isCounterMaterial()) {
				stove(name + "_kitchen_stove", getStoveProps(),
						type.getCounterGrade() == 1 ? CPBlockEntityTypes.STOVE1 : CPBlockEntityTypes.STOVE2);
				baseblock(name + "_chimney_flue", ()->new ChimneyFluteBlock(getTransparentProps()));
				baseblock(name + "_chimney_pot", () -> new ChimneyPotBlock(getTransparentProps()));
				baseblock(name + "_counter", () -> new CPHorizontalBlock(getStoneProps()));
				baseblock(name + "_counter_with_dolium", () -> new CounterDoliumBlock(getTransparentProps()));
			}
			if (type.isHypocaustMaterial()) {
				baseblock(name + "_caliduct", () -> new CaliductBlock(getTransparentProps()));
				baseblock(name + "_hypocaust_firebox", () -> new FireboxBlock(getTransparentProps()));
			}
			if (type.isPillarMaterial()) {
				baseblock(name + "_column_fluted_plinth",
						() -> new BaseColumnBlock(getTransparentProps().strength(2f, 6f), true));
				baseblock(name + "_column_fluted_shaft",
						() -> new BaseColumnBlock(getTransparentProps().strength(2f, 6f), false));
				baseblock(name + "_column_shaft",
						() -> new BaseColumnBlock(getTransparentProps().strength(2f, 6f), false));
				baseblock(name + "_column_plinth",
						() -> new BaseColumnBlock(getTransparentProps().strength(2f, 6f), true));
				baseblock(name + "_ionic_column_capital",
						() -> new ColumnCapitalBlock(getTransparentProps().strength(2f, 6f), true));
				baseblock(name + "_tuscan_column_capital",
						() -> new ColumnCapitalBlock(getTransparentProps().strength(2f, 6f), false));
				baseblock(name + "_acanthine_column_capital",
						() -> new ColumnCapitalBlock(getTransparentProps().strength(2f, 6f), true));
				baseblock(name + "_lacunar_tile",()->new LacunarBlock(getTransparentProps().strength(2f, 6f)
						.isViewBlocking(CPBlocks::isntSolid)));
				baseblock(name+"_spoked_fence",()->new SpokedFenceBlock(getTransparentProps().strength(2f, 6f)));
			}
			if(type.isRoadMaterial()) {
				baseblock(name+"_road_side",()->new CPRoadSideBlock(getTransparentProps().isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid).strength(2f, 6f)));
				baseblock(name+"_road",()->new CPRoadBlock(getTransparentProps().isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid).strength(2f, 6f)));
			}
		}
		registerWood("walnut", WALNUT, WalnutTreeGrower::new, l -> WALNUT_PLANKS = l, l -> WALNUT_LOG = l,
				l -> WALNUT_LEAVE = l, l -> WALNUT_SAPLINGS = l);
		registerBush("fig", FigTreeGrower::new, l -> FIG_LOG = l, l -> FIG_LEAVE = l, l -> FIG_SAPLINGS = l);
		registerBush("wolfberry", WolfberryTreeGrower::new, l -> WOLFBERRY_LOG = l, l -> WOLFBERRY_LEAVE = l,
				l -> WOLFBERRY_SAPLINGS = l);
		for (String s : CPItems.dishes) {
			baseblock(s,
					() -> new DishBlock(Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD).instabreak()
							.noOcclusion().isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid)
							.isViewBlocking(CPBlocks::isntSolid)),
					b -> new DishItem(b, CPItems.createSoupProps()));

		}
	}

	private static void registerBush(String wood, Supplier<AbstractTreeGrower> growth,
			Consumer<RegistryObject<Block>> glog, Consumer<RegistryObject<Block>> gleave,
			Consumer<RegistryObject<Block>> gsap) {
		glog.accept(baseblock(wood + "_log",
				() -> new BushLogBlock(BlockBehaviour.Properties.of(Material.WOOD, (p_152624_) -> MaterialColor.WOOD)
						.strength(2.0F).noOcclusion().sound(SoundType.WOOD))));
		RegistryObject<Block> a = baseblock(wood + "_fruits", () -> new FruitBlock(BlockBehaviour.Properties
				.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
		gleave.accept(baseblock(wood + "_leaves", () -> leaves(SoundType.GRASS, a)));
		gsap.accept(baseblock(wood + "_sapling", () -> new SaplingBlock(growth.get(), BlockBehaviour.Properties
				.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))));

	}

	private static void registerWood(String wood, WoodType wt, Supplier<AbstractTreeGrower> growth,
			Consumer<RegistryObject<Block>> gplank, Consumer<RegistryObject<Block>> glog,
			Consumer<RegistryObject<Block>> gleave, Consumer<RegistryObject<Block>> gsap) {
		RegistryObject<Block> planks = block(wood + "_planks", BlockBehaviour.Properties
				.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD));
		gplank.accept(planks);
		baseblock(wood + "_button", () -> new WoodButtonBlock(
				BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
		baseblock(wood + "_door", () -> new DoorBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
				.strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
		baseblock(wood + "_fence", () -> new FenceBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
				.strength(2.0F, 3.0F).sound(SoundType.WOOD)));
		baseblock(wood + "_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties
				.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
		RegistryObject<Block> f = baseblock(wood + "_fruits", () -> new FruitBlock(BlockBehaviour.Properties
				.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));
		gleave.accept(baseblock(wood + "_leaves", () -> leaves(SoundType.GRASS, f)));
		RegistryObject<Block> sl = baseblock("stripped_" + wood + "_log",
				() -> log(MaterialColor.WOOD, MaterialColor.WOOD, null));
		glog.accept(baseblock(wood + "_log", () -> log(MaterialColor.WOOD, MaterialColor.PODZOL, sl)));

		baseblock(wood + "_pressure_plate",
				() -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties
						.of(Material.WOOD, MaterialColor.WOOD).noCollission().strength(0.5F).sound(SoundType.WOOD)));
		gsap.accept(baseblock(wood + "_sapling", () -> new SaplingBlock(growth.get(), BlockBehaviour.Properties
				.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS))));
		RegistryObject<Block> s = BLOCKS.register(wood + "_sign",
				() -> new CPStandingSignBlock(
						BlockBehaviour.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD),
						wt));
		RegistryObject<Block> ws = BLOCKS.register(wood + "_wall_sign",
				() -> new CPWallSignBlock(
						BlockBehaviour.Properties.of(Material.WOOD).noCollission().strength(1.0F).sound(SoundType.WOOD),
						wt));
		CPItems.ITEMS.register(wood + "_sign",
				() -> new SignItem((new Item.Properties()).stacksTo(16).tab(CPMain.mainGroup), s.get(), ws.get()));
		baseblock(wood + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
				.strength(2.0F, 3.0F).sound(SoundType.WOOD)));
		baseblock(wood + "_stairs", () -> new StairBlock(planks.get()::defaultBlockState, BlockBehaviour.Properties
				.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
		baseblock(wood + "_trapdoor",
				() -> new TrapDoorBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(3.0F)
						.sound(SoundType.WOOD).noOcclusion().isValidSpawn(CPBlocks::never)));
		RegistryObject<Block> sw = baseblock("stripped_" + wood + "_wood", () -> new RotatedPillarBlock(
				BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
		baseblock(wood + "_wood", () -> new CPStripPillerBlock(sw,
				BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
	}

	private static LeavesBlock leaves(SoundType p_152615_, RegistryObject<Block> fruit) {
		return new FruitsLeavesBlock(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2F).randomTicks()
				.sound(p_152615_).noOcclusion().isValidSpawn(CPBlocks::ocelotOrParrot)
				.isSuffocating(CPBlocks::isntSolid).isViewBlocking(CPBlocks::isntSolid), fruit);
	}

	private static RotatedPillarBlock log(MaterialColor pTopColor, MaterialColor pBarkColor, RegistryObject<Block> st) {
		if (st == null)
			return new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD, (p_152624_) -> {
				return p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? pTopColor : pBarkColor;
			}).strength(2.0F).sound(SoundType.WOOD));
		return new CPStripPillerBlock(st, BlockBehaviour.Properties.of(Material.WOOD, (p_152624_) -> {
			return p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? pTopColor : pBarkColor;
		}).strength(2.0F).sound(SoundType.WOOD));
	}

	private static Properties getStoneProps() {
		return Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops().strength(2.0f,
				6);
	}

	private static Properties getStoveProps() {
		return Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops()
				.strength(3.5f, 10).noOcclusion().lightLevel(s -> s.getValue(KitchenStove.LIT) ? 9 : 0)
				.isRedstoneConductor(CPBlocks::isntSolid).isSuffocating(CPBlocks::isntSolid);
	}

	private static Properties getTransparentProps() {
		return Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops()
				.strength(3.5f, 10).noOcclusion();
	}

	@SuppressWarnings("unused")
	private static boolean isntSolid(BlockState state, BlockGetter reader, BlockPos pos) {
		return false;
	}

	@SuppressWarnings("unused")
	private static Boolean never(BlockState p_50779_, BlockGetter p_50780_, BlockPos p_50781_, EntityType<?> p_50782_) {
		return (boolean) false;
	}

	@SuppressWarnings("unused")
	private static Boolean ocelotOrParrot(BlockState p_50822_, BlockGetter p_50823_, BlockPos p_50824_,
			EntityType<?> p_50825_) {
		return p_50825_ == EntityType.OCELOT || p_50825_ == EntityType.PARROT;
	}
}