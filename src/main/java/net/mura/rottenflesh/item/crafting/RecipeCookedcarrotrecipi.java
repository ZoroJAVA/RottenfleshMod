
package net.mura.rottenflesh.item.crafting;

import net.mura.rottenflesh.item.ItemCookedcarrot;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

@ElementsRottenfleshMod.ModElement.Tag
public class RecipeCookedcarrotrecipi extends ElementsRottenfleshMod.ModElement {
	public RecipeCookedcarrotrecipi(ElementsRottenfleshMod instance) {
		super(instance, 21);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		GameRegistry.addSmelting(new ItemStack(Items.CARROT, (int) (1)), new ItemStack(ItemCookedcarrot.block, (int) (1)), 1F);
	}
}
