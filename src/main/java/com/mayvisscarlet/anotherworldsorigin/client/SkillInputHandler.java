
// === 3. キー入力検知とスキル発動クラス ===
package com.mayvisscarlet.anotherworldsorigin.client;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.network.SkillActivationPacket;
import com.mayvisscarlet.anotherworldsorigin.network.ModNetworking;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SkillInputHandler {
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        // スキルキーが押された時
        if (ModKeyBindings.SKILL_KEY.consumeClick()) {
            Player player = mc.player;
            
            // Another Worlds Origin種族チェック
            if (!OriginHelper.isAnotherWorldsOriginUser(player)) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c[Skill] §fAnother Worlds Origin species only!"), 
                    true
                );
                return;
            }
            
            // 既にスキル実行中の場合はスキップ
            if (com.mayvisscarlet.anotherworldsorigin.skills.SkillExecutionManager.isExecutingSkill(player)) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c[Skill] §fAlready executing skill!"), 
                    true
                );
                return;
            }
            
            // サーバーにスキル発動を通知
            ModNetworking.sendToServer(new SkillActivationPacket("test_fire_skill"));
            
            AnotherWorldsOrigin.LOGGER.info("Skill key pressed by {}", player.getDisplayName().getString());
        }
    }
}