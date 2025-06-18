package com.mayvisscarlet.anotherworldsorigin.capability;

import com.mayvisscarlet.anotherworldsorigin.AnotherWorldsOrigin;
import com.mayvisscarlet.anotherworldsorigin.growth.AffinityData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * プレイヤーに親和度データを添付するCapability（安全版）
 */
@Mod.EventBusSubscriber(modid = AnotherWorldsOrigin.MODID)
public class AffinityCapability {
    
    public static final Capability<IAffinityData> AFFINITY_DATA = 
        CapabilityManager.get(new CapabilityToken<>() {});
    
    public static final ResourceLocation AFFINITY_DATA_CAPABILITY = 
        new ResourceLocation(AnotherWorldsOrigin.MODID, "affinity_data");
    
    /**
     * プレイヤーの親和度データを取得
     */
    public static LazyOptional<IAffinityData> getAffinityData(Player player) {
        return player.getCapability(AFFINITY_DATA);
    }
    
    /**
     * プレイヤーに親和度データを添付（安全なログ処理）
     */
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(AFFINITY_DATA).isPresent()) {
                event.addCapability(AFFINITY_DATA_CAPABILITY, new AffinityDataProvider());
                // 安全なログ処理（GameProfileがnullの場合を考慮）
                try {
                    String playerName = player.getDisplayName() != null ? 
                        player.getDisplayName().getString() : "Unknown";
                    AnotherWorldsOrigin.LOGGER.debug("Affinity capability attached to player: {}", playerName);
                } catch (Exception e) {
                    AnotherWorldsOrigin.LOGGER.debug("Affinity capability attached to player (name unavailable)");
                }
            }
        }
    }
    
    /**
     * プレイヤー複製時のデータコピー（死亡復活時のみ）
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(AFFINITY_DATA).ifPresent(oldData -> {
                event.getEntity().getCapability(AFFINITY_DATA).ifPresent(newData -> {
                    CompoundTag nbt = oldData.serializeNBT();
                    newData.deserializeNBT(nbt);
                    AnotherWorldsOrigin.LOGGER.debug("Copied affinity data on death. Level: {}", 
                        newData.getAffinityData().getAffinityLevel());
                });
            });
        }
    }
    
    /**
     * 親和度データのインターフェース
     */
    public interface IAffinityData extends INBTSerializable<CompoundTag> {
        AffinityData getAffinityData();
        void setAffinityData(AffinityData data);
    }
    
    /**
     * 親和度データの実装
     */
    public static class AffinityDataImpl implements IAffinityData {
        private AffinityData affinityData = new AffinityData();
        
        @Override
        public AffinityData getAffinityData() {
            return affinityData;
        }
        
        @Override
        public void setAffinityData(AffinityData data) {
            this.affinityData = data != null ? data : new AffinityData();
        }
        
        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = affinityData.saveToNBT();
            // デバッグログ（簡略化）
            AnotherWorldsOrigin.LOGGER.debug("Serializing affinity data: Level={}, Total={}", 
                affinityData.getAffinityLevel(), affinityData.getTotalAffinityPoints());
            return nbt;
        }
        
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt != null && !nbt.isEmpty()) {
                affinityData.loadFromNBT(nbt);
                // デバッグログ（簡略化）
                AnotherWorldsOrigin.LOGGER.debug("Deserialized affinity data: Level={}, Total={}", 
                    affinityData.getAffinityLevel(), affinityData.getTotalAffinityPoints());
            }
        }
    }
    
    /**
     * Capabilityプロバイダー（INBTSerializable対応）
     */
    public static class AffinityDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final IAffinityData affinityData = new AffinityDataImpl();
        private final LazyOptional<IAffinityData> optional = LazyOptional.of(() -> affinityData);
        
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == AFFINITY_DATA ? optional.cast() : LazyOptional.empty();
        }
        
        @Override
        public CompoundTag serializeNBT() {
            return affinityData.serializeNBT();
        }
        
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            affinityData.deserializeNBT(nbt);
        }
    }
}