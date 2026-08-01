package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

@ElementsRottenfleshMod.ModElement.Tag
public class TorchThrowHandler extends ElementsRottenfleshMod.ModElement {

	// Retient les IDs des boules de neige qu'on a nous-memes lancees comme "torche volante",
	// pour ne pas confondre avec une vraie boule de neige lancee normalement par un joueur.
	private static final Set<Integer> trackedSnowballs = new HashSet<>();

	public TorchThrowHandler(ElementsRottenfleshMod instance) {
		super(instance, 52);
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * A appeler depuis l'item (clic droit) pour lancer la "torche volante".
	 * Cote serveur uniquement (le monde reel qui compte pour placer le bloc).
	 */
	public static void throwTorchSnowball(World world, EntityPlayer player) {
		if (world.isRemote) {
			return;
		}

		EntitySnowball snowball = new EntitySnowball(world, player);
		snowball.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
		world.spawnEntity(snowball);

		trackedSnowballs.add(snowball.getEntityId());
	}

	@SubscribeEvent
	public void onProjectileImpact(ProjectileImpactEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof EntitySnowball)) {
			return;
		}

		World world = entity.world;
		if (world.isRemote) {
			// En solo, client et serveur partagent la meme JVM (donc trackedSnowballs,
			// qui est static). On ignore TOUJOURS cote client, AVANT de toucher au Set,
			// pour ne pas consommer l'entree a la place du serveur.
			return;
		}

		if (!trackedSnowballs.remove(entity.getEntityId())) {
			return; // pas une de nos torches volantes, on laisse faire le comportement normal
		}

		RayTraceResult result = event.getRayTraceResult();
		if (result == null || result.sideHit == null) {
			entity.setDead();
			return;
		}

		BlockPos placePos = result.getBlockPos().offset(result.sideHit);

		if (!world.isAirBlock(placePos)) {
			entity.setDead();
			return;
		}

		if (result.sideHit == EnumFacing.DOWN) {
			// une torche ne peut pas s'accrocher au plafond en vanilla, on ignore
			entity.setDead();
			return;
		}

		IBlockState torchState = Blocks.TORCH.getDefaultState()
				.withProperty(BlockTorch.FACING, result.sideHit);
		world.setBlockState(placePos, torchState);

		entity.setDead();
	}
}
