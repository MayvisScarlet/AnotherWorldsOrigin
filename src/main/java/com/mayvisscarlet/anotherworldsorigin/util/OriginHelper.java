package com.mayvisscarlet.anotherworldsorigin.util;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Origins MODとの連携用ユーティリティクラス（修正版）
 * プレイヤーの種族判定を行う
 */
public class OriginHelper {
    
    // Another Worlds Originの種族ID
    public static final ResourceLocation PATRICIA_ORIGIN = 
        ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "patricia");
    public static final ResourceLocation YURA_ORIGIN = 
        ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "yura");
    public static final ResourceLocation CARNIS_ORIGIN = 
        ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "carnis");
    public static final ResourceLocation VOREY_ORIGIN = 
        ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "vorey");
    
    // Origins標準レイヤー
    private static final ResourceLocation ORIGIN_LAYER = 
        ResourceLocation.fromNamespaceAndPath("origins", "origin");
    
    /**
     * プレイヤーがパトリシア種族かどうかを判定
     */
    public static boolean isPatricia(Player player) {
        return hasOrigin(player, PATRICIA_ORIGIN);
    }
    
    /**
     * プレイヤーがユラ種族かどうかを判定
     */
    public static boolean isYura(Player player) {
        return hasOrigin(player, YURA_ORIGIN);
    }
    
    /**
     * プレイヤーがカーニス種族かどうかを判定
     */
    public static boolean isCarnis(Player player) {
        return hasOrigin(player, CARNIS_ORIGIN);
    }
    
    /**
     * プレイヤーがヴォレイ種族かどうかを判定
     */
    public static boolean isVorey(Player player) {
        return hasOrigin(player, VOREY_ORIGIN);
    }
    
    /**
     * プレイヤーがAnother Worlds Originの任意の種族かどうかを判定
     */
    public static boolean isAnotherWorldsOriginUser(Player player) {
        return isPatricia(player) || isYura(player) || isCarnis(player) || isVorey(player);
    }
    
    /**
     * プレイヤーが指定した種族を持っているかチェック（修正版）
     */
    public static boolean hasOrigin(Player player, ResourceLocation originId) {
        if (player == null || originId == null) {
            return false;
        }
        
        try {
            // Origins Capabilityを取得
            LazyOptional<IOriginContainer> originCapOptional = 
                player.getCapability(OriginsAPI.ORIGIN_CONTAINER);
            
            if (!originCapOptional.isPresent()) {
                AnotherWorldsOrigin.LOGGER.debug("Origin capability not found for player: {}", 
                    player.getDisplayName().getString());
                return false;
            }
            
            IOriginContainer originContainer = originCapOptional.orElse(null);
            if (originContainer == null) {
                AnotherWorldsOrigin.LOGGER.debug("Origin container is null for player: {}", 
                    player.getDisplayName().getString());
                return false;
            }
            
            // 標準のOriginレイヤーを取得
            OriginLayer originLayer = OriginsAPI.getLayersRegistry().get(ORIGIN_LAYER);
            if (originLayer == null) {
                AnotherWorldsOrigin.LOGGER.warn("Standard origin layer not found!");
                return false;
            }
            
            // プレイヤーの現在のOriginを取得（新API対応）
            ResourceKey<Origin> playerOriginKey = originContainer.getOrigin(originLayer);
            if (playerOriginKey == null) {
                AnotherWorldsOrigin.LOGGER.debug("No origin found for player: {} in layer: {}", 
                    player.getDisplayName().getString(), ORIGIN_LAYER);
                return false;
            }
            
            // ResourceKeyからResourceLocationを取得して比較
            ResourceLocation playerOriginId = playerOriginKey.location();
            boolean matches = originId.equals(playerOriginId);
            
            if (matches) {
                AnotherWorldsOrigin.LOGGER.debug("Player {} has origin: {}", 
                    player.getDisplayName().getString(), originId);
            }
            
            return matches;
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Error checking origin for player {}: {}", 
                player.getDisplayName().getString(), e.getMessage());
            return false;
        }
    }
    
    /**
     * プレイヤーの現在のOriginを取得（デバッグ用・修正版）
     */
    public static ResourceLocation getCurrentOrigin(Player player) {
        if (player == null) {
            return null;
        }
        
        try {
            LazyOptional<IOriginContainer> originCapOptional = 
                player.getCapability(OriginsAPI.ORIGIN_CONTAINER);
            
            if (!originCapOptional.isPresent()) {
                return null;
            }
            
            IOriginContainer originContainer = originCapOptional.orElse(null);
            if (originContainer == null) {
                return null;
            }
            
            OriginLayer originLayer = OriginsAPI.getLayersRegistry().get(ORIGIN_LAYER);
            if (originLayer == null) {
                return null;
            }
            
            ResourceKey<Origin> playerOriginKey = originContainer.getOrigin(originLayer);
            if (playerOriginKey == null) {
                return null;
            }
            
            return playerOriginKey.location();
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Error getting current origin for player {}: {}", 
                player.getDisplayName().getString(), e.getMessage());
            return null;
        }
    }
    
    /**
     * デバッグ用：プレイヤーのOrigin情報をログ出力
     */
    public static void debugOriginInfo(Player player) {
        try {
            ResourceLocation currentOrigin = getCurrentOrigin(player);
            
            AnotherWorldsOrigin.LOGGER.info("=== Origin Debug Info for {} ===", 
                player.getDisplayName().getString());
            AnotherWorldsOrigin.LOGGER.info("Current Origin: {}", 
                currentOrigin != null ? currentOrigin.toString() : "None");
            AnotherWorldsOrigin.LOGGER.info("Is Patricia: {}", isPatricia(player));
            AnotherWorldsOrigin.LOGGER.info("Is Another Worlds Origin User: {}", isAnotherWorldsOriginUser(player));
            AnotherWorldsOrigin.LOGGER.info("=== End Debug Info ===");
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Failed to debug origin info for player {}: {}", 
                player.getDisplayName().getString(), e.getMessage());
        }
    }
}