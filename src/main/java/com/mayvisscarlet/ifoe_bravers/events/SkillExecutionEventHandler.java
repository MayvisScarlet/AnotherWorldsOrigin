package com.mayvisscarlet.ifoe_bravers.events;

import com.mayvisscarlet.ifoe_bravers.ifoe_bravers;
import com.mayvisscarlet.ifoe_bravers.skills.SkillExecutionManager;
import com.mayvisscarlet.ifoe_bravers.skills.TestFireSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ifoe_bravers.MODID)
public class SkillExecutionEventHandler {
    
    /**
     * スキル実行中の攻撃制限
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            event.setCanceled(true);
            
            ifoe_bravers.LOGGER.debug("Blocked attack during skill execution for {}", 
                player.getDisplayName().getString());
        }
    }
    
    /**
     * スキル実行中の左クリック制限（ブロック破壊等）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                ifoe_bravers.LOGGER.debug("Blocked left click block during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中の右クリック制限（アイテム使用等）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                // プレイヤーに通知（1秒に1回制限）
                if (player.tickCount % 20 == 0) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c[Skill] §fItem use blocked during skill execution"), 
                        true
                    );
                }
                
                ifoe_bravers.LOGGER.debug("Blocked right click item during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中の右クリック制限（ブロックに対して）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                ifoe_bravers.LOGGER.debug("Blocked right click block during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中のアイテム使用開始制限
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                // プレイヤーに通知（1秒に1回制限）
                if (player.tickCount % 20 == 0) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c[Skill] §fItem use blocked during skill execution"), 
                        true
                    );
                }
                
                ifoe_bravers.LOGGER.debug("Blocked item use start during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中のアイテム使用継続制限
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                ifoe_bravers.LOGGER.debug("Blocked item use tick during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中のブロック破壊制限
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                ifoe_bravers.LOGGER.debug("Blocked block break during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中のブロック設置制限
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                
                ifoe_bravers.LOGGER.debug("Blocked block place during skill execution for {}", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * スキル実行中の乗り物搭乗制限（汎用的対応）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityMount(net.minecraftforge.event.entity.EntityMountEvent event) {
        if (event.getEntity() instanceof Player player && SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable() && event.isMounting()) {
                event.setCanceled(true);
                
                // プレイヤーに通知（1秒に1回制限）
                if (player.tickCount % 20 == 0) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c[Skill] §fCannot mount entities during skill execution"), 
                        true
                    );
                }
                
                ifoe_bravers.LOGGER.debug("Blocked entity mount during skill execution for {}: {}", 
                    player.getDisplayName().getString(), event.getEntityBeingMounted().getClass().getSimpleName());
            }
        }
    }
    
    /**
     * スキル実行中のエンティティ交流制限（完全汎用対応）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (SkillExecutionManager.isExecutingSkill(player)) {
            if (event.isCancelable()) {
                // 設定可能な制限レベル
                EntityInteractionLevel restrictionLevel = getEntityInteractionRestriction();
                
                switch (restrictionLevel) {
                    case ALL_ENTITIES:
                        // 全エンティティとの交流を制限
                        event.setCanceled(true);
                        showEntityInteractionMessage(player, "entities", event.getTarget());
                        break;
                        
                    case VEHICLES_AND_NPCS:
                        // 乗り物とNPCのみ制限
                        if (isLikelyVehicleOrNPC(event.getTarget())) {
                            event.setCanceled(true);
                            showEntityInteractionMessage(player, "vehicles/NPCs", event.getTarget());
                        }
                        break;
                        
                    case VEHICLES_ONLY:
                        // 乗り物のみ制限
                        if (isLikelyVehicle(event.getTarget())) {
                            event.setCanceled(true);
                            showEntityInteractionMessage(player, "vehicles", event.getTarget());
                        }
                        break;
                        
                    case NONE:
                    default:
                        // 制限なし
                        break;
                }
            }
        }
    }
    
    /**
     * エンティティ交流制限レベル
     */
    public enum EntityInteractionLevel {
        NONE,                // 制限なし
        VEHICLES_ONLY,       // 乗り物のみ制限
        VEHICLES_AND_NPCS,   // 乗り物とNPC制限
        ALL_ENTITIES         // 全エンティティ制限
    }
    
