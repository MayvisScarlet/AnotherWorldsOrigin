package com.mayvisscarlet.anotherworldsorigin.events;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.registry.ModPowerTypes;
import io.github.edwinmindcraft.apoli.api.component.IPowerContainer;
import io.github.edwinmindcraft.apoli.api.power.configuration.ConfiguredPower;
import io.github.edwinmindcraft.apoli.common.ApoliCommon;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * PowerFactory統合イベントハンドラー
 * Origins PowerFactoryとForgeイベントシステムを連携
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class PowerEventHandler {
    
    /**
     * ポーション効果適用の阻止
     */
    @SubscribeEvent
    public static void onPotionEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // 採掘速度低下効果をチェック
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            // プレイヤーがUnwaveringWinterパワーを持っているかチェック
            if (hasUnwaveringWinterPower(player)) {
                event.setResult(Event.Result.DENY);
                
                AnotherWorldsOrigin.LOGGER.debug("Player {} UnwaveringWinter blocked DIG_SLOWDOWN effect", 
                    player.getDisplayName().getString());
            }
        }
    }
    
    /**
     * 追加されたポーション効果の即座除去
     */
    @SubscribeEvent
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (event.getEffectInstance().getEffect() == MobEffects.DIG_SLOWDOWN) {
            if (hasUnwaveringWinterPower(player)) {
                // 次のティックで除去
                if (player.getServer() != null) {
                    player.getServer().execute(() -> {
                        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                            player.removeEffect(MobEffects.DIG_SLOWDOWN);
                            
                            AnotherWorldsOrigin.LOGGER.debug("Player {} UnwaveringWinter removed DIG_SLOWDOWN after addition", 
                                player.getDisplayName().getString());
                        }
                    });
                }
            }
        }
    }
    
    /**
     * ダメージ軽減の適用（Cold系バイオーム）
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // UnwaveringWinterパワーを取得
        getUnwaveringWinterPower(player).ifPresent(power -> {
            UnwaveringWinterPowerFactory factory = (UnwaveringWinterPowerFactory) power.getPowerFactory();
            float damageReduction = factory.calculateDamageReduction(player, power.getConfiguration());
            
            if (damageReduction < 1.0f) {
                float originalDamage = event.getAmount();
                float newDamage = originalDamage * damageReduction;
                event.setAmount(newDamage);
                
                AnotherWorldsOrigin.LOGGER.debug("Player {} cold biome damage reduction: {:.2f} -> {:.2f}", 
                    player.getDisplayName().getString(), originalDamage, newDamage);
            }
        });
    }
    
    /**
     * プレイヤーがUnwaveringWinterパワーを持っているかチェック
     */
    private static boolean hasUnwaveringWinterPower(Player player) {
        return getUnwaveringWinterPower(player).isPresent();
    }
    
    /**
     * プレイヤーのUnwaveringWinterパワーを取得
     */
    private static java.util.Optional<ConfiguredPower<UnwaveringWinterPowerFactory.Configuration, ?>> getUnwaveringWinterPower(Player player) {
        try {
            // Power Containerを取得（正しいAPI使用）
            IPowerContainer powerContainer = ApoliCommon.getPowerContainer(player);
            if (powerContainer == null) {
                return java.util.Optional.empty();
            }
            
            // UnwaveringWinterパワーを検索
            var powers = powerContainer.getPowers(ModPowerTypes.UNWAVERING_WINTER.get());
            
            if (!powers.isEmpty()) {
                @SuppressWarnings("unchecked")
                ConfiguredPower<UnwaveringWinterPowerFactory.Configuration, ?> unwaveringPower = 
                    (ConfiguredPower<UnwaveringWinterPowerFactory.Configuration, ?>) powers.get(0);
                return java.util.Optional.of(unwaveringPower);
            }
            
            return java.util.Optional.empty();
            
        } catch (Exception e) {
            AnotherWorldsOrigin.LOGGER.error("Error getting UnwaveringWinter power for player {}: {}", 
                player.getDisplayName().getString(), e.getMessage());
            return java.util.Optional.empty();
        }
    }
}