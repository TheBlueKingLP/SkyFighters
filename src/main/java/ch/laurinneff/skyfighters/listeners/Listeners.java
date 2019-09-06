package ch.laurinneff.skyfighters.listeners;

import ch.laurinneff.skyfighters.SkyFighters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class Listeners implements org.bukkit.event.Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_AIR) {
            // TODO Add shooting here
        }
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            // TODO Add Reloading here
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(ChatColor.GOLD + p.getName() + ChatColor.GREEN + " joined the game.");
        // TODO Give the new player some items
    }

    @EventHandler
    public void ServerListPing(ServerListPingEvent e) {
        e.setMaxPlayers(0);
        e.setMotd(ChatColor.GREEN + "SkyFighters " + ChatColor.GOLD + "v" + SkyFighters.instance.getDescription().getVersion() + "\n" + Integer.toString(Bukkit.getOnlinePlayers().size()) + ChatColor.WHITE + " Players are online.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        e.getPlayer().setRemainingAir(e.getPlayer().getMaximumAir());
        if (e.getPlayer().isGliding() && e.getPlayer().isSneaking()) {
            double vel = e.getPlayer().getVelocity().length();
            double newvel = e.getPlayer().getLocation().getDirection().multiply(1.0f).length();
            if (newvel > vel)
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.0f));
        }
    }

    @EventHandler
    public void toggleGlide(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e.isGliding()) {
                p.setVelocity(p.getLocation().getDirection().multiply(1.0f));
            } else {
                Bukkit.broadcastMessage(ChatColor.GOLD + p.getName() + ChatColor.RED + " stopped flying");
                // TODO Respawn the player
            }
        }
    }
}
