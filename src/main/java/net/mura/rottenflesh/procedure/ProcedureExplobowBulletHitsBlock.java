package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureExplobowBulletHitsBlock extends ElementsRottenfleshMod.ModElement {
	public ProcedureExplobowBulletHitsBlock(ElementsRottenfleshMod instance) {
		super(instance, 2);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure ExplobowBulletHitsBlock!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure ExplobowBulletHitsBlock!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure ExplobowBulletHitsBlock!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure ExplobowBulletHitsBlock!");
			return;
		}
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");
		if (!world.isRemote) {
			world.createExplosion(null, (int) x, (int) y, (int) z, (float) 3, true);
		}
		world.playSound((EntityPlayer) null, x, y, z,
				(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.firework.blast")),
				SoundCategory.NEUTRAL, (float) 1, (float) 1);
	}
}
