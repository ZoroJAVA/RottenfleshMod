package net.mura.rottenflesh.block;

import net.mura.rottenflesh.creativetab.TabRottenflesh;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.entity.EntityMiniTNTPrimed;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.Block;

import javax.annotation.Nullable;

@ElementsRottenfleshMod.ModElement.Tag
public class BlockMiniTNT extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:mini_tnt")
	public static final Block block = null;
	public static final int ENTITYID = 8;

	public BlockMiniTNT(ElementsRottenfleshMod instance) {
		super(instance, 77);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new BlockCustom().setRegistryName("mini_tnt"));
		elements.items.add(() -> new ItemBlock(block).setRegistryName(block.getRegistryName()));
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityMiniTNTPrimed.class)
				.id(new ResourceLocation("rottenflesh", "mini_tnt_primed"), ENTITYID).name("mini_tnt_primed")
				.tracker(64, 10, true).build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation("rottenflesh:mini_tnt", "inventory"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		// Reutilise le rendu vanilla du TNT prime (cube blanc/rouge clignotant)
		RenderingRegistry.registerEntityRenderingHandler(EntityMiniTNTPrimed.class, RenderTNTPrimed::new);
	}

	public static class BlockCustom extends BlockTNT {

		// Correspond exactement au modele JSON : de (4,0,4) a (12,8,12) sur 16,
		// converti en fractions de bloc (divise par 16)
		private static final AxisAlignedBB SMALL_AABB =
				new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D);

		public BlockCustom() {
			setUnlocalizedName("mini_tnt");
			setCreativeTab(TabRottenflesh.tab);
		}

		@Override
		public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
			return SMALL_AABB;
		}

		@Override
		public boolean isFullCube(IBlockState state) {
			return false; // sinon le jeu pense a tort que ca remplit tout le bloc
		}

		@Override
		public boolean isOpaqueCube(IBlockState state) {
			return false; // laisse passer la lumiere, comme un objet pose plutot qu'un mur
		}

		// Remplace le comportement vanilla (spawn EntityTNTPrimed) par notre
		// version reduite (spawn EntityMiniTNTPrimed a la place)
		@Override
		public void explode(World worldIn, BlockPos pos, IBlockState state, @Nullable EntityLivingBase igniter) {
			if (!worldIn.isRemote) {
				EntityMiniTNTPrimed entity = new EntityMiniTNTPrimed(worldIn,
						(double) ((float) pos.getX() + 0.5F), (double) pos.getY(),
						(double) ((float) pos.getZ() + 0.5F), igniter);
				worldIn.spawnEntity(entity);
				worldIn.playSound(null, entity.posX, entity.posY, entity.posZ,
						SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
	}
}