package net.mura.rottenflesh.world;

import net.mura.rottenflesh.block.BlockOrangeoreblock;
import net.mura.rottenflesh.RottenfleshMod;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;

import java.io.IOException;
import java.io.InputStream;

// Colle une structure .schematic (format WorldEdit/MCEdit) a un emplacement fixe dans la dimension
// Mine, et la regenere (nettoyage + recollage) toutes les 30 minutes.
//
// Fichier attendu : src/main/resources/assets/rottenflesh/schematics/mine_structure.schematic
// (voir README dans ce dossier)
//
// Limitation : ne gere que les blocs avec un ID legacy 0-255 (pas de bloc "AddBlocks" > 255) et ignore
// les tile entities / entites du schematic (contenu de coffres etc. non restaure).
@ElementsRottenfleshMod.ModElement.Tag
public class MineSchematicRegenHandler extends ElementsRottenfleshMod.ModElement {
	public MineSchematicRegenHandler(ElementsRottenfleshMod instance) {
		super(instance, 200);
	}

	private static final String SCHEMATIC_RESOURCE = "/assets/rottenflesh/schematics/mine_structure.schematic";

	// Position du coin (0,0,0) de la structure une fois collee, dans la dimension Mine.
	// Ajuste ces coordonnees pour qu'elles correspondent a "devant" ton portail.
	private static final BlockPos ANCHOR = new BlockPos(0, 50, 55);

	private static final int INITIAL_DELAY_TICKS = 100; // 5s apres le chargement du monde
	private static final int REGEN_INTERVAL_TICKS = 20 * 60 * 30; // 30 minutes

	private int ticksUntilRegen = INITIAL_DELAY_TICKS;
	private NBTTagCompound cachedSchematic;
	private boolean loadFailed = false;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.side.isClient())
			return;
		if (event.world.provider.getDimension() != WorldMine.DIMID)
			return;

		if (--ticksUntilRegen > 0)
			return;
		ticksUntilRegen = REGEN_INTERVAL_TICKS;
		regenerate(event.world);
	}

	private void regenerate(World world) {
		NBTTagCompound schematic = loadSchematic();
		if (schematic == null)
			return;

		int width = schematic.getShort("Width") & 0xFFFF;
		int height = schematic.getShort("Height") & 0xFFFF;
		int length = schematic.getShort("Length") & 0xFFFF;
		byte[] blocksArr = schematic.getByteArray("Blocks");
		byte[] dataArr = schematic.getByteArray("Data");

		// 1. Nettoyage de la zone (remise a l'air) avant de recoller, pour effacer les degats/ajouts
		// que des joueurs auraient pu faire depuis la derniere generation.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					world.setBlockState(ANCHOR.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
				}
			}
		}

		// 2. Collage de la structure (index MCEdit/WorldEdit : y-major, puis z, puis x).
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int index = (y * length + z) * width + x;
					int id = blocksArr[index] & 0xFF;
					if (id == 0)
						continue;
					Block block = Block.REGISTRY.getObjectById(id);
					if (block == null || block == Blocks.AIR)
						continue;
					IBlockState state;
					if (block == Blocks.SPONGE) {
						// Toute eponge generee par la schematic se transforme instantanement en orangeoreblock.
						state = BlockOrangeoreblock.block.getDefaultState();
					} else {
						int meta = dataArr[index] & 0xFF;
						state = block.getStateFromMeta(meta);
					}
					world.setBlockState(ANCHOR.add(x, y, z), state, 2);
				}
			}
		}
	}

	private NBTTagCompound loadSchematic() {
		if (cachedSchematic != null)
			return cachedSchematic;
		if (loadFailed)
			return null;
		try (InputStream stream = RottenfleshMod.class.getResourceAsStream(SCHEMATIC_RESOURCE)) {
			if (stream == null) {
				System.err.println("[rottenflesh] Schematic introuvable : " + SCHEMATIC_RESOURCE);
				loadFailed = true;
				return null;
			}
			cachedSchematic = CompressedStreamTools.readCompressed(stream);
			return cachedSchematic;
		} catch (IOException e) {
			System.err.println("[rottenflesh] Impossible de lire le schematic : " + e.getMessage());
			loadFailed = true;
			return null;
		}
	}
}
