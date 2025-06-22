
// === 6. テスト用火炎スキルクラス ===
package com.mayvisscarlet.anotherworldsorigin.skills;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TestFireSkill {
    
    private static final String SKILL_NAME = "Test Fire Skill";
    private static final int SKILL_DURATION = 60; // 3秒 (20ticks * 3)
    
    /**
     * スキルを実行
     */
    public static void execute(ServerPlayer player) {
        // スキル実行状態を設定
        SkillExecutionManager.startSkillExecution(player, SKILL_NAME, SKILL_DURATION);
        
        AnotherWorldsOrigin.LOGGER.info("Executing {} for {}", SKILL_NAME, player.getDisplayName().getString());
    }
    
    /**
     * スキル効果のティック処理（毎ティック呼び出し）
     */
    public static void tickEffect(ServerPlayer player) {
        if (!SkillExecutionManager.isExecutingSkill(player)) {
            return;
        }
        
        SkillExecutionManager.SkillData skillData = SkillExecutionManager.getCurrentSkill(player);
        if (skillData == null || !SKILL_NAME.equals(skillData.getSkillName())) {
            return;
        }
        
        // プレイヤーの前方に炎パーティクルを生成
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();
        
        // プレイヤーの前方1.5ブロック地点
        Vec3 particlePos = playerPos.add(lookVec.scale(1.5)).add(0, player.getEyeHeight(), 0);
        
        if (player.level() instanceof ServerLevel serverLevel) {
            // 炎パーティクルを5個生成
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 0.3;
                double offsetY = (Math.random() - 0.5) * 0.3;
                double offsetZ = (Math.random() - 0.5) * 0.3;
                
                serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    particlePos.x + offsetX,
                    particlePos.y + offsetY,
                    particlePos.z + offsetZ,
                    1, // パーティクル数
                    0, 0, 0, // 速度
                    0.02 // 速度ランダム性
                );
            }
        }
        
        // スキル実行時間を更新
        SkillExecutionManager.updateSkillExecution(player);
    }
}