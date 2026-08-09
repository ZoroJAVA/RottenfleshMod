package net.mura.rottenflesh.entity;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
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
				protected ResourceLocation getEntityTexture(Entity entity) {
					return new ResourceLocation("rottenflesh:textures/entity/herobrine.png");
				}
			};
		});
	}

	public static class EntityCustom extends EntityMob {
		public EntityCustom(World world) {
			super(world);
			setSize(0.6f, 1.95f); // memes dimensions de hitbox qu'un joueur
			experienceValue = 10;
			this.isImmuneToFire = false;
		}

		@Override
		protected void initEntityAI() {
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
			this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
			this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 50.0F));
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
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
			if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30D);
			if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
			if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
				this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.5D);
			if (this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null)
				this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
		}
	}
}
