
package net.mura.rottenflesh.item.crafting;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

@ElementsRottenfleshMod.ModElement.Tag
public class RecipeRottenfleshh extends ElementsRottenfleshMod.ModElement {
	public RecipeRottenfleshh(ElementsRottenfleshMod instance) {
		super(instance, 1);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		GameRegistry.addSmelting(new ItemStack(Items.ROTTEN_FLESH, (int) (1)), new ItemStack(Items.PORKCHOP, (int) (1)), 1F);
	}
}
