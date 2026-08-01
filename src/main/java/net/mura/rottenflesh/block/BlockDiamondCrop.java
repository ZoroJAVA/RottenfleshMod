package net.mura.rottenflesh.block;

import net.mura.rottenflesh.item.ItemDiamondSeeds;
import net.mura.rottenflesh.item.ItemDiamondNugget;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.item.Item;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.Block;

@ElementsRottenfleshMod.ModElement.Tag
public class BlockDiamondCrop extends ElementsRottenfleshMod.ModElement {
	@GameRegistry.ObjectHolder("rottenflesh:diamond_crop")
	public static final Block block = null;
	public BlockDiamondCrop(ElementsRottenfleshMod instance) {
		super(instance, 74);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new BlockCustom().setRegistryName("diamond_crop"));
		// Pas d'ItemBlock ici : comme le ble vanilla, le bloc de culture lui-meme
		// ne doit jamais exister en tant qu'objet ramassable/donnable. Seule la
		// graine (ItemDiamondSeeds) sert a le planter.
	}

	// registerModels() retire : sans ItemBlock, il n'y a plus d'icone d'inventaire
	// a enregistrer pour ce bloc. Le rendu en jeu (les 8 stades de pousse) est
	// gere separement par le blockstate diamond_crop.json.

	// BlockCrops gere deja tout seul : pousse aleatoire au fil du temps (8 stades,
	// property "age" de 0 a 7), obligation d'etre plante sur de la terre labouree
	// (Farmland), hitbox/rendu en "croix" transparente comme le ble vanilla.
	// On ne redefinit que QUOI planter et QUOI recolter.
	public static class BlockCustom extends BlockCrops {
		public BlockCustom() {
			setUnlocalizedName("diamond_crop");
			setSoundType(SoundType.PLANT);
			// Pas de setCreativeTab ici : comme le ble vanilla, seule la graine
			// (ItemDiamondSeeds) doit apparaitre dans l'onglet creatif, pas le
			// bloc de culture lui-meme.
		}

		@Override
		protected Item getSeed() {
			return ItemDiamondSeeds.block;
		}

		@Override
		protected Item getCrop() {
			return ItemDiamondNugget.block;
		}
	}
}