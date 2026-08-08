
package net.mura.rottenflesh.world;

import net.mura.rottenflesh.item.ItemMine;
import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.biome.Biome;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.World;
import net.minecraft.world.Teleporter;
import net.minecraft.world.DimensionType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Biomes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.Block;

import java.util.Random;
import java.util.List;
import java.util.Collections;

import com.google.common.cache.LoadingCache;

@ElementsRottenfleshMod.ModElement.Tag
public class WorldMine extends ElementsRottenfleshMod.ModElement {
	public static int DIMID = 3;
	public static final boolean NETHER_TYPE = false;
	@GameRegistry.ObjectHolder("rottenflesh:mine_portal")
	public static final BlockCustomPortal portal = null;
	public static DimensionType dtype;
	public WorldMine(ElementsRottenfleshMod instance) {
		super(instance, 34);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new BlockCustomPortal());
		elements.items.add(() -> new ItemBlock(portal).setRegistryName(portal.getRegistryName()));
		elements.items.add(() -> new ItemMine().setUnlocalizedName("mine").setRegistryName("mine"));
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		if (DimensionManager.isDimensionRegistered(DIMID)) {
			DIMID = DimensionManager.getNextFreeDimId();
			System.err.println("Dimension ID for dimension mine is already registered. Falling back to ID: " + DIMID);
		}
		dtype = DimensionType.register("mine", "_mine", DIMID, WorldProviderMod.class, true);
		DimensionManager.registerDimension(DIMID, dtype);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(ItemMine.block, 0, new ModelResourceLocation("rottenflesh:mine", "inventory"));
	}
	public static class WorldProviderMod extends WorldProvider {
		@Override
		public void init() {
			// Un seul biome fixe pour tout le monde : pas de generation de biomes, juste un beau ciel bleu.
			this.biomeProvider = new BiomeProviderSingle(Biomes.PLAINS);
			this.nether = NETHER_TYPE;
			this.hasSkyLight = true;
		}

		@Override
		public void calculateInitialWeather() {
		}

		@Override
		public void updateWeather() {
		}

		@Override
		public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk) {
			return false;
		}

		@Override
		public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) {
			return false;
		}

		@Override
		public DimensionType getDimensionType() {
			return dtype;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Vec3d getFogColor(float par1, float par2) {
			return new Vec3d(0.753f, 0.847f, 1);
		}

		@Override
		public IChunkGenerator createChunkGenerator() {
			return new ChunkProviderVoid(this.world);
		}

		@Override
		public boolean isSurfaceWorld() {
			return false;
		}

		@Override
		public boolean canRespawnHere() {
			return false;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public boolean doesXZShowFog(int par1, int par2) {
			return false;
		}

		@Override
		public WorldSleepResult canSleepAt(EntityPlayer player, BlockPos pos) {
			return WorldSleepResult.ALLOW;
		}

		@Override
		protected void generateLightBrightnessTable() {
			float f = 0.5f;
			for (int i = 0; i <= 15; ++i) {
				float f1 = 1 - (float) i / 15f;
				this.lightBrightnessTable[i] = (1 - f1) / (f1 * 3 + 1) * (1 - f) + f;
			}
		}

		@Override
		public boolean doesWaterVaporize() {
			return false;
		}
	}

	public static class TeleporterDimensionMod extends Teleporter {
		// Emplacement unique et fixe du portail dans la dimension Mine : toujours genere ici, jamais
		// ailleurs, ce qui garantit qu'il n'existe jamais plus d'un portail dans cette dimension.
		private static final BlockPos FIXED_MINE_PORTAL_POS = new BlockPos(25, 102, 50);

		// Cle NBT (donnees persistantes du joueur) sous laquelle on retient la position du portail
		// Overworld utilise pour entrer, afin d'y ressortir precisement au retour.
		private static final String MINE_RETURN_POS_KEY = "rottenfleshMineReturnPos";

		private Vec3d lastPortalVec;
		private EnumFacing teleportDirection;
		public TeleporterDimensionMod(WorldServer worldServer, Vec3d lastPortalVec, EnumFacing teleportDirection) {
			super(worldServer);
			this.lastPortalVec = lastPortalVec;
			this.teleportDirection = teleportDirection;
		}

		// Construit le cadre + portail (2 de large, 3 de haut, axe X) a l'emplacement fixe, seulement
		// s'il n'existe pas deja (evite de re-poser les blocs a chaque teleportation).
		// Construit le cadre + portail (2 de large, 3 de haut, axe X) a l'emplacement donne, seulement
		// s'il n'existe pas deja la (permet aussi de reconstruire un portail Overworld detruit).
		private void ensurePortalFrameAt(BlockPos anchor) {
			if (this.world.getBlockState(anchor).getBlock() == portal)
				return;
			IBlockState frame = Blocks.IRON_BLOCK.getDefaultState();
			IBlockState portalState = portal.getDefaultState().withProperty(BlockPortal.AXIS, EnumFacing.Axis.X);
			for (int dx = -1; dx <= 2; dx++) {
				for (int dy = -1; dy <= 3; dy++) {
					BlockPos pos = anchor.add(dx, dy, 0);
					boolean isFrame = dx == -1 || dx == 2 || dy == -1 || dy == 3;
					this.world.setBlockState(pos, isFrame ? frame : portalState, 2);
				}
			}
		}

		private void ensureFixedMinePortal() {
			boolean alreadyExists = this.world.getBlockState(FIXED_MINE_PORTAL_POS).getBlock() == portal;
			ensurePortalFrameAt(FIXED_MINE_PORTAL_POS);
			if (alreadyExists)
				return;
			// Plateforme 6x6 en stone_brick, un bloc en dessous du portail, centree tout autour de lui.
			// Posee seulement lors de la toute premiere creation du portail.
			IBlockState stoneBrick = Blocks.STONEBRICK.getDefaultState();
			int platformY = FIXED_MINE_PORTAL_POS.getY() - 1;
			for (int dx = -2; dx <= 3; dx++) {
				for (int dz = -2; dz <= 3; dz++) {
					this.world.setBlockState(new BlockPos(FIXED_MINE_PORTAL_POS.getX() + dx, platformY, FIXED_MINE_PORTAL_POS.getZ() + dz),
							stoneBrick, 2);
				}
			}
		}

		@Override
		public boolean makePortal(Entity entityIn) {
			int i = 16;
			double d0 = -1.0D;
			int j = MathHelper.floor(entityIn.posX);
			int k = MathHelper.floor(entityIn.posY);
			int l = MathHelper.floor(entityIn.posZ);
			int i1 = j;
			int j1 = k;
			int k1 = l;
			int l1 = 0;
			int i2 = this.random.nextInt(4);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
			for (int j2 = j - 16; j2 <= j + 16; ++j2) {
				double d1 = (double) j2 + 0.5D - entityIn.posX;
				for (int l2 = l - 16; l2 <= l + 16; ++l2) {
					double d2 = (double) l2 + 0.5D - entityIn.posZ;
					label293 : for (int j3 = this.world.getActualHeight() - 1; j3 >= 0; --j3) {
						if (this.world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3, l2))) {
							while (j3 > 0 && this.world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3 - 1, l2))) {
								--j3;
							}
							for (int k3 = i2; k3 < i2 + 4; ++k3) {
								int l3 = k3 % 2;
								int i4 = 1 - l3;
								if (k3 % 4 >= 2) {
									l3 = -l3;
									i4 = -i4;
								}
								for (int j4 = 0; j4 < 3; ++j4) {
									for (int k4 = 0; k4 < 4; ++k4) {
										for (int l4 = -1; l4 < 4; ++l4) {
											int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
											int j5 = j3 + l4;
											int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
											blockpos$mutableblockpos.setPos(i5, j5, k5);
											if (l4 < 0 && !this.world.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid()
													|| l4 >= 0 && !this.world.isAirBlock(blockpos$mutableblockpos)) {
												continue label293;
											}
										}
									}
								}
								double d5 = (double) j3 + 0.5D - entityIn.posY;
								double d7 = d1 * d1 + d5 * d5 + d2 * d2;
								if (d0 < 0.0D || d7 < d0) {
									d0 = d7;
									i1 = j2;
									j1 = j3;
									k1 = l2;
									l1 = k3 % 4;
								}
							}
						}
					}
				}
			}
			if (d0 < 0.0D) {
				for (int l5 = j - 16; l5 <= j + 16; ++l5) {
					double d3 = (double) l5 + 0.5D - entityIn.posX;
					for (int j6 = l - 16; j6 <= l + 16; ++j6) {
						double d4 = (double) j6 + 0.5D - entityIn.posZ;
						label231 : for (int i7 = this.world.getActualHeight() - 1; i7 >= 0; --i7) {
							if (this.world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7, j6))) {
								while (i7 > 0 && this.world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7 - 1, j6))) {
									--i7;
								}
								for (int k7 = i2; k7 < i2 + 2; ++k7) {
									int j8 = k7 % 2;
									int j9 = 1 - j8;
									for (int j10 = 0; j10 < 4; ++j10) {
										for (int j11 = -1; j11 < 4; ++j11) {
											int j12 = l5 + (j10 - 1) * j8;
											int i13 = i7 + j11;
											int j13 = j6 + (j10 - 1) * j9;
											blockpos$mutableblockpos.setPos(j12, i13, j13);
											if (j11 < 0 && !this.world.getBlockState(blockpos$mutableblockpos).getMaterial().isSolid()
													|| j11 >= 0 && !this.world.isAirBlock(blockpos$mutableblockpos)) {
												continue label231;
											}
										}
									}
									double d6 = (double) i7 + 0.5D - entityIn.posY;
									double d8 = d3 * d3 + d6 * d6 + d4 * d4;
									if (d0 < 0.0D || d8 < d0) {
										d0 = d8;
										i1 = l5;
										j1 = i7;
										k1 = j6;
										l1 = k7 % 2;
									}
								}
							}
						}
					}
				}
			}
			int i6 = i1;
			int k2 = j1;
			int k6 = k1;
			int l6 = l1 % 2;
			int i3 = 1 - l6;
			if (l1 % 4 >= 2) {
				l6 = -l6;
				i3 = -i3;
			}
			if (d0 < 0.0D) {
				j1 = MathHelper.clamp(j1, 70, this.world.getActualHeight() - 10);
				k2 = j1;
				for (int j7 = -1; j7 <= 1; ++j7) {
					for (int l7 = 1; l7 < 3; ++l7) {
						for (int k8 = -1; k8 < 3; ++k8) {
							int k9 = i6 + (l7 - 1) * l6 + j7 * i3;
							int k10 = k2 + k8;
							int k11 = k6 + (l7 - 1) * i3 - j7 * l6;
							boolean flag = k8 < 0;
							this.world.setBlockState(new BlockPos(k9, k10, k11),
									flag ? Blocks.IRON_BLOCK.getDefaultState().getBlock().getDefaultState() : Blocks.AIR.getDefaultState());
						}
					}
				}
			}
			IBlockState iblockstate = portal.getDefaultState().withProperty(BlockPortal.AXIS, l6 == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
			for (int i8 = 0; i8 < 4; ++i8) {
				for (int l8 = 0; l8 < 4; ++l8) {
					for (int l9 = -1; l9 < 4; ++l9) {
						int l10 = i6 + (l8 - 1) * l6;
						int l11 = k2 + l9;
						int k12 = k6 + (l8 - 1) * i3;
						boolean flag1 = l8 == 0 || l8 == 3 || l9 == -1 || l9 == 3;
						this.world.setBlockState(new BlockPos(l10, l11, k12),
								flag1 ? Blocks.IRON_BLOCK.getDefaultState().getBlock().getDefaultState() : iblockstate, 2);
					}
				}
				for (int i9 = 0; i9 < 4; ++i9) {
					for (int i10 = -1; i10 < 4; ++i10) {
						int i11 = i6 + (i9 - 1) * l6;
						int i12 = k2 + i10;
						int l12 = k6 + (i9 - 1) * i3;
						BlockPos blockpos = new BlockPos(i11, i12, l12);
						this.world.notifyNeighborsOfStateChange(blockpos, this.world.getBlockState(blockpos).getBlock(), false);
					}
				}
			}
			return true;
		}

		@Override
		public void placeInPortal(Entity entityIn, float rotationYaw) {
			// Direction Overworld -> Mine : un seul portail fixe, toujours au meme endroit, jamais de
			// recherche/creation dynamique ailleurs (empeche l'apparition de plusieurs portails).
			if (this.world.provider.getDimension() == DIMID) {
				ensureFixedMinePortal();
				entityIn.setLocationAndAngles(FIXED_MINE_PORTAL_POS.getX() + 0.5D, FIXED_MINE_PORTAL_POS.getY(),
						FIXED_MINE_PORTAL_POS.getZ() + 0.5D, entityIn.rotationYaw, entityIn.rotationPitch);
				entityIn.motionX = 0.0D;
				entityIn.motionY = 0.0D;
				entityIn.motionZ = 0.0D;
				return;
			}
			// Direction Mine -> Overworld : on ressort exactement au portail d'origine utilise pour
			// entrer (mémorisé sur le joueur), au lieu de laisser la recherche vanilla en creer un
			// nouveau ailleurs. On reconstruit le cadre si le joueur (ou autre chose) l'a detruit.
			if (this.world.provider.getDimension() == 0 && entityIn instanceof EntityPlayer) {
				NBTTagCompound data = ((EntityPlayer) entityIn).getEntityData();
				if (data.hasKey(MINE_RETURN_POS_KEY)) {
					NBTTagCompound saved = data.getCompoundTag(MINE_RETURN_POS_KEY);
					BlockPos returnPos = new BlockPos(saved.getInteger("x"), saved.getInteger("y"), saved.getInteger("z"));
					ensurePortalFrameAt(returnPos);
					entityIn.setLocationAndAngles(returnPos.getX() + 0.5D, returnPos.getY(), returnPos.getZ() + 0.5D, entityIn.rotationYaw,
							entityIn.rotationPitch);
					entityIn.motionX = 0.0D;
					entityIn.motionY = 0.0D;
					entityIn.motionZ = 0.0D;
					return;
				}
			}
			if (this.world.provider.getDimensionType().getId() != 1) {
				if (!this.placeInExistingPortal(entityIn, rotationYaw)) {
					this.makePortal(entityIn);
					this.placeInExistingPortal(entityIn, rotationYaw);
				}
			} else {
				int i = MathHelper.floor(entityIn.posX);
				int j = MathHelper.floor(entityIn.posY) - 1;
				int k = MathHelper.floor(entityIn.posZ);
				int l = 1;
				int i1 = 0;
				for (int j1 = -2; j1 <= 2; ++j1) {
					for (int k1 = -2; k1 <= 2; ++k1) {
						for (int l1 = -1; l1 < 3; ++l1) {
							int i2 = i + k1 * 1 + j1 * 0;
							int j2 = j + l1;
							int k2 = k + k1 * 0 - j1 * 1;
							boolean flag = l1 < 0;
							this.world.setBlockState(new BlockPos(i2, j2, k2),
									flag ? Blocks.IRON_BLOCK.getDefaultState().getBlock().getDefaultState() : Blocks.AIR.getDefaultState());
						}
					}
				}
				entityIn.setLocationAndAngles((double) i, (double) j, (double) k, entityIn.rotationYaw, 0.0F);
				entityIn.motionX = 0.0D;
				entityIn.motionY = 0.0D;
				entityIn.motionZ = 0.0D;
			}
		}

		@Override
		public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
			int i = 128;
			double d0 = -1.0D;
			int j = MathHelper.floor(entityIn.posX);
			int k = MathHelper.floor(entityIn.posZ);
			boolean flag = true;
			BlockPos blockpos = BlockPos.ORIGIN;
			long l = ChunkPos.asLong(j, k);
			if (this.destinationCoordinateCache.containsKey(l)) {
				Teleporter.PortalPosition teleporter$portalposition = (Teleporter.PortalPosition) this.destinationCoordinateCache.get(l);
				d0 = 0.0D;
				blockpos = teleporter$portalposition;
				teleporter$portalposition.lastUpdateTime = this.world.getTotalWorldTime();
				flag = false;
			} else {
				BlockPos blockpos3 = new BlockPos(entityIn);
				for (int i1 = -128; i1 <= 128; ++i1) {
					BlockPos blockpos2;
					for (int j1 = -128; j1 <= 128; ++j1) {
						for (BlockPos blockpos1 = blockpos3.add(i1, this.world.getActualHeight() - 1 - blockpos3.getY(), j1); blockpos1
								.getY() >= 0; blockpos1 = blockpos2) {
							blockpos2 = blockpos1.down();
							if (this.world.getBlockState(blockpos1).getBlock() == portal) {
								for (blockpos2 = blockpos1.down(); this.world.getBlockState(blockpos2).getBlock() == portal; blockpos2 = blockpos2
										.down()) {
									blockpos1 = blockpos2;
								}
								double d1 = blockpos1.distanceSq(blockpos3);
								if (d0 < 0.0D || d1 < d0) {
									d0 = d1;
									blockpos = blockpos1;
								}
							}
						}
					}
				}
			}
			if (d0 >= 0.0D) {
				if (flag) {
					this.destinationCoordinateCache.put(l, new Teleporter.PortalPosition(blockpos, this.world.getTotalWorldTime()));
				}
				double d5 = (double) blockpos.getX() + 0.5D;
				double d7 = (double) blockpos.getZ() + 0.5D;
				BlockPattern.PatternHelper blockpattern$patternhelper = portal.createPatternHelper(this.world, blockpos);
				boolean flag1 = blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
				double d2 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X
						? (double) blockpattern$patternhelper.getFrontTopLeft().getZ()
						: (double) blockpattern$patternhelper.getFrontTopLeft().getX();
				double d6 = (double) (blockpattern$patternhelper.getFrontTopLeft().getY() + 1)
						- lastPortalVec.y * (double) blockpattern$patternhelper.getHeight();
				if (flag1) {
					++d2;
				}
				if (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X) {
					d7 = d2 + (1.0D - lastPortalVec.x) * (double) blockpattern$patternhelper.getWidth()
							* (double) blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
				} else {
					d5 = d2 + (1.0D - lastPortalVec.x) * (double) blockpattern$patternhelper.getWidth()
							* (double) blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
				}
				float f = 0.0F;
				float f1 = 0.0F;
				float f2 = 0.0F;
				float f3 = 0.0F;
				if (blockpattern$patternhelper.getForwards().getOpposite() == teleportDirection) {
					f = 1.0F;
					f1 = 1.0F;
				} else if (blockpattern$patternhelper.getForwards().getOpposite() == teleportDirection.getOpposite()) {
					f = -1.0F;
					f1 = -1.0F;
				} else if (blockpattern$patternhelper.getForwards().getOpposite() == teleportDirection.rotateY()) {
					f2 = 1.0F;
					f3 = -1.0F;
				} else {
					f2 = -1.0F;
					f3 = 1.0F;
				}
				double d3 = entityIn.motionX;
				double d4 = entityIn.motionZ;
				entityIn.motionX = d3 * (double) f + d4 * (double) f3;
				entityIn.motionZ = d3 * (double) f2 + d4 * (double) f1;
				entityIn.rotationYaw = rotationYaw - (float) (teleportDirection.getOpposite().getHorizontalIndex() * 90)
						+ (float) (blockpattern$patternhelper.getForwards().getHorizontalIndex() * 90);
				if (entityIn instanceof EntityPlayerMP) {
					((EntityPlayerMP) entityIn).connection.setPlayerLocation(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
				} else {
					entityIn.setLocationAndAngles(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
				}
				return true;
			} else {
				return false;
			}
		}
	}

	public static class BlockCustomPortal extends BlockPortal {
		public BlockCustomPortal() {
			setHardness(-1.0F);
			setUnlocalizedName("mine_portal");
			setRegistryName("mine_portal");
			setLightLevel(0.666666666667F);
		}

		@Override
		public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		}

		public void portalSpawn(World worldIn, BlockPos pos) {
			BlockCustomPortal.Size portalsize = new BlockCustomPortal.Size(worldIn, pos, EnumFacing.Axis.X);
			if (portalsize.isValid() && portalsize.portalBlockCount == 0) {
				portalsize.placePortalBlocks();
			} else {
				portalsize = new BlockCustomPortal.Size(worldIn, pos, EnumFacing.Axis.Z);
				if (portalsize.isValid() && portalsize.portalBlockCount == 0)
					portalsize.placePortalBlocks();
			}
		}

		@Override
		public BlockPattern.PatternHelper createPatternHelper(World worldIn, BlockPos p_181089_2_) {
			EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Z;
			BlockCustomPortal.Size blockportal$size = new BlockCustomPortal.Size(worldIn, p_181089_2_, EnumFacing.Axis.X);
			LoadingCache<BlockPos, BlockWorldState> loadingcache = BlockPattern.createLoadingCache(worldIn, true);
			if (!blockportal$size.isValid()) {
				enumfacing$axis = EnumFacing.Axis.X;
				blockportal$size = new BlockCustomPortal.Size(worldIn, p_181089_2_, EnumFacing.Axis.Z);
			}
			if (!blockportal$size.isValid()) {
				return new BlockPattern.PatternHelper(p_181089_2_, EnumFacing.NORTH, EnumFacing.UP, loadingcache, 1, 1, 1);
			} else {
				int[] aint = new int[EnumFacing.AxisDirection.values().length];
				EnumFacing enumfacing = blockportal$size.rightDir.rotateYCCW();
				BlockPos blockpos = blockportal$size.bottomLeft.up(blockportal$size.getHeight() - 1);
				for (EnumFacing.AxisDirection enumfacing$axisdirection : EnumFacing.AxisDirection.values()) {
					BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper(
							enumfacing.getAxisDirection() == enumfacing$axisdirection
									? blockpos
									: blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1),
							EnumFacing.getFacingFromAxis(enumfacing$axisdirection, enumfacing$axis), EnumFacing.UP, loadingcache,
							blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
					for (int i = 0; i < blockportal$size.getWidth(); ++i) {
						for (int j = 0; j < blockportal$size.getHeight(); ++j) {
							BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, j, 1);
							if (blockworldstate.getBlockState() != null && blockworldstate.getBlockState().getMaterial() != Material.AIR) {
								++aint[enumfacing$axisdirection.ordinal()];
							}
						}
					}
				}
				EnumFacing.AxisDirection enumfacing$axisdirection1 = EnumFacing.AxisDirection.POSITIVE;
				for (EnumFacing.AxisDirection enumfacing$axisdirection2 : EnumFacing.AxisDirection.values()) {
					if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()]) {
						enumfacing$axisdirection1 = enumfacing$axisdirection2;
					}
				}
				return new BlockPattern.PatternHelper(
						enumfacing.getAxisDirection() == enumfacing$axisdirection1
								? blockpos
								: blockpos.offset(blockportal$size.rightDir, blockportal$size.getWidth() - 1),
						EnumFacing.getFacingFromAxis(enumfacing$axisdirection1, enumfacing$axis), EnumFacing.UP, loadingcache,
						blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
			}
		}

		@Override
		public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
			EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis) state.getValue(AXIS);
			if (enumfacing$axis == EnumFacing.Axis.X) {
				BlockCustomPortal.Size blockportal$size = new BlockCustomPortal.Size(worldIn, pos, EnumFacing.Axis.X);
				if (!blockportal$size.isValid() || blockportal$size.portalBlockCount < blockportal$size.width * blockportal$size.height) {
					worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			} else if (enumfacing$axis == EnumFacing.Axis.Z) {
				BlockCustomPortal.Size blockportal$size1 = new BlockCustomPortal.Size(worldIn, pos, EnumFacing.Axis.Z);
				if (!blockportal$size1.isValid() || blockportal$size1.portalBlockCount < blockportal$size1.width * blockportal$size1.height) {
					worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			}
		}

		@SideOnly(Side.CLIENT)
		@Override
		public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
			for (int i = 0; i < 4; i++) {
				double px = pos.getX() + random.nextFloat();
				double py = pos.getY() + random.nextFloat();
				double pz = pos.getZ() + random.nextFloat();
				double vx = (random.nextFloat() - 0.5) / 2f;
				double vy = (random.nextFloat() - 0.5) / 2f;
				double vz = (random.nextFloat() - 0.5) / 2f;
				int j = random.nextInt(4) - 1;
				if (world.getBlockState(pos.west()).getBlock() != this && world.getBlockState(pos.east()).getBlock() != this) {
					px = pos.getX() + 0.5 + 0.25 * j;
					vx = random.nextFloat() * 2 * j;
				} else {
					pz = pos.getZ() + 0.5 + 0.25 * j;
					vz = random.nextFloat() * 2 * j;
				}
			}
		}

		public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
			if (!worldIn.isRemote && !entityIn.isRiding() && !entityIn.isBeingRidden() && entityIn instanceof EntityPlayerMP) {
				EntityPlayerMP thePlayer = (EntityPlayerMP) entityIn;
				if (thePlayer.timeUntilPortal > 0) {
					thePlayer.timeUntilPortal = 10;
				} else if (thePlayer.dimension != DIMID) {
					thePlayer.timeUntilPortal = 10;
					// Memorise le portail d'origine (position exacte) pour pouvoir y ressortir au retour.
					NBTTagCompound returnPos = new NBTTagCompound();
					returnPos.setInteger("x", pos.getX());
					returnPos.setInteger("y", pos.getY());
					returnPos.setInteger("z", pos.getZ());
					thePlayer.getEntityData().setTag(TeleporterDimensionMod.MINE_RETURN_POS_KEY, returnPos);
					ReflectionHelper.setPrivateValue(EntityPlayerMP.class, thePlayer, true, "invulnerableDimensionChange", "field_184851_cj");
					thePlayer.mcServer.getPlayerList().transferPlayerToDimension(thePlayer, DIMID, getTeleporterForDimension(thePlayer, pos, DIMID));
				} else {
					thePlayer.timeUntilPortal = 10;
					ReflectionHelper.setPrivateValue(EntityPlayerMP.class, thePlayer, true, "invulnerableDimensionChange", "field_184851_cj");
					thePlayer.mcServer.getPlayerList().transferPlayerToDimension(thePlayer, 0, getTeleporterForDimension(thePlayer, pos, 0));
				}
			}
		}

		private TeleporterDimensionMod getTeleporterForDimension(Entity entity, BlockPos pos, int dimid) {
			BlockPattern.PatternHelper bph = portal.createPatternHelper(entity.world, new BlockPos(pos));
			double d0 = bph.getForwards().getAxis() == EnumFacing.Axis.X
					? (double) bph.getFrontTopLeft().getZ()
					: (double) bph.getFrontTopLeft().getX();
			double d1 = bph.getForwards().getAxis() == EnumFacing.Axis.X ? entity.posZ : entity.posX;
			d1 = Math.abs(MathHelper.pct(d1 - (double) (bph.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0),
					d0, d0 - (double) bph.getWidth()));
			double d2 = MathHelper.pct(entity.posY - 1, (double) bph.getFrontTopLeft().getY(),
					(double) (bph.getFrontTopLeft().getY() - bph.getHeight()));
			return new TeleporterDimensionMod(entity.getServer().getWorld(dimid), new Vec3d(d1, d2, 0), bph.getForwards());
		}
		public static class Size {
			private final World world;
			private final EnumFacing.Axis axis;
			private final EnumFacing rightDir;
			private final EnumFacing leftDir;
			private int portalBlockCount;
			private BlockPos bottomLeft;
			private int height;
			private int width;
			public Size(World worldIn, BlockPos p_i45694_2_, EnumFacing.Axis p_i45694_3_) {
				this.world = worldIn;
				this.axis = p_i45694_3_;
				if (p_i45694_3_ == EnumFacing.Axis.X) {
					this.leftDir = EnumFacing.EAST;
					this.rightDir = EnumFacing.WEST;
				} else {
					this.leftDir = EnumFacing.NORTH;
					this.rightDir = EnumFacing.SOUTH;
				}
				for (BlockPos blockpos = p_i45694_2_; p_i45694_2_.getY() > blockpos.getY() - 21 && p_i45694_2_.getY() > 0
						&& this.isEmptyBlock(worldIn.getBlockState(p_i45694_2_.down()).getBlock()); p_i45694_2_ = p_i45694_2_.down()) {
					;
				}
				int i = this.getDistanceUntilEdge(p_i45694_2_, this.leftDir) - 1;
				if (i >= 0) {
					this.bottomLeft = p_i45694_2_.offset(this.leftDir, i);
					this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
					if (this.width < 2 || this.width > 21) {
						this.bottomLeft = null;
						this.width = 0;
					}
				}
				if (this.bottomLeft != null) {
					this.height = this.calculatePortalHeight();
				}
			}

			protected int getDistanceUntilEdge(BlockPos p_180120_1_, EnumFacing p_180120_2_) {
				int i;
				for (i = 0; i < 22; ++i) {
					BlockPos blockpos = p_180120_1_.offset(p_180120_2_, i);
					if (!this.isEmptyBlock(this.world.getBlockState(blockpos).getBlock())
							|| this.world.getBlockState(blockpos.down()).getBlock() != Blocks.IRON_BLOCK.getDefaultState().getBlock()) {
						break;
					}
				}
				Block block = this.world.getBlockState(p_180120_1_.offset(p_180120_2_, i)).getBlock();
				return block == Blocks.IRON_BLOCK.getDefaultState().getBlock() ? i : 0;
			}

			public int getHeight() {
				return this.height;
			}

			public int getWidth() {
				return this.width;
			}

			protected int calculatePortalHeight() {
				label56 : for (this.height = 0; this.height < 21; ++this.height) {
					for (int i = 0; i < this.width; ++i) {
						BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i).up(this.height);
						Block block = this.world.getBlockState(blockpos).getBlock();
						if (!this.isEmptyBlock(block)) {
							break label56;
						}
						if (block == portal) {
							++this.portalBlockCount;
						}
						if (i == 0) {
							block = this.world.getBlockState(blockpos.offset(this.leftDir)).getBlock();
							if (block != Blocks.IRON_BLOCK.getDefaultState().getBlock()) {
								break label56;
							}
						} else if (i == this.width - 1) {
							block = this.world.getBlockState(blockpos.offset(this.rightDir)).getBlock();
							if (block != Blocks.IRON_BLOCK.getDefaultState().getBlock()) {
								break label56;
							}
						}
					}
				}
				for (int j = 0; j < this.width; ++j) {
					if (this.world.getBlockState(this.bottomLeft.offset(this.rightDir, j).up(this.height)).getBlock() != Blocks.IRON_BLOCK
							.getDefaultState().getBlock()) {
						this.height = 0;
						break;
					}
				}
				if (this.height <= 21 && this.height >= 3) {
					return this.height;
				} else {
					this.bottomLeft = null;
					this.width = 0;
					this.height = 0;
					return 0;
				}
			}

			protected boolean isEmptyBlock(Block blockIn) {
				return blockIn.getDefaultState().getMaterial() == Material.AIR || blockIn == Blocks.FIRE || blockIn == portal;
			}

			public boolean isValid() {
				return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
			}

			public void placePortalBlocks() {
				for (int i = 0; i < this.width; ++i) {
					BlockPos blockpos = this.bottomLeft.offset(this.rightDir, i);
					for (int j = 0; j < this.height; ++j) {
						this.world.setBlockState(blockpos.up(j), portal.getDefaultState().withProperty(BlockPortal.AXIS, this.axis), 2);
					}
				}
			}
		}
	}

	// Generateur de terrain "void" : chaque chunk est totalement vide (que de l'air), sans grotte,
	// sans structure, sans decoration de biome. Le seul biome utilise est fixe (Biomes.PLAINS) pour
	// avoir un ciel/climat toujours degage (herite du comportement de WorldProviderMod).
	public static class ChunkProviderVoid implements IChunkGenerator {
		private final World world;

		public ChunkProviderVoid(World world) {
			this.world = world;
		}

		@Override
		public Chunk generateChunk(int x, int z) {
			ChunkPrimer chunkprimer = new ChunkPrimer();
			Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
			byte[] abyte = chunk.getBiomeArray();
			java.util.Arrays.fill(abyte, (byte) Biome.getIdForBiome(Biomes.PLAINS));
			chunk.generateSkylightMap();
			return chunk;
		}

		@Override
		public void populate(int x, int z) {
			// Rien a generer : monde void.
		}

		@Override
		public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
			return Collections.emptyList();
		}

		@Override
		public void recreateStructures(Chunk chunkIn, int x, int z) {
		}

		@Override
		public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
			return false;
		}

		@Override
		public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
			return null;
		}

		@Override
		public boolean generateStructures(Chunk chunkIn, int x, int z) {
			return false;
		}
	}
}
