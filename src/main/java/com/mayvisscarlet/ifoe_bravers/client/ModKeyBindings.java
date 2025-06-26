// === 1. キーバインド登録クラス ===
package com.mayvisscarlet.ifoe_bravers.client;

import com.mayvisscarlet.ifoe_bravers.ifoe_bravers;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ifoe_bravers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {
    
    public static final String KEY_CATEGORY = "key.categories." + ifoe_bravers.MODID;
    
    public static final KeyMapping SKILL_KEY = new KeyMapping(
        "key." + ifoe_bravers.MODID + ".skill", // 翻訳キー
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R, // デフォルトRキー
        KEY_CATEGORY
    );
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SKILL_KEY);
        ifoe_bravers.LOGGER.info("Registered skill key binding");
    }
}
