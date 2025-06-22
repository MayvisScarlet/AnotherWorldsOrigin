package com.mayvisscarlet.anotherworldsorigin.util;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Logger;

/**
 * デバッグ表示統合システム
 * LOGGER・プレイヤーチャット表示を一元管理し、カテゴリ別設定で表示先を制御
 */
public class DebugDisplay {
    private static final Logger LOGGER = AnotherWorldsOrigin.LOGGER;
    
    /**
     * デバッグレベル定義
     */
    public enum DebugLevel {
        ERROR("§c", "ERROR"),
        WARN("§e", "WARN"),
        INFO("§a", "INFO"),
        DEBUG("§b", "DEBUG"),
        TRACE("§7", "TRACE");
        
        private final String colorCode;
        private final String levelName;
        
        DebugLevel(String colorCode, String levelName) {
            this.colorCode = colorCode;
            this.levelName = levelName;
        }
        
        public String getColorCode() {
            return colorCode;
        }
        
        public String getLevelName() {
            return levelName;
        }
    }
    
    /**
     * 表示先制御
     */
    public enum DisplayTarget {
        LOGGER_ONLY,    // ログファイルのみ
        CHAT_ONLY,      // プレイヤーチャットのみ
        BOTH,           // 両方
        NONE            // 表示無し（本番環境）
    }
    
    /**
     * 統一デバッグ表示メソッド
     * 
     * @param level デバッグレベル
     * @param player 対象プレイヤー（null可）
     * @param category カテゴリ名
     * @param message メッセージフォーマット
     * @param args フォーマット引数
     */
    public static void display(DebugLevel level, Player player, String category, String message, Object... args) {
        try {
            // 設定取得
            DisplayTarget target = getDisplayTarget(category);
            if (target == DisplayTarget.NONE) {
                return; // 表示無効
            }
            
            // メッセージフォーマット
            String formattedMessage = String.format(message, args);
            
            // ログ出力
            if (target == DisplayTarget.LOGGER_ONLY || target == DisplayTarget.BOTH) {
                logToFile(level, category, formattedMessage);
            }
            
            // チャット出力
            if (target == DisplayTarget.CHAT_ONLY || target == DisplayTarget.BOTH) {
                if (player != null && isChatDebugEnabled(category)) {
                    sendToChat(player, level, category, formattedMessage);
                }
            }
            
        } catch (Exception e) {
            // デバッグシステム自体のエラーは最小限のログ出力
            LOGGER.error("[DebugDisplay] Error in debug display: {}", e.getMessage());
        }
    }
    
    /**
     * ログファイルへの出力
     */
    private static void logToFile(DebugLevel level, String category, String formattedMessage) {
        String logMessage = "[{}] {}";
        switch (level) {
            case ERROR -> LOGGER.error(logMessage, category, formattedMessage);
            case WARN -> LOGGER.warn(logMessage, category, formattedMessage);
            case INFO -> LOGGER.info(logMessage, category, formattedMessage);
            case DEBUG -> LOGGER.debug(logMessage, category, formattedMessage);
            case TRACE -> LOGGER.trace(logMessage, category, formattedMessage);
        }
    }
    
    /**
     * プレイヤーチャットへの出力
     */
    private static void sendToChat(Player player, DebugLevel level, String category, String formattedMessage) {
        String chatMessage = String.format("%s[%s] §f%s", 
            level.getColorCode(), 
            category, 
            formattedMessage
        );
        player.sendSystemMessage(Component.literal(chatMessage));
    }
    
    /**
     * カテゴリの表示先設定取得
     */
    private static DisplayTarget getDisplayTarget(String category) {
        try {
            // TODO: ConfigManagerの拡張実装後に設定から取得
            // 現在は暫定的にデフォルト設定
            return getDefaultDisplayTarget(category);
        } catch (Exception e) {
            LOGGER.warn("[DebugDisplay] Failed to get display target for category: {}, using default", category);
            return DisplayTarget.BOTH; // デフォルト
        }
    }
    
    /**
     * デフォルト表示先設定
     */
    private static DisplayTarget getDefaultDisplayTarget(String category) {
        return switch (category) {
            case "HEAT_DAMAGE", "AFFINITY_CALCULATION", "COLD_DAMAGE" -> DisplayTarget.BOTH;
            case "SKILL_EXECUTION", "INPUT_DETECTION" -> DisplayTarget.CHAT_ONLY;
            case "PERFORMANCE", "CACHE_MANAGEMENT" -> DisplayTarget.LOGGER_ONLY;
            default -> DisplayTarget.BOTH;
        };
    }
    
    /**
     * チャットデバッグ有効判定
     */
    private static boolean isChatDebugEnabled(String category) {
        try {
            // TODO: ConfigManagerの拡張実装後に設定から取得
            // 現在は暫定的にデフォルト設定
            return getDefaultChatEnabled(category);
        } catch (Exception e) {
            return true; // デフォルトで有効
        }
    }
    
    /**
     * デフォルトチャット表示設定
     */
    private static boolean getDefaultChatEnabled(String category) {
        return switch (category) {
            case "PERFORMANCE", "CACHE_MANAGEMENT" -> false; // 内部処理は非表示
            default -> true;
        };
    }
    
    // ========================================
    // 簡便メソッド群
    // ========================================
    
    /**
     * INFOレベルでの表示
     */
    public static void info(Player player, String category, String message, Object... args) {
        display(DebugLevel.INFO, player, category, message, args);
    }
    
    /**
     * DEBUGレベルでの表示
     */
    public static void debug(Player player, String category, String message, Object... args) {
        display(DebugLevel.DEBUG, player, category, message, args);
    }
    
    /**
     * ERRORレベルでの表示
     */
    public static void error(Player player, String category, String message, Object... args) {
        display(DebugLevel.ERROR, player, category, message, args);
    }
    
    /**
     * WARNレベルでの表示
     */
    public static void warn(Player player, String category, String message, Object... args) {
        display(DebugLevel.WARN, player, category, message, args);
    }
    
    /**
     * TRACEレベルでの表示
     */
    public static void trace(Player player, String category, String message, Object... args) {
        display(DebugLevel.TRACE, player, category, message, args);
    }
    
    // ========================================
    // プレイヤー無しバージョン（ログのみ）
    // ========================================
    
    /**
     * プレイヤー指定無しでのINFO表示（ログのみ）
     */
    public static void info(String category, String message, Object... args) {
        display(DebugLevel.INFO, null, category, message, args);
    }
    
    /**
     * プレイヤー指定無しでのDEBUG表示（ログのみ）
     */
    public static void debug(String category, String message, Object... args) {
        display(DebugLevel.DEBUG, null, category, message, args);
    }
    
    /**
     * プレイヤー指定無しでのERROR表示（ログのみ）
     */
    public static void error(String category, String message, Object... args) {
        display(DebugLevel.ERROR, null, category, message, args);
    }
    
    /**
     * プレイヤー指定無しでのWARN表示（ログのみ）
     */
    public static void warn(String category, String message, Object... args) {
        display(DebugLevel.WARN, null, category, message, args);
    }
}