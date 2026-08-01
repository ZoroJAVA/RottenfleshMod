package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureFire0aEntityWalksOnTheBlock extends ElementsRottenfleshMod.ModElement {
	public ProcedureFire0aEntityWalksOnTheBlock(ElementsRottenfleshMod instance) {
		super(instance, 33);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure Fire0aEntityWalksOnTheBlock!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		entity.setFire((int) 5);
	}
}
