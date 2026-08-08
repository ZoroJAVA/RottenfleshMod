package net.mura.rottenflesh.world;

import net.mura.rottenflesh.block.BlockOrangeoreblock;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

// Dans la dimension Mine uniquement : tout bloc d'eponge est instantanement remplace par
// rottenflesh:orangeoreblock. Couvre trois sources :
// - les eponges deja presentes dans un chunk au moment ou il se charge (ChunkEvent.Load)
// - les eponges placees par un joueur (BlockEvent.PlaceEvent)
// - les eponges collees par MineSchematicRegenHandler (gere directement dans ce fichier-la)
@ElementsRottenfleshMod.ModElement.Tag
public class MineSpongeConverter extends ElementsRottenfleshMod.ModElement {
	public MineSpongeConverter(ElementsRottenfleshMod instance) {
		super(instance, 201);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != WorldMine.DIMID)
			return;
		convertChunk((World) event.getWorld(), event.getChunk());
	}

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event) {
		if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != WorldMine.DIMID)
			return;
		if (event.getPlacedBlock().getBlock() == Blocks.SPONGE)
			event.getWorld().setBlockState(event.getPos(), orangeOreState(), 3);
	}

	private void convertChunk(World world, Chunk chunk) {
		ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
		int baseX = chunk.x << 4;
		int baseZ = chunk.z << 4;
		for (int si = 0; si < sections.length; si++) {
			ExtendedBlockStorage section = sections[si];
			if (section == null)
				continue;
			int baseY = si << 4;
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						if (section.get(x, y, z).getBlock() == Blocks.SPONGE) {
							world.setBlockState(new BlockPos(baseX + x, baseY + y, baseZ + z), orangeOreState(), 2);
						}
					}
				}
			}
		}
	}

	private static IBlockState orangeOreState() {
		return BlockOrangeoreblock.block.getDefaultState();
	}
}
