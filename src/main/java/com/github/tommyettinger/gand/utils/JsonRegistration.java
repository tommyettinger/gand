package com.github.tommyettinger.gand.utils;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gdcrux.PointF2;
import com.github.tommyettinger.gdcrux.PointF3;
import com.github.tommyettinger.gdcrux.PointI2;
import com.github.tommyettinger.gdcrux.PointI3;

/**
 * Utility methods to register common Vector/GridPoint types with an instance of libGDX {@link Json}.
 * This can handle {@link Vector2}, {@link Vector3}, {@link Vector4}, {@link GridPoint2}, and {@link GridPoint3}.
 * It also adds short class tags for each registered type, such as "V2" or "G3". You do not need to manually register
 * the {@link PointF2}, {@link PointF3}, {@link PointI2}, or {@link PointI3} classes, since they implement
 * {@link Json.Serializable} already.
 */
public class JsonRegistration {
    private JsonRegistration() {}

    public static void registerAll(Json json) {
        registerVector2(json);
        registerVector3(json);
        registerVector4(json);
        registerGridPoint2(json);
        registerGridPoint3(json);
    }

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


    public static void registerGridPoint2(Json json) {
        json.addClassTag("G2", GridPoint2.class);
        json.setSerializer(GridPoint2.class, new Json.Serializer<GridPoint2>() {
            @Override
            public void write(Json json, GridPoint2 object, Class knownType) {
                json.writeObjectStart(GridPoint2.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeObjectEnd();
            }

            @Override
            public GridPoint2 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new GridPoint2(jsonData.getInt("x", 0), jsonData.getInt("y", 0));
            }
        });
    }

    public static void registerGridPoint3(Json json) {
        json.addClassTag("G3", GridPoint3.class);
        json.setSerializer(GridPoint3.class, new Json.Serializer<GridPoint3>() {
            @Override
            public void write(Json json, GridPoint3 object, Class knownType) {
                json.writeObjectStart(GridPoint3.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeValue("z", object.z);
                json.writeObjectEnd();
            }

            @Override
            public GridPoint3 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new GridPoint3(jsonData.getInt("x", 0), jsonData.getInt("y", 0), jsonData.getInt("z", 0));
            }
        });
    }

}
