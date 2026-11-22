package com.example.basescout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Passive scan of chunks already loaded on the client. Sends nothing to the server. */
public final class ScanLogic {
    private ScanLogic() {}

    public record Hotspot(int x, int z, int score, int distance) {}

    /** Result of the most recent scan, shown by the screen. */
    public static List<Hotspot> lastResults = new ArrayList<>();
    public static String lastStatus = "Press Scan to look for bases.";
    public static int minScore = 4;

    private static int weight(BlockEntity be) {
        if (be instanceof BeaconBlockEntity) return 6;
        if (be instanceof EnchantingTableBlockEntity) return 3;
        if (be instanceof EnderChestBlockEntity) return 3;
        if (be instanceof ShulkerBoxBlockEntity) return 3;
        if (be instanceof ChestBlockEntity) return 2;
        if (be instanceof BarrelBlockEntity) return 2;
        if (be instanceof AbstractFurnaceBlockEntity) return 2;
        if (be instanceof BrewingStandBlockEntity) return 2;
        if (be instanceof HopperBlockEntity) return 1;
        if (be instanceof SignBlockEntity) return 1;
        return 0;
    }

    private static long key(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
