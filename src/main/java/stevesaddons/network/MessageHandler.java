package stevesaddons.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import stevesaddons.network.message.FullDataSyncMessage;
import stevesaddons.network.message.LabelSyncMessage;
import stevesaddons.network.message.NameDataUpdateMessage;
import stevesaddons.network.message.RFNodeUpdateMessage;
import stevesaddons.network.message.SearchRegistryGenerateMessage;
import stevesaddons.network.message.WorldDataSyncMessage;
import stevesaddons.reference.Reference;

public class MessageHandler implements IMessageHandler
{
    public static SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Reference.ID);

    public static void init()
    {
        INSTANCE.registerMessage(RFNodeUpdateMessage.class, RFNodeUpdateMessage.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(NameDataUpdateMessage.class, NameDataUpdateMessage.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(NameDataUpdateMessage.class, NameDataUpdateMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(FullDataSyncMessage.class, FullDataSyncMessage.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(WorldDataSyncMessage.class, WorldDataSyncMessage.class, 4, Side.CLIENT);
        INSTANCE.registerMessage(LabelSyncMessage.class, LabelSyncMessage.class, 5, Side.SERVER);
        INSTANCE.registerMessage(SearchRegistryGenerateMessage.class, SearchRegistryGenerateMessage.class, 6, Side.CLIENT);
    }

    @Override
    public IMessage onMessage(IMessage message, MessageContext ctx)
    {
        return null;
    }
}