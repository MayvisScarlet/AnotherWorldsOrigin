package com.mayvisscarlet.anotherworldsorigin.origins.patricia.abilities;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * パトリシアの「揺らぐ事なき冬」能力の一部
 * 攻撃速度低下効果を無効化する
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class AttackSpeedImmunity {
    
    /**
     * ポーション効果の適用判定時に攻撃速度低下を阻止
     */
    @SubscribeEvent
    public static void onPotionEffectApplicable(MobEffectEvent.Applicable event) {
        // プレイヤーかつパトリシアかチェック
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 採掘速度低下（攻撃速度にも影響）を無効化
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            event.setResult(Event.Result.DENY);
            
            AnotherWorldsOrigin.LOGGER.debug("Patricia {} blocked DIG_SLOWDOWN effect application", 
                player.getDisplayName().getString());
            
            // プレイヤーに通知（デバッグ用・将来的にはOFF可能に）
            if (shouldShowDebugMessages()) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b[Patricia] §7Attack speed reduction blocked"), 
                    true
                );
            }
        }
    }
    
    /**
     * ポーション効果が追加された後に即座に除去
     * Applicable で阻止できなかった場合のフォールバック
     */
    @SubscribeEvent
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        // プレイヤーかつパトリシアかチェック
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 採掘速度低下が追加された場合は即座に除去
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            // 次のティックで除去（同じティック内での除去は競合状態を避けるため）
            player.getServer().execute(() -> {
                if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                    
                    AnotherWorldsOrigin.LOGGER.debug("Patricia {} removed DIG_SLOWDOWN effect after addition", 
                        player.getDisplayName().getString());
                    
                    if (shouldShowDebugMessages()) {
                        player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§b[Patricia] §7Attack speed reduction removed"), 
                            true
                        );
                    }
                }
            });
        }
    }
    
    /**
     * プレイヤーがパトリシア種族になった時に既存の効果を除去
     * （種族変更時やログイン時のクリーンアップ）
     */
    public static void onPatriciaActivated(Player player) {
        if (!OriginHelper.isPatricia(player)) {
            return;
        }
        
        // 既存の採掘速度低下効果を除去
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            
            AnotherWorldsOrigin.LOGGER.debug("Patricia {} cleaned up existing DIG_SLOWDOWN effect", 
                player.getDisplayName().getString());
            
            if (shouldShowDebugMessages()) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b[Patricia] §7Existing attack speed reduction removed"), 
                    false
                );
            }
        }
    }
    
    /**
     * デバッグメッセージを表示するかどうか
     * 将来的にはJSON設定から取得
     */
    private static boolean shouldShowDebugMessages() {
        // TODO: 設定ファイルから取得
        return false; // 本番では false、デバッグ時は true
    }
    
    /**
     * 手動テスト用メソッド
     * コマンドから呼び出し可能
     */
    public static void testAttackSpeedImmunity(Player player) {
        if (!OriginHelper.isPatricia(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cThis test is only for Patricia origin users"));
            return;
        }
        
        AnotherWorldsOrigin.LOGGER.info("Testing attack speed immunity for {}", 
            player.getDisplayName().getString());
        
        // 現在の状態を記録
        boolean hadEffectBefore = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // テスト用の採掘速度低下を適用を試みる
        net.minecraft.world.effect.MobEffectInstance slowdown = 
            new net.minecraft.world.effect.MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1);
        
        boolean addEffectResult = player.addEffect(slowdown);
        
        // 適用後の状態をチェック
        boolean hasEffectAfter = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // 結果を判定
        if (!hadEffectBefore && !hasEffectAfter) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a[Test Passed] Attack speed reduction was successfully blocked"));
        } else if (hadEffectBefore && !hasEffectAfter) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a[Test Passed] Existing attack speed reduction was removed"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c[Test Failed] Attack speed reduction was not blocked (addEffect: " + addEffectResult + ")"));
        }
        
        AnotherWorldsOrigin.LOGGER.info("Test result for {}: before={}, addResult={}, after={}", 
            player.getDisplayName().getString(), hadEffectBefore, addEffectResult, hasEffectAfter);
    }
}