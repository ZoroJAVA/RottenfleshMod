package net.mura.rottenflesh.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * Version reduite du TNT prime vanilla : meme compte a rebours (fuse),
 * mais explosion beaucoup plus petite (1.5 au lieu de 4.0 en vanilla).
 *
 * Note : EntityTNTPrimed.explode() n'est pas visible depuis un autre package
 * (pas de "protected"/"public" devant), donc impossible a redefinir. On
 * reimplemente onUpdate() en entier a la place, avec la meme logique que
 * vanilla mais une explosion plus petite a la fin.
 */
public class EntityMiniTNTPrimed extends EntityTNTPrimed {

	// Rayon d'explosion : ajuste cette valeur pour rendre le "mini" TNT
	// plus ou moins puissant (le TNT vanilla utilise 4.0F)
	public static final float EXPLOSION_RADIUS = 1.5F;

	public EntityMiniTNTPrimed(World worldIn) {
		super(worldIn);
	}

	public EntityMiniTNTPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter) {
		super(worldIn, x, y, z, igniter);
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.04D;
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if (this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
			this.motionY *= -0.5D;
		}

		int fuse = this.getFuse() - 1;
		this.setFuse(fuse);

		if (fuse <= 0) {
			this.setDead();
			if (!this.world.isRemote) {
				this.world.createExplosion(this, this.posX, this.posY + (double) (this.height / 16.0F), this.posZ,
						EXPLOSION_RADIUS, true);
			}
		} else {
			this.handleWaterMovement();
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ,
					0.0D, 0.0D, 0.0D);
		}
	}
}