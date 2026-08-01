package net.mura.rottenflesh.procedure;

import net.mura.rottenflesh.ElementsRottenfleshMod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Advancement;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

@ElementsRottenfleshMod.ModElement.Tag
public class ProcedureDiamondbowcraftadv extends ElementsRottenfleshMod.ModElement {
	private static final ResourceLocation DIAMONDBOW = new ResourceLocation("rottenflesh:diamondbow");
	private static final int DELAY_TICKS = 20; // 1 seconde

	// Compte a rebours par joueur, avant d'accorder le critere de l'advancement.
	private static final Map<EntityPlayerMP, Integer> pendingGrants = new HashMap<>();

	public ProcedureDiamondbowcraftadv(ElementsRottenfleshMod instance) {
		super(instance, 47);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure Diamondbowcraftadv!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		ItemStack itemStack = (ItemStack) dependencies.get("itemstack");
		if (itemStack == null || itemStack.isEmpty() || !DIAMONDBOW.equals(itemStack.getItem().getRegistryName())) {
			return;
		}
		if (entity instanceof EntityPlayerMP) {
			pendingGrants.put((EntityPlayerMP) entity, DELAY_TICKS);
		}
	}

	private static void grantAdvancement(EntityPlayerMP player) {
		MinecraftServer server = (MinecraftServer) player.mcServer;
		// Le joueur a pu se deconnecter pendant le delai d'1 seconde.
		if (server.getPlayerList().getPlayerByUUID(player.getUniqueID()) != player) {
			return;
		}
		Advancement _adv = server.getAdvancementManager().getAdvancement(new ResourceLocation("rottenflesh:diamondbowadv"));
		AdvancementProgress _ap = player.getAdvancements().getProgress(_adv);
		if (!_ap.isDone()) {
			Iterator _iterator = _ap.getRemaningCriteria().iterator();
			while (_iterator.hasNext()) {
				String _criterion = (String) _iterator.next();
				player.getAdvancements().grantCriterion(_adv, _criterion);
			}
		}
	}

	@SubscribeEvent
	public void onItemCrafted(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
		Entity entity = event.player;
		ItemStack itemStack = event.crafting;
		java.util.HashMap<String, Object> dependencies = new java.util.HashMap<>();
		dependencies.put("entity", entity);
		dependencies.put("itemstack", itemStack);
		dependencies.put("event", event);
		this.executeProcedure(dependencies);
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || pendingGrants.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<EntityPlayerMP, Integer>> iterator = pendingGrants.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<EntityPlayerMP, Integer> entry = iterator.next();
			int remaining = entry.getValue() - 1;
			if (remaining <= 0) {
				grantAdvancement(entry.getKey());
				iterator.remove();
			} else {
				entry.setValue(remaining);
			}
		}
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
