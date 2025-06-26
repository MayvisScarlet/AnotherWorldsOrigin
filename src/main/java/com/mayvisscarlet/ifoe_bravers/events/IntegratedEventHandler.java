package com.mayvisscarlet.ifoe_bravers.events;

import com.mayvisscarlet.ifoe_bravers.ifoe_bravers;
import com.mayvisscarlet.ifoe_bravers.capability.AffinityCapability;
import com.mayvisscarlet.ifoe_bravers.race.RaceManager;
import com.mayvisscarlet.ifoe_bravers.util.DebugDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 統合イベントハンドラー
 * 親和度システムとパトリシア能力を統合管理
 * （PowerFactory根本問題修正版 - 親和度システムのみ管理）
 */
@Mod.EventBusSubscriber(modid = ifoe_bravers.MODID)
public class IntegratedEventHandler {
    
    /**
     * プレイヤーが経験値を獲得した時の処理
     */
    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        int xpGained = event.getAmount();
        
        if (xpGained <= 0) return;
        
        // IfOE_Bravers種族のみ処理
        if (!RaceManager.hasAnyRace(player)) {
            return;
        }
        
        // 親和度データを取得して親和値を追加
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            int oldLevel = affinityData.getAffinityData().getAffinityLevel();
            boolean leveledUp = affinityData.getAffinityData().addAffinityFromXP(xpGained);
            
            if (leveledUp) {
                int newLevel = affinityData.getAffinityData().getAffinityLevel();
                
                // レベルアップ通知（全種族共通）
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        String.format("§l§e親和度レベルアップ！ §r§a%d §7→ §b%d", oldLevel, newLevel)
                    )
                );
                
                // Origins除去後: 属性更新は独自能力システムで自動処理される
                if (RaceManager.isPatricia(player)) {
                    DebugDisplay.debug(player, "AFFINITY_CALCULATION", 
                        "Patricia affinity level changed: %d -> %d (独自システムで自動更新)", 
                        oldLevel, newLevel);
                }
                
                DebugDisplay.info(player, "AFFINITY_CALCULATION", "Player %s affinity level up: %d -> %d", 
                    player.getDisplayName().getString(), oldLevel, newLevel);
            }
            
            // デバッグログ（詳細版）
            if (xpGained >= 10) { // 大きな経験値獲得時のみログ
                DebugDisplay.debug(player, "AFFINITY_CALCULATION", "Player %s gained %d XP -> %.3f affinity points. Level: %d", 
                    player.getDisplayName().getString(), 
                    xpGained, 
                    Math.sqrt(xpGained),
                    affinityData.getAffinityData().getAffinityLevel());
            }
        });
    }
}