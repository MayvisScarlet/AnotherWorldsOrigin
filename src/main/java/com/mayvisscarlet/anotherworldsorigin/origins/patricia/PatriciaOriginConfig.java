package com.mayvisscarlet.anotherworldsorigin.origins.patricia;

import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.config.OriginConfig;

/**
 * パトリシア専用設定クラス（完全一元化版）
 * 数値は一切記載せず、ConfigManagerのみがデフォルト値を管理
 */
public class PatriciaOriginConfig {
    private final OriginConfig baseConfig;
    private static final String ORIGIN_NAME = "patricia";
    
    public PatriciaOriginConfig(OriginConfig config) {
        this.baseConfig = config;
    }
    
    // === 攻撃力計算式 ===
    public double getAffinityAttackCoefficient() {
        return baseConfig.getDouble("unwavering_winter.attack_power.affinity_coefficient", 
            getDefaultDouble("unwavering_winter.attack_power.affinity_coefficient"));
    }
    
    public double getBaseCoefficient() {
        return baseConfig.getDouble("unwavering_winter.attack_power.base_coefficient", 
            getDefaultDouble("unwavering_winter.attack_power.base_coefficient"));
    }
    
    public double getAttackSpeedMultiplier() {
        return baseConfig.getDouble("unwavering_winter.attack_power.attack_speed_multiplier", 
            getDefaultDouble("unwavering_winter.attack_power.attack_speed_multiplier"));
    }
    
    public double getBaseValue() {
        return baseConfig.getDouble("unwavering_winter.attack_power.base_value", 
            getDefaultDouble("unwavering_winter.attack_power.base_value"));
    }
    
    // === Cold系バイオーム ===
    public double getColdBaseDamageReduction() {
        return baseConfig.getDouble("unwavering_winter.cold_biome_benefits.base_damage_reduction", 
            getDefaultDouble("unwavering_winter.cold_biome_benefits.base_damage_reduction"));
    }
    
    public double getColdAffinityCoefficient() {
        return baseConfig.getDouble("unwavering_winter.cold_biome_benefits.affinity_coefficient", 
            getDefaultDouble("unwavering_winter.cold_biome_benefits.affinity_coefficient"));
    }
    
    public double getColdMaxDamageReduction() {
        return baseConfig.getDouble("unwavering_winter.cold_biome_benefits.max_damage_reduction", 
            getDefaultDouble("unwavering_winter.cold_biome_benefits.max_damage_reduction"));
    }
    
    public double getColdTemperatureThreshold() {
        return baseConfig.getDouble("unwavering_winter.cold_biome_benefits.temperature_threshold", 
            getDefaultDouble("unwavering_winter.cold_biome_benefits.temperature_threshold"));
    }
    
    // === 火炎ダメージ ===
    public double getFireBaseMultiplier() {
        return baseConfig.getDouble("heat_vulnerability.fire_damage.base_multiplier", 
            getDefaultDouble("heat_vulnerability.fire_damage.base_multiplier"));
    }
    
    public double getFireMinMultiplier() {
        return baseConfig.getDouble("heat_vulnerability.fire_damage.min_multiplier", 
            getDefaultDouble("heat_vulnerability.fire_damage.min_multiplier"));
    }
    
    public double getFireAffinityReduction() {
        return baseConfig.getDouble("heat_vulnerability.fire_damage.affinity_reduction", 
            getDefaultDouble("heat_vulnerability.fire_damage.affinity_reduction"));
    }
    
    // === Hot系バイオーム ===
    public double getHotBaseDamageIncrease() {
        return baseConfig.getDouble("heat_vulnerability.hot_biome_penalties.base_damage_increase", 
            getDefaultDouble("heat_vulnerability.hot_biome_penalties.base_damage_increase"));
    }
    
    public double getHotAffinityCoefficient() {
        return baseConfig.getDouble("heat_vulnerability.hot_biome_penalties.affinity_coefficient", 
            getDefaultDouble("heat_vulnerability.hot_biome_penalties.affinity_coefficient"));
    }
    
    public double getHotMinDamageIncrease() {
        return baseConfig.getDouble("heat_vulnerability.hot_biome_penalties.min_damage_increase", 
            getDefaultDouble("heat_vulnerability.hot_biome_penalties.min_damage_increase"));
    }
    
    public double getHotTemperatureThreshold() {
        return baseConfig.getDouble("heat_vulnerability.hot_biome_penalties.temperature_threshold", 
            getDefaultDouble("heat_vulnerability.hot_biome_penalties.temperature_threshold"));
    }
    
    // === 回復ボーナス ===
    public double getRecoveryBaseDuration() {
        return baseConfig.getDouble("heat_vulnerability.recovery_bonus.base_duration", 
            getDefaultDouble("heat_vulnerability.recovery_bonus.base_duration"));
    }
    
    public double getRecoveryDurationCoefficient() {
        return baseConfig.getDouble("heat_vulnerability.recovery_bonus.duration_coefficient", 
            getDefaultDouble("heat_vulnerability.recovery_bonus.duration_coefficient"));
    }
    
    public double getRecoveryAmountCoefficient() {
        return baseConfig.getDouble("heat_vulnerability.recovery_bonus.amount_coefficient", 
            getDefaultDouble("heat_vulnerability.recovery_bonus.amount_coefficient"));
    }
    
    public double getRecoveryMaxIncrease() {
        return baseConfig.getDouble("heat_vulnerability.recovery_bonus.max_increase", 
            getDefaultDouble("heat_vulnerability.recovery_bonus.max_increase"));
    }
    
