package nl.kmartin.dartsmatcherapi.features.x01.model;


import java.util.Map;

public record X01LegEntry(int legNumber, X01Leg leg) {
    public X01LegEntry(Map.Entry<Integer, X01Leg> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}
