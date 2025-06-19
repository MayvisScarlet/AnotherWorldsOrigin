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
 * デフォルト値一元管理型JSON設定ファイル管理クラス
 * 各種族のデフォルト値を一箇所で管理し、種族別Configに提供
 */
public class ConfigManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(AnotherWorldsOrigin.MODID);
    
    // 種族別設定キャッシュ
    private static final Map<String, OriginConfig> CONFIG_CACHE = new HashMap<>();
    
    // デフォルト値マップ（種族名 -> 設定キー -> 値）
    private static final Map<String, Map<String, Object>> DEFAULT_VALUES = new HashMap<>();
    
    /**
     * デフォルト値を初期化
     */
    static {
        initializeDefaultValues();
    }
    
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
                config = parseOriginConfig(jsonObject, originName);
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
     * 指定された種族・キーのデフォルト値を取得
     * PatriciaOriginConfig等から呼び出される
     */
    public static Object getDefaultValue(String originName, String key) {
        Map<String, Object> originDefaults = DEFAULT_VALUES.get(originName.toLowerCase());
        if (originDefaults == null) {
            AnotherWorldsOrigin.LOGGER.warn("No default values found for origin: {}", originName);
            return null;
        }
        
        Object value = originDefaults.get(key);
        if (value == null) {
            AnotherWorldsOrigin.LOGGER.debug("No default value found for {}:{}", originName, key);
        }
        
        return value;
    }
    
    /**
     * 型安全なデフォルト値取得メソッド
     */
    public static double getDefaultDouble(String originName, String key, double fallback) {
        Object value = getDefaultValue(originName, key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return fallback;
    }
    
    public static int getDefaultInt(String originName, String key, int fallback) {
        Object value = getDefaultValue(originName, key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return fallback;
    }
    
    public static boolean getDefaultBoolean(String originName, String key, boolean fallback) {
        Object value = getDefaultValue(originName, key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return fallback;
    }
    
    public static String getDefaultString(String originName, String key, String fallback) {
        Object value = getDefaultValue(originName, key);
        if (value instanceof String) {
            return (String) value;
        }
        return fallback;
    }
    
    /**
     * デフォルト値を初期化（一元管理）
     */
    private static void initializeDefaultValues() {
        // パトリシアのデフォルト値
        Map<String, Object> patriciaDefaults = new HashMap<>();
        
        // 基本情報
        patriciaDefaults.put("origin_name", "patricia");
        patriciaDefaults.put("description", "Ice warrior with growth system and weakness management");
        patriciaDefaults.put("version", "1.0.0");
        
        // パッシブ能力設定
        patriciaDefaults.put("unwavering_winter.attack_power.affinity_coefficient", 0.4);
        patriciaDefaults.put("unwavering_winter.attack_power.base_coefficient", 0.1);
        patriciaDefaults.put("unwavering_winter.attack_power.attack_speed_multiplier", 1.5);
        patriciaDefaults.put("unwavering_winter.attack_power.base_value", 4.0);
        
        patriciaDefaults.put("unwavering_winter.cold_biome_benefits.base_damage_reduction", 20.0);
        patriciaDefaults.put("unwavering_winter.cold_biome_benefits.affinity_coefficient", 0.5);
        patriciaDefaults.put("unwavering_winter.cold_biome_benefits.max_damage_reduction", 50.0);
        patriciaDefaults.put("unwavering_winter.cold_biome_benefits.temperature_threshold", 0.2);
        
        patriciaDefaults.put("heat_vulnerability.fire_damage.base_multiplier", 2.0);
        patriciaDefaults.put("heat_vulnerability.fire_damage.min_multiplier", 1.5);
        patriciaDefaults.put("heat_vulnerability.fire_damage.affinity_reduction", 0.01);
        
        patriciaDefaults.put("heat_vulnerability.hot_biome_penalties.base_damage_increase", 25.0);
        patriciaDefaults.put("heat_vulnerability.hot_biome_penalties.affinity_coefficient", 0.8);
        patriciaDefaults.put("heat_vulnerability.hot_biome_penalties.min_damage_increase", 0.0);
        patriciaDefaults.put("heat_vulnerability.hot_biome_penalties.temperature_threshold", 1.0);
        
        patriciaDefaults.put("heat_vulnerability.recovery_bonus.base_duration", 5.0);
        patriciaDefaults.put("heat_vulnerability.recovery_bonus.duration_coefficient", 0.5);
        patriciaDefaults.put("heat_vulnerability.recovery_bonus.amount_coefficient", 2.0);
        patriciaDefaults.put("heat_vulnerability.recovery_bonus.max_increase", 100.0);
        
        // マイルストーン設定
        patriciaDefaults.put("growth_system.milestones.high_affinity_threshold", 30);
        patriciaDefaults.put("growth_system.milestones.advanced_threshold", 50);
        
        // デバッグ設定
        patriciaDefaults.put("debug.show_passive_messages", false);
        
        DEFAULT_VALUES.put("patricia", patriciaDefaults);
        
        // 将来の種族用のデフォルト値を準備
        initializeYuraDefaults();
        initializeCarnisDefaults();
        initializeVoreyDefaults();
        
        AnotherWorldsOrigin.LOGGER.info("Initialized default values for {} origins", DEFAULT_VALUES.size());
    }
    
    /**
     * ユラのデフォルト値（将来実装）
     */
    private static void initializeYuraDefaults() {
        Map<String, Object> yuraDefaults = new HashMap<>();
        yuraDefaults.put("origin_name", "yura");
        yuraDefaults.put("description", "Agile warrior with speed-based abilities");
        yuraDefaults.put("version", "1.0.0");
        
        // 将来の実装用プレースホルダー
        yuraDefaults.put("agility_system.speed_boost", 1.5);
        yuraDefaults.put("agility_system.threshold", 25);
        
        DEFAULT_VALUES.put("yura", yuraDefaults);
    }
    
    /**
     * カーニスのデフォルト値（将来実装）
     */
    private static void initializeCarnisDefaults() {
        Map<String, Object> carnisDefaults = new HashMap<>();
        carnisDefaults.put("origin_name", "carnis");
        carnisDefaults.put("description", "Strength-based warrior with defensive capabilities");
        carnisDefaults.put("version", "1.0.0");
        
        // 将来の実装用プレースホルダー
        carnisDefaults.put("strength_system.multiplier", 1.8);
        carnisDefaults.put("strength_system.threshold", 35);
        
        DEFAULT_VALUES.put("carnis", carnisDefaults);
    }
    
    /**
     * ヴォレイのデフォルト値（将来実装）
     */
    private static void initializeVoreyDefaults() {
        Map<String, Object> voreyDefaults = new HashMap<>();
        voreyDefaults.put("origin_name", "vorey");
        voreyDefaults.put("description", "Magic-based warrior with elemental affinities");
        voreyDefaults.put("version", "1.0.0");
        
        // 将来の実装用プレースホルダー
        voreyDefaults.put("magic_system.amplifier", 2.0);
        voreyDefaults.put("magic_system.threshold", 40);
        
        DEFAULT_VALUES.put("vorey", voreyDefaults);
    }
    
    /**
     * デフォルト設定をテンプレートから作成
     */
    private static OriginConfig createDefaultConfig(String originName) {
        Map<String, Object> defaults = DEFAULT_VALUES.get(originName.toLowerCase());
        
        if (defaults == null) {
            AnotherWorldsOrigin.LOGGER.warn("No default values found for origin: {}", originName);
            return new OriginConfig();
        }
        
        OriginConfig config = new OriginConfig();
        config.originName = (String) defaults.getOrDefault("origin_name", originName);
        config.description = (String) defaults.getOrDefault("description", "No description");
        config.version = (String) defaults.getOrDefault("version", "1.0.0");
        
        // すべてのデフォルト値をコピー
        defaults.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("origin_name") && 
                           !entry.getKey().equals("description") && 
                           !entry.getKey().equals("version"))
            .forEach(entry -> config.addValue(entry.getKey(), entry.getValue()));
        
        return config;
    }
    
    /**
     * JSONからOriginConfigを解析（デフォルト値ベース）
     */
    private static OriginConfig parseOriginConfig(JsonObject jsonObject, String originName) {
        // まずデフォルト設定を取得
        OriginConfig config = createDefaultConfig(originName);
        
        // JSONから値を上書き
        if (jsonObject.has("origin_name")) {
            config.originName = jsonObject.get("origin_name").getAsString();
        }
        if (jsonObject.has("description")) {
            config.description = jsonObject.get("description").getAsString();
        }
        if (jsonObject.has("version")) {
            config.version = jsonObject.get("version").getAsString();
        }
        
        // 設定値を再帰的に解析してマージ
        parseJsonValues(jsonObject, "", config);
        
        return config;
    }
    
    /**
     * JSONの値を再帰的に解析してOriginConfigに格納
     */
    private static void parseJsonValues(JsonObject jsonObject, String prefix, OriginConfig config) {
        jsonObject.entrySet().forEach(entry -> {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            
            // 基本メタデータはスキップ
            if (key.equals("origin_name") || key.equals("description") || key.equals("version")) {
                return;
            }
            
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
    
    /**
     * 設定値の一括更新（管理者用）
     */
    public static void updateOriginConfigValue(String originName, String key, Object value) {
        OriginConfig config = loadOriginConfig(originName);
        config.addValue(key, value);
        saveOriginConfig(originName, config);
        
        AnotherWorldsOrigin.LOGGER.info("Updated config value for {}: {} = {}", originName, key, value);
    }
    
    /**
     * 全種族の設定概要を取得（デバッグ用）
     */
    public static Map<String, String> getAllOriginConfigSummary() {
        Map<String, String> summary = new HashMap<>();
        
        DEFAULT_VALUES.keySet().forEach(originName -> {
            try {
                OriginConfig config = loadOriginConfig(originName);
                summary.put(originName, String.format("%s v%s - %s", 
                    config.originName, config.version, config.description));
            } catch (Exception e) {
                summary.put(originName, "Error loading config: " + e.getMessage());
            }
        });
        
        return summary;
    }
}