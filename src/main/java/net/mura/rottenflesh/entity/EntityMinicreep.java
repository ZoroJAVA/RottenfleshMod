package net.mura.rottenflesh.entity;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.BiomeDictionary;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.mura.rottenflesh.block.BlockMiniTNT;
import net.mura.rottenflesh.client.model.minicreeper;
import net.mura.rottenflesh.item.ItemCreeperHeart;

import java.util.Iterator;
import java.util.ArrayList;

@ElementsRottenfleshMod.ModElement.Tag
public class EntityMinicreep extends ElementsRottenfleshMod.ModElement {
	public static final int ENTITYID = 6;
	public static final int ENTITYID_RANGED = 7;

	// Facteur d'echelle visuelle : desormais inutile pour la taille (le modele
	// custom a deja ses propres proportions definies dans Blockbench), garde ici
	// uniquement au cas ou tu veuilles un ajustement fin supplementaire plus tard.
	public static final float VISUAL_SCALE = 1.0f;

	public EntityMinicreep(ElementsRottenfleshMod instance) {
		super(instance, 72);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
				.id(new ResourceLocation("rottenflesh", "minicreep"), ENTITYID).name("minicreep").tracker(64, 3, true).build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		Biome[] spawnBiomes = overworldBiomes();
		EntityRegistry.addSpawn(EntityCustom.class, 10, 1, 2, EnumCreatureType.CREATURE, spawnBiomes);
	}

	/**
	 * Filtre pour exclure le Nether et The End (evite de faire spawn le mob dans
	 * des environnements ou il brulerait, isImmuneToFire = false, ou n'a aucun
	 * sens thematiquement). BiomeDictionary.Type n'a pas de tag generique
	 * "OVERWORLD" en 1.12.2, donc on exclut plutot les deux tags qui existent.
	 */
	private Biome[] overworldBiomes() {
		ArrayList<Biome> ls = new ArrayList<Biome>();
		Iterator<Biome> itr = Biome.REGISTRY.iterator();
		while (itr.hasNext()) {
			Biome biome = itr.next();
			boolean isNether = BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
			boolean isEnd = BiomeDictionary.hasType(biome, BiomeDictionary.Type.END);
			if (!isNether && !isEnd) {
				ls.add(biome);
			}
		}
		return ls.toArray(new Biome[ls.size()]);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> {
			return new RenderLiving(renderManager, new minicreeper(), 0.3f) {
				protected ResourceLocation getEntityTexture(Entity entity) {
					return new ResourceLocation("rottenflesh:textures/entity/minicreeper.png");
				}
			};
		});
	}
	public static class EntityCustom extends EntityCreature {
		public EntityCustom(World world) {
			super(world);
			// Estimation basee sur les dimensions du modele custom (a ajuster
			// visuellement en jeu si la hitbox parait trop grande/petite)
			setSize(0.4f, 0.7f);
			experienceValue = 4;
			this.isImmuneToFire = false;
			setNoAI(!true);
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.tasks.addTask(1, new EntityAIWander(this, 1));
			this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
			this.tasks.addTask(3, new EntityAILookIdle(this));
			this.tasks.addTask(4, new EntityAISwimming(this));
		}

		@Override
		public EnumCreatureAttribute getCreatureAttribute() {
			return EnumCreatureAttribute.UNDEFINED;
		}

		@Override
		protected Item getDropItem() {
			return new ItemStack(ItemCreeperHeart.block , (int) (1)).getItem();
		}

		@Override
		public net.minecraft.util.SoundEvent getAmbientSound() {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.cat.purr"));
		}

		@Override
		public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.creeper.hurt"));
		}

		@Override
		public net.minecraft.util.SoundEvent getDeathSound() {
			return (net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.creeper.death"));
		}

		@Override
		protected float getSoundVolume() {
			return 1.0F;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source == DamageSource.FALL)
				return false;
			if (source == DamageSource.CACTUS)
				return false;
			return super.attackEntityFrom(source, amount);
		}

		@Override
		public boolean processInteract(EntityPlayer entity, EnumHand hand) {
			super.processInteract(entity, hand);
			entity.startRiding(this);
			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;
			ItemStack itemstack = entity.getHeldItem(hand);
			return true;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3D);
			if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
			if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10D);
			if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2D);
		}

		@Override
		public void travel(float ti, float tj, float tk) {
			Entity entity = this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
			if (this.isBeingRidden()) {
				this.rotationYaw = entity.rotationYaw;
				this.prevRotationYaw = this.rotationYaw;
				this.rotationPitch = entity.rotationPitch * 0.5F;
				this.setRotation(this.rotationYaw, this.rotationPitch);
				this.jumpMovementFactor = this.getAIMoveSpeed() * 0.15F;
				this.renderYawOffset = entity.rotationYaw;
				this.rotationYawHead = entity.rotationYaw;
				this.stepHeight = 1.0F;
				if (entity instanceof EntityLivingBase) {
					this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
					float forward = ((EntityLivingBase) entity).moveForward;
					float strafe = 0;
					super.travel(strafe, 0, forward);
				}
				this.prevLimbSwingAmount = this.limbSwingAmount;
				double d1 = this.posX - this.prevPosX;
				double d0 = this.posZ - this.prevPosZ;
				float f1 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
				if (f1 > 1.0F)
					f1 = 1.0F;
				this.limbSwingAmount += (f1 - this.limbSwingAmount) * 0.4F;
				this.limbSwing += this.limbSwingAmount;
				return;
			}
			this.stepHeight = 0.5F;
			this.jumpMovementFactor = 0.02F;
			super.travel(ti, tj, tk);
		}
	}
}