package stevesaddons.network.message;

import java.io.IOException;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import stevesaddons.naming.NameData;
import stevesaddons.naming.NameRegistry;

public class WorldDataSyncMessage implements IMessage, IMessageHandler<WorldDataSyncMessage, IMessage>
{
    public int dimId;
    public NBTTagCompound tagCompound = new NBTTagCompound();

    public WorldDataSyncMessage()
    {
    }

    public WorldDataSyncMessage(int dimId, NameData data)
    {
        this.dimId = dimId;
        data.writeToNBT(tagCompound);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        PacketBuffer pb = new PacketBuffer(buf);
        this.dimId = pb.readInt();
        try
        {
            tagCompound = pb.readNBTTagCompoundFromBuffer();
        } catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeInt(dimId);
        try
        {
            pb.writeNBTTagCompoundToBuffer(tagCompound);
        } catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public IMessage onMessage(WorldDataSyncMessage message, MessageContext ctx)
    {
        NameRegistry.setWorldData(message);
        return null;
    }
}
