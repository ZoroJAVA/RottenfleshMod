package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.gui.GuiChimneyGUI;
import net.mura.rottenflesh.RottenfleshMod;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedurePrpc1 extends ElementsRottenfleshMod.ModElement {
	public ProcedurePrpc1(ElementsRottenfleshMod instance) {
		super(instance, 27);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure Prpc1!");
			return;
		}
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure Prpc1!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure Prpc1!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure Prpc1!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure Prpc1!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");
		if (entity instanceof EntityPlayer)
			((EntityPlayer) entity).openGui(RottenfleshMod.instance, GuiChimneyGUI.GUIID, world, x, y, z);
		System.out.println("COAL!");
	}
}
