
package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.entity.EntityMinicreep;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemSpawnMiniCreep extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:spawn_mini_creep")
	public static final Item block = null;
	public ItemSpawnMiniCreep(ElementsRottenfleshMod instance) {
		super(instance, 79);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:spawn_mini_creep", "inventory"));
	}
	public static class ItemCustom extends Item {
		public ItemCustom() {
			setMaxDamage(0);
			maxStackSize = 64;
			setUnlocalizedName("spawn_mini_creep");
			setRegistryName("spawn_mini_creep");
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
		public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
			super.addInformation(itemstack, world, list, flag);
			list.add("Spawn a cut HUGE Creeper");
		}

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
			ItemStack itemstack = entity.getHeldItem(hand);
			if (!world.isRemote) {
				double reach = 5.0D;
				Vec3d eyePos = entity.getPositionEyes(1.0F);
				Vec3d lookVec = entity.getLook(1.0F);
				Vec3d farPos = eyePos.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
				RayTraceResult trace = world.rayTraceBlocks(eyePos, farPos, false, true, false);
				Vec3d spawnPos = trace != null ? trace.hitVec : farPos;
				EntityMinicreep.EntityCustom minicreep = new EntityMinicreep.EntityCustom(world);
				minicreep.setLocationAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, entity.rotationYaw, 0);
				world.spawnEntity(minicreep);
				if (!entity.capabilities.isCreativeMode)
					itemstack.shrink(1);
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
	}
}
