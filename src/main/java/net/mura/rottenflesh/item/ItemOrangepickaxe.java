package net.mura.rottenflesh.item;
import net.mura.rottenflesh.procedure.ProcedureHandler;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.Item;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import net.mura.rottenflesh.creativetab.TabRottenflesh;
@ElementsRottenfleshMod.ModElement.Tag
public class ItemOrangepickaxe extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:orangepickaxe")
	public static final Item block = null;
	public ItemOrangepickaxe(ElementsRottenfleshMod instance) {
		super(instance, 48);
	}
	@Override
	public void initElements() {
		// Niveau de minage 3 = diamant, durabilite 3500, efficacite/degats diamant, enchantabilite 10
		elements.items.add(() -> new ItemPickaxe(EnumHelper.addToolMaterial("ORANGEPICKAXE", 3, 3500, 8f, 3f, 10)) {
			{
				this.attackSpeed = -2.8f;
			}
			public Set<String> getToolClasses(ItemStack stack) {
				HashMap<String, Integer> ret = new HashMap<String, Integer>();
				ret.put("pickaxe", 3);
				return ret.keySet();
			}
			@Override
			public boolean onBlockDestroyed(ItemStack itemstack, World world, IBlockState bl, BlockPos pos, EntityLivingBase entity) {
				boolean retval = super.onBlockDestroyed(itemstack, world, bl, pos, entity);
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				{
					Map<String, Object> $_dependencies = new HashMap<>();
					$_dependencies.put("world", world);
					$_dependencies.put("x", x);
					$_dependencies.put("y", y);
					$_dependencies.put("z", z);
					$_dependencies.put("entity", entity);
					ProcedureHandler.executeProcedure($_dependencies);
				}
				return retval;
			}
		}.setUnlocalizedName("orangepickaxe").setRegistryName("orangepickaxe").setCreativeTab(TabRottenflesh.tab));
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:orangepickaxe", "inventory"));
	}
}