    // === マイルストーン ===
    public int getHighAffinityThreshold() {
        return baseConfig.getInt("growth_system.milestones.high_affinity_threshold", 
            getDefaultInt("growth_system.milestones.high_affinity_threshold"));
    }
    
    public int getAdvancedThreshold() {
        return baseConfig.getInt("growth_system.milestones.advanced_threshold", 
            getDefaultInt("growth_system.milestones.advanced_threshold"));
    }
    
    // === デバッグ設定 ===
    public boolean shouldShowDebugMessages() {
        return baseConfig.getBoolean("debug.show_passive_messages", 
            getDefaultBoolean("debug.show_passive_messages"));
    }
    
    // === デフォルト値取得ヘルパーメソッド ===
    
    private double getDefaultDouble(String key) {
        Double value = (Double) ConfigManager.getDefaultValue(ORIGIN_NAME, key);
        if (value == null) {
            throw new IllegalStateException("No default value found for patricia:" + key);
        }
        return value;
    }
    
    private int getDefaultInt(String key) {
        Number value = (Number) ConfigManager.getDefaultValue(ORIGIN_NAME, key);
        if (value == null) {
            throw new IllegalStateException("No default value found for patricia:" + key);
        }
        return value.intValue();
    }
    
    private boolean getDefaultBoolean(String key) {
        Boolean value = (Boolean) ConfigManager.getDefaultValue(ORIGIN_NAME, key);
        if (value == null) {
            throw new IllegalStateException("No default value found for patricia:" + key);
        }
        return value;
    }
    
    private String getDefaultString(String key) {
        String value = (String) ConfigManager.getDefaultValue(ORIGIN_NAME, key);
        if (value == null) {
            throw new IllegalStateException("No default value found for patricia:" + key);
        }
        return value;
    }
    
    // === 計算メソッド（ビジネスロジック） ===
    
    /**
     * 親和度に基づいた攻撃力上昇量を計算
     */
    public double calculateAffinityAttackBonus(int affinityLevel) {
        return affinityLevel * getAffinityAttackCoefficient();
    }
    
    /**
     * 親和度と攻撃速度に基づいた攻撃力補正を計算
     */
    public double calculateAttackSpeedCompensation(int affinityLevel, double currentAttackSpeed) {
        double baseValue = getBaseValue() + (affinityLevel * getBaseCoefficient());
        return Math.max(0, (baseValue - currentAttackSpeed) * getAttackSpeedMultiplier());
    }
    
    /**
     * Cold系バイオームでのダメージ軽減率を計算
     */
    public double calculateColdDamageReduction(int affinityLevel) {
        double reduction = getColdBaseDamageReduction() + (affinityLevel * getColdAffinityCoefficient());
        return Math.min(getColdMaxDamageReduction(), reduction) / 100.0;
    }
    
    /**
     * 火炎ダメージの倍率を計算
     */
    public double calculateFireDamageMultiplier(int affinityLevel) {
        double multiplier = getFireBaseMultiplier() - (affinityLevel * getFireAffinityReduction());
        return Math.max(getFireMinMultiplier(), multiplier);
    }
    
    /**
     * Hot系バイオームでのダメージ増加率を計算
     */
    public double calculateHotDamageIncrease(int affinityLevel) {
        double increase = getHotBaseDamageIncrease() - (affinityLevel * getHotAffinityCoefficient());
        return Math.max(getHotMinDamageIncrease(), increase) / 100.0;
    }
    
    /**
     * 回復ボーナスの持続時間を計算（ティック単位）
     */
    public int calculateRecoveryDuration(int affinityLevel) {
        double seconds = getRecoveryBaseDuration() + (affinityLevel * getRecoveryDurationCoefficient());
        return (int)(seconds * 20); // 秒をティックに変換
    }
    
    /**
     * 回復ボーナスの増加率を計算
     */
    public double calculateRecoveryIncrease(int affinityLevel) {
        double increase = affinityLevel * getRecoveryAmountCoefficient();
        return Math.min(getRecoveryMaxIncrease(), increase) / 100.0;
    }
    
    /**
     * 高親和度特典が有効かどうかを判定
     */
    public boolean isHighAffinityActive(int affinityLevel) {
        return affinityLevel >= getHighAffinityThreshold();
    }
    
    /**
     * バイオーム温度判定
     */
    public boolean isColdBiome(float temperature) {
        return temperature <= getColdTemperatureThreshold();
    }
    
    public boolean isHotBiome(float temperature) {
        return temperature >= getHotTemperatureThreshold();
    }
    
    // === 基底設定へのアクセス（必要時のみ） ===
    
    /**
     * 基底OriginConfigへの直接アクセス（高度な用途）
     */
    public OriginConfig getBaseConfig() {
        return baseConfig;
    }
    
    /**
     * 設定の概要情報を取得
     */
    public String getConfigSummary() {
        return String.format("Patricia Config v%s: %s", 
            baseConfig.version, baseConfig.description);
    }
    
    /**
     * 設定値を動的に取得（拡張用）
     * ConfigManagerからデフォルト値を取得、数値はここに書かない
     */
    public double getCustomDouble(String key) {
        return baseConfig.getDouble(key, getDefaultDouble(key));
    }
    
    public int getCustomInt(String key) {
        return baseConfig.getInt(key, getDefaultInt(key));
    }
    
    public boolean getCustomBoolean(String key) {
        return baseConfig.getBoolean(key, getDefaultBoolean(key));
    }
    
    public String getCustomString(String key) {
        return baseConfig.getString(key, getDefaultString(key));
    }
}