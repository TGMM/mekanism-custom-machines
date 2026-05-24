package com.kaliumstudios.mekanismcustommachines.impl.tile;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kaliumstudios.mekanismcustommachines.api.definition.ChemicalToChemicalDefinition;

import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generic tile entity for Chemical→Chemical custom machines (centrifuge /
 * activator analogs).
 * <p>
 * Unlike Mekanism's Isotopic Centrifuge / Solar Neutron Activator (which are
 * 2-tall multiblocks via {@code IBoundingBlock}), this generic tile is a
 * single block in world. The plan documents this as an intentional v1
 * simplification.
 */
public class GenericChemicalToChemicalTile extends TileEntityRecipeMachine<ChemicalToChemicalRecipe>
        implements ChemicalRecipeLookupHandler<ChemicalToChemicalRecipe> {

    private static final ThreadLocal<ChemicalToChemicalDefinition> CURRENT_DEFINITION = new ThreadLocal<>();

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    private final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> recipeType;
    private final Holder<Block> machine;
    private final ChemicalToChemicalDefinition definition;

    public IChemicalTank inputTank;
    public IChemicalTank outputTank;

    private final IOutputHandler<@NotNull ChemicalStack> outputHandler;
    private final IInputHandler<@NotNull ChemicalStack> inputHandler;

    private MachineEnergyContainer<GenericChemicalToChemicalTile> energyContainer;
    private ChemicalInventorySlot inputSlot;
    private ChemicalInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    private int baselineMaxOperations = 1;
    private RVRecipeTypeWrapper<?, ChemicalToChemicalRecipe, ?> recipeViewerType;

    public GenericChemicalToChemicalTile(
            BlockPos pos,
            BlockState state,
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> recipeType,
            ChemicalToChemicalDefinition definition) {
        super(machine, pos, state, TRACKED_ERROR_TYPES);
        this.recipeType = recipeType;
        this.machine = machine;
        this.definition = definition;

        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, inputTank, outputTank, RelativeSide.FRONT, false, true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL)
                .setCanTankEject(tank -> tank != inputTank);

        inputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    public static BlockEntitySupplier<GenericChemicalToChemicalTile> factory(
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> recipeType,
            ChemicalToChemicalDefinition definition) {
        return (pos, state) -> {
            CURRENT_DEFINITION.set(definition);
            try {
                return new GenericChemicalToChemicalTile(pos, state, machine, recipeType, definition);
            } finally {
                CURRENT_DEFINITION.remove();
            }
        };
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalToChemicalDefinition def = CURRENT_DEFINITION.get();
        long capacity = def != null ? def.maxChemical() : 10_000L;
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        // Allow extracting from the input tank only if it's not external,
        // mirroring Mekanism's isotopic centrifuge pattern (radioactive input safety).
        builder.addTank(inputTank = BasicChemicalTank.createModern(capacity,
                ChemicalTankHelper.radioactiveInputTankPredicate(() -> outputTank),
                ConstantPredicates.alwaysTrueBi(),
                this::containsRecipe,
                ChemicalAttributeValidator.ALWAYS_ALLOW,
                recipeCacheListener));
        builder.addTank(outputTank = BasicChemicalTank.output(capacity, recipeCacheUnpauseListener));
        return builder.build();
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
        builder.addSlot(inputSlot = ChemicalInventorySlot.fill(inputTank, listener, 5, 56));
        builder.addSlot(outputSlot = ChemicalInventorySlot.drain(outputTank, listener, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 155, 14));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        inputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank();
        outputSlot.drainTank();
        recipeCacheLookupMonitor.updateAndProcess(energyContainer);
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> getRecipeType() {
        return recipeType;
    }

    @Nullable
    @Override
    public ChemicalToChemicalRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<ChemicalToChemicalRecipe> createNewCachedRecipe(@NotNull ChemicalToChemicalRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.chemicalToChemical(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setOnFinish(this::markForSave)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setBaselineMaxOperations(() -> baselineMaxOperations);
    }

    @Override
    public IRecipeViewerRecipeType<ChemicalToChemicalRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            var layout = definition.viewerLayout();
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    ChemicalToChemicalRecipe.class,
                    layout.xOffset(),
                    layout.yOffset(),
                    layout.width(),
                    layout.height(),
                    (ItemLike) machine);
        }
        return recipeViewerType;
    }

    public MachineEnergyContainer<GenericChemicalToChemicalTile> getEnergyContainer() {
        return energyContainer;
    }
}
