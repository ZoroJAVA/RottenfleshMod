
package net.mura.rottenflesh.item.crafting;

import net.mura.rottenflesh.block.BlockMiniTNT;
import net.mura.rottenflesh.item.ItemCreeperHeart;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

@ElementsRottenfleshMod.ModElement.Tag
public class RecipeHeartRecip extends ElementsRottenfleshMod.ModElement {
	public RecipeHeartRecip(ElementsRottenfleshMod instance) {
		super(instance, 81);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		GameRegistry.addSmelting(new ItemStack(ItemCreeperHeart.block, (int) (1)), new ItemStack(BlockMiniTNT.block, (int) (1)), 3F);
	}
}
