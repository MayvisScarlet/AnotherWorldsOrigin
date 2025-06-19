package com.mayvisscarlet.anotherworldsorigin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaOriginConfig;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON設定ファイルの管理クラス
 * 各種族の設定を動的に読み込み・保存する
 */
public class ConfigManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(AnotherWorldsOrigin.MODID);
    
    // 種族別設定キャッシュ
    private static final Map<String, OriginConfig> CONFIG_CACHE = new HashMap<>();
    
    /**
     * 設定ディレクトリを初期化
     */
    public static void initializeConfigDirectory() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                AnotherWorldsOrigin.LOGGER.info("Created config directory: {}", CONFIG_DIR);
            }
        } catch (IOException e) {
            AnotherWorldsOrigin.LOGGER.error("Failed to create config directory", e);
        }
    }
    
    /**
     * 指定された種族の設定を読み込み
     * @param originName 種族名（patricia, yura, carnis, vorey）
     * @return 種族設定オブジェクト
     */
    public static OriginConfig loadOriginConfig(String originName) {
        // キャッシュから取得を試行
        if (CONFIG_CACHE.containsKey(originName)) {
            return CONFIG_CACHE.get(originName);
        }
        
        Path configFile = CONFIG_DIR.resolve(originName + "_config.json");
        OriginConfig config;
        
        try {
            if (Files.exists(configFile)) {
                // 既存の設定ファイルを読み込み
                String jsonContent = Files.readString(configFile);
                JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
                config = parseOriginConfig(jsonObject);
                AnotherWorldsOrigin.LOGGER.info("Loaded config for {}: {}", originName, configFile);
            } else {
                // デフォルト設定を作成
                config = createDefaultConfig(originName);
                saveOriginConfig(originName, config);
                AnotherWorldsOrigin.LOGGER.info("Created default config for {}: {}", originName, configFile);
            }
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Failed to load config for {}, using defaults", originName, e);
            config = createDefaultConfig(originName);
        }
        
        // キャッシュに保存
        CONFIG_CACHE.put(originName, config);
        return config;
    }
    
    /**
     * 指定された種族の設定を保存
     */
    public static void saveOriginConfig(String originName, OriginConfig config) {
        Path configFile = CONFIG_DIR.resolve(originName + "_config.json");
        
        try {
            JsonObject jsonObject = config.toJsonObject();
            String jsonString = GSON.toJson(jsonObject);
            Files.writeString(configFile, jsonString);
            
            // キャッシュを更新
            CONFIG_CACHE.put(originName, config);
            AnotherWorldsOrigin.LOGGER.info("Saved config for {}: {}", originName, configFile);
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Failed to save config for {}", originName, e);
        }
    }
    
    /**
     * すべての設定を再読み込み
     */
    public static void reloadAllConfigs() {
        CONFIG_CACHE.clear();
        AnotherWorldsOrigin.LOGGER.info("Reloaded all origin configs");
    }
    
    /**
     * パトリシア用の設定を取得
     */
    public static PatriciaOriginConfig getPatriciaConfig() {
        OriginConfig baseConfig = loadOriginConfig("patricia");
        return new PatriciaOriginConfig(baseConfig);
    }
    
    /**
     * デフォルト設定を作成
     */
    private static OriginConfig createDefaultConfig(String originName) {
        switch (originName.toLowerCase()) {
            case "patricia":
                return createPatriciaDefaultConfig();
            case "yura":
                return createYuraDefaultConfig();
            case "carnis":
                return createCarnisDefaultConfig();
            case "vorey":
                return createVoreyDefaultConfig();
            default:
                return new OriginConfig();
        }
    }
    
    /**
     * パトリシアのデフォルト設定
     */
    private static OriginConfig createPatriciaDefaultConfig() {
        OriginConfig config = new OriginConfig();
        config.originName = "patricia";
        config.description = "Ice warrior with growth system and weakness management";
        config.version = "1.0.0";
        
        // パッシブ能力設定
        config.addValue("unwavering_winter.attack_power.affinity_coefficient", 0.4);
        config.addValue("unwavering_winter.attack_power.base_coefficient", 0.1);
        config.addValue("unwavering_winter.attack_power.attack_speed_multiplier", 1.5);
        config.addValue("unwavering_winter.attack_power.base_value", 4.0);
        
        config.addValue("unwavering_winter.cold_biome_benefits.base_damage_reduction", 15.0);
        config.addValue("unwavering_winter.cold_biome_benefits.affinity_coefficient", 0.35);
        config.addValue("unwavering_winter.cold_biome_benefits.max_damage_reduction", 25.0);
        config.addValue("unwavering_winter.cold_biome_benefits.temperature_threshold", 0.2);
        
        config.addValue("heat_vulnerability.fire_damage.base_multiplier", 2.0);
        config.addValue("heat_vulnerability.fire_damage.min_multiplier", 1.5);
        config.addValue("heat_vulnerability.fire_damage.affinity_reduction", 0.01);
        
        config.addValue("heat_vulnerability.hot_biome_penalties.base_damage_increase", 25.0);
        config.addValue("heat_vulnerability.hot_biome_penalties.affinity_coefficient", 0.8);
        config.addValue("heat_vulnerability.hot_biome_penalties.min_damage_increase", 0.0);
        config.addValue("heat_vulnerability.hot_biome_penalties.temperature_threshold", 1.0);
        
        config.addValue("heat_vulnerability.recovery_bonus.base_duration", 5.0);
        config.addValue("heat_vulnerability.recovery_bonus.duration_coefficient", 0.5);
        config.addValue("heat_vulnerability.recovery_bonus.amount_coefficient", 2.0);
        config.addValue("heat_vulnerability.recovery_bonus.max_increase", 100.0);
        
        // マイルストーン設定
        config.addValue("growth_system.milestones.high_affinity_threshold", 30);
        config.addValue("growth_system.milestones.advanced_threshold", 50);
        
        return config;
    }
    
    /**
     * 他の種族のデフォルト設定（将来実装）
     */
    private static OriginConfig createYuraDefaultConfig() {
        OriginConfig config = new OriginConfig();
        config.originName = "yura";
        // TODO: ユラの設定を追加
        return config;
    }
    
    private static OriginConfig createCarnisDefaultConfig() {
        OriginConfig config = new OriginConfig();
        config.originName = "carnis";
        // TODO: カーニスの設定を追加
        return config;
    }
    
    private static OriginConfig createVoreyDefaultConfig() {
        OriginConfig config = new OriginConfig();
        config.originName = "vorey";
        // TODO: ヴォレイの設定を追加
        return config;
    }
    
    /**
     * JSONからOriginConfigを解析
     */
    private static OriginConfig parseOriginConfig(JsonObject jsonObject) {
        OriginConfig config = new OriginConfig();
        
        if (jsonObject.has("origin_name")) {
            config.originName = jsonObject.get("origin_name").getAsString();
        }
        if (jsonObject.has("description")) {
            config.description = jsonObject.get("description").getAsString();
        }
        if (jsonObject.has("version")) {
            config.version = jsonObject.get("version").getAsString();
        }
        
        // 設定値を再帰的に解析
        parseJsonValues(jsonObject, "", config);
        
        return config;
    }
    
    /**
     * JSONの値を再帰的に解析してOriginConfigに格納
     */
    private static void parseJsonValues(JsonObject jsonObject, String prefix, OriginConfig config) {
        jsonObject.entrySet().forEach(entry -> {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            
            if (entry.getValue().isJsonObject()) {
                parseJsonValues(entry.getValue().getAsJsonObject(), key, config);
            } else if (entry.getValue().isJsonPrimitive()) {
                if (entry.getValue().getAsJsonPrimitive().isNumber()) {
                    config.addValue(key, entry.getValue().getAsDouble());
                } else if (entry.getValue().getAsJsonPrimitive().isBoolean()) {
                    config.addValue(key, entry.getValue().getAsBoolean());
                } else {
                    config.addValue(key, entry.getValue().getAsString());
                }
            }
        });
    }
}