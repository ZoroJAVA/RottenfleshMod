package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.block.BlockChimney;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureChimneyproc extends ElementsRottenfleshMod.ModElement {
	public ProcedureChimneyproc(ElementsRottenfleshMod instance) {
		super(instance, 26);
	}

	private static final int MAX_OUTPUT = 64;

	// Minerais acceptes en entree (slot 0) et le resultat que chacun produit en sortie (slot 2).
	// Ajoute ici tout autre minerai a bruler, ex: ORE_RESULTS.put(Item.getItemFromBlock(Blocks.GOLD_ORE), Items.GOLD_INGOT);
	private static final Map<Item, Item> ORE_RESULTS = new HashMap<>();
	static {
		ORE_RESULTS.put(Item.getItemFromBlock(Blocks.COAL_ORE), Items.COAL);
		ORE_RESULTS.put(Item.getItemFromBlock(Blocks.IRON_ORE), Items.IRON_INGOT);
	}

	// Combustibles acceptes (slot 1) : combien de minerais chacun peut bruler avant
	// d'etre consomme. Ajoute ici tout autre combustible, ex: FUELS.put(Items.BLAZE_ROD, new FuelType(12, null));
	private static final Map<Item, FuelType> FUELS = new HashMap<>();
	static {
		FUELS.put(Items.COAL, new FuelType(6, null));
		FUELS.put(Items.LAVA_BUCKET, new FuelType(120, Items.BUCKET));
	}

	// burnAmount: nombre de minerais que ce combustible brule avant d'etre epuise.
	// remainder: item laisse dans le slot une fois consomme (ex: seau vide), null si l'item disparait totalement.
	private static final class FuelType {
		final int burnAmount;
		final Item remainder;

		FuelType(int burnAmount, Item remainder) {
			this.burnAmount = burnAmount;
			this.remainder = remainder;
		}
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure Chimneyproc!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure Chimneyproc!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure Chimneyproc!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure Chimneyproc!");
			return;
		}
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (!(te instanceof BlockChimney.TileEntityCustom))
			return;
		BlockChimney.TileEntityCustom chimney = (BlockChimney.TileEntityCustom) te;

		for (int index0 = 0; index0 < 100; index0++) {
			ItemStack oreStack = chimney.getStackInSlot(0);
			ItemStack outputStack = chimney.getStackInSlot(2);

			Item resultItem = ORE_RESULTS.get(oreStack.getItem());
			if (resultItem == null)
				break;
			// Le slot de sortie contient deja un resultat different, il faut le vider d'abord.
			if (!outputStack.isEmpty() && outputStack.getItem() != resultItem)
				break;
			if (outputStack.getCount() >= MAX_OUTPUT)
				break;

			if (chimney.getFuelUsesLeft() <= 0) {
				ItemStack fuelStack = chimney.getStackInSlot(1);
				FuelType fuel = FUELS.get(fuelStack.getItem());
				if (fuel == null || fuelStack.isEmpty())
					break;
				if (fuelStack.getCount() > 1) {
					fuelStack.shrink(1);
					chimney.setInventorySlotContents(1, fuelStack);
				} else if (fuel.remainder != null) {
					ItemStack remainderStack = new ItemStack(fuel.remainder);
					EntityPlayer viewer = chimney.getFirstViewer();
					if (viewer != null) {
						// Le GUI est ouvert : on rend le contenant vide (ex: seau) directement au joueur
						// au lieu de le laisser dans le slot de combustible.
						viewer.inventory.addItemStackToInventory(remainderStack);
						if (!remainderStack.isEmpty())
							InventoryHelper.spawnItemStack(world, viewer.posX, viewer.posY, viewer.posZ, remainderStack);
						world.playSound(null, viewer.posX, viewer.posY, viewer.posZ, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
								.getObject(new ResourceLocation("entity.item.pickup")), SoundCategory.PLAYERS, (float) 0.2, (float) 1);
						chimney.setInventorySlotContents(1, ItemStack.EMPTY);
					} else {
						chimney.setInventorySlotContents(1, remainderStack);
					}
				} else {
					chimney.setInventorySlotContents(1, ItemStack.EMPTY);
				}
				chimney.setFuelUsesLeft(fuel.burnAmount);
			}

			chimney.decrStackSize(0, 1);
			chimney.setFuelUsesLeft(chimney.getFuelUsesLeft() - 1);

			ItemStack newOutput = outputStack.isEmpty() ? new ItemStack(resultItem) : outputStack.copy();
			if (!outputStack.isEmpty())
				newOutput.grow(1);
			chimney.setInventorySlotContents(2, newOutput);

			world.playSound((EntityPlayer) null, x, y, z, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
					.getObject(new ResourceLocation("block.furnace.fire_crackle")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
		}
	}
}
