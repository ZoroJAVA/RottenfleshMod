package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureChimneyproc extends ElementsRottenfleshMod.ModElement {
	public ProcedureChimneyproc(ElementsRottenfleshMod instance) {
		super(instance, 26);
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
		for (int index0 = 0; index0 < (int) (100); index0++) {
			if ((((new Object() {
				public int getAmount(BlockPos pos, int sltid) {
					TileEntity inv = world.getTileEntity(pos);
					if (inv instanceof TileEntityLockableLoot) {
						ItemStack stack = ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
						if (stack != null)
							return stack.getCount();
					}
					return 0;
				}
			}.getAmount(new BlockPos((int) x, (int) y, (int) z), (int) (2))) < 64) && ((((new Object() {
				public ItemStack getItemStack(BlockPos pos, int sltid) {
					TileEntity inv = world.getTileEntity(pos);
					if (inv instanceof TileEntityLockableLoot)
						return ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
					return ItemStack.EMPTY;
				}
			}.getItemStack(new BlockPos((int) x, (int) y, (int) z), (int) (0))).getItem() == new ItemStack(Blocks.COAL_ORE, (int) (1)).getItem())
					&& ((new Object() {
						public ItemStack getItemStack(BlockPos pos, int sltid) {
							TileEntity inv = world.getTileEntity(pos);
							if (inv instanceof TileEntityLockableLoot)
								return ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
							return ItemStack.EMPTY;
						}
					}.getItemStack(new BlockPos((int) x, (int) y, (int) z), (int) (1))).getItem() == new ItemStack(Items.COAL, (int) (1), 0)
							.getItem()))
					&& ((((new Object() {
						public int getAmount(BlockPos pos, int sltid) {
							TileEntity inv = world.getTileEntity(pos);
							if (inv instanceof TileEntityLockableLoot) {
								ItemStack stack = ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
								if (stack != null)
									return stack.getCount();
							}
							return 0;
						}
					}.getAmount(new BlockPos((int) x, (int) y, (int) z), (int) (2))) <= 64) && ((new Object() {
						public ItemStack getItemStack(BlockPos pos, int sltid) {
							TileEntity inv = world.getTileEntity(pos);
							if (inv instanceof TileEntityLockableLoot)
								return ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
							return ItemStack.EMPTY;
						}
					}.getItemStack(new BlockPos((int) x, (int) y, (int) z), (int) (2))).getItem() == new ItemStack(Items.COAL, (int) (1), 0)
							.getItem())) || ((new Object() {
								public ItemStack getItemStack(BlockPos pos, int sltid) {
									TileEntity inv = world.getTileEntity(pos);
									if (inv instanceof TileEntityLockableLoot)
										return ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
									return ItemStack.EMPTY;
								}
							}.getItemStack(new BlockPos((int) x, (int) y, (int) z), (int) (2))).getItem() == new ItemStack(Blocks.AIR, (int) (1))
									.getItem()))))) {
				{
					TileEntity inv = world.getTileEntity(new BlockPos((int) x, (int) y, (int) z));
					if (inv instanceof TileEntityLockableLoot)
						((TileEntityLockableLoot) inv).decrStackSize((int) (0), (int) (1));
				}
				{
					TileEntity inv = world.getTileEntity(new BlockPos((int) x, (int) y, (int) z));
					if (inv instanceof TileEntityLockableLoot)
						((TileEntityLockableLoot) inv).decrStackSize((int) (1), (int) (1));
				}
				{
					TileEntity inv = world.getTileEntity(new BlockPos((int) x, (int) y, (int) z));
					if (inv != null && (inv instanceof TileEntityLockableLoot)) {
						ItemStack _setstack = new ItemStack(Items.COAL, (int) (1), 0);
						_setstack.setCount(((new Object() {
							public int getAmount(BlockPos pos, int sltid) {
								TileEntity inv = world.getTileEntity(pos);
								if (inv instanceof TileEntityLockableLoot) {
									ItemStack stack = ((TileEntityLockableLoot) inv).getStackInSlot(sltid);
									if (stack != null)
										return stack.getCount();
								}
								return 0;
							}
						}.getAmount(new BlockPos((int) x, (int) y, (int) z), (int) (2))) + 1));
						((TileEntityLockableLoot) inv).setInventorySlotContents((int) (2), _setstack);
					}
				}
				world.playSound((EntityPlayer) null, x, y, z, (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
						.getObject(new ResourceLocation("block.furnace.fire_crackle")), SoundCategory.NEUTRAL, (float) 1, (float) 1);
			}
		}
	}
}