    /**
     * 現在の制限レベルを取得（設定可能）
     */
    private static EntityInteractionLevel getEntityInteractionRestriction() {
        // TODO: 設定ファイルから読み込み可能にする
        return EntityInteractionLevel.VEHICLES_AND_NPCS; // デフォルト
    }
    
    /**
     * 乗り物らしいエンティティかを汎用的に判定
     */
    private static boolean isLikelyVehicle(net.minecraft.world.entity.Entity entity) {
        String className = entity.getClass().getSimpleName().toLowerCase();
        String packageName = entity.getClass().getPackage().getName().toLowerCase();
        
        // クラス名による判定
        boolean nameIndicatesVehicle = className.contains("boat") ||
                                     className.contains("cart") ||
                                     className.contains("minecart") ||
                                     className.contains("horse") ||
                                     className.contains("vehicle") ||
                                     className.contains("mount") ||
                                     className.contains("rideable") ||
                                     className.contains("camel") ||
                                     className.contains("strider");
        
        // パッケージ名による判定
        boolean packageIndicatesVehicle = packageName.contains("vehicle") ||
                                        packageName.contains("transportation");
        
        // Minecraft標準の乗り物
        boolean isVanillaVehicle = entity instanceof net.minecraft.world.entity.vehicle.AbstractMinecart ||
                                 entity instanceof net.minecraft.world.entity.vehicle.Boat ||
                                 entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse ||
                                 (entity instanceof net.minecraft.world.entity.animal.Pig && 
                                  ((net.minecraft.world.entity.animal.Pig)entity).isSaddled()) ||
                                 entity instanceof net.minecraft.world.entity.animal.camel.Camel ||
                                 entity instanceof net.minecraft.world.entity.monster.Strider;
        
        return nameIndicatesVehicle || packageIndicatesVehicle || isVanillaVehicle;
    }
    
    /**
     * 乗り物またはNPCらしいエンティティかを汎用的に判定
     */
    private static boolean isLikelyVehicleOrNPC(net.minecraft.world.entity.Entity entity) {
        if (isLikelyVehicle(entity)) {
            return true;
        }
        
        String className = entity.getClass().getSimpleName().toLowerCase();
        String packageName = entity.getClass().getPackage().getName().toLowerCase();
        
        // NPC系の判定
        boolean nameIndicatesNPC = className.contains("villager") ||
                                 className.contains("npc") ||
                                 className.contains("trader") ||
                                 className.contains("merchant") ||
                                 className.contains("guard") ||
                                 className.contains("citizen");
        
        boolean packageIndicatesNPC = packageName.contains("npc") ||
                                    packageName.contains("villager") ||
                                    packageName.contains("citizen");
        
        // Minecraft標準のNPC
        boolean isVanillaNPC = entity instanceof net.minecraft.world.entity.npc.AbstractVillager ||
                             entity instanceof net.minecraft.world.entity.animal.Animal;
        
        return nameIndicatesNPC || packageIndicatesNPC || isVanillaNPC;
    }
    
    /**
     * エンティティ交流制限メッセージを表示
     */
    private static void showEntityInteractionMessage(Player player, String entityType, net.minecraft.world.entity.Entity target) {
        // 1秒に1回制限でメッセージ表示
        if (player.tickCount % 20 == 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c[Skill] §fCannot interact with " + entityType + " during skill execution"), 
                true
            );
        }
        
        ifoe_bravers.LOGGER.debug("Blocked {} interaction during skill execution for {}: {} ({})", 
            entityType, player.getDisplayName().getString(), 
            target.getClass().getSimpleName(), target.getClass().getPackage().getName());
    }
    
    /**
     * スキル効果のティック処理
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // スキル実行中の場合、効果を実行
            if (SkillExecutionManager.isExecutingSkill(player)) {
                TestFireSkill.tickEffect(player);
            }
        }
    }
}