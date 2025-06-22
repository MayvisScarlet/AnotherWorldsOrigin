package com.mayvisscarlet.anotherworldsorigin.util;

import com.mayvisscarlet.anotherworldsorigin.client.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * キー入力検知システム
 * WASD移動キーとスキルキーの汎用的な検知・組み合わせ判定を提供
 */
@OnlyIn(Dist.CLIENT)
public class KeyInputDetector {
    
    /**
     * 移動キー定義
     */
    public enum MovementKey {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        NONE
    }
    
    /**
     * スキルキー定義
     */
    public enum SkillKey {
        PRIMARY,
        SECONDARY,
        SPECIAL
    }
    
    /**
     * 入力状態データ
     */
    public static class InputState {
        private final MovementKey movementKey;
        private final SkillKey skillKey;
        private final boolean isMovementPressed;
        private final boolean isSkillPressed;
        private final long timestamp;
        
        public InputState(MovementKey movementKey, SkillKey skillKey, 
                         boolean isMovementPressed, boolean isSkillPressed) {
            this.movementKey = movementKey;
            this.skillKey = skillKey;
            this.isMovementPressed = isMovementPressed;
            this.isSkillPressed = isSkillPressed;
            this.timestamp = System.currentTimeMillis();
        }
        
        public MovementKey getMovementKey() { return movementKey; }
        public SkillKey getSkillKey() { return skillKey; }
        public boolean isMovementPressed() { return isMovementPressed; }
        public boolean isSkillPressed() { return isSkillPressed; }
        public long getTimestamp() { return timestamp; }
        
        public boolean isSimultaneousInput() {
            return isMovementPressed && isSkillPressed;
        }
        
        @Override
        public String toString() {
            return String.format("InputState{movement=%s, skill=%s, movementPressed=%s, skillPressed=%s, simultaneous=%s}",
                    movementKey, skillKey, isMovementPressed, isSkillPressed, isSimultaneousInput());
        }
    }
    
    // ========================================
    // 基本キー検知メソッド
    // ========================================
    
