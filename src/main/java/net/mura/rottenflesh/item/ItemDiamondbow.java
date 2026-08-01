
package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.item.RenderArrowCustom;

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
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemDiamondbow extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:diamondbow")
	public static final Item block = null;
	public static final int ENTITYID = 4;
	public ItemDiamondbow(ElementsRottenfleshMod instance) {
		super(instance, 41);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new RangedItem());
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityArrowCustom.class)
				.id(new ResourceLocation("rottenflesh", "entitybulletdiamondbow"), ENTITYID).name("entitybulletdiamondbow").tracker(64, 1, true)
				.build());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:diamondbow", "inventory"));
	}

// Cette ligne cree une fleche personnalisee avec rendu vanilla v registre de rendu passe de RenderSnowball a RenderArrow.
	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, renderManager -> {
			return new RenderArrowCustom(renderManager);
		});
	}
	public static class RangedItem extends Item {
		public RangedItem() {
			super();
			setMaxDamage(1560);
			setFull3D();
			setUnlocalizedName("diamondbow");
			setRegistryName("diamondbow");
			maxStackSize = 1;
			setCreativeTab(TabRottenflesh.tab);
		}

// For jusqu'a } : demultiplieur de fleche. < 3 vaut 3 fleche par tire.
		@Override
		public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityLivingBase entityLivingBase, int timeLeft) {
			if (!world.isRemote && entityLivingBase instanceof EntityPlayerMP) {
				EntityPlayerMP entity = (EntityPlayerMP) entityLivingBase;
				float power = 2.5f;
				for (int i = 0; i < 3; i++) {
    EntityArrowCustom entityarrow = new EntityArrowCustom(world, entity);

    entityarrow.shoot(
            entity.getLookVec().x,
            entity.getLookVec().y,
            entity.getLookVec().z,
            power * 2,
            1F // Taux de dispersion, 0 = pas de disp, 1F petite disp.
    );

    entityarrow.setSilent(true);
    entityarrow.setIsCritical(true);
    entityarrow.setDamage(8);
    entityarrow.setKnockbackStrength(2);
    entityarrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;

    world.spawnEntity(entityarrow);
}

// Son de tir v
world.playSound(
    null,
    entity.posX,
    entity.posY,
    entity.posZ,
    net.minecraft.init.SoundEvents.ENTITY_ARROW_SHOOT,
    SoundCategory.NEUTRAL,
    1.0F,
    1.0F
);
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

	public static class EntityArrowCustom extends EntityArrow {
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
    protected ItemStack getArrowStack() {
        return new ItemStack(Items.ARROW);
    }
    
		@Override
		protected void arrowHit(EntityLivingBase entity) {
			super.arrowHit(entity);
			entity.setArrowCountInEntity(entity.getArrowCountInEntity() - 1);
		}
	}
}
