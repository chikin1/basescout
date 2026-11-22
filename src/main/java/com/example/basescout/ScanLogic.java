package com.example.basescout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
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
        if (be instanceof BedBlockEntity) return 3;
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
    }

    /** Runs a scan and stores the outcome in lastResults / lastStatus. */
    public static void run() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        lastResults = new ArrayList<>();
        if (level == null || player == null) {
            lastStatus = "Join a world first.";
            return;
        }

        int radius = mc.options.renderDistance().get();
        int pcx = player.blockPosition().getX() >> 4;
        int pcz = player.blockPosition().getZ() >> 4;

        Map<Long, Integer> chunkScores = new HashMap<>();
        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                int score = 0;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    score += weight(be);
                }
                if (score > 0) chunkScores.put(key(cx, cz), score);
            }
        }

        List<int[]> cells = new ArrayList<>(); // {cx, cz, sum}
        for (long k : chunkScores.keySet()) {
            int cx = (int) (k >> 32);
            int cz = (int) k;
            int sum = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    sum += chunkScores.getOrDefault(key(cx + dx, cz + dz), 0);
                }
            }
            if (sum >= minScore) cells.add(new int[] {cx, cz, sum});
        }
        cells.sort((a, b) -> Integer.compare(b[2], a[2]));

        List<int[]> picked = new ArrayList<>();
        for (int[] c : cells) {
            boolean tooClose = false;
            for (int[] p : picked) {
                if (Math.max(Math.abs(p[0] - c[0]), Math.abs(p[1] - c[1])) <= 3) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) picked.add(c);
            if (picked.size() >= 6) break;
        }

        int px = player.blockPosition().getX();
        int pz = player.blockPosition().getZ();
        for (int[] c : picked) {
            int x = c[0] * 16 + 8;
            int z = c[1] * 16 + 8;
            int dist = (int) Math.sqrt((double) (x - px) * (x - px) + (double) (z - pz) * (z - pz));
            lastResults.add(new Hotspot(x, z, c[2], dist));
        }

        int side = radius * 2 + 1;
        lastStatus = lastResults.isEmpty()
            ? "Nothing base-like in " + side + "x" + side + " chunks. Move around and rescan."
            : "Scanned " + side + "x" + side + " chunks. Higher score = more storage/beds clustered.";
    }
}
