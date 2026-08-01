package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.procedure.LiquidStorageHandler;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;
import java.util.List;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemDoublebucket extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:doublebucket")
	public static final Item block = null;
	public ItemDoublebucket(ElementsRottenfleshMod instance) {
		super(instance, 64);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:doublebucket", "inventory"));
	}
	public static class ItemCustom extends Item {
		public ItemCustom() {
			setMaxDamage(0);
			maxStackSize = 1;
			setUnlocalizedName("doublebucket");
			setRegistryName("doublebucket");
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

			LiquidStorageHandler.onRightClick(world, player, itemstack);

			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
			int water = LiquidStorageHandler.getAmount(stack, LiquidStorageHandler.WATER);
			int lava = LiquidStorageHandler.getAmount(stack, LiquidStorageHandler.LAVA);
			String selected = LiquidStorageHandler.getSelected(stack);

			tooltip.add(net.minecraft.util.text.TextFormatting.AQUA + "Eau" + net.minecraft.util.text.TextFormatting.RESET
					+ " : " + water + " / " + LiquidStorageHandler.CAPACITY_MB + " mB");
			tooltip.add(net.minecraft.util.text.TextFormatting.RED + "Lave" + net.minecraft.util.text.TextFormatting.RESET
					+ " : " + lava + " / " + LiquidStorageHandler.CAPACITY_MB + " mB");
			tooltip.add("Selectionne : " + LiquidStorageHandler.getColoredName(selected));
		}
	}
}