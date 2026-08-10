package net.mura.rottenflesh.entity;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Items;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.model.ModelBiped;

// Entite Herobrine : meme silhouette qu'un joueur (utilise le modele vanilla ModelBiped, celui-la
// meme que zombies/squelettes/etc., avec les memes proportions qu'un joueur), texture custom.
@ElementsRottenfleshMod.ModElement.Tag
public class Herobrine extends ElementsRottenfleshMod.ModElement {
	public static final int ENTITYID = 20;

	public Herobrine(ElementsRottenfleshMod instance) {
		super(instance, 210);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
				.id(new ResourceLocation("rottenflesh", "herobrine"), ENTITYID).name("herobrine").tracker(64, 3, true).build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> {
			// 0.0f/0.0f = pas d'inflation supplementaire, 64x64 = format de skin moderne.
			ModelBiped model = new ModelBiped(0.0f, 0.0f, 64, 64);
			// La couche "chapeau" (bipedHeadwear) sert aux cheveux/casquettes des skins de joueur ;
			// herobrine.png n'a pas cette zone proprement transparente, ce qui causait un liseré de
			// pixels flottant autour de la tete. On la desactive puisqu'on ne s'en sert pas.
			model.bipedHeadwear.showModel = false;
			return new RenderLiving(renderManager, model, 0.5f) {
				// RenderLiving ne rend pas les items tenus en main par defaut (contrairement a
				// RenderBiped) ; on ajoute la couche manuellement pour voir l'epee.
				{
					this.addLayer(new LayerHeldItem(this));
				}

				// Pendant l'embuscade (isHidingBody()), cache tout le corps sauf la tete, qui reste
				// visible et flotte seule. Parametre en EntityLiving (pas EntityCustom) : avec le type
				// brut RenderLiving, seule cette signature override reellement doRender (le T generique
				// de RenderLiving<T extends EntityLiving> s'efface en EntityLiving), sinon ce serait
				// juste une surcharge jamais appelee par le moteur de rendu.
				@Override
				public void doRender(net.minecraft.entity.EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks) {
					boolean hidingBody = entity instanceof EntityCustom && ((EntityCustom) entity).isHidingBody();
					model.bipedBody.showModel = !hidingBody;
					model.bipedRightArm.showModel = !hidingBody;
					model.bipedLeftArm.showModel = !hidingBody;
					model.bipedRightLeg.showModel = !hidingBody;
					model.bipedLeftLeg.showModel = !hidingBody;
					super.doRender(entity, x, y, z, entityYaw, partialTicks);
				}

				protected ResourceLocation getEntityTexture(Entity entity) {
					return new ResourceLocation("rottenflesh:textures/entity/herobrine.png");
				}
			};
		});
	}

	public static class EntityCustom extends EntityMob {
		// Vitesse de marche normale (identique a celle d'un zombie), quand il n'a pas encore repere de cible.
		private static final double NORMAL_SPEED = 0.25D;
		// Vitesse d'approche discrete une fois qu'il a repere une cible mais reste a distance :
		// plus lente que la marche normale, pour un effet "il te traque sans se presser".
		private static final double STALK_SPEED = 0.20D;
		// Vitesse de charge finale, au niveau d'un cheval au galop, une fois tout pres de la cible.
		private static final double HORSE_SPEED = 0.3375D;
		// En dessous de cette distance (en blocs) a la cible, il passe de la traque a la charge.
		private static final double CHARGE_DISTANCE = 35.0D;

		// Un coup encaissant au moins ces degats declenche une teleportation derriere l'attaquant.
		private static final float TELEPORT_DAMAGE_THRESHOLD = 4.0F;
		// Delai minimum entre deux teleportations : reduit pour qu'il puisse l'utiliser frequemment
		// en plein combat corps a corps (esquive/repositionnement rapide).
		private static final int TELEPORT_COOLDOWN_TICKS = 30;
		// Distance a laquelle il reapparait derriere la cible : plus grande porte de teleportation.
		private static final double TELEPORT_BEHIND_DISTANCE = 4.0D;

		private int teleportCooldown = 0;
		// Empeche de re-invoquer des zombies a chaque tick une fois passe sous la moitie de sa vie.
		private boolean halfHealthZombiesSummoned = false;

		// Fenetre glissante : s'il ne touche personne pendant 30 secondes d'affilee (le minuteur se
		// reinitialise a chaque coup porte), il devient invisible 4 secondes puis se teleporte a
		// 1 bloc devant le joueur. Peut se reproduire plusieurs fois par combat.
		private static final int NO_HIT_TIMEOUT_TICKS = 20 * 20;
		private static final int AMBUSH_INVISIBILITY_TICKS = 4 * 20;
		private static final double AMBUSH_TELEPORT_DISTANCE = 1.0D;

		private int ticksAliveWithoutHit = 0;
		private int ambushInvisibilityRemaining = 0;

		// Synchronise au client (le rendu est calcule cote client, qui n'a pas acces aux champs
		// serveur ci-dessus) : indique s'il faut cacher le corps en ne laissant que la tete visible.
		private static final DataParameter<Boolean> HIDING_BODY = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);

		@Override
		protected void entityInit() {
			super.entityInit();
			this.dataManager.register(HIDING_BODY, false);
		}

		public boolean isHidingBody() {
			return this.dataManager.get(HIDING_BODY);
		}

		// Barre de boss en haut de l'ecran (meme systeme que le Wither/l'Ender Dragon). Les joueurs
		// y sont ajoutes/retires automatiquement via addTrackingPlayer/removeTrackingPlayer, donc
		// elle apparait des qu'un joueur le voit (entre dans sa zone de tracking).
		private final BossInfoServer bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS);

		public EntityCustom(World world) {
			super(world);
			setSize(0.6f, 1.95f); // memes dimensions de hitbox qu'un joueur
			experienceValue = 10;
			this.isImmuneToFire = false;
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
			// Pas de chance de laisser tomber l'epee a la mort (coherent avec getDropItem() = null).
			this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
		}

		@Override
		public void onLivingUpdate() {
			super.onLivingUpdate();
			if (!this.world.isRemote) {
				if (this.teleportCooldown > 0) {
					this.teleportCooldown--;
				}
				if (!this.halfHealthZombiesSummoned && this.getHealth() <= this.getMaxHealth() / 2.0F) {
					this.halfHealthZombiesSummoned = true;
					summonZombieReinforcements();
				}
				this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

				if (this.ambushInvisibilityRemaining > 0) {
					this.ambushInvisibilityRemaining--;
					if (this.ambushInvisibilityRemaining == 0) {
						this.dataManager.set(HIDING_BODY, false);
						this.ticksAliveWithoutHit = 0; // nouvelle fenetre de 30s apres l'embuscade
					}
				} else {
					this.ticksAliveWithoutHit++;
					if (this.ticksAliveWithoutHit >= NO_HIT_TIMEOUT_TICKS) {
						EntityLivingBase currentTarget = this.getAttackTarget();
						if (currentTarget instanceof EntityPlayer) {
							triggerAmbush((EntityPlayer) currentTarget);
							this.ticksAliveWithoutHit = 0;
						}
						// Sinon (pas de cible pour l'instant) : le compteur reste au-dessus du seuil,
						// on retentera au tick suivant, des qu'un joueur sera cible.
					}
				}
				EntityLivingBase target = this.getAttackTarget();
				double desired = NORMAL_SPEED;
				if (target != null) {
					// Le fixe constamment du regard, meme pendant l'approche, quel que soit l'angle
					// (valeurs de rotation elevees = suit la cible quasi instantanement, sans jamais
					// perdre le contact visuel).
					this.getLookHelper().setLookPositionWithEntity(target, 500.0F, 500.0F);
					double distSq = this.getDistanceSq(target);
					desired = distSq <= CHARGE_DISTANCE * CHARGE_DISTANCE ? HORSE_SPEED : STALK_SPEED;
				}
				IAttributeInstance speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
				if (speed != null && speed.getBaseValue() != desired) {
					speed.setBaseValue(desired);
				}
			}
		}

		@Override
		public void addTrackingPlayer(EntityPlayerMP player) {
			super.addTrackingPlayer(player);
			this.bossInfo.addPlayer(player);
		}

		@Override
		public void removeTrackingPlayer(EntityPlayerMP player) {
			super.removeTrackingPlayer(player);
			this.bossInfo.removePlayer(player);
		}

		@Override
		public void onDeath(DamageSource cause) {
			super.onDeath(cause);
			if (!this.world.isRemote) {
				// Vrai eclair (pas juste visuel) qui tombe pile sur son point de mort.
				this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX, this.posY, this.posZ, false));
			}
		}

		@Override
		public boolean attackEntityAsMob(Entity entityIn) {
			boolean landed = super.attackEntityAsMob(entityIn);
			if (landed) {
				// Coup porte : la fenetre glissante de 30s repart de zero.
				this.ticksAliveWithoutHit = 0;
			}
			return landed;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			boolean hurt = super.attackEntityFrom(source, amount);
			if (hurt && !this.world.isRemote && amount >= TELEPORT_DAMAGE_THRESHOLD && this.teleportCooldown <= 0) {
				Entity trueSource = source.getTrueSource();
				if (trueSource instanceof EntityPlayer) {
					if (teleportBehind((EntityPlayer) trueSource)) {
						this.teleportCooldown = TELEPORT_COOLDOWN_TICKS;
					}
				}
			}
			return hurt;
		}

		/**
		 * Invoque 2 a 3 zombies autour de lui, une seule fois, des qu'il passe sous la moitie de sa
		 * vie. Les zombies recoivent Resistance au Feu en permanence pour survivre en plein jour
		 * (c'est exactement ce que la logique vanilla de combustion des zombies verifie).
		 */
		private void summonZombieReinforcements() {
			int count = 2 + this.rand.nextInt(2); // 2 ou 3
			for (int i = 0; i < count; i++) {
				double angle = this.rand.nextDouble() * Math.PI * 2;
				double dist = 2.0D + this.rand.nextDouble() * 2.0D;
				double x = this.posX + Math.cos(angle) * dist;
				double z = this.posZ + Math.sin(angle) * dist;

				EntityZombie zombie = new EntityZombie(this.world);
				zombie.setPosition(x, this.posY, z);
				zombie.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
				this.world.spawnEntity(zombie);
			}
		}

		/**
		 * Le corps disparait (seule la tete reste visible, cf. le renderer) et se teleporte
		 * immediatement a 1 bloc devant le joueur (dans la direction ou il regarde) ; le corps
		 * reapparait 4 secondes plus tard, deja en place pour une embuscade.
		 */
		private void triggerAmbush(EntityPlayer player) {
			this.ambushInvisibilityRemaining = AMBUSH_INVISIBILITY_TICKS;
			this.dataManager.set(HIDING_BODY, true);

			BlockPos spot = findAmbushSpot(player);
			this.setPositionAndUpdate(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D);
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.0F);
			this.getLookHelper().setLookPositionWithEntity(player, 500.0F, 500.0F);
		}

		/**
		 * Cherche un point degage pour l'embuscade : d'abord juste devant le joueur, puis sur ses 4
		 * cotes s'il est colle a un mur (typiquement dans un batiment), puis en dernier recours sa
		 * position exacte (toujours valide, il s'y tient deja) — garantit qu'il se teleporte meme
		 * si le joueur est a l'interieur d'une structure.
		 */
		private BlockPos findAmbushSpot(EntityPlayer player) {
			float yawRad = player.rotationYaw * ((float) Math.PI / 180F);
			double forwardX = -MathHelper.sin(yawRad);
			double forwardZ = MathHelper.cos(yawRad);
			BlockPos spot = findTeleportSpot(player.posX + forwardX * AMBUSH_TELEPORT_DISTANCE, player.posY,
					player.posZ + forwardZ * AMBUSH_TELEPORT_DISTANCE);
			if (spot != null) {
				return spot;
			}
			for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
				spot = findTeleportSpot(player.posX + dir.getFrontOffsetX() * AMBUSH_TELEPORT_DISTANCE, player.posY,
						player.posZ + dir.getFrontOffsetZ() * AMBUSH_TELEPORT_DISTANCE);
				if (spot != null) {
					return spot;
				}
			}
			return new BlockPos(player);
		}

		/**
		 * Tente de teleporter Herobrine juste derriere le joueur (a l'oppose de la direction ou il
		 * regarde), pour attaquer de l'autre cote. Renvoie true si la teleportation a eu lieu.
		 */
		private boolean teleportBehind(EntityPlayer player) {
			float yawRad = player.rotationYaw * ((float) Math.PI / 180F);
			double forwardX = -MathHelper.sin(yawRad);
			double forwardZ = MathHelper.cos(yawRad);
			double targetX = player.posX - forwardX * TELEPORT_BEHIND_DISTANCE;
			double targetZ = player.posZ - forwardZ * TELEPORT_BEHIND_DISTANCE;

			BlockPos spot = findTeleportSpot(targetX, player.posY, targetZ);
			if (spot == null) {
				return false;
			}

			this.setPositionAndUpdate(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D);
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.0F);
			// Se remet immediatement face au joueur pour enchainer l'attaque depuis sa nouvelle position.
			this.getLookHelper().setLookPositionWithEntity(player, 500.0F, 500.0F);
			return true;
		}

		/**
		 * Cherche, autour de (x, startY, z), un point degage (pieds/tete libres, sol solide) pour
		 * y reapparaitre sans se retrouver coince dans un mur ou dans le vide.
		 */
		private BlockPos findTeleportSpot(double x, double startY, double z) {
			int bx = MathHelper.floor(x);
			int bz = MathHelper.floor(z);
			int baseY = MathHelper.floor(startY);
			for (int dy = 0; dy <= 4; dy++) {
				for (int sign = 1; sign >= -1; sign -= 2) {
					if (dy == 0 && sign == -1) {
						continue;
					}
					int by = baseY + dy * sign;
					if (by < 1 || by > 253) {
						continue;
					}
					BlockPos feet = new BlockPos(bx, by, bz);
					BlockPos head = feet.up();
					BlockPos ground = feet.down();
					boolean feetClear = !this.world.getBlockState(feet).getMaterial().isSolid();
					boolean headClear = !this.world.getBlockState(head).getMaterial().isSolid();
					boolean groundSolid = this.world.getBlockState(ground).getMaterial().isSolid();
					if (feetClear && headClear && groundSolid) {
						return feet;
					}
				}
			}
			return null;
		}

		@Override
		protected void initEntityAI() {
			this.tasks.addTask(0, new EntityAISwimming(this));
			// useLongMemory = true : continue de chercher un chemin vers sa cible meme s'il la perd
			// de vue temporairement (mur, structure) au lieu d'abandonner la poursuite.
			this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));
			this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
			this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 150.0F));
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
			// checkSight = false : le repere/verrouille meme a travers les murs/structures (il "voit"
			// le joueur peu importe les obstacles). Le pathfinding vanilla se charge de le contourner.
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, false));
		}

		@Override
		public EnumCreatureAttribute getCreatureAttribute() {
			return EnumCreatureAttribute.UNDEFINED;
		}

		@Override
		protected Item getDropItem() {
			return null;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			// 100 PV = vie d'un Iron Golem.
			if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100D);
			if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
			if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5D);
			if (this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null)
				this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(150D);
			// 20 points d'armure = exactement un set complet d'armure en diamant (3+8+6+3), plus la
			// tenacite (8 = 2 par piece) qui reduit l'efficacite des gros coups. Sans porter les
			// pieces visuellement.
			if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(20D);
			if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(8D);
		}
	}
}
