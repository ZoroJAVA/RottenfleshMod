
package net.mura.rottenflesh.item.crafting;

import net.mura.rottenflesh.item.ItemOrangeore;
import net.mura.rottenflesh.block.BlockOrangeoreblock;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.item.ItemStack;

@ElementsRottenfleshMod.ModElement.Tag
public class RecipeOrangeoresmelt extends ElementsRottenfleshMod.ModElement {
	public RecipeOrangeoresmelt(ElementsRottenfleshMod instance) {
		super(instance, 53);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		GameRegistry.addSmelting(new ItemStack(BlockOrangeoreblock.block, (int) (1)), new ItemStack(ItemOrangeore.block, (int) (1)), 5F);
	}
}
