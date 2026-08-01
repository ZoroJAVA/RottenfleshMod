package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.procedure.TorchThrowHandler;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemThrowabletorch extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:throwabletorch")
	public static final Item block = null;
	public ItemThrowabletorch(ElementsRottenfleshMod instance) {
		super(instance, 63);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:throwabletorch", "inventory"));
	}
	public static class ItemCustom extends Item {
		public ItemCustom() {
			setMaxDamage(0);
			maxStackSize = 1;
			setUnlocalizedName("throwabletorch");
			setRegistryName("throwabletorch");
			setCreativeTab(TabRottenflesh.tab);
		}

		@Override
		public int getItemEnchantability() {
			return 0;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemstack) {
			return 0;
		}

		@Override
		public float getDestroySpeed(ItemStack par1ItemStack, IBlockState par2Block) {
			return 1F;
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
			ItemStack itemstack = player.getHeldItem(hand);

			TorchThrowHandler.throwTorchSnowball(world, player);

			// itemstack n'est jamais reduit : le lancer est illimite
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
	}
}