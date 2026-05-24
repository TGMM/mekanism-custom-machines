package com.kaliumstudios.mekanismcustommachines.impl.tile;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kaliumstudios.mekanismcustommachines.api.definition.SawmillDefinition;

import mekanism.api.IContentsListener;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generic tile entity for sawmill-style (Item → Item + chance secondary)
 * custom machines.
 * <p>
 * Mirrors {@link mekanism.common.tile.machine.TileEntityPrecisionSawmill} but
 * accepts an arbitrary block holder and recipe type. We can't extend the
 * Mekanism class directly because its constructor hard-codes the sawmill block.
 */
public class GenericSawmillTile extends TileEntityProgressMachine<SawmillRecipe>
        implements ItemRecipeLookupHandler<SawmillRecipe> {

    public static final RecipeError NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR = RecipeError.create();
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    private final RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> recipeType;
    private final Holder<Block> machine;
    private final SawmillDefinition definition;

    private final IOutputHandler<@NotNull ChanceOutput> outputHandler;
    private final IInputHandler<@NotNull ItemStack> inputHandler;

    private MachineEnergyContainer<GenericSawmillTile> energyContainer;
    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private OutputInventorySlot secondaryOutputSlot;
    private EnergyInventorySlot energySlot;

    private RVRecipeTypeWrapper<?, SawmillRecipe, ?> recipeViewerType;

    public GenericSawmillTile(
            BlockPos pos,
            BlockState state,
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> recipeType,
            SawmillDefinition definition) {
        super(machine, pos, state, TRACKED_ERROR_TYPES, definition.baseTicksRequired());
        this.recipeType = recipeType;
        this.machine = machine;
        this.definition = definition;

        configComponent.setupItemIOConfig(Collections.singletonList(inputSlot), List.of(outputSlot, secondaryOutputSlot), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        inputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE, secondaryOutputSlot, NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR);
    }

    public static BlockEntitySupplier<GenericSawmillTile> factory(
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> recipeType,
            SawmillDefinition definition) {
        return (pos, state) -> new GenericSawmillTile(pos, state, machine, recipeType, definition);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(inputSlot = InputInventorySlot.at(this::containsRecipe, recipeCacheListener, 56, 17))
                .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        builder.addSlot(outputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 116, 35));
        builder.addSlot(secondaryOutputSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 132, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 56, 53));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> getRecipeType() {
        return recipeType;
    }

    @Nullable
    @Override
    public SawmillRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@NotNull SawmillRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.sawing(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks)
                .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @Override
    public IRecipeViewerRecipeType<SawmillRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            var layout = definition.viewerLayout();
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    SawmillRecipe.class,
                    layout.xOffset(),
                    layout.yOffset(),
                    layout.width(),
                    layout.height(),
                    (ItemLike) machine);
        }
        return recipeViewerType;
    }

    public MachineEnergyContainer<GenericSawmillTile> getEnergyContainer() {
        return energyContainer;
    }
}
