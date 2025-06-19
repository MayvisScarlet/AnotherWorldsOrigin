package com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.edwinmindcraft.apoli.api.IDynamicFeatureConfiguration;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * パトリシアの「揺らぐ事なき冬」パワーファクトリー
 * PatriciaConstants削除対応版
 */
public class UnwaveringWinterPowerFactory extends PowerFactory<UnwaveringWinterPowerFactory.Configuration> {
    
    private static final UUID AFFINITY_ATTACK_BONUS_UUID = UUID.fromString("12345678-1234-5678-9012-123456789abc");
    private static final UUID ATTACK_SPEED_COMPENSATION_UUID = UUID.fromString("87654321-4321-8765-2109-987654321cba");
    
    /**
     * Configuration用のCodec定義（標準Codecのみ使用）
     */
    public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.optionalFieldOf("attack_speed_immunity", true).forGetter(Configuration::attackSpeedImmunity),
            Codec.BOOL.optionalFieldOf("cold_biome_defense", true).forGetter(Configuration::coldBiomeDefense),
            Codec.BOOL.optionalFieldOf("affinity_attack_bonus", true).forGetter(Configuration::affinityAttackBonus),
            Codec.BOOL.optionalFieldOf("attack_speed_compensation", true).forGetter(Configuration::attackSpeedCompensation)
        ).apply(instance, Configuration::new)
    );
    
    public UnwaveringWinterPowerFactory() {
        super(CODEC);
    }
    
    /**
     * Power設定データクラス（IDynamicFeatureConfiguration実装）
     */
    public static record Configuration(
        boolean attackSpeedImmunity,
        boolean coldBiomeDefense,
        boolean affinityAttackBonus,
        boolean attackSpeedCompensation
    ) implements IDynamicFeatureConfiguration {
        // recordを使用することで自動的にgetterが生成される
        // IDynamicFeatureConfigurationは抽象メソッドなし
    }
    
    /**
     * 親和度に基づいた攻撃力ボーナスを静的に適用
     */
    public static void updateAttackPowerBonus(Player player, Configuration config) {
        if (player == null || config == null) return;
        
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int affinityLevel = affinityData.getAffinityData().getAffinityLevel();
            
            // 現在の攻撃速度を取得
            double currentAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            
            var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                // 既存の修飾子を除去
                attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
                attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
                
                var patriciaConfig = ConfigManager.getPatriciaConfig();
                
                // 親和度による基本攻撃力上昇
                if (config.affinityAttackBonus()) {
                    double affinityBonus = patriciaConfig.calculateAffinityAttackBonus(affinityLevel);
                    if (affinityBonus > 0) {
                        AttributeModifier affinityModifier = new AttributeModifier(
                            AFFINITY_ATTACK_BONUS_UUID,
                            "Patricia Affinity Attack Bonus",
                            affinityBonus,
                            AttributeModifier.Operation.ADDITION
                        );
                        attackAttribute.addPermanentModifier(affinityModifier);
                    }
                }
                
                // 攻撃速度補償
                if (config.attackSpeedCompensation()) {
                    double speedCompensation = patriciaConfig.calculateAttackSpeedCompensation(affinityLevel, currentAttackSpeed);
                    if (speedCompensation > 0) {
                        AttributeModifier compensationModifier = new AttributeModifier(
                            ATTACK_SPEED_COMPENSATION_UUID,
                            "Patricia Attack Speed Compensation",
                            speedCompensation,
                            AttributeModifier.Operation.ADDITION
                        );
                        attackAttribute.addPermanentModifier(compensationModifier);
                    }
                }
                
                AnotherWorldsOrigin.LOGGER.debug("Patricia {} attack bonus updated for affinity level {}", 
                    player.getDisplayName().getString(), affinityLevel);
            }
        });
    }
    
    /**
     * 攻撃力修飾子のクリーンアップ（静的メソッド）
     */
    public static void cleanupAttackModifiers(Player player) {
        if (player == null) return;
        
        var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
            attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
        }
        
        AnotherWorldsOrigin.LOGGER.debug("Patricia {} attack modifiers cleaned up", 
            player.getDisplayName().getString());
    }
    
    /**
     * ダメージ軽減の計算（静的メソッド）
     */
    public static float calculateDamageReduction(Player player, Configuration config) {
        if (player == null || config == null || !config.coldBiomeDefense()) {
            return 1.0f;
        }
        
        // バイオーム温度を取得
        var biome = player.level().getBiome(player.blockPosition()).value();
        float temperature = biome.getBaseTemperature();
        
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        if (patriciaConfig.isColdBiome(temperature)) {
            return AffinityCapability.getAffinityData(player)
                .map(affinityData -> {
                    int affinityLevel = affinityData.getAffinityData().getAffinityLevel();
                    double reduction = patriciaConfig.calculateColdDamageReduction(affinityLevel);
                    return (float)(1.0 - reduction);
                })
                .orElse(1.0f);
        }
        
        return 1.0f;
    }
    
    /**
     * ポーション効果無効化の判定（静的メソッド）
     */
    public static boolean shouldBlockEffect(Player player, Configuration config) {
        return config != null && config.attackSpeedImmunity();
    }
}