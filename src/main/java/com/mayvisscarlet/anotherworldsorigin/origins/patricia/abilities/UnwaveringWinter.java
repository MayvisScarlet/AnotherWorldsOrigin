package com.mayvisscarlet.anotherworldsorigin.origins.patricia.abilities;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * パトリシアの「揺らぐ事なき冬」パッシブ能力
 * 完全イベント駆動による実装（PatriciaConstants削除対応版）
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class UnwaveringWinter {
    
    // デフォルト設定（パトリシア種族用）
    private static final UnwaveringWinterPowerFactory.Configuration DEFAULT_CONFIG = 
        new UnwaveringWinterPowerFactory.Configuration(true, true, true, true);
    
    // パトリシア種族チェックのキャッシュ（60秒間有効）
    private static final Map<UUID, Long> patriciaCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 60000; // 60秒
    
    // 攻撃力変更の監視用キャッシュ
    private static final Map<UUID, Double> lastAttackDamageCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> lastAttackSpeedCache = new ConcurrentHashMap<>();
    
    /**
     * パトリシア種族判定（キャッシュ付き）
     */
    private static boolean isPatriciaOptimized(Player player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        
        // キャッシュから確認
        Long cachedTime = patriciaCache.get(playerId);
        if (cachedTime != null && (currentTime - cachedTime) < CACHE_DURATION) {
            return true; // キャッシュヒット = パトリシア
        }
        
        // 実際の判定
        boolean isPatricia = OriginHelper.isPatricia(player);
        if (isPatricia) {
            patriciaCache.put(playerId, currentTime);
        } else {
            patriciaCache.remove(playerId); // 非パトリシアの場合はキャッシュから削除
        }
        
        return isPatricia;
    }
    
    /**
     * キャッシュクリーンアップ（古いエントリを削除）
     */
    private static void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        patriciaCache.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > CACHE_DURATION
        );
    }
    
    /**
     * 攻撃速度低下効果の適用を阻止
     */
    @SubscribeEvent
    public static void onPotionEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 採掘速度低下（攻撃速度にも影響）の場合のみ詳細チェック
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            if (isPatriciaOptimized(player)) {
                event.setResult(Event.Result.DENY);
                
                AnotherWorldsOrigin.LOGGER.debug("Patricia {} blocked DIG_SLOWDOWN effect", 
                    player.getDisplayName().getString());
                
                if (ConfigManager.getPatriciaConfig().shouldShowDebugMessages()) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§b[揺らぐ事なき冬] §7攻撃速度低下を無効化"), 
                        true
                    );
                }
            }
        }
    }
    
    /**
     * 追加された攻撃速度低下効果を即座に除去（フォールバック）
     */
    @SubscribeEvent
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            if (isPatriciaOptimized(player)) {
                // 次のティックで除去
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                            player.removeEffect(MobEffects.DIG_SLOWDOWN);
                            
                            AnotherWorldsOrigin.LOGGER.debug("Patricia {} removed DIG_SLOWDOWN after addition", 
                                player.getDisplayName().getString());
                            
                            if (ConfigManager.getPatriciaConfig().shouldShowDebugMessages()) {
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
    }
    
    /**
     * Cold系バイオームでのダメージ軽減（ログ表示修正版）
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!isPatriciaOptimized(player)) {
            return;
        }
        
        float damageReduction = UnwaveringWinterPowerFactory.calculateDamageReduction(player, DEFAULT_CONFIG);
        
        if (damageReduction < 1.0f) {
            float originalDamage = event.getAmount();
            float newDamage = originalDamage * damageReduction;
            event.setAmount(newDamage);
            
            // 修正されたログ表示（String.formatを使用）
            float reductionPercent = (1.0f - damageReduction) * 100;
            AnotherWorldsOrigin.LOGGER.info("Patricia {} cold biome damage reduction: {} -> {} ({}% reduced)", 
                player.getDisplayName().getString(), 
                String.format("%.2f", originalDamage),
                String.format("%.2f", newDamage), 
                String.format("%.1f", reductionPercent));
            
            // プレイヤーに視覚的フィードバック
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("§b[冬の恩恵] §7ダメージ軽減: %.1f → %.1f (§a%.1f%%§7軽減)", 
                        originalDamage, newDamage, reductionPercent)
                ), 
                true
            );
        }
    }
    
    /**
     * プレイヤーログイン時の初期化
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        
        if (isPatriciaOptimized(player)) {
            // ログイン時に攻撃力ボーナスを適用
            UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
            
            // 現在の攻撃力・攻撃速度をキャッシュに記録
            cacheCurrentAttributes(player);
            
            // 既存の攻撃速度低下を除去
            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
                AnotherWorldsOrigin.LOGGER.info("Patricia {} login cleanup: removed DIG_SLOWDOWN", 
                    player.getDisplayName().getString());
            }
            
            AnotherWorldsOrigin.LOGGER.info("Patricia {} logged in - abilities initialized", 
                player.getDisplayName().getString());
        }
    }
    
    /**
     * 装備変更時の攻撃力更新
     */
    @SubscribeEvent
    public static void onItemChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // メインハンドまたはオフハンドの装備変更時のみ処理
        if (event.getSlot() == EquipmentSlot.MAINHAND || 
            event.getSlot() == EquipmentSlot.OFFHAND) {
            
            if (isPatriciaOptimized(player)) {
                // 次のティックで攻撃力を更新（装備変更の完了を待つ）
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
                        cacheCurrentAttributes(player);
                        
                        AnotherWorldsOrigin.LOGGER.debug("Patricia {} attack bonus updated due to equipment change", 
                            player.getDisplayName().getString());
                    });
                }
            }
        }
    }
    
    /**
     * プレイヤーのディメンション移動時
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        
        if (isPatriciaOptimized(player)) {
            // ディメンション移動後に攻撃力ボーナスを再適用
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
                    cacheCurrentAttributes(player);
                    
                    AnotherWorldsOrigin.LOGGER.debug("Patricia {} attack bonus updated after dimension change", 
                        player.getDisplayName().getString());
                });
            }
        }
    }
    
    /**
     * 軽量なティック処理（Attribute変更の監視とクリーンアップ）
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // パトリシア判定を最初に行い、以降の処理をスキップ
        if (!isPatriciaOptimized(player)) {
            return;
        }
        
        // 10秒ごとにAttribute変更をチェック（効率的な監視）
        if (player.tickCount % 200 == 0) {
            checkAttributeChanges(player);
        }
        
        // 攻撃速度低下のフォールバッククリーンアップ（2分ごと）
        if (player.tickCount % 2400 == 0) {
            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                player.removeEffect(MobEffects.DIG_SLOWDOWN);
                AnotherWorldsOrigin.LOGGER.debug("Patricia {} fallback cleanup: removed DIG_SLOWDOWN", 
                    player.getDisplayName().getString());
            }
        }
        
        // 5分ごとにキャッシュクリーンアップ
        if (player.tickCount % 6000 == 0) {
            cleanupCache();
        }
    }
    
    /**
     * 現在のAttribute値をキャッシュに記録
     */
    private static void cacheCurrentAttributes(Player player) {
        UUID playerId = player.getUUID();
        double attackDamage = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        double attackSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        
        lastAttackDamageCache.put(playerId, attackDamage);
        lastAttackSpeedCache.put(playerId, attackSpeed);
    }
    
    /**
     * Attribute変更をチェックして、変更があれば攻撃力ボーナスを更新
     */
    private static void checkAttributeChanges(Player player) {
        UUID playerId = player.getUUID();
        double currentAttackDamage = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        double currentAttackSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        
        Double lastAttackDamage = lastAttackDamageCache.get(playerId);
        Double lastAttackSpeed = lastAttackSpeedCache.get(playerId);
        
        // 攻撃力または攻撃速度に変更があった場合
        boolean attackDamageChanged = lastAttackDamage == null || Math.abs(currentAttackDamage - lastAttackDamage) > 0.01;
        boolean attackSpeedChanged = lastAttackSpeed == null || Math.abs(currentAttackSpeed - lastAttackSpeed) > 0.01;
        
        if (attackDamageChanged || attackSpeedChanged) {
            AnotherWorldsOrigin.LOGGER.debug("Patricia {} attribute change detected: damage {} -> {}, speed {} -> {}", 
                player.getDisplayName().getString(),
                lastAttackDamage != null ? String.format("%.2f", lastAttackDamage) : "null",
                String.format("%.2f", currentAttackDamage),
                lastAttackSpeed != null ? String.format("%.2f", lastAttackSpeed) : "null",
                String.format("%.2f", currentAttackSpeed));
            
            // 攻撃力ボーナスを更新
            UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
            
            // 新しい値をキャッシュ
            cacheCurrentAttributes(player);
        }
    }
    
    /**
     * 親和度レベルアップ時の特別処理（IntegratedEventHandlerから呼び出し）
     */
    public static void onAffinityLevelUp(Player player, int newLevel, int oldLevel) {
        if (!isPatriciaOptimized(player)) {
            return;
        }
        
        // 攻撃力ボーナスを即座に更新
        UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
        cacheCurrentAttributes(player);
        
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        // マイルストーン達成時の特別メッセージ
        if (newLevel == patriciaConfig.getHighAffinityThreshold()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "§l§b[Patricia] §r§f高親和度に到達！冬の力がさらに強くなった"
                )
            );
            showAttackPowerIncrease(player, oldLevel, newLevel);
            
        } else if (newLevel == patriciaConfig.getAdvancedThreshold()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "§l§6[Patricia] §r§f上級レベルに到達！真の氷の戦士となった"
                )
            );
            showAttackPowerIncrease(player, oldLevel, newLevel);
        }
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} affinity level up: {} -> {} (attack bonus updated immediately)", 
            player.getDisplayName().getString(), oldLevel, newLevel);
    }
    
    /**
     * 攻撃力増加の詳細表示
     */
    private static void showAttackPowerIncrease(Player player, int oldLevel, int newLevel) {
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        double oldBonus = patriciaConfig.calculateAffinityAttackBonus(oldLevel);
        double newBonus = patriciaConfig.calculateAffinityAttackBonus(newLevel);
        double increase = newBonus - oldBonus;
        
        if (increase > 0) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("§e[攻撃力上昇] §a+%.2f §7(総計: +%.2f)", increase, newBonus)
                )
            );
        }
    }
    
    /**
     * プレイヤーがパトリシア種族になった時の初期化（手動呼び出し用）
     */
    public static void onPatriciaActivated(Player player) {
        if (!isPatriciaOptimized(player)) {
            AnotherWorldsOrigin.LOGGER.warn("Attempted to activate Patricia abilities for non-Patricia player: {}", 
                player.getDisplayName().getString());
            return;
        }
        
        // 既存の攻撃速度低下を除去
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            AnotherWorldsOrigin.LOGGER.info("Patricia {} initial cleanup: removed DIG_SLOWDOWN", 
                player.getDisplayName().getString());
        }
        
        // 攻撃力ボーナスを即座に適用
        UnwaveringWinterPowerFactory.updateAttackPowerBonus(player, DEFAULT_CONFIG);
        cacheCurrentAttributes(player);
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter passive activated", 
            player.getDisplayName().getString());
        
        // プレイヤーに通知
        player.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("§b[Patricia] §f揺らぐ事なき冬が発動しました")
        );
        
        // 現在の攻撃力ボーナスを表示
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            if (level > 0) {
                double bonus = ConfigManager.getPatriciaConfig().calculateAffinityAttackBonus(level);
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        String.format("§e[攻撃力ボーナス] §a+%.2f §7(親和度Lv.%d)", bonus, level)
                    )
                );
            }
        });
    }
    
    /**
     * プレイヤーがパトリシア種族でなくなった時のクリーンアップ
     */
    public static void onPatriciaDeactivated(Player player) {
        UUID playerId = player.getUUID();
        
        // 攻撃力修飾子を除去
        UnwaveringWinterPowerFactory.cleanupAttackModifiers(player);
        
        // 各種キャッシュからも除去
        patriciaCache.remove(playerId);
        lastAttackDamageCache.remove(playerId);
        lastAttackSpeedCache.remove(playerId);
        
        AnotherWorldsOrigin.LOGGER.info("Patricia {} Unwavering Winter passive deactivated", 
            player.getDisplayName().getString());
    }
}