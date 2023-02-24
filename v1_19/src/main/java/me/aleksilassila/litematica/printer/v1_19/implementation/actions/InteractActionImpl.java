package me.aleksilassila.litematica.printer.v1_19.implementation.actions;

import me.aleksilassila.litematica.printer.v1_19.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.v1_19.actions.InteractAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InteractActionImpl extends InteractAction {
    public InteractActionImpl(PrinterPlacementContext context) {
        super(context);
    }

    @Override
    protected void interact(MinecraftClient client, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockPos rpos = hitResult.getBlockPos().offset(hitResult.getSide());
        if(client.getNetworkHandler() == null) return;
        client.getNetworkHandler().getConnection().send(new UpdateSelectedSlotC2SPacket(player.getInventory().selectedSlot));
        client.getNetworkHandler().getConnection().send(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));
        ItemStack holding = player.getInventory().getMainHandStack();
        if(holding.getItem() instanceof BucketItem bi) {
            client.getNetworkHandler().getConnection().send(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            bi.placeFluid(player, client.world, rpos, hitResult);
        }
    }
}
