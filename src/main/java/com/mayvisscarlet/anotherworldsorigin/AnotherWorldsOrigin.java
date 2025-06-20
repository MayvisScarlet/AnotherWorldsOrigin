package com.mayvisscarlet.anotherworldsorigin;

import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.commands.AffinityCommand;
import com.mayvisscarlet.anotherworldsorigin.commands.TestCommand;
import com.mayvisscarlet.anotherworldsorigin.config.ConfigInitializer;
import com.mayvisscarlet.anotherworldsorigin.events.IntegratedEventHandler;
import com.mayvisscarlet.anotherworldsorigin.events.SkillExecutionEventHandler;
import com.mayvisscarlet.anotherworldsorigin.network.ModNetworking;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.HeatVulnerabilityPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.registry.ModPowerTypes;
import com.mayvisscarlet.anotherworldsorigin.test.DependencyTest;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AnotherWorldsOrigin.MODID)
public class AnotherWorldsOrigin {
    public static final String MODID = "anotherworldsorigin";
    public static final Logger LOGGER = LogManager.getLogger();

    public AnotherWorldsOrigin() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // MODイベントバスにリスナーを登録
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        
        // 独自PowerFactoryを登録
        ModPowerTypes.register(modEventBus);
        
        // ForgeイベントバスにMODクラス自体を登録（コマンド登録用&イベント処理用）
        MinecraftForge.EVENT_BUS.register(this);
        
        // クリーンに分離されたイベントハンドラーを登録
        MinecraftForge.EVENT_BUS.register(IntegratedEventHandler.class);
        MinecraftForge.EVENT_BUS.register(UnwaveringWinterPowerFactory.EventHandler.class);
        MinecraftForge.EVENT_BUS.register(HeatVulnerabilityPowerFactory.EventHandler.class);
        MinecraftForge.EVENT_BUS.register(SkillExecutionEventHandler.class);

        // ネットワークパケット登録
        ModNetworking.registerPackets();
        
        LOGGER.info("Another Worlds Origin - Loading...");
        LOGGER.info("=== CLEAN ARCHITECTURE - Powers Properly Separated ===");
        
        // 依存関係チェックを実行
        DependencyTest.performBasicDependencyCheck();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Another Worlds Origin - Common Setup Starting...");
        
        // 設定システムを初期化
        ConfigInitializer.initialize(event);
        
        LOGGER.info("Another Worlds Origin - Common Setup Complete");
        LOGGER.info("Patricia origin ready to be selected!");
        LOGGER.info("Affinity system initialized!");
        LOGGER.info("JSON config system initialized!");
        LOGGER.info("Custom PowerFactories registered!");
        LOGGER.info("=== UNWAVERING WINTER & HEAT VULNERABILITY SYSTEMS READY ===");
    }
    
    /**
     * Capabilityを登録
     */
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(AffinityCapability.IAffinityData.class);
        LOGGER.info("Affinity capability registered!");
    }
    
    /**
     * コマンドを登録（Forgeイベントバス用）
     */
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        LOGGER.info("==== Registering Another Worlds Origin Commands ====");
        
        try {
            // 既存のコマンドクラスを使用
            TestCommand.register(event.getDispatcher());
            LOGGER.info("Test command registered successfully!");
            
            AffinityCommand.register(event.getDispatcher());
            LOGGER.info("Affinity command registered successfully!");
            
        } catch (Exception e) {
            LOGGER.error("Failed to register commands: ", e);
        }
        
        LOGGER.info("==== Command registration complete ====");
    }
}