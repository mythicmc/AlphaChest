package info.mythicmc.alphachest.listeners;

import info.mythicmc.alphachest.AlphaChest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public class InventoryListener implements Listener {

    private final AlphaChest plugin;

    public InventoryListener(AlphaChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith(AlphaChest.CHEST_PREFIX)) {
            Inventory inventory = event.getInventory();
            if (inventory.getHolder() == null && inventory.getViewers().size() <= 1) {
                String playerName = title.substring(AlphaChest.CHEST_PREFIX.length());
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    File folder = plugin.getChestsFolder();
                    if (folder == null)
                        folder = plugin.createChestsFolder();
                    File target = new File(folder, playerName + ".chest.yml");
                    YamlConfiguration result = new YamlConfiguration();
                    try {
                        ArrayList<String> items = new ArrayList<>();
                        for (ItemStack item : inventory.getContents()) {
                            if (item != null) {
                                items.add(Base64.getEncoder().encodeToString(item.serializeAsBytes()));
                            } else items.add(null);
                        }
                        result.set("items", items);
                    } catch (NoSuchMethodError e) {
                        plugin.getLogger().warning("Looks like this isn't Paper 1.15+, falling back" +
                                " to YamlConfiguration. It is recommended to use Paper 1.15+ for /chests" +
                                " to upgrade across versions correctly.");
                        result.set("items", inventory.getContents());
                    }
                    try {
                        result.save(target);
                    } catch (IOException e) {
                        plugin.getLogger().severe("Could not save the old inventory of " + playerName +
                                ", there is a risk that it could be duplicated!");
                    }
                });
                Bukkit.getScheduler().runTask(plugin, () -> plugin.removeChest(playerName));
            }
        }
    }
}
