package by.jackraidenph.dragonsurvival.network.emotes;

import by.jackraidenph.dragonsurvival.handlers.ServerSide.NetworkHandler;
import by.jackraidenph.dragonsurvival.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

import java.util.function.Supplier;

public class SyncEmoteServer implements IMessage<SyncEmoteServer>
{

    private String emote;
    private int emoteTick;
    
    public SyncEmoteServer(String emote, int emoteTick)
    {
        this.emote = emote;
        this.emoteTick = emoteTick;
    }
    
    public SyncEmoteServer() {
    }
    
    @Override
    public void encode(SyncEmoteServer message, PacketBuffer buffer) {
        buffer.writeUtf(message.emote);
        buffer.writeInt(message.emoteTick);
    }

    @Override
    public SyncEmoteServer decode(PacketBuffer buffer) {
        String emote = buffer.readUtf();
        int emoteTick = buffer.readInt();
        return new SyncEmoteServer(emote, emoteTick);
    }

    @Override
    public void handle(SyncEmoteServer message, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayerEntity playerEntity = supplier.get().getSender();

        if(playerEntity == null)
            return;
        
        TargetPoint point = new TargetPoint(playerEntity, playerEntity.position().x, playerEntity.position().y, playerEntity.position().z, 64, playerEntity.level.dimension());
        NetworkHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> point), new SyncEmote(playerEntity.getId(), message.emote, message.emoteTick));
    }
}