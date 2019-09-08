package ch.laurinneff.skyfighters;

import com.destroystokyo.paper.Namespaced;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

@SerializableAs("Weapon")
public class Weapon implements ConfigurationSerializable {
    public String name;
    public ItemStack item;
    public String action;
    public int damage;
    public int charges;

    public Weapon(String name, ItemStack item, String action, int damage, int charges) {
        this.name = name;
        this.item = item;
        this.action = action;
        this.damage = damage;
        this.charges = charges;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("item", item);
        map.put("action", action);
        map.put("damage", damage);
        map.put("charges", charges);
        return map;
    }

    public static Weapon deserialize(Map<String, Object> map) {
        return new Weapon((String) map.get("name"), (ItemStack) map.get("item"), (String) map.get("action"), (int) map.get("damage"), (int) map.get("charges"));
    }
}
