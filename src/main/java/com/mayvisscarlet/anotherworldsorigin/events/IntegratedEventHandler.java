package com.mayvisscarlet.anotherworldsorigin.events;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.abilities.UnwaveringWinter;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 統合イベントハンドラー
 * 親和度システムとパトリシア能力を統合管理
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class IntegratedEventHandler {
    
    /**
     * プレイヤーが経験値を獲得した時の処理
     */
    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        int xpGained = event.getAmount();
        
        if (xpGained <= 0) return;
        
        // Another Worlds Origin種族のみ処理
        if (!OriginHelper.isAnotherWorldsOriginUser(player)) {
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
                
                // パトリシアの場合、追加の特別処理を実行
                if (OriginHelper.isPatricia(player)) {
                    UnwaveringWinter.onAffinityLevelUp(player, newLevel, oldLevel);
                }
                
                AnotherWorldsOrigin.LOGGER.info("Player {} affinity level up: {} -> {}", 
                    player.getDisplayName().getString(), oldLevel, newLevel);
            }
            
            // デバッグログ（詳細版）
            if (xpGained >= 10) { // 大きな経験値獲得時のみログ
                AnotherWorldsOrigin.LOGGER.debug("Player {} gained {} XP -> {:.3f} affinity points. Level: {}", 
                    player.getDisplayName().getString(), 
                    xpGained, 
                    Math.sqrt(xpGained),
                    affinityData.getAffinityData().getAffinityLevel());
            }
        });
    }
}