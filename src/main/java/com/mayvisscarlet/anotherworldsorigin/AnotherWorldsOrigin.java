package com.mayvisscarlet.anotherworldsorigin;

import com.mayvisscarlet.anotherworldsorigin.capability.AffinityCapability;
import com.mayvisscarlet.anotherworldsorigin.commands.AffinityCommand;
import com.mayvisscarlet.anotherworldsorigin.commands.TestCommand;
import com.mayvisscarlet.anotherworldsorigin.events.AffinityEventHandler;
import com.mayvisscarlet.anotherworldsorigin.test.DependencyTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        
        // ForgeイベントバスにMODクラス自体を登録（コマンド登録用）
        MinecraftForge.EVENT_BUS.register(this);
        
        // Affinity関連のイベントハンドラーを登録
        MinecraftForge.EVENT_BUS.register(AffinityEventHandler.class);
        
        LOGGER.info("Another Worlds Origin - Loading...");
        
        // 依存関係チェックを実行
        DependencyTest.performBasicDependencyCheck();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Another Worlds Origin - Common Setup Complete");
        LOGGER.info("Patricia origin ready to be selected!");
        LOGGER.info("Affinity system initialized!");
        
        // Origins連携は実行時に自動で行われる（JSONファイルによる）
        // 将来的にここでカスタム能力の登録などを行う予定
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