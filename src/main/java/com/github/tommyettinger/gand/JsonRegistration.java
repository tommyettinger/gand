package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class JsonRegistration {
    private JsonRegistration() {}

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

    public static void registerVector3(Json json) {
        json.addClassTag("V3", Vector3.class);
        json.setSerializer(Vector3.class, new Json.Serializer<Vector3>() {
            @Override
            public void write(Json json, Vector3 object, Class knownType) {
                json.writeObjectStart(Vector3.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeValue("z", object.z);
                json.writeObjectEnd();
            }

            @Override
            public Vector3 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Vector3(jsonData.getFloat("x", 0f), jsonData.getFloat("y", 0f), jsonData.getFloat("z", 0f));
            }
        });
    }

    public static void registerVector4(Json json) {
        json.addClassTag("V4", Vector4.class);
        json.setSerializer(Vector4.class, new Json.Serializer<Vector4>() {
            @Override
            public void write(Json json, Vector4 object, Class knownType) {
                json.writeObjectStart(Vector4.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeValue("z", object.z);
                json.writeValue("z", object.w);
                json.writeObjectEnd();
            }

            @Override
            public Vector4 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Vector4(jsonData.getFloat("x", 0f), jsonData.getFloat("y", 0f),
                        jsonData.getFloat("z", 0f), jsonData.getFloat("w", 0f));
            }
        });
    }
}
