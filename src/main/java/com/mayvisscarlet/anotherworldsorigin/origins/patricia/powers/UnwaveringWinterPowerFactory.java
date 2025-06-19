package com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.edwinmindcraft.apoli.api.IDynamicFeatureConfiguration;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import java.util.UUID;

/**
 * パトリシアの「揺らぐ事なき冬」パワーファクトリー
 * Origins Forge APIの正しい実装方式に従った独自Power
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
    public record Configuration(
        boolean attackSpeedImmunity,
        boolean coldBiomeDefense,
        boolean affinityAttackBonus,
        boolean attackSpeedCompensation
    ) implements IDynamicFeatureConfiguration {
        
        @Override
        public String getDescription() {
            return "Patricia's Unwavering Winter passive ability";
        }
    }
    
    /**
     * Powerがティックごとに実行する処理
     */
    @Override
    public void serverTick(Entity entity, ConfiguredPower<Configuration, ?> power) {
        if (!(entity instanceof Player player)) return;
        
        Configuration config = power.getConfiguration();
        
        // 5秒ごとに攻撃力を更新（パフォーマンス配慮）
        if (player.tickCount % 100 == 0) {
            updateAttackPowerBonus(player, config);
        }
        
        // 10秒ごとに攻撃速度低下をクリーンアップ
        if (config.attackSpeedImmunity() && player.tickCount % 200 == 0) {
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
                AnotherWorldsOrigin.LOGGER.debug("Patricia {} cleaned up DIG_SLOWDOWN during tick", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * Powerが追加された時の処理
     */
    @Override
    public void onAdded(Entity entity, ConfiguredPower<Configuration, ?> power) {
        if (!(entity instanceof Player player)) return;
        
        Configuration config = power.getConfiguration();
        
        // 既存の攻撃速度低下を除去
        if (config.attackSpeedImmunity() && player.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN)) {
            player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
            AnotherWorldsOrigin.LOGGER.info("Patricia {} initial cleanup: removed DIG_SLOWDOWN", 
                player.getDisplayName().getString());
        }
        
        // 攻撃力ボーナスを即座に適用
        updateAttackPowerBonus(player, config);
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter power activated", 
            player.getDisplayName().getString());
    }
    
    /**
     * Powerが除去された時の処理
     */
    @Override
    public void onRemoved(Entity entity, ConfiguredPower<Configuration, ?> power) {
        if (!(entity instanceof Player player)) return;
        
        // 攻撃力修飾子を除去
        var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
            attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
        }
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter power deactivated", 
            player.getDisplayName().getString());
    }
    
    /**
     * 親和度に基づいた攻撃力ボーナスを更新
     */
    private void updateAttackPowerBonus(Player player, Configuration config) {
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int affinityLevel = affinityData.getAffinityData().getAffinityLevel();
            
            // 現在の攻撃速度を取得
            double currentAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            
            var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                // 既存の修飾子を除去
                attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
                attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
                
                // 親和度による基本攻撃力上昇
                if (config.affinityAttackBonus()) {
                    double affinityBonus = PatriciaConstants.calculateAffinityAttackBonus(affinityLevel);
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
                    double speedCompensation = PatriciaConstants.calculateAttackSpeedCompensation(affinityLevel, currentAttackSpeed);
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
     * ダメージ軽減の計算（Cold系バイオーム）
     */
    public float calculateDamageReduction(Player player, Configuration config) {
        if (!config.coldBiomeDefense()) return 1.0f;
        
        // バイオーム温度を取得
        Biome biome = player.level().getBiome(player.blockPosition()).value();
        float temperature = biome.getBaseTemperature();
        
        if (PatriciaConstants.isColdBiome(temperature)) {
            return AffinityCapability.getAffinityData(player)
                .map(affinityData -> {
                    int affinityLevel = affinityData.getAffinityData().getAffinityLevel();
                    double reduction = PatriciaConstants.calculateColdDamageReduction(affinityLevel);
                    return (float)(1.0 - reduction);
                })
                .orElse(1.0f);
        }
        
        return 1.0f;
    }
    
    /**
     * ポーション効果無効化の判定
     */
    public boolean shouldBlockEffect(Player player, Configuration config) {
        return config.attackSpeedImmunity();
    }
}