package com.mayvisscarlet.anotherworldsorigin.config;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaOriginConfig;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 設定システムの初期化を管理するクラス
 */
public class ConfigInitializer {
    
    /**
     * 設定システムを初期化
     * MOD読み込み時に呼び出される
     */
    public static void initialize(FMLCommonSetupEvent event) {
        AnotherWorldsOrigin.LOGGER.info("Initializing Another Worlds Origin config system...");
        
        // 設定ディレクトリを作成
        ConfigManager.initializeConfigDirectory();
        
        // 各種族のデフォルト設定を読み込み/作成
        event.enqueueWork(() -> {
            loadAllDefaultConfigs();
        });
        
        AnotherWorldsOrigin.LOGGER.info("Config system initialized successfully!");
    }
    
    /**
     * 全種族のデフォルト設定を読み込み
     */
    private static void loadAllDefaultConfigs() {
        try {
            // パトリシア設定
            PatriciaOriginConfig patriciaConfig = ConfigManager.getPatriciaConfig();
            AnotherWorldsOrigin.LOGGER.info("Patricia config loaded. High affinity threshold: {}", 
                patriciaConfig.getHighAffinityThreshold());
            
            // 他の種族も将来的に追加
            // YuraOriginConfig yuraConfig = ConfigManager.getYuraConfig();
            // CarnisOriginConfig carnisConfig = ConfigManager.getCarnisConfig();
            // VoreyOriginConfig voreyConfig = ConfigManager.getVoreyConfig();
            
            AnotherWorldsOrigin.LOGGER.info("All default configs loaded successfully!");
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Failed to load default configs", e);
        }
    }
    
    /**
     * 設定の妥当性をチェック
     */
    public static void validateConfigs() {
        try {
            PatriciaOriginConfig config = ConfigManager.getPatriciaConfig();
            
            // 重要な数値のチェック
            if (config.getAffinityAttackCoefficient() < 0) {
                AnotherWorldsOrigin.LOGGER.warn("Patricia affinity attack coefficient is negative: {}", 
                    config.getAffinityAttackCoefficient());
            }
            
            if (config.getFireBaseMultiplier() < 1.0) {
                AnotherWorldsOrigin.LOGGER.warn("Patricia fire base multiplier is less than 1.0: {}", 
                    config.getFireBaseMultiplier());
            }
            
            if (config.getHighAffinityThreshold() <= 0) {
                AnotherWorldsOrigin.LOGGER.warn("Patricia high affinity threshold is invalid: {}", 
                    config.getHighAffinityThreshold());
            }
            
            AnotherWorldsOrigin.LOGGER.info("Config validation completed");
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Config validation failed", e);
        }
    }
}