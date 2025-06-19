package com.mayvisscarlet.anotherworldsorigin.origins.patricia.abilities;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaConstants;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * パトリシアの「揺らぐ事なき冬」パッシブ能力
 * - 攻撃速度低下無効化
 * - 親和度による攻撃力上昇
 * - 攻撃速度補償システム
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class UnwaveringWinter {
    
    // 攻撃力上昇用のUUID（重複適用防止）
    private static final UUID AFFINITY_ATTACK_BONUS_UUID = UUID.fromString("12345678-1234-5678-9012-123456789abc");
    private static final UUID ATTACK_SPEED_COMPENSATION_UUID = UUID.fromString("87654321-4321-8765-2109-987654321cba");
    
    /**
     * 攻撃速度低下効果の適用を阻止
     */
    @SubscribeEvent
    public static void onPotionEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 採掘速度低下（攻撃速度にも影響）を無効化
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            event.setResult(Event.Result.DENY);
            
            AnotherWorldsOrigin.LOGGER.debug("Patricia {} blocked DIG_SLOWDOWN effect", 
                player.getDisplayName().getString());
            
            if (PatriciaConstants.shouldShowDebugMessages()) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b[揺らぐ事なき冬] §7攻撃速度低下を無効化"), 
                    true
                );
            }
        }
    }
    
    /**
     * 追加された攻撃速度低下効果を即座に除去
     */
    @SubscribeEvent
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            // 次のティックで除去
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                        player.removeEffect(MobEffects.DIG_SLOWDOWN);
                        
                        AnotherWorldsOrigin.LOGGER.debug("Patricia {} removed DIG_SLOWDOWN after addition", 
                            player.getDisplayName().getString());
                        
                        if (PatriciaConstants.shouldShowDebugMessages()) {
                            player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§b[揺らぐ事なき冬] §7攻撃速度低下を除去"), 
                                true
                            );
                        }
                    }
                });
            }
        }
    }
    
    /**
     * プレイヤーのティック処理で親和度による攻撃力強化を適用
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 5秒ごとに攻撃力を更新（パフォーマンス配慮）
        if (player.tickCount % 100 == 0) {
            updateAttackPowerBonus(player);
        }
        
        // 既存の攻撃速度低下効果をクリーンアップ（10秒ごと）
        if (player.tickCount % 200 == 0) {
            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
                AnotherWorldsOrigin.LOGGER.debug("Patricia {} cleaned up DIG_SLOWDOWN during tick", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * 親和度に基づいた攻撃力ボーナスを更新
     */
    private static void updateAttackPowerBonus(Player player) {
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int affinityLevel = affinityData.getAffinityData().getAffinityLevel();
            
            // 現在の攻撃速度を取得
            double currentAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            
            // 親和度による基本攻撃力上昇
            double affinityBonus = PatriciaConstants.calculateAffinityAttackBonus(affinityLevel);
            
            // 攻撃速度補償
            double speedCompensation = PatriciaConstants.calculateAttackSpeedCompensation(affinityLevel, currentAttackSpeed);
            
            // 合計攻撃力ボーナス
            double totalBonus = affinityBonus + speedCompensation;
            
            // 攻撃力属性を更新
            var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                // 既存の修飾子を除去
                attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
                attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
                
                // 新しい修飾子を追加
                if (affinityBonus > 0) {
                    AttributeModifier affinityModifier = new AttributeModifier(
                        AFFINITY_ATTACK_BONUS_UUID,
                        "Patricia Affinity Attack Bonus",
                        affinityBonus,
                        AttributeModifier.Operation.ADDITION
                    );
                    attackAttribute.addPermanentModifier(affinityModifier);
                }
                
                if (speedCompensation > 0) {
                    AttributeModifier compensationModifier = new AttributeModifier(
                        ATTACK_SPEED_COMPENSATION_UUID,
                        "Patricia Attack Speed Compensation",
                        speedCompensation,
                        AttributeModifier.Operation.ADDITION
                    );
                    attackAttribute.addPermanentModifier(compensationModifier);
                }
                
                // デバッグログ
                if (totalBonus > 0) {
                    AnotherWorldsOrigin.LOGGER.debug("Patricia {} attack bonus updated: affinity={:.2f}, compensation={:.2f}, total={:.2f}", 
                        player.getDisplayName().getString(), affinityBonus, speedCompensation, totalBonus);
                }
            }
        });
    }
    
    /**
     * プレイヤーがパトリシア種族になった時の初期化
     */
    public static void onPatriciaActivated(Player player) {
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 既存の攻撃速度低下を除去
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            AnotherWorldsOrigin.LOGGER.info("Patricia {} initial cleanup: removed DIG_SLOWDOWN", 
                player.getDisplayName().getString());
        }
        
        // 攻撃力ボーナスを即座に適用
        updateAttackPowerBonus(player);
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter passive activated", 
            player.getDisplayName().getString());
    }
    
    /**
     * プレイヤーがパトリシア種族でなくなった時のクリーンアップ
     */
    public static void onPatriciaDeactivated(Player player) {
        // 攻撃力修飾子を除去
        var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
            attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
        }
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter passive deactivated", 
            player.getDisplayName().getString());
    }
}