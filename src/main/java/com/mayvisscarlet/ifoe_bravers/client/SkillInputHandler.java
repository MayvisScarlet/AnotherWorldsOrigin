
// === 3. キー入力検知とスキル発動クラス ===
package com.mayvisscarlet.ifoe_bravers.client;

import com.mayvisscarlet.ifoe_bravers.ifoe_bravers;
import com.mayvisscarlet.ifoe_bravers.network.SkillActivationPacket;
import com.mayvisscarlet.ifoe_bravers.network.ModNetworking;
import com.mayvisscarlet.ifoe_bravers.util.DebugDisplay;
import com.mayvisscarlet.ifoe_bravers.race.RaceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ifoe_bravers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SkillInputHandler {
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        // スキルキーが押された時
        if (ModKeyBindings.SKILL_KEY.consumeClick()) {
            Player player = mc.player;
            
            // IfOE_Bravers種族チェック
            if (!RaceManager.hasAnyRace(player)) {
                DebugDisplay.warn(player, "INPUT_DETECTION", "IfOE_Bravers species only!");
                return;
            }
            
            // 既にスキル実行中の場合はスキップ
            if (com.mayvisscarlet.ifoe_bravers.skills.SkillExecutionManager.isExecutingSkill(player)) {
                DebugDisplay.warn(player, "SKILL_EXECUTION", "Already executing skill!");
                return;
            }
            
            // サーバーにスキル発動を通知
            ModNetworking.sendToServer(new SkillActivationPacket("test_fire_skill"));
            
            DebugDisplay.info(player, "INPUT_DETECTION", "Skill key pressed by %s", player.getDisplayName().getString());
        }
    }
}