    /**
     * 指定された移動キーが押下されているかチェック
     */
    public static boolean isMovementKeyPressed(MovementKey key) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.options == null) {
                return false;
            }
            
            return switch (key) {
                case FORWARD -> minecraft.options.keyUp.isDown();
                case BACKWARD -> minecraft.options.keyDown.isDown();
                case LEFT -> minecraft.options.keyLeft.isDown();
                case RIGHT -> minecraft.options.keyRight.isDown();
                case NONE -> false;
            };
        } catch (Exception e) {
            DebugDisplay.warn("INPUT_DETECTION", "Failed to check movement key %s: %s", key, e.getMessage());
            return false;
        }
    }
    
    /**
     * 指定されたスキルキーが押下されているかチェック
     */
    public static boolean isSkillKeyPressed(SkillKey key) {
        try {
            return switch (key) {
                case PRIMARY -> ModKeyBindings.SKILL_KEY.isDown(); // 現在はSKILL_KEYを使用
                case SECONDARY -> false; // TODO: 将来のSECONDARYキー実装時
                case SPECIAL -> false; // TODO: 将来のSPECIALキー実装時
            };
        } catch (Exception e) {
            DebugDisplay.warn("INPUT_DETECTION", "Failed to check skill key %s: %s", key, e.getMessage());
            return false;
        }
    }
    
    /**
     * 現在押下されている移動キーを取得
     */
    public static MovementKey getCurrentMovementInput() {
        // 複数同時押下の場合の優先順位: FORWARD > BACKWARD > LEFT > RIGHT
        if (isMovementKeyPressed(MovementKey.FORWARD)) {
            return MovementKey.FORWARD;
        }
        if (isMovementKeyPressed(MovementKey.BACKWARD)) {
            return MovementKey.BACKWARD;
        }
        if (isMovementKeyPressed(MovementKey.LEFT)) {
            return MovementKey.LEFT;
        }
        if (isMovementKeyPressed(MovementKey.RIGHT)) {
            return MovementKey.RIGHT;
        }
        return MovementKey.NONE;
    }
    
    /**
     * 現在押下されているスキルキーを取得
     */
    public static SkillKey getCurrentSkillInput() {
        // 複数同時押下の場合の優先順位: PRIMARY > SECONDARY > SPECIAL
        if (isSkillKeyPressed(SkillKey.PRIMARY)) {
            return SkillKey.PRIMARY;
        }
        if (isSkillKeyPressed(SkillKey.SECONDARY)) {
            return SkillKey.SECONDARY;
        }
        if (isSkillKeyPressed(SkillKey.SPECIAL)) {
            return SkillKey.SPECIAL;
        }
        return null;
    }
    
    // ========================================
    // 汎用組み合わせ判定
    // ========================================
    
    /**
     * 移動キー + スキルキーの同時入力判定
     */
    public static boolean isSkillDerivationInput(MovementKey movement, SkillKey skill) {
        try {
            boolean movementPressed = isMovementKeyPressed(movement);
            boolean skillPressed = isSkillKeyPressed(skill);
            
            boolean result = movementPressed && skillPressed;
            
            if (result) {
                DebugDisplay.debug("INPUT_DETECTION", "Derivation input detected: %s + %s", movement, skill);
            }
            
            return result;
        } catch (Exception e) {
            DebugDisplay.error("INPUT_DETECTION", "Failed to check derivation input %s + %s: %s", 
                             movement, skill, e.getMessage());
            return false;
        }
    }
    
    /**
     * 現在の入力状態を包括的に取得
     */
    public static InputState getCurrentInputState() {
        try {
            MovementKey currentMovement = getCurrentMovementInput();
            SkillKey currentSkill = getCurrentSkillInput();
            
            boolean isMovementPressed = currentMovement != MovementKey.NONE;
            boolean isSkillPressed = currentSkill != null;
            
            return new InputState(currentMovement, currentSkill, isMovementPressed, isSkillPressed);
        } catch (Exception e) {
            DebugDisplay.error("INPUT_DETECTION", "Failed to get current input state: %s", e.getMessage());
            return new InputState(MovementKey.NONE, null, false, false);
        }
    }
    
    /**
     * 任意の移動方向での派生入力判定
     */
    public static boolean isAnyMovementDerivation(SkillKey skill) {
        return isSkillDerivationInput(MovementKey.FORWARD, skill) ||
               isSkillDerivationInput(MovementKey.BACKWARD, skill) ||
               isSkillDerivationInput(MovementKey.LEFT, skill) ||
               isSkillDerivationInput(MovementKey.RIGHT, skill);
    }
    
    // ========================================
    // デバッグ・テスト用メソッド
    // ========================================
    
    /**
     * 全キーの状態をデバッグ出力
     */
    public static void debugAllKeyStates() {
        try {
            MovementKey movement = getCurrentMovementInput();
            SkillKey skill = getCurrentSkillInput();
            InputState state = getCurrentInputState();
            
            DebugDisplay.debug("INPUT_DETECTION", "=== Key States Debug ===");
            DebugDisplay.debug("INPUT_DETECTION", "Current state: %s", state.toString());
            
            // 各移動キーの個別状態
            DebugDisplay.debug("INPUT_DETECTION", "Individual movement keys - W:%s A:%s S:%s D:%s", 
                             isMovementKeyPressed(MovementKey.FORWARD),
                             isMovementKeyPressed(MovementKey.LEFT),
                             isMovementKeyPressed(MovementKey.BACKWARD),
                             isMovementKeyPressed(MovementKey.RIGHT));
            
            // 各スキルキーの個別状態
            DebugDisplay.debug("INPUT_DETECTION", "Individual skill keys - Primary:%s Secondary:%s Special:%s",
                             isSkillKeyPressed(SkillKey.PRIMARY),
                             isSkillKeyPressed(SkillKey.SECONDARY),
                             isSkillKeyPressed(SkillKey.SPECIAL));
            
        } catch (Exception e) {
            DebugDisplay.error("INPUT_DETECTION", "Failed to debug key states: %s", e.getMessage());
        }
    }
    
    /**
     * 特定の組み合わせ入力のテスト
     */
    public static boolean testDerivationInput(MovementKey movement, SkillKey skill) {
        boolean result = isSkillDerivationInput(movement, skill);
        DebugDisplay.info("INPUT_DETECTION", "Test derivation %s + %s: %s", movement, skill, result);
        return result;
    }
}