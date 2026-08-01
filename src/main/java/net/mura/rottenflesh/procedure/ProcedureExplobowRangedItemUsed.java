package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;

import java.util.Map;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureExplobowRangedItemUsed extends ElementsRottenfleshMod.ModElement {
	public ProcedureExplobowRangedItemUsed(ElementsRottenfleshMod instance) {
		super(instance, 4);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure ExplobowRangedItemUsed!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		if (entity instanceof EntityLivingBase)
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, (int) 10, (int) 5));
	}
}
