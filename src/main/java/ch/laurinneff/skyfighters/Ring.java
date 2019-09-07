package ch.laurinneff.skyfighters;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("Ring")
public class Ring implements ConfigurationSerializable {
    public String type;
    public int x1;
    public int y1;
    public int z1;
    public int x2;
    public int y2;
    public int z2;

    public Ring(String type, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", this.type);
        map.put("x1", this.x1);
        map.put("y1", this.y1);
        map.put("z1", this.z1);
        map.put("x2", this.x2);
        map.put("y2", this.y2);
        map.put("z2", this.z2);
        return map;
    }

    public static Ring deserialize(Map<String, Object> map) {
        return new Ring((String) map.get("type"), (int) map.get("x1"), (int) map.get("y1"), (int) map.get("z1"), (int) map.get("x2"), (int) map.get("y2"), (int) map.get("z2"));
    }
}
