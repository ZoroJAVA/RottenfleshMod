
package net.mura.rottenflesh.creativetab;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.creativetab.CreativeTabs;

@ElementsRottenfleshMod.ModElement.Tag
public class TabRottenflesh extends ElementsRottenfleshMod.ModElement {
	public TabRottenflesh(ElementsRottenfleshMod instance) {
		super(instance, 11);
	}

	@Override
	public void initElements() {
		tab = new CreativeTabs("tabrottenflesh") {
			@SideOnly(Side.CLIENT)
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Items.ROTTEN_FLESH, (int) (1));
			}

			@SideOnly(Side.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};
	}
	public static CreativeTabs tab;
}
