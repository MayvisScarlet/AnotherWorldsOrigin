
// === 4. ネットワークパケット ===
package com.mayvisscarlet.anotherworldsorigin.network;

import com.mayvisscarlet.anotherworldsorigin.skills.TestFireSkill;
import com.mayvisscarlet.anotherworldsorigin.util.OriginHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SkillActivationPacket {
    private final String skillName;
    
    public SkillActivationPacket(String skillName) {
        this.skillName = skillName;
    }
    
    public SkillActivationPacket(FriendlyByteBuf buf) {
        this.skillName = buf.readUtf();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.skillName);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && OriginHelper.isAnotherWorldsOriginUser(player)) {
                
                switch (skillName) {
                    case "test_fire_skill":
                        TestFireSkill.execute(player);
                        break;
                    default:
                        // 未知のスキル
                        break;
                }
            }
        });
        context.setPacketHandled(true);
    }
}