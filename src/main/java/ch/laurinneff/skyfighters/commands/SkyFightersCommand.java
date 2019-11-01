package ch.laurinneff.skyfighters.commands;

import ch.laurinneff.skyfighters.SkyFighters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkyFightersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SkyFighters.enabled = !SkyFighters.enabled;
        return false;
    }
}
