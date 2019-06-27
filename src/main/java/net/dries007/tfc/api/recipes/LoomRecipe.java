/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.api.recipes;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.objects.inventory.ingredient.IIngredient;

public class LoomRecipe extends IForgeRegistryEntry.Impl<LoomRecipe>
{
    @Nullable
    public static LoomRecipe get(ItemStack item)
    {
        return TFCRegistries.LOOM.getValuesCollection().stream().filter(x -> x.isValidInput(item)).findFirst().orElse(null);
    }

    private IIngredient<ItemStack> inputItem;
    private int inputAmount;
    private ItemStack outputItem;
    private IIngredient<ItemStack> outputTestItem;
    private int stepCount;
    private ResourceLocation inProgressTexture;

    public LoomRecipe(ResourceLocation name, IIngredient<ItemStack> input, int inputAmount, ItemStack output, int stepsRequired, ResourceLocation inProgressTexture)
    {
        inputItem = input;
        this.inputAmount = inputAmount;
        outputItem = output;
        outputTestItem = IIngredient.of(outputItem);
        stepCount = stepsRequired;

        this.inProgressTexture = inProgressTexture;

        if (inputItem == null || inputAmount == 0 || outputItem == null || stepsRequired == 0)
            throw new IllegalArgumentException("Input and output are not allowed to be empty");
        setRegistryName(name);
    }

    public boolean isValidInput(ItemStack inputItem)
    {
        return this.inputItem.testIgnoreCount(inputItem);
    }

    public boolean isValidOutput(ItemStack outputItem)
    {
        return this.outputTestItem.testIgnoreCount(outputItem);
    }

    public int getInputCount()
    {
        return inputAmount;
    }

    public int getStepCount()
    {
        return stepCount;
    }

    public ItemStack getOutputItem()
    {
        return outputItem;
    }

    public ResourceLocation getInProgressTexture()
    {
        return inProgressTexture;
    }

}
