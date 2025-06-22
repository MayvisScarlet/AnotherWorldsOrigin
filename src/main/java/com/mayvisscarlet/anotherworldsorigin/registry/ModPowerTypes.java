package com.mayvisscarlet.anotherworldsorigin.registry;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.UnwaveringWinterPowerFactory;
import com.mayvisscarlet.anotherworldsorigin.origins.patricia.powers.HeatVulnerabilityPowerFactory;
import io.github.edwinmindcraft.apoli.api.power.factory.PowerFactory;
import io.github.edwinmindcraft.apoli.api.registry.ApoliRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Another Worlds Origin の独自PowerFactoryを登録するクラス
 */
public class ModPowerTypes {
    
    public static final DeferredRegister<PowerFactory<?>> POWER_FACTORIES = 
        DeferredRegister.create(ApoliRegistries.POWER_FACTORY_KEY, AnotherWorldsOrigin.MODID);
    
    /**
     * パトリシアの「揺らぐ事なき冬」PowerFactory
     */
    public static final RegistryObject<PowerFactory<UnwaveringWinterPowerFactory.Configuration>> UNWAVERING_WINTER = 
        POWER_FACTORIES.register("unwavering_winter", UnwaveringWinterPowerFactory::new);
    
    /**
     * パトリシアの「溶けた氷が固まるまで」PowerFactory
     */
    public static final RegistryObject<PowerFactory<HeatVulnerabilityPowerFactory.Configuration>> HEAT_VULNERABILITY = 
        POWER_FACTORIES.register("heat_vulnerability", HeatVulnerabilityPowerFactory::new);
    
    /**
     * PowerFactory登録の初期化
     */
    public static void register(IEventBus modEventBus) {
        POWER_FACTORIES.register(modEventBus);
        AnotherWorldsOrigin.LOGGER.info("Registering Another Worlds Origin PowerFactories...");
    }
    
    /**
     * 登録済みPowerFactoryのResourceLocationを取得
     */
    public static ResourceLocation getUnwaveringWinterId() {
        return ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "unwavering_winter");
    }
    
    public static ResourceLocation getHeatVulnerabilityId() {
        return ResourceLocation.fromNamespaceAndPath(AnotherWorldsOrigin.MODID, "heat_vulnerability");
    }
}