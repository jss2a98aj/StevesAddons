package vswe.stevesfactory.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import stevesaddons.api.IHiddenInventory;
import vswe.stevesfactory.blocks.ConnectionBlockType;
import vswe.stevesfactory.blocks.TileEntityManager;

public class CraftingBufferFluidElement implements IItemBufferElement, IItemBufferSubElement
{
    private static final ItemStack DUMMY_ITEM = new ItemStack(Item.getItemById(1), 0, 0);
    private CommandExecutor executor;
    private ComponentMenuCrafting craftingMenu;
    private ComponentMenuContainerScrap scrapMenu;
    private IRecipe recipe;
    private ItemStack result;
    private boolean isCrafting;
    private boolean justRemoved;
    private int overflowBuffer;
    private List<ItemStack> containerItems;
    private static final double SPEED_MULTIPLIER = 0.05000000074505806D;
    private static final Random rand = new Random();
    private List<IInventory> inventories = new ArrayList<IInventory>();

    public CraftingBufferFluidElement(CommandExecutor executor, ComponentMenuCrafting craftingMenu, ComponentMenuContainerScrap scrapMenu)
    {
        this.executor = executor;
        this.craftingMenu = craftingMenu;
        this.scrapMenu = scrapMenu;
        this.recipe = craftingMenu.getDummy().getRecipe();
        this.result = this.recipe == null ? null : this.recipe.getCraftingResult(craftingMenu.getDummy());
        this.containerItems = new ArrayList<ItemStack>();
    }

    @Override
    public void prepareSubElements()
    {
        this.isCrafting = true;
        this.justRemoved = false;
    }

    @Override
    public IItemBufferSubElement getSubElement()
    {
        if (this.isCrafting && this.result != null)
        {
            this.isCrafting = false;
            return this;
        } else
        {
            return null;
        }
    }

    @Override
    public void removeSubElement()
    {
    }

    @Override
    public void releaseSubElements()
    {
        if (this.result != null)
        {
            if (this.overflowBuffer > 0)
            {
                ItemStack stack = this.result.copy();
                stack.stackSize = this.overflowBuffer;
                this.disposeOfExtraItem(stack);
                this.overflowBuffer = 0;
            }

            for (ItemStack containerItem : this.containerItems)
            {
                this.disposeOfExtraItem(containerItem);
            }

            this.containerItems.clear();
        }

    }

