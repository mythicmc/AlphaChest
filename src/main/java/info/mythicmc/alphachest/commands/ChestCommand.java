package info.mythicmc.alphachest.commands;

import info.mythicmc.alphachest.AlphaChest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Base64;
import java.util.List;

public class ChestCommand implements CommandExecutor {

    private final AlphaChest plugin;

    public ChestCommand(AlphaChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isConsole = !(sender instanceof Player);
        if (args.length == 0) {
            if (isConsole) {
                sender.sendMessage(AlphaChest.ERROR_PREFIX + "This command can only be executed by players.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("alphachest.chest")) {
                player.sendMessage(AlphaChest.ERROR_PREFIX + "You don't have permission to execute this command.");
                return true;
            }
            openChest(player, player.getName());
        } else if (args.length == 1) {
            if (isConsole) {
                sender.sendMessage(AlphaChest.ERROR_PREFIX + "This command can only be executed by players.");
                return true;
            }
            Player player = (Player) sender;
            String target = args[0];
            if (target.equalsIgnoreCase(player.getName()) && !player.hasPermission("alphachest.chest")) {
                player.sendMessage(AlphaChest.ERROR_PREFIX + "You don't have permission to execute this command.");
                return true;
            } else if (!player.hasPermission("alphachest.admin")) {
                player.sendMessage(AlphaChest.ERROR_PREFIX + "You don't have permission to open another players' chest.");
                return true;
            } else
                openChest(player, target);
        } else if (args.length == 2) {
            String clearText = args[0];
            if (!clearText.equalsIgnoreCase("clear")) {
                sender.sendMessage(AlphaChest.ERROR_PREFIX + "Invalid usage. Correct format: /chest clear <player name>");
                return true;
            } else if (!sender.hasPermission("alphachest.clearchest") && !sender.hasPermission("alphachest.admin")) {
                sender.sendMessage(AlphaChest.ERROR_PREFIX + "You don't have permission to clear chests.");
                return true;
            } else {
                String target = args[1];
                if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("alphachest.admin")) {
                    sender.sendMessage(AlphaChest.ERROR_PREFIX + "You don't have permission to clear other players' chests.");
                    return true;
                }
                if (clearChest(target)) {
                    plugin.getLogger().info("Player " + sender.getName() + " cleared the /chest of " + target + ".");
                    sender.sendMessage(AlphaChest.SUCCESS_PREFIX + "Cleared the /chest of " + target + ".");
                } else
                    sender.sendMessage(AlphaChest.ERROR_PREFIX + "Could not clear the /chest of " + target + ".");
            }
        }
        return true;
    }

    private void openChest(Player player, String target) {
        String finalTarget = target.toLowerCase();
        Inventory inventory = plugin.getChest(finalTarget);
        if (inventory != null) {
            player.openInventory(inventory);
        } else {
            Inventory emptyInventory = Bukkit.createInventory(null, 54,
                    AlphaChest.CHEST_PREFIX + finalTarget);
            plugin.putChest(finalTarget, emptyInventory);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<?> inventoryContents = plugin.getChestContents(finalTarget + ".chest.yml");
                Bukkit.getScheduler().runTask(plugin, () -> showInventory(emptyInventory, inventoryContents, player, finalTarget));
            });
        }
    }

    private void showInventory(Inventory inventory, List<?> inventoryContents, Player player, String name) {
        if (inventoryContents != null) {
            for (int i = 0; i < inventoryContents.size(); i++) {
                if (inventoryContents.get(i) != null) {
                    if (inventoryContents.get(i) instanceof ItemStack) {
                        inventory.setItem(i, (ItemStack) inventoryContents.get(i));
                    } else if (inventoryContents.get(i) instanceof String) {
                        try {
                            byte[] base64Decoded = Base64.getDecoder().decode((String) inventoryContents.get(i));
                            inventory.setItem(i, ItemStack.deserializeBytes(base64Decoded));
                        } catch (NoSuchMethodError e) {
                            plugin.getLogger().severe("Failed to deserialize item with Paper API! Not opening chest for safety.");
                            player.sendMessage("This chest is incompatible with this server software! Not opening chest for safety.");
                            plugin.removeChest(name);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage("Slot " + i + " in the inventory of " + name + " is invalid!");
                        }
                    } else player.sendMessage("Slot " + i + " in the inventory of " + name + " is invalid!");
                }
            }
        }
        player.openInventory(inventory);
    }

    private boolean clearChest(String target) {
        target = target.toLowerCase();
        Inventory inventory = plugin.getChest(target);
        if (inventory != null) {
            inventory.clear();
        } else {
            File folder = plugin.getChestsFolder();
            if (folder == null)
                return true;
            File file = new File(folder, target + ".chest.yml");
            if (file.exists())
                return file.delete();
        }
        return true;
    }
}
