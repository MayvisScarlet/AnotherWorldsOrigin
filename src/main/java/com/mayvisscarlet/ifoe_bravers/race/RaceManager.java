package com.mayvisscarlet.ifoe_bravers.race;

import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 種族管理システム（仮実装）
 * Origins依存関係除去後の独自種族システム
 * 
 * TODO: 後でRaceCapabilityと統合する
 */
public class RaceManager {
    
    // 仮実装：メモリ上での種族管理（セッション終了まで）
    private static final Map<UUID, Race> temporaryRaceMap = new ConcurrentHashMap<>();
    
    /**
     * 現在の種族を取得
     */
    public static Race getCurrentRace(Player player) {
        if (player == null) {
            return Race.NONE;
        }
        
        // 仮実装：最初はパトリシアに設定（テスト用）
        Race race = temporaryRaceMap.get(player.getUUID());
        if (race == null) {
            // TODO: 実装時はRaceCapabilityから取得
            return Race.PATRICIA; // デフォルトでパトリシア（既存機能テスト用）
        }
        return race;
    }
    
    /**
     * 種族を設定
     */
    public static void setRace(Player player, Race race) {
        if (player == null || race == null) {
            return;
        }
        
        Race oldRace = getCurrentRace(player);
        temporaryRaceMap.put(player.getUUID(), race);
        
        // TODO: 実装時はRaceCapabilityに保存
        // TODO: 実装時は能力切り替え処理
        
        onRaceChanged(player, oldRace, race);
    }
    
    /**
     * 種族変更時の処理
     */
    public static void onRaceChanged(Player player, Race oldRace, Race newRace) {
        // TODO: 実装時は能力の適用・除去処理
        
        if (oldRace != newRace) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("§e種族変更: %s → %s", 
                        oldRace.getDisplayName(), 
                        newRace.getDisplayName())
                )
            );
        }
    }
    
    /**
     * パトリシア判定（OriginHelper.isPatricia()の置換）
     */
    public static boolean isPatricia(Player player) {
        return getCurrentRace(player) == Race.PATRICIA;
    }
    
    /**
     * 任意の種族所属確認（OriginHelper.isifoe_braversUser()の置換）
     */
    public static boolean hasAnyRace(Player player) {
        return getCurrentRace(player) != Race.NONE;
    }
    
    /**
     * ユラ判定
     */
    public static boolean isYura(Player player) {
        return getCurrentRace(player) == Race.YURA;
    }
    
    /**
     * カーニス判定
     */
    public static boolean isCarnis(Player player) {
        return getCurrentRace(player) == Race.CARNIS;
    }
    
    /**
     * ヴォレイ判定
     */
    public static boolean isVorey(Player player) {
        return getCurrentRace(player) == Race.VOREY;
    }
}