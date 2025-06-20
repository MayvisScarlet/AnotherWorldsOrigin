// === 2. スキル実行状態管理クラス ===
package com.mayvisscarlet.anotherworldsorigin.skills;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillExecutionManager {
    
    private static final Map<UUID, SkillData> ACTIVE_SKILLS = new ConcurrentHashMap<>();
    
    public static class SkillData {
        private final String skillName;
        private int remainingTicks;
        
        public SkillData(String skillName, int durationTicks) {
            this.skillName = skillName;
            this.remainingTicks = durationTicks;
        }
        
        public void decrementDuration() { remainingTicks--; }
        public boolean isExpired() { return remainingTicks <= 0; }
        public String getSkillName() { return skillName; }
        public int getRemainingTicks() { return remainingTicks; }
    }
    
    /**
     * スキル実行を開始
     */
    public static void startSkillExecution(Player player, String skillName, int durationTicks) {
        UUID playerId = player.getUUID();
        ACTIVE_SKILLS.put(playerId, new SkillData(skillName, durationTicks));
        
        AnotherWorldsOrigin.LOGGER.info("Started skill execution for {}: {} ({}ticks)", 
            player.getDisplayName().getString(), skillName, durationTicks);
        
        // プレイヤーに通知
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("§b[Skill] §f" + skillName + " activated!"), 
            true
        );
    }
    
    /**
     * スキル実行を終了
     */
    public static void endSkillExecution(Player player) {
        UUID playerId = player.getUUID();
        SkillData data = ACTIVE_SKILLS.remove(playerId);
        
        if (data != null) {
            AnotherWorldsOrigin.LOGGER.info("Ended skill execution for {}: {}", 
                player.getDisplayName().getString(), data.getSkillName());
            
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§b[Skill] §f" + data.getSkillName() + " ended"), 
                true
            );
        }
    }
    
    /**
     * スキル実行中かチェック
     */
    public static boolean isExecutingSkill(Player player) {
        return ACTIVE_SKILLS.containsKey(player.getUUID());
    }
    
    /**
     * 現在のスキルデータを取得
     */
    public static SkillData getCurrentSkill(Player player) {
        return ACTIVE_SKILLS.get(player.getUUID());
    }
    
    /**
     * スキル実行時間を更新（ティック処理用）
     */
    public static void updateSkillExecution(Player player) {
        UUID playerId = player.getUUID();
        SkillData data = ACTIVE_SKILLS.get(playerId);
        
        if (data != null) {
            data.decrementDuration();
            if (data.isExpired()) {
                endSkillExecution(player);
            }
        }
    }
    
    /**
     * 全プレイヤーの状態をクリア（デバッグ用）
     */
    public static void clearAllSkills() {
        ACTIVE_SKILLS.clear();
        AnotherWorldsOrigin.LOGGER.info("Cleared all skill executions");
    }
}