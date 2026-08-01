
package net.mura.rottenflesh.item;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

@ElementsRottenfleshMod.ModElement.Tag
public class ItemOrangeboots extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:orangebootshelmet")
	public static final Item helmet = null;
	@GameRegistry.ObjectHolder("rottenflesh:orangebootsbody")
	public static final Item body = null;
	@GameRegistry.ObjectHolder("rottenflesh:orangebootslegs")
	public static final Item legs = null;
	@GameRegistry.ObjectHolder("rottenflesh:orangebootsboots")
	public static final Item boots = null;
	public ItemOrangeboots(ElementsRottenfleshMod instance) {
		super(instance, 62);
	}

	@Override
	public void initElements() {
		ItemArmor.ArmorMaterial enuma = EnumHelper.addArmorMaterial("ORANGEBOOTS", "rottenflesh:orange_armor", 25, new int[]{12, 0, 0, 2}, 15,
				(net.minecraft.util.SoundEvent) net.minecraft.util.SoundEvent.REGISTRY.getObject(new ResourceLocation("")), 0f);
		elements.items.add(() -> new ItemArmor(enuma, 0, EntityEquipmentSlot.FEET).setUnlocalizedName("orangebootsboots")
				.setRegistryName("orangebootsboots").setCreativeTab(TabRottenflesh.tab));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(boots, 0, new ModelResourceLocation("rottenflesh:orangebootsboots", "inventory"));
	}
}
