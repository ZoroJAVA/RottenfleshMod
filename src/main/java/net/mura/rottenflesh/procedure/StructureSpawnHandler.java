package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.entity.Herobrine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure : plateforme 3x3 de blocs d'or, avec du netherrack au centre
 * juste au-dessus, entoure de 4 torches de redstone (nord/sud/est/ouest).
 *
 * Declenchement : uniquement quand le joueur allume le netherrack au briquet
 * (flint and steel), comme pour un portail du Nether. Tant que le feu n'est
 * pas allume, poser/retirer les blocs de la structure ne fait rien.
 *
 * La structure reste en place une fois declenchee (elle n'est plus effacee).
 * Si un seul de ses 14 blocs est casse, l'entite invoquee meurt instantanement.
 *
 * Limitation : le lien structure -> entite est garde en memoire (pas persiste en NBT), il est
 * donc perdu si le serveur redemarre pendant qu'un mob est encore vivant (casser un bloc de la
 * structure apres un redemarrage ne le tuera plus).
 */
@ElementsRottenfleshMod.ModElement.Tag
public class StructureSpawnHandler extends ElementsRottenfleshMod.ModElement {

    public StructureSpawnHandler(ElementsRottenfleshMod instance) {
        super(instance, 80);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Associe chacun des 14 blocs d'une structure declenchee a l'instance qui la decrit.
    private static final Map<BlockPos, StructureInstance> activeStructures = new HashMap<>();

    private static final class StructureInstance {
        final Entity entity;
        final List<BlockPos> blocks;

        StructureInstance(Entity entity, List<BlockPos> blocks) {
            this.entity = entity;
            this.blocks = blocks;
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        ItemStack heldItem = event.getItemStack();
        if (heldItem.isEmpty() || heldItem.getItem() != Items.FLINT_AND_STEEL) {
            return;
        }

        BlockPos pos = event.getPos();
        if (world.getBlockState(pos).getBlock() != Blocks.NETHERRACK) {
            return;
        }

        // Le centre de la plateforme d'or est juste en dessous du netherrack allume.
        if (tryTriggerFromCenter(world, pos.down())) {
            // Structure declenchee : on empeche le feu vanilla de se poser, le mob vient de spawn.
            event.setCanceled(true);
            event.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        StructureInstance instance = activeStructures.get(event.getPos());
        if (instance == null) {
            return;
        }

        if (!instance.entity.isDead) {
            instance.entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        }
        for (BlockPos p : instance.blocks) {
            activeStructures.remove(p);
        }
    }

    /**
     * centerPos = position du bloc d'or CENTRAL de la plateforme 3x3.
     * Renvoie true si la structure etait complete et vient d'etre declenchee.
     */
    private boolean tryTriggerFromCenter(World world, BlockPos centerPos) {
        if (world.getBlockState(centerPos).getBlock() != Blocks.GOLD_BLOCK) {
            return false;
        }

        // Verifie les 9 blocs d'or de la plateforme 3x3
        List<BlockPos> goldBlocks = new ArrayList<>(9);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = centerPos.add(dx, 0, dz);
                if (world.getBlockState(p).getBlock() != Blocks.GOLD_BLOCK) {
                    return false; // plateforme incomplete
                }
                goldBlocks.add(p);
            }
        }

        BlockPos netherrackPos = centerPos.up();
        if (world.getBlockState(netherrackPos).getBlock() != Blocks.NETHERRACK) {
            return false;
        }

        // Deja declenchee et le mob associe est toujours vivant : pas de re-declenchement.
        StructureInstance existing = activeStructures.get(netherrackPos);
        if (existing != null && !existing.entity.isDead) {
            return false;
        }

        // Verifie les 4 torches de redstone autour du netherrack
        List<BlockPos> torchBlocks = new ArrayList<>(4);
        for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
            BlockPos torchPos = netherrackPos.offset(dir);
            if (world.getBlockState(torchPos).getBlock() != Blocks.REDSTONE_TORCH) {
                return false; // torche manquante
            }
            torchBlocks.add(torchPos);
        }

        // Structure complete et pas deja active : la flamme s'allume, un eclair tombe sur le
        // netherrack (effet dramatique du rituel), puis le mob apparait a 15 blocs de la
        // structure, dans une direction aleatoire, a un endroit degage (pas dans un mur/sol).
        // La structure elle-meme reste en place.
        world.addWeatherEffect(
                new EntityLightningBolt(world, netherrackPos.getX() + 0.5D, netherrackPos.getY(), netherrackPos.getZ() + 0.5D, false));

        BlockPos spawnPos = findSpawnPosition(world, netherrackPos);

        Herobrine.EntityCustom mob = new Herobrine.EntityCustom(world);
        mob.setPosition(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
        world.spawnEntity(mob);

        List<BlockPos> allBlocks = new ArrayList<>(goldBlocks.size() + 1 + torchBlocks.size());
        allBlocks.addAll(goldBlocks);
        allBlocks.add(netherrackPos);
        allBlocks.addAll(torchBlocks);
        StructureInstance instance = new StructureInstance(mob, allBlocks);
        for (BlockPos p : allBlocks) {
            activeStructures.put(p, instance);
        }
        return true;
    }

    private static final int SPAWN_DISTANCE = 15;
    private static final int SPAWN_ATTEMPTS = 20;
    private static final int VERTICAL_SEARCH_RANGE = 16;

    /**
     * Cherche un point degage a SPAWN_DISTANCE blocs de origin, dans une direction aleatoire.
     * Retente plusieurs fois avec un nouvel angle si l'emplacement tire est dans un mur/le vide.
     * Si rien de valide n'est trouve, repli sur origin (garanti valide : le joueur s'y tient).
     */
    private BlockPos findSpawnPosition(World world, BlockPos origin) {
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double angle = world.rand.nextDouble() * Math.PI * 2;
            int dx = (int) Math.round(Math.cos(angle) * SPAWN_DISTANCE);
            int dz = (int) Math.round(Math.sin(angle) * SPAWN_DISTANCE);
            BlockPos candidate = findSafeGround(world, origin.getX() + dx, origin.getY(), origin.getZ() + dz);
            if (candidate != null) {
                return candidate;
            }
        }
        return origin;
    }

    /**
     * Cherche, autour de startY (a la meme colonne x/z), une hauteur ou les pieds et la tete sont
     * degages et ou le sol est solide. Explore alternativement au-dessus et en dessous de startY.
     */
    private BlockPos findSafeGround(World world, int x, int startY, int z) {
        for (int dy = 0; dy <= VERTICAL_SEARCH_RANGE; dy++) {
            for (int sign = 1; sign >= -1; sign -= 2) {
                if (dy == 0 && sign == -1) {
                    continue; // evite de tester startY deux fois
                }
                int y = startY + dy * sign;
                if (y < 1 || y > 253) {
                    continue;
                }
                BlockPos feet = new BlockPos(x, y, z);
                BlockPos head = feet.up();
                BlockPos ground = feet.down();
                boolean feetClear = !world.getBlockState(feet).getMaterial().isSolid();
                boolean headClear = !world.getBlockState(head).getMaterial().isSolid();
                boolean groundSolid = world.getBlockState(ground).getMaterial().isSolid();
                if (feetClear && headClear && groundSolid) {
                    return feet;
                }
            }
        }
        return null;
    }
}
