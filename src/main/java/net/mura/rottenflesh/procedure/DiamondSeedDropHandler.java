package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.item.ItemDiamondSeeds;
import net.mura.rottenflesh.item.ItemOrangesword;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@ElementsRottenfleshMod.ModElement.Tag
public class DiamondSeedDropHandler extends ElementsRottenfleshMod.ModElement {

	private static final Random RANDOM = new Random();
	private static final int CHANCE_DENOMINATOR = 50; // 1 chance sur 50

	public DiamondSeedDropHandler(ElementsRottenfleshMod instance) {
		super(instance, 76);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		EntityLivingBase deadEntity = event.getEntityLiving();
		if (deadEntity.world.isRemote) {
			return;
		}

		DamageSource source = event.getSource();
		Entity trueSource = source.getTrueSource();
		if (!(trueSource instanceof EntityPlayer)) {
			return; // seuls les mobs tues par un joueur sont concernes
		}

		EntityPlayer player = (EntityPlayer) trueSource;
		ItemStack heldItem = player.getHeldItemMainhand();

		// Reference vers l'item genere par ItemOrangesword.java
		if (heldItem.isEmpty() || heldItem.getItem() != ItemOrangesword.block) {
			return;
		}

		if (RANDOM.nextInt(CHANCE_DENOMINATOR) != 0) {
			return; // pas de chance cette fois (49 chances sur 50)
		}

		ItemStack seedDrop = new ItemStack(ItemDiamondSeeds.block);
		EntityItem drop = new EntityItem(deadEntity.world, deadEntity.posX, deadEntity.posY, deadEntity.posZ, seedDrop);
		deadEntity.world.spawnEntity(drop);
	}
}