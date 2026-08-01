package net.mura.rottenflesh.item;

import net.mura.rottenflesh.block.BlockDiamondCrop;
import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.ItemSeeds;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemDiamondSeeds extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:diamond_seeds")
	public static final Item block = null;
	public ItemDiamondSeeds(ElementsRottenfleshMod instance) {
		super(instance, 73);
	}

	@Override
	public void initElements() {
		elements.items.add(() -> new ItemCustom());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:diamond_seeds", "inventory"));
	}

	// ItemSeeds gere deja tout seul : clic droit sur de la terre labouree
	// (Farmland) plante le bloc de culture (BlockDiamondCrop) au stade 0,
	// et consomme 1 graine de la stack.
	public static class ItemCustom extends ItemSeeds {
		public ItemCustom() {
			super(BlockDiamondCrop.block, Blocks.FARMLAND);
			setUnlocalizedName("diamond_seeds");
			setRegistryName("diamond_seeds");
			setCreativeTab(TabRottenflesh.tab);
		}
	}
}