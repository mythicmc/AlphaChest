package info.mythicmc.alphachest;

import info.mythicmc.alphachest.commands.ChestCommand;
import info.mythicmc.alphachest.commands.CraftCommand;
import info.mythicmc.alphachest.listeners.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AlphaChest extends JavaPlugin {

    public static final String CHEST_PREFIX = "AlphaChest - ";
    public static final String SUCCESS_PREFIX = ChatColor.translateAlternateColorCodes('&',
            "&8[&aAlphaChest&8]&a");
    public static final String ERROR_PREFIX = ChatColor.translateAlternateColorCodes('&',
            "&8[&aAlphaChest&8]&c");
    public static final String CHESTS_FOLDER = "chests";

    private HashMap<String, Inventory> chestMap;

    @Override
    public void onEnable() {
        File folder = new File(getDataFolder(), CHESTS_FOLDER);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                getLogger().severe("Could not find or create chests folder.");
                return;
            }
        }
        chestMap = new HashMap<>();
        Objects.requireNonNull(getCommand("chest")).setExecutor(new ChestCommand(this));
        Objects.requireNonNull(getCommand("craft")).setExecutor(new CraftCommand());
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }

    public File createChestsFolder() {
        File folder = new File(getDataFolder(), CHESTS_FOLDER);
        if (!folder.mkdirs()) {
            throw new IllegalStateException("Could not find or create chests folder.");
        }
        return folder;
    }

    public File getChestsFolder() {
        File folder = new File(getDataFolder(), CHESTS_FOLDER);
        if (!folder.exists()) {
            return null;
        }
        return folder;
    }

    public Inventory getChest(String name) {
        return chestMap.get(name);
    }

    public void putChest(String name, Inventory inventory) {
        chestMap.put(name, inventory);
    }

    public List<?> getChestContents(String fileName) {
        File folder = getChestsFolder();
        if (folder == null) {
            Bukkit.getScheduler().runTaskAsynchronously(this, this::createChestsFolder);
            return null;
        }
        File file = new File(folder, fileName);
        if (!file.exists())
            return null;
        YamlConfiguration source = YamlConfiguration.loadConfiguration(file);
        if (source.getInt("size") != 0) {
            ConfigurationSection items = source.getConfigurationSection("items");
            if (items == null)
                return null;
            List<ItemStack> result = new ArrayList<>();
            for (int slot = 0; slot < 54; slot++) {
                String slotString = Integer.toString(slot);
                if (items.isItemStack(slotString)) {
                    ItemStack itemStack = items.getItemStack(slotString);
                    result.add(itemStack);
                } else
                    result.add(null);
            }
            return result;
        } else
            return source.getList("items");
    }
}
