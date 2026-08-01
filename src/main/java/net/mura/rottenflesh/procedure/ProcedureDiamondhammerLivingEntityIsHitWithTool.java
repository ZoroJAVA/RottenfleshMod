package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import java.util.Map;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureDiamondhammerLivingEntityIsHitWithTool extends ElementsRottenfleshMod.ModElement {

	public ProcedureDiamondhammerLivingEntityIsHitWithTool(ElementsRottenfleshMod instance) {
		super(instance, 55);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {

		if (!dependencies.containsKey("entity"))
			return;

		if (!dependencies.containsKey("sourceentity"))
			return;


		Entity target = (Entity) dependencies.get("entity");
		Entity source = (Entity) dependencies.get("sourceentity");


		if (!(source instanceof EntityPlayer))
			return;


		EntityPlayer player = (EntityPlayer) source;
		World world = player.world;

		if (player.rotationPitch > -30F) {
		   player.addVelocity(0, 0.6D, -0.5D);
 		   player.velocityChanged = true;
}

		// Son du marteau
		world.playSound(
			null,
			player.posX,
			player.posY,
			player.posZ,
			SoundEvents.ENTITY_GENERIC_EXPLODE,
			SoundCategory.PLAYERS,
			0.5F,
			0.6F
		);


		// Repousse les mobs autour
		for (Entity entity : world.loadedEntityList) {

			if (entity instanceof EntityLivingBase && entity != player) {

				EntityLivingBase mob = (EntityLivingBase) entity;

				double distance = player.getDistance(mob);

				if (distance <= 3.5D) {

					double motionX = mob.posX - player.posX;
					double motionZ = mob.posZ - player.posZ;

					mob.addVelocity(
						motionX * 0.8D,
						0.6D,
						motionZ * 0.8D
					);
				}
			}
		}
	}
}