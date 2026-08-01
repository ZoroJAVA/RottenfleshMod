package net.mura.rottenflesh.procedure;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Logique de stockage de fluides pour l'item "bidon portable".
 * Deux reservoirs separes (eau / lave), chacun jusqu'a 4 seaux (4000 mB).
 * Les quantites sont stockees dans le NBT de l'itemstack lui-meme.
 */
public class LiquidStorageHandler {

	public static final int CAPACITY_MB = 4000; // 4 seaux par fluide
	public static final int BUCKET_MB = 1000;
	public static final String WATER = "water";
	public static final String LAVA = "lava";

	public static int getAmount(ItemStack stack, String fluid) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return 0;
		}
		return nbt.getInteger(fluid + "Amount");
	}

	private static void setAmount(ItemStack stack, String fluid, int amount) {
		NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		nbt.setInteger(fluid + "Amount", amount);
		stack.setTagCompound(nbt);
	}

	public static String getSelected(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Selected")) {
			return WATER;
		}
		return nbt.getString("Selected");
	}

	private static void setSelected(ItemStack stack, String fluid) {
		NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		nbt.setString("Selected", fluid);
		stack.setTagCompound(nbt);
	}

	/**
	 * Renvoie le nom du fluide avec son code couleur colle devant
	 * (bleu pour l'eau, rouge pour la lave).
	 */
	public static String getColoredName(String fluid) {
		if (fluid.equals(WATER)) {
			return TextFormatting.AQUA + "Eau" + TextFormatting.RESET;
		}
		return TextFormatting.RED + "Lave" + TextFormatting.RESET;
	}

	/**
	 * A appeler depuis onItemRightClick() de l'item.
	 */
	public static void onRightClick(World world, EntityPlayer player, ItemStack stack) {
		if (world.isRemote) {
			return;
		}

		if (player.isSneaking()) {
			String current = getSelected(stack);
			String next = current.equals(WATER) ? LAVA : WATER;
			setSelected(stack, next);
			player.sendStatusMessage(
					new TextComponentString("Fluide selectionne : " + getColoredName(next)),
					true);
			return;
		}

		RayTraceResult result = rayTraceIncludingFluids(world, player, 5.0D);
		if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
			return; // rien a portee
		}

		IBlockState targetState = world.getBlockState(result.getBlockPos());
		Material material = targetState.getMaterial();

		if (material == Material.WATER) {
			tryFill(stack, WATER, world, result.getBlockPos(), player);
			return;
		}
		if (material == Material.LAVA) {
			tryFill(stack, LAVA, world, result.getBlockPos(), player);
			return;
		}

		// Pas de fluide vise : on essaie de VIDER le fluide selectionne juste a cote du bloc touche
		BlockPos placePos = result.getBlockPos().offset(result.sideHit);
		if (!world.isAirBlock(placePos)) {
			return;
		}

		String selected = getSelected(stack);
		int amount = getAmount(stack, selected);
		if (amount < BUCKET_MB) {
			player.sendStatusMessage(new TextComponentString("Pas assez de fluide selectionne"), true);
			return;
		}

		IBlockState fluidState = selected.equals(WATER)
				? Blocks.FLOWING_WATER.getDefaultState()
				: Blocks.FLOWING_LAVA.getDefaultState();
		world.setBlockState(placePos, fluidState);
		setAmount(stack, selected, amount - BUCKET_MB);
	}

	private static void tryFill(ItemStack stack, String fluid, World world, BlockPos sourcePos, EntityPlayer player) {
		int current = getAmount(stack, fluid);
		if (current + BUCKET_MB > CAPACITY_MB) {
			player.sendStatusMessage(
					new TextComponentString(getColoredName(fluid) + " : reservoir plein"), true);
			return;
		}
		world.setBlockToAir(sourcePos);
		setAmount(stack, fluid, current + BUCKET_MB);
	}

	private static RayTraceResult rayTraceIncludingFluids(World world, EntityPlayer player, double distance) {
		Vec3d eyePos = player.getPositionEyes(1.0F);
		Vec3d lookVec = player.getLook(1.0F);
		Vec3d endPos = eyePos.addVector(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
		// stopOnLiquid = true : contrairement au raytrace par defaut du joueur,
		// celui-ci detecte aussi les blocs d'eau/lave, pas seulement les blocs solides
		return world.rayTraceBlocks(eyePos, endPos, true, false, false);
	}
}