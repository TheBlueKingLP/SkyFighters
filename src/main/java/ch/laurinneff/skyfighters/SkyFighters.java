package ch.laurinneff.skyfighters;

import ch.laurinneff.skyfighters.commands.SkyFightersCommand;
import ch.laurinneff.skyfighters.listeners.Listeners;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SkyFighters extends JavaPlugin {
    public static SkyFighters instance;
    public List<Ring> rings;
    public List<Weapon> weapons;
    public static boolean enabled = true;

    static {
        ConfigurationSerialization.registerClass(Ring.class, "Ring");
        ConfigurationSerialization.registerClass(Weapon.class, "Weapon");
    }

    public void onEnable() {
        instance = this;
        PaperLib.suggestPaper(this);
        PluginCommand skyFightersCommand = getCommand("skyfighters");
        assert skyFightersCommand != null;
        skyFightersCommand.setExecutor(new SkyFightersCommand());
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Listeners(), this);
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false));
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_WEATHER_CYCLE, false));
        FileConfiguration config = this.getConfig();

        // Config Defaults
        config.addDefault("rings", new Ring[]{new Ring("boost", 0, 0, 0, 0, 0, 0)});
        config.addDefault("weapons", new Weapon[]{new Weapon("Fireball", new ItemStack(Material.FIRE_CHARGE), "fireball", 3, 3, 1)});
        config.addDefault("spawnArea.x1", 0);
        config.addDefault("spawnArea.y1", 0);
        config.addDefault("spawnArea.z1", 0);
        config.addDefault("spawnArea.x2", 0);
        config.addDefault("spawnArea.y2", 0);
        config.addDefault("spawnArea.z2", 0);
        config.addDefault("spawnPoint.x", 0);
        config.addDefault("spawnPoint.y", 0);
        config.addDefault("spawnPoint.z", 0);
        config.addDefault("spawnPoint.yaw", 0);
        config.addDefault("spawnPoint.pitch", 0);

        config.options().copyDefaults(true);
        saveConfig();

        rings = config.getObject("rings", List.class);
        weapons = config.getObject("weapons", List.class);
    }

    public void onDisable() {
    }
}
