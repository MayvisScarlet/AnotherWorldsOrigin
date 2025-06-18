package com.mayvisscarlet.anotherworldsorigin.events;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * プレイヤーの経験値獲得を監視して親和値に変換
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class AffinityEventHandler {
    
    /**
     * プレイヤーが経験値を獲得した時の処理
     */
    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        int xpGained = event.getAmount();
        
        if (xpGained <= 0) return;
        
        // Another Worlds Originの種族を選択しているかチェック
        if (!isAnotherWorldsOriginUser(player)) {
            return;
        }
        
        // 親和度データを取得して親和値を追加
        AffinityCapability.getAffinityData(player).ifPresent(affinityData -> {
            boolean leveledUp = affinityData.getAffinityData().addAffinityFromXP(xpGained);
            
            if (leveledUp) {
                int newLevel = affinityData.getAffinityData().getAffinityLevel();
                AnotherWorldsOrigin.LOGGER.debug("Player {} affinity level up to {}", 
                    player.getDisplayName().getString(), newLevel);
                
                // 将来的にここでレベルアップ通知やエフェクトを追加可能
            }
            
            // デバッグログ（double精度対応）
            AnotherWorldsOrigin.LOGGER.debug("Player {} gained {} XP -> {:.6f} affinity points. Current: {}", 
                player.getDisplayName().getString(), 
                xpGained, 
                Math.sqrt(xpGained),
                affinityData.getAffinityData().getDetailedInfo());
        });
    }
    
    /**
     * プレイヤーがAnother Worlds Originの種族を使用しているかチェック
     * 現在は簡易実装（実際のOrigins連携は後で実装）
     */
    private static boolean isAnotherWorldsOriginUser(Player player) {
        // TODO: 実際のOrigins APIを使用して種族をチェック
        // 現在は開発用として常にtrueを返す
        return true;
        
        /* 将来の実装例:
        try {
            // Origins APIを使用した種族チェック
            Optional<Origin> origin = OriginComponent.getOriginFromPlayer(player, OriginLayers.getLayer("origins:origin"));
            return origin.isPresent() && 
                   origin.get().getIdentifier().getNamespace().equals(AnotherWorldsOrigin.MODID);
        } catch (Exception e) {
            return false;
        }
        */
    }
}