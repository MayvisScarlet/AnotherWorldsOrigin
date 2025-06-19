package com.mayvisscarlet.anotherworldsorigin.origins.patricia;

import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaOriginConfig;

/**
 * パトリシアの能力に関する数値を管理するクラス（JSON設定対応版）
 * 設定ファイルから動的に値を取得し、フォールバック機能を提供
 */
public class PatriciaConstants {
    
    // 設定取得用のインスタンス（遅延初期化）
    private static PatriciaOriginConfig config;
    
    /**
     * 設定インスタンスを取得（遅延初期化）
     */
    private static PatriciaOriginConfig getConfig() {
        if (config == null) {
            config = ConfigManager.getPatriciaConfig();
        }
        return config;
    }
    
    /**
     * 設定を強制リロード
     */
    public static void reloadConfig() {
        config = null;
        ConfigManager.reloadAllConfigs();
    }
    
    // === 攻撃力計算式の係数（JSON設定対応） ===
    
    /**
     * 親和度による攻撃力上昇係数
     */
    public static double getAffinityAttackCoefficient() {
        return getConfig().getAffinityAttackCoefficient();
    }
    
    /**
     * 親和度による基準値上昇係数
     */
    public static double getAffinityBaseCoefficient() {
        return getConfig().getBaseCoefficient();
    }
    
    /**
     * 攻撃速度差を攻撃力に変換する倍率
     */
    public static double getAttackSpeedMultiplier() {
        return getConfig().getAttackSpeedMultiplier();
    }
    
    /**
     * 基準値
     */
    public static double getBaseValue() {
        return getConfig().getBaseValue();
    }
    
    // === Cold系バイオームでのダメージ軽減（JSON設定対応） ===
    
    /**
     * Cold系バイオームでの基本ダメージ軽減率（％）
     */
    public static double getColdBaseDamageReduction() {
        return getConfig().getColdBaseDamageReduction();
    }
    
    /**
     * 親和度によるダメージ軽減率上昇係数
     */
    public static double getColdAffinityCoefficient() {
        return getConfig().getColdAffinityCoefficient();
    }
    
    /**
     * Cold系バイオームでの最大ダメージ軽減率（％）
     */
    public static double getColdMaxDamageReduction() {
        return getConfig().getColdMaxDamageReduction();
    }
    
    // === 火炎・溶岩ダメージ増加（JSON設定対応） ===
    
    /**
     * 親和度0時の火炎ダメージ倍率
     */
    public static double getFireBaseDamageMultiplier() {
        return getConfig().getFireBaseMultiplier();
    }
    
    /**
     * 火炎ダメージの最小倍率
     */
    public static double getFireMinDamageMultiplier() {
        return getConfig().getFireMinMultiplier();
    }
    
    /**
     * 親和度による火炎ダメージ軽減係数
     */
    public static double getFireAffinityReduction() {
        return getConfig().getFireAffinityReduction();
    }
    
    // === Hot系バイオームでのダメージ増加（JSON設定対応） ===
    
    /**
     * Hot系バイオームでの基本ダメージ増加率（％）
     */
    public static double getHotBaseDamageIncrease() {
        return getConfig().getHotBaseDamageIncrease();
    }
    
    /**
     * 親和度によるHot系ダメージ増加軽減係数
     */
    public static double getHotAffinityCoefficient() {
        return getConfig().getHotAffinityCoefficient();
    }
    
    /**
     * Hot系バイオームでのダメージ増加率下限（％）
     */
    public static double getHotMinDamageIncrease() {
        return getConfig().getHotMinDamageIncrease();
    }
    
    // === 回復ボーナスシステム（JSON設定対応） ===
    
    /**
     * 回復ボーナスの基本持続時間（秒）
     */
    public static double getRecoveryBaseDuration() {
        return getConfig().getRecoveryBaseDuration();
    }
    
    /**
     * 親和度による持続時間延長係数
     */
    public static double getRecoveryDurationCoefficient() {
        return getConfig().getRecoveryDurationCoefficient();
    }
    
    /**
     * 親和度による回復量増加係数
     */
    public static double getRecoveryAmountCoefficient() {
        return getConfig().getRecoveryAmountCoefficient();
    }
    
    /**
     * 回復ボーナスの最大増加率（％）
     */
    public static double getRecoveryMaxIncrease() {
        return getConfig().getRecoveryMaxIncrease();
    }
    
    // === 高親和度特典（JSON設定対応） ===
    
    /**
     * 高親和度特典の発動条件（親和度レベル）
     */
    public static int getHighAffinityThreshold() {
        return getConfig().getHighAffinityThreshold();
    }
    
    /**
     * 上級レベルの閾値
     */
    public static int getAdvancedThreshold() {
        return getConfig().getAdvancedThreshold();
    }
    
    // === バイオーム判定基準（JSON設定対応） ===
    
    /**
     * Cold系バイオームと判定する温度の上限
     */
    public static float getColdBiomeTemperature() {
        return (float) getConfig().getColdTemperatureThreshold();
    }
    
    /**
     * Hot系バイオームと判定する温度の下限
     */
    public static float getHotBiomeTemperature() {
        return (float) getConfig().getHotTemperatureThreshold();
    }
    
    // === 計算用メソッド（JSON設定対応） ===
    
    /**
     * 親和度に基づいた攻撃力上昇量を計算
     */
    public static double calculateAffinityAttackBonus(int affinityLevel) {
        return getConfig().calculateAffinityAttackBonus(affinityLevel);
    }
    
    /**
     * 親和度と攻撃速度に基づいた攻撃力補正を計算
     */
    public static double calculateAttackSpeedCompensation(int affinityLevel, double currentAttackSpeed) {
        return getConfig().calculateAttackSpeedCompensation(affinityLevel, currentAttackSpeed);
    }
    
    /**
     * Cold系バイオームでのダメージ軽減率を計算
     */
    public static double calculateColdDamageReduction(int affinityLevel) {
        return getConfig().calculateColdDamageReduction(affinityLevel);
    }
    
    /**
     * 火炎ダメージの倍率を計算
     */
    public static double calculateFireDamageMultiplier(int affinityLevel) {
        return getConfig().calculateFireDamageMultiplier(affinityLevel);
    }
    
    /**
     * Hot系バイオームでのダメージ増加率を計算
     */
    public static double calculateHotDamageIncrease(int affinityLevel) {
        return getConfig().calculateHotDamageIncrease(affinityLevel);
    }
    
    /**
     * 回復ボーナスの持続時間を計算（ティック単位）
     */
    public static int calculateRecoveryDuration(int affinityLevel) {
        return getConfig().calculateRecoveryDuration(affinityLevel);
    }
    
    /**
     * 回復ボーナスの増加率を計算
     */
    public static double calculateRecoveryIncrease(int affinityLevel) {
        return getConfig().calculateRecoveryIncrease(affinityLevel);
    }
    
    /**
     * 高親和度特典が有効かどうかを判定
     */
    public static boolean isHighAffinityActive(int affinityLevel) {
        return getConfig().isHighAffinityActive(affinityLevel);
    }
    
    /**
     * デバッグメッセージを表示するかどうか
     */
    public static boolean shouldShowDebugMessages() {
        return getConfig().getBoolean("debug.show_passive_messages", false);
    }
    
    /**
     * バイオーム温度による判定
     */
    public static boolean isColdBiome(float temperature) {
        return getConfig().isColdBiome(temperature);
    }
    
    public static boolean isHotBiome(float temperature) {
        return getConfig().isHotBiome(temperature);
    }
}