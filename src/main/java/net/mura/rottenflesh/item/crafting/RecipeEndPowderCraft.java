
package net.mura.rottenflesh.item.crafting;

import net.mura.rottenflesh.item.ItemEnderPowder;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

@ElementsRottenfleshMod.ModElement.Tag
public class RecipeEndPowderCraft extends ElementsRottenfleshMod.ModElement {
	public RecipeEndPowderCraft(ElementsRottenfleshMod instance) {
		super(instance, 86);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		GameRegistry.addSmelting(new ItemStack(Items.ENDER_EYE, (int) (1)), new ItemStack(ItemEnderPowder.block, (int) (1)), 1F);
	}
}
