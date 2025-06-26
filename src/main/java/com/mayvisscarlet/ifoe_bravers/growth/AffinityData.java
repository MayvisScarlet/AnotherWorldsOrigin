package com.mayvisscarlet.ifoe_bravers.growth;

import net.minecraft.nbt.CompoundTag;

/**
 * プレイヤーの親和度データを管理するクラス
 * MinecraftのXPシステムと同じロジックを使用
 * double型による精密計算対応
 */
public class AffinityData {
    private double totalAffinityPoints = 0.0;  // 累積親和値（小数対応）
    private int affinityLevel = 0;             // 現在の親和度
    private double currentLevelPoints = 0.0;   // 現在のレベルでの親和値（小数対応）
    
    /**
     * 親和値を追加（Minecraft経験値の平方根）
     * @param minecraftXP 獲得したMinecraft経験値
     * @return レベルアップしたかどうか
     */
    public boolean addAffinityFromXP(int minecraftXP) {
        if (minecraftXP <= 0) return false;
        
        // 平方根を精密計算して親和値として追加
        double affinityPoints = Math.sqrt(minecraftXP);
        return addAffinityPoints(affinityPoints);
    }
    
    /**
     * 親和値を直接追加
     * @param points 追加する親和値
     * @return レベルアップしたかどうか
     */
    public boolean addAffinityPoints(double points) {
        if (points <= 0.0) return false;
        
        this.totalAffinityPoints += points;
        this.currentLevelPoints += points;
        
        return checkLevelUp();
    }
    
    /**
     * レベルアップチェック（MinecraftのXPシステムと同じ）
     */
    private boolean checkLevelUp() {
        boolean leveledUp = false;
        
        while (this.currentLevelPoints >= getPointsToNextLevel()) {
            this.currentLevelPoints -= getPointsToNextLevel();
            this.affinityLevel++;
            leveledUp = true;
        }
        
        return leveledUp;
    }
    
    /**
     * 次のレベルまでに必要な親和値を計算（MinecraftのXPシステム準拠）
     */
    public int getPointsToNextLevel() {
        if (affinityLevel >= 30) {
            return 112 + (affinityLevel - 30) * 9;
        } else if (affinityLevel >= 15) {
            return 37 + (affinityLevel - 15) * 5;
        } else {
            return 7 + affinityLevel * 2;
        }
    }
    
    /**
     * 指定レベルの総親和値を計算
     */
    public static int getTotalPointsForLevel(int level) {
        if (level <= 0) return 0;
        
        int total = 0;
        for (int i = 0; i < level; i++) {
            if (i >= 30) {
                total += 112 + (i - 30) * 9;
            } else if (i >= 15) {
                total += 37 + (i - 15) * 5;
            } else {
                total += 7 + i * 2;
            }
        }
        return total;
    }
    
    // Getters
    public int getAffinityLevel() { return affinityLevel; }
    public double getCurrentLevelPoints() { return currentLevelPoints; }
    public double getTotalAffinityPoints() { return totalAffinityPoints; }
    
    /**
     * 進行度を0.0-1.0で取得
     */
    public double getLevelProgress() {
        int needed = getPointsToNextLevel();
        return needed > 0 ? currentLevelPoints / needed : 0.0;
    }
    
    /**
     * NBTに保存（double対応）
     */
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("TotalAffinityPoints", totalAffinityPoints);
        tag.putInt("AffinityLevel", affinityLevel);
        tag.putDouble("CurrentLevelPoints", currentLevelPoints);
        return tag;
    }
    
    /**
     * NBTから読み込み（double対応）
     */
    public void loadFromNBT(CompoundTag tag) {
        this.totalAffinityPoints = tag.getDouble("TotalAffinityPoints");
        this.affinityLevel = tag.getInt("AffinityLevel");
        this.currentLevelPoints = tag.getDouble("CurrentLevelPoints");
        
        // データ整合性チェック
        validateData();
    }
    
    /**
     * データの整合性をチェック・修正（double対応）
     */
    private void validateData() {
        // 総親和値から正しいレベルを再計算
        int correctLevel = 0;
        double remainingPoints = totalAffinityPoints;
        
        while (remainingPoints > 0.0) {
            int needed = (correctLevel >= 30) ? 112 + (correctLevel - 30) * 9 :
                         (correctLevel >= 15) ? 37 + (correctLevel - 15) * 5 :
                         7 + correctLevel * 2;
            
            if (remainingPoints >= needed) {
                remainingPoints -= needed;
                correctLevel++;
            } else {
                break;
            }
        }
        
        this.affinityLevel = correctLevel;
        this.currentLevelPoints = remainingPoints;
    }
    
    /**
     * 親和値の詳細情報を取得（デバッグ用）
     */
    public String getDetailedInfo() {
        return String.format("AffinityLevel: %d, Progress: %.3f/%d (%.1f%%), Total: %.3f", 
                affinityLevel, currentLevelPoints, getPointsToNextLevel(), 
                getLevelProgress() * 100, totalAffinityPoints);
    }
    
    @Override
    public String toString() {
        return String.format("AffinityLevel: %d, Progress: %.2f/%d (%.1f%%)", 
                affinityLevel, currentLevelPoints, getPointsToNextLevel(), 
                getLevelProgress() * 100);
    }
}