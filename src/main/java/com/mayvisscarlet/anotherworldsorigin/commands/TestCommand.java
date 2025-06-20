package com.mayvisscarlet.anotherworldsorigin.commands;

import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.HeatVulnerabilityPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * 統合されたテスト用コマンド
 * 全てのテスト機能を一元管理（統合PowerFactory対応版）
 */
public class TestCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anwstest")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("§aAnother Worlds Origin テストコマンドが動作しています！"), false);
                showTestHelp(context.getSource());
                return 1;
            })
            .then(Commands.literal("help")
                .executes(context -> {
                    showTestHelp(context.getSource());
                    return 1;
                })
            )
            .then(Commands.literal("patricia")
                .then(Commands.literal("cold_test")
                    .executes(context -> testColdDamageReduction(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> testColdDamageReduction(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("damage_test")
                    .executes(context -> testDamageReduction(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> testDamageReduction(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("immunity")
                    .executes(context -> testAttackSpeedImmunity(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> testAttackSpeedImmunity(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("status")
                    .executes(context -> showPatriciaStatus(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> showPatriciaStatus(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("activate")
                    .executes(context -> activatePatriciaAbilities(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> activatePatriciaAbilities(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("heat_test")
                    .executes(context -> testHeatVulnerability(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> testHeatVulnerability(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
            )
            // 将来の種族拡張用
            .then(Commands.literal("yura")
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("§cYura abilities not implemented yet"));
                    return 0;
                })
            )
            .then(Commands.literal("carnis")
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("§cCarnis abilities not implemented yet"));
                    return 0;
                })
            )
            .then(Commands.literal("vorey")
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("§cVorey abilities not implemented yet"));
                    return 0;
                })
            )
        );
    }
    
    /**
     * テストコマンドのヘルプ表示
     */
    private static void showTestHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§e=== Another Worlds Origin Test Commands ==="), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia cold_test §7- 寒冷バイオーム効果テスト"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia damage_test §7- 実際のダメージ軽減テスト"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia immunity §7- 攻撃速度無効化テスト"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia status §7- パトリシア状態表示"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia activate §7- 能力手動有効化"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia heat_test §7- 熱ダメージ脆弱性テスト"), false);
        source.sendSuccess(() -> Component.literal("§7将来実装予定: yura, carnis, vorey"), false);
    }
    
    /**
     * 寒冷バイオーム効果テスト（統合PowerFactory版）
     */
    private static int testColdDamageReduction(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§b[Cold Test] Testing cold biome effects for " + player.getDisplayName().getString()), true);
        
        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        // バイオーム情報を表示
        var biome = player.level().getBiome(player.blockPosition()).value();
        float temperature = biome.getBaseTemperature();
        boolean isCold = patriciaConfig.isColdBiome(temperature);
        
        player.sendSystemMessage(Component.literal(
            String.format("§eCurrent Biome: §f%s", biome.toString())
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eTemperature: §f%.2f §7(cold threshold: ≤%.2f)", 
                temperature, 
                patriciaConfig.getColdTemperatureThreshold())
        ));
        player.sendSystemMessage(Component.literal(
            String.format("§eIs Cold Biome: %s", isCold ? "§aYES" : "§cNO")
        ));
        
        // 親和度とダメージ軽減率を表示
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            double reduction = patriciaConfig.calculateColdDamageReduction(level);
            
            player.sendSystemMessage(Component.literal(
                String.format("§eAffinity Level: §a%d", level)
            ));
            player.sendSystemMessage(Component.literal(
                String.format("§eDamage Reduction: §a%.1f%% §7(max: %.1f%%)", 
                    reduction * 100, 
                    patriciaConfig.getColdMaxDamageReduction())
            ));
            
            if (isCold) {
                player.sendSystemMessage(Component.literal(
                    "§a[Cold Biome] Damage reduction is ACTIVE!"
                ));
                
                // 統合PowerFactoryを使用してテストダメージを模擬
                float testDamage = 10.0f;
                var config = new UnwaveringWinterPowerFactory.Configuration(true, true, true, true);
                float multiplier = UnwaveringWinterPowerFactory.calculateDamageReduction(player, config);
                float reducedDamage = testDamage * multiplier;
                
                player.sendSystemMessage(Component.literal(
                    String.format("§eSimulated Test: §c%.1f§7 damage → §a%.1f§7 damage", testDamage, reducedDamage)
                ));
                
                source.sendSuccess(() -> Component.literal(String.format(
                    "§a[Test Result] Damage reduction active: %.1f%% (%.1f → %.1f damage)", 
                    (1.0f - multiplier) * 100, testDamage, reducedDamage)), false);
            } else {
                player.sendSystemMessage(Component.literal(
                    "§c[Not Cold] Damage reduction is INACTIVE. Go to snowy biome!"
                ));
                source.sendSuccess(() -> Component.literal("§c[Test Result] Not in cold biome - no damage reduction"), false);
            }
        });
        
        return 1;
    }
    
    /**
     * 実際のダメージ軽減テスト（統合PowerFactory版）
     */
    private static int testDamageReduction(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }
        
        // 現在の状況を確認
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        boolean isCold = patriciaConfig.isColdBiome(
            player.level().getBiome(player.blockPosition()).value().getBaseTemperature()
        );
        
        if (!isCold) {
            source.sendFailure(Component.literal("§cYou must be in a cold biome for this test. Go to a snowy area first!"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§b[Damage Test] Applying 5 magic damage in cold biome..."), true);
        player.sendSystemMessage(Component.literal("§e[Damage Test] Applying 5 damage in cold biome..."));
        
        // 実際にダメージを与える
        player.hurt(player.damageSources().magic(), 5.0f);
        
        source.sendSuccess(() -> Component.literal("§a[Damage Test] Damage applied! Check reduction logs."), false);
        player.sendSystemMessage(Component.literal("§a[Damage Test] Damage applied! Check your health and the reduction logs."));
        
        return 1;
    }
    
    /**
     * 攻撃速度無効化テスト（統合PowerFactory版）
     */
    private static int testAttackSpeedImmunity(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§b[Immunity Test] Testing attack speed immunity for " + player.getDisplayName().getString()), true);
        
        // 現在の状態を記録
        boolean hadEffectBefore = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // テスト用の採掘速度低下を適用
        MobEffectInstance slowdown = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1);
        boolean addEffectResult = player.addEffect(slowdown);
        
        // 適用後の状態をチェック
        boolean hasEffectAfter = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // 結果を判定
        if (!hadEffectBefore && !hasEffectAfter) {
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 攻撃速度低下が正常に無効化されました"), false);
            player.sendSystemMessage(Component.literal("§a[Patricia Test] 攻撃速度無効化テスト成功"));
        } else if (hadEffectBefore && !hasEffectAfter) {
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 既存の攻撃速度低下が除去されました"), false);
            player.sendSystemMessage(Component.literal("§a[Patricia Test] 既存効果除去テスト成功"));
        } else {
            source.sendFailure(Component.literal("§c[Test Failed] 攻撃速度低下が無効化されませんでした"));
            player.sendSystemMessage(Component.literal("§c[Patricia Test] 攻撃速度無効化テスト失敗"));
        }
        
        return 1;
    }
    
    /**
     * パトリシアの状態表示（統合PowerFactory版）
     */
    private static int showPatriciaStatus(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        // 基本情報
        source.sendSuccess(() -> Component.literal("§b=== " + player.getDisplayName().getString() + " のパトリシア状態 ==="), false);
        
        // 親和度情報
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            var data = affinityData.getAffinityData();
            source.sendSuccess(() -> Component.literal(String.format(
                "§e親和度: §aLv.%d §7(%.1f/%.0f)",
                data.getAffinityLevel(),
                data.getCurrentLevelPoints(),
                (double)data.getPointsToNextLevel()
            )), false);
            
            // 攻撃力ボーナス計算
            double affinityBonus = patriciaConfig.calculateAffinityAttackBonus(data.getAffinityLevel());
            if (affinityBonus > 0) {
                source.sendSuccess(() -> Component.literal(String.format(
                    "§e攻撃力ボーナス: §a+%.2f §7(親和度効果)",
                    affinityBonus
                )), false);
            }
            
            // Cold系ダメージ軽減
            double coldReduction = patriciaConfig.calculateColdDamageReduction(data.getAffinityLevel());
            source.sendSuccess(() -> Component.literal(String.format(
                "§eCold軽減: §a%.1f%% §7(max: %.1f%%)",
                coldReduction * 100, patriciaConfig.getColdMaxDamageReduction()
            )), false);
            
            // Fire系ダメージ増加
            double fireMultiplier = patriciaConfig.calculateFireDamageMultiplier(data.getAffinityLevel());
            source.sendSuccess(() -> Component.literal(String.format(
                "§cFire倍率: §c%.2fx §7(min: %.2fx)",
                fireMultiplier, patriciaConfig.getFireMinMultiplier()
            )), false);
        });
        
        // 攻撃関連
        double attackDamage = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        double attackSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        
        source.sendSuccess(() -> Component.literal(String.format(
            "§e攻撃力: §a%.2f §7| §e攻撃速度: §a%.2f",
            attackDamage, attackSpeed
        )), false);
        
        // バイオーム情報
        float biomeTemp = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
        boolean isCold = patriciaConfig.isColdBiome(biomeTemp);
        boolean isHot = patriciaConfig.isHotBiome(biomeTemp);
        
        String biomeType = isCold ? "§bCold" : isHot ? "§cHot" : "§7Normal";
        source.sendSuccess(() -> Component.literal(String.format(
            "§eバイオーム: %s §7(温度: %.2f)",
            biomeType, biomeTemp
        )), false);
        
        // 効果状態
        boolean hasSlowdown = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        source.sendSuccess(() -> Component.literal(String.format(
            "§e採掘速度低下: %s",
            hasSlowdown ? "§c有効" : "§a無効化済み"
        )), false);
        
        // マイルストーン情報
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            boolean isHighAffinity = patriciaConfig.isHighAffinityActive(level);
            
            source.sendSuccess(() -> Component.literal(String.format(
                "§eマイルストーン: §7高親和度(Lv.%d) %s",
                patriciaConfig.getHighAffinityThreshold(),
                isHighAffinity ? "§a達成" : "§c未達成"
            )), false);
        });
        
        return 1;
    }
    
    /**
     * パトリシア能力の手動有効化（統合PowerFactory版）
     */
    private static int activatePatriciaAbilities(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
        try {
            // 統合されたPowerFactoryの初期化メソッドを呼び出し
            UnwaveringWinterPowerFactory.onPatriciaActivated(player);
            
            source.sendSuccess(() -> Component.literal("§a" + player.getDisplayName().getString() + " のパトリシア能力を手動で有効化しました"), true);
            player.sendSystemMessage(Component.literal("§b[Patricia] §f能力が手動で再有効化されました"));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c能力の有効化中にエラーが発生しました: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 熱ダメージ脆弱性テスト（新規実装）
     */
    private static int testHeatVulnerability(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§c[Heat Test] Testing heat damage vulnerability for " + player.getDisplayName().getString()), true);
        
        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        
        // 親和度と倍率を表示
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            double multiplier = patriciaConfig.calculateFireDamageMultiplier(level);
            
            player.sendSystemMessage(Component.literal(
                String.format("§eAffinity Level: §a%d", level)
            ));
            player.sendSystemMessage(Component.literal(
                String.format("§cHeat Damage Multiplier: §c%.2fx §7(base: %.2fx, min: %.2fx)", 
                    multiplier, 
                    patriciaConfig.getFireBaseMultiplier(),
                    patriciaConfig.getFireMinMultiplier())
            ));
            
            // テスト用の火炎ダメージを適用
            float testDamage = 5.0f;
            player.sendSystemMessage(Component.literal(
                String.format("§e[Heat Test] Applying %.1f fire damage...", testDamage)
            ));
            
            // 実際に火炎ダメージを与える
            player.hurt(player.damageSources().onFire(), testDamage);
            
            // 予想結果を表示
            float expectedDamage = testDamage * (float)multiplier;
            player.sendSystemMessage(Component.literal(
                String.format("§e[Expected Result] §c%.1f§7 damage → §c%.1f§7 damage", 
                    testDamage, expectedDamage)
            ));
            
            source.sendSuccess(() -> Component.literal(String.format(
                "§c[Test Result] Applied fire damage with %.2fx multiplier (Level %d)", 
                multiplier, level)), false);
        });
        
        // 熱ダメージソース判定テスト
        player.sendSystemMessage(Component.literal("§e=== Heat Damage Source Tests ==="));
        
        // 各種熱ダメージソースのテスト
        testHeatDamageSource(player, player.damageSources().onFire(), "ON_FIRE");
        testHeatDamageSource(player, player.damageSources().inFire(), "IN_FIRE");
        testHeatDamageSource(player, player.damageSources().lava(), "LAVA");
        testHeatDamageSource(player, player.damageSources().hotFloor(), "HOT_FLOOR");
        testHeatDamageSource(player, player.damageSources().magic(), "MAGIC (non-heat)");
        
        source.sendSuccess(() -> Component.literal("§c[Heat Test] Heat damage vulnerability test completed"), false);
        return 1;
    }
    
    /**
     * 熱ダメージソース判定のテストヘルパー
     */
    private static void testHeatDamageSource(Player player, net.minecraft.world.damagesource.DamageSource damageSource, String sourceName) {
        boolean isHeat = HeatVulnerabilityPowerFactory.HeatDamageCalculator.isHeatDamage(damageSource);
        player.sendSystemMessage(Component.literal(
            String.format("§e%s: %s", 
                sourceName, 
                isHeat ? "§cHEAT" : "§7NOT HEAT")
        ));
    }
}