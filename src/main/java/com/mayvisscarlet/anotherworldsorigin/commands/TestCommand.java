package com.mayvisscarlet.anotherworldsorigin.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * シンプルなテスト用コマンド
 */
public class TestCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("anwstest")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal("§aAnother Worlds Origin コマンドが動作しています！"), false);
                return 1;
            })
        );
    }
}