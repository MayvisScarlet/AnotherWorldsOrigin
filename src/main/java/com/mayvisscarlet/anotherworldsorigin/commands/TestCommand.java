package com.mayvisscarlet.anotherworldsorigin.commands;

import com.mayvisscarlet.anotherworldsorigin.origins.patricia.abilities.UnwaveringWinter;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 拡張されたテスト用コマンド
 * パトリシアの能力テストを含む
 */
public class TestCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anwstest")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("§aAnother Worlds Origin コマンドが動作しています！"), false);
                return 1;
            })
            .then(Commands.literal("patricia")
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
            )
        );
    }
    
    /**
     * 攻撃速度無効化テスト
     */
    private static int testAttackSpeedImmunity(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§b[Test] " + player.getDisplayName().getString() + " の攻撃速度無効化をテストします..."), true);
        
        // 現在の状態を記録
        boolean hadEffectBefore = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // テスト用の採掘速度低下を適用
        MobEffectInstance slowdown = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1);
        boolean addEffectResult = player.addEffect(slowdown);
        
        // 適用後の状態をチェック
        boolean hasEffectAfter = player.hasEffect(MobEffects.DIG_SLOWDOWN);
        
        // 結果を判定
        if (!hadEffectBefore && !hasEffectAfter) {
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 攻撃速度低下が正常に無効化されました"), true);
            player.sendSystemMessage(Component.literal("§a[Patricia Test] 攻撃速度無効化テスト成功"));
        } else if (hadEffectBefore && !hasEffectAfter) {
            source.sendSuccess(() -> Component.literal("§a[Test Passed] 既存の攻撃速度低下が除去されました"), true);
            player.sendSystemMessage(Component.literal("§a[Patricia Test] 既存効果除去テスト成功"));
        } else {
            source.sendFailure(Component.literal("§c[Test Failed] 攻撃速度低下が無効化されませんでした"));
            player.sendSystemMessage(Component.literal("§c[Patricia Test] 攻撃速度無効化テスト失敗"));
        }
        
        return 1;
    }
    
    /**
     * パトリシアの状態表示
     */
    private static int showPatriciaStatus(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
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
        boolean isCold = com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaConstants.isColdBiome(biomeTemp);
        boolean isHot = com.mayvisscarlet.anotherworldsorigin.origins.patricia.PatriciaConstants.isHotBiome(biomeTemp);
        
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
        
        return 1;
    }
    
    /**
     * パトリシア能力の手動有効化
     */
    private static int activatePatriciaAbilities(CommandSourceStack source, ServerPlayer player) {
        if (!OriginHelper.isPatricia(player)) {
            source.sendFailure(Component.literal("§c指定されたプレイヤーはパトリシア種族ではありません"));
            return 0;
        }
        
        try {
            UnwaveringWinter.onPatriciaActivated(player);
            
            source.sendSuccess(() -> Component.literal("§a" + player.getDisplayName().getString() + " のパトリシア能力を手動で有効化しました"), true);
            player.sendSystemMessage(Component.literal("§b[Patricia] §f能力が手動で再有効化されました"));
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c能力の有効化中にエラーが発生しました: " + e.getMessage()));
            return 0;
        }
    }
}