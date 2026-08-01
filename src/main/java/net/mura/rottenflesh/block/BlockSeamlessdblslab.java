
package net.mura.rottenflesh.block;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.Block;

@ElementsRottenfleshMod.ModElement.Tag
public class BlockSeamlessdblslab extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:seamlessdblslab")
	public static final Block block = null;
	public BlockSeamlessdblslab(ElementsRottenfleshMod instance) {
		super(instance, 35);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new BlockCustom().setRegistryName("seamlessdblslab"));
		elements.items.add(() -> new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation("rottenflesh:seamlessdblslab", "inventory"));
	}
	public static class BlockCustom extends Block {
		public BlockCustom() {
			super(Material.ROCK);
			setUnlocalizedName("seamlessdblslab");
			setSoundType(SoundType.STONE);
			setHarvestLevel("pickaxe", 1);
			setHardness(1F);
			setResistance(10F);
			setLightLevel(0F);
			setLightOpacity(255);
			setCreativeTab(TabRottenflesh.tab);
		}
	}
}
