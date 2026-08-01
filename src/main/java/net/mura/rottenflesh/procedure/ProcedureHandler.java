package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureHandler extends ElementsRottenfleshMod.ModElement {

	public ProcedureHandler(ElementsRottenfleshMod instance) {
		super(instance, 49);
	}

	// Blocs consideres comme "pierre et minerais"
	private static final Set<Block> TARGET_BLOCKS = new HashSet<>(Arrays.asList(
			Blocks.STONE,
			Blocks.COAL_ORE,
			Blocks.IRON_ORE,
			Blocks.GOLD_ORE,
			Blocks.DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.LAPIS_ORE,
			Blocks.REDSTONE_ORE,
			Blocks.LIT_REDSTONE_ORE,
			Blocks.QUARTZ_ORE
	));

	// Empeche la recursion infinie quand on casse les blocs voisins
	private static boolean isMining = false;

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (isMining) {
			return;
		}
		if (!dependencies.containsKey("world") || !dependencies.containsKey("x")
				|| !dependencies.containsKey("y") || !dependencies.containsKey("z")
				|| !dependencies.containsKey("entity")) {
			return;
		}

		World world = (World) dependencies.get("world");
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		Entity entity = (Entity) dependencies.get("entity");

		if (world.isRemote) {
			return;
		}
		if (!(entity instanceof EntityPlayerMP)) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) entity;
		ItemStack heldItem = player.getHeldItemMainhand();

		// Accroupi = minage normal d'un seul bloc
		if (player.isSneaking()) {
			return;
		}

		BlockPos originPos = new BlockPos(x, y, z);
		IBlockState originState = world.getBlockState(originPos);
		if (!TARGET_BLOCKS.contains(originState.getBlock())) {
			return;
		}

		EnumFacing.Axis axis = getLookAxis(player);
		List<BlockPos> plane = get3x3Plane(originPos, axis);

		isMining = true;
		try {
			for (BlockPos pos : plane) {
				if (pos.equals(originPos)) {
					continue;
				}

				if (heldItem.isEmpty() || heldItem.getItemDamage() >= heldItem.getMaxDamage() - 1) {
					break;
				}

				IBlockState state = world.getBlockState(pos);
				if (!TARGET_BLOCKS.contains(state.getBlock())) {
					continue;
				}

				// Gere correctement drops, fortune/silk touch, XP et degradation de l'outil
				player.interactionManager.tryHarvestBlock(pos);
			}
		} finally {
			isMining = false;
		}
	}

	private static EnumFacing.Axis getLookAxis(EntityPlayer player) {
		RayTraceResult trace = player.rayTrace(6.0D, 1.0F);
		if (trace != null && trace.sideHit != null) {
			return trace.sideHit.getAxis();
		}
		return player.getHorizontalFacing().getAxis();
	}

	private static List<BlockPos> get3x3Plane(BlockPos center, EnumFacing.Axis axis) {
		List<BlockPos> result = new ArrayList<>(9);
		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				switch (axis) {
					case X:
						result.add(center.add(0, a, b));
						break;
					case Y:
						result.add(center.add(a, 0, b));
						break;
					case Z:
						result.add(center.add(a, b, 0));
						break;
				}
			}
		}
		return result;
	}
}