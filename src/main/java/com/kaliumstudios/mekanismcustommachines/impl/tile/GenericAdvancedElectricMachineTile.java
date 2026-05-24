package com.kaliumstudios.mekanismcustommachines.impl.tile;

import org.jetbrains.annotations.NotNull;

import com.kaliumstudios.mekanismcustommachines.api.definition.ItemChemicalToItemDefinition;

import mekanism.api.IContentsListener;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generic tile entity for Item+Chemical→Item custom machines (Compressor,
 * Purifier, Injector, etc.).
 * <p>
 * Extends {@link TileEntityAdvancedElectricMachine} to inherit all standard
 * Mekanism behaviour for this shape: standard four-slot layout (item input,
 * chemical-fill, output, energy), per-tick chemical consumption, energy
 * container, upgrades, side configuration, ejector.
 * <p>
 * Because {@link TileEntityAdvancedElectricMachine} hard-codes the chemical
 * tank capacity, the per-definition capacity is passed through a thread-local
 * during construction: the factory lambda below sets {@link #CURRENT_DEFINITION}
 * immediately before invoking the constructor, the inherited super-constructor
 * calls our {@link #getInitialChemicalTanks} (which reads the thread-local),
 * and the {@code finally} block clears it. Subclass fields cannot be used
 * because they are not yet assigned when {@code getInitialChemicalTanks} runs.
 */
public class GenericAdvancedElectricMachineTile extends TileEntityAdvancedElectricMachine {

    private static final ThreadLocal<ItemChemicalToItemDefinition> CURRENT_DEFINITION = new ThreadLocal<>();

    private final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> recipeType;
    private final Holder<Block> machine;
    private final ItemChemicalToItemDefinition definition;

    // Lazily initialised to avoid allocating before the recipe type is resolved.
    private RVRecipeTypeWrapper<?, ItemStackChemicalToItemStackRecipe, ?> recipeViewerType;

    public GenericAdvancedElectricMachineTile(
            BlockPos pos,
            BlockState state,
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> recipeType,
            ItemChemicalToItemDefinition definition) {
        super(machine, pos, state, definition.baseTicksRequired());
        this.recipeType = recipeType;
        this.machine = machine;
        this.definition = definition;
    }

    /**
     * Returns a {@link BlockEntitySupplier} that closes over the given machine
     * block provider, recipe type, and definition. The supplier sets the
     * thread-local definition so that {@link #getInitialChemicalTanks} can
     * size the chemical tank correctly during the super-constructor.
     */
    public static BlockEntitySupplier<GenericAdvancedElectricMachineTile> factory(
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> recipeType,
            ItemChemicalToItemDefinition definition) {
        return (pos, state) -> {
            CURRENT_DEFINITION.set(definition);
            try {
                return new GenericAdvancedElectricMachineTile(pos, state, machine, recipeType, definition);
            } finally {
                CURRENT_DEFINITION.remove();
            }
        };
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        // CURRENT_DEFINITION is set by factory(...) before the constructor runs.
        // Subclass fields (this.definition) cannot be used here — they are not
        // assigned until after the super-constructor returns, but this method
        // is called from inside the super-constructor.
        ItemChemicalToItemDefinition def = CURRENT_DEFINITION.get();
        long capacity = def != null ? def.maxChemical() : MAX_GAS;
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        // Mirrors TileEntityAdvancedElectricMachine.getInitialChemicalTanks but with
        // configurable capacity. We can't reference the package-private `inputSlot`
        // directly, so the insert check goes through the protected `itemInputHandler`
        // (lazy lambda — itemInputHandler is populated by the parent constructor
        // after this method returns, but the lambda only runs at insert-time).
        builder.addTank(chemicalTank = BasicChemicalTank.createModern(
                capacity,
                allowExtractingChemical() ? ConstantPredicates.alwaysTrueBi() : ConstantPredicates.notExternal(),
                (chemical, automationType) -> containsRecipeBA(itemInputHandler.getInput(), chemical),
                this::containsRecipeB,
                recipeCacheListener));
        return builder.build();
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> getRecipeType() {
        return recipeType;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            var layout = definition.viewerLayout();
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    ItemStackChemicalToItemStackRecipe.class,
                    layout.xOffset(),
                    layout.yOffset(),
                    layout.width(),
                    layout.height(),
                    (ItemLike) machine);
        }
        return recipeViewerType;
    }
}
