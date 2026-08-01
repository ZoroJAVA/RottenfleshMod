package net.mura.rottenflesh.item;
import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.procedure.ShovelMiningHandler;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.Item;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityLivingBase;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
@ElementsRottenfleshMod.ModElement.Tag
public class ItemOrangeshovel extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:orangeshovel")
	public static final Item block = null;
	public ItemOrangeshovel(ElementsRottenfleshMod instance) {
		super(instance, 69);
	}
	@Override
	public void initElements() {
		elements.items.add(() -> new ItemSpade(EnumHelper.addToolMaterial("ORANGESHOVEL", 1, 3200, 4f, 0f, 12)) {
			{
				this.attackSpeed = -3f;
			}
			public Set<String> getToolClasses(ItemStack stack) {
				HashMap<String, Integer> ret = new HashMap<String, Integer>();
				ret.put("spade", 1);
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
					ShovelMiningHandler.executeProcedure($_dependencies);
				}
				return retval;
			}
		}.setUnlocalizedName("orangeshovel").setRegistryName("orangeshovel").setCreativeTab(TabRottenflesh.tab));
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("rottenflesh:orangeshovel", "inventory"));
	}
}