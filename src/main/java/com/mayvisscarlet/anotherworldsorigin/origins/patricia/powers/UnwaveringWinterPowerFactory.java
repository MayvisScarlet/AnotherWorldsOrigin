package com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.util.DebugDisplay;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.edwinmindcraft.apoli.api.IDynamicFeatureConfiguration;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * パトリシアの「揺らぐ事なき冬」PowerFactory
 * 攻撃速度無効化、Cold系優位性、親和度攻撃力ボーナスを管理
 */
public class UnwaveringWinterPowerFactory extends PowerFactory<UnwaveringWinterPowerFactory.Configuration> {
    
    private static final UUID AFFINITY_ATTACK_BONUS_UUID = UUID.fromString("12345678-1234-5678-9012-123456789abc");
    private static final UUID ATTACK_SPEED_COMPENSATION_UUID = UUID.fromString("87654321-4321-8765-2109-987654321cba");
    
    /**
     * Configuration用のCodec定義
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
     * Power設定データクラス
     */
    public static record Configuration(
        boolean attackSpeedImmunity,
        boolean coldBiomeDefense,
        boolean affinityAttackBonus,
        boolean attackSpeedCompensation
    ) implements IDynamicFeatureConfiguration {
    }
    
    // ========================================
    // プレイヤー状態管理システム
    // ========================================
    
    /**
     * プレイヤー状態管理の静的内部クラス
     */
    public static class PlayerStateManager {
        // パトリシア種族チェックのキャッシュ（60秒間有効）
        private static final Map<UUID, PatriciaCache> patriciaCache = new ConcurrentHashMap<>();
        private static final long CACHE_DURATION = 60000; // 60秒
        
        /**
         * パトリシア種族判定結果のキャッシュデータ
         */
        private static class PatriciaCache {
            final boolean isPatricia;
            final long timestamp;
            
            PatriciaCache(boolean isPatricia, long timestamp) {
                this.isPatricia = isPatricia;
                this.timestamp = timestamp;
            }
        }
        
        // 攻撃力変更の監視用キャッシュ
        private static final Map<UUID, Double> lastAttackDamageCache = new ConcurrentHashMap<>();
        private static final Map<UUID, Double> lastAttackSpeedCache = new ConcurrentHashMap<>();
        
        /**
         * パトリシア種族判定（キャッシュ付き・修正版）
         */
        public static boolean isPatriciaOptimized(Player player) {
            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();
            
            // キャッシュから確認
            PatriciaCache cachedData = patriciaCache.get(playerId);
            if (cachedData != null && (currentTime - cachedData.timestamp) < CACHE_DURATION) {
                return cachedData.isPatricia; // 実際の判定結果を返す
            }
            
            // 実際の判定
            boolean isPatricia = OriginHelper.isPatricia(player);
            
            // パトリシア/非パトリシア両方の結果をキャッシュ
            patriciaCache.put(playerId, new PatriciaCache(isPatricia, currentTime));
            
            DebugDisplay.debug("AFFINITY_CALCULATION", "Patricia check for %s: %s (cached)", 
                player.getDisplayName().getString(), isPatricia);
            
            return isPatricia;
        }
        
        /**
         * 現在のAttribute値をキャッシュに記録
         */
        public static void cacheCurrentAttributes(Player player) {
            UUID playerId = player.getUUID();
            double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double attackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            
            lastAttackDamageCache.put(playerId, attackDamage);
            lastAttackSpeedCache.put(playerId, attackSpeed);
        }
        
        /**
         * Attribute変更をチェックして、変更があれば攻撃力ボーナスを更新
         */
        public static boolean checkAttributeChanges(Player player) {
            UUID playerId = player.getUUID();
            double currentAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double currentAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            
            Double lastAttackDamage = lastAttackDamageCache.get(playerId);
            Double lastAttackSpeed = lastAttackSpeedCache.get(playerId);
            
            // 攻撃力または攻撃速度に変更があった場合
            boolean attackDamageChanged = lastAttackDamage == null || Math.abs(currentAttackDamage - lastAttackDamage) > 0.01;
            boolean attackSpeedChanged = lastAttackSpeed == null || Math.abs(currentAttackSpeed - lastAttackSpeed) > 0.01;
            
            return attackDamageChanged || attackSpeedChanged;
        }
        
