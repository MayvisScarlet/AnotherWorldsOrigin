
// === 5. ネットワーク登録クラス ===
package com.mayvisscarlet.anotherworldsorigin.network;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    
    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AnotherWorldsOrigin.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void registerPackets() {
        INSTANCE.messageBuilder(SkillActivationPacket.class, packetId++)
            .encoder(SkillActivationPacket::encode)
            .decoder(SkillActivationPacket::new)
            .consumerMainThread(SkillActivationPacket::handle)
            .add();
        
        AnotherWorldsOrigin.LOGGER.info("Registered network packets");
    }
    
    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}