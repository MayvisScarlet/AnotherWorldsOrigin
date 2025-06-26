package com.mayvisscarlet.ifoe_bravers;

import com.mayvisscarlet.ifoe_bravers.capability.AffinityCapability;
import com.mayvisscarlet.ifoe_bravers.commands.AffinityCommand;
import com.mayvisscarlet.ifoe_bravers.commands.TestCommand;
import com.mayvisscarlet.ifoe_bravers.config.ConfigInitializer;
import com.mayvisscarlet.ifoe_bravers.events.IntegratedEventHandler;
import com.mayvisscarlet.ifoe_bravers.events.SkillExecutionEventHandler;
import com.mayvisscarlet.ifoe_bravers.network.ModNetworking;
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

@Mod(ifoe_bravers.MODID)
public class ifoe_bravers {
    public static final String MODID = "ifoe_bravers";
    public static final Logger LOGGER = LogManager.getLogger();

    public ifoe_bravers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // MODイベントバスにリスナーを登録
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        
        // ForgeイベントバスにMODクラス自体を登録（コマンド登録用&イベント処理用）
        MinecraftForge.EVENT_BUS.register(this);
        
        // クリーンに分離されたイベントハンドラーを登録
        MinecraftForge.EVENT_BUS.register(IntegratedEventHandler.class);
        MinecraftForge.EVENT_BUS.register(SkillExecutionEventHandler.class);

        // ネットワークパケット登録
        ModNetworking.registerPackets();
        
        LOGGER.info("IfOE_Bravers (Inherit from Outer Epic_Bravers) - Loading...");
        LOGGER.info("=== CLEAN ARCHITECTURE - Powers Properly Separated ===");
        
        // Origins依存関係除去完了
        LOGGER.info("独立MODとして動作します（Origins依存なし）");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("IfOE_Bravers - Common Setup Starting...");
        
        // 設定システムを初期化
        ConfigInitializer.initialize(event);
        
        LOGGER.info("IfOE_Bravers - Common Setup Complete");
        LOGGER.info("独自種族システム初期化準備完了！");
        LOGGER.info("親和度システム初期化完了!");
        LOGGER.info("JSON設定システム初期化完了!");
        LOGGER.info("Origins依存関係除去完了!");
        LOGGER.info("=== 独立MODとして動作開始 ===");
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
        LOGGER.info("==== Registering IfOE_Bravers Commands ====");
        
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