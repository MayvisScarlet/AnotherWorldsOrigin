package com.mayvisscarlet.anotherworldsorigin.config;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 種族設定の基底クラス
 * JSON設定を動的に管理する
 */
public class OriginConfig {
    public String originName = "";
    public String description = "";
    public String version = "1.0.0";
    
    // 動的設定値マップ
    private final Map<String, Object> values = new HashMap<>();
    
    /**
     * 設定値を追加
     */
    public void addValue(String key, Object value) {
        values.put(key, value);
    }
    
    /**
     * double値を取得
     */
    public double getDouble(String key, double defaultValue) {
        Object value = values.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /**
     * int値を取得
     */
    public int getInt(String key, int defaultValue) {
        Object value = values.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * boolean値を取得
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = values.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    /**
     * String値を取得
     */
    public String getString(String key, String defaultValue) {
        Object value = values.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
    
    /**
     * JSONオブジェクトに変換
     */
    public JsonObject toJsonObject() {
        JsonObject root = new JsonObject();
        root.addProperty("origin_name", originName);
        root.addProperty("description", description);
        root.addProperty("version", version);
        
        // 設定値を階層構造で追加
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            addToJsonHierarchy(root, entry.getKey(), entry.getValue());
        }
        
        return root;
    }
    
    /**
     * JSONの階層構造を作成
     */
    private void addToJsonHierarchy(JsonObject root, String key, Object value) {
        String[] parts = key.split("\\.");
        JsonObject current = root;
        
        // 最後の要素以外は階層を作成
        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i])) {
                current.add(parts[i], new JsonObject());
            }
            current = current.getAsJsonObject(parts[i]);
        }
        
        // 最後の要素に値を設定
        String finalKey = parts[parts.length - 1];
        if (value instanceof Number) {
            current.addProperty(finalKey, (Number) value);
        } else if (value instanceof Boolean) {
            current.addProperty(finalKey, (Boolean) value);
        } else if (value instanceof String) {
            current.addProperty(finalKey, (String) value);
        }
    }
}