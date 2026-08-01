package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.procedure.HomingArrowHandler;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.EnumAction;
import net.minecraft.init.Items;
import net.minecraft.init.Enchantments;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.client.renderer.entity.RenderTippedArrow;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemEnderBow extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:ender_bow")
	public static final Item block = null;
	public static final int ENTITYID = 5;
	public ItemEnderBow(ElementsRottenfleshMod instance) {
		super(instance, 71);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem());
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityArrowCustom.class)
				.id(new ResourceLocation("rottenflesh", "entitybulletender_bow"), ENTITYID).name("entitybulletender_bow").tracker(64, 1, true)
				.build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:ender_bow", "inventory"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, renderManager -> {
			return new RenderTippedArrow(renderManager);
		});
	}
	public static class RangedItem extends Item {
		public RangedItem() {
			super();
			setMaxDamage(390);
			setFull3D();
			setUnlocalizedName("ender_bow");
			setRegistryName("ender_bow");
			maxStackSize = 1;
			setCreativeTab(TabRottenflesh.tab);
		}

		@Override
		public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entityLivingBase, int timeLeft) {
			if (!world.isRemote && entityLivingBase instanceof EntityPlayerMP) {
				EntityPlayerMP entity = (EntityPlayerMP) entityLivingBase;
				int slotID = -1;
				for (int i = 0; i < entity.inventory.mainInventory.size(); i++) {
					ItemStack stack = entity.inventory.mainInventory.get(i);
					if (stack != null && stack.getItem() == new ItemStack(Items.ARROW, (int) (1)).getItem()
							&& stack.getMetadata() == new ItemStack(Items.ARROW, (int) (1)).getMetadata()) {
						slotID = i;
						break;
					}
				}
				if (entity.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, itemstack) > 0
						|| slotID != -1) {
					float power = 1f;
					EntityArrowCustom entityarrow = new EntityArrowCustom(world, entity);
					entityarrow.shoot(entity.getLookVec().x, entity.getLookVec().y, entity.getLookVec().z, power * 2, 0);
					entityarrow.setSilent(true);
					entityarrow.setIsCritical(false);
					entityarrow.setDamage(10);
					entityarrow.setKnockbackStrength(5);
					// Memorise la vitesse de depart : le guidage l'utilisera pour que les
					// degats a l'impact (bases sur la vitesse) ne s'affaiblissent pas
					// meme apres un long vol en poursuite d'une cible.
					entityarrow.lockedSpeed = (float) new net.minecraft.util.math.Vec3d(
							entityarrow.motionX, entityarrow.motionY, entityarrow.motionZ).lengthVector();
					itemstack.damageItem(1, entity);
					int x = (int) entity.posX;
					int y = (int) entity.posY;
					int z = (int) entity.posZ;
					world.playSound((EntityPlayer) null, (double) x, (double) y, (double) z,
							(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY
									.getObject(new ResourceLocation(("entity.arrow.shoot"))),
							SoundCategory.NEUTRAL, 1, 1f / (itemRand.nextFloat() * 0.5f + 1f) + (power / 2));
					if (entity.capabilities.isCreativeMode) {
						entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
					} else {
						if (new ItemStack(Items.ARROW, (int) (1)).isItemStackDamageable()) {
							ItemStack stack = entity.inventory.getStackInSlot(slotID);
							if (stack.attemptDamageItem(1, itemRand, entity)) {
								stack.shrink(1);
								stack.setItemDamage(0);
							}
						} else {
							entity.inventory.clearMatchingItems(new ItemStack(Items.ARROW, (int) (1)).getItem(), -1, 1, null);
						}
					}
					if (!world.isRemote)
						world.spawnEntity(entityarrow);
				}
			}
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
			entity.setActiveHand(hand);
			return new ActionResult(EnumActionResult.SUCCESS, entity.getHeldItem(hand));
		}

		@Override
		public EnumAction getItemUseAction(ItemStack itemstack) {
			return EnumAction.BOW;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemstack) {
			return 72000;
		}
	}

	public static class EntityArrowCustom extends EntityTippedArrow {

		// Vitesse figee au moment du tir, utilisee par le guidage pour eviter
		// que les degats (bases sur la vitesse d'impact) ne s'affaiblissent
		// avec le temps de vol. -1 = pas encore definie, guidage utilise la
		// vitesse naturelle (avec decroissance) comme repli.
		public float lockedSpeed = -1f;

		public EntityArrowCustom(World a) {
			super(a);
		}

		public EntityArrowCustom(World worldIn, double x, double y, double z) {
			super(worldIn, x, y, z);
		}

		public EntityArrowCustom(World worldIn, EntityLivingBase shooter) {
			super(worldIn, shooter);
		}

		@Override
		protected void arrowHit(EntityLivingBase entity) {
			super.arrowHit(entity);
			entity.setArrowCountInEntity(entity.getArrowCountInEntity() - 1);
		}

		@Override
		public void onUpdate() {
			// On corrige la trajectoire AVANT le deplacement vanilla de ce tick,
			// pour que le mouvement (et l'orientation visuelle qui en decoule)
			// reflete deja la nouvelle direction.
			if (!this.world.isRemote && !this.inGround) {
				HomingArrowHandler.steerTowardNearestTarget(this, this.lockedSpeed);
			}

			super.onUpdate();

			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;
			World world = this.world;
			Entity entity = (Entity) shootingEntity;
			if (this.inGround) {
				this.world.removeEntity(this);
			}
		}
	}
}