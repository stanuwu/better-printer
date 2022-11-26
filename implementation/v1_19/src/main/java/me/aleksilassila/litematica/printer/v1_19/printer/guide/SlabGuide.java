package me.aleksilassila.litematica.printer.v1_19.printer.guide;

import me.aleksilassila.litematica.printer.v1_19.printer.SchematicBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class SlabGuide extends PlacementGuide {
    @Override
    protected List<Direction> getPossibleSides(SchematicBlockState state, BlockState currentState, BlockState targetState) {
        return Arrays.stream(Direction.values()).filter(d -> d != (getRequiredHalf(state).getOpposite())).toList();
//        if (state.currentState.contains(SlabBlock.TYPE)) {
//        } else {
//            return Arrays.asList(Direction.values());
//        }
    }

    @Override
    protected Vec3d getHitModifier(SchematicBlockState state, Direction validSide) {
        Direction requiredHalf = getRequiredHalf(state);
        if (validSide.getHorizontal() != -1) {
            return new Vec3d(0, requiredHalf.getOffsetY() * 0.25, 0);
        } else {
            return new Vec3d(0, 0, 0);
        }
    }

    private Direction getRequiredHalf(SchematicBlockState state) {
        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        if (!currentState.contains(SlabBlock.TYPE)) {
            return targetState.get(SlabBlock.TYPE) == SlabType.TOP ? Direction.UP : Direction.DOWN;
        } else if (currentState.get(SlabBlock.TYPE) != targetState.get(SlabBlock.TYPE)) {
            return currentState.get(SlabBlock.TYPE) == SlabType.TOP ? Direction.DOWN : Direction.UP;
        } else {
            return Direction.DOWN;
        }
    }
}
