package stevesaddons.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vswe.stevesfactory.components.ScrollController;

public class SearchItems implements Runnable
{
    public static List<SearchEntry> searchEntries = new ArrayList<SearchEntry>();

    private String search;
    private ScrollController controller;
    private boolean showAll;

    public SearchItems(String search, ScrollController controller, boolean showAll)
    {
        this.search = search;
        this.controller = controller;
        this.showAll = showAll;
    }

    @Override
    public void run()
    {
        List<ItemStack> itemStacks = new ArrayList<ItemStack>();
        if (search.equals(".inv"))
        {
            InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;
            for (int itemStack = 0; itemStack < inventory.getSizeInventory(); ++itemStack)
            {
                ItemStack stack = inventory.getStackInSlot(itemStack);
                if (stack != null)
                {
                    stack = stack.copy();
                    stack.stackSize = 1;
                    itemStacks.add(stack);
                }
            }
        } else
        {
            if (!showAll)
            {
                Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
                boolean advanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
                for (SearchEntry entry : searchEntries) entry.search(pattern, itemStacks, advanced);
            } else
            {
                for (SearchEntry entry : searchEntries) itemStacks.add(entry.getStack());
            }
        }
        setResult(this.controller, itemStacks);
    }

    public static void setResult(ScrollController controller, List<ItemStack> stackList)
    {
        ThreadSafeHandler.handle.put(controller, stackList);
    }

    public static void setItems()
    {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (Object anItemRegistry : Item.itemRegistry)
        {
            try
            {
                Item item = (Item)anItemRegistry;
                item.getSubItems(item, item.getCreativeTab(), stacks);
            } catch (Exception ignore)
            {
            }
        }
        for (ItemStack stack : stacks)
        {
            try
            {
                List tooltipList = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
                List advTooltipList = stack.getTooltip(Minecraft.getMinecraft().thePlayer, true);
                String searchString = "";
                for (Object string : tooltipList)
                {
                    if (string != null)
                        searchString += string + "\n";
                }
                String advSearchString = "";
                for (Object string : advTooltipList)
                {
                    if (string != null)
                        advSearchString += string + "\n";
                }
                searchEntries.add(new SearchEntry(searchString, advSearchString, stack));
            } catch (Throwable ignore)
            {
            }
        }
    }

    public static class SearchEntry
    {
        private String toolTip;
        private String advToolTip;
        private ItemStack stack;

        public SearchEntry(String searchString, String advSearchString, ItemStack stack)
        {
            this.toolTip = searchString;
            this.advToolTip = advSearchString;
            this.stack = stack;
        }

        public void search(Pattern pattern, List<ItemStack> stacks, boolean advanced)
        {
            if (pattern.matcher(advanced ? advToolTip : toolTip).find()) stacks.add(stack);
        }

        public ItemStack getStack()
        {
            return stack;
        }
    }
}
