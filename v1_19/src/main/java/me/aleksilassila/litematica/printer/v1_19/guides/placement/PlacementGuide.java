package me.aleksilassila.litematica.printer.v1_19.guides.placement;

import me.aleksilassila.litematica.printer.v1_19.LitematicaMixinMod;
import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_19.actions.Action;
import me.aleksilassila.litematica.printer.v1_19.actions.PrepareAction;
import me.aleksilassila.litematica.printer.v1_19.actions.ReleaseShiftAction;
import me.aleksilassila.litematica.printer.v1_19.guides.Guide;
import me.aleksilassila.litematica.printer.v1_19.implementation.actions.InteractActionImpl;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Guide that clicks its neighbors to create a placement in target position.
 */
abstract public class PlacementGuide extends Guide {
    public PlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected ItemStack getBlockItem(BlockState state) {
        if(!state.getFluidState().isEmpty()) return state.getFluidState().getFluid().getBucketItem().getDefaultStack();
        return state.getBlock().getPickStack(this.state.world, this.state.blockPos, state);
    }

    protected Optional<Block> getRequiredItemAsBlock(ClientPlayerEntity player) {
        Optional<ItemStack> requiredItem = getRequiredItem(player);

        if (requiredItem.isEmpty()) {
            return Optional.empty();
        } else {
            ItemStack itemStack = requiredItem.get();

            if (itemStack.getItem() instanceof BlockItem)
                return Optional.of(((BlockItem) itemStack.getItem()).getBlock());
            else return Optional.empty();
        }
    }

    @Override
    protected @NotNull List<ItemStack> getRequiredItems() {
        return Collections.singletonList(getBlockItem(state.targetState));
    }

    abstract protected boolean getUseShift(SchematicBlockState state);

    @Nullable
    abstract public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player);

    @Override
    public boolean canExecute(ClientPlayerEntity player) {
        if (!super.canExecute(player)) return false;

        List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.stream().allMatch(i -> i.isOf(Items.AIR)))
            return false;

        ItemPlacementContext ctx = getPlacementContext(player);
        if (ctx == null || !ctx.canPlace()) return false;
//        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (!LitematicaMixinMod.REPLACE_FLUIDS_SOURCE_BLOCKS.getBooleanValue()
                && getProperty(state.currentState, FluidBlock.LEVEL).orElse(1) == 0)
            return false;

        BlockState resultState = getRequiredItemAsBlock(player)
                .orElse(targetState.getBlock())
                .getPlacementState(ctx);

        if (resultState != null) {
            if (!resultState.canPlaceAt(state.world, state.blockPos)) return false;
            return !(currentState.getBlock() instanceof FluidBlock) || canPlaceInWater(resultState);
        } else {
            return false;
        }
    }

    @Override
    public List<Action> execute(ClientPlayerEntity player) {
        PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null) return null;

        List<Action> actions = new ArrayList<>();
        actions.add(new PrepareAction(ctx));
        actions.add(new InteractActionImpl(ctx));
        if (ctx.shouldSneak) actions.add(new ReleaseShiftAction());

        return actions;
    }

    protected static boolean canBeClicked(World world, BlockPos pos) {
        return getOutlineShape(world, pos) != VoxelShapes.empty() && !(world.getBlockState(pos).getBlock() instanceof AbstractSignBlock); // FIXME signs
    }

    private static VoxelShape getOutlineShape(World world, BlockPos pos) {
        return world.getBlockState(pos).getOutlineShape(world, pos);
    }

    public boolean isInteractive(Block block) {
        for (Class<?> clazz : interactiveBlocks) {
            if (clazz.isInstance(block)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceInWater(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof FluidFillable) {
            return true;
        } else if (!(block instanceof DoorBlock) && !(blockState.getBlock() instanceof AbstractSignBlock) && !blockState.isOf(Blocks.LADDER) && !blockState.isOf(Blocks.SUGAR_CANE) && !blockState.isOf(Blocks.BUBBLE_COLUMN)) {
            Material material = blockState.getMaterial();
            if (material != Material.PORTAL && material != Material.STRUCTURE_VOID && material != Material.UNDERWATER_PLANT && material != Material.REPLACEABLE_UNDERWATER_PLANT) {
                return material.blocksMovement();
            } else {
                return true;
            }
        }

        return true;
    }
}