    private void disposeOfExtraItem(ItemStack itemStack)
    {
        TileEntityManager manager = this.craftingMenu.getParent().getManager();
        List<SlotInventoryHolder> inventories = CommandExecutor.getContainers(manager, this.scrapMenu, ConnectionBlockType.INVENTORY);

        for (SlotInventoryHolder inventoryHolder : inventories)
        {
            if (inventoryHolder.getTile() instanceof IHiddenInventory)
            {
                IHiddenInventory hidden = (IHiddenInventory) inventoryHolder.getTile();
                int moveCount = Math.min(hidden.getInsertable(itemStack), itemStack.stackSize);
                if (moveCount > 0)
                {
                    ItemStack toInsert = itemStack.copy();
                    toInsert.stackSize = moveCount;
                    hidden.insertItemStack(toInsert);
                    itemStack.stackSize -= moveCount;
                    if (itemStack.stackSize == 0)
                    {
                        return;
                    }
                }
            }
            else
            {
                IInventory inventory = inventoryHolder.getInventory();

                for (int i = 0; i < inventory.getSizeInventory(); ++i)
                {
                    if (inventory.isItemValidForSlot(i, itemStack))
                    {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (stack == null || stack.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemStack, stack) && itemStack.isStackable())
                        {
                            int itemCountInSlot = stack == null ? 0 : stack.stackSize;
                            int stackSize = Math.min(itemStack.stackSize, Math.min(inventory.getInventoryStackLimit(), itemStack.getMaxStackSize()) - itemCountInSlot);
                            if (stackSize > 0)
                            {
                                if (stack == null)
                                {
                                    stack = itemStack.copy();
                                    stack.stackSize = 0;
                                    inventory.setInventorySlotContents(i, stack);
                                }

                                stack.stackSize += stackSize;
                                itemStack.stackSize -= stackSize;
                                inventory.markDirty();
                                if (itemStack.stackSize == 0)
                                {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        double x = (double)manager.xCoord + rand.nextDouble() * 0.8D + 0.1D;
        double y = (double)manager.xCoord + rand.nextDouble() * 0.3D + 1.1D;
        double z = (double)manager.xCoord + rand.nextDouble() * 0.8D + 0.1D;
        EntityItem item = new EntityItem(manager.getWorldObj(), x, y, z, itemStack);
        item.motionX = rand.nextGaussian() * SPEED_MULTIPLIER;
        item.motionY = (rand.nextGaussian() + 4) * SPEED_MULTIPLIER;
        item.motionZ = rand.nextGaussian() * SPEED_MULTIPLIER;
        manager.getWorldObj().spawnEntityInWorld(item);
    }

    @Override
    public int retrieveItemCount(int moveCount)
    {
        return moveCount;
    }

    @Override
    public void decreaseStackSize(int moveCount)
    {
    }

    @Override
    public void remove()
    {
    }

    @Override
    public void onUpdate()
    {
        for (IInventory inventory : this.inventories)
        {
            if (inventory != null) inventory.markDirty();
        }
        this.inventories.clear();
    }

    @Override
    public int getSizeLeft()
    {
        if (!this.justRemoved)
        {
            return this.overflowBuffer > 0 ? this.overflowBuffer : (this.findItems(false) ? this.result.stackSize : 0);
        } else
        {
            this.justRemoved = false;
            return 0;
        }
    }

    @Override
    public void reduceAmount(int amount)
    {
        this.justRemoved = true;
        if (this.overflowBuffer > 0)
        {
            this.overflowBuffer -= amount;
        } else
        {
            this.findItems(true);
            this.overflowBuffer = this.result.stackSize - amount;
        }

        this.isCrafting = true;
    }

    @Override
    public ItemStack getItemStack()
    {
        if (this.useAdvancedDetection())
        {
            this.findItems(false);
        }

        return this.result;
    }

    private boolean useAdvancedDetection()
    {
        return this.craftingMenu.getResultItem().getFuzzyMode() != FuzzyMode.PRECISE;
    }

    private boolean findItems(boolean remove)
    {
        List<CraftingSetting> settings = new ArrayList<CraftingSetting>();
        for (Setting setting : this.craftingMenu.getSettings()) settings.add((CraftingSetting)setting);

        HashMap<Integer, ItemStack> foundItems = new HashMap<Integer, ItemStack>();
        Set<ItemStack> usedStacks = new HashSet<ItemStack>();

        for (ItemBufferElement itemBufferElement : this.executor.itemBuffer)
        {
            int count = itemBufferElement.retrieveItemCount(9);
            Iterator iterator = itemBufferElement.getSubElements().iterator();

            while (iterator.hasNext())
            {
                IItemBufferSubElement itemBufferSubElement = (IItemBufferSubElement)iterator.next();
                ItemStack itemstack = itemBufferSubElement.getItemStack();
                if (usedStacks.contains(itemstack)) continue;
                int subCount = Math.min(count, itemBufferSubElement.getSizeLeft());

                for (int i = 0; i < 9; ++i)
                {
                    CraftingSetting setting = settings.get(i);
                    if (foundItems.get(i) == null)
                    {
                        if (!setting.isValid())
                        {
                            foundItems.put(i, DUMMY_ITEM);
                        } else if (subCount > 0 && setting.isEqualForCommandExecutor(itemstack))
                        {
                            foundItems.put(i, itemstack.copy());
                            if (this.craftingMenu.getDummy().isItemValidForRecipe(this.recipe, this.craftingMenu.getResultItem(), foundItems, this.useAdvancedDetection()))
                            {
                                usedStacks.add(itemstack);
                                --subCount;
                                --count;
                                if (remove)
                                {
                                    if (itemstack.getItem().hasContainerItem(itemstack))
                                    {
                                        this.containerItems.add(itemstack.getItem().getContainerItem(itemstack));
                                    }

                                    itemBufferElement.decreaseStackSize(1);
                                    itemBufferSubElement.reduceAmount(1);
                                    if (itemBufferSubElement.getSizeLeft() == 0)
                                    {
                                        itemBufferSubElement.remove();
                                        iterator.remove();
                                    }

                                    this.inventories.add(((SlotStackInventoryHolder)itemBufferSubElement).getInventory());
                                }
                            } else
                            {
                                foundItems.remove(Integer.valueOf(i));
                            }
                        }
                    }
                }
            }
        }

        if (foundItems.size() < 9)
        {
            List<FluidElement> fluids = new ArrayList<FluidElement>();
            for (int i = 0; i < 9; i++)
            {
                CraftingSetting setting = settings.get(i);
                if (foundItems.get(i) == null && isBucket(setting))
                {
                    boolean newFluid = true;
                    for (FluidElement fluidElement : fluids)
                    {
                        if (fluidElement.bucket.isItemEqual(setting.getItem()))
                        {
                            fluidElement.amountToFind += fluidElement.fluid.amount;
                            fluidElement.slots.add(i);
                            newFluid = false;
                        }
                    }
                    if (newFluid) fluids.add(new FluidElement(setting.getItem().copy(), i));
                }
            }

            if (fluids.size() > 0)
            {
                for (LiquidBufferElement liquidBufferElement : this.executor.liquidBuffer)
                {
                    if (fluids.isEmpty()) break;
                    Iterator<StackTankHolder> itr = liquidBufferElement.getHolders().iterator();
                    while (itr.hasNext())
                    {
                        StackTankHolder tank = itr.next();
                        for (Iterator<FluidElement> fluidItr = fluids.iterator(); fluidItr.hasNext(); )
                        {
                            FluidElement fluidElement = fluidItr.next();
                            int maxAmount = liquidBufferElement.retrieveItemCount(fluidElement.amountToFind);
                            if (tank.getFluidStack().isFluidEqual(fluidElement.fluid))
                            {
                                maxAmount = Math.min(maxAmount, tank.getSizeLeft());
                                fluidElement.amountToFind -= maxAmount;
                                if (remove)
                                {
                                    tank.reduceAmount(maxAmount);
                                    FluidStack toRemove = fluidElement.fluid;
                                    toRemove.amount = maxAmount;
                                    tank.getTank().drain(tank.getSide(), maxAmount, true);
                                }
                                if (fluidElement.amountToFind == 0)
                                {
                                    fluidItr.remove();
                                    for (int i : fluidElement.slots)
                                        foundItems.put(i, fluidElement.bucket);
                                    break;
                                }
                            }
                        }
                        if (tank.getSizeLeft() == 0) itr.remove();
                    }
                }
            }
        }

        if (foundItems.size() == 9)
        {
            this.result = this.craftingMenu.getDummy().getResult(foundItems);
            this.result = this.result != null ? this.result.copy() : null;
            return true;
        } else
        {
            return false;
        }
    }

    private static boolean isBucket(CraftingSetting setting)
    {
        return FluidContainerRegistry.isBucket(setting.getItem());
    }

    private static class FluidElement
    {
        public FluidStack fluid;
        public ItemStack bucket;
        public int amountToFind;
        List<Integer> slots = new ArrayList<Integer>();

        private FluidElement(ItemStack bucket, int i)
        {
            this.bucket = bucket;
            this.fluid = FluidContainerRegistry.getFluidForFilledItem(bucket);
            amountToFind = this.fluid.amount;
            slots.add(i);
        }
    }
}
