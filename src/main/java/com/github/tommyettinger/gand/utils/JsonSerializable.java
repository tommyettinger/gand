package com.github.tommyettinger.gand.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * An expanded version of {@link Json.Serializable}, allowing code to write directly to a JsonValue with
 * {@link #writeToJsonValue(Json, JsonValue)}, read directly from a JsonValue into the current instance with
 * {@link #readFromJsonValue(Json, JsonValue)}, and perform both of those operations into a child JsonValue with a given
 * String name, in addition to what Json.Serializable provides. The named-child operations have default implementations.
 */
public interface JsonSerializable extends Json.Serializable {
    /**
     * Writes the serialized value of this to the given Json.
     * @param json a non-null libGDX Json instance that this will use to write a JsonValue
     */
    @Override
    void write(Json json);

    /**
     * Reads the data from {@code jsonData} into this object, using {@code json} to help read if necessary.
     * Modifies this object in-place.
     *
     * @param json a non-null libGDX Json instance that this may or may not use to help read from jsonData
     * @param jsonData a JsonValue containing the serialized form of an object that can be assigned to this
     */
    @Override
    void read(Json json, JsonValue jsonData);

    /**
     * Writes the state of this into a String JsonValue. This overload does not write to a child
     * of the given JsonValue, and instead {@link JsonValue#set(String) sets} the JsonValue directly.
     * @param modifying the JsonValue that will be set to the serialized version of this
     */
    void writeToJsonValue(Json json, JsonValue modifying);

    /**
     * Reads the state of this from a String JsonValue.
     * @param value the JsonValue that will be used to assign this
     */
    void readFromJsonValue(Json json, JsonValue value);

    /**
     * Writes the state of this into a String child of the given JsonValue.
     * @param parent the JsonValue that will have this added as a child using the given key name
     * @param key the name to store the state into
     */
    default void writeToJsonValue(Json json, JsonValue parent, String key) {
        JsonValue jv = new JsonValue(JsonValue.ValueType.object);
        writeToJsonValue(json, jv);
        parent.addChild(key, jv);
    }

    /**
     * Reads the state of this from a String child of the given JsonValue.
     * @param parent the JsonValue that this will look the given key name up in
     * @param key the name to read the data from
     */
    default void readFromJsonValue(Json json, JsonValue parent, String key) {
        JsonValue jv = parent.get(key);
        if(jv != null) {
            readFromJsonValue(json, jv);
        }
    }

}
