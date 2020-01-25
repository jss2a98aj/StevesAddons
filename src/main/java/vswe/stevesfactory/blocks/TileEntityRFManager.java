package vswe.stevesfactory.blocks;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityRFManager extends TileEntityManager
{
    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        tagCompound.setString("id", "TileEntityMachineManagerName");
    }
}
