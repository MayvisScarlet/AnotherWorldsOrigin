package com.mayvisscarlet.ifoe_bravers.commands;

import com.mayvisscarlet.ifoe_bravers.capability.AffinityCapability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 親和度データの確認・操作用デバッグコマンド
 * /anwsorigin affinity ~ の形式
 */
public class AffinityCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anwsorigin")
            .requires(source -> source.hasPermission(2)) // OP権限必要
            .then(Commands.literal("affinity")
                .then(Commands.literal("get")
                    .executes(context -> getAffinity(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> getAffinity(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
                .then(Commands.literal("add")
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                        .executes(context -> addAffinity(context.getSource(), context.getSource().getPlayerOrException(), DoubleArgumentType.getDouble(context, "amount")))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> addAffinity(context.getSource(), EntityArgument.getPlayer(context, "player"), DoubleArgumentType.getDouble(context, "amount")))
                        )
                    )
                )
                .then(Commands.literal("reset")
                    .executes(context -> resetAffinity(context.getSource(), context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> resetAffinity(context.getSource(), EntityArgument.getPlayer(context, "player")))
                    )
                )
            )
        );
    }
    
    private static int getAffinity(CommandSourceStack source, ServerPlayer player) {
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            var data = affinityData.getAffinityData();
            source.sendSuccess(() -> Component.literal(String.format(
                "§b%s§r の親和度データ:\n" +
                "§e親和度レベル: §a%d\n" +
                "§e現在の親和値: §a%.3f§7/§a%d §7(%.1f%%)\n" +
                "§e累積親和値: §a%.3f",
                player.getDisplayName().getString(),
                data.getAffinityLevel(),
                data.getCurrentLevelPoints(),
                data.getPointsToNextLevel(),
                data.getLevelProgress() * 100,
                data.getTotalAffinityPoints()
            )), false);
        });
        
        if (!AffinityCapability.getAffinityData(player).isPresent()) {
            source.sendFailure(Component.literal("§c親和度データが見つかりません"));
        }
        return 1;
    }
    
    private static int addAffinity(CommandSourceStack source, ServerPlayer player, double amount) {
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            var data = affinityData.getAffinityData();
            int oldLevel = data.getAffinityLevel();
            boolean leveledUp = data.addAffinityPoints(amount);
            
            if (leveledUp) {
                source.sendSuccess(() -> Component.literal(String.format(
                    "§b%s§r に親和値 §a%.3f§r を追加しました。" +
                    " §e%d§r → §a%d§r レベルアップ！",
                    player.getDisplayName().getString(),
                    amount,
                    oldLevel,
                    data.getAffinityLevel()
                )), true);
                
                player.sendSystemMessage(Component.literal(
                    "§l§b親和度レベルアップ！ §r§e" + oldLevel + " §7→ §a" + data.getAffinityLevel()
                ));
            } else {
                source.sendSuccess(() -> Component.literal(String.format(
                    "§b%s§r に親和値 §a%.3f§r を追加しました。",
                    player.getDisplayName().getString(),
                    amount
                )), true);
            }
        });
        
        if (!AffinityCapability.getAffinityData(player).isPresent()) {
            source.sendFailure(Component.literal("§c親和度データが見つかりません"));
        }
        return 1;
    }
    
    private static int resetAffinity(CommandSourceStack source, ServerPlayer player) {
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            var newData = new com.mayvisscarlet.ifoe_bravers.growth.AffinityData();
            affinityData.setAffinityData(newData);
            
            source.sendSuccess(() -> Component.literal(String.format(
                "§b%s§r の親和度データをリセットしました。",
                player.getDisplayName().getString()
            )), true);
            
            player.sendSystemMessage(Component.literal("§c親和度データがリセットされました"));
        });
        
        if (!AffinityCapability.getAffinityData(player).isPresent()) {
            source.sendFailure(Component.literal("§c親和度データが見つかりません"));
        }
        return 1;
    }
}