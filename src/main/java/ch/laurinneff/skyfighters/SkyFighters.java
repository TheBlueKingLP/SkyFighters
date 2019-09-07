package ch.laurinneff.skyfighters;

import ch.laurinneff.skyfighters.commands.SkyFightersCommand;
import ch.laurinneff.skyfighters.listeners.Listeners;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SkyFighters extends JavaPlugin {
    public static SkyFighters instance;
    public List<Ring> rings = new ArrayList<Ring>();

    static {
        ConfigurationSerialization.registerClass(Ring.class, "Ring");
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

        config.addDefault("rings", new Ring[]{new Ring("boost", 0, 0, 0, 0, 0, 0)});

        config.options().copyDefaults(true);
        saveConfig();

        rings = config.getObject("rings", List.class);
    }

    public void onDisable() {
    }
}
