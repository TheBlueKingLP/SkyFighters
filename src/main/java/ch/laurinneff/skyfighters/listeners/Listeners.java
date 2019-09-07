package ch.laurinneff.skyfighters.listeners;

import ch.laurinneff.skyfighters.Ring;
import ch.laurinneff.skyfighters.SkyFighters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Listeners implements org.bukkit.event.Listener {
    Map<Player, Long> boostCooldowns = new HashMap<Player, Long>();

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
        giveItems(p);
    }

    private void giveItems(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setChestplate(new ItemStack(Material.ELYTRA));
        p.getInventory().setContents(inv.getContents());
        p.updateInventory();
    }

    @EventHandler
    public void ServerListPing(ServerListPingEvent e) {
        e.setMaxPlayers(0);
        e.setMotd(ChatColor.GREEN + "SkyFighters " + ChatColor.GOLD + "v" + SkyFighters.instance.getDescription().getVersion() + "\n" + Integer.toString(Bukkit.getOnlinePlayers().size()) + ChatColor.WHITE + " Players are online.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        p.setRemainingAir(p.getMaximumAir());
        if (p.isGliding() && p.isSneaking() &&
                (!boostCooldowns.containsKey(p) ||
                        (boostCooldowns.get(p) + 1 > Instant.now().getEpochSecond()) ||
                        (boostCooldowns.get(p) + 3 < Instant.now().getEpochSecond()))) {
            double vel = p.getVelocity().length();
            double newvel = p.getLocation().getDirection().multiply(1.0f).length();
            if (newvel > vel) {
                p.setVelocity(p.getLocation().getDirection().multiply(1.0f));
                if (!boostCooldowns.containsKey(p) ||
                        !(boostCooldowns.get(p) + 1 > Instant.now().getEpochSecond()))
                    boostCooldowns.put(p, Instant.now().getEpochSecond() + 1);
            }
        }

        if (p.isGliding()) {
            if (!boostCooldowns.containsKey(p) || boostCooldowns.get(p) + 3 < Instant.now().getEpochSecond()) {
                p.sendActionBar(ChatColor.GREEN + "BOOST (SHIFT)");
            } else if (boostCooldowns.get(p) + 1 > Instant.now().getEpochSecond()) {
                p.sendActionBar(ChatColor.BLUE + "BOOSTING");
                double vel = p.getVelocity().length();
                double newvel = p.getLocation().getDirection().multiply(1.0f).length();
                if (newvel > vel)
                    p.setVelocity(p.getLocation().getDirection().multiply(1.0f));
            } else {
                long cooldownTimer = boostCooldowns.get(p) + 3 - Instant.now().getEpochSecond();
                p.sendActionBar(ChatColor.RED + "COOLDOWN " + cooldownTimer);
            }
        }

        Location loc = p.getLocation();
        for (Ring ring : SkyFighters.instance.rings) {
            if (p.isGliding() && (ring.x1 < loc.getX() && loc.getX() < ring.x2 && ring.y1 < loc.getY() && loc.getY() < ring.y2 && ring.z1 < loc.getZ() && loc.getZ() < ring.z2 ||
                    ring.x1 > loc.getX() && loc.getX() > ring.x2 && ring.y1 > loc.getY() && loc.getY() > ring.y2 && ring.z1 > loc.getZ() && loc.getZ() > ring.z2)) {
                if (ring.type.equals("boost")) {
                    double vel = p.getVelocity().length();
                    double newvel = p.getLocation().getDirection().multiply(2.0f).length();
                    if (newvel > vel)
                        p.setVelocity(p.getLocation().getDirection().multiply(2.0f));
                }
            }
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
                p.sendActionBar(" ");
                // TODO Respawn the player
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }
}