        /**
         * キャッシュクリーンアップ（古いエントリを削除）
         */
        public static void cleanupCache() {
            long currentTime = System.currentTimeMillis();
            patriciaCache.entrySet().removeIf(entry -> 
                (currentTime - entry.getValue().timestamp) > CACHE_DURATION
            );
        }
        
        /**
         * プレイヤーがパトリシア種族でなくなった時のクリーンアップ
         */
        public static void onPatriciaDeactivated(Player player) {
            UUID playerId = player.getUUID();
            
            // 攻撃力修飾子を除去
            cleanupAttackModifiers(player);
            
            // 各種キャッシュからも除去
            patriciaCache.remove(playerId);
            lastAttackDamageCache.remove(playerId);
            lastAttackSpeedCache.remove(playerId);
            
            DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s Unwavering Winter passive deactivated", 
                player.getDisplayName().getString());
        }
    }
    
    // ========================================
    // 効果計算システム
    // ========================================
    
    /**
     * 効果計算の静的内部クラス
     */
    public static class EffectCalculator {
        
        /**
         * Cold系バイオームでのダメージ軽減の計算
         */
        public static float calculateColdDamageReduction(Player player, Configuration config) {
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
         * ポーション効果無効化の判定
         */
        public static boolean shouldBlockEffect(Player player, Configuration config) {
            return config != null && config.attackSpeedImmunity();
        }
        
        /**
         * 攻撃力増加の詳細表示
         */
        public static void showAttackPowerIncrease(Player player, int oldLevel, int newLevel) {
            var patriciaConfig = ConfigManager.getPatriciaConfig();
            double oldBonus = patriciaConfig.calculateAffinityAttackBonus(oldLevel);
            double newBonus = patriciaConfig.calculateAffinityAttackBonus(newLevel);
            double increase = newBonus - oldBonus;
            
            if (increase > 0) {
                DebugDisplay.info(player, "AFFINITY_CALCULATION", "§e[攻撃力上昇] §a+%.2f §7(総計: +%.2f)", increase, newBonus);
            }
        }
    }
    
    // ========================================
    // 静的機能メソッド（従来のAPI維持）
    // ========================================
    
    /**
     * 親和度に基づいた攻撃力ボーナスを適用
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
                
                DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Patricia %s attack bonus updated for affinity level %d", 
                    player.getDisplayName().getString(), affinityLevel);
            }
        });
    }
    
    /**
     * 攻撃力修飾子のクリーンアップ
     */
    public static void cleanupAttackModifiers(Player player) {
        if (player == null) return;
        
        var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(AFFINITY_ATTACK_BONUS_UUID);
            attackAttribute.removeModifier(ATTACK_SPEED_COMPENSATION_UUID);
        }
        
        DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Patricia %s attack modifiers cleaned up", 
            player.getDisplayName().getString());
    }
    
    /**
     * Cold系ダメージ軽減の計算（従来API）
     */
    public static float calculateDamageReduction(Player player, Configuration config) {
        return EffectCalculator.calculateColdDamageReduction(player, config);
    }
    
    /**
     * ポーション効果無効化の判定（従来API）
     */
    public static boolean shouldBlockEffect(Player player, Configuration config) {
        return EffectCalculator.shouldBlockEffect(player, config);
    }
    
    // ========================================
    // 公開API（他クラスから呼び出し用）
    // ========================================
    
    /**
     * プレイヤーがパトリシア種族になった時の初期化
     */
    public static void onPatriciaActivated(Player player) {
        if (!PlayerStateManager.isPatriciaOptimized(player)) {
            DebugDisplay.warn(player, "ATTACK_SPEED_IMMUNITY", "Attempted to activate Patricia abilities for non-Patricia player: %s", 
                player.getDisplayName().getString());
            return;
        }
        
        // デフォルト設定で初期化
        Configuration config = new Configuration(true, true, true, true);
        
        // 既存の攻撃速度低下を除去
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
            DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s initial cleanup: removed DIG_SLOWDOWN", 
                player.getDisplayName().getString());
        }
        
        // 攻撃力ボーナスを適用
        updateAttackPowerBonus(player, config);
        PlayerStateManager.cacheCurrentAttributes(player);
        
        DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s Unwavering Winter passive activated", 
            player.getDisplayName().getString());
        
        // プレイヤーに通知
        DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "§b[Patricia] §f揺らぐ事なき冬が発動しました");
        
        // 現在の攻撃力ボーナスを表示
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            if (level > 0) {
                double bonus = ConfigManager.getPatriciaConfig().calculateAffinityAttackBonus(level);
                DebugDisplay.info(player, "AFFINITY_CALCULATION", "§e[攻撃力ボーナス] §a+%.2f §7(親和度Lv.%d)", bonus, level);
            }
        });
    }
    
    /**
     * 親和度レベルアップ時の特別処理
     */
    public static void onAffinityLevelUp(Player player, int newLevel, int oldLevel) {
        if (!PlayerStateManager.isPatriciaOptimized(player)) {
            return;
        }
        
        // デフォルト設定で攻撃力ボーナスを更新
        Configuration config = new Configuration(true, true, true, true);
        updateAttackPowerBonus(player, config);
        PlayerStateManager.cacheCurrentAttributes(player);
        
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        // マイルストーン達成時の特別メッセージ
        if (newLevel == patriciaConfig.getHighAffinityThreshold()) {
            DebugDisplay.info(player, "AFFINITY_CALCULATION", "§l§b[Patricia] §r§f高親和度に到達！冬の力がさらに強くなった");
            EffectCalculator.showAttackPowerIncrease(player, oldLevel, newLevel);
            
        } else if (newLevel == patriciaConfig.getAdvancedThreshold()) {
            DebugDisplay.info(player, "AFFINITY_CALCULATION", "§l§6[Patricia] §r§f上級レベルに到達！真の氷の戦士となった");
            EffectCalculator.showAttackPowerIncrease(player, oldLevel, newLevel);
        }
        
        DebugDisplay.info(player, "AFFINITY_CALCULATION", "Patricia %s affinity level up: %d -> %d (attack bonus updated immediately)", 
            player.getDisplayName().getString(), oldLevel, newLevel);
    }
    
    // ========================================
    // イベントハンドラー
    // ========================================
    
    /**
     * 統合イベントハンドラーの静的内部クラス
     */
    @Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
    public static class EventHandler {
        
        // デフォルト設定（パトリシア種族用）
        private static final Configuration DEFAULT_CONFIG = 
            new Configuration(true, true, true, true);
        
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
                if (PlayerStateManager.isPatriciaOptimized(player)) {
                    event.setResult(Event.Result.DENY);
                    
                    DebugDisplay.debug(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s blocked DIG_SLOWDOWN effect", 
                        player.getDisplayName().getString());
                    
                    if (ConfigManager.getPatriciaConfig().shouldShowDebugMessages()) {
                        DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "§b[揺らぐ事なき冬] §7攻撃速度低下を無効化");
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
                if (PlayerStateManager.isPatriciaOptimized(player)) {
                    // 次のティックで除去
                    if (player.getServer() != null) {
                        player.getServer().execute(() -> {
                            if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                                player.removeEffect(MobEffects.DIG_SLOWDOWN);
                                
                                DebugDisplay.debug(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s removed DIG_SLOWDOWN after addition", 
                                    player.getDisplayName().getString());
                                
                                if (ConfigManager.getPatriciaConfig().shouldShowDebugMessages()) {
                                    DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "§b[揺らぐ事なき冬] §7攻撃速度低下を除去");
                                }
                            }
                        });
                    }
                }
            }
        }
        
        /**
         * Cold系バイオームでのダメージ軽減
         */
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            
            if (!PlayerStateManager.isPatriciaOptimized(player)) {
                return;
            }
            
            float damageReduction = EffectCalculator.calculateColdDamageReduction(player, DEFAULT_CONFIG);
            
            if (damageReduction < 1.0f) {
                float originalDamage = event.getAmount();
                float newDamage = originalDamage * damageReduction;
                event.setAmount(newDamage);
                
                // ログ表示
                float reductionPercent = (1.0f - damageReduction) * 100;
                DebugDisplay.info(player, "COLD_DAMAGE", "Patricia %s cold biome damage reduction: %.2f -> %.2f (%.1f%% reduced)", 
                    player.getDisplayName().getString(), 
                    originalDamage,
                    newDamage, 
                    reductionPercent);
                
                // プレイヤーに視覚的フィードバック
                DebugDisplay.info(player, "COLD_DAMAGE", "§b[冬の恩恵] §7ダメージ軽減: %.1f → %.1f (§a%.1f%%§7軽減)", 
                    originalDamage, newDamage, reductionPercent);
            }
        }
        
        /**
         * プレイヤーログイン時の初期化
         */
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            
            if (PlayerStateManager.isPatriciaOptimized(player)) {
                // ログイン時に攻撃力ボーナスを適用
                updateAttackPowerBonus(player, DEFAULT_CONFIG);
                
                // 現在の攻撃力・攻撃速度をキャッシュに記録
                PlayerStateManager.cacheCurrentAttributes(player);
                
                // 既存の攻撃速度低下を除去
                if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                    DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s login cleanup: removed DIG_SLOWDOWN", 
                        player.getDisplayName().getString());
                }
                
                DebugDisplay.info(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s logged in - abilities initialized", 
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
                
                if (PlayerStateManager.isPatriciaOptimized(player)) {
                    // 次のティックで攻撃力を更新
                    if (player.getServer() != null) {
                        player.getServer().execute(() -> {
                            updateAttackPowerBonus(player, DEFAULT_CONFIG);
                            PlayerStateManager.cacheCurrentAttributes(player);
                            
                            DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Patricia %s attack bonus updated due to equipment change", 
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
            
            if (PlayerStateManager.isPatriciaOptimized(player)) {
                // ディメンション移動後に攻撃力ボーナスを再適用
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        updateAttackPowerBonus(player, DEFAULT_CONFIG);
                        PlayerStateManager.cacheCurrentAttributes(player);
                        
                        DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Patricia %s attack bonus updated after dimension change", 
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
            if (!PlayerStateManager.isPatriciaOptimized(player)) {
                return;
            }
            
            // 10秒ごとにAttribute変更をチェック（効率的な監視）
            if (player.tickCount % 200 == 0) {
                if (PlayerStateManager.checkAttributeChanges(player)) {
                    DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Patricia %s attribute change detected, updating attack bonus", 
                        player.getDisplayName().getString());
                    
                    // 攻撃力ボーナスを更新
                    updateAttackPowerBonus(player, DEFAULT_CONFIG);
                    
                    // 新しい値をキャッシュ
                    PlayerStateManager.cacheCurrentAttributes(player);
                }
            }
            
            // 攻撃速度低下のフォールバッククリーンアップ（2分ごと）
            if (player.tickCount % 2400 == 0) {
                if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                    DebugDisplay.debug(player, "ATTACK_SPEED_IMMUNITY", "Patricia %s fallback cleanup: removed DIG_SLOWDOWN", 
                        player.getDisplayName().getString());
                }
            }
            
            // 5分ごとにキャッシュクリーンアップ
            if (player.tickCount % 6000 == 0) {
                DebugDisplay.debug("PERFORMANCE", "Patricia cache cleanup performed");
                PlayerStateManager.cleanupCache();
            }
        }
    }
}