package com.mayvisscarlet.ifoe_bravers.race;

/**
 * 種族列挙型
 * Origins依存関係除去後の独自種族システム
 */
public enum Race {
    NONE("無所属", "none"),
    PATRICIA("パトリシア", "patricia"), 
    YURA("ユラ", "yura"),
    CARNIS("カーニス", "carnis"),
    VOREY("ヴォレイ", "vorey");
    
    private final String displayName;
    private final String id;
    
    Race(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * IDから種族を取得
     */
    public static Race fromId(String id) {
        for (Race race : values()) {
            if (race.getId().equals(id)) {
                return race;
            }
        }
        return NONE;
    }
}