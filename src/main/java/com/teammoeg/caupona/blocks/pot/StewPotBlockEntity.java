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

package com.teammoeg.caupona.blocks.pot;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.teammoeg.caupona.CPBlockEntityTypes;
import com.teammoeg.caupona.Config;
import com.teammoeg.caupona.CPMain;
import com.teammoeg.caupona.blocks.stove.IStove;
import com.teammoeg.caupona.data.recipes.AspicMeltingRecipe;
import com.teammoeg.caupona.data.recipes.BoilingRecipe;
import com.teammoeg.caupona.data.recipes.BowlContainingRecipe;
import com.teammoeg.caupona.data.recipes.DissolveRecipe;
import com.teammoeg.caupona.data.recipes.DoliumRecipe;
import com.teammoeg.caupona.data.recipes.FoodValueRecipe;
import com.teammoeg.caupona.data.recipes.SpiceRecipe;
import com.teammoeg.caupona.data.recipes.StewCookingRecipe;
import com.teammoeg.caupona.data.recipes.StewPendingContext;
import com.teammoeg.caupona.fluid.SoupFluid;
import com.teammoeg.caupona.network.CPBaseBlockEntity;
import com.teammoeg.caupona.util.IInfinitable;
import com.teammoeg.caupona.util.LazyTickWorker;
import com.teammoeg.caupona.util.StewInfo;
import com.teammoeg.caupona.util.SyncedFluidHandler;
import com.teammoeg.caupona.util.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public class StewPotBlockEntity extends CPBaseBlockEntity implements MenuProvider, IInfinitable {
	private ItemStackHandler inv = new ItemStackHandler(12) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot < 9)
				return (stack.getItem() == Items.POTION&&!PotionUtils.getMobEffects(stack).stream().anyMatch(t->t.getDuration()==1)) || StewCookingRecipe.isCookable(stack);
			if (slot == 9) {
				Item i = stack.getItem();
				return i == Items.BOWL || BowlContainingRecipe.getFluidType(stack)!=Fluids.EMPTY || AspicMeltingRecipe.find(stack) != null;
			}
			if (slot == 11)
				return SpiceRecipe.isValid(stack);
			return false;
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot < 9)
				return 1;
			return super.getSlotLimit(slot);
		}
	};

	public ItemStackHandler getInv() {
		return inv;
	}

	public StewInfo current;
	private FluidTank tank = new FluidTank(1250, StewCookingRecipe::isBoilable) {
		protected void onContentsChanged() {
			if (this.isEmpty())
				current = null;
			still.rewind();
		}

	};

	public StewPotBlockEntity(BlockPos p, BlockState s) {
		super(CPBlockEntityTypes.STEW_POT.get(), p, s);
		still=new LazyTickWorker(Config.COMMON.staticTime.get(),()->{
			if (inv.getStackInSlot(10).isEmpty()) {
				DoliumRecipe recipe = DoliumRecipe.testPot(getTank().getFluid());
				if (recipe != null) {
					ItemStack out = recipe.handle(getTank().getFluid());

					inv.setStackInSlot(10, out);
				}

			}
			return true;
		});
		contain=new LazyTickWorker(Config.SERVER.containerTick.get(),()->{
			if (isInfinite) {
				FluidStack fs = new FluidStack(tank.getFluid(), tank.getFluidAmount());
				if (canAddFluid())
					tryContianFluid();
				tank.setFluid(fs);
			} else {
				if (canAddFluid()) {
					if(tryContianFluid())
						return true;
				}
			}
			return false;
		});
	}

	public FluidTank getTank() {
		return tank;
	}

	//Process
	public int process;
	public int processMax;

	public LazyTickWorker still;

	public LazyTickWorker contain;

	//stores working properties
	public boolean working = false;
	public boolean operate = false;
	public short proctype = 0;
	public boolean rsstate = false;
	
	boolean isInfinite = false;

	//stores Result
	public Fluid become;
	public ResourceLocation nextbase;
	boolean removesNBT=false;
	public void resetResult() {
		become=null;
		nextbase=null;
		removesNBT=false;
	}
	public static final short NOP = 0;
	public static final short BOILING = 1;
	public static final short COOKING = 2;
	public static final short STIRING = 3;

	@Override
	public void tick() {
		boolean syncNeeded=false;
		if (!level.isClientSide) {
			working = false;
			if (processMax > 0) {
				still.rewind();
				if (level.getBlockEntity(worldPosition.below()) instanceof IStove stove) {
					int rh = stove.requestHeat();
					if (!isInfinite) {
						process += rh;
						if(rh>0)
							syncNeeded=true;
					}
					if (rh > 0)
						working = true;
					if (process >= processMax) {
						process = 0;
						processMax = 0;
						doWork();
						syncNeeded=true;
					}
				} else
					return;

			} else {
				if (!tank.isEmpty() && !isInfinite) {
					syncNeeded|=still.tick();
				}
				if (!isInfinite&&proctype<=1) {
					prepareWork();
					if(proctype!=0)
						syncNeeded=true;
				}
				syncNeeded|=contain.tick();
			}
			if(syncNeeded)
				this.syncData();
		}
		
		
	}

	private FluidStack tryAddSpice(FluidStack fs) {
		SpiceRecipe spice = null;
		ItemStack spi = inv.getStackInSlot(11);
		System.out.println(tank.getFluid().getFluid().getClass().getSimpleName());
		if (fs.getAmount() % 250 == 0 && fs.getFluid() instanceof SoupFluid)
			spice = SpiceRecipe.find(spi);
		if (spice != null) {
			StewInfo si = SoupFluid.getInfo(fs);
			if (!si.canAddSpice())
				return fs;
			if (!isInfinite) {
				int consume = fs.getAmount() / 250;
				if (SpiceRecipe.getMaxUse(spi) < consume)
					return fs;
				inv.setStackInSlot(11, SpiceRecipe.handle(spi, consume));
			}
			si.addSpice(spice.effect, spi);
			SoupFluid.setInfo(fs, si);
		}
		return fs;

	}

	private boolean tryContianFluid() {
		ItemStack is = inv.getStackInSlot(9);
		if (!is.isEmpty() && inv.getStackInSlot(10).isEmpty()) {
			if (is.getItem() == Items.BOWL && tank.getFluidAmount() >= 250) {
				BowlContainingRecipe recipe = BowlContainingRecipe.recipes.get(this.tank.getFluid().getFluid());
				if (recipe != null) {
					is.shrink(1);
					inv.setStackInSlot(10, recipe.handle(tryAddSpice(tank.drain(250, FluidAction.EXECUTE))));
					still.rewind();
					return true;
				}
			}

			FluidStack out=BowlContainingRecipe.extractFluid(is);
			if (!out.isEmpty()) {
				if (tryAddFluid(out)) {
					ItemStack ret = is.getCraftingRemainingItem();
					is.shrink(1);
					still.rewind();
					inv.setStackInSlot(10, ret);
					return true;
				}
				return false;
			}
			if (!isInfinite) {
				AspicMeltingRecipe amr = AspicMeltingRecipe.find(is);
				if (amr != null) {
					int remainSpace = tank.getCapacity() - tank.getFluidAmount();
					int produce = Math.min(remainSpace / amr.amount, is.getCount());
					FluidStack fs = amr.handle(is);
					fs.setAmount(fs.getAmount() * produce);
					if (tryAddFluid(fs, amr.time, false)) {
						ItemStack ret = is.getCraftingRemainingItem();
						ret.setCount(produce);
						is.shrink(produce);
						still.rewind();
						inv.setStackInSlot(10, ret);
						return true;
					}
					return false;
				}
			}
			FluidActionResult far = FluidUtil.tryFillContainer(is, this.tank, 1250, null, true);
			if (far.isSuccess()) {
				is.shrink(1);
				if (far.getResult() != null) {
					still.rewind();
					inv.setStackInSlot(10, far.getResult());
				}
			}
		}
		return false;
	}

	public boolean canAddFluid() {
		return proctype == 0;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean isClient) {
		process = nbt.getInt("process");
		processMax = nbt.getInt("processMax");
		proctype = nbt.getShort("worktype");
		rsstate = nbt.getBoolean("rsstate");
		if (inv.getSlots() < 12)
			inv.setSize(12);
		if (isClient)
			working = nbt.getBoolean("working");
		tank.readFromNBT(nbt);
		if (nbt.contains("result"))
			become = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("result")));
		else
			become = null;
		isInfinite = nbt.getBoolean("inf");
		if (!isClient) {
			inv.deserializeNBT(nbt.getCompound("inv"));
			current = nbt.contains("current") ? new StewInfo(nbt.getCompound("current")) : null;
			nextbase = nbt.contains("resultBase") ? new ResourceLocation(nbt.getString("resultBase")) : null;
			still.read(nbt,"nowork");
			removesNBT=nbt.getBoolean("removeNbt");
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean isClient) {
		nbt.putInt("process", process);
		nbt.putInt("processMax", processMax);
		nbt.putShort("worktype", proctype);
		nbt.putBoolean("rsstate", rsstate);
		if (isClient)
			nbt.putBoolean("working", working);

		tank.writeToNBT(nbt);
		
		if (become != null)
			nbt.putString("result", Utils.getRegistryName(become).toString());
		nbt.putBoolean("inf", isInfinite);
		if (!isClient) {
			nbt.put("inv", inv.serializeNBT());
			still.write(nbt,"nowork");
			if (current != null)
				nbt.put("current", current.save());
			if (nextbase != null)
				nbt.putString("resultBase", nextbase.toString());
			nbt.putBoolean("removeNbt",removesNBT);
		}
	}

	private void prepareWork() {
		if (rsstate&&proctype==0&& !operate && level.hasNeighborSignal(this.worldPosition))
			operate = true;

		if (operate&&proctype==0) {
			operate = false;
			if (!(level.getBlockEntity(worldPosition.below()) instanceof IStove stove) || !stove.canEmitHeat())
				return;
			if (doBoil())
				proctype = 1;
			else if (makeSoup())
				proctype = 2;
		} else if (proctype == 1) {
			if (makeSoup())
				proctype = 2;
			else {
				proctype = 0;
				this.syncData();
			}
		}
	}

	private void doWork() {
		if (proctype == 1) {
			finishBoil();
			boolean hasItem = false;
			for (int i = 0; i < 9; i++) {
				ItemStack is = inv.getStackInSlot(i);
				if (!is.isEmpty()) {
					hasItem = true;
					break;
				}
			}
			if (!hasItem)
				proctype = 0;
		} else if (proctype == 2 || proctype == 3)
			finishSoup();
	}

	private boolean doBoil() {
		BoilingRecipe recipe = BoilingRecipe.recipes.get(this.tank.getFluid().getFluid());
		if (recipe == null)
			return false;
		become = recipe.after;
		this.processMax = (int) (recipe.time * (this.tank.getFluidAmount() / 250f));
		this.process = 0;
		return true;
	}

	private void finishBoil() {
		BoilingRecipe recipe = BoilingRecipe.recipes.get(this.tank.getFluid().getFluid());
		if (recipe == null)
			return;
		current = null;
		tank.setFluid(recipe.handle(tank.getFluid()));
	}

	private void adjustParts(int count) {
		float oparts = tank.getFluidAmount() / 250f;
		int parts = (int) (oparts + count);
		getCurrent().adjustParts(oparts, parts);
		tank.getFluid().setAmount(parts * 250);
	}

	private boolean makeSoup() {
		if (tank.getFluidAmount() <= 250)
			return false;// cant boil if under one bowl
		if (getCurrent().stacks.size() > 27)
			return false;// too much ingredients
		int oparts = tank.getFluidAmount() / 250;
		int parts = oparts - 1;
		int itms = 0;
		List<MobEffectInstance> cr = new ArrayList<>(current.effects);
		for (int i = 0; i < 9; i++) {
			ItemStack is = inv.getStackInSlot(i);
			if (!is.isEmpty()) {

				if (is.getItem() == Items.POTION) {
					outer: for (MobEffectInstance n : PotionUtils.getMobEffects(is)) {
						for (MobEffectInstance eff : cr) {
							if (StewInfo.isEffectEquals(eff, n))
								continue outer;
						}
						cr.add(n);
					}
				} else if (StewCookingRecipe.isCookable(is))
					itms++;
				else
					return false;
			}
		}
		if (itms / (float) parts + (current.getDensity() * oparts) / parts > 3 || cr.size() > 3) {// too dense
			return false;
		}

		process = 0;
		adjustParts(-1);
		boolean hasItem = false;
		NonNullList<ItemStack> interninv = NonNullList.withSize(9, ItemStack.EMPTY);
		for (int i = 0; i < 9; i++) {
			ItemStack is = inv.getStackInSlot(i);
			if (!is.isEmpty()) {
				if (is.getItem() == Items.POTION) {
					for (MobEffectInstance eff : PotionUtils.getMobEffects(is))
						current.addEffect(eff, parts);
					inv.setStackInSlot(i, new ItemStack(Items.GLASS_BOTTLE));
				} else {
					for (int j = 0; j < 9; j++) {
						ItemStack ois = interninv.get(j);
						if (ois.isEmpty()) {
							interninv.set(j, is.copy());
							break;
						} else if (ois.sameItem(is) && ItemStack.tagMatches(ois, is)) {
							ois.setCount(ois.getCount() + is.getCount());
							break;
						}
					}
					inv.setStackInSlot(i, is.getCraftingRemainingItem());
				}
				hasItem = true;
			}
		}

		if (!hasItem) {// just reduce water
			current.completeEffects();
			processMax = Math.max(Config.SERVER.potCookTimeBase.get(), decideSoup());
			return true;
		}
		int tpt = Config.SERVER.potMixTimeBase.get();
		outer: for (int i = 0; i < 9; i++) {
			ItemStack is = interninv.get(i);
			if (is.isEmpty())
				break;
			current.addItem(is, parts);
			for (DissolveRecipe rs : DissolveRecipe.recipes) {
				if (rs.item.test(is)) {
					tpt += rs.time;
					continue outer;
				}
			}
			FoodValueRecipe fvr = FoodValueRecipe.recipes.get(is.getItem());
			if (fvr != null)
				tpt += fvr.processtimes.getOrDefault(is.getItem(), 0);
		}
		current.completeAll();
		tpt = Math.max(Config.SERVER.potCookTimeBase.get(), tpt);
		interninv.clear();
		processMax = Math.max(decideSoup(), tpt);
		return true;
	}
	
	private int decideSoup() {
		become = tank.getFluid().getFluid();

		StewPendingContext ctx = new StewPendingContext(getCurrent(), Utils.getRegistryName(become));
		nextbase = current.base;
		if (ctx.getItems().isEmpty()) {
			return 0;
		}
		
		for (StewCookingRecipe cr : StewCookingRecipe.sorted) {
			int mt = cr.matches(ctx);
			if (mt != 0) {
				if (mt == 2)
					nextbase = Utils.getRegistryName(become);
				else
					nextbase = current.base;
				become = cr.output;
				removesNBT=cr.removeNBT;
				return cr.time;
			}
		}

		return 0;
	}

	private void finishSoup() {
		if (nextbase != null && become != null) {
			
			FluidStack fss = new FluidStack(become, tank.getFluidAmount());
			if(!removesNBT) {
				if(!getCurrent().base.equals(nextbase)) {
					current.shrinkedFluid=0;
				}
				current.base = nextbase;
				current.recalculateHAS();
				SoupFluid.setInfo(fss, current);
			}
			tank.setFluid(fss);
		}
		resetResult();
		proctype = 0;
	}

	public boolean canAddFluid(FluidStack fs) {
		if (isInfinite)
			return false;
		int tryfill = tank.fill(fs, FluidAction.SIMULATE);
		if (tryfill > 0) {
			if (tryfill == fs.getAmount()) {
				return true;
			}
			return false;
		}
		if (tank.getCapacity() - tank.getFluidAmount() < fs.getAmount())
			return false;
		if (!(level.getBlockEntity(worldPosition.below()) instanceof IStove stove) || !stove.canEmitHeat())
			return false;
		StewInfo n = SoupFluid.getInfo(fs);
		if (!getCurrent().base.equals(n.base) && !current.base.equals(Utils.getRegistryName(fs))
				&& !n.base.equals(Utils.getRegistryName(tank.getFluid()))) {
			BoilingRecipe bnx = BoilingRecipe.recipes.get(fs.getFluid());
			if (bnx == null)
				return false;
			if (!current.base.equals(Utils.getRegistryName(bnx.after)))
				return false;
		}
		return current.canMerge(n, tank.getFluidAmount() / 250f, fs.getAmount() / 250f);
	}

	public boolean tryAddFluid(FluidStack fs) {
		return tryAddFluid(fs, Config.SERVER.potMixTimeBase.get(), true);
	}

	public boolean tryAddFluid(FluidStack fs, int extraTime, boolean canIgnoreHeat) {
		if (isInfinite)
			return false;
		if (canIgnoreHeat) {
			int tryfill = tank.fill(fs, FluidAction.SIMULATE);
			if (tryfill > 0) {
				if (tryfill == fs.getAmount()) {
					tank.fill(fs, FluidAction.EXECUTE);
					still.rewind();
					return true;
				}
				return false;
			}
		} else if (tank.isEmpty()) {
			int tryfill = tank.fill(fs, FluidAction.SIMULATE);
			if (tryfill > 0) {
				if (tryfill == fs.getAmount()) {
					tank.fill(fs, FluidAction.EXECUTE);
					this.proctype = 3;
					this.process = 0;
					this.processMax = extraTime;
					resetResult();
					still.rewind();
					return true;
				}
				return false;
			}
		}
		if (tank.getCapacity() - tank.getFluidAmount() < fs.getAmount())
			return false;
		if (!(level.getBlockEntity(worldPosition.below()) instanceof IStove stove) || !stove.canEmitHeat())
			return false;
		StewInfo n = SoupFluid.getInfo(fs);
		int pm = 0;

		if (!getCurrent().base.equals(n.base) && !current.base.equals(Utils.getRegistryName(fs))
				&& !n.base.equals(Utils.getRegistryName(tank.getFluid()))) {
			BoilingRecipe bnx = BoilingRecipe.recipes.get(fs.getFluid());
			if (bnx == null)
				return false;
			if (!getCurrent().base.equals(Utils.getRegistryName(bnx.after)))
				return false;
			fs = bnx.handle(fs);
			pm = (int) (bnx.time * (fs.getAmount() / 250f));
		}

		if (current.merge(n, tank.getFluidAmount() / 250f, fs.getAmount() / 250f)) {
			this.adjustParts(fs.getAmount() / 250);
			int num = Math.max(decideSoup(), extraTime);
			this.proctype = 3;
			this.process = 0;
			this.processMax = Math.max(pm, num);
			still.rewind();
			return true;
		}

		return false;
	}

	@Override
	public AbstractContainerMenu createMenu(int p1, Inventory p2, Player p3) {
		return new StewPotContainer(p1, p2, this);
	}

	@Override
	public Component getDisplayName() {
		return MutableComponent.create(new TranslatableContents("container." + CPMain.MODID + ".stewpot.title"));
	}

	@Override
	public void handleMessage(short type, int data) {
		if (type == 0)
			if (this.proctype == 0)
				this.operate = true;
		if (type == 1) {
			if (data == 1)
				rsstate = false;
			else if (data == 2)
				rsstate = true;
		}

	}

	IFluidHandler handler = new SyncedFluidHandler(this,new IFluidHandler() {
		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int t) {
			if (t == 0)
				return tank.getFluid();
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int t) {
			if (t == 0)
				return tank.getCapacity();
			return 0;
		}

		@Override
		public boolean isFluidValid(int t, FluidStack stack) {
			if (t == 0 && canAddFluid())
				return tank.isFluidValid(stack);
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (canAddFluid() && !isInfinite)
				return tank.fill(resource, action);
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {

			if (canAddFluid()) {
				if (isInfinite)
					return action.simulate() ? resource : tryAddSpice(resource);
				return action.simulate() ? tank.drain(resource, action) : tryAddSpice(tank.drain(resource, action));
			}
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {

			if (canAddFluid()) {
				if (isInfinite)
					return action.simulate() ? new FluidStack(tank.getFluid(), maxDrain)
							: tryAddSpice(new FluidStack(tank.getFluid(), maxDrain));
				return action.simulate() ? tank.drain(maxDrain, action) : tryAddSpice(tank.drain(maxDrain, action));
			}
			return FluidStack.EMPTY;
		}

	});
	public IItemHandler bowl = new IItemHandler() {

		@Override
		public int getSlots() {
			return inv.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return inv.getStackInSlot(slot);
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if(slot<9||slot == 10)
				return stack;
			return inv.insertItem(slot, stack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(slot==9||slot==11)
				return ItemStack.EMPTY;
			if(slot<9&&inv.isItemValid(slot,inv.getStackInSlot(slot))){
				return ItemStack.EMPTY;
			}
			return inv.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return inv.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			if(slot<9||slot == 10)
				return false;
			return inv.isItemValid(slot, stack);
		}
	};
	RangedWrapper ingredient = new RangedWrapper(inv, 0, 10) {

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}
	};
	LazyOptional<IItemHandler> up = LazyOptional.of(() -> ingredient);
	LazyOptional<IItemHandler> side = LazyOptional.of(() -> bowl);
	LazyOptional<IFluidHandler> fl = LazyOptional.of(() -> handler);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == Direction.UP)
				return up.cast();
			return this.side.cast();
		}
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return fl.cast();
		return super.getCapability(cap, side);
	}

	public StewInfo getCurrent() {
		if (current == null)
			current = SoupFluid.getInfo(tank.getFluid());
		return current;
	}

	@Override
	public boolean setInfinity() {
		return isInfinite = !isInfinite;
	}
}
