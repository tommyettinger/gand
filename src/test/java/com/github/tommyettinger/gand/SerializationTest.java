package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.utils.ObjectDeque;
import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {
    public static void registerVector2(Json json) {
        json.addClassTag("V2", Vector2.class);
        json.setSerializer(Vector2.class, new Json.Serializer<Vector2>() {
            @Override
            public void write(Json json, Vector2 object, Class knownType) {
                json.writeObjectStart(Vector2.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeObjectEnd();
            }

            @Override
            public Vector2 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Vector2(jsonData.getFloat("x", 0f), jsonData.getFloat("y", 0f));
            }
        });
    }


    @Test
    public void testObjectDeque() {
        Json json = new Json();
        registerVector2(json);
        ObjectDeque<Vector2> data = ObjectDeque.with(new Vector2(1, 0.1f), new Vector2(2, 0.2f), new Vector2(3, 0.3f));
        String text = json.toJson(data, ObjectDeque.class, Vector2.class);
        System.out.println(text);
        ObjectDeque next = json.fromJson(ObjectDeque.class, Vector2.class, text);
        System.out.println(json.toJson(next, ObjectDeque.class, Vector2.class));
        Assert.assertEquals(data, next);
    }
}
