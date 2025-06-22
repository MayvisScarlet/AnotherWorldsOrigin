package com.mayvisscarlet.anotherworldsorigin.commands;

import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.HeatVulnerabilityPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import com.mayvisscarlet.anotherworldsorigin.util.DebugDisplay;
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
 * 全てのテスト機能を一元管理（クリーンアーキテクチャ対応版）
 */
public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anwstest")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§aAnother Worlds Origin テストコマンドが動作しています！"),
                            false);
                    showTestHelp(context.getSource());
                    return 1;
                })
                .then(Commands.literal("help")
                        .executes(context -> {
                            showTestHelp(context.getSource());
                            return 1;
                        }))
                .then(Commands.literal("patricia")
                        .then(Commands.literal("cold_test")
                                .executes(context -> testColdDamageReduction(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testColdDamageReduction(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("damage_test")
                                .executes(context -> testDamageReduction(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testDamageReduction(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("immunity")
                                .executes(context -> testAttackSpeedImmunity(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testAttackSpeedImmunity(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("status")
                                .executes(context -> showPatriciaStatus(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> showPatriciaStatus(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("activate")
                                .executes(context -> activatePatriciaAbilities(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> activatePatriciaAbilities(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("heat_test")
                                .executes(context -> testHeatVulnerability(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testHeatVulnerability(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("biome_test")
                                .executes(context -> testBiomeEffects(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testBiomeEffects(context.getSource(),
                                                EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("recovery_test")
                                .executes(context -> testRecoveryBonus(context.getSource(),
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> testRecoveryBonus(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"))))))
                // 将来の種族拡張用
                .then(Commands.literal("yura")
                        .executes(context -> {
                            context.getSource().sendFailure(Component.literal("§cYura abilities not implemented yet"));
                            return 0;
                        }))
                .then(Commands.literal("carnis")
                        .executes(context -> {
                            context.getSource()
                                    .sendFailure(Component.literal("§cCarnis abilities not implemented yet"));
                            return 0;
                        }))
                .then(Commands.literal("vorey")
                        .executes(context -> {
                            context.getSource().sendFailure(Component.literal("§cVorey abilities not implemented yet"));
                            return 0;
                        })));
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
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia biome_test §7- バイオーム効果テスト"), false);
        source.sendSuccess(() -> Component.literal("§b/anwstest patricia recovery_test §7- 回復ボーナステスト"), false);
        source.sendSuccess(() -> Component.literal("§7将来実装予定: yura, carnis, vorey"), false);
    }

    /**
     * 寒冷バイオーム効果テスト（UnwaveringWinter版）
     */
    private static int testColdDamageReduction(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }

        DebugDisplay.info(player, "TEST_EXECUTION", "Starting cold biome effects test for %s", player.getDisplayName().getString());
        source.sendSuccess(
                () -> Component
                        .literal("§b[Cold Test] Testing cold biome effects for " + player.getDisplayName().getString()),
                true);

        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();

        // バイオーム情報を表示
        var biome = player.level().getBiome(player.blockPosition()).value();
        float temperature = biome.getBaseTemperature();
        boolean isCold = patriciaConfig.isColdBiome(temperature);

        DebugDisplay.debug(player, "BIOME_DETECTION", "Current Biome: %s, Temperature: %.2f, Cold threshold: ≤%.2f, Is Cold: %s", 
            biome.toString(), temperature, patriciaConfig.getColdTemperatureThreshold(), isCold ? "YES" : "NO");

        // 親和度とダメージ軽減率を表示
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player)
                .ifPresent(affinityData -> {
                    int level = affinityData.getAffinityData().getAffinityLevel();
                    double reduction = patriciaConfig.calculateColdDamageReduction(level);

                    DebugDisplay.info(player, "AFFINITY_CALCULATION", "Affinity Level: %d, Damage Reduction: %.1f%% (max: %.1f%%)", 
                        level, reduction * 100, patriciaConfig.getColdMaxDamageReduction());

                    if (isCold) {
                        DebugDisplay.info(player, "TEST_EXECUTION", "Cold Biome damage reduction is ACTIVE!");

                        // UnwaveringWinterPowerFactoryを使用してテストダメージを模擬
                        float testDamage = 10.0f;
                        var config = new UnwaveringWinterPowerFactory.Configuration(true, true, true, true);
                        float multiplier = UnwaveringWinterPowerFactory.calculateDamageReduction(player, config);
                        float reducedDamage = testDamage * multiplier;

                        DebugDisplay.info(player, "TEST_EXECUTION", "Simulated Test: %.1f damage → %.1f damage (%.1f%% reduction)", 
                            testDamage, reducedDamage, (1.0f - multiplier) * 100);

                        source.sendSuccess(() -> Component.literal(String.format(
                                "§a[Test Result] Damage reduction active: %.1f%% (%.1f → %.1f damage)",
                                (1.0f - multiplier) * 100, testDamage, reducedDamage)), false);
                    } else {
                        DebugDisplay.warn(player, "TEST_EXECUTION", "Not in cold biome - damage reduction INACTIVE. Go to snowy biome!");
                        source.sendSuccess(
                                () -> Component.literal("§c[Test Result] Not in cold biome - no damage reduction"),
                                false);
                    }
                });

        return 1;
    }

    /**
     * 実際のダメージ軽減テスト（UnwaveringWinter版）
     */
    private static int testDamageReduction(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }

        // 現在の状況を確認
        var patriciaConfig = ConfigManager.getPatriciaConfig();
        boolean isCold = patriciaConfig.isColdBiome(
                player.level().getBiome(player.blockPosition()).value().getBaseTemperature());

        if (!isCold) {
            source.sendFailure(
                    Component.literal("§cYou must be in a cold biome for this test. Go to a snowy area first!"));
            return 0;
        }

        DebugDisplay.info(player, "TEST_EXECUTION", "Applying 5 magic damage in cold biome for damage reduction test");
        source.sendSuccess(() -> Component.literal("§b[Damage Test] Applying 5 magic damage in cold biome..."), true);

        // 実際にダメージを与える
        player.hurt(player.damageSources().magic(), 5.0f);

        DebugDisplay.info(player, "TEST_EXECUTION", "Damage test completed - damage applied, check reduction logs");
        source.sendSuccess(() -> Component.literal("§a[Damage Test] Damage applied! Check reduction logs."), false);

        return 1;
    }

    /**
     * 攻撃速度無効化テスト（UnwaveringWinter版）
     */
    private static int testAttackSpeedImmunity(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }

        DebugDisplay.info(player, "TEST_EXECUTION", "Starting attack speed immunity test for %s", player.getDisplayName().getString());
        source.sendSuccess(
                () -> Component.literal(
                        "§b[Immunity Test] Testing attack speed immunity for " + player.getDisplayName().getString()),
                true);

        // 現在の状態を記録
        boolean hadEffectBefore = player.hasEffect(MobEffects.DIG_SLOWDOWN);

        // テスト用の採掘速度低下を適用
        MobEffectInstance slowdown = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1);
        boolean addEffectResult = player.addEffect(slowdown);

        // 適用後の状態をチェック
        boolean hasEffectAfter = player.hasEffect(MobEffects.DIG_SLOWDOWN);

        // 結果を判定
        if (!hadEffectBefore && !hasEffectAfter) {
            DebugDisplay.info(player, "TEST_EXECUTION", "Attack speed immunity test PASSED - effect successfully blocked");
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 攻撃速度低下が正常に無効化されました"), false);
        } else if (hadEffectBefore && !hasEffectAfter) {
            DebugDisplay.info(player, "TEST_EXECUTION", "Attack speed immunity test PASSED - existing effect removed");
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 既存の攻撃速度低下が除去されました"), false);
        } else {
            DebugDisplay.error(player, "TEST_EXECUTION", "Attack speed immunity test FAILED - effect not blocked");
            source.sendFailure(Component.literal("§c[Test Failed] 攻撃速度低下が無効化されませんでした"));
        }

        return 1;
    }

    /**
     * パトリシアの状態表示（両PowerFactory対応版）
     */
    private static int showPatriciaStatus(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }

        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();

        // 基本情報
        source.sendSuccess(() -> Component.literal("§b=== " + player.getDisplayName().getString() + " のパトリシア状態 ==="),
                false);

        // 親和度情報
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player)
                .ifPresent(affinityData -> {
                    var data = affinityData.getAffinityData();
                    DebugDisplay.info(player, "AFFINITY_CALCULATION", "Affinity Status - Level: %d, Points: %.1f/%.0f", 
                        data.getAffinityLevel(), data.getCurrentLevelPoints(), (double) data.getPointsToNextLevel());
                    source.sendSuccess(() -> Component.literal(String.format(
                            "§e親和度: §aLv.%d §7(%.1f/%.0f)",
                            data.getAffinityLevel(),
                            data.getCurrentLevelPoints(),
                            (double) data.getPointsToNextLevel())), false);

                    // 攻撃力ボーナス計算（UnwaveringWinter）
                    double affinityBonus = patriciaConfig.calculateAffinityAttackBonus(data.getAffinityLevel());
                    if (affinityBonus > 0) {
                        source.sendSuccess(() -> Component.literal(String.format(
                                "§e攻撃力ボーナス: §a+%.2f §7(揺らぐ事なき冬)",
                                affinityBonus)), false);
                    }

                    // Cold系ダメージ軽減（UnwaveringWinter）
                    double coldReduction = patriciaConfig.calculateColdDamageReduction(data.getAffinityLevel());
                    source.sendSuccess(() -> Component.literal(String.format(
                            "§eCold軽減: §a%.1f%% §7(max: %.1f%%, 揺らぐ事なき冬)",
                            coldReduction * 100, patriciaConfig.getColdMaxDamageReduction())), false);

                    // Fire系ダメージ増加（HeatVulnerability）
                    double fireMultiplier = patriciaConfig.calculateFireDamageMultiplier(data.getAffinityLevel());
                    source.sendSuccess(() -> Component.literal(String.format(
                            "§cFire倍率: §c%.2fx §7(min: %.2fx, 溶けた氷が固まるまで)",
                            fireMultiplier, patriciaConfig.getFireMinMultiplier())), false);
                });

        // 攻撃関連
        double attackDamage = player
                .getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        double attackSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);

        source.sendSuccess(() -> Component.literal(String.format(
                "§e攻撃力: §a%.2f §7| §e攻撃速度: §a%.2f",
                attackDamage, attackSpeed)), false);

        // バイオーム情報
        float biomeTemp = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
        boolean isCold = patriciaConfig.isColdBiome(biomeTemp);
        boolean isHot = patriciaConfig.isHotBiome(biomeTemp);

        String biomeType = isCold ? "§bCold" : isHot ? "§cHot" : "§7Normal";
        source.sendSuccess(() -> Component.literal(String.format(
                "§eバイオーム: %s §7(温度: %.2f)",
                biomeType, biomeTemp)), false);

        // 効果状態
        boolean hasSlowdown = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        source.sendSuccess(() -> Component.literal(String.format(
                "§e採掘速度低下: %s",
                hasSlowdown ? "§c有効" : "§a無効化済み")), false);

        // マイルストーン情報
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player)
                .ifPresent(affinityData -> {
                    int level = affinityData.getAffinityData().getAffinityLevel();
                    boolean isHighAffinity = patriciaConfig.isHighAffinityActive(level);

                    source.sendSuccess(() -> Component.literal(String.format(
                            "§eマイルストーン: §7高親和度(Lv.%d) %s",
                            patriciaConfig.getHighAffinityThreshold(),
                            isHighAffinity ? "§a達成" : "§c未達成")), false);
                });

        return 1;
    }

    /**
     * パトリシア能力の手動有効化（両PowerFactory対応版）
     */
    private static int activatePatriciaAbilities(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }

        try {
            // 両方のPowerFactoryの初期化メソッドを呼び出し
            UnwaveringWinterPowerFactory.onPatriciaActivated(player);
            HeatVulnerabilityPowerFactory.onPatriciaActivated(player);

            DebugDisplay.info(player, "TEST_EXECUTION", "Patricia abilities manually activated for %s", player.getDisplayName().getString());
            source.sendSuccess(
                    () -> Component.literal("§a" + player.getDisplayName().getString() + " のパトリシア能力を手動で有効化しました"), true);

            return 1;
        } catch (Exception e) {
            DebugDisplay.error(player, "TEST_EXECUTION", "Error during Patricia abilities activation: %s", e.getMessage());
            source.sendFailure(Component.literal("§c能力の有効化中にエラーが発生しました: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 熱ダメージ脆弱性テスト（HeatVulnerability版）
     */
    private static int testHeatVulnerability(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }

        DebugDisplay.info(player, "HEAT_DAMAGE", "Starting heat damage vulnerability test for %s", player.getDisplayName().getString());
        source.sendSuccess(
                () -> Component.literal(
                        "§c[Heat Test] Testing heat damage vulnerability for " + player.getDisplayName().getString()),
                true);

        // 設定を取得
        var patriciaConfig = ConfigManager.getPatriciaConfig();

        // 親和度と倍率を表示
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player)
                .ifPresent(affinityData -> {
                    int level = affinityData.getAffinityData().getAffinityLevel();
                    double multiplier = patriciaConfig.calculateFireDamageMultiplier(level);

                    DebugDisplay.info(player, "HEAT_DAMAGE", "Heat vulnerability - Level: %d, Multiplier: %.2fx (base: %.2fx, min: %.2fx)", 
                        level, multiplier, patriciaConfig.getFireBaseMultiplier(), patriciaConfig.getFireMinMultiplier());

                    // テスト用の火炎ダメージを適用
                    float testDamage = 5.0f;
                    DebugDisplay.info(player, "HEAT_DAMAGE", "Applying %.1f fire damage for vulnerability test", testDamage);

                    // 実際に火炎ダメージを与える
                    player.hurt(player.damageSources().onFire(), testDamage);

                    // 予想結果を表示
                    float expectedDamage = testDamage * (float) multiplier;
                    DebugDisplay.info(player, "HEAT_DAMAGE", "Expected result: %.1f damage → %.1f damage (%.2fx multiplier)", 
                        testDamage, expectedDamage, multiplier);

                    source.sendSuccess(() -> Component.literal(String.format(
                            "§c[Test Result] Applied fire damage with %.2fx multiplier (Level %d)",
                            multiplier, level)), false);
                });

        // 熱ダメージソース判定テスト
        DebugDisplay.debug(player, "HEAT_DAMAGE", "Starting heat damage source identification tests");

        // 各種熱ダメージソースのテスト
        testHeatDamageSource(player, player.damageSources().onFire(), "ON_FIRE");
        testHeatDamageSource(player, player.damageSources().inFire(), "IN_FIRE");
        testHeatDamageSource(player, player.damageSources().lava(), "LAVA");
        testHeatDamageSource(player, player.damageSources().hotFloor(), "HOT_FLOOR");
        testHeatDamageSource(player, player.damageSources().magic(), "MAGIC (non-heat)");

        DebugDisplay.info(player, "HEAT_DAMAGE", "Heat damage vulnerability test completed");
        source.sendSuccess(() -> Component.literal("§c[Heat Test] Heat damage vulnerability test completed"), false);
        return 1;
    }

    /**
     * 熱ダメージソース判定のテストヘルパー
     */
    private static void testHeatDamageSource(Player player, net.minecraft.world.damagesource.DamageSource damageSource,
            String sourceName) {
        boolean isHeat = HeatVulnerabilityPowerFactory.HeatDamageCalculator.isHeatDamage(damageSource);
        DebugDisplay.debug(player, "HEAT_DAMAGE", "Heat source test - %s: %s", sourceName, isHeat ? "HEAT" : "NOT HEAT");
    }

    /**
     * バイオーム効果テスト（Hot系バイオーム対応）
     */
    private static int testBiomeEffects(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }

        DebugDisplay.info(player, "BIOME_DETECTION", "Starting biome effects test for %s", player.getDisplayName().getString());
        source.sendSuccess(
                () -> Component
                        .literal("§6[Biome Test] Testing biome effects for " + player.getDisplayName().getString()),
                true);

        var patriciaConfig = ConfigManager.getPatriciaConfig();

        // 現在のバイオーム情報
        var biome = player.level().getBiome(player.blockPosition()).value();
        float temperature = biome.getBaseTemperature();
        boolean isCold = patriciaConfig.isColdBiome(temperature);
        boolean isHot = patriciaConfig.isHotBiome(temperature);

        DebugDisplay.debug(player, "BIOME_DETECTION", "Biome analysis - Current: %s, Temperature: %.2f, Cold threshold: ≤%.2f, Hot threshold: ≥%.2f", 
            biome.toString(), temperature, patriciaConfig.getColdTemperatureThreshold(), patriciaConfig.getHotTemperatureThreshold());

        // バイオーム判定結果
        String biomeType = isCold ? "COLD" : isHot ? "HOT" : "NORMAL";
        DebugDisplay.info(player, "BIOME_DETECTION", "Biome classification: %s (Cold: %s, Hot: %s)", biomeType, isCold, isHot);

        // 親和度による効果計算
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player)
                .ifPresent(affinityData -> {
                    int level = affinityData.getAffinityData().getAffinityLevel();

                    if (isCold) {
                        double coldReduction = patriciaConfig.calculateColdDamageReduction(level);
                        DebugDisplay.info(player, "BIOME_DETECTION", "Cold biome effects - Damage reduction: %.1f%% (Level %d)", coldReduction * 100, level);
                    }

                    if (isHot) {
                        double hotIncrease = patriciaConfig.calculateHotDamageIncrease(level);
                        DebugDisplay.info(player, "BIOME_DETECTION", "Hot biome effects - Damage increase: +%.1f%%, Exhaustion: 2x (Level %d)", hotIncrease * 100, level);

                        // テスト用ダメージ適用
                        if (hotIncrease > 0) {
                            DebugDisplay.debug(player, "BIOME_DETECTION", "Applying 3 magic damage for hot biome effect test");
                            player.hurt(player.damageSources().magic(), 3.0f);
                        }
                    }

                    if (!isCold && !isHot) {
                        DebugDisplay.debug(player, "BIOME_DETECTION", "Normal biome - no special effects active");
                    }
                });

        DebugDisplay.info(player, "BIOME_DETECTION", "Biome effects test completed");
        source.sendSuccess(() -> Component.literal("§6[Biome Test] Biome effects test completed"), false);
        return 1;
    }

    /**
     * 回復ボーナステスト（Phase 3新規実装）
     */
    private static int testRecoveryBonus(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§cThis test is only for Patricia origin users"));
            return 0;
        }

        DebugDisplay.info(player, "RECOVERY_BONUS", "Starting recovery bonus test for %s", player.getDisplayName().getString());
        source.sendSuccess(
            () -> Component.literal(
                "§a[Recovery Test] Testing recovery bonus for " + player.getDisplayName().getString()),
            true);

        // 現在の状況表示
        var config = ConfigManager.getPatriciaConfig();
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            
            // 設定値表示
            int duration = config.calculateRecoveryDuration(level);
            double increase = config.calculateRecoveryIncrease(level);
            boolean isHighAffinity = config.isHighAffinityActive(level);
            
            DebugDisplay.info(player, "RECOVERY_BONUS", "Recovery settings - Level: %d, Duration: %ds, Increase: %.1f%%, High Affinity: %s", 
                level, duration / 20, increase * 100, isHighAffinity ? "YES" : "NO");
            
            // 既存のボーナス状態確認
            var existingBonus = HeatVulnerabilityPowerFactory.PlayerStateManager.getRecoveryBonusData(player);
            if (existingBonus != null) {
                DebugDisplay.info(player, "RECOVERY_BONUS", "Current active bonus: %.1fx for %d ticks (source: %s)", 
                    existingBonus.getBonusMultiplier(), existingBonus.getRemainingTicks(), existingBonus.getTriggerSource());
            } else {
                DebugDisplay.debug(player, "RECOVERY_BONUS", "No recovery bonus currently active");
            }
            
            // 各発動条件をテスト
            testRecoveryTriggers(player, source);
        });

        return 1;
    }

    /**
     * 回復ボーナス発動トリガーのテスト
     */
    private static void testRecoveryTriggers(Player player, CommandSourceStack source) {
        // 火炎ダメージ発動テスト
        DebugDisplay.debug(player, "RECOVERY_BONUS", "Starting recovery trigger tests");
        
        // 1. 火炎ダメージ発動
        DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 1: Fire damage trigger");
        HeatVulnerabilityPowerFactory.RecoveryBonusManager.triggerRecoveryBonus(player, "fire_test");
        
        // 2秒後に状態確認（Minecraft Serverのスケジューラーを使用）
        if (player.getServer() != null) {
            // 実際のサーバースケジューラーでの遅延実行
            player.getServer().execute(() -> {
                try {
                    Thread.sleep(2000); // 2秒待機
                    var bonusData = HeatVulnerabilityPowerFactory.PlayerStateManager.getRecoveryBonusData(player);
                    if (bonusData != null) {
                        DebugDisplay.info(player, "RECOVERY_BONUS", "Test 1 result: Active bonus %.1fx for %ds", 
                            bonusData.getBonusMultiplier(), bonusData.getRemainingTicks() / 20);
                    } else {
                        DebugDisplay.warn(player, "RECOVERY_BONUS", "Test 1 result: No active bonus detected");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 2. Hot系バイオーム滞在発動テスト
        DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 2: Hot biome endurance trigger");
        HeatVulnerabilityPowerFactory.RecoveryBonusManager.triggerRecoveryBonus(player, "hot_biome_endurance_test");
        
        // 3. 高親和度Cold系発動テスト
        var config = ConfigManager.getPatriciaConfig();
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            var biome = player.level().getBiome(player.blockPosition()).value();
            boolean isCold = config.isColdBiome(biome.getBaseTemperature());
            
            DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 3 info - Level: %d, High Affinity: %s, Cold Biome: %s", 
                level, config.isHighAffinityActive(level) ? "YES" : "NO", isCold ? "YES" : "NO");
            
            if (config.isHighAffinityActive(level)) {
                if (isCold) {
                    DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 3: High affinity cold bonus trigger activated");
                    HeatVulnerabilityPowerFactory.RecoveryBonusManager.triggerRecoveryBonus(player, "high_affinity_cold_test");
                } else {
                    DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 3: Not in cold biome - move to snowy area");
                }
            } else {
                DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 3: High affinity not available (Level %d < %d)", level, config.getHighAffinityThreshold());
            }
        });
        
        // 4. 総合条件チェックテスト
        DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 4: Comprehensive condition check");
        
        // 現在の条件を確認して適切なメッセージを表示
        var currentBiome = player.level().getBiome(player.blockPosition()).value();
        float temp = currentBiome.getBaseTemperature();
        boolean isCold = config.isColdBiome(temp);
        boolean isHot = config.isHotBiome(temp);
        
        com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int level = affinityData.getAffinityData().getAffinityLevel();
            boolean isHighAffinity = config.isHighAffinityActive(level);
            
            DebugDisplay.debug(player, "RECOVERY_BONUS", "Current conditions - Level: %d, Temp: %.2f, Cold: %s, Hot: %s, HighAff: %s", 
                level, temp, isCold ? "YES" : "NO", isHot ? "YES" : "NO", isHighAffinity ? "YES" : "NO");
            
            // 発動可能条件の確認
            if (isHighAffinity && isCold) {
                DebugDisplay.info(player, "RECOVERY_BONUS", "Available: High affinity cold bonus");
            } else if (isHot && level >= 10) {
                DebugDisplay.info(player, "RECOVERY_BONUS", "Available: Hot biome endurance bonus");
            } else if (isCold && level >= 5) {
                DebugDisplay.info(player, "RECOVERY_BONUS", "Available: Cold biome comfort bonus");
            } else {
                DebugDisplay.debug(player, "RECOVERY_BONUS", "No bonus conditions currently met");
            }
        });
        
        // 5. 手動終了テスト
        DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 5: Manual end test (in 3 seconds)");
        if (player.getServer() != null) {
            player.getServer().execute(() -> {
                try {
                    Thread.sleep(3000); // 3秒待機
                    HeatVulnerabilityPowerFactory.RecoveryBonusManager.endRecoveryBonus(player);
                    DebugDisplay.debug(player, "RECOVERY_BONUS", "Test 5 result: Manual end executed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        DebugDisplay.info(player, "RECOVERY_BONUS", "All recovery bonus tests initiated");
        source.sendSuccess(() -> Component.literal("§a[Recovery Test] All recovery bonus tests initiated"), false);
    }
}