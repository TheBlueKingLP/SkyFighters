package ch.laurinneff.skyfighters;

import ch.laurinneff.skyfighters.commands.SkyFightersCommand;
import ch.laurinneff.skyfighters.listeners.Listeners;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.*;
import java.util.Objects;

public class SkyFighters extends JavaPlugin {
    public static SkyFighters instance;

    public void onEnable() {
        instance = this;
        PaperLib.suggestPaper(this);
        this.saveDefaultConfig();
        PluginCommand skyFightersCommand = getCommand("skyfighters");
        assert skyFightersCommand != null;
        skyFightersCommand.setExecutor(new SkyFightersCommand());
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Listeners(), this);
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false));
    }

    public void onDisable() {
    }
}
