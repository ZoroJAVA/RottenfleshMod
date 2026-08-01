package net.mura.rottenflesh.procedure;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Boite a outils pour faire "coller" progressivement une fleche vers un mob
 * hostile situe a peu pres devant elle. Pas de tracking d'entites ici : on
 * appelle steerTowardNearestTarget(this, ...) directement depuis le onUpdate()
 * de la fleche custom concernee, a chaque tick.
 */
public class HomingArrowHandler {

	private static final double LOCK_RADIUS = 128.0D; // portee de detection des cibles
	private static final double TURN_STRENGTH = 0.5D; // 0 = ligne droite, 1 = collage parfait instantane

	// Angle maximum (en degres) entre la direction actuelle de la fleche et une
	// cible pour qu'elle soit consideree "dans le champ de vision" de la fleche.
	// En dessous de cet angle, la cible est ignoree meme si elle est tres proche.
	private static final double FIELD_OF_VIEW_DEGREES = 70.0D;
	private static final double FIELD_OF_VIEW_COS = Math.cos(Math.toRadians(FIELD_OF_VIEW_DEGREES));

	private HomingArrowHandler() {
	}

	/**
	 * Version simple : la vitesse d'impact suit la decroissance naturelle du jeu
	 * (la fleche ralentit avec le temps comme une fleche normale).
	 */
	public static void steerTowardNearestTarget(EntityArrow arrow) {
		steerTowardNearestTarget(arrow, 10.0D);
	}

	/**
	 * Version avec vitesse forcee : si forcedSpeed > 0, la fleche garde toujours
	 * cette vitesse au lieu de subir la decroissance naturelle. Utile pour que
	 * les degats a l'impact (qui dependent de la vitesse) restent constants meme
	 * apres un long vol en poursuite d'une cible.
	 */
	public static void steerTowardNearestTarget(EntityArrow arrow, double forcedSpeed) {
		World world = arrow.world;

		Vec3d currentMotion = new Vec3d(arrow.motionX, arrow.motionY, arrow.motionZ);
		double naturalSpeed = currentMotion.lengthVector();
		if (naturalSpeed < 0.001D) {
			return;
		}
		Vec3d currentDir = currentMotion.normalize();

		AxisAlignedBB searchBox = arrow.getEntityBoundingBox().grow(LOCK_RADIUS);
		List<EntityLivingBase> candidates = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox,
				e -> (e instanceof IMob || e instanceof EntityDragon) && !(e instanceof EntityEnderman) && e.isEntityAlive());

		// Parmi les cibles DANS LE CHAMP DE VISION de la fleche (cone devant elle),
		// on garde la plus proche. Une cible derriere ou sur le cote est ignoree,
		// meme si elle est plus proche qu'une cible devant.
		EntityLivingBase closestInView = null;
		double closestDistSq = Double.MAX_VALUE;

		for (EntityLivingBase candidate : candidates) {
			Vec3d toCandidate = new Vec3d(
					candidate.posX - arrow.posX,
					(candidate.posY + candidate.height * 0.5D) - arrow.posY,
					candidate.posZ - arrow.posZ
			).normalize();

			double dot = currentDir.dotProduct(toCandidate); // 1 = pile devant, 0 = perpendiculaire, -1 = derriere
			if (dot < FIELD_OF_VIEW_COS) {
				continue; // hors du champ de vision, on l'ignore
			}

			double distSq = arrow.getDistanceSq(candidate);
			if (distSq < closestDistSq) {
				closestDistSq = distSq;
				closestInView = candidate;
			}
		}

		if (closestInView == null) {
			return; // rien devant la fleche, elle continue tout droit
		}

		Vec3d toTarget = new Vec3d(
				closestInView.posX - arrow.posX,
				(closestInView.posY + closestInView.height * 0.5D) - arrow.posY,
				closestInView.posZ - arrow.posZ
		).normalize();

		Vec3d correction = toTarget.subtract(currentDir).scale(TURN_STRENGTH);
		Vec3d newDir = currentDir.add(correction).normalize();

		double speed = forcedSpeed > 0 ? forcedSpeed : naturalSpeed;
		arrow.motionX = newDir.x * speed;
		arrow.motionY = newDir.y * speed;
		arrow.motionZ = newDir.z * speed;
	}
}