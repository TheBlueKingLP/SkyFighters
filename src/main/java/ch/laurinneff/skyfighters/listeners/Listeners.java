package ch.laurinneff.skyfighters.listeners;

import ch.laurinneff.skyfighters.Ring;
import ch.laurinneff.skyfighters.SkyFighters;
import ch.laurinneff.skyfighters.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;

public class Listeners implements org.bukkit.event.Listener {
    private Map<Player, Long> boostCooldowns = new HashMap<>();
    private List<Player> reloadingPlayers = new ArrayList<>();

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (SkyFighters.enabled) {
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
                        f.setIsIncendiary(false);
                        f.setCustomName(w.name + ";" + p.getName());
                        f.setVelocity(p.getLocation().getDirection().normalize().multiply(3 * w.speed));
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
        int reloadSlot = p.getInventory().getHeldItemSlot();
        if (Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).getAmount() < w.charges && !reloadingPlayers.contains(p)) {
            reloadingPlayers.add(p);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (reloadingPlayers.contains(p)) {
                        if (Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).getType() == Material.BARRIER) {
                            PlayerInventory inv = p.getInventory();
                            ItemStack item = w.item;
                            item.setAmount(1);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(w.name);
                            item.setItemMeta(meta);
                            inv.setItem(reloadSlot, item);
                        } else {
                            if (Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).getAmount() < w.charges)
                                Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).setAmount(Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).getAmount() + 1);
                        }

                        if (Objects.requireNonNull(p.getInventory().getItem(reloadSlot)).getAmount() >= w.charges) {
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
        if (SkyFighters.enabled) {
            Player p = e.getPlayer();
            e.setJoinMessage(ChatColor.AQUA + p.getName() + ChatColor.GREEN + " joined the game.");
            respawn(p);
        }
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

    private void respawn(Player p) {
        giveItems(p);
        p.teleport(new Location(p.getWorld(), SkyFighters.instance.getConfig().getDouble("spawnPoint.x"),
                SkyFighters.instance.getConfig().getDouble("spawnPoint.y"), SkyFighters.instance.getConfig().getDouble("spawnPoint.z"),
                (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.yaw"), (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.pitch")));
    }

    @EventHandler
    public void ServerListPing(ServerListPingEvent e) {
        if (SkyFighters.enabled) {
            e.setMaxPlayers(0);
            e.setMotd(ChatColor.GREEN + "SkyFighters " + ChatColor.GOLD + "v" + SkyFighters.instance.getDescription().getVersion() + "\n" + Bukkit.getOnlinePlayers().size() + ChatColor.WHITE + " Players are online.");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (SkyFighters.enabled) {
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
            } else {
                FileConfiguration config = SkyFighters.instance.getConfig();
                Location loc = p.getLocation();
                double x1 = config.getDouble("spawnArea.x1");
                double y1 = config.getDouble("spawnArea.y1");
                double z1 = config.getDouble("spawnArea.z1");
                double x2 = config.getDouble("spawnArea.x2");
                double y2 = config.getDouble("spawnArea.y2");
                double z2 = config.getDouble("spawnArea.z2");
                if (!(x1 < loc.getX() && loc.getX() < x2 && y1 < loc.getY() && loc.getY() < y2 && z1 < loc.getZ() && loc.getZ() < z2 ||
                        x1 > loc.getX() && loc.getX() > x2 && y1 > loc.getY() && loc.getY() > y2 && z1 > loc.getZ() && loc.getZ() > z2)) {
                    p.teleport(new Location(p.getWorld(), SkyFighters.instance.getConfig().getDouble("spawnPoint.x"),
                            SkyFighters.instance.getConfig().getDouble("spawnPoint.y"), SkyFighters.instance.getConfig().getDouble("spawnPoint.z"),
                            (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.yaw"), (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.pitch")));
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
    }

    @EventHandler
    public void toggleGlide(EntityToggleGlideEvent e) {
        if (SkyFighters.enabled) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();
                if (e.isGliding()) {
                    p.setVelocity(p.getLocation().getDirection().multiply(1.0f));
                } else {
                    Bukkit.broadcastMessage(ChatColor.AQUA + p.getName() + ChatColor.RED + " stopped flying");
                    p.sendActionBar(" ");
                    FileConfiguration config = SkyFighters.instance.getConfig();
                    Location loc = p.getLocation();
                    double x1 = config.getDouble("spawnArea.x1");
                    double y1 = config.getDouble("spawnArea.y1");
                    double z1 = config.getDouble("spawnArea.z1");
                    double x2 = config.getDouble("spawnArea.x2");
                    double y2 = config.getDouble("spawnArea.y2");
                    double z2 = config.getDouble("spawnArea.z2");
                    if (!(x1 < loc.getX() && loc.getX() < x2 && y1 < loc.getY() && loc.getY() < y2 && z1 < loc.getZ() && loc.getZ() < z2 ||
                            x1 > loc.getX() && loc.getX() > x2 && y1 > loc.getY() && loc.getY() > y2 && z1 > loc.getZ() && loc.getZ() > z2)) {
                        p.teleport(new Location(p.getWorld(), SkyFighters.instance.getConfig().getDouble("spawnPoint.x"),
                                SkyFighters.instance.getConfig().getDouble("spawnPoint.y"), SkyFighters.instance.getConfig().getDouble("spawnPoint.z"),
                                (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.yaw"), (float) SkyFighters.instance.getConfig().getDouble("spawnPoint.pitch")));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent e) {
        if (SkyFighters.enabled) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (SkyFighters.enabled) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();
                e.setCancelled(true);

                if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    Weapon w = new Weapon("Generic Weapon", new ItemStack(Material.FIRE_CHARGE), "fireball", 1, 1, 1);
                    for (Weapon weapon : SkyFighters.instance.weapons) {
                        if (weapon.name.equals(Objects.requireNonNull(e.getDamager().getCustomName()).split(";")[0])) {
                            w = weapon;
                            break;
                        }
                    }
                    double newHealth = p.getHealth() - w.damage;
                    if (newHealth < 0) {
                        Bukkit.broadcastMessage(ChatColor.AQUA + p.getName() + ChatColor.RED + " was killed by " +
                                ChatColor.AQUA + Objects.requireNonNull(e.getDamager().getCustomName()).split(";")[1] +
                                ChatColor.RED +" with " + ChatColor.AQUA + w.name);
                        p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
                        p.setFoodLevel(20);
                        respawn(p);
                    } else
                        p.setHealth(newHealth);
                }
            }
        }
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        if (SkyFighters.enabled) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent e) {
        // stop fireballs from destroying blocks
        e.setCancelled(true);
    }

    @EventHandler
    public void itemDrop(PlayerDropItemEvent e) {
        if (SkyFighters.enabled) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void foodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
}
