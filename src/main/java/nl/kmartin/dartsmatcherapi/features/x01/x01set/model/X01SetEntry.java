package nl.kmartin.dartsmatcherapi.features.x01.x01set.model;

import java.util.Map;

/**
 * Represents a numbered X01 set.
 *
 * @param setNumber the set number
 * @param set the set
 */
public record X01SetEntry(int setNumber, X01Set set) {

    /**
     * Creates a set entry from a map entry.
     *
     * @param mapEntry the numbered set map entry
     */
    public X01SetEntry(Map.Entry<Integer, X01Set> mapEntry) {
        this(mapEntry.getKey(), mapEntry.getValue());
    }
}