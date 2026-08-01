package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.mura.rottenflesh.item.ItemOrangeboots;

import net.mura.rottenflesh.item.ItemOrangeboots;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsRottenfleshMod.ModElement.Tag
public class DoubleJumpHandler extends ElementsRottenfleshMod.ModElement {

	// true = le joueur peut encore faire son double saut pour ce vol en cours
	private static boolean canDoubleJump = true;
	// true = on vient tout juste de quitter le sol (on ignore ce tick pour ne pas
	// confondre la pression du PREMIER saut avec une tentative de double saut)
	private static boolean justLeftGround = false;

	public DoubleJumpHandler(ElementsRottenfleshMod instance) {
		super(instance, 50);
		// S'enregistre lui-meme sur le bus d'evenements Forge (pas besoin
		// de passer par l'editeur visuel de procedures pour ce genre de logique)
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		// ClientTickEvent se declenche 2 fois par tick (debut/fin), on ne garde qu'une des deux
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		if (player == null) {
			return;
		}

		// Des qu'on retouche le sol, on peut refaire un double saut la prochaine fois
		if (player.onGround) {
			canDoubleJump = true;
			justLeftGround = false;
			return;
		}

		// Reference vers les bottes generees par ItemOrangeboots.java
		ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		if (boots.isEmpty() || boots.getItem() != ItemOrangeboots.boots) {
			return;
		}

		// Premier tick ou le joueur est detecte "en l'air" : c'est le decollage
		// normal du premier saut. Minecraft nous a deja fait quitter le sol CE
		// MEME tick que la pression de la touche, donc si on ne fait rien de
		// special, cette meme pression serait a tort comptee comme le double
		// saut. On "consomme" volontairement l'appui en trop ici sans agir,
		// pour que seul un VRAI second appui, en l'air, declenche le boost.
		if (!justLeftGround) {
			justLeftGround = true;
			mc.gameSettings.keyBindJump.isPressed(); // vide la file, resultat ignore
			return;
		}

		// isPressed() detecte une VRAIE nouvelle pression de la touche (pas maintenue en continu)
		if (canDoubleJump && mc.gameSettings.keyBindJump.isPressed()) {
			player.motionY = 0.6D; // hauteur du second saut, ajustable
			canDoubleJump = false;
		}
	}
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		EntityLivingBase entity = event.getEntityLiving();

		ItemStack boots = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		if (boots.isEmpty() || boots.getItem() != ItemOrangeboots.boots) {
			return;
		}

		event.setCanceled(true);
	}
}
