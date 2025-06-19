package com.mayvisscarlet.anotherworldsorigin.origins.patricia;

import com.mayvisscarlet.anotherworldsorigin.config.OriginConfig;

/**
 * パトリシア専用設定クラス
 * 型安全なアクセサを提供
 */
public class PatriciaOriginConfig {
    private final OriginConfig config;
    
    public PatriciaOriginConfig(OriginConfig config) {
        this.config = config;
    }
    
    // === 攻撃力計算式 ===
    public double getAffinityAttackCoefficient() {
        return config.getDouble("unwavering_winter.attack_power.affinity_coefficient", 0.4);
    }
    
    public double getBaseCoefficient() {
        return config.getDouble("unwavering_winter.attack_power.base_coefficient", 0.1);
    }
    
    public double getAttackSpeedMultiplier() {
        return config.getDouble("unwavering_winter.attack_power.attack_speed_multiplier", 1.5);
    }
    
    public double getBaseValue() {
        return config.getDouble("unwavering_winter.attack_power.base_value", 4.0);
    }
    
    // === Cold系バイオーム ===
    public double getColdBaseDamageReduction() {
        return config.getDouble("unwavering_winter.cold_biome_benefits.base_damage_reduction", 15.0);
    }
    
    public double getColdAffinityCoefficient() {
        return config.getDouble("unwavering_winter.cold_biome_benefits.affinity_coefficient", 0.35);
    }
    
    public double getColdMaxDamageReduction() {
        return config.getDouble("unwavering_winter.cold_biome_benefits.max_damage_reduction", 25.0);
    }
    
    public double getColdTemperatureThreshold() {
        return config.getDouble("unwavering_winter.cold_biome_benefits.temperature_threshold", 0.2);
    }
    
    // === 火炎ダメージ ===
    public double getFireBaseMultiplier() {
        return config.getDouble("heat_vulnerability.fire_damage.base_multiplier", 2.0);
    }
    
    public double getFireMinMultiplier() {
        return config.getDouble("heat_vulnerability.fire_damage.min_multiplier", 1.5);
    }
    
    public double getFireAffinityReduction() {
        return config.getDouble("heat_vulnerability.fire_damage.affinity_reduction", 0.01);
    }
    
    // === Hot系バイオーム ===
    public double getHotBaseDamageIncrease() {
        return config.getDouble("heat_vulnerability.hot_biome_penalties.base_damage_increase", 25.0);
    }
    
    public double getHotAffinityCoefficient() {
        return config.getDouble("heat_vulnerability.hot_biome_penalties.affinity_coefficient", 0.8);
    }
    
    public double getHotMinDamageIncrease() {
        return config.getDouble("heat_vulnerability.hot_biome_penalties.min_damage_increase", 0.0);
    }
    
    public double getHotTemperatureThreshold() {
        return config.getDouble("heat_vulnerability.hot_biome_penalties.temperature_threshold", 1.0);
    }
    
    // === 回復ボーナス ===
    public double getRecoveryBaseDuration() {
        return config.getDouble("heat_vulnerability.recovery_bonus.base_duration", 5.0);
    }
    
    public double getRecoveryDurationCoefficient() {
        return config.getDouble("heat_vulnerability.recovery_bonus.duration_coefficient", 0.5);
    }
    
    public double getRecoveryAmountCoefficient() {
        return config.getDouble("heat_vulnerability.recovery_bonus.amount_coefficient", 2.0);
    }
    
    public double getRecoveryMaxIncrease() {
        return config.getDouble("heat_vulnerability.recovery_bonus.max_increase", 100.0);
    }
    
    // === マイルストーン ===
    public int getHighAffinityThreshold() {
        return config.getInt("growth_system.milestones.high_affinity_threshold", 30);
    }
    
    public int getAdvancedThreshold() {
        return config.getInt("growth_system.milestones.advanced_threshold", 50);
    }
    
    // === 計算メソッド ===
    
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
}