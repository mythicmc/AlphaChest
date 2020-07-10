package info.mythicmc.alphachest.commands;

import info.mythicmc.alphachest.AlphaChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(AlphaChest.ERROR_PREFIX + "This command can only be executed by players.");
            return true;
        }
        Player player = (Player) sender;
        player.openWorkbench(null, true);
        return true;
    }
}
