package ch.laurinneff.skyfighters.listeners;

import ch.laurinneff.skyfighters.Ring;
import ch.laurinneff.skyfighters.SkyFighters;
import ch.laurinneff.skyfighters.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Listeners implements org.bukkit.event.Listener {
    private Map<Player, Long> boostCooldowns = new HashMap<>();
    private List<Player> reloadingPlayers = new ArrayList<>();

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        switch (e.getAction()) {
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                Weapon weapon = SkyFighters.instance.weapons.get(p.getInventory().getHeldItemSlot());
                shootWeapon(weapon, p);
                break;
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                weapon = SkyFighters.instance.weapons.get(p.getInventory().getHeldItemSlot());
                reloadWeapon(weapon, p);
                break;
        }
    }

    private void shootWeapon(Weapon w, Player p) {
        reloadingPlayers.remove(p);

        PlayerInventory inv = p.getInventory();
        boolean setBarrier = false;
        if (inv.getItemInMainHand().getAmount() == 1) {
            setBarrier = true;
        }
        if (inv.getItemInMainHand().getType() != Material.BARRIER) {
            switch (w.action) {
                case "fireball":
                    if (inv.getItemInMainHand().getAmount() > 0) {
                        Fireball f = p.launchProjectile(Fireball.class);
                        f.setVelocity(p.getLocation().getDirection().normalize().multiply(3));
                        inv.setItemInMainHand(inv.getItemInMainHand().subtract(1));
                    }
                    break;
            }
        }
        if (setBarrier) {
            ItemStack item = new ItemStack(Material.BARRIER);
            item.setAmount(1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(w.name);
            item.setItemMeta(meta);
            inv.setItemInMainHand(item);
        }
        p.getInventory().setContents(inv.getContents());
        p.updateInventory();
    }

    private void reloadWeapon(Weapon w, Player p) {
        reloadingPlayers.add(p);
        if (p.getInventory().getItemInMainHand().getAmount() != w.charges) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (reloadingPlayers.contains(p)) {
                        if (p.getInventory().getItemInMainHand().getType() == Material.BARRIER) {
                            PlayerInventory inv = p.getInventory();
                            ItemStack item = w.item;
                            item.setAmount(1);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(w.name);
                            item.setItemMeta(meta);
                            inv.setItemInMainHand(item);
                        } else {
                            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() + 1);
                        }

                        if (p.getInventory().getItemInMainHand().getAmount() >= w.charges) {
                            reloadingPlayers.remove(p);
                            cancel();
                        }
                    } else
                        cancel();
                }
            }.runTaskTimerAsynchronously(SkyFighters.instance, 0, 20);
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
        for (Weapon weapon : SkyFighters.instance.weapons) {
            ItemStack item = weapon.item;
            item.setAmount(weapon.charges);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(weapon.name);
            item.setItemMeta(meta);
            inv.setItem(SkyFighters.instance.weapons.indexOf(weapon), item);
        }
        p.getInventory().setContents(inv.getContents());
        p.updateInventory();
    }

    @EventHandler
    public void ServerListPing(ServerListPingEvent e) {
        e.setMaxPlayers(0);
        e.setMotd(ChatColor.GREEN + "SkyFighters " + ChatColor.GOLD + "v" + SkyFighters.instance.getDescription().getVersion() + "\n" + Bukkit.getOnlinePlayers().size() + ChatColor.WHITE + " Players are online.");
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
