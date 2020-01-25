package stevesaddons.registry;

import java.util.ArrayList;
import java.util.Arrays;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import stevesaddons.items.ItemLabeler;
import stevesaddons.items.ItemSFMDrive;
import stevesaddons.reference.Names;
import vswe.stevesfactory.blocks.ModBlocks;

public class ItemRegistry
{
    public static Item duplicator;
    public static Item labeler;
    public static ItemStack defaultLabeler;

    public static void registerItems()
    {
        GameRegistry.registerItem(duplicator = new ItemSFMDrive(), Names.DRIVE);
        GameRegistry.registerItem(labeler = new ItemLabeler(), Names.LABELER);
    }

    public static void registerRecipes()
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(duplicator), " x ", "xyx", " x ", 'x', "ingotIron", 'y', new ItemStack(ModBlocks.blockManager)));
        defaultLabeler = new ItemStack(labeler);
        ItemLabeler.saveStrings(defaultLabeler, new ArrayList<String>(Arrays.asList("Energy Receiver", "Energy Provider", "Input Inventory", "Input Tank", "Output Inventory", "Output Tank")));
        GameRegistry.addRecipe(new ShapedOreRecipe(defaultLabeler, "ppp", " i ", "rxr", 'p', new ItemStack(Items.paper), 'i', "dyeBlack", 'r', "dustRedstone", 'x', new ItemStack(Blocks.piston)));
    }
}
