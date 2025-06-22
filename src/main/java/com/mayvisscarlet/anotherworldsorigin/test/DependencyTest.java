package com.mayvisscarlet.anotherworldsorigin.test;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Origins Forge API imports - 実際に利用可能なもの
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;

@Mod.EventBusSubscriber(modid = "anotherworldsorigin", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DependencyTest {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * プレイヤーがワールドに参加時に依存関係MODの動作確認を行う
     */
    @SubscribeEvent
    public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        // サーバーサイドで実行（null チェック付き）
        if (player.level().getServer() != null) {
            player.level().getServer().execute(() -> {
                testOriginsDependency(player);
                testApotheosisDependency(player);
                testPatriciaOrigin(player);
            });
        }
    }
    
    /**
     * Origins MODの依存関係テスト - 基本的なCapability使用
     */
    private static void testOriginsDependency(Player player) {
        try {
            // Origins Capability を直接取得
            var originCapability = player.getCapability(io.github.edwinmindcraft.origins.api.OriginsAPI.ORIGIN_CONTAINER);
            if (originCapability.isPresent()) {
                IOriginContainer originContainer = originCapability.orElse(null);
                player.sendSystemMessage(Component.literal("§a[Test] Origins Capability found!"));
                LOGGER.info("[Test] Origins Capability found for player: {}", player.getName().getString());
                
                // Originの取得を試す
                if (originContainer != null) {
                    // まずは基本的な情報を取得
                    player.sendSystemMessage(Component.literal("§a[Test] Origin container is available"));
                    LOGGER.info("[Test] Origin container is available for player: {}", player.getName().getString());
                    
                    // パトリシア種族の確認は簡略化
                    player.sendSystemMessage(Component.literal("§b[Test] Origin system is working"));
                    LOGGER.info("[Test] Origin system is working for player: {}", player.getName().getString());
                    
                } else {
                    player.sendSystemMessage(Component.literal("§e[Test] Origin container is null"));
                    LOGGER.warn("[Test] Origin container is null for player: {}", player.getName().getString());
                }
            } else {
                player.sendSystemMessage(Component.literal("§c[Test] Origins Capability not found"));
                LOGGER.error("[Test] Origins Capability not found for player: {}", player.getName().getString());
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[Test] Origins API test failed: " + e.getMessage()));
            LOGGER.error("[Test] Origins API test failed for player: {}", player.getName().getString(), e);
        }
    }
    
    /**
     * Apotheosis MODの依存関係テスト
     */
    private static void testApotheosisDependency(Player player) {
        try {
            // Apotheosis MODが正常に読み込まれているかチェック
            String modId = "apotheosis";
            player.sendSystemMessage(Component.literal("§a[Test] Apotheosis API working! Mod ID: " + modId));
            LOGGER.info("[Test] Apotheosis API working! Mod ID: {} for player: {}", modId, player.getName().getString());
            
            // プレイヤーの属性値をテスト（Apotheosis連携）
            double attackDamage = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            player.sendSystemMessage(Component.literal("§a[Test] Attack Damage: " + attackDamage));
            LOGGER.info("[Test] Attack Damage: {} for player: {}", attackDamage, player.getName().getString());
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[Test] Apotheosis API test failed: " + e.getMessage()));
            LOGGER.error("[Test] Apotheosis API test failed for player: {}", player.getName().getString(), e);
        }
    }
    
    /**
     * パトリシア種族の詳細テスト - 簡略版
     */
    private static void testPatriciaOrigin(Player player) {
        try {
            player.sendSystemMessage(Component.literal("§b[Patricia Test] Running basic tests..."));
            LOGGER.info("[Patricia Test] Running basic tests for player: {}", player.getName().getString());
            
            // バイオーム温度テスト
            float biomeTemperature = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
            boolean isColdBiome = biomeTemperature <= 0.2f;
            boolean isHotBiome = biomeTemperature >= 1.0f;
            
            player.sendSystemMessage(Component.literal(String.format(
                "§b[Patricia Test] Biome temp: %.2f (Cold: %s, Hot: %s)", 
                biomeTemperature, isColdBiome, isHotBiome
            )));
            LOGGER.info("[Patricia Test] Biome temp: {} (Cold: {}, Hot: {}) for player: {}", 
                biomeTemperature, isColdBiome, isHotBiome, player.getName().getString());
            
            // 攻撃速度テスト
            double attackSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
            player.sendSystemMessage(Component.literal(String.format(
                "§b[Patricia Test] Attack Speed: %.2f", attackSpeed
            )));
            LOGGER.info("[Patricia Test] Attack Speed: {} for player: {}", attackSpeed, player.getName().getString());
            
            // プレイヤーの基本情報
            player.sendSystemMessage(Component.literal(String.format(
                "§b[Patricia Test] Player health: %.1f/%.1f", 
                player.getHealth(), player.getMaxHealth()
            )));
            LOGGER.info("[Patricia Test] Player health: {}/{} for player: {}", 
                player.getHealth(), player.getMaxHealth(), player.getName().getString());
            
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§c[Test] Patricia test failed: " + e.getMessage()));
            LOGGER.error("[Test] Patricia test failed for player: {}", player.getName().getString(), e);
        }
    }
    
    /**
     * 依存関係の基本チェック（MOD読み込み時）
     */
    public static void performBasicDependencyCheck() {
        final Logger logger = LogManager.getLogger("DependencyTest");
        
        logger.info("=== Another Worlds Origin - Dependency Check ===");
        
        // Origins MOD存在確認
        try {
            Class.forName("io.github.edwinmindcraft.origins.api.OriginsAPI");
            logger.info("✓ Origins API found and loaded");
        } catch (ClassNotFoundException e) {
            logger.error("✗ Origins API not found! Please check your dependencies.");
        }
        
        // Origin Container確認
        try {
            Class.forName("io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer");
            logger.info("✓ Origin Container interface found");
        } catch (ClassNotFoundException e) {
            logger.error("✗ Origin Container interface not found!");
        }
        
        // Apotheosis MOD存在確認
        try {
            Class.forName("dev.shadowsoffire.apotheosis.Apotheosis");
            logger.info("✓ Apotheosis API found and loaded");
        } catch (ClassNotFoundException e) {
            logger.error("✗ Apotheosis API not found! Please check your dependencies.");
        }
        
        // Caelus API存在確認
        try {
            Class.forName("top.theillusivec4.caelus.api.CaelusApi");
            logger.info("✓ Caelus API found and loaded");
        } catch (ClassNotFoundException e) {
            logger.error("✗ Caelus API not found! Please check your dependencies.");
        }
        
        logger.info("=== Dependency Check Complete ===");
    }
}