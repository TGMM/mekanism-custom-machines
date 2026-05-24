package com.kaliumstudios.mekanismcustommachines.impl.tile;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kaliumstudios.mekanismcustommachines.api.definition.FluidToFluidDefinition;

import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Generic tile entity for Fluid→Fluid custom machines.
 * <p>
 * No direct Mekanism single-block analog exists for this shape (the
 * {@code EVAPORATING} recipe-type is normally consumed by the multi-block
 * Thermal Evaporation Plant). This tile gives plain non-heated single-block
 * fluid processing with the standard four-slot pattern: fluid-fill bucket
 * slot → input tank → output tank → fluid-drain bucket slot → energy.
 */
public class GenericFluidToFluidTile extends TileEntityProgressMachine<FluidToFluidRecipe>
        implements FluidRecipeLookupHandler<FluidToFluidRecipe> {

    private static final ThreadLocal<FluidToFluidDefinition> CURRENT_DEFINITION = new ThreadLocal<>();

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
            RecipeError.NOT_ENOUGH_ENERGY,
            RecipeError.NOT_ENOUGH_INPUT,
            RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
            RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    private final RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> recipeType;
    private final Holder<Block> machine;
    private final FluidToFluidDefinition definition;

    public BasicFluidTank inputTank;
    public BasicFluidTank outputTank;

    private final IOutputHandler<@NotNull FluidStack> outputHandler;
    private final IInputHandler<@NotNull FluidStack> inputHandler;

    private MachineEnergyContainer<GenericFluidToFluidTile> energyContainer;
    private FluidInventorySlot inputBucketSlot;
    private OutputInventorySlot outputBucketSlot;
    private FluidInventorySlot outputDrainSlot;
    private OutputInventorySlot drainedSlot;
    private EnergyInventorySlot energySlot;

    private RVRecipeTypeWrapper<?, FluidToFluidRecipe, ?> recipeViewerType;

    public GenericFluidToFluidTile(
            BlockPos pos,
            BlockState state,
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> recipeType,
            FluidToFluidDefinition definition) {
        super(machine, pos, state, TRACKED_ERROR_TYPES, definition.baseTicksRequired());
        this.recipeType = recipeType;
        this.machine = machine;
        this.definition = definition;

        configComponent.setupItemIOConfig(
                List.of(inputBucketSlot, outputDrainSlot),
                List.of(outputBucketSlot, drainedSlot),
                energySlot, false);
        configComponent.setupIOConfig(TransmissionType.FLUID, inputTank, outputTank, RelativeSide.FRONT, false, true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.FLUID)
                .setCanTankEject(tank -> tank != inputTank);

        inputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    public static BlockEntitySupplier<GenericFluidToFluidTile> factory(
            Holder<Block> machine,
            RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> recipeType,
            FluidToFluidDefinition definition) {
        return (pos, state) -> {
            CURRENT_DEFINITION.set(definition);
            try {
                return new GenericFluidToFluidTile(pos, state, machine, recipeType, definition);
            } finally {
                CURRENT_DEFINITION.remove();
            }
        };
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        FluidToFluidDefinition def = CURRENT_DEFINITION.get();
        int capacity = def != null ? def.maxFluid() : 10_000;
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(inputTank = BasicFluidTank.input(capacity, this::containsRecipe, recipeCacheListener));
        builder.addTank(outputTank = BasicFluidTank.output(capacity, recipeCacheUnpauseListener));
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
        builder.addSlot(inputBucketSlot = FluidInventorySlot.fill(inputTank, listener, 26, 17));
        builder.addSlot(outputBucketSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 26, 53));
        builder.addSlot(outputDrainSlot = FluidInventorySlot.drain(outputTank, listener, 134, 17));
        builder.addSlot(drainedSlot = OutputInventorySlot.at(recipeCacheUnpauseListener, 134, 53));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 152, 35));
        inputBucketSlot.setSlotType(ContainerSlotType.INPUT);
        inputBucketSlot.setSlotOverlay(SlotOverlay.PLUS);
        outputDrainSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputDrainSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputBucketSlot.drainTank(outputBucketSlot);
        outputDrainSlot.fillTank(drainedSlot);
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> getRecipeType() {
        return recipeType;
    }

    @Nullable
    @Override
    public FluidToFluidRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<FluidToFluidRecipe> createNewCachedRecipe(@NotNull FluidToFluidRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.fluidToFluid(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(this::canFunction)
                .setActive(this::setActive)
                .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public IRecipeViewerRecipeType<FluidToFluidRecipe> recipeViewerType() {
        if (recipeViewerType == null) {
            var layout = definition.viewerLayout();
            recipeViewerType = new RVRecipeTypeWrapper<>(
                    recipeType,
                    FluidToFluidRecipe.class,
                    layout.xOffset(),
                    layout.yOffset(),
                    layout.width(),
                    layout.height(),
                    (ItemLike) machine);
        }
        return recipeViewerType;
    }

    public MachineEnergyContainer<GenericFluidToFluidTile> getEnergyContainer() {
        return energyContainer;
    }
}
