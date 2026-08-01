package net.mura.rottenflesh.commande;

import net.minecraft.util.text.TextFormatting;
import net.mura.rottenflesh.ElementsRottenfleshMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@ElementsRottenfleshMod.ModElement.Tag
public class CommandHandler extends ElementsRottenfleshMod.ModElement {

	public CommandHandler(ElementsRottenfleshMod instance) {
		super(instance, 78);
	}

	// Le vrai point d'entree fourni par MCreator pour le demarrage du serveur
	// (verifie directement dans ElementsRottenfleshMod.ModElement) : pas
	// "serverStarting", pas d'enregistrement manuel sur un bus, juste ca.
	@Override
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandHeal());
	}

	// /heal remet la vie du joueur qui l'execute a son maximum
	public static class CommandHeal extends CommandBase {

		@Override
		public String getName() {
			return "heal";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "/heal";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 0;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!(sender instanceof EntityPlayer)) {
				sender.sendMessage(new TextComponentString("Cette commande doit etre executee par un joueur."));
				return;
			}

			EntityPlayer player = (EntityPlayer) sender;
			player.setHealth(player.getMaxHealth());
			sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Healed !"));
		}
	}
}